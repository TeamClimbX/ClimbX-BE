package com.climbx.climbx.problem.entity;

import com.climbx.climbx.common.entity.BaseTimeEntity;
import com.climbx.climbx.common.enums.ActiveStatusType;
import com.climbx.climbx.gym.entity.GymAreaEntity;
import com.climbx.climbx.gym.entity.GymEntity;
import com.climbx.climbx.gym.enums.GymTierType;
import com.climbx.climbx.problem.enums.HoldColorType;
import com.climbx.climbx.problem.enums.ProblemTagType;
import com.climbx.climbx.problem.enums.ProblemTierType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "problems")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class ProblemEntity extends BaseTimeEntity {

    @Id
    @Column(name = "problem_id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID problemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private GymEntity gymEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_area_id", nullable = false)
    private GymAreaEntity gymArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "local_level", columnDefinition = "varchar(32)", nullable = false)
    private GymTierType localLevel; // 클라이밍장별 레벨, 예: "빨강", "파랑", "초록" 등

    @Enumerated(EnumType.STRING)
    @Column(name = "hold_color", columnDefinition = "varchar(32)", nullable = false)
    private HoldColorType holdColor; // 홀드 색상, 예: "빨강", "파랑", "초록" 등

    @Builder.Default
    @Column(name = "problem_rating", nullable = false)
    @Min(value = 0)
    @Max(value = 30)
    private Integer rating = 0; // 문제 난이도

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_tier", columnDefinition = "varchar(16)", nullable = false)
    private ProblemTierType tier;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_tag", columnDefinition = "varchar(16)")
    private ProblemTagType primaryTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_tag", columnDefinition = "varchar(16)")
    private ProblemTagType secondaryTag;

    @Column(name = "problem_image_cdn_url", length = 512)
    @Size(max = 512)
    private String problemImageCdnUrl; // 문제 이미지 CDN URL

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status", columnDefinition = "varchar(16)", nullable = false)
    private ActiveStatusType activeStatus; // 문제 상태 (예: 활성화, 비활성화 등)

    public void updateRatingAndTierAndTags(
        Integer newRating,
        ProblemTierType newTier,
        List<ProblemTagType> newTags
    ) {
        this.rating = newRating;
        this.tier = newTier;
        this.primaryTag = !newTags.isEmpty() ? newTags.getFirst() : null;
        this.secondaryTag = newTags.size() > 1 ? newTags.get(1) : null;
    }
}