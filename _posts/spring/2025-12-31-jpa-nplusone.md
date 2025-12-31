---
title: "[Spring] JPA N+1 문제 완벽 해결하기"

tagline: "1:N 관계에서 발생하는 N+1 쿼리 문제를 Fetch Join, EntityGraph, Batch Size로 해결하는 실무 가이드"

header:
  overlay_image: /assets/post/spring/2025-12-31-jpa-nplusone/overlay.png
  overlay_filter: 0.5

categories:
  - Spring

tags:
  - JPA
  - N+1 문제
  - 성능 최적화
  - Fetch Join
  - EntityGraph
  - Batch Size

toc: true
show_date: true

last_modified_at: 2025-12-31T23:59:59+09:00
---

JPA를 사용하는 개발자라면 거의 반드시 한 번은 겪게 되는 문제가 있습니다. 바로 **N+1 쿼리 문제**입니다. 개발 초기에는 괜찮다가, 데이터가 많아지면서 갑자기 수백, 수천 개의 쿼리가 날아가는 걸 보고 당황하게 되죠.

이 글에서는 N+1 문제가 왜 발생하는지, 그리고 **Fetch Join, EntityGraph, Batch Size** 세 가지 해결 방법을 실무적인 관점에서 비교하고, 어떤 상황에서 어떤 방법을 사용해야 하는지 알려드리겠습니다.

---

## N+1 문제란?

### 1:N 관계에서 발생하는 쿼리 폭발

N+1 문제는 주로 **1:N 관계**에서 발생합니다. 예를 들어 `게시글(Post) : 댓글(Comment) = 1 : N` 관계를 생각해봅시다.

```java
@Entity
class Post {
    @Id
    private Long id;
    private String title;
    
    @OneToMany(fetch = FetchType.LAZY)
    private List<Comment> comments;
}
```

### 문제 상황

```java
// 게시글 10개 조회
List<Post> posts = postRepository.findAll();

// 반복문에서 댓글 사용
for (Post post : posts) {
    System.out.println(post.getComments().size());
}
```

**실행되는 쿼리:**
- 1번째: `SELECT * FROM post;` (게시글 10개 조회)
- 2~11번째: 각 게시글마다 `SELECT * FROM comment WHERE post_id = ?` (댓글 조회)
- **총 11번의 쿼리 = 1 + 10 = 1 + N**

데이터가 1000개라면? 1001번의 쿼리가 실행됩니다. 이것이 N+1 문제의 정체입니다.

---

## 왜 이 문제가 발생할까?

### 지연 로딩(Lazy Loading)의 작동 원리

JPA는 기본적으로 연관된 데이터를 **필요할 때만** 가져오는 "지연 로딩" 전략을 사용합니다.

```java
Post post = postRepository.findById(1L);
System.out.println(post.getTitle());  // ✅ 이 시점: Post만 조회 (댓글 X)

post.getComments().size();  // ⚠️ 이 순간: 댓글 조회 쿼리 실행!
```

### JPA의 근본적인 한계

JPA는 각 객체의 연관 데이터를 **독립적으로** 처리합니다.

```java
post1.getComments()  // "1번 게시글의 댓글 필요" → SELECT 쿼리
post2.getComments()  // "2번 게시글의 댓글 필요" → SELECT 쿼리
post3.getComments()  // "3번 게시글의 댓글 필요" → SELECT 쿼리
```

JPA는 이 세 개의 호출이 **같은 맥락**이라는 걸 모릅니다. 각각을 별개의 요청으로 처리하는 거죠. 미래에 필요할 데이터를 미리 예측하고 한 번에 가져올 수는 없는 것입니다.

---

## N+1 문제의 세 가지 해결 방법

### 1️⃣ Fetch Join - SQL JOIN으로 한 번에

```java
@Query("SELECT p FROM Post p JOIN FETCH p.comments")
List<Post> findAllWithComments();
```

**동작:**
```sql
SELECT p.*, c.* 
FROM post p 
INNER JOIN comment c ON p.id = c.post_id;
```

**장점:**
- ✅ 쿼리 1번으로 해결 (가장 빠름)

**단점:**
- ⚠️ 댓글이 없는 게시글은 조회되지 않음 (INNER JOIN)
- ⚠️ LEFT OUTER JOIN 사용 시에도 페이징 불가능
- ⚠️ 게시글에 댓글이 10개면 게시글 데이터가 중복으로 10번 조회됨

### 2️⃣ EntityGraph - 애노테이션으로 더 간단하게

