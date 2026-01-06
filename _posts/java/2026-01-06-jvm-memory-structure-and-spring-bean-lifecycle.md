---
title: "[Java] JVM 메모리 구조와 Spring Bean 생명주기 완벽 이해하기"

tagline: "Stack, Heap, GC부터 Singleton Bean까지 - Spring 개발자가 꼭 알아야 할 JVM의 모든 것"

header:
  overlay_image: /assets/post/java/2026-01-06-jvm-memory-structure-and-spring-bean-lifecycle/overlay.png
  overlay_filter: 0.5

categories:
  - Java

tags:
  - JVM
  - Memory
  - Heap
  - Stack
  - GarbageCollection
  - Spring

toc: true
show_date: true

last_modified_at: 2026-01-06T23:59:59+09:00
---

Spring Framework를 매일 사용하면서도, 그 아래에서 JVM이 메모리를 어떻게 관리하는지 생각해본 적 있으신가요?

`@Service`, `@Autowired`, `@Transactional` 같은 편리한 어노테이션 뒤에는 **JVM의 메모리 할당, 객체 생명주기, Garbage Collection**이라는 복잡한 메커니즘이 숨어있습니다.

이 글은 **"JVM의 행동을 볼 수 있는 눈"**을 갖고 싶은 개발자를 위해 작성되었습니다. 실무에서 문제가 터지기 전에, Stack과 Heap의 차이부터 GC 동작 원리, Spring Bean Scope까지 완벽히 이해해봅시다.

> **핵심 메시지:** Spring Bean은 JVM 위에서 도는 평범한 객체입니다. JVM을 모르면 Spring을 디버깅할 수 없습니다.

---

## Stack vs Heap - 객체는 어디에 저장될까?

### Stack 메모리: 메소드 실행의 작업 공간

Stack은 **각 Thread마다 독립적으로 생성**되며, 메소드 호출 시 생성되는 Stack Frame에 다음을 저장합니다:

- **로컬 변수** (primitive 타입, 객체 참조 주소)
- **매개변수**
- **메소드 호출 정보**

```java
@Service
public class UserService {
    public User getUser(Long id) {  // Stack Frame 생성
        User user = new User();      // user 변수(주소값)는 Stack에
        String name = user.getName(); // name 변수(참조)는 Stack에
        return user;
    } // Stack Frame 제거
}
```

**Thread A와 Thread B가 동시에 호출하면?**

```
[Thread A Stack]  id=1, user=0x1000, name=0x2000
[Thread B Stack]  id=2, user=0x3000, name=0x4000

→ 각 Thread는 독립적인 Stack을 가지므로 Thread-safe!
```

### Heap 메모리: 객체의 실제 저장소

모든 객체(Object)는 Heap에 저장되며, **모든 Thread가 공유**합니다.

```java
@Service  // Singleton Bean
public class UserService {
    private final UserRepository userRepository;  // Heap의 참조값
    
    public User getUser(Long id) {
        User user = userRepository.findById(id);
        // user 변수는 Stack, 실제 User 객체는 Heap
        return user;
    }
}
```

**메모리 구조:**

```
[Heap 메모리]
UserService@0x1000 (Singleton, 1개만 존재)
  └─ userRepository → UserRepository@0x2000
User@0x3000 (getUser()로 생성된 객체)
User@0x4000 (또 다른 요청으로 생성)

[Thread A Stack]  user → 0x3000
[Thread B Stack]  user → 0x4000
```

**핵심 포인트:**
- UserService는 **Heap에 1개**만 존재 (Singleton)
- 각 Thread는 **독립적인 Stack 변수**를 가짐
- 그래서 Singleton Bean이 Thread-safe한 것!

---

## new 키워드의 진짜 의미

```java
User user = new User("John");
```

이 한 줄에서 일어나는 일:

### 1단계: Heap에 메모리 할당
```
new 키워드 → JVM이 Heap의 Eden 영역에 객체 크기만큼 메모리 할당
```

### 2단계: 생성자 실행
```
User 생성자 호출 → 필드 초기화
```

### 3단계: 참조값 저장
```
user 변수(Stack)에 객체의 메모리 주소 저장
user = 0x7F8A2C3D
```

