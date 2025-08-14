package com.climbx.climbx.submission.entity;

import com.climbx.climbx.common.entity.BaseTimeEntity;
import com.climbx.climbx.common.enums.StatusType;
import com.climbx.climbx.problem.entity.ProblemEntity;
import com.climbx.climbx.video.entity.VideoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "submissions")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class SubmissionEntity extends BaseTimeEntity {

    @Id
    @Column(name = "video_id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID videoId; // 비디오 ID, VideoEntity와 동일한 ID 사용

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", columnDefinition = "BINARY(16)")
    private VideoEntity videoEntity; // 비디오 엔티티

    @Column(name = "problem_id", columnDefinition = "BINARY(16)", insertable = false, updatable = false, nullable = false)
    private UUID problemId; // 문제 ID, ProblemEntity와 동일한 ID 사용

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", columnDefinition = "BINARY(16)")
    private ProblemEntity problemEntity; // 문제 엔티티

    @Column(name = "status", columnDefinition = "varchar(32)", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusType status; // 제출 상태, 예: PENDING, ACCEPTED, REJECTED 등

    @Column(name = "reject_reason", length = 256, nullable = true)
    @Size(max = 256)
    @Builder.Default
    private String statusReason = null; // 거절 사유, nullable

    @Column(name = "appeal_content", length = 256, nullable = true)
    @Size(max = 256)
    @Builder.Default
    private String appealContent = null; // 항의 내용, nullable

    @Column(name = "appeal_status", columnDefinition = "varchar(32)")
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private StatusType appealStatus = null; // 항소 상태, 예: PENDING, ACCEPTED, REJECTED 등

    public void setAppealContent(String appealContent) {
        this.appealStatus = StatusType.PENDING;
        this.appealContent = appealContent;
    }

    public void setStatus(StatusType status, String statusReason) {
        this.status = status;
        this.statusReason = statusReason;
    }
}