package com.climbx.climbx.admin.submission;

import com.climbx.climbx.admin.submission.dto.SubmissionReviewRequestDto;
import com.climbx.climbx.admin.submission.dto.SubmissionReviewResponseDto;
import com.climbx.climbx.admin.submission.service.AdminSubmissionService;
import com.climbx.climbx.common.annotation.SuccessStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin/submissions")
@RequiredArgsConstructor
@Slf4j
public class AdminSubmissionController implements AdminSubmissionApiDocumentation {

    private final AdminSubmissionService submissionService;

    /**
     * 특정 비디오 ID의 제출물을 검토하여 승인(ACCEPTED) 또는 거부(REJECTED) 처리
     *
     * @param videoId 비디오 ID
     * @param request 제출물 검토 요청 DTO
     * @return 검토 결과 DTO
     */
    @Override
    @PutMapping("/{videoId}/status")
    @SuccessStatus(HttpStatus.OK)
    public SubmissionReviewResponseDto reviewSubmission(
        @PathVariable("videoId")
        UUID videoId,

        @RequestBody
        SubmissionReviewRequestDto request
    ) {
        log.info("제출물 리뷰(ACCEPTED/REJECTED) videoId: {}, status: {}, reason: {}",
            videoId, request.status(), request.reason());

        return submissionService.reviewSubmission(videoId, request);
    }
}
