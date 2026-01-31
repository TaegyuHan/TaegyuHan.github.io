---
title: "[Reference] Windows 명령 프롬프트(CMD) 명령어 정리"

tagline: "윈도우 CMD의 필수 명령어를 한눈에 확인하세요"

header:
  overlay_image: /assets/post/reference/2026-02-01-windows-cmd-reference/overlay.png
  overlay_filter: 0.5

categories:
  - Reference

tags:
  - Windows
  - CMD
  - Command
  - Reference
  - CheatSheet
  - 명령어
  - 가이드

toc: true
show_date: true

last_modified_at: 2026-02-01T00:00:00+09:00
---

Windows 명령 프롬프트(Command Prompt, CMD)는 Windows 운영체제의 기본 커맨드 라인 인터페이스입니다. 파일 및 디렉토리 관리, 시스템 정보 조회, 네트워크 설정 등 다양한 시스템 작업을 수행할 수 있습니다. 이 문서는 자주 사용되는 CMD 명령어들을 카테고리별로 정리했습니다.

# 파일 및 디렉토리 관련 명령어

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `dir` | 현재 디렉토리의 파일 및 폴더 목록 표시 | `dir` / `dir /s` (하위 폴더 포함) |
| `cd [경로]` | 디렉토리 이동 | `cd C:\Users` / `cd ..` (상위 폴더) |
| `mkdir [폴더명]` | 새 폴더 생성 | `mkdir NewFolder` / `mkdir C:\Users\NewFolder` |
| `rmdir [폴더명]` | 빈 폴더 삭제 | `rmdir OldFolder` / `rmdir /s /q Folder` (하위 포함) |
| `del [파일명]` | 파일 삭제 | `del file.txt` / `del *.txt` (모든 txt 파일) |
| `copy [원본] [대상]` | 파일 복사 | `copy source.txt destination.txt` |
| `move [원본] [대상]` | 파일 이동 또는 이름 변경 | `move oldname.txt newname.txt` |
| `ren [기존명] [새이름]` | 파일 이름 변경 | `ren oldname.txt newname.txt` |
| `attrib [옵션] [파일]` | 파일 속성 변경 | `attrib +r file.txt` (읽기 전용) / `attrib -h file.txt` (숨김 제거) |
| `tree [경로]` | 디렉토리 구조를 트리 형태로 표시 | `tree` / `tree C:\Users` / `tree /f` (파일 포함) |

## 파일 생성 명령어

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `type nul > [파일명]` | 빈 파일 생성 | `type nul > newfile.txt` |
| `echo. > [파일명]` | 한 줄이 포함된 파일 생성 | `echo. > newfile.txt` |
| `echo [내용] > [파일명]` | 지정한 내용으로 파일 생성 | `echo Hello > greeting.txt` |
| `echo [내용] >> [파일명]` | 파일에 내용 추가 (기존 내용 유지) | `echo World >> greeting.txt` |
| `(echo line1 & echo line2) > [파일명]` | 여러 줄의 내용으로 파일 생성 | `(echo line1 & echo line2) > file.txt` |

## 파일 관련 명령어 사용 예제

```powershell
# 현재 디렉토리의 파일 목록 확인
dir

# 현재 디렉토리 및 하위 디렉토리의 모든 파일 표시 (상세 정보 포함)
dir /s /b

# 새 폴더 생성
mkdir MyFolder

# 빈 파일 생성
type nul > newfile.txt

# 특정 내용의 파일 생성
echo Hello World > greeting.txt

# 파일에 내용 추가
echo Goodbye World >> greeting.txt

# 여러 줄의 내용으로 파일 생성
(echo Line 1) > multiline.txt
(echo Line 2 & echo Line 3) >> multiline.txt

# 파일 복사
copy C:\source\file.txt C:\destination\file.txt

# 여러 파일 한 번에 삭제
del C:\temp\*.log

# 파일명 일괄 변경 (ren 또는 rename 사용 가능)
ren oldfile.txt newfile.txt

# 읽기 전용 속성 설정
attrib +r important.txt

# 디렉토리 구조 파일로 저장
tree C:\ /f > directory_tree.txt
```

---

