package com.climbx.climbx.common.service;

import static com.climbx.climbx.common.util.FileUploadUtils.extractFileExtension;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;
import com.climbx.climbx.common.util.FileUploadUtils;
import com.climbx.climbx.video.exception.AwsBucketNameNotConfiguredException;
import com.climbx.climbx.video.exception.AwsBucketNotFoundException;
import com.climbx.climbx.video.exception.AwsCloudFrontDomainNotConfiguredException;
import com.climbx.climbx.video.exception.FileExtensionNotExistsException;
import com.github.benmanes.caffeine.cache.Cache;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;

    private final S3Client s3Client;

    private final Cache<String, Boolean> existingBuckets;

    @Value("${aws.s3.videos-source-bucket-name}")
    private String videosSourceBucketName;

    @Value("${aws.s3.images-bucket-name}")
    private String imagesBucketName;

    @Value("${aws.s3.presigned-url-expiration}")
    private long presignedUrlExpiration;

    @Value("${aws.cloudfront.domain}")
    private String cloudfrontDomain;

    public String generateVideoUploadPresignedUrl(UUID videoId, String fileExtension) {
        log.info("Generating video upload presigned URL for videoId: {}, fileExtension: {}",
            videoId, fileExtension);

        // 입력값 검증
        FileUploadValidator.validateVideoParameters(videoId, fileExtension);

        // 버킷 이름이 설정되어 있지 않으면 예외 발생
        if (videosSourceBucketName == null || videosSourceBucketName.isEmpty()) {
            log.error("S3 video source bucket name is not configured");
            throw new AwsBucketNameNotConfiguredException(
                ErrorCode.S3_BUCKET_NAME_NOT_CONFIGURED,
                "S3 video source bucket name is not configured."
            );
        }

        // 버킷 이름이 캐시에 없으면 S3에서 버킷 존재 여부 확인 후 캐싱
        ensureBucketExists(videosSourceBucketName);

        // S3 키 생성 (videoId.mp4)
        fileExtension = ensureDotPrefix(fileExtension);
        String s3Key = videoId + fileExtension;

        // S3 presigned URL 생성
        String presignedUrl = createPresignedUrl(s3Key, videosSourceBucketName);

        log.info("Successfully generated presigned URL for videoId: {}", videoId);
        return presignedUrl;
    }

    /**
     * 프로필 이미지를 S3에 업로드하고 CDN URL을 반환합니다.
     */
    public String uploadProfileImage(Long userId, MultipartFile profileImage) {
        log.info("Uploading profile image for userId: {}, fileName: {}",
            userId, profileImage.getOriginalFilename());

        // 입력값 검증
        FileUploadValidator.validateProfileImageParameters(userId, profileImage);

        // 버킷 이름이 설정되어 있지 않으면 예외 발생
        if (imagesBucketName == null || imagesBucketName.isEmpty()) {
            log.error("S3 bucket name is not configured");
            throw new AwsBucketNameNotConfiguredException(
                ErrorCode.S3_BUCKET_NAME_NOT_CONFIGURED,
                "Image S3 bucket name is not configured."
            );
        }

        // 버킷 존재 여부 확인
        ensureBucketExists(imagesBucketName);

        // 파일 확장자 추출
        String fileExtension = extractFileExtension(
            profileImage.getOriginalFilename());

        // S3 키 생성 (profile-images/user-{userId}/yyyy-MM-dd-HH-mm-ss-{userId}.extension)
        String s3Key = FileUploadUtils.generateProfileImageKey(userId, fileExtension);

        try {
            // S3에 파일 업로드
            uploadFileToS3(imagesBucketName, s3Key, profileImage);

            // CDN URL 생성
            String cdnUrl = generateCdnUrl(s3Key);

            log.info("Successfully uploaded profile image: userId={}, CDN URL={}",
                userId, cdnUrl);

            return cdnUrl;

        } catch (IOException e) {
            log.error("Failed to read profile image file: userId={}, fileName={}",
                userId, profileImage.getOriginalFilename(), e);
            throw new BusinessException(
                ErrorCode.INTERNAL_ERROR, "Failed to read profile image file");
        }
    }

    /**
     * 문제 이미지를 S3에 업로드하고 CDN URL을 반환합니다.
     */
    public String uploadProblemImage(UUID problemId, Long gymAreaId, MultipartFile problemImage) {
        log.info("Uploading problem image: problemId={}, gymAreaId={}, fileName={}",
            problemId, gymAreaId, problemImage.getOriginalFilename());

        // 입력값 검증
        FileUploadValidator.validateProblemImageParameters(problemId, problemImage);

        // 파일 확장자 추출
        String fileExtension = extractFileExtension(problemImage.getOriginalFilename());

        // S3 키 생성 (problem-images/{gymAreaId}/{yyyy-MM-dd-HH-mm-ss}-{problemId}.extension)
        String s3Key = FileUploadUtils.generateProblemImageKey(problemId, gymAreaId, fileExtension);

        try {
            // S3에 파일 업로드
            uploadFileToS3(imagesBucketName, s3Key, problemImage);

            // CDN URL 생성
            String cdnUrl = generateCdnUrl(s3Key);

            log.info(
                "Successfully uploaded problem image: problemId={}, gymAreaId={}, CDN URL={}",
                problemId, gymAreaId, cdnUrl
            );
            return cdnUrl;

        } catch (IOException e) {
            log.error(
                "Failed to read problem image file: problemId={}, gymAreaId={}, fileName={}",
                problemId, gymAreaId, problemImage.getOriginalFilename(), e
            );
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                "Failed to read problem image file");
        }
    }

    /**
     * 클라이밍장 2D 맵 이미지들을 S3에 업로드하고 map2dImageCdnUrl을 반환
     */
    public String uploadGym2dMapImages(
        Long gymId,
        MultipartFile map2dImage
    ) {
        log.info("Uploading gym 2D map images: gymId={}, map2dImage={}",
            gymId, map2dImage.getOriginalFilename());

        // 버킷 이름이 설정되어 있지 않으면 예외 발생
        if (imagesBucketName == null || imagesBucketName.isEmpty()) {
            log.error("Image S3 bucket name is not configured");
            throw new AwsBucketNameNotConfiguredException(
                ErrorCode.S3_BUCKET_NAME_NOT_CONFIGURED,
                "Image S3 bucket name is not configured."
            );
        }

        // 버킷 존재 여부 확인
        ensureBucketExists(imagesBucketName);

        // 입력값 검증
        if (map2dImage == null || map2dImage.isEmpty()) {
            log.warn("Map 2D image file cannot be null or empty");
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                "Map 2D image file cannot be null or empty");
        }

        try {
            // Base 이미지 업로드
            String extension = extractFileExtension(map2dImage.getOriginalFilename());
            String map2dImageKey = FileUploadUtils.generateGymMap2dImageKey(gymId, extension);

            uploadFileToS3(imagesBucketName, map2dImageKey, map2dImage);

            String map2dImageCdnUrl = generateCdnUrl(map2dImageKey);

            log.info("Successfully uploaded gym 2D map images: gymId={}, map2dImageCdnUrl={}",
                gymId, map2dImageCdnUrl);

            return map2dImageCdnUrl;

        } catch (IOException e) {
            log.error("Failed to read gym 2D map image files: gymId={}", gymId, e);
            throw new BusinessException(
                ErrorCode.INTERNAL_ERROR, "Failed to read gym 2D map image files");
        }
    }

    /*
     * 클라이밍장 벽 2D 사진을 S3에 업로드하고 Map으로 반환
     */
    public Map<Long, String> uploadGymAreaImages(
        Long gymId,
        Map<Long, MultipartFile> areaImages
    ) {
        if (imagesBucketName == null || imagesBucketName.isEmpty()) {
            log.error("Image S3 bucket name is not configured");
            throw new AwsBucketNameNotConfiguredException(
                ErrorCode.S3_BUCKET_NAME_NOT_CONFIGURED,
                "Image S3 bucket name is not configured."
            );
        }

        // 버킷 존재 여부 확인
        ensureBucketExists(imagesBucketName);

        List<String> uploadedImageKeys = new ArrayList<>();     // 업로드가 성공한 이미지 키들을 저장할 리스트(실패 시 롤백 용도)
        Map<Long, String> areaImageCdnUrls = new HashMap<>();
        try {
            for (Map.Entry<Long, MultipartFile> entry : areaImages.entrySet()) {
                Long areaId = entry.getKey();
                MultipartFile areaImage = entry.getValue();

                String extension = extractFileExtension(areaImage.getOriginalFilename());
                String areaImageKey = FileUploadUtils.generateGymAreaImageKey(gymId, areaId,
                    extension);

                uploadFileToS3(imagesBucketName, areaImageKey, areaImage);
                uploadedImageKeys.add(areaImageKey);  // Area 이미지 키를 업로드 리스트에 추가

                String areaImageCdnUrl = generateCdnUrl(areaImageKey);
                areaImageCdnUrls.put(areaId, areaImageCdnUrl);
            }

            return areaImageCdnUrls;

        } catch (IOException e) {
            log.error("Failed to read gym area map image files: gymId={}", gymId, e);
            cleanUpUploadedFiles(imagesBucketName, uploadedImageKeys);
            throw new BusinessException(
                ErrorCode.INTERNAL_ERROR, "Failed to read gym area map image files");
        }
    }

    private void ensureBucketExists(String bucketName) {
        if (existingBuckets.getIfPresent(bucketName) == null) {
            log.debug("Bucket '{}' not found in cache, checking existence", bucketName);
            if (doesBucketExist(bucketName)) {
                existingBuckets.put(bucketName, true);
                log.info("Bucket '{}' verified and cached", bucketName);
            } else {
                log.error("S3 bucket does not exist: {}", bucketName);
                throw new AwsBucketNotFoundException(
                    ErrorCode.S3_BUCKET_NOT_FOUND, "Bucket does not exist: " + bucketName);
            }
        } else {
            log.debug("Using cached bucket: {}", bucketName);
        }
    }

    private void uploadFileToS3(String bucketName, String s3Key, MultipartFile file)
        throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            .build();

        RequestBody requestBody = RequestBody.fromInputStream(
            file.getInputStream(), file.getSize());

        s3Client.putObject(putObjectRequest, requestBody);
        log.debug("File uploaded to S3 bucket {} successfully: {}", bucketName, s3Key);
    }

    private void cleanUpUploadedFiles(String bucketName, List<String> uploadedImageKeys) {
        log.info("S3에서 파일 삭제를 시도합니다. Key: {}", uploadedImageKeys.toString());

        for (String uploadedImageKey : uploadedImageKeys) {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(uploadedImageKey)
                .build();
            try {
                s3Client.deleteObject(deleteObjectRequest);
                log.info("S3에서 파일을 성공적으로 삭제했습니다. Key: {}", uploadedImageKey);
            } catch (S3Exception e) {
                log.error("S3 파일 삭제 중 오류가 발생했습니다. Key: {}", uploadedImageKey, e);
                // 필요에 따라 사용자 정의 예외를 던질 수 있습니다.
                throw new BusinessException(ErrorCode.S3_FILE_DELETE_FAILED, "S3 파일 삭제에 실패했습니다.");
            }
        }
    }

    private String generateCdnUrl(String s3Key) {
        if (cloudfrontDomain == null || cloudfrontDomain.isEmpty()) {
            log.warn("CloudFront domain is not configured, returning S3 URL");
            throw new AwsCloudFrontDomainNotConfiguredException(
                ErrorCode.CLOUDFRONT_DOMAIN_NOT_CONFIGURED, "CloudFront domain is not configured."
            );
        }

        // CloudFront CDN URL 생성
        String cdnUrl = String.format("https://%s/%s", cloudfrontDomain, s3Key);
        log.debug("Generated CDN URL: {}", cdnUrl);
        return cdnUrl;
    }

    private String createPresignedUrl(String s3Key, String bucketName) {
        try {
            log.debug("Creating presigned URL for S3 key: {}", s3Key);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                .putObjectRequest(putObjectRequest)
                .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.debug("Presigned URL created successfully, expiration: {} seconds",
                presignedUrlExpiration);
            return presignedUrl;

        } catch (AwsServiceException ase) {
            log.error(
                "AWS service error while creating presigned URL for key: {}, statusCode: {}, errorCode: {}, message: {}",
                s3Key, ase.statusCode(), ase.awsErrorDetails().errorCode(), ase.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                "AWS service error: " + ase.getMessage());
        } catch (SdkException se) {
            log.error("AWS SDK error while creating presigned URL for key: {}, message: {}",
                s3Key, se.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                "AWS SDK error: " + se.getMessage());
        }
    }

    private String ensureDotPrefix(String fileExtension) {
        log.debug("Ensuring dot prefix for file extension: {}", fileExtension);

        if (fileExtension == null) {
            log.warn("File extension is null");
            throw new FileExtensionNotExistsException(
                ErrorCode.FILE_EXTENSION_NOT_EXISTS, "File extension is required."
            );
        }

        if (!fileExtension.startsWith(".")) {
            String originalExtension = fileExtension;
            fileExtension = "." + fileExtension; // 확장자 앞에 점 추가
            log.debug("Added dot prefix to file extension: {} -> {}", originalExtension,
                fileExtension);
        } else {
            log.debug("File extension already has dot prefix: {}", fileExtension);
        }

        return fileExtension;
    }

    private boolean doesBucketExist(String bucketName) {
        log.debug("Checking if bucket exists: {}", bucketName);

        try {
            s3Client.getBucketAcl(r -> r.bucket(bucketName));
            log.debug("Bucket exists and accessible: {}", bucketName);
            return true;
        } catch (AwsServiceException ase) {
            // A redirect error or an AccessDenied exception means the bucket exists but it's not in this region
            // or we don't have permissions to it.
            if ((ase.statusCode() == HttpStatusCode.MOVED_PERMANENTLY) || "AccessDenied".equals(
                ase.awsErrorDetails().errorCode())) {
                log.warn(
                    "Bucket exists but moved permanently or access denied - bucketName: {}, statusCode: {}, errorCode: {}",
                    bucketName, ase.statusCode(), ase.awsErrorDetails().errorCode());
                return true;
            }
            if (ase.statusCode() == HttpStatusCode.NOT_FOUND) {
                log.warn("Bucket not found - bucketName: {}, statusCode: {}",
                    bucketName, ase.statusCode());
                return false;
            }

            log.error(
                "Unexpected AWS service error while checking bucket existence - bucketName: {}, statusCode: {}, errorCode: {}, message: {}",
                bucketName, ase.statusCode(), ase.awsErrorDetails().errorCode(), ase.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                "Failed to check bucket existence: " + ase.getMessage());
        } catch (Exception e) {
            log.error(
                "Unexpected error while checking bucket existence - bucketName: {}, error: {}",
                bucketName, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                "Failed to check bucket existence: " + e.getMessage());
        }
    }
}