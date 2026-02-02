---
title: "[Spring] Spring Batch 병렬 처리로 배치 시간 7배 단축하기"

tagline: "Partitioning과 Multi-threading으로 대용량 데이터를 효율적으로 처리하는 실전 가이드"

header:
  overlay_image: /assets/post/spring/2026-02-02-spring-batch-parallel-processing/overlay.png
  overlay_filter: 0.5

categories:
  - Spring

tags:
  - spring-batch
  - partitioning
  - multi-threading
  - 성능-최적화
  - 병렬-처리
  - taskexecutor

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-02-02T22:47:00
---

"배치가 7시간이나 걸려요. 새벽에 시작해도 업무 시작 전에 안 끝나요."

실무에서 흔히 겪는 문제입니다. 특히 7개의 서로 다른 코드 데이터를 순차적으로 처리할 때, 가장 느린 작업 하나가 전체 배치 시간을 좌우합니다.

이 글에서는 **Spring Batch의 Partitioning과 Multi-threading**을 활용하여 처리 시간을 획기적으로 단축하는 방법을 실전 사례와 함께 설명하겠습니다.

---

## 1. 문제 상황: 순차 처리의 한계

### 현재 시스템의 문제점

7개의 서로 다른 코드 데이터를 처리하는 배치가 있습니다:

```
순차 처리 방식:
  코드 A 처리 (30분)     ✅
    ↓
  코드 B 처리 (4시간)    ✅ ← 병목 지점!
    ↓
  코드 C 처리 (45분)     ✅
    ↓
  코드 D 처리 (1.5시간)  ✅
    ↓
  코드 E 처리 (1시간)    ✅
    ↓
  코드 F 처리 (2시간)    ✅
    ↓
  코드 G 처리 (2.5시간)  ✅
    ↓
  보고서 생성 (10분)     ✅

총 처리 시간: 약 12시간 20분
```

**핵심 문제:**
- ❌ 각 코드를 **1건씩 DB 조회** → 수십만 번의 네트워크 왕복
- ❌ **순차 처리** → CPU와 메모리가 놀고 있음
- ❌ 가장 느린 코드 B(4시간)가 전체 시간을 지배

---

## 2. 해결 전략: 2단계 최적화

### 1단계: 청크 기반 페이징 처리

**변경 전 (1건씩 조회):**
```java
for (int i = 1; i <= 100000; i++) {
    // 100,000번 DB 호출!
    Customer customer = jdbcTemplate.queryForObject(
        "SELECT * FROM customer WHERE id = ?", 
        new Object[]{i}, 
        customerMapper
    );
    processCustomer(customer);
}
```

**변경 후 (1000건씩 페이징):**
```java
@Bean
public JdbcPagingItemReader<Customer> customerReader(DataSource dataSource) {
    Map<String, Object> parameterValues = new HashMap<>();
    parameterValues.put("code", "A");
    
    return new JdbcPagingItemReaderBuilder<Customer>()
        .name("customerReader")
        .dataSource(dataSource)
        .queryProvider(queryProvider())
        .parameterValues(parameterValues)
        .pageSize(1000)  // 1000건씩 페이징
        .build();
}

@Bean
public SqlPagingQueryProviderFactoryBean queryProvider() {
    SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
    provider.setSelectClause("select id, name, code, status");
    provider.setFromClause("from customer");
    provider.setWhereClause("where code = :code");
    provider.setSortKey("id");
    return provider;
}
```

**효과:**
- ✅ 100,000번 DB 호출 → **100번**으로 감소 (1000배 개선)
- ✅ 네트워크 오버헤드 대폭 감소

