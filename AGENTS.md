# Repository Guidelines

## Project Structure & Module Organization

- `src/main/java`: Spring Boot code under `com.climbx.climbx` (Controller/Service/Repository,
  dto/entity/exception/config).
- `src/main/resources`: app configs (Swagger, logging). Dev YAMLs also under `bin/main` for local
  runs.
- `src/test/java`: JUnit 5 tests and fixtures (`.../fixture/*`).
- `docker/dev/mysql`: local MySQL via Compose.

## Build, Test, and Development Commands

- `./gradlew clean build`: Compile, run tests, Checkstyle, package.
- `./gradlew bootRun`: Run locally (see README env vars, MySQL in Docker).
- `./gradlew test`: Unit/integration tests with JUnit Platform.
- `./gradlew checkstyleMain checkstyleTest`: Lint Java sources.
- `./gradlew jacocoTestReport`: Generate coverage at
  `build/reports/jacoco/test/jacocoTestReport.xml` (Sonar uses this).
- `cd docker/dev/mysql && docker-compose up -d`: Start local DB.

## Coding Style & Naming Conventions

- Language: Java 21. Indent 4 spaces; Google Checkstyle (`config/checkstyle/google_checks.xml`, zero
  warnings allowed).
- Packages: lowercase (e.g., `com.climbx.climbx.problem`). Classes: `PascalCase` (e.g.,
  `ProblemService`).
- Suffixes: `Controller`, `Service`, `Repository`, `Entity`, `Dto`, `Exception`.
- Lombok allowed; prefer constructor injection (no field injection).
- REST docs at `/api/swagger` via springdoc; keep DTOs immutable when feasible.

## Testing Guidelines

- Frameworks: Spring Boot Test, JUnit 5, Security Test.
- Location: mirror main packages under `src/test/java`; name tests `*Test` (e.g.,
  `ProblemServiceTest`).
- Coverage: Jacoco report required in CI; add tests for services and edge cases.
- Use fixtures in `.../fixture` and test resources in `src/test/resources`.

## Commit & Pull Request Guidelines

- Commit style: `[SWM-123] type: summary` (types: feat, fix, refact, test, chore).
- PRs: use `.github/pull_request_template.md`; include description, changes checklist, how to test,
  related issue (`Closes: SWM-123`).
- Branches: PRs to `develop` run tests; pushes to `develop` build and push to ECR; pushes to `main`
  run prod CI.

## Security & Configuration Tips

- Do not commit secrets. Use env vars from README and GitHub Secrets in workflows.
- Profiles: set `SPRING_PROFILES_ACTIVE` (`dev`/`prod`). Configure DB via
  `DB_URL/DB_USER/DB_PASSWORD`.
