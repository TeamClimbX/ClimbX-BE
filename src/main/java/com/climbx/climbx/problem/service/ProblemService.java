package com.climbx.climbx.problem.service;

import com.climbx.climbx.common.enums.ActiveStatusType;
import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.common.exception.InvalidParameterException;
import com.climbx.climbx.common.service.S3Service;
import com.climbx.climbx.common.util.RatingUtil;
import com.climbx.climbx.gym.entity.GymAreaEntity;
import com.climbx.climbx.gym.entity.GymEntity;
import com.climbx.climbx.gym.repository.GymAreaRepository;
import com.climbx.climbx.problem.dto.ContributionRequestDto;
import com.climbx.climbx.problem.dto.ContributionResponseDto;
import com.climbx.climbx.problem.dto.ProblemCreateRequestDto;
import com.climbx.climbx.problem.dto.ProblemCreateResponseDto;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.problem.entity.ContributionEntity;
import com.climbx.climbx.problem.entity.ContributionTagEntity;
import com.climbx.climbx.problem.entity.ProblemEntity;
import com.climbx.climbx.problem.entity.ProblemTagEntity;
import com.climbx.climbx.problem.enums.ProblemTagType;
import com.climbx.climbx.problem.enums.ProblemTierType;
import com.climbx.climbx.problem.exception.ForbiddenProblemVoteException;
import com.climbx.climbx.problem.exception.GymAreaNotFoundException;
import com.climbx.climbx.problem.repository.ContributionRepository;
import com.climbx.climbx.problem.repository.ContributionTagRepository;
import com.climbx.climbx.problem.repository.ProblemRepository;
import com.climbx.climbx.problem.repository.ProblemTagRepository;
import com.climbx.climbx.submission.entity.SubmissionEntity;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProblemService {

    private final UserAccountRepository userAccountRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final GymAreaRepository gymAreaRepository;
    private final ContributionRepository contributionRepository;
    private final ContributionTagRepository contributionTagRepository;
    private final ProblemTagRepository problemTagRepository;
    private final RatingUtil ratingUtil;
    private final S3Service s3Service;

    public List<ProblemInfoResponseDto> getProblemsWithFilters(
        Long gymId,
        Long gymAreaId,
        String localLevel,
        String holdColor,
        ProblemTierType problemTier,
        ActiveStatusType activeStatus
    ) {
        // 필터링된 문제들 조회
        return problemRepository.findByGymAndAreaAndLevelAndColorAndProblemTierAndActiveStatus(
            gymId, gymAreaId, localLevel, holdColor, problemTier, activeStatus);
    }

    @Transactional
    public ProblemCreateResponseDto registerProblem(
        ProblemCreateRequestDto request,
        MultipartFile problemImage
    ) {
        log.info("Creating problem: gymAreaId={}, localLevel={}, holdColor={}",
            request.gymAreaId(), request.localLevel(), request.holdColor());

        // GymArea 조회
        GymAreaEntity gymArea = gymAreaRepository.findById(request.gymAreaId())
            .orElseThrow(() -> new GymAreaNotFoundException(request.gymAreaId()));

        // Gym 정보는 GymArea를 통해 가져옴
        GymEntity gym = gymArea.gym();

        if (problemImage == null || problemImage.isEmpty()) {
            throw new InvalidParameterException(
                ErrorCode.INVALID_PARAMETER, "Problem image must not be null or empty");
        }

        // 이미지 업로드 처리
        UUID problemId = UUID.randomUUID();
        String imageCdnUrl = s3Service.uploadProblemImage(problemId, gymArea.gymAreaId(),
            problemImage);

        // Problem 엔티티 생성
        ProblemEntity problem = ProblemEntity.builder()
            .problemId(problemId)
            .gymEntity(gym)
            .gymArea(gymArea)
            .localLevel(request.localLevel())
            .holdColor(request.holdColor())
            // Todo .rating()
            .problemImageCdnUrl(imageCdnUrl)
            .activeStatus(ActiveStatusType.ACTIVE)
            .build();

        ProblemEntity savedProblem = problemRepository.save(problem);

        log.info(
            "Problem created successfully: problemId={}, localLevel={}, holdColor={}, imageCdnUrl={}",
            savedProblem.problemId(), savedProblem.localLevel(), savedProblem.holdColor(),
            imageCdnUrl);

        return ProblemCreateResponseDto.from(savedProblem);
    }

    @Transactional
    public ProblemInfoResponseDto voteProblem(
        Long userId,
        UUID problemId,
        ContributionRequestDto voteRequest
    ) {
        log.info("난이도 투표 요청: userId={}, problemId={}, vote={}",
            userId, problemId, voteRequest);

        UserAccountEntity user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        SubmissionEntity submission = submissionRepository.findByProblemIdAndVideoEntity_UserIdAndStatus(
            problemId,
            userId,
            StatusType.ACCEPTED
        ).orElseThrow(() -> new ForbiddenProblemVoteException(problemId, userId));

        ProblemEntity problem = submission.problemEntity();

        ContributionEntity contribution = ContributionEntity.builder()
            .userAccountEntity(user)
            .problemEntity(problem)
            .tier(voteRequest.tier())
            .comment(voteRequest.comment())
            .build();

        contributionRepository.save(contribution);

        if (voteRequest.tags() != null) {
            voteRequest.tags().forEach(tag -> {
                contributionTagRepository.save(
                    ContributionTagEntity.builder()
                        .contributionEntity(contribution)
                        .tag(tag)
                        .build()
                );

                ProblemTagEntity problemTag = problemTagRepository.findByProblemEntityAndTag(
                        problem, tag)
                    .orElseGet(() -> problemTagRepository.save(
                        ProblemTagEntity.builder()
                            .problemEntity(problem)
                            .tag(tag)
                            .build()
                    ));

                problemTag.addPriority(1); // TODO: 추후 유저 레이팅에 따른 영향력 설계 필요
            });
        }

        Integer newProblemRating = ratingUtil.calculateProblemTier(
            contributionRepository.findAllByProblemEntity_ProblemId(problem.problemId())
                .stream()
                .map(ContributionEntity::toVoteTierDto)
                .toList()
        );

        ProblemTierType newProblemTier = ProblemTierType.fromValue(newProblemRating);

        List<ProblemTagType> primary2tags = problemTagRepository
            .findTop2ByProblemEntityOrderByPriorityDesc(problem)
            .stream()
            .map(ProblemTagEntity::tag)
            .toList();

        problem.updateRatingAndTierAndTags(
            newProblemRating,
            newProblemTier,
            primary2tags
        );

        return ProblemInfoResponseDto.from(problem, problem.gymEntity(), problem.gymArea());
    }

    public List<ContributionResponseDto> getProblemVotes(
        UUID problemId,
        Pageable pageable
    ) {
        List<ContributionEntity> contributions = contributionRepository
            .findAllByProblemEntity_ProblemIdOrderByCreatedAtDesc(problemId, pageable);
        return contributions.stream()
            .map(ContributionResponseDto::from)
            .toList();
    }
}
