package com.climbx.climbx.admin;

import com.climbx.climbx.common.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/api/admin/health-check")
    public ApiResponseDto<String> healthCheck() {
        return ApiResponseDto.success("OK", HttpStatus.OK);
    }
}
