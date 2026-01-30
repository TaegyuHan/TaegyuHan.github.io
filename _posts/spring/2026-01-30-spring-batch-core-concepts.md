---
title: "[Spring] Spring Batch 입문자를 위한 핵심 개념 완벽 이해"

tagline: "거래 처리 사례로 배우는 Job, Step, Chunk, Listener의 모든 것"

header:
  overlay_image: /assets/post/spring/2026-01-30-spring-batch-core-concepts/overlay.png
  overlay_filter: 0.5

categories:
  - Spring

tags:
  - spring-batch
  - 배치-처리
  - job-step
  - itemreader-writer
  - 청크-처리
  - 에러-처리

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-30T23:48:00
---

매일 새벽 2시, 은행 시스템에서는 수백만 개의 거래 내역을 처리합니다. 

만약 이 작업을 **일반적인 웹 애플리케이션**으로 처리한다면?
- 메모리 부족으로 중단됨
- 오류 하나로 전체 거래가 실패
- 재시작 시 처음부터 다시 처리
- 진행 상황을 추적하기 어려움

**Spring Batch**는 이런 대용량 데이터를 효율적으로 처리하기 위해 태어났습니다. 그런데 처음 배우는 입장에서는 Job, Step, ItemReader, Listener 등 낯선 개념들이 많습니다.

이 글에서는 **거래 내역 일괄 처리**라는 현실적인 예시를 통해 Spring Batch의 핵심 개념을 차근차근 설명하겠습니다.

---

## 1. Job과 Step: 계층적 구조의 이해

### Job이란? "전체 작업의 집합"

매일 새벽 2시에 거래 내역을 처리하는 프로세스를 상상해보세요:

1. 은행 시스템에서 거래 파일 수신
2. 파일을 읽어서 데이터 검증
3. 데이터베이스에 저장
4. 통계 계산
5. 최종 리포트 생성

이 **"전체 거래 처리 프로세스"**를 **Job**이라고 부릅니다.

### Step이란? "Job 내의 개별 작업"

위의 프로세스를 더 자세히 보면, 실제로는 **3개의 독립적인 Step**으로 나뉩니다:

```
🎯 Job: 거래 내역 일괄 처리
  ├─ 📋 Step 1: 거래 데이터 검증 & 저장 (Read → Validate → Write)
  ├─ 📊 Step 2: 통계 계산
  └─ 📄 Step 3: 최종 리포트 생성
```

각 Step은:
- **순차적으로 실행** (Step 1 → Step 2 → Step 3)
- **독립적으로 실행** 가능 (필요하면 특정 Step만 재실행 가능)
- **자신의 상태 관리** (시작/실패/완료 정보 기록)

<div class="mermaid mermaid-center">
graph LR
    A["🎯 Job<br/>거래 처리"] --> B["📋 Step 1<br/>검증 & 저장"]
    B --> C["📊 Step 2<br/>통계 계산"]
    C --> D["📄 Step 3<br/>리포트 생성"]
    
    style A fill:#2d3748,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style B fill:#1a202c,stroke:#68d391,stroke-width:2px,color:#e2e8f0
    style C fill:#1a202c,stroke:#68d391,stroke-width:2px,color:#e2e8f0
    style D fill:#1a202c,stroke:#68d391,stroke-width:2px,color:#e2e8f0
</div>

---

## 2. Read → Process → Write: 기본 흐름의 이해

Step 1 "거래 데이터 검증 & 저장"은 Spring Batch의 핵심 패턴인 **ItemReader → ItemProcessor → ItemWriter** 구조를 따릅니다.

### ItemReader: 데이터를 읽다

```java
@Bean
public FlatFileItemReader<Transaction> transactionReader() {
    return new FlatFileItemReaderBuilder<Transaction>()
        .name("transactionReader")
        .resource(new FileSystemResource("transactions.csv"))
        .delimited()
        .names("id", "amount", "date")
        .targetType(Transaction.class)
        .build();
}
```

- CSV, Excel, 데이터베이스 등 다양한 소스에서 데이터 읽음
- **청크 단위로** 처리 (예: 100개씩)

### ItemProcessor: 데이터를 검증/변환하다

```java
@Component
public class TransactionProcessor implements ItemProcessor<Transaction, Transaction> {
    @Override
    public Transaction process(Transaction transaction) throws Exception {
        // 거래 금액 검증
        if (transaction.getAmount() <= 0) {
            throw new InvalidTransactionException("유효하지 않은 금액");
        }
        
        // 거래 상태 설정
        transaction.setStatus("PROCESSED");
        return transaction;
    }
}
```

