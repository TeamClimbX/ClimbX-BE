package com.climbx.climbx.admin.user;

import com.climbx.climbx.admin.user.service.AdminUserService;
import com.climbx.climbx.common.annotation.SuccessStatus;
import com.climbx.climbx.user.dto.UserProfileResponseDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PatchMapping("{nickname}/rating")
    @SuccessStatus(value = HttpStatus.OK)
    public UserProfileResponseDto updateRating(
        @PathVariable
        String nickname,

        @RequestBody
        @NotNull
        @Min(0)
        @Max(3100)
        Integer rating
    ) {
        log.info("관리자 사용자 등급 업데이트: nickname={}, rating={}", nickname, rating);
        return adminUserService.updateRating(nickname, rating);
    }
}
