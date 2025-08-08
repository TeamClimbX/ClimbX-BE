package com.climbx.climbx.submission;

import com.climbx.climbx.common.annotation.SuccessStatus;
import com.climbx.climbx.submission.dto.SubmissionAppealRequestDto;
import com.climbx.climbx.submission.dto.SubmissionAppealResponseDto;
import com.climbx.climbx.submission.dto.SubmissionCancelResponseDto;
import com.climbx.climbx.submission.dto.SubmissionCreateRequestDto;
import com.climbx.climbx.submission.dto.SubmissionListResponseDto;
import com.climbx.climbx.submission.dto.SubmissionResponseDto;
import com.climbx.climbx.submission.service.SubmissionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.data.web.SortDefault.SortDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController implements SubmissionApiDocumentation {

    private final SubmissionService submissionService;

    @Override
    @GetMapping
    @SuccessStatus(value = HttpStatus.OK)
    public SubmissionListResponseDto getSubmissions(
        @RequestParam(name = "nickname", required = false)
        String nickname,

        @RequestParam(name = "problemId", required = false)
        Long problemId,

        @RequestParam(name = "holdColor", required = false)
        String holdColor,

        @RequestParam(name = "ratingFrom", required = false)
        Integer ratingFrom,

        @RequestParam(name = "ratingTo", required = false)
        Integer ratingTo,

        @PageableDefault(page = 0, size = 20)
        @SortDefaults(
            @SortDefault(sort = "createdAt", direction = Direction.DESC)
        )
        Pageable pageable
    ) {
        return submissionService.getSubmissions(
            nickname,
            problemId,
            holdColor,
            ratingFrom,
            ratingTo,
            pageable
        );
    }

    @Override
    @PostMapping
    @SuccessStatus(value = HttpStatus.CREATED)
    public SubmissionResponseDto createSubmission(
        @AuthenticationPrincipal
        Long userId,

        @RequestBody
        SubmissionCreateRequestDto request
    ) {
        return submissionService.createSubmission(userId, request);
    }

    @Override
    @GetMapping("/{videoId}")
    @SuccessStatus(value = HttpStatus.OK)
    public SubmissionResponseDto getSubmissionByVideoId(
        @PathVariable(name = "videoId")
        UUID videoId
    ) {
        return submissionService.getSubmissionByVideoId(videoId);
    }

    @Override
    @PatchMapping("/{videoId}")
    @SuccessStatus(value = HttpStatus.OK)
    public SubmissionCancelResponseDto cancelSubmission(
        @AuthenticationPrincipal
        Long userId,

        @PathVariable(name = "videoId")
        UUID videoId
    ) {
        return submissionService.cancelSubmission(userId, videoId);
    }

    @Override
    @GetMapping("/{videoId}/appeal")
    @SuccessStatus(value = HttpStatus.OK)
    public SubmissionAppealResponseDto getSubmissionAppeal(
        @PathVariable(name = "videoId")
        UUID videoId
    ) {
        return submissionService.getSubmissionAppeal(videoId);
    }

    @Override
    @PostMapping("/{videoId}/appeal")
    @SuccessStatus(value = HttpStatus.CREATED)
    public SubmissionAppealResponseDto appealSubmission(
        @AuthenticationPrincipal
        Long userId,

        @PathVariable(name = "videoId")
        UUID videoId,

        @RequestBody
        SubmissionAppealRequestDto request // Optional reason for appeal
    ) {
        return submissionService.appealSubmission(userId, videoId, request);
    }
}