---
title: "[Spring Boot] 컨트롤러 API 데이터 수신: 계층별 책임 분리 전략"

tagline: "REST API에서 요청 데이터를 안전하고 효율적으로 받는 계층 설계"

header:
  overlay_image: /assets/post/spring/2026-01-04-controller-data-handling/overlay.png
  overlay_filter: 0.5

categories:
  - Spring

tags:
  - Spring Boot
  - REST API
  - 데이터 검증
  - 컨트롤러 설계
  - 계층 분리
  - 보안

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-04T12:00:00
---

Spring Boot로 REST API를 개발하다 보면 반복되는 고민이 생깁니다.

**"컨트롤러에서 들어오는 데이터를 어떻게 처리할 것인가?"**

흔한 실수들을 보면:
- DTO에서 검증하지 않고 Service에서 `if (data == null)` 처리
- 권한 검증을 Service에서 담당 → Service가 보안 로직으로 오염
- 컨트롤러에서 모든 검증을 함 → 계층 책임이 불명확함

이 글은 실무에서 정립한 **"계층별 책임 분리" 기반의 API 데이터 수신 전략**을 공유합니다. 안전하면서도 효율적인 구조를 만드는 방법입니다.

---

## 문제 상황: 왜 이게 필요한가?

### 일반적인 개발 흐름의 문제점

많은 개발자들이 API 데이터 처리를 할 때 다음과 같은 구조를 사용합니다:

```java
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
    // 컨트롤러: 모든 검증을 여기서?
    if (request.getName() == null) {
        return ResponseEntity.badRequest().body(...);
    }
    if (request.getEmail() == null) {
        return ResponseEntity.badRequest().body(...);
    }
    
    // 또는 Service에서:
    userService.createUser(request);
}

// Service
public void createUser(UserRequest request) {
    // Service에서도 검증?
    if (request.getName() == null) {
        throw new IllegalArgumentException("이름은 필수입니다");
    }
    // 실제 비즈니스 로직
}
```

**문제점:**
- 검증 로직이 여러 곳에 산재됨
- Service가 데이터 검증으로 오염됨
- 각 계층의 책임이 불명확함
- 같은 검증을 반복 작성

### 더 심각한 문제: 보안 관련

```java
@PutMapping("/users/{userId}/password")
public ResponseEntity<Void> changePassword(
    @PathVariable Long userId,
    @RequestBody PasswordChangeRequest request) {
    
    // 누가 권한을 확인하나?
    userService.changePassword(userId, request);
}
```

이 API는 누구나 호출할 수 있습니다. 다른 사람의 userId를 대입하면?
- Service에서 권한 검증? → Service가 보안 로직으로 오염
- Filter에서 검증? → 구조는 맞지만 어떻게 구현할 것인가?

---

## 해결책: 계층별 책임 분리 전략

이 문제들의 해결책은 **각 계층이 자신의 책임만 충실히 하도록 분리**하는 것입니다.

### 전체 Flow 구조

