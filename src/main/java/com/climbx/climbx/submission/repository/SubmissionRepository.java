package com.climbx.climbx.submission.repository;

import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.problem.entity.ProblemEntity;
import com.climbx.climbx.submission.entity.SubmissionEntity;
import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Long> {


    /**
     * 사용자가 푼(accepted=true) Submission 에서 video.userAccount.userId = :userId 인 것들만 추리고, 그 안의
     * Problem(p) 을 DISTINCT 하여 p.rating DESC 순으로 정렬한 뒤 Pageable 로 페이지(=상위 N개) 리미트
     */
    @Query("""
        SELECT DISTINCT s.problemEntity
          FROM SubmissionEntity s
          JOIN s.videoEntity v
         WHERE v.userId = :userId
           AND s.status = :status
        """)
    List<ProblemEntity> getUserSubmissionProblems(
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
}
