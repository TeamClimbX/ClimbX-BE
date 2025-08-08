package com.climbx.climbx.problem;

import com.climbx.climbx.common.annotation.SuccessStatus;
import com.climbx.climbx.common.enums.ActiveStatusType;
import com.climbx.climbx.problem.dto.ProblemCreateRequestDto;
import com.climbx.climbx.problem.dto.ProblemCreateResponseDto;
import com.climbx.climbx.problem.dto.ProblemInfoResponseDto;
import com.climbx.climbx.problem.enums.ProblemTierType;
import com.climbx.climbx.problem.service.ProblemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController implements ProblemApiDocumentation {

    private final ProblemService problemService;

    @Override
    @GetMapping
    @SuccessStatus(value = HttpStatus.OK)
    public List<ProblemInfoResponseDto> getProblemsWithFilters(
        @RequestParam(value = "gymId", required = false)
        Long gymId,

        @RequestParam(value = "gymAreaId", required = false)
        Long gymAreaId,

        @RequestParam(value = "localLevel", required = false)
        String localLevel,

        @RequestParam(value = "holdColor", required = false)
        String holdColor,

        @RequestParam(value = "problemTier", required = false)
        ProblemTierType problemTier,

        @RequestParam(value = "activeStatus", required = false)
        ActiveStatusType activeStatus
    ) {
        log.info(
            "문제 조회: gymId={}, gymAreaId={}, localLevel={}, holdColor={}, tier={}, activeStatus={}",
            gymId, gymAreaId, localLevel, holdColor, problemTier, activeStatus);

        return problemService.getProblemsWithFilters(gymId, gymAreaId, localLevel, holdColor,
            problemTier, activeStatus);
    }

    @Override
    @PostMapping
    @SuccessStatus(value = HttpStatus.CREATED)
    public ProblemCreateResponseDto registerProblem(
        @RequestPart("request")
        @Valid
        ProblemCreateRequestDto request,

        @RequestPart(value = "problemImage", required = true)
        @NotNull
        MultipartFile problemImage
    ) {
        log.info("문제 생성: gymAreaId={}, localLevel={}, holdColor={}",
            request.gymAreaId(), request.localLevel(), request.holdColor());
        return problemService.registerProblem(request, problemImage);
    }

    @Override
    @DeleteMapping("/{problemId}")
    @SuccessStatus(value = HttpStatus.NO_CONTENT)
    public void softDeleteProblem(@PathVariable("problemId") UUID problemId) {
        log.info("문제 삭제: problemId={}", problemId);
        problemService.softDeleteProblem(problemId);
    }
} 