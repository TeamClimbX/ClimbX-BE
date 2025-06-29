package com.climbx.climbx.user.entity;

import com.climbx.climbx.common.entity.BaseTimeEntity;
import com.climbx.climbx.common.enums.UserHistoryCriteriaType;
import com.climbx.climbx.video.entity.VideoEntity;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "user_ranking_histories")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "part", length = 20, nullable = false)
    @NotNull
    private UserHistoryCriteriaType part; // 히스토리 타입

    @Column(name = "user_id", insertable = false, updatable = false, nullable = false)
    @NotNull
    private Long userId; // 사용자 ID (Foreign Key)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccountEntity userAccountEntity; // 사용자 계정과의 관계

    @Column(name = "value", nullable = false)
    @NotNull
    private Long value; // 히스토리 값 (레이팅, 스트릭 등의 수치)
}