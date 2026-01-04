---
title: "[MyBatis] MyBatis 페이징 처리 최적화: Window 함수와 Cursor 방식 비교"

tagline: "MyBatis에서 페이징 쿼리를 효율적으로 처리하는 3가지 방법과 각각의 장단점"

header:
  overlay_image: /assets/post/mybatis/2026-01-01-mybatis-pagination-optimization/overlay.png
  overlay_filter: 0.5

categories:
  - MyBatis

tags:
  - mybatis
  - pagination
  - window-function
  - mariadb
  - 성능최적화
  - sql
  - 쿼리최적화
  - 데이터베이스

toc: true
show_date: true

last_modified_at: 2026-01-01T18:56:00+09:00
---


MyBatis를 사용하여 데이터를 페이징으로 조회할 때, 많은 개발자들이 다음과 같은 방식을 사용하고 있습니다.

```java
// 데이터 조회 (1번)
List<User> userList = userMapper.selectUserList(offset, pageSize);

// 전체 개수 조회 (1번)
int totalCount = userMapper.selectUserCount();

return new PageResponse(userList, totalCount);
```

이 방식은 **데이터베이스에 2번 접근**해야 합니다. 과연 이것이 최선의 방법일까요? 이 글에서는 MyBatis 페이징 처리를 더 효율적으로 개선하는 방법들을 실전 코드와 함께 소개합니다.

---

## 기존 방식의 문제점

### 데이터베이스 왕복 비용

```
쿼리 1: SELECT * FROM users LIMIT 10 OFFSET 0;
쿼리 2: SELECT COUNT(*) FROM users;
```

**단순해 보이지만 숨은 비용들:**

- ❌ **WHERE 조건이 2번 평가됨**: 두 쿼리 모두 동일한 WHERE 조건을 처리
- ❌ **인덱스 스캔 2회**: 각 쿼리가 별도의 인덱스 스캔 수행
- ❌ **쿼리 파싱/컴파일 2회**: 데이터베이스가 두 쿼리 문자열을 각각 파싱
- ❌ **트랜잭션/락 오버헤드**: 두 번의 독립적인 쿼리 실행
- ❌ **데이터 정합성**: 두 쿼리 사이에 데이터가 변경될 수 있음

특히 **복잡한 WHERE 조건**이 있을 때 이 비용은 더 커집니다.

---

## 해결 방법 비교

### 방식 1: 기존 방식 (2개 쿼리)

**MyBatis Mapper XML:**
```xml
<!-- 데이터 조회 -->
<select id="selectUserList" parameterType="map" resultType="UserDTO">
  SELECT id, name, email, status, created_at
  FROM users
  WHERE status = #{status}
  <if test="keyword != null">
    AND name LIKE CONCAT('%', #{keyword}, '%')
  </if>
  ORDER BY id DESC
  LIMIT #{pageSize} OFFSET #{offset}
</select>

<!-- 전체 개수 조회 -->
<select id="selectUserCount" parameterType="map" resultType="int">
  SELECT COUNT(*) 
  FROM users
  WHERE status = #{status}
  <if test="keyword != null">
    AND name LIKE CONCAT('%', #{keyword}, '%')
  </if>
</select>
```

**Java Service:**
```java
public PageResponse<UserDTO> getUserPage(String status, String keyword, int page, int pageSize) {
    int offset = (page - 1) * pageSize;
    
    List<UserDTO> list = userMapper.selectUserList(status, keyword, offset, pageSize);
    int totalCount = userMapper.selectUserCount(status, keyword);  // 2번 쿼리
    
    return new PageResponse<>(list, totalCount, page, pageSize);
}
```

| 장점 | 단점 |
|------|------|
| 로직이 간단하고 이해하기 쉬움 | DB 왕복 2회 |
| 대부분의 프레임워크에서 지원 | WHERE 조건 2회 평가 |
| 코드 복잡도 낮음 | 쿼리 파싱 오버헤드 |
| | 데이터 정합성 위험 |

