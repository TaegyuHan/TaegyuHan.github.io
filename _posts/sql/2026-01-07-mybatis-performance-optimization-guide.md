---
title: "[SQL] 신입 개발자가 놓치기 쉬운 SQL 성능 최적화 5가지"

tagline: "데이터가 증가하면서 발생하는 SQL 성능 문제와 최적화 방법을 MyBatis 예제로 배우기"

header:
  overlay_image: /assets/post/sql/2026-01-07-mybatis-performance-optimization-guide/overlay.png
  overlay_filter: 0.5

categories:
  - SQL

tags:
  - MyBatis
  - SQL성능최적화
  - 데이터베이스
  - 쿼리최적화
  - 신입개발자
  - 성능튜닝

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-07T10:00:00+09:00
---

## 들어가며

신입 개발자로 첫 프로젝트를 시작하면 데이터베이스와 상호작용하는 SQL 쿼리를 작성하게 됩니다. 초기에는 "쿼리가 작동하는 것"에만 집중하기 쉽지만, **데이터가 늘어나면서 예상하지 못한 성능 문제**가 발생합니다.

필자도 최근 개발 중 시니어 개발자로부터 SQL 성능 관련 조언을 받았습니다. 그 경험을 통해 발견한 **신입이 놓치기 쉬운 SQL 성능 문제 5가지**를 정리했습니다. 이 글이 당신의 코드가 나중에 느려지는 것을 미리 방지하는 데 도움이 되길 바랍니다.

---

## 1. 서브쿼리와 N+1 문제: SELECT 내 서브쿼리의 함정

### 문제 상황

공통 코드(코드 테이블)를 사용할 때, SELECT 문 안에 서브쿼리를 넣어 코드명으로 변환하는 상황을 생각해봅시다.

```xml
<!-- 나쁜 예: SELECT 절 안의 서브쿼리 -->
<select id="selectOrders" resultType="OrderDto">
    SELECT 
        id,
        amount,
        (SELECT code_name FROM codes WHERE code_type = 'STATUS' AND code = o.status) as statusName
    FROM orders o
</select>
```

**이것이 왜 문제일까요?**

- 주문 데이터가 1,000건이라면, 서브쿼리는 **1,000번** 실행됩니다
- 각각의 서브쿼리마다 데이터베이스 왕복이 일어납니다
- 결과: N+1 쿼리 문제 발생 (1개의 메인 쿼리 + N개의 서브쿼리)

### 해결 방법: JOIN 사용

```xml
<!-- 좋은 예: JOIN을 사용한 효율적인 조회 -->
<select id="selectOrders" resultType="OrderDto">
    SELECT 
        o.id,
        o.amount,
        c.code_name as statusName
    FROM orders o
    LEFT JOIN codes c ON c.code_type = 'STATUS' AND c.code = o.status
</select>
```

**JOIN의 장점:**
- 한 번의 쿼리로 모든 데이터 조회
- 데이터베이스가 최적화된 실행 계획 생성 가능
- 성능: 1,000배 이상 향상 (체감상)

---

## 2. WHERE 조건의 형변환: 컬럼을 변환하지 말고 조건을 변환하라

### 문제 상황

검색 조건을 처리할 때 자주 실수하는 부분입니다.

```xml
<!-- 나쁜 예: 컬럼을 형변환 -->
<select id="searchMembers">
    SELECT * FROM members
    WHERE CAST(phone AS VARCHAR) = #{searchPhone}
       OR CAST(birthDate AS VARCHAR) LIKE #{searchBirth}
</select>
```

**왜 이것이 문제일까요?**

- 컬럼을 형변환하면 **데이터베이스가 인덱스를 사용할 수 없습니다**
- 풀 테이블 스캔(Full Table Scan)이 발생합니다
- 데이터가 1,000만 건이면 매번 1,000만 건을 모두 검사해야 합니다

### 해결 방법: 검색 조건을 형변환

