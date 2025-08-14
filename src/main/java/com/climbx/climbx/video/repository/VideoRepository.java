package com.climbx.climbx.video.repository;

import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.video.entity.VideoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@SQLRestriction("deleted_at IS NULL") // soft delete 자동 적용
public interface VideoRepository extends JpaRepository<VideoEntity, UUID> {

    /**
     * 특정 사용자의 특정 상태 비디오를 생성일 역순으로 조회 (@SQLRestriction 자동 적용)
     */
    List<VideoEntity> findByUserIdAndStatusInOrderByCreatedAtDesc(
        Long userId,
        List<StatusType> status
    );

    /**
     * 특정 사용자의 모든 비디오를 조회합니다 (soft delete 포함).
     */
    List<VideoEntity> findByUserId(Long userId);

    /**
     * 특정 상태인 비디오를 조회합니다.
     */
    Optional<VideoEntity> findByVideoIdAndStatus(
        UUID videoId,
        StatusType status
    );

    /**
     * 특정 사용자의 모든 Video를 soft delete 처리합니다.
     */
    @Modifying
    @Query("UPDATE VideoEntity v SET v.deletedAt = CURRENT_TIMESTAMP WHERE v.userId = :userId")
    int softDeleteAllByUserId(@Param("userId") Long userId);
} 
