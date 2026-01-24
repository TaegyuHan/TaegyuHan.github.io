---
title: "[DevOps] 리눅스에 Node Exporter 설치하기"

tagline: "Prometheus로 호스트 CPU·메모리·디스크 메트릭을 수집하기 위한 Node Exporter 설치 가이드"

header:
  overlay_image: /assets/post/devops/2026-01-24-node-exporter-install/overlay.png
  overlay_filter: 0.5

categories:
  - DevOps

tags:
  - NodeExporter
  - Prometheus
  - Monitoring
  - Linux
  - Metrics
  - SystemD

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-24T15:00:00+09:00
---

Prometheus만 설치해도 호스트의 CPU·메모리·디스크·네트워크 메트릭이 자동으로 수집되는 것은 아니다. 리눅스/유닉스 환경에서 이런 시스템 메트릭을 수집하려면 **Node Exporter**라는 작은 에이전트를 설치하고, Prometheus가 이를 스크레이프하도록 설정해야 한다. 이 글은 공식 가이드([Node Exporter 공식 가이드](https://github.com/prometheus/docs/blob/main/docs/guides/node-exporter.md))를 기반으로 최소 설치 과정을 정리한다.

---

## Node Exporter가 필요한 이유

- **Prometheus 자체는 메트릭 저장소**다. 스크레이프할 대상의 `/metrics` 엔드포인트에서 메트릭을 긁어올 뿐, 호스트 OS 메트릭을 직접 수집하지 않는다.
- **Node Exporter**는 리눅스/유닉스 호스트의 CPU·메모리·디스크·네트워크·파일시스템 정보를 `/metrics` 형식으로 노출한다.
- Prometheus가 Node Exporter를 스크레이프하면, `node_cpu_seconds_total`, `node_memory_MemTotal_bytes` 같은 메트릭을 PromQL로 조회할 수 있다.

---

## 설치 과정

### 1) 바이너리 다운로드 및 설치

최신 릴리스는 [Prometheus 다운로드 페이지](https://prometheus.io/download/#node_exporter)에서 확인한다. 아래는 1.7.0 기준 예시다.

```bash
cd /tmp
curl -LO https://github.com/prometheus/node_exporter/releases/download/v1.7.0/node_exporter-1.7.0.linux-amd64.tar.gz
tar xvfz node_exporter-1.7.0.linux-amd64.tar.gz
sudo mv node_exporter-1.7.0.linux-amd64/node_exporter /usr/local/bin/
```

ARM 보드나 다른 아키텍처는 `linux-arm64` 등 맞는 파일을 받아야 한다.

### 2) systemd 서비스 등록

Node Exporter를 백그라운드에서 계속 실행하려면 systemd 서비스로 등록한다.

```bash
sudo tee /etc/systemd/system/node_exporter.service >/dev/null <<'EOF'
[Unit]
Description=Prometheus Node Exporter
After=network.target

[Service]
User=node-exp
Group=node-exp
Type=simple
ExecStart=/usr/local/bin/node_exporter --web.listen-address=:9100

[Install]
WantedBy=multi-user.target
EOF
```

### 3) 실행 계정 생성 및 서비스 시작

```bash
sudo useradd -rs /bin/false node-exp
sudo systemctl daemon-reload
sudo systemctl enable --now node_exporter
sudo systemctl status node_exporter --no-pager
```

정상이면 "active (running)" 상태가 표시된다.

### 4) 방화벽 및 로컬 확인

- 기본 포트: **9100/tcp**
- 로컬에서 메트릭 확인:
```bash
curl http://localhost:9100/metrics
```
`node_cpu_seconds_total`, `node_memory_MemTotal_bytes` 등의 메트릭이 보이면 정상이다.

---

## Prometheus 연동

### 1) prometheus.yml 스크레이프 설정 추가

```yaml
scrape_configs:
  - job_name: "node"
    static_configs:
      - targets: ["localhost:9100"]
```

만약 원격 호스트라면 `["192.168.1.100:9100"]` 형식으로 IP를 지정한다.

### 2) Prometheus 재시작

```bash
sudo systemctl restart prometheus
```

### 3) 스크레이프 상태 확인

Prometheus 웹 UI → Status → Targets에서 `node` job이 **UP** 상태인지 확인한다. 또는 쿼리로 확인:
```text
up{job="node"}
```
결과가 `1`이면 정상이다.

---

## 실무 쿼리 예시

Node Exporter가 정상 작동하면 아래 PromQL을 바로 쓸 수 있다.

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

## 흐름 한눈에 보기

<div class="mermaid mermaid-center">
flowchart LR
    A[리눅스 호스트<br>Node Exporter :9100]:::exporter --> B[Prometheus<br>스크레이프 job node]:::prometheus
    B --> C[PromQL 쿼리<br>node_cpu_seconds_total]:::query
    C --> D[Grafana 대시보드<br>CPU/메모리/디스크]:::dashboard

    classDef exporter fill:#1e293b,stroke:#10b981,color:#e2e8f0;
    classDef prometheus fill:#0f172a,stroke:#22d3ee,color:#e0f2fe;
    classDef query fill:#1e293b,stroke:#f59e0b,color:#fef3c7;
    classDef dashboard fill:#1e293b,stroke:#ec4899,color:#fce7f3;
</div>

---

## 결론/팁

- Prometheus는 메트릭 저장소일 뿐, 호스트 메트릭을 직접 수집하지 않는다. Node Exporter를 설치해야 `node_*` 메트릭을 쓸 수 있다.
- systemd 서비스로 등록하면 재부팅 후에도 자동 실행된다.
- 포트를 바꾸고 싶으면 `--web.listen-address=:9101`처럼 옵션을 주고, Prometheus 타깃도 동일 포트로 맞춘다.
- 윈도우는 windows_exporter(구 wmi_exporter)를 설치하고 기본 포트 9182를 스크레이프한다.
- 공식 가이드: [Monitoring Linux host metrics with the Node Exporter](https://github.com/prometheus/docs/blob/main/docs/guides/node-exporter.md)

이제 Grafana에서 Node Exporter 메트릭을 패널로 만들어 CPU·메모리·디스크·네트워크 대시보드를 구성할 수 있다. 다음 단계로 cAdvisor(컨테이너 메트릭)와 Spring Boot Actuator(애플리케이션 메트릭) 연동을 이어가면 풀스택 모니터링이 완성된다.
