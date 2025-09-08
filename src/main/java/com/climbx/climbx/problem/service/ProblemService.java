package com.climbx.climbx.problem.service;

import com.climbx.climbx.common.enums.ActiveStatusType;
import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.InvalidParameterException;
import com.climbx.climbx.common.service.S3Service;
import com.climbx.climbx.gym.entity.GymAreaEntity;
import com.climbx.climbx.gym.entity.GymEntity;
import com.climbx.climbx.gym.enums.GymTierType;
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
import com.climbx.climbx.problem.enums.HoldColorType;
import com.climbx.climbx.problem.enums.ProblemTagType;
import com.climbx.climbx.problem.enums.ProblemTierType;
import com.climbx.climbx.problem.exception.GymAreaNotFoundException;
import com.climbx.climbx.problem.exception.ProblemAlreadyDeletedException;
import com.climbx.climbx.problem.exception.ProblemNotFoundException;
import com.climbx.climbx.problem.repository.ContributionRepository;
import com.climbx.climbx.problem.repository.ContributionTagRepository;
import com.climbx.climbx.problem.repository.ProblemRepository;
import com.climbx.climbx.problem.repository.ProblemTagRepository;
import com.climbx.climbx.problem.util.ProblemRatingUtil;
import com.climbx.climbx.submission.repository.SubmissionRepository;
import com.climbx.climbx.user.entity.UserAccountEntity;
import com.climbx.climbx.user.entity.UserStatEntity;
import com.climbx.climbx.user.exception.UserNotFoundException;
import com.climbx.climbx.user.repository.UserAccountRepository;
import com.climbx.climbx.user.util.UserRatingUtil;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
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
    private final ProblemRatingUtil problemRatingUtil;
    private final S3Service s3Service;

    public List<ProblemInfoResponseDto> getProblemsWithFilters(
        Long gymId,
        Long gymAreaId,
        GymTierType localLevel,
        HoldColorType holdColor,
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

        GymTierType localTier = request.localLevel();
        ProblemTierType problemTier = localTier.globalTier();

        // Problem 엔티티 생성
        ProblemEntity problem = ProblemEntity.builder()
            .problemId(problemId)
            .gymEntity(gym)
            .gymArea(gymArea)
            .localLevel(localTier)
            .holdColor(request.holdColor())
            .rating(problemTier.value())
            .tier(problemTier)
            .problemImageCdnUrl(imageCdnUrl)
            .activeStatus(ActiveStatusType.ACTIVE)
            .build();

        ProblemEntity savedProblem = problemRepository.save(problem);

        log.info(
            "Problem created successfully: problemId={}, localLevel={}, holdColor={}, imageCdnUrl={}",
            savedProblem.problemId(), savedProblem.localLevel(), savedProblem.holdColor(),
            imageCdnUrl);

        // 기본 티어 투표 3건 삽입 (user/tag 없이 tier만 설정)
        List<ContributionEntity> defaultVotes = IntStream.range(0, 3)
            .mapToObj(i -> ContributionEntity.builder()
                .problemEntity(savedProblem)
                .tier(problemTier)
                .build())
            .toList();

        contributionRepository.saveAll(defaultVotes);

        

        return ProblemCreateResponseDto.from(savedProblem);
    }

    @Transactional
    public void applyVoteToProblem(
        ProblemEntity problem,
        ContributionEntity contribution,
        List<ContributionTagEntity> votedTags
    ) {
        contribution.accept();

        votedTags.forEach(t -> {
            ProblemTagType tag = t.tag();
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

        // Recalculate problem rating and tier from all contributions
        Integer newProblemRating = problemRatingUtil.calculateProblemTier(
            contributionRepository.findAllByProblemEntity_ProblemId(problem.problemId())
                .stream()
                .filter(ContributionEntity::isAccepted)
                .map(ContributionEntity::toVoteTierDto)
                .toList()
        );

        ProblemTierType newProblemTier = ProblemTierType.fromValue(newProblemRating);

        // Update primary/secondary tags from accumulated priorities
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

        boolean isAlreadyAccepted = submissionRepository.isAcceptedSubmissionExist(
            userId,
            problemId
        );

        ProblemEntity problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new ProblemNotFoundException(problemId));

        ContributionEntity contribution = ContributionEntity.builder()
            .userAccountEntity(user)
            .problemEntity(problem)
            .tier(voteRequest.tier())
            .comment(voteRequest.comment())
            .isAccepted(isAlreadyAccepted)
            .build();

        contributionRepository.save(contribution);

        List<ContributionTagEntity> votedTags = List.of();
        if (voteRequest.tags() != null && !voteRequest.tags().isEmpty()) {
            votedTags = voteRequest.tags().stream()
                .map(tag -> ContributionTagEntity.builder()
                    .contributionEntity(contribution)
                    .tag(tag)
                    .build()
                )
                .toList();
            contributionTagRepository.saveAll(votedTags);
        }

        if (isAlreadyAccepted) {
            log.info("User {} has already accepted problem {}, marking vote as accepted",
                userId, problemId);

            applyVoteToProblem(problem, contribution, votedTags);
        }

        UserStatEntity userStat = user.userStatEntity();

        // 기여 개수 증가 및 기여 점수 증분값 계산
        int prevContributionRating = UserRatingUtil.calculateContributionScore(
            userStat.contributionCount());
        userStat.incrementContributionCount();
        int newContributionRating = UserRatingUtil.calculateContributionScore(
            userStat.contributionCount());

        // 기여에 따른 유저 레이팅 증분 반영
        userStat.incrementRatingByContribution(newContributionRating - prevContributionRating);

        return ProblemInfoResponseDto.from(problem, problem.gymEntity(), problem.gymArea());
    }

    public List<ContributionResponseDto> getProblemVotes(
        UUID problemId,
        Pageable pageable
    ) {
        List<ContributionEntity> contributions = contributionRepository
            .findRecentUserVotes(problemId, pageable);
        return contributions.stream()
            .map(ContributionResponseDto::from)
            .toList();
    }

    @Transactional
    public void softDeleteProblem(UUID problemId) {
        log.info("Soft deleting problem: problemId={}", problemId);

        ProblemEntity problem = problemRepository.findByIdForUpdate(problemId)
            .orElseThrow(() -> new ProblemNotFoundException(problemId));

        if (problem.deletedAt() != null) {
            throw new ProblemAlreadyDeletedException(
                ErrorCode.PROBLEM_ALREADY_DELETED, "Problem has already been soft deleted");
        }
        problem.softDelete();

        log.info("Problem soft deleted successfully: problemId={}", problemId);
    }
}
