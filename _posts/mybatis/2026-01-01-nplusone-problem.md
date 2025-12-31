---
title: "[MyBatis] MyBatis에도 N+1 문제가 있다고? 실무 해결 전략 완벽 정리"

tagline: "JPA만의 문제가 아니다! MyBatis N+1 문제의 원인부터 실전 해결법까지"

header:
  overlay_image: /assets/post/mybatis/2026-01-01-nplusone-problem/overlay.png
  overlay_filter: 0.5

categories:
  - mybatis

tags:
  - MyBatis
  - N+1문제
  - 성능최적화
  - ResultMap
  - 쿼리최적화
  - 데이터베이스

toc: true
show_date: true

last_modified_at: 2026-01-01T15:30:00+09:00
---

많은 개발자들이 **N+1 문제는 JPA에만 있는 것**으로 알고 있습니다. "SQL을 직접 작성하는 MyBatis에는 그런 문제가 없겠지?"라고 생각하죠. 

하지만 현실은 다릅니다. MyBatis에도 N+1 문제는 존재하며, 오히려 **눈에 잘 띄지 않아 더 위험**할 수 있습니다.

이 글에서는 MyBatis의 숨겨진 N+1 문제부터 실무에서 바로 적용 가능한 해결 전략까지 모두 다뤄보겠습니다.

## MyBatis의 N+1 문제, 어디서 발생하나?

### JPA vs MyBatis의 N+1 발생 지점

**JPA의 경우:**
```java
@Entity
public class Post {
    @OneToMany(fetch = FetchType.LAZY)  // 여기서 N+1 발생 가능
    private List<Comment> comments;
}
```

**MyBatis의 경우:**
```xml
<resultMap id="PostWithComments" type="Post">
    <id property="id" column="post_id"/>
    <result property="title" column="title"/>
    <!-- 여기서 N+1 발생! -->
    <collection property="comments" 
                column="post_id" 
                select="selectCommentsByPostId"/>
</resultMap>
```

MyBatis의 `<collection>` 또는 `<association>` 태그에서 **`select` 속성**을 사용하면, 각 row마다 추가 쿼리가 실행됩니다. Post가 100개면 Comment 조회 쿼리가 100번 실행되는 것이죠.

### 왜 눈에 잘 안 띄나?

JPA는 프레임워크가 자동으로 쿼리를 생성하기 때문에 N+1 문제가 유명합니다. 하지만 MyBatis는 개발자가 **SQL을 직접 작성**하기 때문에 "내가 쿼리를 다 작성했는데 무슨 N+1이야?"라고 생각하게 됩니다.

실제로는 MyBatis가 내부적으로 추가 쿼리를 실행하고 있는데 말이죠.

## 해결 전략 1: JOIN + ResultMap으로 한 방에 조회

### 가장 먼저 떠올리는 해결책

N+1 문제를 해결하는 가장 직관적인 방법은 **JOIN을 사용해 한 번에 조회**하는 것입니다.

```xml
<select id="selectPostsWithComments" resultMap="PostWithComments">
    SELECT 
        p.id AS post_id,
        p.title,
        c.id AS comment_id,
        c.content,
        c.post_id
    FROM post p
    LEFT JOIN comment c ON p.id = c.post_id
</select>

<resultMap id="PostWithComments" type="Post">
    <id property="id" column="post_id"/>
    <result property="title" column="title"/>
    <collection property="comments" ofType="Comment">
        <id property="id" column="comment_id"/>
        <result property="content" column="content"/>
    </collection>
</resultMap>
```

### MyBatis의 자동 그룹핑 동작 원리

MyBatis는 `<resultMap>`을 보고 다음과 같이 동작합니다:

1. JOIN 결과로 Post와 Comment가 섞인 여러 row 반환
2. `<id>` 태그의 컬럼으로 Post를 구분
3. 같은 Post id를 가진 row들을 하나의 Post 객체로 묶음
4. `<collection>`의 Comment들은 자동으로 List에 수집

개발자가 직접 그룹핑 로직을 작성할 필요가 없습니다!

### 치명적인 문제점: 데이터 중복 전송