- 데이터 검증, 변환, 필터링 담당
- **검증 실패 시 예외 발생** → Listener에서 캐치

### ItemWriter: 데이터를 저장하다

```java
@Bean
public RepositoryItemWriter<Transaction> transactionWriter(TransactionRepository repository) {
    RepositoryItemWriter<Transaction> writer = new RepositoryItemWriter<>();
    writer.setRepository(repository);
    writer.setMethodName("save");
    return writer;
}
```

- 검증된 데이터를 데이터베이스에 저장
- **배치 단위로** 저장 (예: 100개씩 한 번에)

<div class="mermaid mermaid-center">
graph LR
    A["📖 ItemReader<br/>CSV 파일에서<br/>거래 100개 읽음"] --> B["✅ ItemProcessor<br/>각 거래 검증"]
    B --> C{검증 결과?}
    C -->|성공| D["💾 ItemWriter<br/>DB에 저장"]
    C -->|실패| E["🔔 Listener<br/>오류 로그 테이블 저장"]
    
    style A fill:#1a202c,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style B fill:#1a202c,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style C fill:#2d3748,stroke:#f6ad55,stroke-width:2px,color:#e2e8f0
    style D fill:#1a202c,stroke:#68d391,stroke-width:2px,color:#e2e8f0
    style E fill:#1a202c,stroke:#fc8181,stroke-width:2px,color:#e2e8f0
</div>

---

## 3. 청크(Chunk): 메모리와 성능의 균형

여기서 중요한 개념이 **청크(Chunk)**입니다.

### 청크란?

한 번에 처리할 데이터의 단위입니다. 예를 들어, **청크 사이즈가 100**이면:

```
1차: 1~100번 거래 읽기 → 검증 → 저장
2차: 101~200번 거래 읽기 → 검증 → 저장
3차: 201~300번 거래 읽기 → 검증 → 저장
...
```

### 왜 청크를 사용할까?

**1개씩 처리:**
```
거래 1: Read → Process → Write
거래 2: Read → Process → Write
...
거래 100만: Read → Process → Write
```
❌ 연산 오버헤드가 너무 큼

**100~1000개씩 청크 처리:**
```
청크 1: 1~100번 거래 동시 처리
청크 2: 101~200번 거래 동시 처리
...
```
✅ 성능과 메모리 균형

**10,000개씩 처리:**
```
한 번에 10,000개를 메모리에 로드
```
❌ 메모리 부하, 실패 시 재처리 범위 커짐

### 청크 사이즈 설정

```java
@Bean
public Step transactionStep() {
    return stepBuilderFactory.get("transactionStep")
        .<Transaction, Transaction>chunk(1000)  // 1000개씩 청크 처리
        .reader(transactionReader())
        .processor(transactionProcessor())
        .writer(transactionWriter())
        .build();
}
```

**권장 청크 사이즈:**
- 데이터 크기가 작으면 (예: 간단한 숫자) → 1000개 이상
- 데이터 크기가 크면 (예: 이미지, 복잡한 객체) → 100개 이하
- 실제 운영 환경에서 성능 테스트 후 결정

---

## 4. 에러 처리: Listener를 통한 우아한 실패 관리

100만 개의 거래 중 **몇 개가 포맷 오류**가 있다면?

일반적인 처리:
```
❌ 한 건 오류 발생 → 전체 Job 중단
```

Spring Batch의 처리:
```
✅ 한 건 오류 발생 → 로그 남기고 다음 거래 계속 처리
```

### Listener 구현

```java
@Component
public class TransactionProcessListener implements ItemProcessListener<Transaction, Transaction> {
    
    private final ErrorLogRepository errorLogRepository;
    
    @Override
    public void onProcessError(Transaction item, Exception e) {
        // 오류난 거래를 별도 테이블에 저장
        ErrorLog errorLog = ErrorLog.builder()
            .transactionId(item.getId())
            .errorMessage(e.getMessage())
            .errorTime(LocalDateTime.now())
            .status("FAILED")
            .build();
        
        errorLogRepository.save(errorLog);
    }
    
    @Override
    public void onProcessSuccess(Transaction item, Transaction result) {
        // 성공한 거래에 대한 처리 (필요시)
    }
}
```

### Step에 Listener 등록

```java
@Bean
public Step transactionStep() {
    return stepBuilderFactory.get("transactionStep")
        .<Transaction, Transaction>chunk(1000)
        .reader(transactionReader())
        .processor(transactionProcessor())
        .writer(transactionWriter())
        .listener(transactionProcessListener())  // Listener 등록
        .build();
}
```

### 흐름 정리

