---
title: "[MyBatis] 마이바티스 동적 쿼리: 성능과 가독성의 균형점 찾기"

tagline: "JOIN ON 조건 배치의 딜레마를 해결하는 실무 접근법"

header:
  overlay_image: /assets/post/mybatis/2026-01-01-mybatis-dynamic-query-performance/overlay.png
  overlay_filter: 0.5

categories:
  - MyBatis

tags:
  - MyBatis
  - 동적쿼리
  - 성능최적화
  - SQL
  - 개발경험
  - 실무팁

toc: true
show_date: true

last_modified_at: 2026-01-01T02:16:00
---

마이바티스를 사용하다 보면 한 가지 반복되는 고민이 생깁니다. 바로 **동적 쿼리에서 조건을 어디에 배치할 것인가**라는 문제입니다.

특히 관리자 페이지나 조회 필터 기능을 구현할 때면 이 딜레마가 더욱 심해집니다:

- **JOIN ON 절에 조건을 넣으면?** 성능은 좋지만 XML의 `<if>` 태그로 인한 가독성 악화
- **WHERE 절에 조건을 넣으면?** 코드는 깔끔하지만 성능 손실 가능성

이 글은 실무에서 마주친 이 고민을 어떻게 해결했는지, 그리고 그 과정에서 찾은 **실질적인 해결책**을 공유합니다.

---

## 마이바티스 동적 쿼리의 현실

### 왜 복잡해지는가?

마이바티스에서 동적 쿼리를 작성할 때 주로 사용하는 방식은 XML 매핑입니다:

```xml
<select id="findUsers" parameterType="SearchCondition" resultType="User">
  SELECT u.* FROM users u
  WHERE 1=1
  <if test="userName != null">
    AND u.name LIKE CONCAT('%', #{userName}, '%')
  </if>
  <if test="userStatus != null">
    AND u.status = #{userStatus}
  </if>
</select>
```

간단한 조건이면 이 정도면 충분합니다. 하지만 **여러 테이블을 조인하고 조건이 많아지면** 상황이 달라집니다.

### 실무의 전형적인 시나리오

관리자 페이지에서 주문 목록을 조회하는 경우를 예로 들어봅시다:

```xml
<select id="findOrders" parameterType="OrderSearchCondition" resultType="OrderVO">
  SELECT 
    o.order_id, o.order_date, o.total_amount,
    c.customer_name, c.customer_email,
    p.product_name, p.category
  FROM orders o
  LEFT JOIN customers c ON o.customer_id = c.customer_id
  LEFT JOIN order_items oi ON o.order_id = oi.order_id
  LEFT JOIN products p ON oi.product_id = p.product_id
  WHERE 1=1
  <if test="customerName != null">
    AND c.customer_name LIKE CONCAT('%', #{customerName}, '%')
  </if>
  <if test="productCategory != null">
    AND p.category = #{productCategory}
  </if>
</select>
```

여기서 생기는 문제는 **LEFT JOIN의 의미가 흐려진다**는 것입니다. WHERE 절에서 조건을 처리하면:

- 동적으로 포함/제외되는 데이터의 범위가 명확하지 않음
- 개발자마다 쿼리의 의도를 다르게 해석할 수 있음
- 나중에 버그가 생겼을 때 원인 파악이 어려움

---

## 성능 vs 가독성의 딜레마

### JOIN ON에 조건을 넣으면?

성능 최적화를 위해 JOIN ON에 조건을 배치하는 방식입니다:

```xml
<select id="findOrders" parameterType="OrderSearchCondition" resultType="OrderVO">
  SELECT o.order_id, o.order_date, o.total_amount, c.customer_name, p.product_name
  FROM orders o
  LEFT JOIN customers c ON o.customer_id = c.customer_id
    <if test="customerName != null">
      AND c.customer_name LIKE CONCAT('%', #{customerName}, '%')
    </if>
  LEFT JOIN order_items oi ON o.order_id = oi.order_id
  LEFT JOIN products p ON oi.product_id = p.product_id
    <if test="productCategory != null">
      AND p.category = #{productCategory}
    </if>
  WHERE 1=1
</select>
```

**장점:**
- 데이터베이스가 조인 단계에서 이미 필터링 → 처리할 행 수 감소
- 특히 LEFT JOIN에서는 결과 행 수 자체가 달라짐
- 큰 데이터셋에서 성능 개선 효과 뚜렷

**단점:**
- XML 소스코드가 복잡해짐
- ON 절 내 `<if>` 태그의 중첩으로 가독성 악화
- 어떤 조건이 선택적인지 한눈에 파악 어려움
- 코드 리뷰 시 의도 파악 어려움

---

