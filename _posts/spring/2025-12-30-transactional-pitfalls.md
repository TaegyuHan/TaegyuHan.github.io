---
title: "[Spring Boot] @Transactional의 함정과 올바른 사용법"

tagline: "@Transactional을 올바르게 이해하고 사용하기 위한 실무 가이드"

header:
  overlay_image: /assets/post/spring/2025-12-30-transactional-pitfalls/overlay.png
  overlay_filter: 0.5

categories:
  - Spring

tags:
  - Spring Boot
  - Transactional
  - Transaction Management
  - AOP
  - Database
  - Best Practices

toc: true
show_date: true

last_modified_at: 2025-12-30T23:55:00
---

Spring Boot 개발자라면 누구나 `@Transactional`을 사용해봤을 거에요. 하지만 **단순히 "메서드에 붙이면 트랜잭션이 된다"고 생각하는 것만으로는 충분하지 않습니다.** 

실무에서 자주 마주치는 "분명히 @Transactional을 붙였는데 왜 롤백이 안 되지?"라는 문제들은 대부분 **@Transactional의 동작 원리를 모르기 때문**에 발생해요. 

이 글에서는 우리가 실제로 토론한 내용을 바탕으로 **@Transactional의 함정들과 실무에서 어떻게 대처해야 하는지**를 알아봅시다.

---

## 토론 요약: @Transactional의 5가지 함정

이번 토론에서 우리가 발견한 @Transactional의 주요 함정들은:

1. **Self-invocation 문제** - 같은 클래스 내부 호출 시 트랜잭션이 안 걸림
2. **Exception 타입에 따른 롤백 차이** - Checked Exception은 기본적으로 롤백 안 됨
3. **복잡한 예외 처리 로직** - try-catch의 가독성 문제
4. **Propagation 속성의 이해 부족** - 중첩 호출 시 트랜잭션 범위 혼동
5. **과도한 복잡성** - 모든 속성을 다 쓰려다가 코드가 복잡해짐

---

## 함정 1: Self-Invocation 문제 (프록시 패턴)

### 문제 상황

`@Transactional`이 작동하는 핵심은 **Spring AOP의 프록시 패턴**입니다. Spring은 `@Transactional`이 붙은 메서드를 감싸는 프록시 객체를 생성해요:

- **외부에서 호출** → 프록시를 거쳐서 호출 → 트랜잭션 O
- **같은 클래스 내부에서 호출** → this를 통해 직접 호출 → 트랜잭션 X

### 코드 예제

```java
@Service
public class UserService {
    
    // 외부에서 호출하면 @Transactional이 적용됨
    public void registerUser(User user) {
        saveUser(user);  // ❌ 이 호출은 프록시를 거치지 않음
    }
    
    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
        
        // 예외가 발생해도 롤백이 안 됨!
        if (user.getAge() < 0) {
            throw new IllegalArgumentException("Invalid age");
        }
    }
}
```

`registerUser`에서 `saveUser`를 호출하는 것은 내부적으로 `this.saveUser()`와 같아요. 프록시를 거치지 않으므로 `@Transactional`이 적용되지 않습니다.

### 해결 방법

**1) 메서드 분리 후 외부에서 호출하도록 설계:**
```java
@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    
    // 트랜잭션이 필요한 부분은 별도 메서드로 분리
    @Transactional
    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }
    
    // 트랜잭션이 필요 없는 로직은 이 메서드에서
    public void processPayment(Payment payment) throws IOException {
        savePayment(payment);  // 외부 호출이므로 프록시를 거침
        sendPaymentEmail(payment);
    }
}
```

**2) 별도 클래스로 분리:**
```java
@Service
public class TransactionalUserService {
    @Transactional
    public void saveUser(User user) {
        // 트랜잭션 관련 작업
    }
}

@Service
public class UserService {
    @Autowired
    private TransactionalUserService transactionalUserService;
    
    public void registerUser(User user) {
        transactionalUserService.saveUser(user);  // 외부 호출
    }
}
```

---

## 함정 2: Exception 타입에 따른 롤백 차이

### 문제 상황

많은 개발자들이 모르는 사실: **Spring은 Exception 타입에 따라 롤백 여부가 달라집니다.**

- **RuntimeException** (NullPointerException, IllegalArgumentException 등) → **롤백 O**
- **Checked Exception** (Exception, IOException 등) → **롤백 X** ⚠️

이는 Spring의 설계 철학 때문인데, Checked Exception은 "예상 가능한 오류"로 보기 때문이에요.

### 왜 이렇게 설계했을까?

이를 이해하려면 먼저 **Java Exception의 계층구조**를 알아야 해요:

```
Throwable
├── Error (시스템 오류)
└── Exception
    ├── Checked Exception (IOException, SQLException 등)
    └── RuntimeException
        ├── NullPointerException
        ├── IllegalArgumentException
        └── 등등...
```

**Spring의 설계 원칙:**