```java
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 순수 JPA 메서드에 바로 적용
    @EntityGraph(attributePaths = {"comments"})
    List<Post> findAll();
    
    @EntityGraph(attributePaths = {"comments"})
    List<Post> findByTitleContaining(String keyword);
    
    @EntityGraph(attributePaths = {"comments"})
    Optional<Post> findById(Long id);
}
```

**동작:**
```sql
SELECT p.*, c.* 
FROM post p 
LEFT OUTER JOIN comment c ON p.id = c.post_id;
```

**장점:**
- ✅ @Query 없이 순수 JPA 메서드에 적용 가능
- ✅ 코드가 매우 깔끔
- ✅ 댓글이 없는 게시글도 조회 가능 (LEFT OUTER JOIN)

**단점:**
- ⚠️ 마찬가지로 페이징 불가능
- ⚠️ 중복 데이터 문제 존재

### 3️⃣ Batch Size - IN 쿼리로 묶어서 조회

```java
@Entity
class Post {
    @Id
    private Long id;
    private String title;
    
    @OneToMany
    @BatchSize(size = 10)  // 핵심!
    private List<Comment> comments;
}
```

**동작:**
```sql
-- 1번째: 게시글 조회
SELECT * FROM post;

-- 2번째: 댓글을 10개씩 묶어서 조회
SELECT * FROM comment 
WHERE post_id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
```

**장점:**
- ✅ 페이징 가능 (게시글에만 페이징 적용)
- ✅ 댓글이 없는 게시글도 조회 가능
- ✅ 유연한 쿼리 전략
- ✅ 대량 데이터 처리에 최적

**단점:**
- ⚠️ 여전히 2번의 쿼리 (Fetch Join보다는 느림)

---

## 세 가지 방법 비교

| 방법 | 쿼리 횟수 | 페이징 | 중복 데이터 | 코드 간결도 |
|------|-----------|--------|-----------|----------|
| **Fetch Join** | 1번 | ❌ | ⚠️ 있음 | 중간 |
| **EntityGraph** | 1번 | ❌ | ⚠️ 있음 | ✅ 우수 |
| **Batch Size** | 2번 | ✅ | ❌ 없음 | ✅ 우수 |

---

## 실무 적용 가이드

### 상황별 선택 전략

#### 1. 페이징이 필요한 목록 조회
```
추천: Batch Size
이유: 게시글은 페이징, 댓글은 모두 한 번에 조회
```

**사용 예:**
```java
// Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Pageable pageable);  // 게시글은 페이징됨
}

// Service - Batch Size 설정으로 댓글은 10개씩 IN 쿼리
List<Post> posts = postRepository.findAll(PageRequest.of(0, 20)).getContent();
posts.forEach(post -> System.out.println(post.getComments().size()));
// 1번: 게시글 20개 조회
// 2번: 댓글 IN 쿼리 (10개씩 2번)
```

#### 2. 상세 조회 페이지 (단일 게시글 + 모든 댓글)
```
추천: EntityGraph
이유: 페이징 불필요, 코드 간결
```

**사용 예:**
```java
@EntityGraph(attributePaths = {"comments"})
Optional<Post> findById(Long id);
```

#### 3. 성능이 극도로 중요한 조회 (여러 연관 데이터)
```
추천: Fetch Join
이유: 쿼리 1번으로 최고 성능
```

**사용 예:**
```java
@Query("SELECT p FROM Post p " +
       "JOIN FETCH p.comments c " +
       "JOIN FETCH p.tags t")
List<Post> findAllWithAllRelations();
```

### 주의사항

#### ❌ 즉시 로딩(Eager Loading)은 피하세요
```java
@OneToMany(fetch = FetchType.EAGER)  // 절대 금지!
```

모든 조회에서 무조건 연관 데이터를 가져오기 때문에 성능이 나빠집니다.

#### ⚠️ Fetch Join + 페이징의 위험성
```java
// 위험한 코드
@Query("SELECT p FROM Post p JOIN FETCH p.comments")
Page<Post> findAllWithComments(Pageable pageable);
```

JPA는 메모리에서 페이징을 수행하므로 데이터가 많으면 심각한 성능 문제가 발생합니다.

#### ⚠️ Fetch Join 사용 시 Distinct 문제
```java
// 문제: 댓글 10개가 있으면 게시글이 10번 중복으로 조회됨
@Query("SELECT p FROM Post p JOIN FETCH p.comments")
List<Post> findAllWithComments();

// 해결 1: SQL의 DISTINCT 사용
@Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.comments")
List<Post> findAllWithComments();

// 해결 2: Set 사용으로 중복 제거
Set<Post> posts = new HashSet<>(findAllWithComments());
```