```xml
<!-- 좋은 예: 검색 조건(입력값)을 형변환 -->
<select id="searchMembers">
    SELECT * FROM members
    WHERE phone = CAST(#{searchPhone} AS NUMBER)
       OR birthDate LIKE CONCAT(#{searchBirth}, '%')
</select>
```

**이점:**
- 컬럼은 그대로 두고 입력값만 변환
- 데이터베이스가 인덱스를 활용할 수 있음
- 같은 데이터도 10배 이상 빠르게 검색

**기억하세요:** "컬럼을 건드리지 말고, 조건을 맞춰라"

---

## 3. LIKE 검색의 와일드카드 위치: 인덱스를 살리는 방법

### 문제 상황

검색 기능을 만들 때 사용자가 어디서 검색할지 모르니까 양쪽에 와일드카드를 붙입니다.

```xml
<!-- 나쁜 예: 양쪽에 와일드카드 -->
<select id="searchProducts">
    SELECT * FROM products
    WHERE name LIKE '%' || #{keyword} || '%'
</select>
```

### 인덱스와 와일드카드의 관계

데이터베이스 인덱스는 **책의 목차**처럼 정렬된 상태입니다. 검색할 때 **왼쪽부터** 읽어나갑니다.

```
인덱스 구조 (이름 컬럼 예시):
'김준호' → [인덱스 위치 1]
'김태규' → [인덱스 위치 2]
'박재훈' → [인덱스 위치 3]
'이순신' → [인덱스 위치 4]
```

- ✅ `name LIKE '김%'` → "김"으로 시작 → 인덱스 활용 가능 (인덱스 1-2번 확인)
- ❌ `name LIKE '%김%'` → 어디든 포함 → 인덱스 활용 불가 (모든 데이터 확인)
- ❌ `name LIKE '%김'` → "김"으로 끝남 → 인덱스 활용 불가

### 해결 방법 3가지

**방법 1: 앞글자 검색으로 제한 (가장 빠름)**
```xml
<select id="searchProducts">
    SELECT * FROM products
    WHERE name LIKE #{keyword} || '%'
</select>
```

**방법 2: Elasticsearch 같은 검색 엔진 도입 (복잡한 검색 필요 시)**
```java
// Elasticsearch를 사용하면 전문 검색 가능
SearchResponse response = client.search(request);
```

**방법 3: 전문 검색(Full Text Search) 사용 (MySQL 예시)**
```xml
<select id="searchProducts">
    SELECT * FROM products
    WHERE MATCH(name) AGAINST(#{keyword} IN BOOLEAN MODE)
</select>
```

### 성능 비교 (100만 건 데이터 기준)
- `LIKE '김%'`: 약 0.1초
- `LIKE '%김%'`: 약 10초 이상 (100배 느림)

---

## 4. 배치 처리: 개별 INSERT의 성능 함정

### 문제 상황

대량의 데이터를 저장할 때 루프를 돌며 한 건씩 INSERT합니다.

```java
// 나쁜 예: 1,000건을 1,000번 쿼리
for (Order order : orders) {
    orderMapper.insert(order);  // 쿼리 1회
}
// 총 시간: 10초 ~ 1분 이상
```

**문제점:**
- 데이터베이스와 **1,000번** 왕복
- 네트워크 오버헤드가 엄청남
- 트랜잭션 로그 처리로 인한 성능 저하

### 해결 방법: 배치 INSERT

```xml
<!-- MyBatis 배치 INSERT -->
<insert id="insertBatch" parameterType="list">
    INSERT INTO orders (id, amount, status) VALUES
    <foreach collection="list" item="order" separator=",">
        (#{order.id}, #{order.amount}, #{order.status})
    </foreach>
</insert>
```

```java
// Java 코드: 한 번에 처리
List<Order> orders = new ArrayList<>();
// ... 1,000건 추가
orderMapper.insertBatch(orders);  // 쿼리 1회
// 총 시간: 1초 미만
```

### 성능 비교
- 개별 INSERT (1,000건): 약 30초
- 배치 INSERT (1,000건): 약 1초
- **성능 향상: 30배**

