package com.climbx.climbx.auth.repository;

import com.climbx.climbx.auth.entity.UserAuthEntity;
import com.climbx.climbx.auth.enums.OAuth2ProviderType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAuthRepository extends JpaRepository<UserAuthEntity, Long> {

    /**
     * OAuth2 제공자와 제공자 ID로 사용자 인증 정보를 조회합니다.
     */
    Optional<UserAuthEntity> findByProviderAndProviderId(
        OAuth2ProviderType oauthProvider,
        String oauthProviderId
    );

    /**
     * 특정 사용자의 주 인증 수단을 조회합니다.
     */
    Optional<UserAuthEntity> findByUserIdAndIsPrimaryTrue(Long userId);

    /**
     * 특정 사용자가 특정 제공자로 인증한 정보가 있는지 확인합니다.
     */
    boolean existsByUserIdAndProvider(
        Long userId,
        OAuth2ProviderType oauthProvider
    );

    /**
     * 특정 이메일로 등록된 인증 정보를 조회합니다.
     */
    List<UserAuthEntity> findByProviderEmail(String email);

    /**
     * 특정 사용자의 모든 인증 정보를 조회합니다.
     */
    List<UserAuthEntity> findByUserId(Long userId);

    /**
     * 특정 사용자의 모든 UserAuth를 soft delete 처리합니다.
     */
    @Modifying
    @Query("UPDATE UserAuthEntity u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.userId = :userId AND u.deletedAt IS NULL")
    int softDeleteAllByUserId(@Param("userId") Long userId);
} 