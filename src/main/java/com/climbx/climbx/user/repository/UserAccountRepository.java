package com.climbx.climbx.user.repository;

import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.user.entity.UserAccountEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Long> {

    /*
     * 사용자 닉네임으로 조회, 중복 검사 (@SQLRestriction 자동 적용)
     */
    Optional<UserAccountEntity> findByNickname(String nickname);

    boolean existsByNickname(String nickname);

    /*
     * 역할에 관계없이 닉네임 중복 체크 (시스템 전체 닉네임 유일성 보장)
     */
    @Query("SELECT COUNT(u) > 0 FROM UserAccountEntity u WHERE u.nickname = :nickname AND u.deletedAt IS NULL")
    boolean existsByNicknameIgnoringRole(@Param("nickname") String nickname);

    /*
     * 사용자 id로 조회 (@SQLRestriction 자동 적용)
     */
    Optional<UserAccountEntity> findByUserId(Long userId);

    /*
     * 닉네임에 특정 문자열을 포함하는 사용자들 조회 (@SQLRestriction 자동 적용 - USER role만)
     */
    List<UserAccountEntity> findByNicknameContaining(String nickname);
}