**1) Checked Exception = "예상된 상황의 실패"**
```java
// IOException은 "파일이 없을 수도 있다"는 걸 미리 알고 있음
public void sendPaymentEmail(Payment payment) throws IOException {
    // 개발자가 미리 예상하고 처리 전략을 정했을 확률이 높음
    // "메일 전송 실패 → 사용자에게 알림" 같은 처리를 할 수 있음
}
```

- 컴파일 타임에 처리를 **강제**함
- 개발자가 처리 방법을 알고 있다고 가정
- 비즈니스 로직상 "예상된 상황"일 가능성 높음

**2) RuntimeException = "프로그래밍 오류"**
```java
// NullPointerException은 개발자가 의도하지 않은 오류
public void saveUser(User user) {
    user.getName();  // ❌ user가 null이면 예상 밖의 상황!
}
```

- 컴파일 타임에 처리를 강제하지 않음
- 개발자가 의도하지 않은 오류 = **버그**
- **데이터 일관성이 보장되지 않으므로 무조건 롤백 필수**

### 비유로 이해하기

```
Checked Exception: "예상된 상황의 실패"
├─ "파일을 찾을 수 없었어" (IOException)
├─ "DB 연결이 끊어졌어" (SQLException)
└─ → 개발자가 대처 방법을 알고 있을 거야
   → 트랜잭션 롤백할지 말지 판단해야 함

RuntimeException: "뭔가 잘못됐어" (= 버그!)
├─ "null을 참조했어" (NullPointerException)
├─ "잘못된 값을 전달했어" (IllegalArgumentException)
└─ → 이건 예상 밖의 오류야! 
   → 데이터를 저장하면 안 돼
   → 무조건 롤백해야 함
```

### 코드 예제

```java
@Service
public class PaymentService {
    
    @Transactional
    public void processPayment(Payment payment) throws IOException {
        paymentRepository.save(payment);  // DB에 저장됨
        
        // Checked Exception 발생
        sendPaymentEmail(payment);  // IOException 발생!
    }
    
    private void sendPaymentEmail(Payment payment) throws IOException {
        // 이메일 전송 로직...
    }
}
```

이 상황에서:
- **결제 정보는 DB에 저장됨** (커밋됨)
- **이메일 전송은 실패함**
- **트랜잭션은 롤백되지 않음** ❌

결과적으로 DB에는 데이터가 남아있고, 이메일만 실패하는 상태가 됩니다. 이건 비즈니스 로직상 큰 문제가 될 수 있어요!

### 해결 방법

**1) rollbackFor 속성으로 명시 (권장):**
```java
@Transactional(rollbackFor = Exception.class)
public void processPayment(Payment payment) throws IOException {
    paymentRepository.save(payment);
    sendPaymentEmail(payment);  // IOException 발생 시 롤백됨
}
```

**2) Unchecked Exception으로 변환:**
```java
@Transactional
public void processPayment(Payment payment) {
    paymentRepository.save(payment);
    
    try {
        sendPaymentEmail(payment);
    } catch (IOException e) {
        // RuntimeException으로 변환하면 자동 롤백
        throw new RuntimeException("Failed to send email", e);
    }
}
```

**3) 비즈니스 로직으로 분리 (복잡도가 높을 때):**
```java
@Service
public class PaymentService {
    @Autowired
    private EmailService emailService;
    
    @Transactional
    public void processPayment(Payment payment) {
        paymentRepository.save(payment);
        // 이메일은 트랜잭션 밖에서 처리
    }
    
    public void sendPaymentEmail(Payment payment) {
        try {
            emailService.send(payment);
        } catch (IOException e) {
            log.error("Failed to send email", e);
            // 로깅만 하고 진행
        }
    }
}
```

---

## 함정 3: 예외 처리와 가독성

### 문제 상황

위의 "Unchecked Exception으로 변환"하는 방법은 작동하지만, **코드가 복잡해집니다:**

```java
@Transactional
public void processPayment(Payment payment) {
    paymentRepository.save(payment);
    
    try {
        sendPaymentEmail(payment);
    } catch (IOException e) {
        throw new RuntimeException("Failed to send email", e);
    }
}
```

try-catch 블록이 많아지면 **비즈니스 로직이 묻혀버리고** 코드 가독성이 떨어져요.

### 최선의 실무 해법

**명시적인 설정이 가장 깔끔합니다:**

```java
@Transactional(rollbackFor = Exception.class)
public void processPayment(Payment payment) throws IOException {
    paymentRepository.save(payment);
    sendPaymentEmail(payment);
}
```

- 코드가 깔끔해요 ✅
- 의도가 명확해요 ✅
- 예외 처리가 안 숨겨져요 ✅

---

## 함정 4: Propagation 속성과 중첩 트랜잭션

### Spring의 Propagation 속성

Spring은 7가지 전파 속성을 제공하지만, **실무에서는 2가지만 쓰여요:**

