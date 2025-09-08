---
allowed-tools: Bash(git add:*), Bash(git status:*), Bash(git commit:*), Bash(git diff:*), Bash(git log:*)
argument-hint: [message]
description: Create well-formatted commits with conventional commit format and emoji
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
3. Checks which files are staged with `git status`
4. If 0 files are staged, automatically adds all modified and new files with `git add`
5. Performs a `git diff` to understand what changes are being committed
6. Analyzes the diff to determine if multiple distinct logical changes are present
7. If multiple distinct changes are detected, suggests breaking the commit into multiple smaller commits
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
- If specific files are already staged, the command will only commit those files  
- If no files are staged, it will automatically stage all modified and new files
- The commit message will be constructed based on the changes detected and existing patterns
- Before committing, the command will review the diff to identify if multiple commits would be more appropriate
- If suggesting multiple commits, it will help you stage and commit the changes separately
- Always reviews the commit diff to ensure the message matches the changes
- **Follow the established commit format**: `[TICKET-ID] type: description` when ticket ID is available from branch name
- **DO NOT** include Claude Code author information in commit messages
- Keep commit messages concise and focused on actual changes
- Examples:
  - From `feat/SWM-309` branch: `[SWM-309] feat: 사용자 인증 시스템 추가`
  - From `feat/SWM-309` branch: `[SWM-309] chore: CI 워크플로우에서 데이터베이스 환경 변수 제거`