하지만 여기에는 **숨겨진 성능 문제**가 있습니다.

**상황:**
- Post 1개 (데이터 크기: 1KB)
- Comment 50개 (각 0.5KB)

**전송량 비교:**
- **N+1 방식**: 1KB + 25KB = **26KB**
- **JOIN 방식**: (1KB + 0.5KB) × 50 = **75KB** ❌

JOIN 결과에서 Post 데이터가 50번 중복되어 전송됩니다! 댓글이 많을수록 Post 데이터가 계속 반복되는 것이죠.

게다가:
- 네트워크 전송량 증가
- 서버 메모리에서 중복 데이터 처리
- 그룹핑 연산 부하

## 해결 전략 2: 2번의 쿼리로 분리 (Best Practice)

### 가장 효율적인 접근법

실무에서 가장 많이 사용하는 방법은 **쿼리를 2번으로 나누되, 데이터 중복 없이 조회**하는 것입니다.

**1단계: Post 조회**
```xml
<select id="selectPosts" resultType="Post">
    SELECT * FROM post
    WHERE ... 
    LIMIT 10 OFFSET 0
</select>
```

**2단계: Comment 일괄 조회**
```xml
<select id="selectCommentsByPostIds" resultType="Comment">
    SELECT * FROM comment
    WHERE post_id IN
    <foreach collection="postIds" item="id" 
             open="(" separator="," close=")">
        #{id}
    </foreach>
</select>
```

**3단계: Java 코드에서 매핑**
```java
// 1. Post 조회
List<Post> posts = postMapper.selectPosts();

// 2. Post ID 수집
List<Long> postIds = posts.stream()
    .map(Post::getId)
    .collect(Collectors.toList());

// 3. Comment 일괄 조회
List<Comment> comments = commentMapper.selectCommentsByPostIds(postIds);

// 4. Map으로 그룹핑
Map<Long, List<Comment>> commentMap = comments.stream()
    .collect(Collectors.groupingBy(Comment::getPostId));

// 5. Post에 Comment 매핑
posts.forEach(post -> {
    List<Comment> postComments = commentMap.getOrDefault(
        post.getId(), 
        Collections.emptyList()
    );
    post.setComments(postComments);
});
```

### 왜 이 방법이 최선인가?

**장점:**
- ✅ 쿼리 2번 (N+1보다 훨씬 적음)
- ✅ 데이터 중복 전송 없음 (JOIN보다 효율적)
- ✅ DB 부하 최소화 (JOIN 연산 불필요)
- ✅ 성능 예측 가능
- ✅ 코드로 제어 가능 (디버깅 쉬움)

**단점:**
- ❌ 코드 길이 증가
- ❌ 매핑 로직 직접 작성 필요
- ❌ 보일러플레이트 코드

하지만 **"코드가 좀 길어도 명확하고 성능 좋은 게 낫다"**는 것이 실무의 정석입니다. 특히 트래픽이 많은 서비스에서는 더욱 그렇습니다.

## 페이징 처리 시의 전략

### 페이징이 들어가면 더 간단해진다

실무에서는 대부분 페이징 처리를 하기 때문에, 오히려 더 효율적입니다.

```java
// 1. Post 10개만 조회 (페이징)
List<Post> posts = postMapper.selectPosts(page, size);  // 10개

// 2. Comment 조회 (딱 10개의 Post ID만)
List<Long> postIds = extractIds(posts);  // 10개의 ID만
List<Comment> comments = commentMapper.selectCommentsByPostIds(postIds);

// 3. 매핑
mapCommentsToPost(posts, comments);
```

**페이징의 이점:**
- IN절에 들어가는 ID가 적음 (10개 vs 10,000개)
- 메모리 사용량 최소화
- 빠른 응답 속도

### Comment도 페이징: 미리보기 전략

Comment가 너무 많을 경우 **Comment도 페이징 처리**를 고려해야 합니다.

**방법 1: 미리보기 방식 (Window 함수 활용)**
```sql
SELECT * FROM (
    SELECT 
        *,
        ROW_NUMBER() OVER (
            PARTITION BY post_id 
            ORDER BY created_at DESC
        ) as rn
    FROM comment
    WHERE post_id IN (1, 2, 3, ..., 10)
) sub
WHERE rn <= 3  -- 각 Post당 최신 3개만
```