#### ⚠️ 여러 1:N 관계가 있을 때의 함정
```java
@Entity
class Post {
    @OneToMany
    private List<Comment> comments;      // 1:N
    
    @OneToMany
    private List<Tag> tags;              // 1:N
}

// 위험한 코드 - 곱셈 문제 발생!
@Query("SELECT p FROM Post p " +
       "JOIN FETCH p.comments " +
       "JOIN FETCH p.tags")
List<Post> findAllWithAll();
```

예시: 게시글 1개가 댓글 10개, 태그 5개를 가지고 있다면
- 예상: 1개 게시글 조회
- 실제: 1 × 10 × 5 = 50개의 중복 행 발생!

**해결책:**
```java
// 방법 1: Batch Size 조합 (권장)
@Entity
class Post {
    @OneToMany
    @BatchSize(size = 10)
    private List<Comment> comments;
    
    @OneToMany
    @BatchSize(size = 10)
    private List<Tag> tags;
}

// 방법 2: Fetch Join은 하나만, 나머지는 Batch Size
@Query("SELECT p FROM Post p JOIN FETCH p.comments")
List<Post> findAllWithComments();
```

#### ⚠️ EntityGraph의 다중 관계 처리
```java
// 주의: 여러 관계를 동시에 FETCH하면 중복 문제 발생 가능
@EntityGraph(attributePaths = {"comments", "tags"})
List<Post> findAll();

// 해결: 한 번에 하나씩 로드하고 나머지는 Batch Size에 의존
@EntityGraph(attributePaths = {"comments"})
List<Post> findAll();
```

#### ⚠️ 트랜잭션 범위와 지연 로딩
```java
// 위험한 코드: 트랜잭션 밖에서 지연 로딩 실행
@Transactional(readOnly = true)
public List<Post> getPosts() {
    return postRepository.findAll();
}

public void processData() {
    List<Post> posts = getPosts();  // 트랜잭션 끝남
    posts.forEach(p -> {
        System.out.println(p.getComments().size());  // LazyInitializationException!
    });
}

// 해결 1: Batch Size 또는 Fetch Join 사용
@Transactional(readOnly = true)
public List<Post> getPosts() {
    return postRepository.findAll();  // 여기서 모두 로드됨
}

// 해결 2: 명시적으로 로드
public List<Post> getPosts() {
    List<Post> posts = postRepository.findAll();
    posts.forEach(p -> p.getComments().size());  // 트랜잭션 내에서 미리 로드
    return posts;
}
```

---

## 실전 팁

### 1. Batch Size 기본값 설정
```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100  # 글로벌 설정
```

### 2. Query 로깅으로 모니터링
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 3. 쿼리 프로파일링
```java
// Spring Boot Actuator로 성능 모니터링
@Configuration
public class HibernateMetricsConfiguration {
    @Bean
    public SessionFactoryMetrics sessionFactoryMetrics(SessionFactory sessionFactory) {
        return new SessionFactoryMetrics(sessionFactory);
    }
}
```

---

## 최고의 선택: Batch Size

실무 경험상 **Batch Size**가 가장 실용적인 해결책입니다.

```java
@Entity
class Post {
    @Id
    private Long id;
    private String title;
    
    @OneToMany
    @BatchSize(size = 10)
    private List<Comment> comments;
}

// Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Pageable pageable);
}

// Service
List<Post> posts = postRepository.findAll(PageRequest.of(0, 20)).getContent();
posts.forEach(post -> {
    System.out.println(post.getComments().size());  // N+1 문제 해결!
});
```

**장점:**
- ✅ 게시글 페이징 가능
- ✅ 댓글이 100개든 1000개든 안전 (10개씩 묶음)
- ✅ 댓글이 없는 게시글도 조회됨
- ✅ 설정 변경으로 사이즈 조절 가능
- ✅ 코드가 간결

---

## 마치며

JPA의 N+1 문제는 ORM의 근본적인 특성에서 비롯된 문제입니다. 중요한 건 **상황에 맞는 최적의 해결책을 선택하는 것**입니다.

- **페이징이 필요한 대량 데이터 조회**: Batch Size
- **단일 상세 조회**: EntityGraph
- **극도의 성능 최적화**: Fetch Join

이제 당신도 N+1 문제를 두려워하지 않고, 자신의 상황에 맞는 최적의 방법을 선택할 수 있을 것입니다.

---

**당신은 실무에서 어떤 방법을 주로 사용하고 있나요? 
아니면 N+1 문제로 고민했던 경험이 있다면 댓글로 공유해주세요!**