<div class="mermaid mermaid-center">
flowchart TD
    Start([🚀 API 요청]) --> Filter
    
    Filter["1️⃣ Filter/Interceptor ━━━━━━━━━━━━━━━━ 권한/소유권 검증 (보안 계층)"]
    Filter -->|✅ PASS| DTO
    Filter -->|❌ FAIL| Forbidden["403 Forbidden 권한 없음"]
    
    DTO["2️⃣ DTO + @Valid ━━━━━━━━━━━━━━━━ 기술적 검증 (null, 타입, 포맷)"]
    DTO -->|✅ PASS| Controller
    DTO -->|❌ FAIL| BadRequest["400 Bad Request MethodArgumentNotValidException @ControllerAdvice 처리"]
    
    Controller["3️⃣ Controller ━━━━━━━━━━━━━━━━ 데이터 상태 판별 (여러 필드 간 관계)"]
    Controller -->|✅ PASS| Service
    Controller -->|❌ FAIL| Unprocessable["422 Unprocessable Entity 명확한 오류 메시지"]
    
    Service["4️⃣ Service ━━━━━━━━━━━━━━━━ 순수 비즈니스 로직 방어 코드 불필요"]
    Service -->|✅ SUCCESS| Success["✅ 성공 응답"]
    Service -->|❌ EXCEPTION| BusinessError["비즈니스 오류 (중복, 재고 부족 등)"]
    
    style Start fill:#fff9c4,stroke:#fbc02d,stroke-width:2px,color:#000
    style Filter fill:#e1f5ff,stroke:#0277bd,stroke-width:3px,color:#000
    style DTO fill:#fff4e1,stroke:#f57f17,stroke-width:3px,color:#000
    style Controller fill:#ffe1f5,stroke:#c2185b,stroke-width:3px,color:#000
    style Service fill:#e1ffe1,stroke:#388e3c,stroke-width:3px,color:#000
    
    style Success fill:#ccffcc,stroke:#2e7d32,stroke-width:2px,color:#000
    style Forbidden fill:#ffcccc,stroke:#d32f2f,stroke-width:2px,color:#000
    style BadRequest fill:#ffcccc,stroke:#d32f2f,stroke-width:2px,color:#000
    style Unprocessable fill:#ffcccc,stroke:#d32f2f,stroke-width:2px,color:#000
    style BusinessError fill:#ffcccc,stroke:#d32f2f,stroke-width:2px,color:#000
</div>

### 각 계층의 역할 정의

| 계층 | 책임 | 실패 시 응답 |
|------|------|------------|
| **Filter** | 권한/소유권 검증 | 403 Forbidden |
| **DTO @Valid** | 기술적 검증 (null, 타입, 포맷) | 400 Bad Request |
| **Controller** | 데이터 상태 판별 | 400/422 Unprocessable Entity |
| **Service** | 순수 비즈니스 로직 | 비즈니스 예외 |

---

## 계층별 상세 구현

### 1단계: Filter에서 권한 검증

```java
@Component
public class AuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response,
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // PUT /users/{userId}/password 같은 요청 처리
        if (isProtectedResource(requestURI, method)) {
            Long requestedUserId = extractUserIdFromPath(requestURI);
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = getCurrentUserId(auth);
            boolean isAdmin = hasAdminRole(auth);
            
            // 권한 검증: 본인이거나 관리자만 허용
            if (!isAdmin && !requestedUserId.equals(currentUserId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                        "status": 403,
                        "message": "권한이 없습니다",
                        "timestamp": "%s"
                    }
                    """.formatted(LocalDateTime.now()));
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isProtectedResource(String uri, String method) {
        // /users/{userId}/password PUT 요청 보호
        return uri.matches("/users/\\d+/password") && "PUT".equals(method);
    }
    
    private Long extractUserIdFromPath(String uri) {
        Pattern pattern = Pattern.compile("/users/(\\d+)");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }
}
```

**특징:**
- ✅ 컨트롤러 진입 전에 차단
- ✅ Service는 권한 검증 없이 순수 로직만
- ✅ 401/403 상태 코드로 명확한 응답

### 2단계: DTO에서 기술적 검증

```java
public class PasswordChangeRequest {
    
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    private String currentPassword;
    
    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Length(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다")
    private String newPassword;
    
    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String newPasswordConfirm;
    
    // Getter, Setter
}

public class UserCreateRequest {
    
    @NotNull(message = "이름은 필수입니다")
    @NotBlank(message = "이름은 공백일 수 없습니다")
    private String name;
    
    @NotNull(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;
    
    @NotNull(message = "나이는 필수입니다")
    @Min(value = 18, message = "18세 이상만 가입 가능합니다")
    @Max(value = 120, message = "유효한 나이가 아닙니다")
    private Integer age;
}
```

