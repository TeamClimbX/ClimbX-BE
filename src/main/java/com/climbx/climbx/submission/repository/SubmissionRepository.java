package com.climbx.climbx.submission.repository;

import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.problem.dto.TagRatingPairDto;
import com.climbx.climbx.submission.entity.SubmissionEntity;
import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@SQLRestriction("deleted_at IS NULL")
public interface SubmissionRepository extends JpaRepository<SubmissionEntity, UUID> {

    /**
     * 사용자가 푼(accepted=true) Submission 에서 video.userAccount.userId = :userId 인 것들만 추린 뒤 gymArea를
     * fetch join함. 그 안의 Problem(p) 을 DISTINCT 하여 p.rating DESC 순으로 정렬한 뒤 Pageable 로 페이지(=상위 N개)
     * 리미트
     */
    @Query("""
        SELECT DISTINCT new com.climbx.climbx.problem.dto.ProblemInfoResponseDto(
            p.problemId,
            g.gymId,
            g.name,
            ga.gymAreaId,
            ga.areaName,
            p.localLevel,
            p.holdColor,
            p.tier,
            p.rating,
            p.problemImageCdnUrl,
            p.activeStatus,
            p.createdAt
        )
        FROM SubmissionEntity s
        JOIN s.videoEntity v
        JOIN s.problemEntity p
        JOIN p.gymEntity g
        JOIN p.gymArea ga
        WHERE v.userId = :userId
          AND s.status = :status
        ORDER BY p.rating DESC
        """)
    List<ProblemInfoResponseDto> getUserTopProblems(
        @Param("userId") Long userId,
        @Param("status") StatusType status,
        Pageable pageable
    );

    /**
     * 사용자가 특정 기간 동안 일별로 푼 문제 수를 조회 from, to가 null이면 모든 기간
     */
    @Query("""
        SELECT new com.climbx.climbx.user.dto.DailyHistoryResponseDto(
                DATE(s.createdAt),
                COUNT(DISTINCT s.problemEntity.problemId)
            )
          FROM SubmissionEntity s
          JOIN s.videoEntity v
         WHERE v.userId = :userId
           AND s.status = :status
           AND (:from is NUll OR DATE(s.createdAt) >= :from)
           AND (:to is NULL OR DATE(s.createdAt) <= :to)
         GROUP BY DATE(s.createdAt)
         ORDER BY DATE(s.createdAt) ASC
        """)
    List<DailyHistoryResponseDto> getUserDateSolvedCount(
        @Param("userId") Long userId,
        @Param("status") StatusType status,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    /**
     * 특정 사용자의 모든 제출을 조회합니다 (soft delete 포함).
     */
    @Query("""
        SELECT s FROM SubmissionEntity s
        JOIN s.videoEntity v
        WHERE v.userId = :userId
        """)
    List<SubmissionEntity> findByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 모든 Submission을 soft delete 처리합니다.
     */
    @Modifying
    @Query("""
        UPDATE SubmissionEntity s
        SET s.deletedAt = CURRENT_TIMESTAMP
        WHERE s.videoEntity.userId = :userId
        AND s.deletedAt IS NULL
        """)
    int softDeleteAllByUserId(@Param("userId") Long userId);

    /**
     * 다양한 필터 조건으로 제출 목록을 조회합니다.
     */
    @Query("""
        SELECT s FROM SubmissionEntity s
        JOIN FETCH s.videoEntity v
        JOIN FETCH v.userAccountEntity u
        JOIN FETCH s.problemEntity p
        JOIN FETCH p.gymEntity g
        WHERE (:userId IS NULL OR v.userId = :userId)
          AND (:problemId IS NULL OR p.problemId = :problemId)
          AND (:holdColor IS NULL OR p.holdColor = :holdColor)
          AND (:ratingFrom IS NULL OR p.rating >= :ratingFrom)
          AND (:ratingTo IS NULL OR p.rating <= :ratingTo)
        """)
    Page<SubmissionEntity> findSubmissionsWithFilters(
        @Param("userId") Long userId,
        @Param("problemId") Long problemId,
        @Param("holdColor") String holdColor,
        @Param("ratingFrom") Integer ratingFrom,
        @Param("ratingTo") Integer ratingTo,
        Pageable pageable
    );

    // 1) Primary 만
    @Query("""
          SELECT new com.climbx.climbx.problem.dto.TagRatingPairDto(
                p.primaryTag, p.rating
            )
          FROM SubmissionEntity s
          JOIN s.videoEntity v
          JOIN s.problemEntity p
          WHERE v.userId = :userId
            AND (:accepted IS NULL OR s.status = :accepted)
            AND (p.primaryTag IS NOT NULL)
        """)
    List<TagRatingPairDto> summarizeByPrimary(@Param("userId") Long userId,
        @Param("accepted") StatusType accepted);

    // 2) Secondary 만
    @Query("""
          SELECT new com.climbx.climbx.problem.dto.TagRatingPairDto(
                p.secondaryTag, p.rating
            )
          FROM SubmissionEntity s
          JOIN s.videoEntity v
          JOIN s.problemEntity p
          WHERE v.userId = :userId
            AND (:accepted IS NULL OR s.status = :accepted)
            AND (p.secondaryTag IS NOT NULL)
        """)
    List<TagRatingPairDto> summarizeBySecondary(@Param("userId") Long userId,
        @Param("accepted") StatusType accepted);

    default List<TagRatingPairDto> getUserAcceptedSubmissionTagSummary(
        Long userId,
        StatusType accepted
    ) {
        // accepted 가 null 이면 모든 상태의 Submission 을 대상으로 함
        List<TagRatingPairDto> primaryTags = summarizeByPrimary(userId, accepted);
        List<TagRatingPairDto> secondaryTags = summarizeBySecondary(userId, accepted);

        // Primary 와 Secondary 를 합쳐서 반환
        return Stream.concat(primaryTags.stream(), secondaryTags.stream())
            .toList();
    }

    // ProblemEntity를 fetch join
    @EntityGraph(attributePaths = "problemEntity")
    Optional<SubmissionEntity> findByProblemIdAndVideoEntity_UserIdAndStatus(
        UUID problemId, Long userId, StatusType status
    );
}
