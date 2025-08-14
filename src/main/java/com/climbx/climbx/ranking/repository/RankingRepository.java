package com.climbx.climbx.ranking.repository;

import com.climbx.climbx.common.enums.RoleType;
import com.climbx.climbx.user.entity.UserStatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankingRepository extends JpaRepository<UserStatEntity, Long> {

    @Query("""
        SELECT u FROM UserStatEntity u
                JOIN FETCH u.userAccountEntity ua
                WHERE ua.role = :role
        """)
    Page<UserStatEntity> findAllByUserRole(Pageable pageable, @Param("role") RoleType role);
}
