package com.climbx.climbx.user.repository;

import com.climbx.climbx.user.dto.DailyHistoryResponseDto;
import com.climbx.climbx.user.entity.UserRankingHistoryEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRankingHistoryRepository extends
    JpaRepository<UserRankingHistoryEntity, Long> {

    /**
     * 사용자가 특정 기간 동안 특정 criteria에 대한 일별 히스토리를 조회
     */
    @Query("""
        SELECT new com.climbx.climbx.user.dto.DailyHistoryResponseDto(
            DATE(h.createdAt),
            SUM(h.value)
        )
          FROM UserRankingHistoryEntity h
         WHERE h.userId = :userId
           AND h.part = :criteria
           AND (:from IS NULL OR DATE(h.createdAt) >= :from)
           AND (:to IS NULL OR DATE(h.createdAt) <= :to)
            GROUP BY DATE(h.createdAt)
         ORDER BY DATE(h.createdAt) ASC
        """)
    List<DailyHistoryResponseDto> getUserDailyHistory(
        @Param("userId") Long userId,
        @Param("criteria") String criteria,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );
}
