---
title: "[Java] 상속 vs 합성: 실무에서 어떻게 선택할까?"

tagline: "객체지향 설계에서 상속과 합성을 언제 사용해야 하는지, 실무 경험을 바탕으로 알아봅니다"

header:
  overlay_image: /assets/post/java/2026-02-11-inheritance-vs-composition/overlay.png
  overlay_filter: 0.5

categories:
  - Java

tags:
  - Java
  - OOP
  - 상속
  - 합성
  - 객체지향
  - 설계패턴

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-02-11T15:30:00+09:00
---

"상속은 나쁘다", "합성이 항상 우월하다"는 말을 자주 들어보셨나요? 하지만 실무에서는 이런 이분법적 사고가 오히려 비효율적일 수 있습니다. 이번 글에서는 **상속(Inheritance)**과 **합성(Composition)**을 실무에서 어떻게 선택하고 활용하는지, 실제 경험을 바탕으로 정리해보겠습니다.

## 핵심 질문: 상속은 정말 나쁜 습관일까?

객체지향 설계에서 코드 재사용을 위해 상속을 쓰는 것이 나쁜 습관일까요? 합성이 항상 우월할까요? 

**결론부터 말하자면: "상황에 따라 다르다"입니다.** 

상속과 합성은 각각 적합한 장소에서 사용해야 하는 코드 설계 원칙입니다. 이 둘을 적절히 활용하는 것이 실무에서 더 중요합니다.

## 상속을 사용하는 실무 시나리오

### 1. 언제 상속을 고려하는가?

실무에서는 **처음부터 상속을 설계하지 않습니다**. 대신 다음과 같은 접근을 합니다:

1. ✅ 먼저 코드를 작성한다
2. ✅ 중복 패턴이 발견되면 그때 판단한다
3. ✅ 상속으로 올릴지 말지 결정한다

이런 "선 개발, 후 리팩토링" 방식이 실무적으로 더 합리적입니다.

### 2. DTO에서의 상속 활용

상속이 가장 효과적으로 사용되는 곳은 바로 **DTO(Data Transfer Object)**입니다.

#### 공통 필드 추출 예제

```java
// ✅ 상속으로 공통 필드 관리
public class BaseDto {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    
    // 공통 메소드
    public void setAuditInfo(String userId) {
        this.createdBy = userId;
        this.createdAt = LocalDateTime.now();
    }
}

// 로그인 요청 DTO
public class LoginRequestDto extends BaseDto {
    private String username;
    private String password;
}

// 페이징 요청 DTO
public class PageRequestDto extends BaseDto {
    private int page;
    private int size;
    private String sort;
}
```

**왜 DTO에서 상속이 유용한가?**

- **데이터 구조가 명확**하게 보입니다
- 공통 필드를 **한 곳에서 관리**할 수 있습니다
- 코드 중복을 효과적으로 줄입니다

### 3. 상속 사용 시 제약 사항

실무에서는 다음과 같은 규칙을 지키는 것이 좋습니다:

⚠️ **최대 1레벨 상속만 허용**
```java
// ❌ 다단계 상속은 복잡도를 증가시킴
BaseDto → CommonDto → UserDto → AdminUserDto (X)

// ✅ 1레벨 상속으로 단순하게 유지
BaseDto → UserDto (O)
```

⚠️ **불필요한 필드나 메소드를 상속받지 않도록 설계**
- 자식 클래스가 부모의 일부만 필요하다면, 그건 잘못된 상속입니다
- 이런 경우 상속을 올리지 말아야 합니다

## 합성(Composition)이 필수인 경우

### 1. Spring Boot의 기본 패턴

Spring Boot를 사용하면 **대부분 합성을 사용**하게 됩니다. 왜냐하면 **의존성 주입(DI) 자체가 합성 패턴**이기 때문입니다.

<div class="mermaid mermaid-center">
graph TB
    A[Controller] -->|의존성 주입| B[UserService]
    A -->|의존성 주입| C[OrderService]
    A -->|의존성 주입| D[PaymentService]
    B -->|의존성 주입| E[UserRepository]
    C -->|의존성 주입| F[OrderRepository]
    
    style A fill:#2d3748,stroke:#4299e1,stroke-width:2px,color:#e2e8f0
    style B fill:#2d3748,stroke:#48bb78,stroke-width:2px,color:#e2e8f0
    style C fill:#2d3748,stroke:#48bb78,stroke-width:2px,color:#e2e8f0
    style D fill:#2d3748,stroke:#48bb78,stroke-width:2px,color:#e2e8f0
    style E fill:#2d3748,stroke:#ed8936,stroke-width:2px,color:#e2e8f0
    style F fill:#2d3748,stroke:#ed8936,stroke-width:2px,color:#e2e8f0