**특징:**
- ✅ 기술적 검증만 담당 (null, 타입, 포맷)
- ✅ 명확한 오류 메시지
- ✅ 재사용 가능

### 3단계: @ControllerAdvice로 검증 실패 처리

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("요청 데이터 검증 실패")
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
}

@Getter
@Builder
public class ErrorResponse {
    private int status;
    private String message;
    private Map<String, String> errors;
    private LocalDateTime timestamp;
}
```

**응답 예시:**
```json
{
    "status": 400,
    "message": "요청 데이터 검증 실패",
    "errors": {
        "email": "유효한 이메일 형식이 아닙니다",
        "age": "18세 이상만 가입 가능합니다"
    },
    "timestamp": "2026-01-04T10:30:00"
}
```

### 4단계: Controller에서 여러 필드 간 관계 검증

```java
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        
        // 이미 DTO 검증을 통과한 데이터
        // 여기서는 비즈니스 포맷만 검증
        
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        
        // 1. Filter에서 권한 검증 완료 (본인 또는 관리자)
        // 2. DTO @Valid로 기술적 검증 완료
        // 3. Service에서 모든 비즈니스 검증 처리
        
        userService.changePassword(userId, request);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // GET은 @RequestParam으로 선택적 조건 처리
        Page<UserResponse> users = userService.searchUsers(name, email, page, size);
        return ResponseEntity.ok(users);
    }
}

// 비즈니스 검증 예외
public class InvalidPasswordConfirmException extends RuntimeException {
    public InvalidPasswordConfirmException(String message) {
        super(message);
    }
}
```

### 5단계: Service는 순수 비즈니스 로직만

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreateRequest request) {
        // 컨트롤러에서 이미 검증됨:
        // - name, email, age가 null이 아님 (DTO)
        // - age >= 18 (DTO)
        // - 권한이 있음 (Filter)
        
        // 순수 비즈니스 로직만 구현
        // 중복 이메일 체크는 비즈니스 규칙
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 가입된 이메일입니다");
        }
        
        // Factory Method를 사용한 엔티티 생성
        User user = User.of(request, passwordEncoder);
        
        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }
}

// User 엔티티에 추가
public class User {
    // ... 기타 필드 및 메서드
    
    // Factory Method: DTO에서 엔티티로 변환
    public static User of(UserCreateRequest request, PasswordEncoder passwordEncoder) {
        return User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .age(request.getAge())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
    }

    public void changePassword(Long userId, PasswordChangeRequest request) {
        // 컨트롤러에서 이미 검증됨:
        // - 비밀번호와 확인이 모두 필수입니다 (DTO @NotBlank)
        // - 길이 범위가 맞습니다 (DTO @Length)
        // - 권한이 있습니다 (Filter)
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
        
        // 비즈니스 검증 1: 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException("현재 비밀번호가 일치하지 않습니다");
        }
        
        // 비즈니스 검증 2: 새 비밀번호와 확인 비밀번호 일치
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new InvalidPasswordConfirmException("새 비밀번호가 일치하지 않습니다");
        }
        
        // 비즈니스 검증 3: 기존 비밀번호와 새 비밀번호가 다른지 확인
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new SamePasswordException("새 비밀번호는 기존 비밀번호와 달라야 합니다");
        }
        
        // 비밀번호 변경
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
```

**특징:**
- ✅ 방어 코드 없음 (null 체크 불필요)
- ✅ 순수 비즈니스 로직만 구현
- ✅ 코드가 깔끔하고 읽기 쉬움

---

## 실무 시나리오별 적용 예제

### 시나리오 1: 주문 생성 (중첩 객체 + 여러 필드 관계)