### 추가: 배치 처리 시 에러 처리 전략

대량 데이터 처리 시 부분 실패를 고려해야 합니다:

**전략 1: All or Nothing (전부 성공 또는 전부 실패)**
```java
// 금융 거래, 재고 갱신처럼 정확성이 중요한 경우
@Transactional
public void saveOrdersBatch(List<Order> orders) {
    orderMapper.insertBatch(orders);  // 하나라도 실패하면 전부 롤백
}
```

**전략 2: 성공/실패 분리 처리 (배치 + 개별 재처리)**
```java
// 회원 정보 일괄 업로드처럼 부분 성공을 허용하는 경우
public BatchResult saveOrdersBatchWithPartialSuccess(List<Order> orders) {
    List<Order> successList = new ArrayList<>();
    List<Order> failList = new ArrayList<>();
    
    int batchSize = 1000;
    
    // 배치 단위로 처리
    for (int i = 0; i < orders.size(); i += batchSize) {
        int end = Math.min(i + batchSize, orders.size());
        List<Order> batch = orders.subList(i, end);
        
        try {
            // 배치 INSERT 시도
            orderMapper.insertBatch(batch);
            successList.addAll(batch);
        } catch (Exception e) {
            // 배치 실패 시 개별로 재처리
            for (Order order : batch) {
                try {
                    orderMapper.insert(order);
                    successList.add(order);
                } catch (Exception ex) {
                    // 실패한 데이터만 따로 저장
                    failList.add(order);
                }
            }
        }
    }
    
    return new BatchResult(successList, failList);
}
```

**선택 기준:**
- 금융/재고: All or Nothing (데이터 정합성 필수)
- 회원/상품: 부분 성공 허용 (실패 건은 따로 재처리)

---

## 5. 동적 쿼리의 OR 조건: 같은 필드라면 IN을 사용하라

### 문제 상황

마이바티스의 동적 쿼리에서 같은 필드에 대해 OR 조건을 여러 개 만듭니다.

```xml
<!-- 나쁜 예: 같은 필드에 OR 조건 -->
<select id="searchOrders">
    SELECT * FROM orders WHERE 1=1
    <if test="status1 != null">
        OR status = #{status1}
    </if>
    <if test="status2 != null">
        OR status = #{status2}
    </if>
    <if test="status3 != null">
        OR status = #{status3}
    </if>
</select>
```

### OR과 인덱스의 관계

데이터베이스가 OR 조건을 처리할 때의 실행 과정:

**OR 조건의 실행 과정 (비효율적)**
<div class="mermaid mermaid-center">
graph TD
    A["SELECT * FROM orders WHERE<br/>status = 'PENDING' OR<br/>status = 'PROCESSING' OR<br/>status = 'WAITING'"] --> B["조건 1: status = 'PENDING'<br/>인덱스 스캔<br/>→ 50,000개 행 검색"]
    A --> C["조건 2: status = 'PROCESSING'<br/>인덱스 스캔<br/>→ 75,000개 행 검색"]
    A --> D["조건 3: status = 'WAITING'<br/>인덱스 스캔<br/>→ 100,000개 행 검색"]
    B --> E["UNION 연산<br/>3개의 결과 합치기"]
    C --> E
    D --> E
    E --> F["중복 제거<br/>최종: 225,000개 행"]
    
    style B fill:#4a0000,stroke:#ff7f7f,color:#ff9999
    style C fill:#4a0000,stroke:#ff7f7f,color:#ff9999
    style D fill:#4a0000,stroke:#ff7f7f,color:#ff9999
    style E fill:#662d2d,stroke:#ff9999,color:#ffb3b3
    style F fill:#662d2d,stroke:#ff9999,color:#ffb3b3
</div>

