package com.climbx.climbx.common.comcode.entity;

import com.climbx.climbx.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "comcodes")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class ComcodeEntity extends BaseTimeEntity {

    /**
     * 공통 코드 엔티티, is_active는 BaseTimeEntity에서 deleted_at으로 대체
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_code", length = 50, nullable = false)
    private String groupCode;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "code_name", length = 100, nullable = false)
    private String codeName;

    @Column(name = "description", length = 255, nullable = true)
    private String description; // e.g. "티어 이름을 정의하는 코드", "태그의 분류를 정의하는 코드"

    @Builder.Default
    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 0")
    private int sortOrder = 0;
}