# 시스템 정보 및 관리 명령어

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `systeminfo` | 시스템의 상세 정보 표시 | `systeminfo` |
| `tasklist` | 현재 실행 중인 프로세스 목록 표시 | `tasklist` / `tasklist /v` (상세 정보) |
| `taskkill /PID [ID]` | 지정한 PID의 프로세스 종료 | `taskkill /PID 1234` / `taskkill /IM notepad.exe /F` (강제 종료) |
| `ipconfig` | 네트워크 설정 정보 표시 | `ipconfig` / `ipconfig /all` (전체 정보) |
| `ping [호스트]` | 네트워크 연결 상태 확인 | `ping google.com` / `ping 8.8.8.8` |
| `netstat` | 네트워크 연결 상태 및 포트 사용 현황 표시 | `netstat` / `netstat -ano` (PID 포함) |
| `chkdsk [드라이브]: /f` | 디스크 오류 검사 및 수정 | `chkdsk C: /f` |
| `sfc /scannow` | 시스템 파일 검사 및 복구 | `sfc /scannow` (관리자 권한 필요) |
| `shutdown /s /t [초]` | 지정한 시간 후 컴퓨터 종료 | `shutdown /s /t 3600` (1시간 후) / `shutdown /a` (취소) |
| `ver` | Windows 버전 정보 표시 | `ver` |
| `cls` | 명령 프롬프트 화면 지우기 | `cls` |

## 시스템 관리 명령어 사용 예제

```powershell
# 시스템 정보 확인
systeminfo

# 현재 실행 중인 프로세스 목록 조회 (상세 정보 포함)
tasklist /v

# 특정 프로세스 강제 종료
taskkill /IM notepad.exe /F

# 특정 포트를 사용 중인 프로세스 확인
netstat -ano | findstr :8080

# 네트워크 구성 확인
ipconfig /all

# 특정 호스트에 대한 연결 확인
ping google.com

# 디스크 오류 검사 및 수정 예약 (다음 부팅 시 실행)
chkdsk C: /f /R

# 시스템 파일 검사 및 복구 (관리자 권한 필요)
sfc /scannow

# 1시간 후 컴퓨터 종료
shutdown /s /t 3600

# 예정된 종료 취소
shutdown /a

# 컴퓨터 재부팅 (30초 후)
shutdown /r /t 30 /c "정기 유지보수"

# 현재 Windows 버전 확인
ver

# 명령 프롬프트 화면 지우기
cls
```

---

# 네트워크 관련 명령어

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `net use [드라이브]: \\[서버]\[폴더]` | 네트워크 드라이브 연결 | `net use Z: \\192.168.1.100\Share` |
| `net share [이름]=[경로]` | 폴더를 네트워크에 공유 | `net share MyShare=C:\SharedFolder` |
| `tracert [호스트]` | 네트워크 경로 추적 | `tracert google.com` |
| `nslookup [도메인]` | 도메인 이름의 IP 주소 조회 | `nslookup google.com` |
| `arp -a` | ARP 캐시 테이블 표시 | `arp -a` / `arp -d` (캐시 삭제) |
| `ipconfig /release` | DHCP에서 IP 주소 해제 | `ipconfig /release` |
| `ipconfig /renew` | DHCP에서 새 IP 주소 획득 | `ipconfig /renew` |
| `net stop [서비스명]` | 서비스 중지 | `net stop "Windows Update"` |
| `net start [서비스명]` | 서비스 시작 | `net start "Windows Update"` |

## 네트워크 관련 명령어 사용 예제

```powershell
# 네트워크 드라이브 연결
net use Z: \\192.168.1.100\Share /user:username password

# 특정 폴더를 네트워크에 공유
net share MyShare=C:\MyFolder /grant:Everyone,FULL

# 네트워크 공유 목록 확인
net share

# 네트워크 드라이브 연결 해제
net use Z: /delete

# 도메인 이름의 IP 주소 조회
nslookup google.com

# 특정 DNS 서버를 통한 도메인 조회
nslookup google.com 8.8.8.8

# 네트워크 경로 추적
tracert google.com

# ARP 캐시 테이블 확인
arp -a

# ARP 캐시 항목 추가
arp -s 192.168.1.100 00-aa-bb-cc-dd-ee

# DHCP에서 IP 주소 갱신
ipconfig /release
ipconfig /renew

# DNS 캐시 초기화
ipconfig /flushdns

# 서비스 시작
net start "Windows Update"

# 서비스 중지
net stop "Windows Update"
```

---

# 고급 활용 패턴

## 파일 검색 및 처리

