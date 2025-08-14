package com.climbx.climbx.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED) // 기본 생성자는 protected로 설정
@Accessors(fluent = true)
@Getter
public abstract class BaseTimeEntity extends SoftDeleteTimeEntity {

    /*
     * JPA Auditing으로 생성자(@CreatedBy), 수정자(@LastModifiedBy)을 자동으로 주입
     */

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", updatable = true)
    private LocalDateTime updatedAt;
}