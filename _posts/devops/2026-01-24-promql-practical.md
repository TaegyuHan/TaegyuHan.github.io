---
title: "[DevOps] PromQL 실무 패턴: 리눅스+Docker+Spring Boot"

tagline: "노드 익스포터, cAdvisor, Spring Boot Actuator 메트릭을 PromQL로 조회·알림하는 방법"

header:
  overlay_image: /assets/post/devops/2026-01-24-text-practical/overlay.png
  overlay_filter: 0.5

categories:
  - DevOps

tags:
  - PromQL
  - Prometheus
  - NodeExporter
  - cAdvisor
  - SpringBoot
  - Actuator

toc: true
show_date: true
mermaid: true

param_replace:
  - key: "job-top"
    default: "spring-app-1"
  - key: "job-bottom"
    default: "spring-app-2"

last_modified_at: 2026-01-24T16:00:00+09:00
---

리눅스 노드, Docker 컨테이너, Spring Boot Actuator 환경에서 바로 붙여 넣어 쓸 수 있는 최소 PromQL 쿼리만 정리했다. 문법 근거는 Prometheus 공식 문서를 참고한다([PromQL basics](https://prometheus.io/docs/prometheus/latest/querying/basics), [functions](https://prometheus.io/docs/prometheus/2.55/querying/functions), [histograms practice](https://prometheus.io/docs/practices/histograms)).

---

## 1. 리눅스 서버(Node Exporter)

### CPU 사용률(%)
```text
100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```

### 메모리 사용률(%)
```text
(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100
```

### 디스크 사용률(%)
```text
(max by (instance,device) (node_filesystem_size_bytes{fstype!~"tmpfs|overlay"})
 - max by (instance,device) (node_filesystem_free_bytes{fstype!~"tmpfs|overlay"}))
 / max by (instance,device) (node_filesystem_size_bytes{fstype!~"tmpfs|overlay"}) * 100
```

### 네트워크 수신/송신 Bps
```text
sum by (instance) (rate(node_network_receive_bytes_total{device!~"lo"}[5m]))
sum by (instance) (rate(node_network_transmit_bytes_total{device!~"lo"}[5m]))
```

---

## 2. 도커 컨테이너(cAdvisor)

### 컨테이너 CPU 사용률(%)
```text
sum by (container) (rate(container_cpu_usage_seconds_total{image!=""}[5m])) * 100
```

### 컨테이너 메모리 사용 바이트
```text
sum by (container) (container_memory_working_set_bytes{image!=""})
```

### 컨테이너 재시작 감지(최근 10분)
```text
increase(container_last_seen{image!=""}[10m]) < 1
```

### 컨테이너 네트워크 수신/송신 Bps
```text
sum by (container) (rate(container_network_receive_bytes_total{image!=""}[5m]))
sum by (container) (rate(container_network_transmit_bytes_total{image!=""}[5m]))
```

---

{% include post/param-replacer.html key="job-top" default="spring-app-1" %}

{% include post/param-replacer.html key="job-bottom" default="spring-app-2" %}

## 3. Spring Boot Actuator
### RPS
```text
sum by (instance) (rate(http_server_requests_seconds_count{job="spring-app-1"}[1m]))
```

### p95 응답시간
```text
histogram_quantile(0.95,
  sum by (le) (rate(http_server_requests_seconds_bucket{job="spring-app-2"}[5m]))
)
```

### 에러율(5xx 비율)
```text
sum(rate(http_server_requests_seconds_count{job="spring-app-1",status=~"5.."}[5m]))
/
sum(rate(http_server_requests_seconds_count{job="spring-app-2"}[5m]))
```

### JVM 힙 사용량
```text
jvm_memory_used_bytes{job="spring-app-1",area="heap"}
```

### GC Pause 합계(5m)
```text
sum by (gc) (rate(jvm_gc_pause_seconds_sum{job="spring-app-1"}[5m]))
```