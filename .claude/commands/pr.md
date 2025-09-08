---
allowed-tools: Bash(git branch:*), Bash(git log:*), Bash(git diff:*), Read(.github/pull_request_template.md)
argument-hint: 
description: Create PR title and body using upstream/develop diff analysis
model: sonnet
---

# PR Command

Creates a Pull Request title and body by analyzing changes between upstream/develop and current branch.

## Usage

```
/pr
```

## What it does

1. **Gets current branch**: Extracts current branch name and ticket ID (e.g., `docs/SWM-313` → `SWM-313`)
2. **Analyzes branch changes**: Uses `git diff upstream/develop..HEAD` to see all changes in current branch
3. **Reviews commit history**: Gets commit messages from `git log upstream/develop..HEAD` for context
4. **Reads PR template**: Uses `.github/pull_request_template.md` as the base structure
5. **Generates PR content**: Creates Korean title and body based on actual code changes

## Output format

- **Title**: `[TICKET-ID] type: description` format
- **Body**: Follows the Korean template structure with practical, concise descriptions
- **Clean format**: No pipe characters or formatting that would interfere with copying
- **Ready to use**: Content is formatted for direct copying into GitHub PR creation

## Dependencies

- Requires git repository with upstream/develop branch
- Uses `.github/pull_request_template.md` for structure
- Needs readable commit history and file changes

## Example

```bash
$ /pr

PR Title:
[SWM-313] docs: Claude Code 설정 및 가이드 문서 정리

PR Body:
## 📝 작업 내용 (Description)
- Claude Code 전용 설정 파일 및 커스텀 에이전트 추가
- CLAUDE.md 가이드 문서 업데이트

## ✨ 변경 사항 (Changes)
[✓] .claude/commands/pr.md 커맨드 파일 추가
[✓] CLAUDE.md 프로젝트 가이드라인 업데이트

## ✅ 테스트 방법 (How to Test)
- /pr 커맨드 실행하여 정상 동작 확인

## 🚀관련 이슈 (Related Issue)
Closes: SWM-313
```

## Analysis Method

- **File Changes**: Analyzes `git diff upstream/develop..HEAD --name-status` for modified files
- **Code Changes**: Uses `git diff upstream/develop..HEAD` for detailed code analysis  
- **Commit Context**: Reviews `git log upstream/develop..HEAD --oneline` for implementation context
- **Smart Categorization**: Automatically categorizes changes (feat, fix, docs, etc.) based on file patterns

## Notes

- Focuses on actual code changes rather than PR history analysis
- Content generated in Korean following project template structure
- Optimized for copying directly into GitHub PR interface
- Does not create the PR, only generates title and body content