---

### 방식 2: Window 함수 (1개 쿼리) ⭐ 추천

**Window 함수란?** 각 행에 대해 그 행 주변의 "윈도우(범위)"에 있는 데이터들에 대한 계산을 할 수 있게 해주는 SQL 함수입니다.

**기본 개념:**
```sql
-- 일반 COUNT: 그룹으로 집계 (행 감소)
SELECT status, COUNT(*) as total FROM users GROUP BY status;

-- Window 함수 COUNT: 각 행에 전체 개수 추가 (행 유지)
SELECT id, name, status, COUNT(*) OVER() as total FROM users;
```

**MyBatis Mapper XML:**
```xml
<select id="selectUserPageWithTotal" parameterType="map" resultType="UserPageDTO">
  SELECT 
    id, name, email, status, created_at,
    COUNT(*) OVER() as totalCount
  FROM users
  WHERE status = #{status}
  <if test="keyword != null">
    AND name LIKE CONCAT('%', #{keyword}, '%')
  </if>
  ORDER BY id DESC
  LIMIT #{pageSize} OFFSET #{offset}
</select>
```

**Java DTO:**
```java
public class UserPageDTO {
    private Long id;
    private String name;
    private String email;
    private String status;
    private LocalDateTime createdAt;
    private Long totalCount;  // Window 함수에서 온 값
    
    // getter, setter...
}
```

**Java Service:**
```java
public PageResponse<UserDTO> getUserPage(String status, String keyword, int page, int pageSize) {
    int offset = (page - 1) * pageSize;
    
    List<UserPageDTO> result = userMapper.selectUserPageWithTotal(status, keyword, offset, pageSize);
    
    // 첫 번째 행에서 totalCount 추출
    long totalCount = result.isEmpty() ? 0 : result.get(0).getTotalCount();
    
    // DTO를 응답 DTO로 변환
    List<UserDTO> list = result.stream()
        .map(dto -> new UserDTO(
            dto.getId(), 
            dto.getName(), 
            dto.getEmail(),
            dto.getStatus(),
            dto.getCreatedAt()
        ))
        .collect(Collectors.toList());
    
    return new PageResponse<>(list, totalCount, page, pageSize);
}
```

**응답 JSON:**
```json
{
  "data": [
    {"id": 1, "name": "홍길동", "email": "hong@example.com"},
    {"id": 2, "name": "김철수", "email": "kim@example.com"},
    ...
  ],
  "totalCount": 150,
  "pageNumber": 1,
  "pageSize": 10
}
```

| 장점 | 단점 |
|------|------|
| DB 왕복 1회 | totalCount가 각 행에 중복 |
| WHERE 조건 1회 평가 | DTO 복잡도 증가 |
| 쿼리 파싱 1회 | MariaDB 10.2+ 필요 |
| 데이터 정합성 우수 | 매퍼 로직 복잡도 증가 |

**MariaDB Window 함수 지원:** MariaDB 10.2.0부터 지원

---

### 방식 3: Cursor 기반 (응답 구조 변경) ⭐ 무한스크롤 최적

전체 개수를 정확히 알 필요 없다면, **다음 페이지 존재 여부만 판단**하는 방식도 있습니다.

**아이디어:** LIMIT을 원래 필요한 수 + 1로 설정하여 다음 페이지 존재 여부 판단

**MyBatis Mapper XML:**
```xml
<select id="selectUserPageWithCursor" parameterType="map" resultType="UserDTO">
  SELECT id, name, email, status, created_at
  FROM users
  WHERE status = #{status}
  <if test="keyword != null">
    AND name LIKE CONCAT('%', #{keyword}, '%')
  </if>
  ORDER BY id DESC
  LIMIT #{pageSize + 1}  <!-- +1로 다음 페이지 여부 판단 -->
  OFFSET #{offset}
</select>
```

