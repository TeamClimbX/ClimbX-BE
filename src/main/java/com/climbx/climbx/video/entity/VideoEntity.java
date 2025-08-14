package com.climbx.climbx.video.entity;

import com.climbx.climbx.common.entity.BaseTimeEntity;
import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.submission.entity.SubmissionEntity;
import com.climbx.climbx.user.entity.UserAccountEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "videos")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class VideoEntity extends BaseTimeEntity {

    @Id
    @Column(name = "video_id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID videoId;

    @Column(name = "user_id", insertable = false, updatable = false, nullable = false)
    private Long userId; // 사용자 ID, UserAccountEntity와 동일한 ID 사용

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccountEntity userAccountEntity;

    @OneToOne(mappedBy = "videoEntity", fetch = FetchType.LAZY)
    private SubmissionEntity submissionEntity; // 비디오 제출 엔티티

    @Column(name = "file_size", nullable = false)
    @NotNull
    @Min(value = 0)
    @Max(value = 1024 * 1024 * 1024) // 최대 1GB
    private Long fileSize; // 비디오 파일 크기 (바이트 단위)

    @Column(name = "original_s3_url", length = 512)
    @Size(min = 1, max = 512)
    private String originalS3Url; // 원본 S3 URL

    @Column(name = "hls_s3_url", length = 512)
    @Size(min = 1, max = 512)
    private String hlsS3Url; // HLS 스트리밍 S3 URL

    @Column(name = "thumbnail_s3_url", length = 512)
    @Size(min = 1, max = 512)
    private String thumbnailS3Url; // 썸네일 S3 URL

    @Column(name = "hls_cdn_url", length = 512)
    @Size(min = 1, max = 512)
    private String hlsCdnUrl; // HLS 스트리밍 CDN URL

    @Column(name = "thumbnail_cdn_url", length = 512)
    @Size(min = 1, max = 512)
    private String thumbnailCdnUrl; // 썸네일 CDN URL

    @Column(name = "duration_seconds")
    @Min(value = 0)
    private Integer durationSeconds; // 비디오 길이 (초 단위)

    @Column(name = "job_id", length = 256)
    @Size(min = 1, max = 256)
    private String jobId; // AWS MediaConvert 변환 작업 ID

    @Column(name = "status", columnDefinition = "varchar(16)", nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusType status; // 비디오 변환 상태, 예: PENDING, COMPLETED, FAILED 등

    @Column(name = "processed_at")
    private LocalDateTime processedAt; // 비디오 변환 완료 시간
}
