package com.climbx.climbx.user.entity;

import com.climbx.climbx.common.entity.BaseTimeEntity;
import com.climbx.climbx.user.enums.CriteriaType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "user_ranking_histories")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class UserRankingHistoryEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id", updatable = false, nullable = false)
    @NotNull
    private Long historyId; // 히스토리 ID (Primary Key)

    @Column(name = "criteria", length = 32, nullable = false)
    @NotNull
    private CriteriaType criteria; // 히스토리 종류

    @Column(name = "user_id", insertable = false, updatable = false, nullable = false)
    @NotNull
    @Min(1L)
    private Long userId; // 사용자 ID (Foreign Key)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccountEntity userAccountEntity; // 사용자 계정과의 관계

    @Column(name = "value", nullable = false)
    @NotNull
    private Integer value; // 히스토리 값 (레이팅, 스트릭 등의 수치)
}