**Java Service:**
```java
public CursorPageResponse<UserDTO> getUserPage(String status, String keyword, int page, int pageSize) {
    int offset = (page - 1) * pageSize;
    
    // LIMIT을 pageSize + 1로 설정
    List<UserDTO> result = userMapper.selectUserPageWithCursor(
        status, keyword, offset, pageSize + 1
    );
    
    // 다음 페이지 존재 여부 판단
    boolean hasNextPage = result.size() > pageSize;
    
    // pageSize개만 응답
    List<UserDTO> pageList = result.size() > pageSize 
        ? result.subList(0, pageSize) 
        : result;
    
    return new CursorPageResponse<>(pageList, hasNextPage, page, pageSize);
}
```

**응답 JSON:**
```json
{
  "data": [
    {"id": 1, "name": "홍길동", "email": "hong@example.com"},
    {"id": 2, "name": "김철수", "email": "kim@example.com"},
    ...
    (10개만 포함)
  ],
  "hasNextPage": true,
  "pageNumber": 1,
  "pageSize": 10
}
```

| 장점 | 단점 |
|------|------|
| DB 쿼리 1회 | 전체 개수 정보 제공 불가 |
| 데이터 중복 없음 | 페이지 점프 불가능 |
| 네트워크 효율 최고 | "총 150건 중 30-40건 보기" 표시 불가 |
| 무한스크롤에 최적 | 응답 구조 변경 필요 |

---

## 3가지 방식 종합 비교

| 항목 | 기존 방식 | Window 함수 | Cursor 방식 |
|------|---------|-----------|------------|
| **DB 쿼리 수** | 2개 ❌ | 1개 ✅ | 1개 ✅ |
| **WHERE 평가** | 2회 | 1회 ✅ | 1회 ✅ |
| **데이터 중복** | 없음 ✅ | 있음 (totalCount) | 없음 ✅ |
| **전체 개수 표시** | 가능 ✅ | 가능 ✅ | 불가능 |
| **페이지 점프** | 가능 ✅ | 가능 ✅ | 불가능 |
| **무한스크롤** | 비효율 | 비효율 | 최적 ✅ |
| **코드 복잡도** | 낮음 ✅ | 중간 | 낮음 ✅ |
| **마리아DB 지원** | 모든 버전 ✅ | 10.2+ ✅ | 모든 버전 ✅ |

---

## 데이터베이스 연산 비용 분석

### Window 함수가 더 효율적인 이유

**기존 방식:**
```
쿼리 1: WHERE 조건 적용 → 인덱스 스캔 → 정렬 → LIMIT/OFFSET
쿼리 2: WHERE 조건 적용 → 인덱스 스캔 → COUNT
─────────────────────────────────────────────────
비용: WHERE 2회, 인덱스 스캔 2회, 쿼리 파싱 2회
```

**Window 함수 방식:**
```
쿼리 1: WHERE 조건 적용 → 인덱스 스캔 → 정렬 → COUNT() OVER() → LIMIT/OFFSET
─────────────────────────────────────────────────────────────
비용: WHERE 1회, 인덱스 스캔 1회, 쿼리 파싱 1회
```

**개선 효과:**
- WHERE 조건 평가 횟수 50% 감소
- 인덱스 스캔 횟수 50% 감소
- DB 왕복 횟수 50% 감소
- 쿼리 파싱 오버헤드 50% 감소

---

## 실무 적용 시나리오

### 언제 어떤 방식을 써야 할까?

**✅ Window 함수 방식 추천:**
- 관리자 페이지: 전체 건수 정보가 필수적
- 검색 결과 페이지: "총 OOO건 중 XX-YY번째"
- 일반 페이지네이션: 이전/다음 페이지 버튼이 필요

**✅ Cursor 방식 추천:**
- 모바일 앱: 무한스크롤이 표준
- 실시간 피드: SNS 스타일의 피드
- 대용량 데이터: 성능과 네트워크 효율 중요