```java
// DTO 정의
public class CreateOrderRequest {
    
    @NotNull(message = "주문일자는 필수입니다")
    @PastOrPresent(message = "미래 날짜는 허용되지 않습니다")
    private LocalDate orderDate;
    
    @NotNull(message = "배송 시작일은 필수입니다")
    private LocalDate shippingStartDate;
    
    @NotNull(message = "배송 예정일은 필수입니다")
    private LocalDate shippingEndDate;
    
    @NotEmpty(message = "상품 목록은 필수입니다")
    private List<@Valid OrderItemRequest> items;
    
    // Getter, Setter
}

public class OrderItemRequest {
    
    @NotNull(message = "상품 ID는 필수입니다")
    @Positive(message = "상품 ID는 양수여야 합니다")
    private Long productId;
    
    @NotNull(message = "수량은 필수입니다")
    @Positive(message = "수량은 1개 이상이어야 합니다")
    private Integer quantity;
}

// Controller: 여러 필드 간 관계 검증
@PostMapping("/orders")
public ResponseEntity<OrderResponse> createOrder(
        @Valid @RequestBody CreateOrderRequest request) {
    
    // 비즈니스 포맷 검증
    if (request.getShippingStartDate().isAfter(request.getShippingEndDate())) {
        throw new InvalidDateRangeException("배송 시작일이 배송 예정일보다 뒤에 있을 수 없습니다");
    }
    
    if (request.getShippingStartDate().isBefore(request.getOrderDate())) {
        throw new InvalidDateException("배송 시작일이 주문일보다 빠를 수 없습니다");
    }
    
    OrderResponse response = orderService.createOrder(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

// Service: 순수 비즈니스 로직
public OrderResponse createOrder(CreateOrderRequest request) {
    // 모든 검증이 완료된 상태
    
    // 재고 확인 (비즈니스 규칙)
    for (OrderItemRequest item : request.getItems()) {
        if (!productService.hasStock(item.getProductId(), item.getQuantity())) {
            throw new InsufficientStockException(
                "상품 ID " + item.getProductId() + "의 재고가 부족합니다"
            );
        }
    }
    
    // Factory Method를 사용한 엔티티 생성
    Order order = Order.of(request);
    
    Order savedOrder = orderRepository.save(order);
    return OrderResponse.from(savedOrder);
}

// Order 엔티티에 추가
public class Order {
    // ... 기타 필드 및 메서드
    
    // Factory Method: DTO에서 엔티티로 변환
    public static Order of(CreateOrderRequest request) {
        return Order.builder()
            .orderDate(request.getOrderDate())
            .shippingStartDate(request.getShippingStartDate())
            .shippingEndDate(request.getShippingEndDate())
            .items(request.getItems().stream()
                .map(OrderItem::of)
                .collect(Collectors.toList()))
            .build();
    }
}

// OrderItem 엔티티에 추가
public class OrderItem {
    // ... 기타 필드 및 메서드
    
    // Factory Method: DTO에서 엔티티로 변환
    public static OrderItem of(OrderItemRequest request) {
        return OrderItem.builder()
            .productId(request.getProductId())
            .quantity(request.getQuantity())
            .build();
    }
}
```

### 시나리오 2: 권한 검증이 필요한 리소스 수정