<div class="mermaid mermaid-center">
graph LR
    A["📖 ItemReader<br/>거래 읽음"] --> B["✅ ItemProcessor<br/>검증"]
    B --> C{검증<br/>성공?}
    C -->|YES| D["💾 ItemWriter<br/>정상 저장"]
    C -->|NO| E["🔔 Listener<br/>onProcessError"]
    E --> F["📝 ErrorLog 테이블<br/>오류 기록"]
    D --> G["다음 청크 처리"]
    F --> G
    
    style A fill:#1a202c,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style B fill:#1a202c,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style C fill:#2d3748,stroke:#f6ad55,stroke-width:2px,color:#e2e8f0
    style D fill:#1a202c,stroke:#68d391,stroke-width:2px,color:#e2e8f0
    style E fill:#1a202c,stroke:#fc8181,stroke-width:2px,color:#e2e8f0
    style F fill:#1a202c,stroke:#fc8181,stroke-width:2px,color:#e2e8f0
    style G fill:#2d3748,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
</div>

---

## 5. 실제 오류 로그 테이블 설계

오류를 효과적으로 추적하기 위한 테이블 구조:

```sql
CREATE TABLE error_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    error_message VARCHAR(500),
    error_type VARCHAR(100),
    error_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**각 컬럼의 의미:**
- `transaction_id`: 어떤 거래가 실패했는지
- `error_message`: 왜 실패했는지
- `error_type`: VALIDATION_ERROR, DB_ERROR 등 분류
- `retry_count`: 재처리 시도 횟수 추적
- `status`: FAILED, RETRYING, RESOLVED 등

이렇게 하면:
- 📊 **통계**: 처리된 거래 vs 실패한 거래 비교
- 🔄 **재처리**: 실패한 거래만 선택해서 다시 처리
- 📋 **감사**: 누가/언제/왜 실패했는지 기록

---

## 6. 전체 흐름 다시 보기

```
🎯 매일 새벽 2시 자동 실행 (Scheduler)
  ↓
📋 Job 시작: 거래 내역 일괄 처리
  ↓
📖 Step 1: 거래 데이터 검증 & 저장
  ├─ ItemReader: CSV에서 1000개씩 읽음
  ├─ ItemProcessor: 각 거래 검증
  │   ├─ ✅ 성공 → ItemWriter에서 DB 저장
  │   └─ ❌ 실패 → Listener에서 ErrorLog 테이블에 기록
  ↓
📊 Step 2: 통계 계산
  ├─ 전체 거래 수
  ├─ 성공한 거래 수
  ├─ 실패한 거래 수
  ↓
📄 Step 3: 최종 리포트 생성
  ├─ CSV로 내보내기
  └─ 관리자에게 메일 발송
  ↓
✅ Job 완료
```

---

## 7. Spring Batch를 사용해야 하는 이유 정리

| 기능 | 일반 웹앱 | Spring Batch |
|------|---------|-------------|
| **메모리 효율성** | 모든 데이터 메모리 로드 | 청크 단위로 처리 |
| **에러 처리** | 하나 실패 → 전체 실패 | 일부 실패해도 계속 진행 |
| **재시작** | 처음부터 다시 처리 | 마지막 지점부터 재개 |
| **모니터링** | 구현 필요 | 내장 JobRepository로 추적 |
| **스케줄링** | 추가 라이브러리 필요 | Spring Scheduler 통합 |

---

## 결론: 핵심 개념 요약

| 개념 | 설명 | 예시 |
|------|------|------|
| **Job** | 전체 작업 단위 | 거래 처리 전체 프로세스 |
| **Step** | 작업 내 개별 단계 | 검증&저장, 통계, 리포트 |
| **ItemReader** | 데이터 읽기 | CSV 파일에서 거래 읽음 |
| **ItemProcessor** | 데이터 검증/변환 | 거래 금액 유효성 검증 |
| **ItemWriter** | 데이터 저장 | DB에 거래 정보 저장 |
| **Chunk** | 한 번에 처리할 단위 | 1000개씩 묶어서 처리 |
| **Listener** | 이벤트 감시자 | 오류 발생 시 ErrorLog 저장 |

---

## 다음 단계

이제 기본 개념은 이해했습니다. 다음에 배워야 할 것들:

- ✅ **지금 배운 것**: Job, Step, Reader/Processor/Writer, Chunk, Listener
- 🔜 **다음에 배울 것**: JobRepository, ExecutionContext, Scheduler 통합, 고급 에러 처리 (Skip, Retry)

---

**여러분의 실무에서는 Spring Batch를 어떻게 활용하고 있나요?** 거래 처리 외에 다른 사례가 있다면 댓글로 공유해주세요!