```powershell
# 특정 파일명 검색
dir /s /b *.txt

# 파일 내용에서 특정 문자열 검색
findstr "keyword" C:\Folder\*.txt

# 특정 확장자 모든 파일 삭제
del /s /q C:\Folder\*.tmp

# 폴더의 크기 확인
dir /s C:\Folder | find "File(s)"
```

## 프로세스 관리

```powershell
# 특정 프로세스의 메모리 사용량 확인
tasklist /v | findstr notepad

# 포트를 사용 중인 프로세스 찾기
netstat -ano | findstr :8080

# 종료된 프로세스 재시작
tasklist | findstr notepad || (start notepad.exe)
```

## 파일 생성 및 편집

```powershell
# 빈 파일 생성
type nul > empty.txt

# 기본 설정 파일 생성
echo [DEFAULT] > config.ini
echo timeout=30 >> config.ini

# 배치 파일 생성
(
echo @echo off
echo setlocal enabledelayedexpansion
echo for /r %%f in (*.log) do del "%%f"
echo echo 로그 파일 정리 완료
) > cleanup.bat

# CSV 파일 생성
echo Name,Age,Email > data.csv
echo John,25,john@example.com >> data.csv
echo Jane,30,jane@example.com >> data.csv

# 여러 파일 한 번에 생성
for /L %%i in (1,1,5) do type nul > file%%i.txt
```

## 배치 파일 자동화

```powershell
# REM 파일 백업
copy C:\Important\data.txt C:\Backup\data_%date:~0,10%.txt

REM 로그 파일 정리 (7일 이상된 파일 삭제)
forfiles /S /M *.log /D +7 /C "cmd /c del @file"

REM 폴더 크기 계산
dir /s C:\Folder
```

# 자주 사용하는 패턴

| 상황 | 명령어 |
|------|--------|
| 현재 디렉토리 확인 | `cd` |
| 디렉토리 목록 상세 조회 | `dir /s /b` |
| 빈 파일 생성 | `type nul > filename.txt` |
| 텍스트 파일 생성 | `echo content > filename.txt` |
| 파일에 내용 추가 | `echo content >> filename.txt` |
| CSV 파일 생성 | `echo field1,field2 > data.csv` |
| 파일 일괄 삭제 | `del /s /q C:\Path\*.ext` |
| 네트워크 연결 상태 확인 | `ipconfig /all` |
| 포트 사용 현황 확인 | `netstat -ano` |
| 특정 포트 사용 프로세스 찾기 | `netstat -ano \| findstr :PORT` |
| 프로세스 강제 종료 | `taskkill /IM process.exe /F` |
| 디렉토리 구조 파일로 저장 | `tree /f > tree.txt` |
| 파일 내용 검색 | `findstr "text" C:\Path\*.txt` |
| 파일 복사 진행률 보기 | `xcopy source dest /Y /E /V` |

# 팁 & 주의사항

- ⚠️ `del` 명령어는 파일을 영구 삭제합니다. `/s` 옵션과 함께 사용할 때 특히 주의가 필요합니다.

- ⚠️ 관리자 권한이 필요한 명령어: `sfc`, `chkdsk`, `shutdown`, 네트워크 공유 설정 등. 명령 프롬프트를 관리자 권한으로 실행해야 합니다.

- ⚠️ `rmdir /s /q`는 폴더와 그 내용을 즉시 삭제합니다. 확인 없이 삭제되므로 매우 주의가 필요합니다.

- 💡 파이프(`|`)를 사용하여 명령어 결과를 필터링할 수 있습니다: `tasklist | findstr notepad`

- 💡 `>` 기호로 명령어 실행 결과를 파일로 저장할 수 있습니다: `dir > output.txt`

- 💡 `/h` 또는 `/?` 옵션으로 각 명령어의 자세한 도움말을 확인할 수 있습니다: `dir /?`

- 💡 배치 파일(`.bat`)을 이용하여 반복되는 명령어들을 자동화할 수 있습니다.

- 💡 `ipconfig /flushdns`를 사용하여 DNS 캐시를 초기화할 수 있습니다. 네트워크 문제 해결 시 유용합니다.

- 💡 PowerShell을 사용하면 더 강력한 기능과 스크립팅 능력을 활용할 수 있습니다.

- 🔗 [Microsoft Windows Commands Documentation](https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/windows-commands)

- 🔗 [Windows Terminal 공식 문서](https://learn.microsoft.com/en-us/windows/terminal/)

- 🔗 [PowerShell 공식 문서](https://learn.microsoft.com/en-us/powershell/)
