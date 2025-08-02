package com.climbx.climbx.user.repository;

import com.climbx.climbx.user.entity.UserStatEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserStatRepository extends JpaRepository<UserStatEntity, Long> {

    /*
     * 사용자 통계 단일 조회 (@SQLRestriction 자동 적용)
     */

    /**
     * 특정 레이팅을 가진 사용자의 순위(1-based) 조회 (@SQLRestriction 자동 적용)
     */
    @Query("""
        SELECT COUNT(us) + 1
        FROM UserStatEntity us
        WHERE us.rating > :rating
        OR (us.rating = :rating AND us.updatedAt < :updatedAt)
        OR (us.rating = :rating AND us.updatedAt = :updatedAt AND us.userId < :userId)
        """)
    Integer findRankByRatingAndUpdatedAtAndUserId(
        @Param("rating") Integer rating,
        @Param("updatedAt") LocalDateTime updatedAt,
        @Param("userId") Long userId
    );

    Optional<UserStatEntity> findByUserId(Long userId);

    /**
     * 특정 사용자의 UserStat을 soft delete 처리합니다.
     */
    @Modifying
    @Query("UPDATE UserStatEntity u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.userId = :userId AND u.deletedAt IS NULL")
    int softDeleteByUserId(@Param("userId") Long userId);

    // 유저의 랭킹

    /**
     * 최장 스트릭, 해결한 문제 수 기준 사용자의 순위(1-based) 조회
     * TODO: 유저 랭킹 히스토리 또는 프로필에 랭킹 정보 포함 시 구현 필요
     */
//    long countByLongestStreakGreaterThan(Long longestStreak);
//    default Long findLongestStreakRank(Long longestStreak) {
//        return countByLongestStreakGreaterThan(longestStreak) + 1;
//    }
//
//    long countBySolvedProblemsCountGreaterThan(Long solvedCount);
//    default Long findSolvedProblemsCountRank(Long solvedCount) {
//        return countBySolvedProblemsCountGreaterThan(solvedCount) + 1;
//    }
}