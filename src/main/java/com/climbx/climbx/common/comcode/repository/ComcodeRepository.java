package com.climbx.climbx.common.comcode.repository;

import com.climbx.climbx.common.comcode.entity.ComcodeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComcodeRepository extends JpaRepository<ComcodeEntity, Long> {

    List<ComcodeEntity> findByGroupCode(String groupCode);

    Optional<ComcodeEntity> findByGroupCodeAndCode(String groupCode, String code);
}