```java
// Filter: 권한 검증
@Component
public class ResourceOwnershipFilter extends OncePerRequestFilter {
    
    private static final Pattern UPDATE_USER_PATTERN = Pattern.compile("/users/(\\d+)");
    private static final Pattern UPDATE_ORDER_PATTERN = Pattern.compile("/orders/(\\d+)");
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {
        
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        // PUT/DELETE 요청 검증
        if (isModifyRequest(method)) {
            if (isUserResource(uri)) {
                validateUserOwnership(request, response, uri);
            } else if (isOrderResource(uri)) {
                validateOrderOwnership(request, response, uri);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void validateUserOwnership(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String uri) throws IOException {
        Long resourceUserId = extractId(uri, UPDATE_USER_PATTERN);
        Long currentUserId = getCurrentUserId();
        
        if (!isAuthorized(currentUserId, resourceUserId)) {
            sendForbiddenResponse(response);
            return;
        }
    }
    
    private void validateOrderOwnership(HttpServletRequest request,
                                       HttpServletResponse response,
                                       String uri) throws IOException {
        Long orderId = extractId(uri, UPDATE_ORDER_PATTERN);
        Long currentUserId = getCurrentUserId();
        
        // 주문의 소유자 확인 (DB 조회)
        if (!orderService.isOrderOwner(orderId, currentUserId)) {
            sendForbiddenResponse(response);
            return;
        }
    }
    
    private void sendForbiddenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("""
            {
                "status": 403,
                "message": "이 리소스에 대한 권한이 없습니다"
            }
            """);
    }
}

// Controller: 권한은 이미 확인됨
@PutMapping("/orders/{orderId}")
public ResponseEntity<OrderResponse> updateOrder(
        @PathVariable Long orderId,
        @Valid @RequestBody UpdateOrderRequest request) {
    
    // Filter에서 권한 확인 완료
    // DTO에서 기술적 검증 완료
    // 여기서는 데이터 상태만 검증
    
    if (request.getShippingDate().isBefore(request.getOrderDate())) {
        throw new InvalidDateException("배송일은 주문일 이후여야 합니다");
    }
    
    OrderResponse response = orderService.updateOrder(orderId, request);
    return ResponseEntity.ok(response);
}
```

---

## 체크리스트: 올바른 계층 분리

API 개발 시 다음을 확인하세요:

### Filter/Interceptor 체크리스트
- ✅ 권한/소유권 검증을 수행하는가?
- ✅ 검증 실패 시 403 Forbidden을 반환하는가?
- ✅ 비즈니스 로직은 포함되지 않았는가?
- ✅ 필요한 경우만 DB 조회를 수행하는가?

### DTO 체크리스트
- ✅ @NotNull, @NotBlank, @Email 등의 기술적 검증만 포함되어 있는가?
- ✅ 명확한 오류 메시지를 제공하는가?
- ✅ 다른 필드와의 관계 검증은 포함하지 않았는가? (예: start < end)
- ✅ 비즈니스 규칙은 포함하지 않았는가? (예: 중복 확인)

### Controller 체크리스트
- ✅ 여러 필드 간 관계 검증을 수행하는가? (데이터 상태 판별)
- ✅ 명확한 오류 메시지와 함께 예외를 발생시키는가?
- ✅ Service 호출 전에 모든 기술적 검증이 완료되었는가?
- ✅ 비즈니스 로직은 Service에 위임했는가?

### Service 체크리스트
- ✅ 들어오는 데이터가 유효하다고 가정하는가?
- ✅ 방어 코드(null 체크 등)를 최소화했는가?
- ✅ 순수 비즈니스 로직만 구현했는가?
- ✅ DB 조회가 필요한 검증을 수행하는가? (예: 중복 이메일)

---

## 실무 팁

### 1. GET 요청은 다르게 처리

```java
// GET은 필터링 목적이므로 @RequestParam 사용
@GetMapping("/orders")
public ResponseEntity<Page<OrderResponse>> searchOrders(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    
    // startDate와 endDate의 관계 검증
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
        throw new InvalidDateRangeException("시작일이 종료일보다 뒤에 있을 수 없습니다");
    }
    
    return ResponseEntity.ok(orderService.searchOrders(status, startDate, endDate, page, size));
}
```

### 2. 복잡한 권한 검증은 Annotation으로

