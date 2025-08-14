package com.climbx.climbx.problem.entity;

import com.climbx.climbx.common.entity.BaseTimeEntity;
import com.climbx.climbx.problem.enums.ProblemTagType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "contribution_tag")
@IdClass(ContributionTagId.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class ContributionTagEntity extends BaseTimeEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribution_id", nullable = false)
    private ContributionEntity contributionEntity;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "tag", columnDefinition = "varchar(32)", nullable = false) // updatable은 추후 논의 필요
    private ProblemTagType tag;
}