👉 목록 화면에서 많이 사용 (유튜브 댓글 미리보기처럼)

**방법 2: API 완전 분리**
```
GET /posts?page=1          → Post 목록 + Comment 개수만
GET /posts/1/comments?page=1  → Comment는 상세보기에서
```

👉 트래픽 분산, 명확한 책임 분리

## 실무 적용 가이드

### 1단계: N+1 문제 발견하기

**개발 단계: 쿼리 로그 확인**
```yaml
# application.yml
logging:
  level:
    org.apache.ibatis: DEBUG  # MyBatis 쿼리 로그 활성화
```

로그를 보면 쿼리가 여러 번 실행되는 것을 확인할 수 있습니다.

**테스트 단계: 쿼리 카운터**
```java
@Test
void testNPlusOne() {
    // 쿼리 카운트 측정
    int beforeCount = getQueryCount();
    
    List<Post> posts = postService.getPosts();
    
    int afterCount = getQueryCount();
    int executedQueries = afterCount - beforeCount;
    
    // 쿼리는 5번 이하만 허용
    assertThat(executedQueries).isLessThan(5);
}
```

**운영 단계: APM 모니터링**
- Pinpoint, New Relic, DataDog 활용
- Slow Query Log 분석
- DB Connection Pool 모니터링

**예방 단계: 코드 리뷰**
```xml
<!-- 이런 코드가 보이면 빨간불! -->
<collection property="comments" 
            select="selectComments"/>  ⚠️ N+1 위험!
```

### 2단계: 상황별 해결 전략 선택

| 상황 | 추천 방법 | 이유 |
|------|-----------|------|
| 소량 데이터 (< 100개) | JOIN + ResultMap | 간단하고 충분히 빠름 |
| 중량 데이터 (100~1000개) | 2번 쿼리 분리 | 데이터 중복 최소화 |
| 대량 데이터 (> 1000개) | 페이징 + 2번 쿼리 | 필수! |
| 관계가 1:Many이고 Many가 많음 | Comment 페이징 | 미리보기 또는 API 분리 |

### 3단계: 성능 최적화는 측정 후 진행

**조기 최적화는 악의 근원입니다.** (Premature optimization is the root of all evil)

실무 접근법:
1. **먼저 빠르게 개발** → 기능 우선
2. **운영하면서 모니터링** → 실제 부하 확인
3. **문제 발견 시 최적화** → 측정 기반 개선
4. **AI 도움받아 리팩토링** → 빠른 개선

사용자가 10명일 때는 N+1이 100번 발생해도 문제없습니다. 사용자 10만 명이 되면 그때 최적화하면 됩니다.

## 실전 코드 예제

### 완전한 Service 구현

```java
@Service
@RequiredArgsConstructor
public class PostService {
    
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    
    /**
     * N+1 문제를 해결한 Post 목록 조회
     */
    public List<PostDto> getPostsWithComments(int page, int size) {
        // 1. Post 페이징 조회
        List<Post> posts = postMapper.selectPosts(page, size);
        
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 2. Post ID 추출
        List<Long> postIds = posts.stream()
            .map(Post::getId)
            .collect(Collectors.toList());
        
        // 3. Comment 일괄 조회 (단 1번의 쿼리)
        List<Comment> comments = commentMapper.selectCommentsByPostIds(postIds);
        
        // 4. Post별 Comment 그룹핑
        Map<Long, List<Comment>> commentMap = comments.stream()
            .collect(Collectors.groupingBy(Comment::getPostId));
        
        // 5. DTO 변환 + 매핑
        return posts.stream()
            .map(post -> {
                List<Comment> postComments = commentMap.getOrDefault(
                    post.getId(), 
                    Collections.emptyList()
                );
                return PostDto.of(post, postComments);
            })
            .collect(Collectors.toList());
    }
}
```

### Mapper XML

