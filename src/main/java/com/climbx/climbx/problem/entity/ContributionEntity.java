package com.climbx.climbx.problem.entity;

import com.climbx.climbx.common.entity.BaseTimeEntity;
import com.climbx.climbx.problem.dto.VoteTierDto;
import com.climbx.climbx.problem.enums.ProblemTierType;
import com.climbx.climbx.user.entity.UserAccountEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "contributions")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class ContributionEntity extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    UserAccountEntity userAccountEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    ProblemEntity problemEntity;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "contributionEntity")
    private List<ContributionTagEntity> contributionTags;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contribution_id", nullable = false, updatable = false)
    private Long contributionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", columnDefinition = "varchar(16)", nullable = false)
    private ProblemTierType tier;

    @Column(name = "comment", length = 512) // nullable
    private String comment; // 예시: "문제 어디가 어려웠고, 추천할 만한 문제인지 등

    public static VoteTierDto toVoteTierDto(ContributionEntity c) {
        return VoteTierDto.builder()
            .tier(c.tier())
            .dateTime(c.createdAt())
            .build();
    }
}