**중요한 사실:**

```java
User original = new User("Alice");

public void modify(User user) {
    user.setName("Bob");          // ✅ 원본 객체 수정됨!
    user = new User("Charlie");   // ❌ 새 객체 할당 (로컬만 변경)
}

modify(original);
System.out.println(original.getName());  // "Bob"
```

**Java는 Pass by Value이지만, 그 값이 "참조값(주소)"입니다!**

```
[main Stack]   original → 0x1000 (Alice 객체)
[modify Stack] user → 0x1000 (주소값 복사!)

둘 다 같은 객체를 가리킴 → setName()은 원본에 영향!
새로운 객체 할당(new)은 복사된 변수만 변경!
```

---

## Garbage Collection - 메모리 회수의 비밀

### GC Root: 살아있는 객체의 출발점

**GC는 "Root에서 도달 가능한가?"로 객체 생존 여부를 판단합니다.**

**GC Root가 되는 것들:**
1. **Stack의 로컬 변수**
2. **Static 변수** (Metaspace)
3. **JNI References**
4. **Active Threads**

```java
@Service  // Static 영역에 등록 → GC Root!
public class UserService {
    private List<User> users = new ArrayList<>();
    
    public void temp() {
        User local = new User();  // Stack → GC Root
    } // local 참조 사라짐 → GC 수거 대상!
}
```

**GC 동작 방식 (Mark & Sweep):**

```
Step 1 (Mark): Root에서 출발 → 참조 그래프 탐색 (BFS/DFS)
               도달 가능한 객체에 "살아있음" 표시

[Root] → UserService ✅
         └─ users ✅
            ├─ User1 ✅
            ├─ User2 ✅
            └─ User3 ✅

Heap의 User@0x9999 ❌ (고아 객체)

Step 2 (Sweep): 표시 안 된 객체들 메모리 회수
```

### Young Generation vs Old Generation

**왜 Heap을 나눴을까?**

```
Weak Generational Hypothesis: 대부분의 객체는 금방 죽는다!

public void process() {
    String temp = "temporary";  // 메소드 끝나면 바로 죽음
    User temp2 = new User();    // 바로 죽음
}
// 이런 "젊은" 객체들이 전체의 90% 이상!
```

**메모리 구조:**

```
Young Generation (새로 생성된 객체)
├─ Eden: new로 생성되는 곳
└─ Survivor (S0, S1): Minor GC 생존자

Old Generation (오래 살아남은 객체)
└─ Tenured: 여러 번 GC 생존 시 승격
```

**성능 최적화:**
- **Minor GC** (Young): 자주 실행, 빠름
- **Major GC** (Old): 가끔 실행, 느림

→ 자주 죽는 것들만 자주 확인!

### Stop-the-World: GC의 치명적 단점

**GC가 실행되는 동안 모든 애플리케이션 Thread가 멈춥니다!**

**왜 멈춰야 할까?**

```java
// GC가 Mark 중이라고 가정
[Root] → A → B → C (탐색 중...)

// 만약 애플리케이션이 동시 실행된다면?
Thread1: B.ref = null;   // 참조 끊김!
Thread2: A.ref = D;      // 새 참조 추가!

// 결과: 잘못된 수거, 살아있는 객체 제거 → 💥
```

**실무 영향:**

```java
public User getUser(Long id) {
    // 여기서 갑자기 Full GC 발생!
    // → 0.5초 ~ 10초 멈춤
    return userRepository.findById(id);
}
// 사용자는 응답을 기다리다가 타임아웃!
```

**최신 GC의 발전:**

```bash
# 전통적 GC
Parallel GC: 10GB Heap → 3초 STW

# 최신 GC
G1 GC:  STW 최소화
ZGC:    10GB Heap → 5ms STW (!)
Shenandoah: Concurrent Compaction
```

---

## Spring Bean Scope와 메모리 관계

### Singleton Scope: Spring의 기본값

```java
@Service  // 기본값 Singleton
public class UserService {
    private final UserRepository userRepository;
}
```

