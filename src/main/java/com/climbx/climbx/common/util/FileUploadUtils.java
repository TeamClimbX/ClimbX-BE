package com.climbx.climbx.common.util;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.video.exception.FileExtensionNotExistsException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FileUploadUtils {

    public static String extractFileExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new FileExtensionNotExistsException(
                ErrorCode.FILE_EXTENSION_NOT_EXISTS, "File extension is required.");
        }

        return originalFileName.substring(originalFileName.lastIndexOf("."));
    }

    public static String generateVideoKey(UUID videoId, String fileExtension) {
        String now = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        return String.format("hls-videos/video-%s/%s-%s%s", videoId, now, videoId, fileExtension);
    }

    public static String generateProfileImageKey(Long userId, String fileExtension) {
        String now = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        return String.format("profile-images/user-%d/%s-%d%s", userId, now, userId, fileExtension);
    }

    public static String generateProblemImageKey(
        UUID problemId,
        Long gymAreaId,
        String fileExtension
    ) {
        String now = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        return String.format("problem-images/area-%d/%s-%s%s", gymAreaId, now, problemId,
            fileExtension);
    }

    public static String generateGymMap2dImageKey(Long gymId, String extension) {
        String now = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        return String.format("gym-images/%d/2d-map/map-2d-images/%s%s", gymId, now, extension);
    }

    public static String generateGymAreaImageKey(Long gymId, Long areaId, String extension) {
        String now = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        return String.format("gym-images/%d/2d-map/area-images/%s-%d%s", gymId, now, areaId,
            extension);
    }
}
