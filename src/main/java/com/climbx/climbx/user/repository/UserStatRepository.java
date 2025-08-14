package com.climbx.climbx.user.repository;

import com.climbx.climbx.user.dto.UserRankingDto;
import com.climbx.climbx.user.entity.UserStatEntity;
import java.time.LocalDateTime;
import java.util.List;
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

    /*
     * 배치 조회: 여러 사용자 ID에 대한 UserStat 조회
     */
    List<UserStatEntity> findByUserIdIn(List<Long> userIds);

    /*
     * 배치 조회: 여러 사용자의 랭킹 정보 조회 (rating 기준)
     */
    @Query("""
        SELECT new com.climbx.climbx.user.dto.UserRankingDto(
            us1.userId,
            CAST((SELECT COUNT(us2) + 1
                FROM UserStatEntity us2
                WHERE us2.rating > us1.rating
                   OR (us2.rating = us1.rating AND us2.updatedAt < us1.updatedAt)
                   OR (us2.rating = us1.rating AND us2.updatedAt = us1.updatedAt AND us2.userId < us1.userId)
               ) as int)
        )
        FROM UserStatEntity us1
        WHERE us1.userId IN :userIds
        """)
    List<UserRankingDto> findRanksByUserIds(@Param("userIds") List<Long> userIds);

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