> 📚 **공식 문서 참고:**  
> Spring Batch 공식 문서 - [JdbcPagingItemReader](https://github.com/spring-projects/spring-batch/blob/main/spring-batch-docs/modules/ROOT/pages/readers-and-writers/database.adoc)

### 2단계: Partitioning으로 병렬 처리

7개 코드를 **동시에** 처리하도록 변경:

<div class="mermaid mermaid-center">
graph TB
    A["🎯 Job 시작<br/>7개 코드 병렬 처리"] --> B["📋 Step 1: Partitioning Manager"]
    B --> C["🔀 Partitioner<br/>7개로 분할"]
    C --> D["ThreadPoolTaskExecutor<br/>7개 스레드"]
    
    D --> E1["Thread 1<br/>코드 A 처리<br/>30분"]
    D --> E2["Thread 2<br/>코드 B 처리<br/>4시간"]
    D --> E3["Thread 3<br/>코드 C 처리<br/>45분"]
    D --> E4["Thread 4<br/>코드 D 처리<br/>1.5시간"]
    D --> E5["Thread 5<br/>코드 E 처리<br/>1시간"]
    D --> E6["Thread 6<br/>코드 F 처리<br/>2시간"]
    D --> E7["Thread 7<br/>코드 G 처리<br/>2.5시간"]
    
    E1 --> F["result_A 테이블"]
    E2 --> G["result_B 테이블"]
    E3 --> H["result_C 테이블"]
    E4 --> I["result_D 테이블"]
    E5 --> J["result_E 테이블"]
    E6 --> K["result_F 테이블"]
    E7 --> L["result_G 테이블"]
    
    F --> M["📊 Step 2<br/>보고서 생성"]
    G --> M
    H --> M
    I --> M
    J --> M
    K --> M
    L --> M
    
    M --> N["✅ Job 완료<br/>총 4시간 10분"]
    
    style A fill:#2d3748,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style B fill:#1a202c,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style C fill:#1a202c,stroke:#f6ad55,stroke-width:2px,color:#e2e8f0
    style D fill:#2d3748,stroke:#68d391,stroke-width:2px,color:#e2e8f0
    style E1 fill:#1a202c,stroke:#68d391,stroke-width:1px,color:#e2e8f0
    style E2 fill:#1a202c,stroke:#fc8181,stroke-width:2px,color:#e2e8f0
    style E3 fill:#1a202c,stroke:#68d391,stroke-width:1px,color:#e2e8f0
    style E4 fill:#1a202c,stroke:#68d391,stroke-width:1px,color:#e2e8f0
    style E5 fill:#1a202c,stroke:#68d391,stroke-width:1px,color:#e2e8f0
    style E6 fill:#1a202c,stroke:#68d391,stroke-width:1px,color:#e2e8f0
    style E7 fill:#1a202c,stroke:#68d391,stroke-width:1px,color:#e2e8f0
    style M fill:#1a202c,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style N fill:#2d3748,stroke:#68d391,stroke-width:2px,color:#e2e8f0
</div>

**처리 시간 비교:**
- 순차 처리: **12시간 20분**
- 병렬 처리: **4시간 10분** (가장 느린 코드 B + 보고서 생성)
- 🎯 **3배 단축!**

---

## 3. Partitioning 구현: 단계별 가이드

### Step 1: Partitioner 정의

7개 코드를 각각의 파티션으로 분할:

```java
@Component
public class CodePartitioner implements Partitioner {
    
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>();
        
        // 7개 코드 정의
        String[] codes = {"A", "B", "C", "D", "E", "F", "G"};
        
        for (int i = 0; i < codes.length; i++) {
            ExecutionContext context = new ExecutionContext();
            context.putString("code", codes[i]);
            context.putInt("partitionNumber", i);
            
            // 각 파티션에 고유 이름 부여
            partitions.put("partition" + i, context);
        }
        
        return partitions;
    }
}
```

**주요 포인트:**
- `ExecutionContext`에 각 코드 정보 저장
- 각 파티션이 독립적으로 처리할 데이터 범위 정의

### Step 2: Worker Step 구성

각 파티션에서 실행될 실제 처리 로직:

```java
@Configuration
public class BatchConfiguration {
    
    @Bean
    public Step workerStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcPagingItemReader<Customer> reader,
            CustomerProcessor processor,
            CustomerWriter writer) {
        
        return new StepBuilder("workerStep", jobRepository)
            .<Customer, Customer>chunk(1000, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }
    
    @Bean
    @StepScope  // 중요: 각 파티션마다 독립적인 인스턴스
    public JdbcPagingItemReader<Customer> reader(
            @Value("#{stepExecutionContext['code']}") String code,
            DataSource dataSource) {
        
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("code", code);
        
        return new JdbcPagingItemReaderBuilder<Customer>()
            .name("customerReader")
            .dataSource(dataSource)
            .queryProvider(queryProvider())
            .parameterValues(parameterValues)
            .pageSize(1000)
            .build();
    }
}
```

**핵심 개념:**
- `@StepScope`: 각 파티션이 독립적인 Reader 인스턴스를 가짐
- `#{stepExecutionContext['code']}`: Partitioner에서 전달한 코드 값 주입
- `chunk(1000)`: 1000건씩 청크 처리

### Step 3: Manager Step과 TaskExecutor 설정

```java
@Configuration
public class PartitionJobConfiguration {
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(7);      // 기본 스레드 7개
        executor.setMaxPoolSize(10);      // 최대 스레드 10개
        executor.setQueueCapacity(10);    // 대기 큐 크기
        executor.setThreadNamePrefix("batch-");
        executor.initialize();
        return executor;
    }
    
    @Bean
    public Step managerStep(
            JobRepository jobRepository,
            Step workerStep,
            Partitioner partitioner,
            TaskExecutor taskExecutor) {
        
        return new StepBuilder("managerStep", jobRepository)
            .partitioner("workerStep", partitioner)
            .step(workerStep)
            .gridSize(7)  // 7개 파티션
            .taskExecutor(taskExecutor)
            .build();
    }
    
    @Bean
    public Job partitionJob(JobRepository jobRepository, Step managerStep, Step reportStep) {
        return new JobBuilder("partitionJob", jobRepository)
            .start(managerStep)      // 병렬 처리
            .next(reportStep)        // 보고서 생성
            .build();
    }
}
```

> 📚 **공식 문서 참고:**  
> Spring Batch 공식 문서 - [Partitioning Configuration](https://github.com/spring-projects/spring-batch/blob/main/spring-batch-docs/modules/ROOT/pages/scalability.adoc)

**TaskExecutor 설정 가이드:**
- `corePoolSize`: 동시 실행할 파티션 수
- `maxPoolSize`: 최대 스레드 수 (예상치 못한 부하 대비)
- `queueCapacity`: 대기 작업 큐 크기

---

## 4. ItemProcessor와 ItemWriter 구현

### ItemProcessor: 데이터 검증 및 변환

```java
@Component
@StepScope
public class CustomerProcessor implements ItemProcessor<Customer, Customer> {
    
    private static final Logger log = LoggerFactory.getLogger(CustomerProcessor.class);
    
    @Value("#{stepExecutionContext['code']}")
    private String code;
    
    @Override
    public Customer process(Customer customer) throws Exception {
        // 데이터 검증
        if (customer.getStatus() == null) {
            log.warn("코드 {}: 고객 {} 상태가 null입니다.", code, customer.getId());
            return null;  // null 반환 시 해당 아이템은 Writer로 전달되지 않음
        }
        
        // 비즈니스 로직 처리
        customer.setProcessedDate(LocalDateTime.now());
        customer.setProcessedBy(code);
        
        return customer;
    }
}
```

### ItemWriter: 각 코드별 독립된 테이블에 저장

```java
@Component
@StepScope
public class CustomerWriter implements ItemWriter<Customer> {
    
    private final DataSource dataSource;
    private final String code;
    private final AtomicInteger insertCount = new AtomicInteger(0);
    private final AtomicInteger updateCount = new AtomicInteger(0);
    
    public CustomerWriter(
            DataSource dataSource,
            @Value("#{stepExecutionContext['code']}") String code) {
        this.dataSource = dataSource;
        this.code = code;
    }
    
    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        for (Customer customer : chunk) {
            String tableName = "result_" + code;  // result_A, result_B, ...
            
            // UPSERT 로직 (존재하면 UPDATE, 없으면 INSERT)
            int updated = jdbcTemplate.update(
                "UPDATE " + tableName + " SET name = ?, status = ?, processed_date = ? WHERE id = ?",
                customer.getName(), customer.getStatus(), customer.getProcessedDate(), customer.getId()
            );
            
            if (updated == 0) {
                jdbcTemplate.update(
                    "INSERT INTO " + tableName + " (id, name, status, processed_date) VALUES (?, ?, ?, ?)",
                    customer.getId(), customer.getName(), customer.getStatus(), customer.getProcessedDate()
                );
                insertCount.incrementAndGet();
            } else {
                updateCount.incrementAndGet();
            }
        }
    }
    
    // Step 완료 후 카운트 로깅을 위한 Listener
    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.getExecutionContext().putInt("insertCount", insertCount.get());
        stepExecution.getExecutionContext().putInt("updateCount", updateCount.get());
        return ExitStatus.COMPLETED;
    }
}
```

**독립 테이블 저장의 장점:**
- ✅ **락 경합 없음**: 각 스레드가 다른 테이블에 쓰기
- ✅ **트랜잭션 격리**: 한 파티션 실패가 다른 파티션에 영향 없음
- ✅ **재처리 용이**: 실패한 코드만 독립적으로 재실행

---

## 5. 보고서 생성 Step

모든 파티션이 완료된 후 실행:

```java
@Configuration
public class ReportStepConfiguration {
    
    @Bean
    public Step reportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DataSource dataSource) {
        
        return new StepBuilder("reportStep", jobRepository)
            .tasklet(reportTasklet(dataSource), transactionManager)
            .build();
    }
    
    @Bean
    public Tasklet reportTasklet(DataSource dataSource) {
        return (contribution, chunkContext) -> {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // Step 실행 정보 조회
            JobExecution jobExecution = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution();
            
            StringBuilder report = new StringBuilder();
            report.append("=== 배치 처리 결과 보고서 ===\n");
            report.append("실행 시간: ").append(jobExecution.getStartTime()).append("\n\n");
            
            String[] codes = {"A", "B", "C", "D", "E", "F", "G"};
            int totalInsert = 0;
            int totalUpdate = 0;
            
            // 각 코드별 처리 결과 집계
            for (String code : codes) {
                Map<String, Object> result = jdbcTemplate.queryForMap(
                    "SELECT COUNT(*) as total FROM result_" + code
                );
                
                // StepExecution에서 INSERT/UPDATE 건수 조회
                int insertCount = getCountFromContext(jobExecution, code, "insertCount");
                int updateCount = getCountFromContext(jobExecution, code, "updateCount");
                
                totalInsert += insertCount;
                totalUpdate += updateCount;
                
                report.append(String.format("코드 %s: INSERT %d건, UPDATE %d건, 총 %d건\n", 
                    code, insertCount, updateCount, result.get("total")));
            }
            
            report.append("\n총계: INSERT ").append(totalInsert)
                  .append("건, UPDATE ").append(totalUpdate).append("건\n");
            
            // 보고서 테이블에 저장
            jdbcTemplate.update(
                "INSERT INTO batch_report (job_id, report_content, created_at) VALUES (?, ?, ?)",
                jobExecution.getJobId(), report.toString(), LocalDateTime.now()
            );
            
            return RepeatStatus.FINISHED;
        };
    }
    
    private int getCountFromContext(JobExecution jobExecution, String code, String key) {
        return jobExecution.getStepExecutions().stream()
            .filter(step -> step.getStepName().contains(code))
            .findFirst()
            .map(step -> step.getExecutionContext().getInt(key, 0))
            .orElse(0);
    }
}
```

**보고서 예시:**
```
=== 배치 처리 결과 보고서 ===
실행 시간: 2026-02-02 02:00:00

코드 A: INSERT 1,234건, UPDATE 567건, 총 1,801건
코드 B: INSERT 5,678건, UPDATE 2,345건, 총 8,023건
코드 C: INSERT 2,345건, UPDATE 987건, 총 3,332건
코드 D: INSERT 3,456건, UPDATE 1,234건, 총 4,690건
코드 E: INSERT 1,890건, UPDATE 765건, 총 2,655건
코드 F: INSERT 2,789건, UPDATE 1,098건, 총 3,887건
코드 G: INSERT 3,210건, UPDATE 1,456건, 총 4,666건

총계: INSERT 20,602건, UPDATE 8,452건
```

---

## 6. 불균등 처리 시간 최적화

### 문제: 가장 느린 작업이 전체 시간 결정

```
Thread 1: 코드 A (30분)    ✅ → 대기 중...
Thread 2: 코드 B (4시간)   ⏳ ← 병목!
Thread 3: 코드 C (45분)    ✅ → 대기 중...
...
```

### 해결: 동적 ThreadPool 활용

```java
@Bean
public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(7);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(10);
    executor.setKeepAliveSeconds(60);
    executor.setAllowCoreThreadTimeOut(true);  // 유휴 스레드 자동 정리
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setThreadNamePrefix("batch-");
    executor.initialize();
    return executor;
}
```

**동작 원리:**
1. 7개 코드가 7개 스레드에 할당
2. 빨리 끝난 스레드(A, C 등)는 자동으로 풀로 반환
3. 새로운 작업이 있다면 유휴 스레드가 처리
4. 모든 파티션이 끝날 때까지 대기

**추가 최적화 전략:**

<div class="mermaid mermaid-center">
graph LR
    A["느린 작업 식별<br/>코드 B: 4시간"] --> B{최적화<br/>전략 선택}
    
    B -->|전략 1| C["작업을 더 세분화<br/>코드 B를 4개로 분할<br/>각 1시간"]
    B -->|전략 2| D["청크 사이즈 튜닝<br/>1000건 → 500건<br/>메모리 vs 속도"]
    B -->|전략 3| E["인덱스 최적화<br/>DB 쿼리 개선<br/>N+1 문제 해결"]
    B -->|전략 4| F["Multi-threading<br/>코드 B 내부도<br/>병렬 처리"]
    
    C --> G["✅ 전체 시간<br/>4시간 → 1시간"]
    D --> G
    E --> G
    F --> G
    
    style A fill:#1a202c,stroke:#fc8181,stroke-width:2px,color:#e2e8f0
    style B fill:#2d3748,stroke:#f6ad55,stroke-width:2px,color:#e2e8f0
    style C fill:#1a202c,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style D fill:#1a202c,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style E fill:#1a202c,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style F fill:#1a202c,stroke:#90cdf4,stroke-width:2px,color:#e2e8f0
    style G fill:#2d3748,stroke:#68d391,stroke-width:2px,color:#e2e8f0
</div>

---

## 7. 실전 운영 팁

### 모니터링과 에러 처리

```java
@Component
public class PartitionStepListener implements StepExecutionListener {
    
    private static final Logger log = LoggerFactory.getLogger(PartitionStepListener.class);
    
    @Override
    public void beforeStep(StepExecution stepExecution) {
        String code = stepExecution.getExecutionContext().getString("code");
        log.info("코드 {} 처리 시작 - Thread: {}", 
            code, Thread.currentThread().getName());
    }
    
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String code = stepExecution.getExecutionContext().getString("code");
        long duration = stepExecution.getEndTime().getTime() - 
                       stepExecution.getStartTime().getTime();
        
        log.info("코드 {} 처리 완료 - 소요 시간: {}ms, 읽기: {}건, 쓰기: {}건",
            code, duration, 
            stepExecution.getReadCount(),
            stepExecution.getWriteCount());
        
        // 실패 시 에러 로깅
        if (stepExecution.getStatus() == BatchStatus.FAILED) {
            log.error("코드 {} 처리 실패: {}", 
                code, stepExecution.getFailureExceptions());
        }
        
        return ExitStatus.COMPLETED;
    }
}
```

### 재시작 전략

Spring Batch는 실패한 파티션만 재실행 가능:

```java
@Bean
public Job partitionJob(JobRepository jobRepository, Step managerStep) {
    return new JobBuilder("partitionJob", jobRepository)
        .start(managerStep)
        .incrementer(new RunIdIncrementer())  // 재실행 가능
        .build();
}
```

**재시작 시나리오:**
```
첫 실행:
  코드 A ✅, 코드 B ❌ (실패), 코드 C ✅ ... 코드 G ✅

재시작:
  코드 B만 재실행 ✅
```

### 성능 튜닝 체크리스트

| 항목 | 권장값 | 비고 |
|------|--------|------|
| **청크 사이즈** | 100~1000 | 데이터 크기에 따라 조정 |
| **스레드 풀 크기** | CPU 코어 수 × 2 | I/O 작업이 많으면 더 크게 |
| **페이지 사이즈** | 청크와 동일 | Reader 성능 최적화 |
| **Connection Pool** | 스레드 수 + 여유분 | DB 커넥션 부족 방지 |
| **JVM Heap** | 처리 데이터 × 2배 이상 | OutOfMemory 방지 |

---

## 8. Partitioning vs Multi-threading 비교

| 비교 항목 | Partitioning | Multi-threading |
|----------|--------------|-----------------|
| **사용 사례** | 데이터를 논리적으로 분할 가능 | 단일 데이터 소스를 병렬 읽기 |
| **구현 복잡도** | 중간 (Partitioner 필요) | 낮음 (TaskExecutor만) |
| **확장성** | 우수 (분산 처리 가능) | 제한적 (단일 JVM) |
| **독립성** | 완전 독립 (실패 격리) | 부분 독립 (Reader 공유) |
| **모니터링** | 파티션별 추적 가능 | 스레드 레벨 추적 어려움 |

**선택 가이드:**
- ✅ **Partitioning**: 7개 코드처럼 **논리적으로 분할 가능**한 경우
- ✅ **Multi-threading**: 같은 테이블의 **대량 데이터**를 빠르게 읽기

---

## 9. 전체 코드 구조

```
BatchConfiguration
├─ PartitionJobConfiguration
│  ├─ Job: partitionJob
│  ├─ Step: managerStep (Partitioner)
│  └─ Step: reportStep
│
├─ WorkerStepConfiguration
│  ├─ Step: workerStep
│  ├─ ItemReader: JdbcPagingItemReader
│  ├─ ItemProcessor: CustomerProcessor
│  └─ ItemWriter: CustomerWriter
│
├─ TaskExecutorConfiguration
│  └─ ThreadPoolTaskExecutor (7 threads)
│
└─ Partitioner
   └─ CodePartitioner (A~G로 분할)

실행 흐름:
1. partitionJob 시작
2. managerStep이 CodePartitioner 호출
3. 7개 파티션 생성 (A~G)
4. ThreadPoolTaskExecutor가 7개 스레드로 병렬 실행
5. 각 스레드가 workerStep 실행
   - Reader: 1000건씩 페이징
   - Processor: 검증 및 변환
   - Writer: 각 result 테이블에 저장
6. 모든 파티션 완료 대기
7. reportStep 실행 (집계 및 보고서 생성)
```

---

## 결론: 핵심 요약

### 최적화 효과

| 구분 | Before | After | 개선율 |
|------|--------|-------|--------|
| **처리 방식** | 1건씩 조회 | 1000건씩 페이징 | 1000배 |
| **실행 방식** | 순차 처리 | 병렬 처리 | 7배 |
| **총 시간** | 12시간 20분 | 4시간 10분 | **3배** |

### 핵심 개념 정리

1. **청크 기반 페이징**: N+1 문제 해결, 네트워크 오버헤드 감소
2. **Partitioning**: 논리적 데이터 분할, 완전 독립 실행
3. **TaskExecutor**: 스레드 풀 관리, 동적 리소스 할당
4. **독립 테이블 저장**: 락 경합 없음, 트랜잭션 격리
5. **ExecutionContext**: 파티션 간 데이터 공유, 집계

### 적용 시 주의사항

⚠️ **메모리**: 스레드 수 × 청크 사이즈만큼 메모리 사용  
⚠️ **DB 커넥션**: Connection Pool 크기를 스레드 수보다 크게  
⚠️ **트랜잭션**: 각 청크는 독립 트랜잭션 (롤백 범위 고려)  
⚠️ **재시작**: JobInstance 관리 (실패 시 재실행 전략)

---

**여러분의 배치 시스템은 얼마나 걸리나요?** 병렬 처리를 적용한 경험이나 겪은 문제가 있다면 댓글로 공유해주세요! 💬

## 참고 자료

- [Spring Batch 공식 문서 - Scalability](https://github.com/spring-projects/spring-batch/blob/main/spring-batch-docs/modules/ROOT/pages/scalability.adoc)
- [Spring Batch 공식 문서 - JdbcPagingItemReader](https://github.com/spring-projects/spring-batch/blob/main/spring-batch-docs/modules/ROOT/pages/readers-and-writers/database.adoc)
- [Spring Batch Samples - Local Partitioning](https://github.com/spring-projects/spring-batch/blob/main/spring-batch-samples/README.md)
