---
allowed-tools: Bash(git add:*), Bash(git status:*), Bash(git commit:*), Bash(git diff:*), Bash(git log:*)
argument-hint: [message]
description: Create well-formatted commits with smart staging and conventional commit format
model: sonnet
---

# Smart Git Commit

Create well-formatted commit: $ARGUMENTS

## Current Repository State

- Git status: !`git status --porcelain`
- Current branch: !`git branch --show-current`
- Staged changes: !`git diff --cached --stat`
- Unstaged changes: !`git diff --stat`
- Recent commits: !`git log --oneline -5`

## What This Command Does

1. **Analyzes recent git history** by reading the last 5 commits to understand existing commit message conventions and patterns
2. **Extracts ticket ID from branch name** if branch follows `feat/TICKET-ID` or similar pattern
3. Checks the current staging status with `git status`
4. Analyzes all changes (both staged and unstaged) with `git diff` and `git diff --cached`
5. Determines if multiple distinct logical changes are present that should be split into separate commits
6. **Smart commit strategy**:
   - If changes should be split: Guides you through staging specific files for each logical commit using `git add <files>`
   - If changes are cohesive: Stages all changes with `git add .` and creates a single commit
7. For each commit, uses the appropriate staged files and creates a well-formatted commit message
8. **Follows repository-specific conventions** based on recent commit history analysis and branch naming
9. For each commit (or the single commit if not split), creates a commit message using the format `[TICKET-ID] type: description` when ticket ID is available

## Best Practices for Commits

- **Follow existing conventions**: Always analyze recent commit history to match the project's established patterns
- **Atomic commits**: Each commit should contain related changes that serve a single purpose
- **Split large changes**: If changes touch multiple concerns, split them into separate commits
- **Conventional commit format**: Use the format `<type>: <description>` where type is one of:
  - `feat`: A new feature
  - `fix`: A bug fix
  - `docs`: Documentation changes
  - `style`: Code style changes (formatting, etc)
  - `refact`: Code changes that neither fix bugs nor add features
  - `perf`: Performance improvements
  - `test`: Adding or fixing tests
  - `chore`: Changes to the build process, tools, etc.

## Guidelines for Splitting Commits

When analyzing the diff, consider splitting commits based on these criteria:

1. **Different concerns**: Changes to unrelated parts of the codebase
2. **Different types of changes**: Mixing features, fixes, refactoring, etc.
3. **File patterns**: Changes to different types of files (e.g., source code vs documentation)
4. **Logical grouping**: Changes that would be easier to understand or review separately
5. **Size**: Very large changes that would be clearer if broken down

## Important Notes

- **Always analyze recent git history first** to understand and follow the project's commit message conventions
- **Extract ticket ID from branch name** when branch follows patterns like `feat/SWM-309`, `fix/SWM-123`, etc.
- **Intelligent commit splitting**: 
  - Analyzes all changes to detect if they should be split into multiple logical commits
  - When splitting is needed, uses `git add <specific-files>` to stage files for each individual commit
  - When changes are cohesive, stages everything with `git add .` for a single commit
  - Respects any files that are already staged and incorporates them into the commit strategy
- The commit message will be constructed based on the changes detected and existing patterns
- **Multi-step commit process**: When changes need to be split, the command will:
  1. Identify logical groupings of files/changes
  2. Use `git add <specific-files>` to stage files for the first commit
  3. Create and execute the first commit
  4. Repeat the process for remaining changes until all are committed
- Always reviews the commit diff to ensure the message matches the changes
- **Follow the established commit format**: `[TICKET-ID] type: description` when ticket ID is available from branch name
- **DO NOT** include Claude Code author information in commit messages
- Keep commit messages concise and focused on actual changes
- Examples:
  - From `feat/SWM-309` branch: `[SWM-309] feat: 사용자 인증 시스템 추가`
  - From `feat/SWM-309` branch: `[SWM-309] chore: CI 워크플로우에서 데이터베이스 환경 변수 제거`