```xml
<!-- PostMapper.xml -->
<mapper namespace="com.example.mapper.PostMapper">
    
    <!-- Post 페이징 조회 -->
    <select id="selectPosts" resultType="Post">
        SELECT 
            id,
            title,
            content,
            author_id,
            created_at
        FROM post
        ORDER BY created_at DESC
        LIMIT #{size} OFFSET #{offset}
    </select>
    
</mapper>

<!-- CommentMapper.xml -->
<mapper namespace="com.example.mapper.CommentMapper">
    
    <!-- Comment 일괄 조회 (IN 절 사용) -->
    <select id="selectCommentsByPostIds" resultType="Comment">
        SELECT 
            id,
            post_id,
            content,
            author_id,
            created_at
        FROM comment
        WHERE post_id IN
        <foreach collection="postIds" item="id" 
                 open="(" separator="," close=")">
            #{id}
        </foreach>
        ORDER BY created_at DESC
    </select>
    
    <!-- Comment 미리보기 조회 (각 Post당 3개씩) -->
    <select id="selectPreviewComments" resultType="Comment">
        SELECT * FROM (
            SELECT 
                *,
                ROW_NUMBER() OVER (
                    PARTITION BY post_id 
                    ORDER BY created_at DESC
                ) as row_num
            FROM comment
            WHERE post_id IN
            <foreach collection="postIds" item="id" 
                     open="(" separator="," close=")">
                #{id}
            </foreach>
        ) ranked
        WHERE row_num <![CDATA[<=]]> 3
    </select>
    
</mapper>
```

## MyBatis vs JPA의 N+1 해결 비교

| 항목 | MyBatis | JPA |
|------|---------|-----|
| **발생 원인** | `<collection select="">` | Lazy Loading |
| **기본 해결책** | JOIN + ResultMap | Fetch Join |
| **고급 해결책** | 2번 쿼리 분리 | @BatchSize |
| **제어 수준** | 완전 수동 (SQL 직접) | 반자동 (JPQL) |
| **학습 곡선** | SQL 이해 필요 | JPA 메커니즘 이해 필요 |
| **디버깅** | 쿼리 로그로 명확 | 때때로 불명확 |

**핵심:** 두 기술 모두 N+1 문제가 있으며, 본질적인 해결 방법(JOIN 또는 Batch Fetch)은 동일합니다.

## 마무리: 실무 팁 정리

### ✅ 해야 할 것

1. **쿼리 로그 항상 확인**
   - 개발 환경에서 MyBatis DEBUG 로그 켜기
   - 예상과 다르게 쿼리가 여러 번 실행되는지 체크

2. **`<collection select="">` 지양**
   - 편하지만 N+1의 주범
   - 대신 2번 쿼리 분리 방식 사용

3. **페이징은 필수**
   - 목록 조회는 무조건 페이징 처리
   - 10개씩 끊어서 처리하면 성능 문제 대부분 해결

4. **측정 기반 최적화**
   - 문제가 생기기 전에 과도한 최적화 금지
   - APM으로 실제 병목 지점 확인 후 개선

### ❌ 하지 말아야 할 것

1. **JOIN이 무조건 정답이라고 생각**
   - 데이터 중복 전송 문제 고려
   - 관계가 복잡하면 오히려 역효과

2. **복잡한 쿼리를 한 방에 해결하려는 욕심**
   - 가독성과 유지보수성 희생
   - 2~3번의 단순한 쿼리가 더 나을 수 있음

3. **Java 코드 매핑을 무조건 꺼리기**
   - "XML에서 다 해결해야 해!" → 고정관념
   - 코드로 매핑하면 디버깅도 쉽고 명확함

## 당신의 경험은 어떤가요?

이 글에서 다룬 MyBatis N+1 문제 해결 전략들, 실무에서 어떻게 적용하고 계신가요?

- JOIN과 2번 쿼리 분리 중 어느 것을 선호하시나요?
- 페이징 처리할 때 특별한 노하우가 있으신가요?
- N+1 문제로 고생했던 경험이 있으신가요?

댓글로 여러분의 경험과 노하우를 공유해주세요! 함께 배우고 성장하는 개발자 커뮤니티를 만들어가요. 🚀
