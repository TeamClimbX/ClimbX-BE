# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ClimbX is a Spring Boot-based backend API for climbing gym problem submission and ranking systems. Features OAuth2 authentication, AWS S3 video uploads, and comprehensive ranking systems with Domain-Driven Design.

## Subagent Usage Guidelines

**Always use specialized subagents for better performance and accuracy:**

- **code-reviewer**: Use after any code changes to ensure quality and security
- **backend-architect**: Use for API design, database schemas, and system architecture decisions
- **database-optimization**: Use for query performance, indexing, and database issues
- **api-documenter**: Use for OpenAPI specs, documentation, and client libraries
- **general-purpose**: Use for complex searches, multi-step analysis, or when unsure which agent to use

## Core Domains

- **Authentication**: OAuth2 (Kakao, Google, Apple), JWT tokens
- **User Management**: Profiles, statistics, rankings, tiers
- **Problem System**: CRUD, tags, ratings, difficulty classification
- **Submission System**: Problem completions, admin review, appeals
- **Ranking**: Multi-criteria rankings, historical data
- **Video**: AWS S3 integration, CloudFront CDN

## Essential Commands

```bash
# Development
./gradlew bootRun
cd docker/dev/mysql && docker-compose up -d  # Start DB first
# App: http://localhost:8080, Swagger: /swagger-ui.html

# Testing
./gradlew test
./gradlew test --tests UserServiceTest

# Code Quality
./gradlew checkstyleMain checkstyleTest
./gradlew build
```


## Coding Standards

### Entity Design
- Extend `BaseTimeEntity` or `SoftDeleteTimeEntity`
- Use `@Accessors(fluent = true)` and Builder pattern
- Protected no-args constructor

### Service Layer
- Class-level `@Transactional(readOnly = true)`
- Method-level `@Transactional` for writes
- Custom business exceptions

### DTOs
- Record-based with validation annotations
- Static factory methods in response DTOs

## Testing 

### BDD Style (Mandatory)
- **Use `then()` instead of `verify()`** for mock verification
- Given-When-Then with `@Nested` organization
- Import: `import static org.mockito.BDDMockito.*;`
- Use Fixture classes from `src/test/java/fixture/`
- `@DataJpaTest` for repositories, `@WebMvcTest` for controllers

## Environment & Libraries

**Required env vars**: JWT_SECRET, DB_USER, DB_PASSWORD, AWS credentials, OAuth2 keys
**Key libraries**: Spring Boot 3.5.0 (Java 21), Spring Security OAuth2, JPA/MySQL, AWS SDK, SpringDoc OpenAPI

## Pull Request Guidelines
- Follow `.github/pull_request_template.md` structure
- Use Korean for descriptions
- Include issue references (e.g., Closes: SWM-XXX)

## Git Commit Format
- Use `[TICKET-ID] type: description` format
- Extract ticket ID from branch name (e.g., `feat/SWM-309`)
- Always analyze recent commit history to follow established patterns