</div>

### 2. 왜 Service/Repository는 합성을 사용할까?

**Java의 단일 상속 제약** 때문입니다. Controller에서 여러 Service를 사용해야 하는데, 상속으로는 1개만 가능합니다.

```java
// ❌ 상속으로는 불가능 (자바는 다중 상속 불가)
public class OrderController extends UserService, OrderService, PaymentService {
    // 컴파일 에러!
}

// ✅ 합성으로 여러 의존성 조합
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    
    @PostMapping("/orders")
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        // 여러 Service를 자유롭게 조합하여 사용
        User user = userService.findById(request.getUserId());
        Order order = orderService.create(request);
        paymentService.process(order);
        return new OrderResponse(order);
    }
}
```

### 3. Spring의 생성자 주입 패턴

```java
@Component
public class MovieRecommender {
    private final CustomerPreferenceDao customerPreferenceDao;

    @Autowired  // Spring 4.3부터는 단일 생성자인 경우 생략 가능
    public MovieRecommender(CustomerPreferenceDao customerPreferenceDao) {
        this.customerPreferenceDao = customerPreferenceDao;
    }
}
```

이것이 바로 **합성(Composition) 패턴**입니다. 객체를 상속받는 것이 아니라, **필요한 객체를 멤버 변수로 포함**하는 방식입니다.

## DTO에서 합성 vs 상속

### 1. DTO에서 합성이 복잡해지는 이유

DTO에서 외부 객체와 합성을 하면 의존성이 "엉킨 실처럼 복잡해집니다":

```java
// ❌ DTO가 외부 객체에 의존 (비추천)
public class UserResponse {
    private String username;
    private OrderService orderService;  // DTO가 Service를 의존?
    private ExternalOrderInfo orderInfo;  // 외부 클래스 의존
    
    // 복잡도 증가, 테스트 어려움, DTO의 역할 모호
}
```

### 2. Inner Class로 응집도 유지

대신 **Inner Class**를 활용하여 DTO 내부에서 처리하는 것이 좋습니다:

```java
// ✅ Inner class로 응집도 유지
@Getter
public class UserResponse {
    private String username;
    private String email;
    private List<OrderInfo> orders;
    
    @Getter
    public static class OrderInfo {
        private String orderId;
        private BigDecimal amount;
        private LocalDateTime orderDate;
        
        public OrderInfo(Order order) {
            this.orderId = order.getId();
            this.amount = order.getAmount();
            this.orderDate = order.getCreatedAt();
        }
    }
    
    public UserResponse(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.orders = user.getOrders().stream()
            .map(OrderInfo::new)
            .collect(Collectors.toList());
    }
}
```

**장점:**
- DTO 내부에서 모든 것이 해결됩니다
- 외부 의존성이 없어 테스트가 쉽습니다
- 데이터 구조가 명확하게 보입니다

<div class="mermaid mermaid-center">
graph LR
    A[UserResponse] --> B[OrderInfo Inner Class]
    A --> C[AddressInfo Inner Class]
    
    style A fill:#2d3748,stroke:#4299e1,stroke-width:3px,color:#e2e8f0
    style B fill:#1a202c,stroke:#48bb78,stroke-width:2px,color:#e2e8f0
    style C fill:#1a202c,stroke:#48bb78,stroke-width:2px,color:#e2e8f0
</div>

## 실무 판단 기준 정리

### 상속을 사용하는 경우

| 상황 | 판단 기준 | 예시 |
|------|-----------|------|
| DTO 공통 필드 | ✅ 여러 DTO에서 동일한 필드가 반복될 때 | BaseDto (createdAt, updatedAt) |
| 공통 메소드 | ✅ 공통 필드를 사용하는 메소드가 있을 때 | 페이징 정보, 감사(Audit) 정보 |
| 상속 깊이 | ✅ 최대 1레벨까지만 | BaseDto → UserDto (O) |

### 합성을 사용하는 경우

| 상황 | 판단 기준 | 예시 |
|------|-----------|------|
| 여러 기능 조합 | ✅ 여러 객체를 함께 사용해야 할 때 | Controller → 여러 Service |
| 의존성 주입 | ✅ Spring DI 패턴 | @Autowired, 생성자 주입 |
| 동적 변경 | ✅ 런타임에 구현체를 바꿔야 할 때 | 인터페이스 기반 설계 |

## Java 공식 문서의 상속과 합성

### 상속의 장단점

**장점:**
- ✅ 코드 재사용성 향상
- ✅ 다형성(Polymorphism) 지원
- ✅ 추상화를 통한 유지보수성 향상

**단점:**
- ⚠️ 상속 계층이 깊어지면 복잡도 증가
- ⚠️ 강한 결합도 (부모-자식 간 강한 의존성)

