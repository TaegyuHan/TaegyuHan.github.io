---
title: "[DevOps] cAdvisor로 컨테이너 메트릭 수집하기"

tagline: "Prometheus와 cAdvisor로 Docker 컨테이너 CPU·메모리 메트릭을 가져오는 최소 설정"

header:
  overlay_image: /assets/post/devops/2026-01-24-cadvisor-install/overlay.png
  overlay_filter: 0.5

categories:
  - DevOps

tags:
  - cAdvisor
  - Prometheus
  - Docker
  - Metrics
  - Monitoring
  - Container

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-24T15:30:00+09:00
---

컨테이너 CPU·메모리·디스크 I/O·네트워크 메트릭은 Prometheus만으로는 보이지 않는다. 호스트에 **cAdvisor**를 띄워야 컨테이너 메트릭이 `/metrics` 형식으로 노출된다. 이 글은 공식 가이드([cAdvisor guide](https://github.com/prometheus/docs/blob/main/docs/guides/cadvisor.md))를 기반으로 최소 설치와 Prometheus 연동 방법을 정리했다.

---

## 왜 cAdvisor가 필요한가
- Prometheus는 “긁어서 저장”만 한다. 컨테이너 런타임의 메트릭을 노출해줄 엔드포인트가 없으면 빈 결과가 나온다.
- cAdvisor는 같은 호스트에서 실행 중인 모든 컨테이너의 CPU/메모리/디스크/네트워크 메트릭을 수집해 `/metrics`로 노출한다.
- Prometheus가 cAdvisor를 스크레이프하면 `container_cpu_usage_seconds_total`, `container_memory_working_set_bytes` 같은 메트릭을 PromQL로 바로 조회할 수 있다.

---

## 설치 (Docker 한 줄 실행)

```bash
docker run -d --name cadvisor --restart=unless-stopped \
  -p 8080:8080 \
  -v /:/rootfs:ro \
  -v /var/run:/var/run:ro \
  -v /sys:/sys:ro \
  -v /var/lib/docker/:/var/lib/docker:ro \
  gcr.io/cadvisor/cadvisor:v0.47.2
```

- 포트: 8080 (필요하면 `-p 8081:8080`처럼 바꿔도 됨)
- 필수 볼륨: `/var/lib/docker`, `/sys`, `/var/run`, `/` → 컨테이너 상태/디스크 정보를 읽기 위해 필요
- 웹 UI: http://localhost:8080/docker/<container> 로 개별 컨테이너 상태 확인 가능

---

## Prometheus 스크레이프 설정

prometheus.yml에 cAdvisor 타깃을 추가한다(공식 예시 참고: [scrape configs](https://github.com/prometheus/docs/blob/main/docs/guides/cadvisor.md)).

```yaml
scrape_configs:
  - job_name: "cadvisor"
    scrape_interval: 5s
    static_configs:
      - targets: ["localhost:8080"]
```

Prometheus를 재시작하거나 `/-/reload`(web.enable-lifecycle 사용 시)로 다시 로드한다. 이후 확인 쿼리:
- `up{job="cadvisor"}` → 1이면 정상 스크레이프
- `container_cpu_usage_seconds_total` → 메트릭이 출력되는지 확인

---

## 자주 쓰는 컨테이너 메트릭 쿼리

### 컨테이너 CPU 사용률(%)
```text
sum by (container) (rate(container_cpu_usage_seconds_total{image!=""}[5m])) * 100
```

### 컨테이너 메모리 사용량
```text
sum by (container) (container_memory_working_set_bytes{image!=""})
```

### 컨테이너 네트워크 수신/송신 Bps
```text
sum by (container) (rate(container_network_receive_bytes_total{image!=""}[5m]))
sum by (container) (rate(container_network_transmit_bytes_total{image!=""}[5m]))
```
### 컨테이너 재시작 감지(최근 10분)
```text
increase(container_last_seen{image!=""}[10m]) < 1
```


라벨이 환경마다 다를 수 있다. 컨테이너 이름 라벨이 `name`이나 `container_label_com_docker_swarm_service_name`인 경우도 있으니, `label_values(container_cpu_usage_seconds_total, name)` 등으로 실제 라벨을 먼저 확인하고 `by (...)` 라벨을 맞춰 사용한다.

---

## 흐름 한눈에 보기
<div class="mermaid mermaid-center">
flowchart LR
    A[cAdvisor<br>컨테이너 메트릭 :8080]:::exporter --> B[Prometheus<br>scrape job cadvisor]:::prom
    B --> C[PromQL<br>container_cpu_usage_seconds_total]:::query
    C --> D[Grafana 대시보드<br>컨테이너 CPU/메모리]:::dash

    classDef exporter fill:#1e293b,stroke:#22c55e,color:#e2e8f0;
    classDef prom fill:#0f172a,stroke:#22d3ee,color:#e0f2fe;
    classDef query fill:#1e293b,stroke:#f59e0b,color:#fef3c7;
    classDef dash fill:#1e293b,stroke:#ec4899,color:#fce7f3;
</div>

---

## 결론/팁
- 컨테이너 메트릭이 비어 있으면 cAdvisor가 없거나 스크레이프 실패일 가능성이 높다. `up{job="cadvisor"}`부터 확인한다.
- 볼륨 마운트가 빠지면 일부 메트릭이 0 또는 노출되지 않을 수 있으니 예시대로 마운트한다.
- 포트를 바꿨다면 prometheus.yml의 타깃도 동일하게 맞춘다.
- K8s/Containerd 환경은 kubelet 내장 cAdvisor나 CRI metrics endpoint를 스크레이프해야 한다(이 글은 단일 Docker 호스트 기준).

이 설정 후에는 Grafana에서 컨테이너 CPU/메모리/네트워크 패널을 바로 만들 수 있다. 문제가 생기면 `up{job="cadvisor"}`와 `container_cpu_usage_seconds_total` 출력 여부를 먼저 확인하자.