**IN 조건의 실행 과정 (효율적)**
<div class="mermaid mermaid-center">
graph TD
    A["SELECT * FROM orders WHERE<br/>status IN('PENDING',<br/>'PROCESSING', 'WAITING')"] --> B["하나의 조건으로 평가<br/>인덱스 스캔<br/>→ 225,000개 행 검색"]
    B --> C["결과 반환<br/>최종: 225,000개 행"]
    
    style B fill:#004a00,stroke:#7fff7f,color:#99ff99
    style C fill:#266626,stroke:#99ff99,color:#b3ffb3
</div>

**문제:** OR은 각 조건을 별도로 평가 후 UNION 처리로 인한 오버헤드 발생

### 해결 방법: IN 절 사용

```xml
<!-- 좋은 예: IN 절로 한 번에 처리 -->
<select id="searchOrders">
    SELECT * FROM orders WHERE 1=1
    <if test="statusList != null and statusList.size() > 0">
        AND status IN
        <foreach collection="statusList" item="status" open="(" close=")" separator=",">
            #{status}
        </foreach>
    </if>
</select>
```

### 성능 비교 (100만 건 데이터 기준)
- OR 조건: 약 2초
- IN 절: 약 0.2초
- **성능 향상: 10배**

### 다른 필드 간의 OR은?

```xml
<!-- 이름 OR 이메일로 검색: 어쩔 수 없이 OR 사용 -->
<select id="searchMembers">
    SELECT * FROM members WHERE 1=1
    <if test="keyword != null">
        AND (name LIKE #{keyword} OR email LIKE #{keyword})
    </if>
</select>
```

이 경우는 OR을 피할 수 없으므로:
- **Full Text Search** 사용 고려
- **Elasticsearch** 같은 검색 엔진 도입 검토

---

## 보너스: 페이징은 필수, LIMIT 없는 조회는 금지

마지막으로 당연하지만 중요한 것입니다.

```xml
<!-- 나쁜 예: 전체 데이터 조회 -->
<select id="getAllMembers">
    SELECT * FROM members
</select>
```

100만 건의 회원 데이터를 모두 메모리에 로드하면:
- Out of Memory 에러 발생 가능
- 네트워크 전송 시간 증가

```xml
<!-- 좋은 예: 페이징 처리 -->
<select id="getMembersWithPaging">
    SELECT * FROM members
    LIMIT #{offset}, #{pageSize}
</select>
```

관리자 페이지라도 **반드시 페이징을 적용**하세요.

---

## 실무 적용 체크리스트

마이바티스 쿼리 작성 시 이 체크리스트를 확인하세요:

- ✅ SELECT 절에 서브쿼리가 있는가? → JOIN으로 변경 검토
- ✅ WHERE 조건에서 컬럼을 형변환하고 있는가? → 입력값 형변환으로 변경
- ✅ LIKE 검색에 양쪽 와일드카드를 붙였는가? → 앞글자 검색으로 변경 또는 검색 엔진 도입
- ✅ 대량 데이터 INSERT를 개별로 처리하는가? → 배치 처리로 변경
- ✅ 같은 필드에 OR 조건이 여러 개 있는가? → IN 절로 변경
- ✅ LIMIT 없이 전체 데이터를 조회하는가? → 페이징 추가

---

## 결론: 작은 습관이 모인다

지금은 데이터가 적어서 성능 문제가 보이지 않을 수 있습니다. 하지만 **6개월 후, 1년 후 데이터가 100배로 늘어나면?** 지금 쓴 쿼리가 10배, 100배 느려집니다.

신입 때부터 이런 최적화 관습을 들이면:
- 나중에 리팩토링 비용이 줄어듭니다
- 성능 문제로 인한 장애를 미리 방지합니다

당신이 오늘 받은 시니어의 조언처럼, **이 글을 읽는 신입 개발자들도 같은 실수를 피하길** 바랍니다.

---

## 당신의 경험을 공유해주세요

마이바티스를 사용하면서 **"이거 실수했는데 큰 성능 문제였다"** 같은 경험이 있으신가요? 댓글로 공유해주시면 다른 개발자들에게 큰 도움이 될 거예요!