### Java의 상속 제약

Java는 **단일 상속만 지원**합니다:

```java
// ❌ 다중 상속 불가
public class AdminUser extends User, Employee {
    // 컴파일 에러!
}

// ✅ 인터페이스를 통한 다중 구현
public class AdminUser extends User implements Employee, Auditable {
    // 이것은 가능 (클래스 1개 + 인터페이스 여러 개)
}
```

## 실전 코드 비교

### 시나리오: 사용자 관리 시스템

#### 잘못된 상속 사용

```java
// ❌ 상속 남용
public class BaseService {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected String serviceName;
    
    public void logInfo(String message) {
        logger.info("[{}] {}", serviceName, message);
    }
}

public class UserService extends BaseService {
    private UserRepository userRepository;
    
    public User findById(Long id) {
        logInfo("Finding user: " + id);
        return userRepository.findById(id).orElseThrow();
    }
}

public class OrderService extends BaseService {
    private OrderRepository orderRepository;
    
    public Order create(OrderRequest request) {
        logInfo("Creating order");
        return orderRepository.save(new Order(request));
    }
}
```

**문제점:**
- 로깅 기능 때문에 모든 Service가 상속받아야 함
- 나중에 다른 부모 클래스가 필요하면? (단일 상속 제약)
- Service 간의 불필요한 결합도 증가

#### 올바른 합성 사용

```java
// ✅ 합성으로 개선
@Component
public class ServiceLogger {
    private final Logger logger = LoggerFactory.getLogger(ServiceLogger.class);
    
    public void logInfo(String serviceName, String message) {
        logger.info("[{}] {}", serviceName, message);
    }
}

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ServiceLogger serviceLogger;
    
    public User findById(Long id) {
        serviceLogger.logInfo("UserService", "Finding user: " + id);
        return userRepository.findById(id).orElseThrow();
    }
}

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ServiceLogger serviceLogger;
    
    public Order create(OrderRequest request) {
        serviceLogger.logInfo("OrderService", "Creating order");
        return orderRepository.save(new Order(request));
    }
}
```

**장점:**
- 각 Service가 독립적입니다
- ServiceLogger를 쉽게 교체 가능 (테스트 시 Mock)
- 다른 기능 추가 시 유연합니다

<div class="mermaid mermaid-center">
graph TB
    subgraph "합성 패턴"
        US[UserService]
        OS[OrderService]
        SL[ServiceLogger]
        
        US -.->|의존| SL
        OS -.->|의존| SL
    end
    
    subgraph "상속 패턴"
        BS[BaseService]
        US2[UserService]
        OS2[OrderService]
        
        BS -->|상속| US2
        BS -->|상속| OS2
    end
    
    style US fill:#2d3748,stroke:#48bb78,stroke-width:2px,color:#e2e8f0
    style OS fill:#2d3748,stroke:#48bb78,stroke-width:2px,color:#e2e8f0
    style SL fill:#2d3748,stroke:#4299e1,stroke-width:2px,color:#e2e8f0
    style BS fill:#2d3748,stroke:#f56565,stroke-width:2px,color:#e2e8f0
    style US2 fill:#1a202c,stroke:#718096,stroke-width:2px,color:#e2e8f0
    style OS2 fill:#1a202c,stroke:#718096,stroke-width:2px,color:#e2e8f0
</div>

## 결론: 적재적소에 적합한 도구를 사용하라

### 핵심 정리

1. **"상속은 나쁘다"는 교조적 사고를 버리세요**
   - 상속과 합성은 각각의 적합한 용도가 있습니다

2. **DTO에서는 상속이 효과적입니다**
   - 공통 필드/메소드 관리에 유용
   - 단, 1레벨 상속으로 제한하세요
   - Inner class로 응집도를 유지하세요

3. **비즈니스 로직에서는 합성을 선호하세요**
   - Spring의 DI는 합성 패턴입니다
   - 여러 기능을 조합할 수 있습니다
   - 테스트가 쉽고 유연합니다

4. **선 개발, 후 리팩토링**
   - 처음부터 완벽한 설계는 어렵습니다
   - 중복이 발견되면 그때 판단하세요

### 실무 체크리스트

다음 질문으로 판단하세요:

- [ ] 여러 객체를 조합해야 하나요? → **합성**
- [ ] 단순히 공통 데이터를 공유하나요? → **상속 고려**
- [ ] 나중에 다른 부모가 필요할 수 있나요? → **합성**
- [ ] DTO의 공통 필드인가요? → **상속**
- [ ] 테스트를 쉽게 만들고 싶나요? → **합성**

---

이 글이 도움이 되셨나요? 여러분은 실무에서 상속과 합성을 어떻게 구분하여 사용하시나요? 댓글로 경험을 공유해주세요! 💬
