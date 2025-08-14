# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ClimbX is a Spring Boot-based backend API for climbing gym problem submission and ranking systems. The application features OAuth2 authentication, AWS S3 video uploads, and comprehensive ranking systems built with Domain-Driven Design principles.

## Architecture

- **Domain-Driven Design**: Code organized by business domains (auth, user, gym, problem, submission, ranking, video)
- **Layered Architecture**: Controller → Service → Repository pattern
- **RESTful API**: Standard HTTP methods with comprehensive Swagger documentation

## Essential Commands

### Development
```bash
# Start application
./gradlew bootRun

# Start MySQL database (required before running app)
cd docker/dev/mysql && docker-compose up -d

# Application runs on http://localhost:8080
# Swagger UI available at http://localhost:8080/swagger-ui.html
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests UserServiceTest

# Run with coverage report
./gradlew test jacocoTestReport
```

### Code Quality
```bash
# Run checkstyle (Google style)
./gradlew checkstyleMain checkstyleTest

# Build with all checks
./gradlew build

# SonarQube analysis (CI)
./gradlew sonarqube
```

## Core Domain Modules

### Authentication (`com.climbx.climbx.auth`)
- OAuth2 social login (Kakao, Google, Apple)
- JWT access/refresh token management
- Provider-specific user info extraction
- Token blacklist service

### User Management (`com.climbx.climbx.user`)
- Profile management with nickname system
- User statistics and tier calculations
- Ranking history tracking
- Rating system integration

### Problem System (`com.climbx.climbx.problem`)
- Climbing problem CRUD operations
- Tag-based categorization and rating
- Contribution system for community ratings
- Tier-based difficulty classification

### Submission System (`com.climbx.climbx.submission`)
- Problem completion submissions
- Admin review and approval workflow
- Appeal system for rejected submissions
- Status tracking (pending/approved/rejected)

### Ranking (`com.climbx.climbx.ranking`)
- Multi-criteria user rankings
- Historical ranking data
- Performance analytics

### Video Management (`com.climbx.climbx.video`)
- AWS S3 integration for video uploads
- Pre-signed URL generation
- CloudFront CDN distribution
- Video metadata management

## Coding Patterns

### Entity Design
- Entities extend `BaseTimeEntity` for timestamps or `SoftDeleteTimeEntity` for logical deletion
- Use fluent accessors pattern with `@Accessors(fluent = true)`
- Builder pattern required with protected no-args constructor

```java
@Entity
@Table(name = "submissions")
@Getter
@Accessors(fluent = true)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionEntity extends BaseTimeEntity {
    // Implementation
}
```

### Service Layer
- Class-level `@Transactional(readOnly = true)` 
- Method-level `@Transactional` for write operations
- Comprehensive exception handling with custom business exceptions

### DTOs
- Record-based request/response DTOs
- Validation annotations mandatory
- Static factory methods in response DTOs

```java
public record SubmissionCreateRequestDto(
    
    @NotNull Long problemId,
    @NotBlank String description,
    @Valid List<Long> videoIds
) {}

@Builder
public record SubmissionResponseDto(...) {
    public static SubmissionResponseDto from(SubmissionEntity entity) {
        // Conversion logic
    }
}
```

### Exception Handling
- Custom exceptions extend `BusinessException`
- Domain-specific error codes in `ErrorCode` enum
- Global exception handling via `GlobalExceptionHandler`

## Testing Requirements

### BDD Style Testing (Mandatory)
- **Always use `then()` instead of `verify()`** for mock verification
- Given-When-Then structure with `@Nested` test organization
- BDD imports: `import static org.mockito.BDDMockito.*;`

```java
@Test
@DisplayName("정상적으로 제출을 생성한다")
void shouldCreateSubmissionSuccessfully() {
    // given
    given(repository.save(any())).willReturn(savedEntity);
    
    // when
    SubmissionResponseDto result = service.create(request);
    
    // then
    assertThat(result.id()).isNotNull();
    then(repository).should().save(any(SubmissionEntity.class));
}
```

### Test Data
- Use Fixture classes for test data creation (see `src/test/java/fixture/`)
- Repository tests with `@DataJpaTest`
- Controller tests with `@WebMvcTest`

## Environment Configuration

Required environment variables for development:
- JWT_SECRET, DB_USER, DB_PASSWORD
- AWS credentials for S3 integration
- OAuth2 provider keys (Kakao, Google, Apple)

Database initialization data available in `src/main/resources/db/init/data.sql`.

## Key Libraries

- Spring Boot 3.5.0 with Java 21
- Spring Security OAuth2 Resource Server
- Spring Data JPA with MySQL
- AWS SDK for S3 integration
- SpringDoc OpenAPI for documentation
- Caffeine for caching
- Checkstyle with Google style guide

## Git Commit Guidelines

### Commit Message Format
- **DO NOT** include Claude Code author information in commit messages
- Use conventional commit format: `type: description`
- Keep commit messages concise and focused on actual changes
- Example: `feat: add user authentication system`

### Pull Request Guidelines
- **ALWAYS** reference the PR template at `.github/pull_request_template.md`
- Follow the template structure:
  - 📝 작업 내용 (Description)
  - ✨ 변경 사항 (Changes)
  - ✅ 테스트 방법 (How to Test)
  - 💬 리뷰어에게 (To Reviewer)
  - 🚀관련 이슈 (Related Issue)
  - 📋 앞으로의 과제 (Todo)
- Use Korean language for all descriptions
- Include relevant issue references (e.g., Closes: SWM-XXX)