| 속성 | 설명 | 사용도 |
|------|------|--------|
| REQUIRED | 트랜잭션 있으면 사용, 없으면 생성 (기본값) | ⭐⭐⭐⭐⭐ |
| REQUIRES_NEW | 항상 새로운 트랜잭션 생성 | ⭐⭐⭐ |
| SUPPORTS | 있으면 사용, 없으면 상관없음 | ⭐ |
| NOT_SUPPORTED | 트랜잭션 미사용 | ⭐ |
| MANDATORY | 트랜잭션 필수 (없으면 예외) | ⭐ |
| NEVER | 트랜잭션 금지 (있으면 예외) | ⭐ |
| NESTED | 중첩 트랜잭션 (savepoint 사용) | ⭐ |

### 코드 예제

```java
@Service
public class PaymentService {
    @Autowired
    private OrderService orderService;
    
    @Transactional
    public void processPayment(Payment payment) {
        paymentRepository.save(payment);
        orderService.updateOrder(payment);
    }
}

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOrder(Payment payment) {
        orderRepository.update(payment);
        throw new RuntimeException("에러 발생!");
    }
}
```

**결과:**
- OrderService의 updateOrder가 **새로운 트랜잭션**에서 실행됨
- updateOrder의 에러로 OrderService의 변경사항은 **롤백됨**
- PaymentService의 saveUser는 **커밋됨** (독립적인 트랜잭션)

---

## 실무 적용: 올바른 @Transactional 사용법

### 1단계: 기본값으로 설계하기

```java
// 대부분의 경우 이렇게 사용하면 됨
@Service
public class UserService {
    
    @Transactional  // 기본값 REQUIRED, rollbackFor는 보통 안 씀
    public void createUser(User user) {
        userRepository.save(user);
        // 관련 로직들...
    }
}
```

**왜 기본값이 좋을까?**
- 가장 직관적이고 명확해요
- 대부분의 비즈니스 로직에 맞아요
- 복잡한 설정을 피할 수 있어요

### 2단계: Exception 처리 명시하기

Checked Exception을 처리할 때는 **rollbackFor를 명시하세요:**

```java
@Transactional(rollbackFor = Exception.class)
public void processPayment(Payment payment) throws IOException {
    paymentRepository.save(payment);
    emailService.send(payment);  // IOException 발생 시 롤백
}
```

### 3단계: 독립적인 트랜잭션이 필요할 때만 REQUIRES_NEW

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveAuditLog(String action) {
    // 메인 트랜잭션과 별개로 실행
    // 메인 트랜잭션이 롤백되어도 로그는 남음
    auditLogRepository.save(new AuditLog(action));
}
```

### 4단계: Self-invocation은 외부 호출로 처리

```java
@Service
public class OrderService {
    @Autowired
    private PaymentService paymentService;
    
    public void createOrder(Order order) {
        // 외부 서비스 호출 - 프록시를 거침
        paymentService.processPayment(order.getPayment());
        orderRepository.save(order);
    }
}
```

---

## 체크리스트: 당신의 @Transactional 사용은 안전한가?

다음 항목들을 확인해보세요:

- [ ] 같은 클래스 내에서 @Transactional 메서드를 호출하지 않고 있나?
- [ ] Checked Exception이 발생할 수 있으면 `rollbackFor = Exception.class`를 썼나?
- [ ] try-catch가 많아서 비즈니스 로직이 묻혀있진 않나?
- [ ] REQUIRES_NEW를 정말 필요한 경우에만 쓰고 있나?
- [ ] Propagation 속성을 쓸 때 의도를 명확히 했나?

---

## 핵심 정리

1. **@Transactional은 프록시 기반** - 외부 호출일 때만 작동
2. **Exception 타입 주의** - Checked Exception은 기본적으로 롤백 안 됨
3. **명시적인 설정이 최고** - try-catch보다 `rollbackFor` 속성 사용
4. **기본값이 정답** - REQUIRED와 기본 설정만으로도 대부분 충분
5. **복잡함은 피하기** - 모든 Propagation 속성을 다 쓸 필요는 없음

---

## 마치며

@Transactional은 강력한 도구이지만, **올바르게 이해하지 못하면 잘못된 사용**으로 인한 버그가 발생할 수 있어요.

특히:
- 자동 롤백이 안 되는 문제
- Self-invocation으로 인한 트랜잭션 누락
- 복잡한 Exception 처리

이런 문제들은 모두 **@Transactional의 동작 원리를 제대로 알면 쉽게 피할 수 있습니다.**

실무에서는 **기본값을 신뢰하고, 필요할 때만 속성을 추가하는 방식**이 가장 안전합니다.

---

## 당신의 경험은 어떤가요?

`@Transactional`을 사용하면서 예상치 못한 문제를 경험한 적이 있으신가요? 또는 당신만의 트랜잭션 관리 팁이 있으신가요? 

댓글로 경험을 공유해주세요!