# ClimbX Backend

## 필수 요구사항

- Java 21
- Gradle 8.x
- Docker 및 Docker Compose

## 환경 변수 설정

### Dev 환경

```properties
# 시스템 설정
SPRING_CONTAINER_NAME=climbx-be
SPRING_CONTAINER_PORT=8080

# JWT 설정
JWT_SECRET_DEV=your-jwt-secret-key

# 데이터베이스 설정
DEV_DB_USER=your-db-username
DEV_DB_PASSWORD=your-db-password

# AWS 설정
## 기본 설정
AWS_REGION=your-aws-region
AWS_ACCESS_KEY_ID=your-aws-access-key
AWS_SECRET_ACCESS_KEY=your-aws-secret-key
## EC2
AWS_DEV_BE_CORE_EC2_HOST=your-ec2-hostname
AWS_DEV_BE_CORE_EC2_USERNAME=your-ec2-username
AWS_DEV_BE_CORE_EC2_SSH_KEY_PEM=your-ec2-ssh-key.pem
## ECR
AWS_DEV_BE_CORE_ECR_REPOSITORY_URI=your-ecr-repository-uri
## S3
AWS_DEV_S3_VIDEOS_SOURCE_BUCKET_NAME=your-s3-videos-source-bucket-name
AWS_DEV_S3_IMAGES_BUCKET_NAME=your-s3-profile-image-bucket-name
AWS_DEV_S3_PRESIGNED_URL_EXPIRATION=180
## CloudFront
AWS_DEV_CLOUDFRONT_DOMAIN=your-cloudfront-domain

# OAuth2 설정
KAKAO_NATIVE_APP_KEY=your-kakao-app-key
GOOGLE_ANDROID_APP_KEY=your-google-android-key
GOOGLE_IOS_APP_KEY=your-google-ios-key
APPLE_ANDROID_APP_KEY=your-apple-android-key
APPLE_IOS_APP_KEY=your-apple-ios-key
```

### Prod 환경

```properties
# 시스템 설정
SPRING_PROFILES_ACTIVE=prod
SPRING_CONTAINER_NAME=climbx-be
SPRING_CONTAINER_PORT=8080

# JWT 설정
JWT_SECRET_PROD=your-jwt-secret-key

# 데이터베이스 설정
PROD_DB_USER=your-db-username
PROD_DB_PASSWORD=your-db-password

# AWS 설정
## 기본 설정
AWS_REGION=your-aws-region
AWS_ACCESS_KEY_ID=your-aws-access-key
AWS_SECRET_ACCESS_KEY=your-aws-secret-key
## EC2
AWS_PROD_BE_CORE_EC2_HOST=your-ec2-hostname
AWS_PROD_BE_CORE_EC2_USERNAME=your-ec2-username
AWS_PROD_BE_CORE_EC2_SSH_KEY_PEM=your-ec2-ssh-key.pem
## ECR
AWS_PROD_BE_CORE_ECR_REPOSITORY_URI=your-ecr-repository-uri
## S3
AWS_PROD_S3_VIDEOS_SOURCE_BUCKET_NAME=your-s3-videos-source-bucket-name
AWS_PROD_S3_IMAGES_BUCKET_NAME=your-s3-profile-image-bucket-name
AWS_PROD_S3_PRESIGNED_URL_EXPIRATION=180
## CloudFront
AWS_PROD_CLOUDFRONT_DOMAIN=your-cloudfront-domain

# OAuth2 설정
KAKAO_NATIVE_APP_KEY=your-kakao-app-key
GOOGLE_ANDROID_APP_KEY=your-google-android-key
GOOGLE_IOS_APP_KEY=your-google-ios-key
APPLE_ANDROID_APP_KEY=your-apple-android-key
APPLE_IOS_APP_KEY=your-apple-ios-key
```

## 데이터베이스 실행

```bash
# MySQL Docker 컨테이너 시작
cd docker/dev/mysql
docker-compose up -d
```

## 애플리케이션 실행

```bash
# Gradle로 실행
./gradlew bootRun
```

## 확인

- 애플리케이션: http://localhost:8080
- Swagger UI: http://localhost:8080/api/swagger

## 테스트

```bash
# 전체 테스트 실행
./gradlew test
```
