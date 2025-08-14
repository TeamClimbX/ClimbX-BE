package com.climbx.climbx.common.service;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;
import com.climbx.climbx.common.exception.ImageSizeExceededException;
import com.climbx.climbx.video.exception.FileExtensionNotExistsException;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class FileUploadValidator {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Set<String> allowedContentTypes = Set.of("image/jpeg", "image/png",
        "image/jpg", "image/gif", "image/webp", "image/bmp", "image/heic");

    public static void validateVideoParameters(UUID videoId, String fileExtension) {
        if (videoId == null) {
            log.warn("VideoId cannot be null");
            throw new IllegalArgumentException("VideoId cannot be null");
        }

        if (fileExtension == null || fileExtension.trim().isEmpty()) {
            log.warn("File extension cannot be null or empty");
            throw new FileExtensionNotExistsException(
                ErrorCode.FILE_EXTENSION_NOT_EXISTS, "File extension is required."
            );
        }

        log.debug("Input parameters validated successfully");
    }

    public static void validateProfileImageParameters(Long userId, MultipartFile profileImage) {
        if (userId == null) {
            log.warn("UserId cannot be null");
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "UserId cannot be null");
        }

        if (profileImage == null || profileImage.isEmpty()) {
            log.warn("Profile image file cannot be null or empty");
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST, "Profile image file cannot be null or empty");
        }

        // 파일 크기 검증 (예: 5MB 제한)
        if (profileImage.getSize() > MAX_IMAGE_SIZE) {
            log.warn("Profile image file size exceeds limit: {} bytes", profileImage.getSize());
            throw new ImageSizeExceededException(
                ErrorCode.IMAGE_SIZE_EXCEEDED, "Profile image file size must be less than 5MB");
        }

        // Content Type 검증
        String contentType = profileImage.getContentType();

        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            log.warn("Invalid content type for profile image: {}", contentType);
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Profile image must be an image file (JPEG, PNG, JPG, GIF, WEBP, BMP, HEIC)"
            );
        }

        log.debug("Profile image parameters validated successfully");
    }

    public static void validateProblemImageParameters(UUID problemId, MultipartFile problemImage) {
        if (problemId == null) {
            log.warn("problemId cannot be null");
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST, "problemId cannot be null");
        }

        if (problemImage == null || problemImage.isEmpty()) {
            log.warn("Problem image file cannot be null or empty");
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST, "Problem image file cannot be null or empty");
        }

        // 파일 크기 검증 (예: 5MB 제한)
        if (problemImage.getSize() > MAX_IMAGE_SIZE) {
            log.warn("Problem image file size exceeds limit: fileSize={} bytes, limit={} btyes",
                problemImage.getSize(), MAX_IMAGE_SIZE);
            throw new ImageSizeExceededException(
                ErrorCode.IMAGE_SIZE_EXCEEDED, "Problem image file size must be less than 10MB");
        }

        // Content Type 검증
        String contentType = problemImage.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            log.warn("Invalid content type for problem image: {}", contentType);
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Problem image must be an image file (JPEG, PNG, JPG, GIF, WEBP, BMP, HEIC)"
            );
        }

        log.debug("Problem image parameters validated successfully");
    }

}
