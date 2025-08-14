# ClimbX Backend - GitHub Copilot Instructions

You are working on ClimbX, a Spring Boot-based backend API for climbing gym problem submission and ranking systems.

## Project Overview
ClimbX is a climbing gym problem submission and ranking system backend API built with Spring Boot, featuring OAuth2 authentication, video uploads, and comprehensive ranking systems.

## Architecture Patterns
- **Domain-Driven Design (DDD)**: Modularized by domain
- **Layered Architecture**: Controller → Service → Repository
- **RESTful API**: Standard HTTP methods and status codes

## Core Modules

### 1. Authentication (`auth`)
- OAuth2 social login (Kakao, Google, Apple)
- JWT token-based authentication/authorization
- Refresh token management

### 2. User Management (`user`)
- User profile management
- User statistics and ranking history
- User account information

### 3. Gym & Problem (`gym`, `problem`)
- Climbing gym information management
- Climbing problem (route) information management

### 4. Submission System (`submission`)
- Problem completion submissions
- Submission verification and approval/rejection
- Submission appeal system

### 5. Ranking System (`ranking`)
- User rankings based on various criteria
- Rating, completion count, difficulty-based statistics

### 6. Video Management (`video`)
- AWS S3-based video uploads
- Video metadata management

## Coding Standards

### Entity Design
- JPA Entity + Fluent Accessor pattern
- Inherit from `BaseTimeEntity` for common timestamp fields
- Use `SoftDeleteTimeEntity` for logical deletion

```java
@Entity
@Table(name = "users")
public class UserAccountEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Fluent accessors
    public UserAccountEntity id(Long id) {
        this.id = id;
        return this;
    }
}
```

### DTO Pattern
- Record-based DTO usage
- Extensive use of validation annotations
- Clear naming (`*RequestDto`, `*ResponseDto`)

```java
public record CreateSubmissionRequestDto(
    @NotNull Long problemId,
    @NotBlank String description,
    @Valid List<Long> videoIds
) {}
```

### Service Layer
- Business logic encapsulation
- Transaction management
- Exception handling

```java
@Service
@Transactional(readOnly = true)
public class SubmissionService {
    
    @Transactional
    public SubmissionResponseDto createSubmission(CreateSubmissionRequestDto request) {
        // Business logic
    }
}
```

### Controller Design
- `@RestController` + `@RequestMapping`
- Swagger documentation (`@Operation`, `@ApiResponse`)
- Standard HTTP status codes

```java
@RestController
@RequestMapping("/api/submissions")
@Tag(name = "Submission", description = "Submission management API")
public class SubmissionController {
    
    @PostMapping
    @Operation(summary = "Create submission", description = "Creates a new climbing problem submission")
    public ResponseEntity<SubmissionResponseDto> createSubmission(
        @Valid @RequestBody CreateSubmissionRequestDto request
    ) {
        // Implementation
    }
}
```

## Testing Strategy

### BDD Style Testing
- Given-When-Then pattern
- Test scenario expression through method names

```java
@DisplayName("Submission creation tests")
class SubmissionServiceTest {
    
    @Test
    @DisplayName("Creating submission with valid request should succeed")
    void givenValidRequest_whenCreateSubmission_thenSuccess() {
        // Given
        CreateSubmissionRequestDto request = new CreateSubmissionRequestDto(...);
        
        // When
        SubmissionResponseDto result = submissionService.createSubmission(request);
        
        // Then
        assertThat(result.id()).isNotNull();
    }
}
```

### Test Types
- **Unit Tests**: `*ServiceTest` - Business logic testing
- **Integration Tests**: `*ControllerTest` - API endpoint testing
- **Security Tests**: `*SecurityTest` - Authentication/authorization testing

## Security Considerations

### JWT Authentication
- Access Token: Short expiration time
- Refresh Token: Long expiration time, secure storage
- Token validation filter implementation

### API Security
- CORS configuration
- Rate Limiting (future implementation)
- Input validation

## Database Design

### Core Tables
- `users`: User account information
- `user_auth`: Authentication information (OAuth2)
- `user_stats`: User statistics
- `gyms`: Gym information
- `problems`: Climbing problems
- `submissions`: Submission information
- `videos`: Video metadata
- `user_ranking_history`: Ranking history

### Relationships
- User 1:1 UserAuth
- User 1:1 UserStats
- User 1:N Submissions
- Problem 1:N Submissions
- Submission 1:N Videos

## Development Workflow

### When developing new features
1. **Domain Analysis**: Determine which module it belongs to
2. **Entity Design**: Data model definition
3. **Repository Implementation**: Data access layer
4. **Service Implementation**: Business logic
5. **Controller Implementation**: API endpoints
6. **DTO Definition**: Request/response models
7. **Test Writing**: Unit + Integration tests
8. **Swagger Documentation**: API documentation

### Code Review Checklist
- [ ] Proper package structure
- [ ] Validation annotations applied
- [ ] Exception handling implemented
- [ ] Test coverage secured
- [ ] Swagger documentation completed
- [ ] Naming conventions followed

## Useful Commands

```bash
# Run development environment
./gradlew bootRun

# Run all tests
./gradlew test

# Code quality check
./gradlew checkstyleMain checkstyleTest

# Test coverage
./gradlew jacocoTestReport

# Build
./gradlew build
```

## References
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [JPA/Hibernate](https://spring.io/guides/gs/accessing-data-jpa/)
- [Swagger/OpenAPI](https://springdoc.org/)