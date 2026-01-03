---
title: "[Reference] Git 명령어 및 사용법 정리"

tagline: "Git의 필수 명령어와 옵션을 한눈에 확인하세요"

header:
  overlay_image: /assets/post/reference/2026-01-03-git-reference/overlay.png
  overlay_filter: 0.5

categories:
  - reference

tags:
  - Git
  - Reference
  - CheatSheet
  - 명령어
  - 버전관리
  - 가이드

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-03T16:11:00
---

Git은 분산 버전 관리 시스템으로, 소스 코드의 변경 이력을 추적하고 여러 개발자가 협업할 수 있도록 지원합니다. 이 문서는 Git의 필수 명령어를 빠르게 찾아보고 실무에 바로 적용할 수 있도록 정리했습니다.

공식 문서: [https://git-scm.com/docs](https://git-scm.com/docs)

## Git의 4가지 영역과 기본 명령어 흐름

<div class="mermaid mermaid-center">
sequenceDiagram
    participant WD as 작업 디렉토리<br/>(Working Directory)
    participant SA as 스테이징 영역<br/>(Staging Area)
    participant LR as 로컬 저장소<br/>(Local Repository)
    participant RR as 원격 저장소<br/>(Remote Repository)
    
    Note over WD,RR: 📝 파일 변경 → 스테이징 → 커밋 → 푸시
    WD->>SA: git add
    SA->>LR: git commit
    LR->>RR: git push
    
    Note over WD,RR: 📥 원격 저장소에서 가져오기
    RR->>LR: git fetch
    RR->>WD: git pull (fetch + merge)
    RR->>WD: git clone (최초)
    
    Note over WD,RR: ⏪ 변경사항 되돌리기
    LR->>WD: git checkout
    SA->>WD: git restore --staged
    LR->>SA: git reset
</div>

# 기본 명령어

## 저장소 초기화 및 기본 작업

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `git init` | 새 Git 저장소 생성 | `git init` |
| `git clone <url>` | 원격 저장소 복제 | `git clone https://github.com/user/repo.git` |
| `git status` | 작업 디렉토리 상태 확인 | `git status` |
| `git add <file>` | 파일을 스테이징 영역에 추가 | `git add index.html` |
| `git add .` | 모든 변경사항 스테이징 | `git add .` |
| `git commit -m "<message>"` | 스테이징된 변경사항 커밋 | `git commit -m "Add new feature"` |
| `git log` | 커밋 이력 조회 | `git log` |
| `git log --oneline` | 커밋 이력을 한 줄로 조회 | `git log --oneline` |
| `git diff` | 변경사항 확인 | `git diff` |
| `git show <commit>` | 특정 커밋 내용 확인 | `git show abc1234` |

## 사용 예제

```bash
# 프로젝트 디렉토리에서 Git 저장소 초기화
cd project
git init

# 모든 파일을 스테이징하고 첫 커밋
git add .
git commit -m "Initial commit"

# 현재 상태 확인
git status

# 마지막 5개의 커밋 확인
git log --oneline -5
```

# 브랜치 관리

## 브랜치 동작 시각화

<div class="mermaid mermaid-center">
gitGraph
    commit id: "Initial commit"
    commit id: "Add feature A"
    branch feature
    checkout feature
    commit id: "Start feature B"
    commit id: "Implement feature B"
    checkout main
    commit id: "Fix bug"
    checkout feature
    commit id: "Complete feature B"
    checkout main
    merge feature tag: "v1.0"
    commit id: "Release"
</div>

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `git branch` | 로컬 브랜치 목록 조회 | `git branch` |
| `git branch <name>` | 새 브랜치 생성 | `git branch feature` |
| `git branch -d <name>` | 브랜치 삭제 | `git branch -d feature` |
| `git branch -D <name>` | 브랜치 강제 삭제 | `git branch -D feature` |
| `git switch <branch>` | 브랜치 전환 (Git 2.23+) | `git switch main` |
| `git switch -c <branch>` | 브랜치 생성 및 전환 | `git switch -c feature` |
| `git checkout <branch>` | 브랜치 전환 (구버전) | `git checkout main` |
| `git checkout -b <branch>` | 브랜치 생성 및 전환 (구버전) | `git checkout -b feature` |
| `git merge <branch>` | 현재 브랜치로 병합 | `git merge feature` |
| `git branch --no-merged` | 병합되지 않은 브랜치 조회 | `git branch --no-merged` |

## 사용 예제

```bash
# 새 기능 개발을 위한 브랜치 생성 및 전환
git switch -c feature/user-login

# 작업 완료 후 main 브랜치로 전환
git switch main

# feature 브랜치를 main으로 병합
git merge feature/user-login

# 병합 완료 후 feature 브랜치 삭제
git branch -d feature/user-login
```

# 원격 저장소

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `git remote` | 원격 저장소 목록 조회 | `git remote` |
| `git remote -v` | 원격 저장소 URL 포함 조회 | `git remote -v` |
| `git remote add <name> <url>` | 원격 저장소 추가 | `git remote add origin https://github.com/user/repo.git` |
| `git fetch <remote>` | 원격 저장소에서 변경사항 가져오기 | `git fetch origin` |
| `git pull <remote> <branch>` | 원격 저장소에서 가져오고 병합 | `git pull origin main` |
| `git push <remote> <branch>` | 로컬 브랜치를 원격으로 푸시 | `git push origin main` |
| `git push -u <remote> <branch>` | 푸시하고 업스트림 설정 | `git push -u origin main` |
| `git push --force` | 강제 푸시 (⚠️ 주의) | `git push --force` |
| `git push --tags` | 태그 푸시 | `git push --tags` |

## 사용 예제

```bash
# 원격 저장소 추가
git remote add origin https://github.com/user/repo.git

# 로컬 커밋을 원격으로 푸시 (처음)
git push -u origin main

# 이후 푸시는 간단하게
git push

# 원격 변경사항 가져오기
git fetch origin

# 원격 변경사항 가져오고 병합
git pull origin main
```

# 변경사항 되돌리기

## Reset 옵션 비교

<div class="mermaid mermaid-center">
flowchart TD
    Start["커밋 상태<br/>Working Dir: 수정됨<br/>Staging: 스테이징됨<br/>Repository: 커밋됨"]
    
    Start -->|git reset --soft HEAD~1| Soft["Working Dir: 수정됨 ✅<br/>Staging: 스테이징됨 ✅<br/>Repository: 커밋 취소 ❌"]
    Start -->|git reset --mixed HEAD~1| Mixed["Working Dir: 수정됨 ✅<br/>Staging: 스테이징 취소 ❌<br/>Repository: 커밋 취소 ❌"]
    Start -->|git reset --hard HEAD~1| Hard["Working Dir: 수정 취소 ❌<br/>Staging: 스테이징 취소 ❌<br/>Repository: 커밋 취소 ❌"]
    
    style Soft fill:#e8f5e9
    style Mixed fill:#fff4e1
    style Hard fill:#ffebee
</div>

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `git reset <file>` | 스테이징 취소 | `git reset index.html` |
| `git reset --soft HEAD~1` | 마지막 커밋 취소 (변경사항 유지) | `git reset --soft HEAD~1` |
| `git reset --mixed HEAD~1` | 마지막 커밋 취소 (스테이징 해제) | `git reset --mixed HEAD~1` |
| `git reset --hard HEAD~1` | 마지막 커밋 취소 (변경사항 삭제) | `git reset --hard HEAD~1` |
| `git revert <commit>` | 특정 커밋을 되돌리는 새 커밋 생성 | `git revert abc1234` |
| `git checkout -- <file>` | 파일 변경사항 취소 | `git checkout -- index.html` |
| `git restore <file>` | 파일 변경사항 취소 (Git 2.23+) | `git restore index.html` |
| `git restore --staged <file>` | 스테이징 취소 (Git 2.23+) | `git restore --staged index.html` |

## 사용 예제

```bash
# 실수로 스테이징한 파일 취소
git restore --staged config.json

# 파일의 변경사항 완전히 취소
git restore config.json

# 마지막 커밋 취소 (변경사항은 유지)
git reset --soft HEAD~1

# 특정 커밋을 되돌리는 새로운 커밋 생성
git revert abc1234
```

# Stash (임시 저장)

## Stash 동작 시각화

<div class="mermaid mermaid-center">
stateDiagram-v2
    [*] --> WorkingDirectory: 작업 중
    WorkingDirectory --> StashStack: git stash
    StashStack --> WorkingDirectory: git stash pop
    StashStack --> StashStack: git stash (여러 번 가능)
    WorkingDirectory --> CleanState: 작업 디렉토리 깨끗
    CleanState --> [*]
    
    note right of StashStack
        stash@{0}: 가장 최근
        stash@{1}: 이전
        stash@{2}: 더 이전
    end note
</div>

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `git stash` | 현재 작업 임시 저장 | `git stash` |
| `git stash save "<message>"` | 메시지와 함께 임시 저장 | `git stash save "WIP: feature"` |
| `git stash list` | 저장된 stash 목록 조회 | `git stash list` |
| `git stash show [<stash>]` | stash 내용 확인 | `git stash show stash@{0}` |
| `git stash apply [<stash>]` | stash 적용 (목록에 유지) | `git stash apply stash@{0}` |
| `git stash pop [<stash>]` | stash 적용 후 삭제 | `git stash pop` |
| `git stash drop [<stash>]` | 특정 stash 삭제 | `git stash drop stash@{0}` |
| `git stash clear` | 모든 stash 삭제 | `git stash clear` |
| `git stash push -u` | 추적되지 않은 파일 포함 | `git stash push -u` |
| `git stash push -p` | 대화형으로 선택 저장 | `git stash push -p` |

## 사용 예제

```bash
# 급하게 다른 브랜치로 이동해야 할 때
git stash save "현재 작업 중인 기능"

# 다른 작업 완료 후 돌아와서 적용
git stash list  # 저장된 stash 확인
git stash pop   # 가장 최근 stash 적용 및 삭제

# 추적되지 않은 파일도 포함하여 stash
git stash push -u
```

# 태그 관리

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `git tag` | 태그 목록 조회 | `git tag` |
| `git tag <name>` | 경량 태그 생성 | `git tag v1.0.0` |
| `git tag -a <name> -m "<message>"` | 주석 태그 생성 | `git tag -a v1.0.0 -m "Release 1.0.0"` |
| `git tag -d <name>` | 로컬 태그 삭제 | `git tag -d v1.0.0` |
| `git push origin <tag>` | 특정 태그 푸시 | `git push origin v1.0.0` |
| `git push --tags` | 모든 태그 푸시 | `git push --tags` |
| `git push origin :refs/tags/<tag>` | 원격 태그 삭제 | `git push origin :refs/tags/v1.0.0` |

## 사용 예제

```bash
# 릴리즈 버전 태그 생성
git tag -a v1.0.0 -m "First stable release"

# 태그를 원격 저장소에 푸시
git push origin v1.0.0

# 모든 태그 푸시
git push --tags
```

# 고급 활용

## Rebase

```bash
# feature 브랜치를 main의 최신 상태로 재배치
git switch feature
git rebase main

# 대화형 rebase로 여러 커밋 정리
git rebase -i HEAD~3

# rebase 중 충돌 해결 후 계속
git rebase --continue

# rebase 중단
git rebase --abort
```

## Cherry-pick

```bash
# 다른 브랜치의 특정 커밋만 가져오기
git cherry-pick abc1234

# 여러 커밋 가져오기
git cherry-pick abc1234 def5678
```

## Clean

```bash
# 추적되지 않은 파일 보기 (dry-run)
git clean -n

# 추적되지 않은 파일 삭제
git clean -f

# 디렉토리도 포함하여 삭제
git clean -fd

# .gitignore에 명시된 파일도 삭제
git clean -fdx
```

# 자주 사용하는 패턴

## 1. Feature 브랜치 워크플로우

### 워크플로우 시각화

<div class="mermaid mermaid-center">
sequenceDiagram
    participant WD as 작업 디렉토리
    participant Main as main 브랜치
    participant Feature as feature 브랜치
    participant Remote as 원격 저장소
    
    WD->>Feature: git switch -c feature/new
    Note over Feature: 새 기능 개발
    WD->>Feature: git add & commit
    Feature->>Remote: git push -u origin feature
    Note over Remote: Pull Request 생성
    Main->>Main: git pull (최신 상태 확인)
    Feature->>Main: git merge (리뷰 완료 후)
    Main->>Remote: git push
    Feature->>Feature: git branch -d (로컬 삭제)
    Remote->>Remote: Delete branch (원격 삭제)
</div>

### 명령어 순서

```bash
# 새 기능 개발 시작
git switch -c feature/new-feature

# 작업 및 커밋
git add .
git commit -m "Implement new feature"

# main 브랜치의 최신 변경사항 반영
git switch main
git pull origin main
git switch feature/new-feature
git rebase main

# 원격에 푸시
git push -u origin feature/new-feature

# PR 생성 및 리뷰 후 main에 병합
git switch main
git merge feature/new-feature
git push origin main

# feature 브랜치 삭제
git branch -d feature/new-feature
git push origin --delete feature/new-feature
```

## 2. 실수한 커밋 수정

```bash
# 마지막 커밋 메시지 수정
git commit --amend -m "Correct commit message"

# 마지막 커밋에 파일 추가
git add forgotten-file.txt
git commit --amend --no-edit

# 여러 커밋 전 상태로 되돌리기
git reset --hard HEAD~3
```

## 3. 충돌 해결

```bash
# pull 중 충돌 발생
git pull origin main

# 충돌 파일 수정 후
git add conflicted-file.txt
git commit -m "Resolve merge conflict"

# 또는 merge 취소
git merge --abort
```

# 팁 & 주의사항

## ✅ 유용한 팁

**별칭(Alias) 설정으로 생산성 향상**
```bash
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.st status
git config --global alias.lg "log --oneline --graph --all"
```

**유용한 로그 조회**
```bash
# 그래프 형태로 브랜치 보기
git log --oneline --graph --all --decorate

# 특정 파일의 변경 이력
git log --follow filename.txt

# 특정 작성자의 커밋
git log --author="John"
```

**대화형 스테이징**
```bash
# 파일의 일부만 선택적으로 스테이징
git add -p
```

## ⚠️ 주의사항

- **`git reset --hard`**: 작업 내용이 완전히 삭제되므로 신중하게 사용
- **`git push --force`**: 공유 브랜치에서는 절대 사용하지 말 것 (다른 사람의 작업을 덮어쓸 수 있음)
- **`git clean -fdx`**: .gitignore 파일도 삭제하므로 빌드 결과물 등이 삭제될 수 있음
- **Rebase vs Merge**: 공유 브랜치에서는 rebase보다 merge 사용 권장

## 💡 플랫폼별 차이

**Windows**
- 줄바꿈 문자 설정: `git config --global core.autocrlf true`
- 파일명 대소문자 구분: `git config --global core.ignorecase false`

**Linux/Mac**
- 줄바꿈 문자 설정: `git config --global core.autocrlf input`
- 파일 권한 변경도 Git이 추적

## 🔗 관련 문서

- [Git 공식 문서](https://git-scm.com/docs)
- [Pro Git 책 (무료)](https://git-scm.com/book/ko/v2)
- [GitHub Git Cheat Sheet](https://training.github.com/downloads/github-git-cheat-sheet/)
- [Git 튜토리얼](https://git-scm.com/docs/gittutorial)