**메모리 특징:**
- **생성 시점:** ApplicationContext 초기화 (ComponentScan)
- **개수:** Spring Container 당 **1개**
- **저장 위치:** Heap (GC Root로 등록)
- **GC:** 애플리케이션 종료 시까지 절대 수거 안 됨!

**왜 Thread-safe?**

```
[Heap] UserService@0x1000 (Singleton, 공유)

[Thread A Stack]  id=1, user=0x3000
[Thread B Stack]  id=2, user=0x4000

→ 메소드의 로컬 변수는 각 Thread Stack에 독립적!
→ userRepository는 읽기만 함 (Stateless) → 안전!
```

⚠️ **하지만 이건 위험:**

```java
@Service
public class UserService {
    private List<User> cache = new ArrayList<>();  // ⚠️ Mutable!
    
    public void addCache(User user) {
        cache.add(user);  // Thread-unsafe!
    }
}
```

**두 가지 문제:**

**① Thread-safety 문제**
```
Thread A: cache.add(user1)  // 내부 배열 resize 중...
Thread B: cache.add(user2)  // 동시에 resize!
→ ArrayIndexOutOfBoundsException!
```

**② Memory Leak (OOM)**
```
GC Root: UserService (Singleton)
  ↓ (도달 가능)
cache (List)
  ↓ (도달 가능)
User1, User2, ... User999999

→ 모든 User가 GC Root에 도달 가능!
→ GC가 절대 제거 안 함
→ OutOfMemoryError!
```

### Prototype Scope: 매번 새 인스턴스

```java
@Component
@Scope("prototype")
public class TaskProcessor {
    // 요청마다 새 인스턴스
}
```

**메모리 특징:**
- **생성 시점:** `getBean()` 또는 `@Autowired` 주입 시마다
- **개수:** 요청할 때마다 새로 생성
- **저장 위치:** Heap의 **Eden 영역** (일반 객체처럼)
- **GC:** ⚠️ **Spring이 관리 안 함!** 참조 끊기면 바로 GC 대상

**실무 예시:**

```java
@Service
public class TaskService {
    @Autowired
    private ApplicationContext context;
    
    public void process() {
        TaskProcessor p1 = context.getBean(TaskProcessor.class);
        TaskProcessor p2 = context.getBean(TaskProcessor.class);
        // p1 != p2 (다른 객체!)
        
        p1.process();
        p2.process();
    } // 메소드 끝 → p1, p2 참조 사라짐 → GC 대상!
}
```

**메모리 흐름:**

```
요청 1: new TaskProcessor@0x1000 → Eden 할당
요청 2: new TaskProcessor@0x2000 → Eden 할당
메소드 종료 → Stack 참조 제거 → GC 수거

vs Singleton:
UserService@0x1000 → 계속 살아있음 (Old Generation)
```

### Request Scope: HTTP 요청마다

```java
@Component
@RequestScope
public class RequestContext {
    private String requestId;
    private Long userId;
}
```

**메모리 특징:**
- **생성 시점:** HTTP 요청마다 1개
- **개수:** 동시 요청 수만큼
- **저장 위치:** Heap의 Eden 영역
- **GC:** 요청 처리 완료 후 참조 끊기면 GC 대상

**Thread-local처럼 동작:**
```
Request A → RequestContext@0x1000 (Thread A Stack에서 참조)
Request B → RequestContext@0x2000 (Thread B Stack에서 참조)

→ 같은 요청 내에서는 같은 인스턴스
→ 다른 요청은 각자 독립!
```

**실무 예시:**

```java
@RestController
public class UserController {
    @Autowired
    private RequestContext requestContext;  // 요청마다 다른 객체!
    
    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        requestContext.setUserId(id);  // 이 요청에만 영향
        return userService.getUser(id);
    } // 응답 완료 → RequestContext 참조 제거 → GC 대상
}
```

---

## 실무: Memory Leak 추적하기

### 증상 파악

```
정상: Heap 사용량
████████████ (Full GC 후)
██░░░░░░░░░░ (Young GC 후)
████████████ (Full GC 후)
...반복...

Leak 발생: Heap 사용량
█░░░░░░░░░░░
███░░░░░░░░░
██████░░░░░░
██████████░░
████████████░
█████████████ → OutOfMemoryError!
```

### 도구 활용