**✅ 기존 방식 유지:**
- 마리아DB 10.2 미만 버전
- 높은 유지보수 용이성이 최우선
- 기존 코드베이스와의 호환성 필요

---

## 마이그레이션 팁

### 기존에서 Window 함수로 전환할 때

**1. 점진적 마이그레이션:**
```java
// 기존 메서드는 유지
userMapper.selectUserList(...);
userMapper.selectUserCount(...);

// 새로운 메서드 추가
userMapper.selectUserPageWithTotal(...);
```

**2. 유닛 테스트 작성:**
```java
@Test
public void testWindowFunctionPagination() {
    // Window 함수 결과와 기존 방식 결과가 동일한지 검증
    List<UserPageDTO> windowResult = userMapper.selectUserPageWithTotal(...);
    List<UserDTO> legacyList = userMapper.selectUserList(...);
    int legacyCount = userMapper.selectUserCount(...);
    
    assertEquals(windowResult.size(), legacyList.size());
    assertEquals(windowResult.get(0).getTotalCount(), legacyCount);
}
```

**3. 성능 모니터링:**
```java
long startTime = System.currentTimeMillis();
List<UserPageDTO> result = userMapper.selectUserPageWithTotal(...);
long duration = System.currentTimeMillis() - startTime;

logger.info("Window function pagination took: {}ms", duration);
```

---

## 주의사항

### Window 함수 사용 시 주의할 점

❌ **부분 PARTITION BY와 함께 사용할 때:**
```sql
-- ❌ 잘못된 사용
SELECT id, status, 
       COUNT(*) OVER(PARTITION BY status) as statusCount  -- 각 상태별 개수
FROM users
LIMIT 10;

-- ✅ 올바른 사용
SELECT id, status,
       COUNT(*) OVER() as totalCount  -- 전체 개수
FROM users
WHERE status = 'active'
LIMIT 10;
```

❌ **복잡한 WINDOW FRAME과 LIMIT의 상호작용:**
```sql
-- Window 함수가 LIMIT 전에 실행되므로
-- 전체 테이블의 COUNT를 계산한 후 LIMIT 적용
-- 대용량 테이블에서는 주의 필요
```

---

## 결론 및 추천

### 당신의 상황에 맞는 선택

**지금 당신이 기존 방식(2개 쿼리)을 사용 중이라면:**

1. **즉시 전환 권장:** Window 함수 방식
   - 개발 노력: 중간 수준
   - 성능 개선: 명확함
   - MariaDB 10.2 이상 필요 (확인 후 적용)

2. **모바일 무한스크롤:** Cursor 방식으로 별도 구현
   - 개발 노력: 낮음
   - 네트워크 효율: 최고
   - 기존 코드와 공존 가능

3. **점진적 마이그레이션:**
   - 새로운 기능부터 Window 함수 적용
   - 기존 코드는 유지보수 모드
   - 테스트를 통해 동등성 검증

---

## 마지막 조언

페이징 처리는 **사소해 보이지만 극도로 자주 사용**되는 기능입니다. 작은 개선이 모여 **전체 시스템 성능**에 큰 영향을 미칩니다.

당신의 프로젝트 특성에 맞게 선택하되, **성능 개선과 코드 복잡도의 균형**을 고려하세요.

---

## 다음 읽을거리

- [MariaDB 공식 문서: Window Functions](https://mariadb.com/kb/en/window-functions/)
- [MyBatis 페이지 처리 공식 가이드](https://mybatis.org/mybatis-3/)
- [SQL Window 함수 심화](https://modern-sql.com/feature/window-functions)

---

**혹시 당신의 프로젝트에서 페이징 처리로 인한 성능 문제를 경험한 적이 있나요? 또는 다른 최적화 방법을 사용하고 있다면, 댓글로 공유해주세요!**
