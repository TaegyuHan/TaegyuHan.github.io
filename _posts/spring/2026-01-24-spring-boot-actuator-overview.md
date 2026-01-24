---
title: "[Spring Boot] Actuator의 역할 정리"

tagline: "APM/모니터링 체인에서 Actuator의 역할, 제공 정보, 한계 한눈에 보기"

header:
  overlay_image: /assets/post/spring/2026-01-24-spring-boot-actuator-overview/overlay.png
  overlay_filter: 0.5

categories:
  - Spring

tags:
  - Spring Boot
  - Actuator
  - Metrics
  - Health Check
  - Monitoring
  - APM

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-24T12:00:00+09:00
---

Spring Boot 애플리케이션에 APM을 얹을 때 가장 먼저 만나는 것이 Actuator다. 하지만 Actuator 자체가 모니터링·알림·대시보드까지 다 해주는 것은 아니다. 이 글은 Actuator가 **무엇을 제공하고 무엇을 제공하지 않는지**를 분리해 정리한다.

---

## 토론 요약
- Actuator는 Spring Boot 앱 내부 상태를 HTTP 엔드포인트로 내보내는 **진단 모듈**이다.
- 메트릭/헬스/정보를 내보낼 뿐, **저장·질의·시각화·알림**은 하지 않는다. 그것은 Prometheus와 Grafana의 역할이다.
- 운영 보안이 중요하다: 헬스 상세, 환경 정보 등은 인증·네트워크로 보호하거나 최소화해야 한다.

---

## 심화 설명: 제공하는 것 vs. 못 하는 것

### Actuator가 제공하는 것 (제공)
- **헬스**: `/actuator/health`로 liveness/readiness와 DB, 디스크 등 인디케이터 상태를 JSON으로 제공. 참고: Spring Boot 3.5.9 공식 문서의 health 엔드포인트 예시 [링크](https://github.com/spring-projects/spring-boot/blob/v3.5.9/spring-boot-project/spring-boot-actuator-autoconfigure/src/docs/antora/modules/api/pages/rest/actuator/health.adoc).
- **메트릭 목록/단건 조회**: `/actuator/metrics`(목록)와 `/actuator/metrics/{name}`(예: `http.server.requests`, `jvm.memory.used`).
- **Prometheus 스크레이프 포맷**: `/actuator/prometheus`에서 모든 메트릭을 Prometheus 텍스트 포맷으로 노출. 필터링 쿼리 `includedNames`도 지원 [링크](https://github.com/spring-projects/spring-boot/blob/v3.5.9/spring-boot-project/spring-boot-actuator-autoconfigure/src/docs/antora/modules/api/pages/rest/actuator/prometheus.adoc).
- **애플리케이션 정보**: `/actuator/info`로 빌드/버전/임의의 info.* 속성 노출.
- **선택적 진단 엔드포인트**: `/actuator/threaddump`, `/actuator/loggers`, `/actuator/heapdump` 등(노출은 설정 필요).
- **노출 범위 제어**: `management.endpoints.web.exposure.include/exclude`로 공개 엔드포인트를 제어. 전체 공개 예시 [링크](https://github.com/spring-projects/spring-boot/blob/v3.5.9/spring-boot-project/spring-boot-docs/src/docs/antora/modules/reference/pages/actuator/endpoints.adoc).

### Actuator가 제공하지 않는 것 (미제공)
- **장기 보관·질의**: 시계열 저장소와 쿼리는 하지 않는다 → Prometheus, VictoriaMetrics 등 필요.
- **시각화·대시보드**: 대시보드/그래프/탐색은 제공하지 않는다 → Grafana 등 필요.
- **알림**: 임계치 기반 알람을 보내지 않는다 → Prometheus Alertmanager나 Grafana Alerting 필요.
- **강제 인증/인가**: 별도 보안 계층을 내장하지 않는다. Spring Security나 인프라 레벨에서 보호해야 한다.
- **애플리케이션 코드 수정 없이 모든 진단을 자동 제공**: 커스텀 비즈니스 메트릭이나 도메인 헬스는 직접 등록해야 한다.

### 왜 이런 분리가 중요한가?
- **SRE/DevOps 관점**: Actuator는 데이터 소스일 뿐, 모니터링 파이프라인의 시작점이다. 데이터 파이프라인(수집→저장→시각화→알림)을 분리해야 책임이 명확해진다.
- **보안 관점**: 헬스 상세나 환경 정보는 공격 표면이 될 수 있다. 운영 환경에서는 `show-details=when_authorized`와 네트워크 제어를 권장.
- **성능 관점**: `/actuator/prometheus`는 Pull 모델이라 스크레이프 주기가 짧을수록 부하가 늘 수 있다. 엔드포인트는 경량이지만, 레이블 폭발을 막도록 메트릭 태그 설계를 해야 한다.

---

## 실무 적용 예제

### 1) 최소 노출 + Prometheus 스크레이프만 허용
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```
활용: K8s liveness/readiness 프로브는 `/actuator/health`, Prometheus는 `/actuator/prometheus`만 스크레이프. `/metrics`는 사람 탐색용으로만 열어두고, 인증을 붙인다.

### 2) 필수 보안 스니펫 (Spring Security 예시)
```java
@Configuration
public class ActuatorSecurity {

    @Bean
    SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }
}
```
활용: 헬스/프로메테우스는 프로브·스크레이퍼용으로 개방, 나머지는 인증 필요.

### 3) Prometheus 설정 예시 (pull 대상 등록)
```yaml
scrape_configs:
  - job_name: 'kamis-app'
    scrape_interval: 15s
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['kamis.example.com:8080']
        labels:
          application: 'kamis'
```
활용: 애플리케이션 태그를 레이블로 붙여 대시보드/알람에서 필터링.

### 4) 헬스 인디케이터 커스터마이징 개요
- 기본: DB, 디스크, Ping 등 자동 등록.
- 커스텀: `HealthIndicator` 구현체를 빈으로 등록해 도메인 헬스 상태를 추가.
- 보안: `show-details`를 `when-authorized` 이상으로 설정해 민감도 제어.

### 흐름 한눈에 보기
<div class="mermaid mermaid-center">
flowchart LR
    A[Spring Boot Actuator /actuator/prometheus]:::node --> B[Prometheus 스크레이프/저장/알림 룰]:::node
    A --> C[K8s/로드밸런서 /actuator/health]:::node
    B --> D[Grafana 대시보드/Alerting]:::node

    classDef node fill:#1e293b,stroke:#94a3b8,color:#e2e8f0;
    classDef edge stroke:#94a3b8,color:#94a3b8;
</div>

---

## 결론/팁
- Actuator는 **데이터 제공자**다. 저장·질의·시각화·알림은 별도 도구(프로메테우스/그라파나)가 담당한다.
- 운영에서는 **보안 설정**이 최우선: `show-details` 최소화, 인증/네트워크로 보호.
- 레이블(태그) 설계를 신중히: `application`처럼 공통 레이블을 넣어 대시보드·알림 필터를 쉽게 만든다.
- 스크레이프 주기와 엔드포인트 부담을 균형 있게 잡고, 필요 시 `includedNames`로 트래픽을 줄인다.

여기까지 Actuator 편이다. Prometheus/Grafana 편에서 “그다음 단계”를 이어 정리할 예정인데, 추가로 궁금한 포인트나 강조하고 싶은 사례가 있다면 알려 달라.