**① VisualVM (초보자 추천)**
```bash
jvisualvm  # GUI 실행

Monitor 탭: Heap 사용량 실시간 그래프
Memory 탭: Heap Dump 생성 (.hprof)
Threads 탭: Thread 상태 확인
```

**② jstat (프로덕션 환경)**
```bash
jstat -gc -h10 <pid> 1000
# Young, Old 영역 크기, GC 횟수 실시간 추적
# Old 영역이 계속 증가 → Memory Leak 의심!
```

**③ jmap (Heap Dump)**
```bash
jmap -dump:live,format=b,file=heap.bin <pid>
# heap.bin 파일 생성
# VisualVM에서 분석: 가장 많은 메모리를 점유하는 객체 확인
```

**④ Prometheus + Grafana (모니터링)**
```
jvm_memory_used_bytes{area="heap"} 지속 증가 감지
jvm_gc_pause_seconds 증가 → GC 빈도 증가
```

### 해결 전략

**코드 개선:**

```java
// ❌ 나쁜 예: 무한정 캐싱
@Service
public class UserService {
    private Map<Long, User> cache = new HashMap<>();
    
    public User getUser(Long id) {
        return cache.computeIfAbsent(id, 
            k -> userRepository.findById(k));
    }
}

// ✅ 좋은 예: 크기 제한 + TTL
@Service
public class UserService {
    private final Cache<Long, User> cache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();
    
    public User getUser(Long id) {
        return cache.get(id, 
            k -> userRepository.findById(k));
    }
}
```

**JVM 튜닝:**

```bash
# Heap 크기 조정
-Xms2g -Xmx2g

# GC 알고리즘 변경
-XX:+UseG1GC          # 기본 추천
-XX:+UseZGC           # 초저지연 필요 시

# GC 로그 활성화
-Xlog:gc*:file=gc.log
-XX:+PrintGCDetails
```

---

## 정리하며

### 핵심 개념 요약

**Stack vs Heap**
- Stack: Thread 독립, 메소드 로컬 변수, 빠름
- Heap: Thread 공유, 모든 객체 저장, GC 대상

**Garbage Collection**
- GC Root에서 도달 불가능한 객체 제거
- Young (Eden → Survivor) → Old 승격
- Stop-the-World: GC 중 애플리케이션 멈춤

**Spring Bean Scope**
- Singleton: Heap에 1개, Old Generation, Thread-safe (Stateless)
- Prototype: 요청마다 생성, Eden 할당, Spring 관리 안 함
- Request: HTTP 요청마다 생성, 요청 종료 시 GC 대상

**Memory Leak 방지**
- Singleton에 Mutable Collection 저장 금지
- 캐시는 크기 제한 + TTL 설정
- Heap 사용량 모니터링 (VisualVM, Prometheus)

### 실무에서 기억할 것

**✅ Spring Bean은 평범한 JVM 객체다**
- Singleton도 결국 Heap에 저장
- GC Root로 등록되어 절대 수거 안 됨
- Thread-safety는 코드로 보장해야 함

**✅ JVM을 이해해야 Spring을 디버깅할 수 있다**
- OOM 발생 시: Heap Dump 분석
- 응답 느림: GC 로그 확인
- Memory Leak: GC Root 추적

**✅ 성능 튜닝의 출발점**
- 불필요한 객체 생성 최소화 (Prototype 주의)
- GC 알고리즘 선택 (G1, ZGC)
- Heap 크기 적절히 설정

---

## 참고 자료

- [Oracle JDK GC Tuning Guide](https://docs.oracle.com/en/java/javase/17/gctuning/)
- [Spring Framework - Bean Scopes](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html)
- [JVM Garbage Collection Handbook](https://plumbr.io/handbook/garbage-collection-in-java)
- [공식 문서: Oracle JDK Garbage Collection](https://docs.oracle.com/en/java/javase/17/gctuning/introduction-garbage-collection-tuning.html)
- [공식 문서: Spring Bean Scopes - Prototype](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html#beans-factory-scopes-prototype)

---

**당신은 어떤 경험이 있으신가요?** JVM 메모리 관련 문제를 겪어보신 적 있다면 댓글로 공유해주세요!