```java
// 커스텀 Annotation 정의
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireResourceOwnership {
    String resourceIdParamName();
    String resourceType();
}

// AOP로 처리
@Component
@Aspect
public class ResourceOwnershipAspect {
    
    @Before("@annotation(requireResourceOwnership)")
    public void checkOwnership(JoinPoint joinPoint, 
                             RequireResourceOwnership requireResourceOwnership) {
        // 소유권 검증 로직
    }
}

// Controller에서 사용
@PutMapping("/orders/{orderId}")
@RequireResourceOwnership(resourceIdParamName = "orderId", resourceType = "ORDER")
public ResponseEntity<OrderResponse> updateOrder(
        @PathVariable Long orderId,
        @Valid @RequestBody UpdateOrderRequest request) {
    // 권한은 Aspect에서 처리됨
    return ResponseEntity.ok(orderService.updateOrder(orderId, request));
}
```

### 3. 검증 순서 최적화

```java
// 성능 최적화: 빠른 검증부터
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody UserCreateRequest request) {
    
    // 1. 빠른 검증 (메모리 기반)
    if (request.getName().length() > 100) {
        throw new ValidationException("이름이 너무 깁니다");
    }
    
    // 2. DB 조회가 필요한 검증 (느린 검증)
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new DuplicateEmailException("이미 가입된 이메일입니다");
    }
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.createUser(request));
}
```

---

## 결론

**Spring Boot에서 API 데이터를 안전하고 효율적으로 받는 방법:**

<div class="mermaid mermaid-center">
flowchart TD
    A[Filter ━━━━━ 권한/소유권 검증 403 Forbidden] 
    B[DTO @Valid ━━━━━ 기술적 검증 400 Bad Request]
    C[Controller ━━━━━ 데이터 상태 판별 422 Unprocessable]
    D[Service ━━━━━ 순수 비즈니스 로직]
    
    A -->|✅ PASS| B
    A -->|❌ FAIL| Fail1["❌ 차단"]
    B -->|✅ PASS| C
    B -->|❌ FAIL| Fail2["❌ 차단"]
    C -->|✅ PASS| D
    C -->|❌ FAIL| Fail3["❌ 차단"]
    D -->|✅ SUCCESS| Success["✅ 성공 응답"]
    D -->|❌ EXCEPTION| Fail4["❌ 비즈니스 오류"]
    
    style A fill:#e1f5ff,stroke:#0277bd,stroke-width:3px,color:#000
    style B fill:#fff4e1,stroke:#f57f17,stroke-width:3px,color:#000
    style C fill:#ffe1f5,stroke:#c2185b,stroke-width:3px,color:#000
    style D fill:#e1ffe1,stroke:#388e3c,stroke-width:3px,color:#000
    
    style Success fill:#ccffcc,stroke:#2e7d32,stroke-width:2px,color:#000
    style Fail1 fill:#ffcccc,stroke:#d32f2f,stroke-width:2px,color:#000
    style Fail2 fill:#ffcccc,stroke:#d32f2f,stroke-width:2px,color:#000
    style Fail3 fill:#ffcccc,stroke:#d32f2f,stroke-width:2px,color:#000
    style Fail4 fill:#ffcccc,stroke:#d32f2f,stroke-width:2px,color:#000
</div>

**이 구조의 장점:**

✅ **명확성**: 각 계층의 책임이 분명함
✅ **재사용성**: DTO를 다양한 곳에서 재사용 가능
✅ **테스트 용이**: 각 계층을 독립적으로 테스트 가능
✅ **유지보수**: 한 곳의 변경이 전체에 영향 최소화
✅ **보안**: 권한 검증이 일관성 있게 적용됨
✅ **코드 품질**: Service가 순수 로직으로 깔끔함

이 구조를 적용하면 **복잡한 API도 체계적으로 관리**할 수 있으며, 팀원들과도 명확하게 의사소통할 수 있습니다.

---

**댓글로 의견을 나눠주세요:**
- 현재 프로젝트에서 사용 중인 데이터 검증 구조는?
- 권한 검증을 어느 계층에서 처리하고 계신가요?
- 이 구조를 적용하면서 겪은 어려움이나 개선 사항이 있나요?

다른 개발자들의 경험이 모여 더 좋은 실무 가이드라인을 만들 수 있습니다.
