---
title: "[Reference] Docker 명령어 및 사용법 정리"

tagline: "Docker의 필수 명령어와 옵션을 한눈에 확인하세요"

header:
  overlay_image: /assets/post/tutorial/2026-01-03-docker-reference/overlay.png
  overlay_filter: 0.5

categories:
  - tutorial

tags:
  - Docker
  - Reference
  - CheatSheet
  - 명령어
  - 컨테이너
  - 가이드

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-03T17:00:00+09:00
---

Docker는 애플리케이션을 컨테이너로 패키징하여 어디서나 일관되게 실행할 수 있도록 하는 플랫폼입니다. 이 문서는 Docker의 필수 명령어를 빠르게 찾아보고 실무에 바로 적용할 수 있도록 정리했습니다.

공식 문서: [https://docs.docker.com/](https://docs.docker.com/)

## Docker 아키텍처와 명령어 흐름

<div class="mermaid mermaid-center">
sequenceDiagram
    participant Client as Docker Client
    participant Daemon as Docker Daemon
    participant Registry as Docker Registry
    participant Container as Container
    
    Note over Client,Container: 📦 이미지 가져오기
    Client->>Daemon: docker pull
    Daemon->>Registry: 이미지 요청
    Registry->>Daemon: 이미지 다운로드
    
    Note over Client,Container: 🚀 컨테이너 실행
    Client->>Daemon: docker run
    Daemon->>Container: 컨테이너 생성 및 시작
    
    Note over Client,Container: 🔧 컨테이너 관리
    Client->>Daemon: docker ps/logs/exec
    Daemon->>Container: 명령 실행
    Container->>Daemon: 결과 반환
    Daemon->>Client: 출력 표시
    
    Note over Client,Container: 📤 이미지 배포
    Client->>Daemon: docker push
    Daemon->>Registry: 이미지 업로드
</div>

# 기본 명령어

## 컨테이너 실행 및 관리

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `docker run <image>` | 새 컨테이너 실행 | `docker run nginx` |
| `docker run -d <image>` | 백그라운드로 실행 (detached) | `docker run -d nginx` |
| `docker run -it <image>` | 대화형 모드로 실행 | `docker run -it ubuntu bash` |
| `docker run --name <name>` | 컨테이너 이름 지정 | `docker run --name my-nginx nginx` |
| `docker run -p <host>:<container>` | 포트 매핑 | `docker run -p 8080:80 nginx` |
| `docker run --rm` | 종료 시 자동 삭제 | `docker run --rm ubuntu` |
| `docker ps` | 실행 중인 컨테이너 목록 | `docker ps` |
| `docker ps -a` | 모든 컨테이너 목록 (중지 포함) | `docker ps -a` |
| `docker start <container>` | 중지된 컨테이너 시작 | `docker start my-nginx` |
| `docker stop <container>` | 실행 중인 컨테이너 중지 | `docker stop my-nginx` |
| `docker restart <container>` | 컨테이너 재시작 | `docker restart my-nginx` |
| `docker rm <container>` | 컨테이너 삭제 | `docker rm my-nginx` |
| `docker rm -f <container>` | 실행 중인 컨테이너 강제 삭제 | `docker rm -f my-nginx` |

## 사용 예제

```bash
# Nginx 웹 서버를 8080 포트로 실행
docker run -d -p 8080:80 --name web nginx

# 실행 중인 컨테이너 확인
docker ps

# 컨테이너 중지
docker stop web

# 컨테이너 삭제
docker rm web

# 한 번에 실행, 작업 후 자동 삭제
docker run --rm -it ubuntu bash
```

# 컨테이너 접근 및 로그

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `docker logs <container>` | 컨테이너 로그 조회 | `docker logs web` |
| `docker logs -f <container>` | 로그 실시간 추적 (follow) | `docker logs -f web` |
| `docker logs -n <num>` | 마지막 N개 로그만 조회 | `docker logs -n 100 web` |
| `docker exec <container> <cmd>` | 실행 중인 컨테이너에서 명령 실행 | `docker exec web ls /app` |
| `docker exec -it <container> bash` | 컨테이너에 쉘 접속 | `docker exec -it web bash` |
| `docker attach <container>` | 컨테이너의 표준 입출력에 연결 | `docker attach web` |
| `docker cp <src> <container>:<dest>` | 파일 복사 (호스트→컨테이너) | `docker cp file.txt web:/app/` |
| `docker cp <container>:<src> <dest>` | 파일 복사 (컨테이너→호스트) | `docker cp web:/app/log.txt .` |

## 사용 예제

```bash
# 실행 중인 컨테이너 로그 실시간 확인
docker logs -f web

# 컨테이너 안에서 bash 셸 실행
docker exec -it web bash

# 컨테이너 내부 파일 확인
docker exec web ls -la /var/log

# 호스트에서 컨테이너로 파일 복사
docker cp config.json web:/etc/app/

# 컨테이너에서 호스트로 파일 복사
docker cp web:/var/log/app.log ./logs/
```

# 이미지 관리

## 이미지 라이프사이클

<div class="mermaid mermaid-center">
stateDiagram-v2
    [*] --> Registry: docker pull
    Registry --> Local: 이미지 다운로드
    Local --> Container: docker run
    Container --> Local: docker commit
    Local --> Registry: docker push
    Local --> [*]: docker rmi
    
    note right of Local
        로컬 이미지 저장소
        - docker images
        - docker tag
    end note
</div>

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `docker images` | 로컬 이미지 목록 조회 | `docker images` |
| `docker pull <image>` | 이미지 다운로드 | `docker pull nginx:latest` |
| `docker push <image>` | 이미지 업로드 | `docker push myuser/myapp:1.0` |
| `docker build -t <tag> <path>` | Dockerfile로 이미지 빌드 | `docker build -t myapp:1.0 .` |
| `docker tag <image> <tag>` | 이미지에 태그 추가 | `docker tag myapp:1.0 myapp:latest` |
| `docker rmi <image>` | 이미지 삭제 | `docker rmi nginx` |
| `docker rmi -f <image>` | 이미지 강제 삭제 | `docker rmi -f nginx` |
| `docker image prune` | 사용하지 않는 이미지 삭제 | `docker image prune` |
| `docker image prune -a` | 사용하지 않는 모든 이미지 삭제 | `docker image prune -a` |
| `docker history <image>` | 이미지 레이어 히스토리 조회 | `docker history nginx` |
| `docker inspect <image>` | 이미지 상세 정보 조회 | `docker inspect nginx` |

## 사용 예제

```bash
# 이미지 다운로드
docker pull node:18-alpine

# Dockerfile로 이미지 빌드
docker build -t myapp:1.0 .

# 이미지에 추가 태그 지정
docker tag myapp:1.0 myapp:latest
docker tag myapp:1.0 myuser/myapp:1.0

# Docker Hub에 업로드
docker push myuser/myapp:1.0

# 사용하지 않는 이미지 정리
docker image prune -a
```

# 볼륨 (데이터 영속성)

## 볼륨 타입 비교

<div class="mermaid mermaid-center">
flowchart TD
    Host[호스트 시스템]
    
    subgraph "Docker 스토리지"
        Volume[Volume<br/>Docker 관리 영역]
        Bind[Bind Mount<br/>호스트 특정 경로]
        Tmpfs[Tmpfs Mount<br/>메모리 임시 저장]
    end
    
    Container[컨테이너]
    
    Volume -.->|docker volume| Container
    Bind -.->|직접 마운트| Container
    Tmpfs -.->|메모리| Container
    Host -->|관리| Volume
    Host -->|직접 연결| Bind
    
    style Volume fill:#e8f5e9
    style Bind fill:#fff4e1
    style Tmpfs fill:#e1f5ff
</div>

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `docker volume ls` | 볼륨 목록 조회 | `docker volume ls` |
| `docker volume create <name>` | 볼륨 생성 | `docker volume create mydata` |
| `docker volume inspect <name>` | 볼륨 상세 정보 | `docker volume inspect mydata` |
| `docker volume rm <name>` | 볼륨 삭제 | `docker volume rm mydata` |
| `docker volume prune` | 사용하지 않는 볼륨 삭제 | `docker volume prune` |
| `docker run -v <volume>:<path>` | 볼륨 마운트 | `docker run -v mydata:/data nginx` |
| `docker run -v <host>:<container>` | 바인드 마운트 | `docker run -v /host/path:/container/path nginx` |
| `docker run --mount` | 상세한 마운트 옵션 | `docker run --mount source=mydata,target=/data nginx` |

## 사용 예제

```bash
# 볼륨 생성
docker volume create postgres-data

# 볼륨을 사용하는 컨테이너 실행
docker run -d \
  --name db \
  -v postgres-data:/var/lib/postgresql/data \
  postgres:15

# 호스트 디렉토리를 바인드 마운트
docker run -d \
  --name web \
  -v /home/user/app:/usr/share/nginx/html:ro \
  nginx

# 읽기 전용(ro) 마운트
docker run -v $(pwd):/app:ro myapp

# 볼륨 목록 확인
docker volume ls

# 사용하지 않는 볼륨 정리
docker volume prune
```

# 네트워크

## 네트워크 타입

<div class="mermaid mermaid-center">
flowchart TB
    subgraph "Docker 네트워크 타입"
        Bridge[Bridge<br/>기본 네트워크]
        Host[Host<br/>호스트 네트워크 공유]
        None[None<br/>네트워크 없음]
        Custom[Custom Bridge<br/>사용자 정의]
    end
    
    C1[컨테이너 1]
    C2[컨테이너 2]
    C3[컨테이너 3]
    
    Bridge --> C1
    Bridge --> C2
    Custom --> C3
    
    style Bridge fill:#e1f5ff
    style Custom fill:#e8f5e9
    style Host fill:#fff4e1
    style None fill:#ffebee
</div>

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `docker network ls` | 네트워크 목록 조회 | `docker network ls` |
| `docker network create <name>` | 네트워크 생성 | `docker network create mynet` |
| `docker network inspect <name>` | 네트워크 상세 정보 | `docker network inspect mynet` |
| `docker network rm <name>` | 네트워크 삭제 | `docker network rm mynet` |
| `docker network connect <net> <container>` | 컨테이너를 네트워크에 연결 | `docker network connect mynet web` |
| `docker network disconnect <net> <container>` | 네트워크에서 컨테이너 분리 | `docker network disconnect mynet web` |
| `docker run --network <name>` | 특정 네트워크로 실행 | `docker run --network mynet nginx` |

## 사용 예제

```bash
# 사용자 정의 브리지 네트워크 생성
docker network create --driver bridge myapp-net

# 네트워크에 연결된 컨테이너 실행
docker run -d --name db --network myapp-net postgres
docker run -d --name web --network myapp-net nginx

# 같은 네트워크 내에서 컨테이너 이름으로 통신 가능
docker exec web ping db

# 기존 컨테이너를 네트워크에 연결
docker network connect myapp-net redis

# 네트워크 정보 확인
docker network inspect myapp-net

# 네트워크에서 컨테이너 분리
docker network disconnect myapp-net redis
```

# Docker Compose

## Compose 워크플로우

<div class="mermaid mermaid-center">
sequenceDiagram
    participant User as 개발자
    participant File as docker-compose.yml
    participant Compose as Docker Compose
    participant Docker as Docker Engine
    participant Services as 서비스들
    
    User->>File: 서비스 정의 작성
    User->>Compose: docker compose up
    Compose->>Docker: 이미지 빌드/풀
    Compose->>Docker: 네트워크 생성
    Compose->>Docker: 볼륨 생성
    Docker->>Services: 컨테이너들 시작
    Services->>User: 애플리케이션 실행
    
    User->>Compose: docker compose down
    Compose->>Docker: 컨테이너 중지 및 삭제
    Compose->>Docker: 네트워크 삭제
</div>

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `docker compose up` | 서비스 시작 | `docker compose up` |
| `docker compose up -d` | 백그라운드로 서비스 시작 | `docker compose up -d` |
| `docker compose down` | 서비스 중지 및 삭제 | `docker compose down` |
| `docker compose ps` | 서비스 상태 확인 | `docker compose ps` |
| `docker compose logs` | 서비스 로그 조회 | `docker compose logs` |
| `docker compose logs -f` | 로그 실시간 추적 | `docker compose logs -f` |
| `docker compose exec <service>` | 서비스에서 명령 실행 | `docker compose exec web bash` |
| `docker compose build` | 이미지 빌드 | `docker compose build` |
| `docker compose pull` | 이미지 다운로드 | `docker compose pull` |
| `docker compose restart` | 서비스 재시작 | `docker compose restart` |
| `docker compose stop` | 서비스 중지 (컨테이너 유지) | `docker compose stop` |
| `docker compose start` | 중지된 서비스 시작 | `docker compose start` |

## 사용 예제

```yaml
# docker-compose.yml 예제
version: '3.8'

services:
  web:
    image: nginx:alpine
    ports:
      - "8080:80"
    volumes:
      - ./html:/usr/share/nginx/html
    networks:
      - frontend
    depends_on:
      - api

  api:
    build: ./api
    environment:
      - DATABASE_URL=postgresql://db:5432/mydb
    networks:
      - frontend
      - backend
    depends_on:
      - db

  db:
    image: postgres:15
    environment:
      - POSTGRES_PASSWORD=secret
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - backend

volumes:
  postgres-data:

networks:
  frontend:
  backend:
```

```bash
# 모든 서비스 시작
docker compose up -d

# 특정 서비스만 시작
docker compose up -d web

# 로그 확인
docker compose logs -f api

# 서비스 상태 확인
docker compose ps

# 모든 서비스 중지 및 삭제
docker compose down

# 볼륨까지 함께 삭제
docker compose down -v
```

# 시스템 관리

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `docker system df` | 디스크 사용량 조회 | `docker system df` |
| `docker system prune` | 사용하지 않는 리소스 정리 | `docker system prune` |
| `docker system prune -a` | 모든 사용하지 않는 리소스 정리 | `docker system prune -a` |
| `docker info` | Docker 시스템 정보 | `docker info` |
| `docker version` | Docker 버전 정보 | `docker version` |
| `docker stats` | 컨테이너 리소스 사용량 실시간 확인 | `docker stats` |
| `docker top <container>` | 컨테이너 프로세스 확인 | `docker top web` |

## 사용 예제

```bash
# 디스크 사용량 확인
docker system df

# 중지된 컨테이너, 사용하지 않는 네트워크 등 정리
docker system prune

# 사용하지 않는 이미지도 함께 정리
docker system prune -a

# 모든 컨테이너의 리소스 사용량 실시간 모니터링
docker stats

# Docker 정보 확인
docker info
```

# 자주 사용하는 패턴

## 1. 개발 환경 구성

```bash
# Node.js 개발 환경
docker run -it --rm \
  -v $(pwd):/app \
  -w /app \
  -p 3000:3000 \
  node:18-alpine \
  sh

# 데이터베이스 개발용 실행
docker run -d \
  --name dev-db \
  -e POSTGRES_PASSWORD=dev \
  -e POSTGRES_DB=myapp \
  -p 5432:5432 \
  -v postgres-dev:/var/lib/postgresql/data \
  postgres:15
```

## 2. 로그 및 디버깅

```bash
# 최근 100줄의 로그 확인
docker logs --tail 100 my-container

# 타임스탬프와 함께 로그 실시간 추적
docker logs -f --timestamps my-container

# 컨테이너 리소스 사용량 확인
docker stats my-container

# 컨테이너 내부 프로세스 확인
docker top my-container
```

## 3. 정리 및 최적화

```bash
# 중지된 모든 컨테이너 삭제
docker container prune

# 사용하지 않는 모든 이미지 삭제
docker image prune -a

# 사용하지 않는 볼륨 삭제
docker volume prune

# 한 번에 모두 정리
docker system prune -a --volumes
```

# 팁 & 주의사항

## ✅ 유용한 팁

**별칭(Alias) 설정으로 생산성 향상**
```bash
# ~/.bashrc 또는 ~/.zshrc에 추가
alias dps='docker ps'
alias dpsa='docker ps -a'
alias di='docker images'
alias dex='docker exec -it'
alias dlog='docker logs -f'
alias dcp='docker compose'
```

**컨테이너 이름으로 자동완성**
```bash
# Bash completion 설치
sudo apt-get install bash-completion

# Docker completion 활성화
sudo curl -L https://raw.githubusercontent.com/docker/cli/master/contrib/completion/bash/docker -o /etc/bash_completion.d/docker
```

**자주 사용하는 docker run 옵션 조합**
```bash
# 개발용: 현재 디렉토리 마운트, 자동 삭제, 대화형
docker run -it --rm -v $(pwd):/app -w /app node:18-alpine sh

# 서비스용: 백그라운드, 재시작 정책, 포트 매핑
docker run -d --restart unless-stopped -p 8080:80 nginx
```

## ⚠️ 주의사항

- **`docker rm -f`**: 실행 중인 컨테이너를 강제 삭제하면 데이터 손실 가능
- **`docker system prune -a`**: 사용하지 않는 모든 이미지가 삭제되므로 재빌드 시간 증가 가능
- **볼륨 삭제**: `docker compose down -v`는 데이터를 영구적으로 삭제하므로 주의
- **포트 충돌**: 호스트 포트가 이미 사용 중이면 컨테이너 시작 실패
- **메모리 제한**: 프로덕션에서는 `--memory` 옵션으로 메모리 제한 설정 권장

## 💡 플랫폼별 차이

**Windows**
- Docker Desktop 사용 필요
- WSL2 백엔드 권장
- 볼륨 경로: `/c/Users/...` 형식 사용

**Linux**
- Docker Engine 직접 설치
- sudo 없이 사용하려면 docker 그룹에 사용자 추가: `sudo usermod -aG docker $USER`

**macOS**
- Docker Desktop 사용
- 파일 공유 성능: Bind mount보다 볼륨 사용 권장

## 🔗 관련 문서

- [Docker 공식 문서](https://docs.docker.com/)
- [Docker Hub](https://hub.docker.com/)
- [Dockerfile Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
- [Docker Compose 문서](https://docs.docker.com/compose/)
