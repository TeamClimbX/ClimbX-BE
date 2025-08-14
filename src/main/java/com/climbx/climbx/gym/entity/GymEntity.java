package com.climbx.climbx.gym.entity;

import com.climbx.climbx.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "gyms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@Builder
public class GymEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gym_id", updatable = false, nullable = false)
    @NotNull
    private Long gymId; // 클라이밍장 ID

    @Column(name = "name", length = 64, nullable = false)
    @NotBlank
    @Size(min = 1, max = 64)
    private String name; // 클라이밍장 이름

    @Column(name = "latitude")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude; // 위도

    @Column(name = "longitude")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0", inclusive = false)
    private Double longitude; // 경도

    @Column(name = "address", length = 128)
    private String address; // 주소

    @Column(name = "phone_number", length = 64)
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$")
    private String phoneNumber; // 전화번호, 형식: 010-1234-5678

    @Column(name = "map_2d_image_cdn_url", length = 256)
    private String map2dImageCdnUrl; // 2D 맵 기본 이미지 URL

    @OneToMany(mappedBy = "gym", fetch = FetchType.LAZY)
    private List<GymAreaEntity> gymAreas; // 클라이밍장 영역 목록

    // Todo : 매장 메타데이터 json 컬럼

    public void setMap2dImageCdnUrl(String map2dImageCdnUrl) {
        this.map2dImageCdnUrl = map2dImageCdnUrl;
    }
}