## 데이터 규모를 모를 때의 선택

개발 초기 단계에서는 보통 **실제 운영 데이터 규모를 모릅니다**. 이 상황에서 개발자는 두 가지 선택지 중 하나를 해야 합니다:

1. **사후 최적화**: 일단 깔끔하게 작성하고 성능 문제 발생 시 수정
2. **선제적 최적화**: 성능을 고려해 처음부터 최적화된 형태로 작성

선제적 최적화를 선택했을 때의 이유:

- **마이바티스의 리팩토링 비용**: JPA와 달리 쿼리가 여러 곳에 퍼져있음
- **나중에 수정할 때의 어려움**: 이미 작성된 쿼리를 모두 찾아 수정해야 함
- **버전 관리**: 이미 운영 중인 쿼리를 변경하려면 주의가 필요함

결과적으로 **성능을 우선시하되, 가독성 문제를 다른 방식으로 해결**하기로 결정했습니다.

---

## 실질적 해결책: 로그 기반 검증

### 문제의 핵심은 "XML 소스가 복잡"

생각을 바꿔보니 중요한 깨달음이 생겼습니다:

> **실제로 읽어야 하는 것은 "실행된 SQL"이지, "XML 매핑 코드"가 아니다.**

개발 과정에서 로그를 활용하면 **실제로 실행되는 쿼리**를 볼 수 있습니다. 마이바티스는 설정만 해주면 실행된 SQL을 로그에 출력해줍니다.

### 마이바티스 쿼리 로그 설정

**application.yml / application.properties:**

```yaml
# MyBatis SQL 로깅
logging:
  level:
    org.apache.ibatis: DEBUG
    org.apache.ibatis.session.defaults.DefaultSqlSession: DEBUG
```

또는 더 자세하게:

```properties
logging.level.org.apache.ibatis=DEBUG
logging.level.org.apache.ibatis.session.defaults.DefaultSqlSession=DEBUG
```

이렇게 설정하면 애플리케이션 실행 시 다음과 같은 로그가 출력됩니다:

```
==>  Preparing: SELECT ... FROM orders o LEFT JOIN customers c ON o.customer_id = c.customer_id AND c.customer_name LIKE ? ...
==> Parameters: (%keyword%)(...)
<==    Columns: ...
<==        Row: ...
```

### 로그를 통한 성능 검증 워크플로우

이제 다음과 같은 프로세스를 따릅니다:

1. **쿼리 작성**: 성능을 우선으로 JOIN ON에 조건 배치
2. **로그 확인**: 실제 실행되는 SQL 확인
3. **SQL 분석**: 로그에 출력된 SQL의 가독성은 우수함 (일반 SQL)
4. **성능 검증**: EXPLAIN PLAN 등으로 쿼리 계획 확인
5. **반복 개선**: 성능에 문제 있으면 수정

---

## 실무 적용 예제

### Before: 가독성만 고려한 방식

```xml
<select id="findFilteredOrders" parameterType="FilterCondition" resultType="OrderVO">
  SELECT o.id, o.date, c.name, p.name
  FROM orders o
  LEFT JOIN customers c ON o.customer_id = c.customer_id
  LEFT JOIN order_items oi ON o.order_id = oi.order_id
  LEFT JOIN products p ON oi.product_id = p.product_id
  WHERE 1=1
  <if test="customerName != null">
    AND c.name LIKE CONCAT('%', #{customerName}, '%')
  </if>
  <if test="productCategory != null">
    AND p.category = #{productCategory}
  </if>
  <if test="orderDateFrom != null">
    AND o.date >= #{orderDateFrom}
  </if>
</select>
```

**문제:** 데이터 규모가 커지면 LEFT JOIN에서 불필요한 행들이 모두 메모리에 로드됨

### After: 성능을 고려한 방식

```xml
<select id="findFilteredOrders" parameterType="FilterCondition" resultType="OrderVO">
  SELECT o.id, o.date, c.name, p.name
  FROM orders o
  LEFT JOIN customers c ON o.customer_id = c.customer_id
    <if test="customerName != null">
      AND c.name LIKE CONCAT('%', #{customerName}, '%')
    </if>
  LEFT JOIN order_items oi ON o.order_id = oi.order_id
  LEFT JOIN products p ON oi.product_id = p.product_id
    <if test="productCategory != null">
      AND p.category = #{productCategory}
    </if>
  WHERE 1=1
  <if test="orderDateFrom != null">
    AND o.date >= #{orderDateFrom}
  </if>
</select>
```

**개선:** 조인 단계에서 이미 필터링되므로 처리할 데이터 량 감소

### 로그 출력 결과

개발 중에는 이 로그를 확인합니다:

```sql
-- customerName = '홍길동', productCategory = '전자제품', orderDateFrom = '2025-01-01'
SELECT o.id, o.date, c.name, p.name
FROM orders o
LEFT JOIN customers c ON o.customer_id = c.customer_id AND c.name LIKE '%홍길동%'
LEFT JOIN order_items oi ON o.order_id = oi.order_id
LEFT JOIN products p ON oi.product_id = p.product_id AND p.category = '전자제품'
WHERE 1=1 AND o.date >= '2025-01-01'
```

**실제 SQL을 읽으면:**
- 로직이 명확함
- 조인 조건과 필터 조건이 구분됨
- 성능 최적화가 적용되었는지 확인 가능

---

## 추가 팁: 복잡한 쿼리 관리하기

JOIN ON에 조건이 많아지면 다음과 같은 방식으로 가독성을 높일 수 있습니다:

### 1. `<include>`를 활용한 재사용 가능한 조건 모듈화

```xml
<!-- 별도 SQL 파일에 정의 -->
<sql id="customerNameCondition">
  <if test="customerName != null">
    AND c.name LIKE CONCAT('%', #{customerName}, '%')
  </if>
</sql>

<!-- 메인 쿼리에서 사용 -->
<select id="findFilteredOrders" parameterType="FilterCondition" resultType="OrderVO">
  SELECT o.id, o.date, c.name, p.name
  FROM orders o
  LEFT JOIN customers c ON o.customer_id = c.customer_id
    <include refid="customerNameCondition" />
  ...
</select>
```

### 2. 복잡한 조건은 서브쿼리로 분리

```xml
<select id="findFilteredOrders" parameterType="FilterCondition" resultType="OrderVO">
  SELECT o.id, o.date, c.name, p.name
  FROM orders o
  LEFT JOIN customers c ON o.customer_id = c.customer_id
  LEFT JOIN (
    SELECT order_id, product_name, category
    FROM order_items oi
    JOIN products p ON oi.product_id = p.product_id
    <if test="productCategory != null">
      WHERE p.category = #{productCategory}
    </if>
  ) filtered_items ON o.order_id = filtered_items.order_id
  WHERE 1=1
  <if test="orderDateFrom != null">
    AND o.date >= #{orderDateFrom}
  </if>
</select>
```

### 3. 동적 쿼리 로깅 강화

application.yml에서 더 자세한 로깅 설정:

```yaml
logging:
  level:
    org.apache.ibatis.executor.SimpleExecutor: TRACE
    org.mybatis.spring.SqlSessionUtils: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

---

## 팀 차원의 가이드라인 제안

### 마이바티스 동적 쿼리 작성 원칙

1. **성능 우선 사고**
   - 데이터 규모가 불명확할 때는 성능을 먼저 고려
   - 조인 조건에 필터를 배치해 불필요한 데이터 처리 제거

2. **로그 기반 검증**
   - 개발 중 실행된 SQL을 항상 확인
   - 로그에서 쿼리 가독성이 확보되면 OK

3. **복잡성 관리**
   - 조건이 5개 이상이면 `<include>` 또는 서브쿼리로 분리
   - 코드 리뷰 시 실행된 SQL을 함께 검토

4. **성능 검증**
   - 큰 데이터셋에 대해 EXPLAIN PLAN 분석
   - 인덱스 활용 여부 확인

---

## 결론

**마이바티스의 동적 쿼리에서 성능과 가독성은 상충관계가 아닙니다.**

- **XML 소스**: 성능을 우선으로 JOIN ON에 조건 배치 → 약간 복잡해질 수 있음
- **실행 쿼리**: 로그를 통해 확인 → 충분히 가독성 있음
- **검증**: 실제 SQL로 성능 분석 및 최적화 → 근거 있는 개선

이 접근법의 핵심은 **"XML 코드의 아름다움보다 실행된 SQL의 효율성을 우선한다"**는 철학입니다. 개발 단계부터 성능을 고려하고, 로그를 통해 검증하며, 필요할 때 개선하는 방식이 결국 가장 실용적입니다.

---

## 당신의 경험은?

마이바티스로 복잡한 동적 쿼리를 다루면서 비슷한 경험을 하셨나요? 혹은 다른 방식으로 이 문제를 해결하고 있으신가요?

**댓글로 의견을 나눠주세요:**
- 현재 사용 중인 마이바티스 쿼리 패턴은?
- 성능 vs 가독성 문제를 어떻게 해결하고 있나요?
- 혹시 활용 중인 팁이나 노하우가 있다면?

다른 개발자들의 경험이 모여 더 좋은 실무 가이드라인을 만들 수 있습니다.
