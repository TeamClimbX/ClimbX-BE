package com.climbx.climbx.auth.entity;

import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import com.climbx.climbx.common.entity.BaseTimeEntity;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
    name = "user_auths",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_auths_provider_id",
            columnNames = {"provider", "provider_id"}
        ),
        @UniqueConstraint(
            name = "uk_user_auths_user_provider",
            columnNames = {"user_id", "provider"}
        )
    }
)
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class UserAuthEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id", updatable = false, nullable = false)
    private Long authId; // 인증 정보 ID (Primary Key)

    @Column(name = "user_id", insertable = false, updatable = false, nullable = false)
    private Long userId; // 사용자 ID (조회용)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private UserAccountEntity userAccountEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 32, nullable = false)
    @NotNull
    private OAuth2ProviderType provider; // OAuth2 provider (e.g. KAKAO)

    @Column(name = "provider_id", length = 128, nullable = false)
    @NotBlank
    @Size(max = 128)
    private String providerId; // provider 고유 user ID

    @Column(name = "provider_email", length = 128, nullable = true)
    @Email
    @Size(max = 128)
    private String providerEmail; // provider email

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false; // 주 인증 수단인지 true, false

    public void markAsPrimary() {
        this.isPrimary = true;
    }

    public void unmarkAsPrimary() {
        this.isPrimary = false;
    }

    public void updateProviderEmail(String email) {
        this.providerEmail = email;
    }
} 