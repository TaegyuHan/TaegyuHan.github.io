---
title: "[DevOps] 프로메테우스(Prometheus)의 역할 정리"

tagline: "메트릭 스크레이프·TSDB·알림까지, Prometheus의 역할 범위와 한계"

header:
  overlay_image: /assets/post/devops/2026-01-24-prometheus-overview/overlay.png
  overlay_filter: 0.5

categories:
  - DevOps

tags:
  - Prometheus
  - Metrics
  - Alerting
  - Exporter
  - TSDB
  - Monitoring

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-24T12:30:00+09:00
---

APM을 구성하면 Actuator가 데이터를 내보내고 Prometheus가 그것을 모아 보관한다. 하지만 Prometheus가 대시보드나 무제한 보관까지 다 해주는 것은 아니다. 이 글은 Prometheus가 **무엇을 해주고 무엇을 해주지 않는지**를 실무 관점에서 정리한다.

---

## 토론 요약
- Prometheus는 **Pull 기반 시계열 수집기 + 내장 TSDB + 규칙/알림 평가기**다.
- 저장·질의는 Prometheus가 하지만, **리치 대시보드와 중앙 알림 라우팅 UI는 Grafana/Alertmanager**가 담당한다.
- 장기 보관·수평 확장은 내장 TSDB 한계를 넘어서기 위해 **remote_write/remote_read**로 외부 TSDB를 붙인다(공식 문서: [remote_write](https://prometheus.io/docs/operating/configuration/#remote_write)).

---

## 제공하는 것 (Prometheus가 해주는 일)
- **스크레이프(Pull)**: `scrape_configs`에 따라 대상의 `/metrics`(Spring Boot는 `/actuator/prometheus`)를 주기적으로 수집.
- **내장 TSDB 저장·압축**: 시계열을 로그 구조로 저장해 빠른 쓰기와 시간 범위 쿼리 제공.
- **PromQL 질의**: 라벨 기반 필터, 집계, 창 연산으로 대시보드·알림에 바로 사용 가능. 기본 문법: [PromQL basics](https://prometheus.io/docs/prometheus/latest/querying/basics/).
- **규칙/알림 평가**: 녹다운 룰·알림 룰을 주기적으로 평가 후 Alertmanager로 전송(설정 예시: [alerting config](https://prometheus.io/docs/prometheus/2.55/configuration/)).
- **페더레이션/리모트 연동**: 여러 Prometheus를 계층화하거나 외부 장기보관으로 보내는 federation/remote_write 지원([federation 예시](https://prometheus.io/docs/prometheus/2.55/federation)).

## 제공하지 않는 것 (Prometheus가 못 하는 일)
- **리치 대시보드/탐색 UI**: 기본 콘솔은 단순하다. 실사용 대시는 Grafana 등에서 처리.
- **장기 무제한 보관·수평 확장**: 단일 Prometheus는 로컬 디스크 보관 기간이 제한적이다. 장기 보관/확장은 remote_write 뒤에 TSDB(VictoriaMetrics, Mimir, Thanos 등)를 둔다.
- **로그·트레이싱 수집**: 메트릭만 담당한다. 로그는 Loki, 트레이싱은 Tempo/Jaeger 같은 별도 스택 필요.
- **알림 라우팅 UI**: Alertmanager가 라우팅/사일런스/집계를 제공하며, Prometheus는 알림을 “발송”만 한다.

---

## 실무 적용 예제

### 1) 최소 스크레이프 설정 예시
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: "farm-predict-kamis-dev"
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["farm.example.com:8080"]
        labels:
          application: "kamis"

alerting:
  alertmanagers:
    - static_configs:
        - targets: ["alertmanager:9093"]
```
설정 항목들은 공식 구성 문서에 상세 설명이 있다([configuration](https://prometheus.io/docs/prometheus/2.55/configuration)).

### 2) 알림 룰 예시 (에러율 1분 평균이 5% 이상)
```yaml
groups:
  - name: app-rules
    rules:
      - alert: HighErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{job="farm-predict-kamis-dev",status=~"5.."}[1m]))
          /
          sum(rate(http_server_requests_seconds_count{job="farm-predict-kamis-dev"}[1m]))
          > 0.05
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "farm-predict-kamis-dev 5xx error rate > 5%"
```
알림 라우팅은 Alertmanager에서 담당한다([alertmanager_config](https://prometheus.io/docs/prometheus/2.55/configuration/#alertmanager_config)).

### 3) 자주 쓰는 PromQL 스니펫 (job만 교체)
- 수집 상태: `up{job="farm-predict-kamis-dev"}`
- RPS: `sum by (instance) (rate(http_server_requests_seconds_count{job="farm-predict-kamis-dev"}[5m]))`
- 에러율: `sum(rate(http_server_requests_seconds_count{job="farm-predict-kamis-dev",status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count{job="farm-predict-kamis-dev"}[5m]))`
- p95 응답시간: `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{job="farm-predict-kamis-dev"}[5m])))`
- JVM 힙: `jvm_memory_used_bytes{job="farm-predict-kamis-dev",area="heap"}`
- 스크레이프 샘플 수: `scrape_samples_post_metric_relabeling{job="farm-predict-kamis-dev"}`

### 4) 데이터가 정말 들어오는지 확인하는 빠른 절차
- Prometheus UI → Status → Targets에서 해당 job이 `UP`인지 확인.
- Graph/콘솔에서 `up{job="farm-predict-kamis-dev"}` 실행(1이면 수집 중).
- 주요 메트릭 쿼리로 값이 그려지는지 확인. 없으면 라벨 불일치나 인증/네트워크를 점검.
- `/actuator/prometheus`를 직접 curl로 열어 실제 메트릭이 노출되는지 확인.

### 흐름 한눈에 보기
<div class="mermaid mermaid-center">
flowchart LR
    A[Spring Boot Actuator /actuator/prometheus]:::node --> B[Prometheus 스크레이프/TSDB/알림 평가]:::node
    B --> C[Alertmanager 라우팅/사일런스]:::node
    B --> D[Grafana 쿼리·대시보드·Alerting]:::node

    classDef node fill:#1e293b,stroke:#94a3b8,color:#e2e8f0;
    classDef edge stroke:#94a3b8,color:#94a3b8;
</div>

---

## 결론/팁
- Prometheus는 **메트릭 수집·저장·PromQL 평가**까지가 역할이다. 대시보드와 알림 라우팅은 Grafana/Alertmanager에 맡긴다.
- 장기 보관·확장이 필요하면 **remote_write** 뒤에 TSDB를 붙인다.
- 라벨 설계와 스크레이프 주기를 조정해 부하와 카디널리티 폭발을 방지한다.
- 알림은 “조건”을 PromQL로 짜고, 라우팅/사일런스 정책은 Alertmanager에서 다룬다.

다음 단계로 Grafana 편에서 PromQL을 그대로 패널/Alerting에 넣는 방법과, 알림 템플릿/대시보드 구성 포인트를 이어서 다룰 예정이다.
