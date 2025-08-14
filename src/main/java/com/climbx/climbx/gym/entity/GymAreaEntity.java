package com.climbx.climbx.gym.entity;

import com.climbx.climbx.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "gym_areas")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class GymAreaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gym_area_id", updatable = false, nullable = false)
    private Long gymAreaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private GymEntity gym;

    @Column(name = "area_name", length = 64, nullable = false)
    @Size(min = 1, max = 64)
    private String areaName;

    @Column(name = "area_image_cdn_url", length = 256)
    @Size(max = 256)
    private String areaImageCdnUrl;

    public void setAreaImageCdnUrl(String areaImageCdnUrl) {
        this.areaImageCdnUrl = areaImageCdnUrl;
    }
}
