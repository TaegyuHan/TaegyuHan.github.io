---
title: "[DevOps] 그라파나(Grafana)의 역할 정리"

tagline: "PromQL을 그대로 쓰는 대시보드/Alerting UI, Grafana의 역할 범위"

header:
  overlay_image: /assets/post/devops/2026-01-24-grafana-overview/overlay.png
  overlay_filter: 0.5

categories:
  - DevOps

tags:
  - Grafana
  - Dashboard
  - Alerting
  - Prometheus
  - PromQL
  - Visualization

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-24T13:30:00+09:00
---

Prometheus가 메트릭을 모아두면 Grafana는 그 위에 **시각화·탐색·Alerting UI**를 얹는다. 하지만 Grafana는 데이터를 저장하지 않고, PromQL 쿼리를 그대로 실행해 결과를 보여줄 뿐이다. 이 글은 실무 대화를 통해 정리한 Grafana의 역할과 구체적인 대시보드 구성법을 담는다.

---

## 토론 요약
- Grafana는 **조회·시각화·Alerting UI**를 제공한다. 데이터 저장은 Prometheus가 담당하고, Grafana는 읽기만 한다.
- Prometheus를 데이터소스로 등록하면, Grafana에서 작성한 모든 PromQL이 Prometheus에 실시간으로 실행된다.
- 대시보드를 열 때마다 저장된 쿼리가 다시 실행되어 최신 데이터를 표시한다. 쿼리 결과를 저장하는 것이 아니라, **어떤 쿼리를 실행할지만 저장**한다.
- 패널별 PromQL 쿼리가 가장 중요하다. 올바른 쿼리 없이는 올바른 대시보드를 만들 수 없다.

---

## Grafana의 역할: 데이터소스 → 쿼리 → 시각화

### 1) Prometheus 데이터소스 등록
- Grafana → Settings(톱니바퀴) → Data sources → Add data source
- Type: Prometheus
- URL: Prometheus 서버 주소(예: http://prometheus:9090)
- Save & test
- 이 단계부터 Grafana가 Prometheus의 PromQL을 실행할 준비가 완료된다.

### 2) 쿼리 → 패널 → 대시보드 흐름
```
새 대시보드 → Add visualization 
→ 데이터소스(Prometheus) 선택 
→ PromQL 작성 
→ 시각화 옵션 설정(차트타입, 색상, 단위 등) 
→ 패널 저장 
→ 대시보드에 추가 
→ 대시보드 저장
```
패널을 열 때마다 저장된 쿼리가 Prometheus에 실행되어 최신 데이터를 가져온다.

---

## 실무 예제: 경매 데이터 수집 백엔드 대시보드 구성

경매 데이터를 주기적으로 수집하는 백엔드라면, 일반적인 HTTP 메트릭 외에 **배치/수집 작업 상태**도 봐야 한다.

### 기본 패널 구성
- **HTTP/API 메트릭**: RPS, p95 응답시간, 에러율(5xx), 인스턴스 가용성
- **배치 작업 메트릭**: 마지막 실행 시간, 완료 여부, 처리 건수, 실패 건수
- **데이터 수집 메트릭**: 수집 시간, 시간당 처리 건수, 외부 API 호출 성공/실패율
- **저장소 메트릭**: DB 인서트 성공/실패, 처리량
- **JVM/리소스**: 메모리, GC, 스레드, CPU

### 자주 쓰는 PromQL 패턴 (job은 "farm-predict-kamis-dev"로 교체)
- RPS: `sum by (instance) (rate(http_server_requests_seconds_count{job="$job"}[$__rate_interval]))`
- p95 응답시간: `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{job="$job"}[$__rate_interval])))`
- 에러율: `sum(rate(http_server_requests_seconds_count{job="$job",status=~"5.."}[$__rate_interval])) / sum(rate(http_server_requests_seconds_count{job="$job"}[$__rate_interval]))`
- JVM 힙: `jvm_memory_used_bytes{job="$job",area="heap"}`
- 스크레이프 상태: `up{job="$job"}`

### 패널별 시각화 설정
- RPS, p95, 에러율, GC: **Time series** (시계열 그래프)
- 에러율: 임계값(예: 5%) 표시로 한눈에 이상 여부 판단
- JVM 힙: **Time series** + 단위 설정(Bytes)
- 인스턴스 수: **Stat** (숫자 표시) - `count(up{job="$job"} == 1)`

---

## 템플릿 변수로 재사용성 높이기

동일한 대시보드를 여러 서비스/환경에 재사용할 수 있다.

### 변수 정의
- Dashboard settings → Variables → Add variable
- Name: `job`
- Type: Query
- Data source: Prometheus
- Query: `label_values(http_server_requests_seconds_count, job)`
- Multi-select 활성화하면 여러 서비스 동시 비교 가능

### 쿼리에서 변수 사용
```promql
rate(http_server_requests_seconds_count{job=~"$job"}[$__rate_interval])
```
dropdown에서 job을 선택하면 쿼리가 자동으로 필터된다.

---

## Explore로 쿼리 실험 후 패널에 옮기기

- 좌측 메뉴 → Explore
- 데이터소스: Prometheus 선택
- PromQL 작성 후 라벨/윈도/집계를 실험하며 조정
- 원하는 시계열이 나오면 "Add to dashboard" → 새 패널로 저장
- 이후 패널을 대시보드에서 편집해 공통 변수나 시각화 옵션 추가

---

## Alerting 간단 구성

Grafana Alerting으로 임계값 기반 알림을 만들 수 있다.

### 알림 룰 생성
1. Alert rules → Create new alert rule
2. PromQL 쿼리 입력 (예: `sum(rate(http_server_requests_seconds_count{job="$job",status=~"5.."}[$__rate_interval])) / sum(rate(http_server_requests_seconds_count{job="$job"}[$__rate_interval]))`)
3. 조건 설정 (예: > 0.05, 지속시간 2m)
4. Notifier 선택 (Slack, Email, Webhook 등)
5. 저장

### 역할 분담
- **Grafana Alerting**: 조건 평가 및 알림 발송
- **Alertmanager** (선택): 알림 라우팅, 사일런스, 집계 (더 복잡한 정책 필요 시)

---

## 흐름 한눈에 보기

<div class="mermaid mermaid-center">
flowchart LR
    A[Prometheus 시계열/TSDB]:::node --> B[Grafana 데이터소스  http GET PromQL]:::node
    B --> C[Grafana Dashboard  패널 쿼리 실행]:::node
    C --> D[시각화 차트/Stat/테이블]:::node
    
    C --> E[Grafana Alerting 임계값 평가]:::node
    E --> F[Notifier Slack/Email/Webhook]:::node
    
    classDef node fill:#1e293b,stroke:#94a3b8,color:#e2e8f0;
</div>

---

## 결론/팁
- **쿼리가 핵심**: 올바른 PromQL 없이는 올바른 대시보드를 만들 수 없다.
- **실시간 실행**: 대시보드를 열 때마다 쿼리가 Prometheus에 실행되므로, 항상 최신 데이터를 본다.
- **Explore 활용**: 쿼리를 완성한 뒤 대시보드에 옮기는 것이 효율적이다.
- **변수로 재사용**: 동일한 대시보드 구조를 여러 서비스에 맞춰 쓸 수 있다.
- **언어 설정**: Preferences → Language에서 한국어를 선택할 수 있지만, 번역이 부분적일 수 있다.
