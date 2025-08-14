package com.climbx.climbx.admin.submission.dto;

import com.climbx.climbx.common.enums.StatusType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SubmissionReviewRequestDto(

    @NotNull
    StatusType status,

    @NotBlank
    String reason
) {

}