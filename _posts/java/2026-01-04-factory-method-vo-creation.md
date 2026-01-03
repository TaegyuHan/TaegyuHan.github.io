---
title: "[Java] 팩토리 메소드 패턴으로 Service 레이어 가독성 개선하기"

tagline: "DTO에서 VO로의 변환을 팩토리 메소드로 효율적으로 처리하는 방법"

header:
  overlay_image: /assets/post/java/2026-01-04-factory-method-vo-creation/overlay.png
  overlay_filter: 0.5

categories:
  - java

tags:
  - 팩토리메소드
  - 디자인패턴
  - VO변환
  - Service레이어
  - 코드가독성
  - 검증로직

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-04T01:42:00
---

비지니스 로직이 복잡해 지면서 Service 레이어의 코드가 복잡해지기 시작했습니다. 특히 **DTO를 VO로 변환할 때** 문제가 두드러졌습니다. 해당 문제는 로직의 가독성을 크게 떨어뜨렸습니다.

처음에는 다음과 같이 단순하게 진행했습니다:

```java
public class UserService {
    public void registerUser(UserDTO dto) {
        UserVO vo = new UserVO(
            dto.getEmail(),
            dto.getAge(),
            dto.getName(),
            dto.getPhoneNumber(),
            dto.getAddress(),
            LocalDateTime.now(),
            UserStatus.ACTIVE
        );
        // 비즈니스 로직...
    }
}
```

생성자로는 필요한 필드가 많아지면서 코드가 길어졌고, 파라미터 순서를 헷갈리기 쉬웠습니다. 그래서 빌더 패턴으로 전환했습니다.

```java
public void registerUser(UserDTO dto) {
    UserVO vo = UserVO.builder()
        .email(dto.getEmail())
        .age(dto.getAge())
        .name(dto.getName())
        .phoneNumber(dto.getPhoneNumber())
        .address(dto.getAddress())
        .joinDate(LocalDateTime.now())
        .status(UserStatus.ACTIVE)
        .build();
    
    // 비즈니스 로직이 어디 시작하는지 불명확...
    processUserRegistration(vo);
    sendWelcomeEmail(vo);
    createUserProfile(vo);
}
```

**문제가 발생했습니다:**

- ❌ VO 생성 코드가 7-8줄을 차지
- ❌ 실제 비즈니스 로직이 어디서 시작하는지 불명확
- ❌ 메소드를 읽을 때 집중력이 산만해짐
- ❌ 여러 팩토리 메소드를 만들 때 검증 로직이 중복됨

이 문제를 어떻게 해결할 수 있을까 고민하다가, **팩토리 메소드 패턴**을 적극적으로 적용하기로 결정했습니다.

---

## 문제: Service 메소드의 가독성 저하

Service 레이어에서는 Entity를 받아 비즈니스 로직을 처리한 후 VO로 변환하는 작업이 일어납니다. 문제는 **빌드 패턴이나 생성자를 직접 사용할 때** 발생합니다.

```java
public class UserService {
    public UserVO registerUser(UserDTO dto) {
        // 이메일 형식 검증
        String normalizedEmail = dto.getEmail().toLowerCase().trim();
        
        // 휴대폰 형식 정규화
        String formattedPhone = dto.getPhoneNumber()
            .replaceAll("[^0-9]", "");
        
        // 주소 필터링
        String address = StringUtils.isNotBlank(dto.getAddress()) 
            ? dto.getAddress() : "미등록";
        
        // VO 생성 (여기서 많은 라인 차지)
        UserVO userVO = UserVO.builder()
            .email(normalizedEmail)
            .age(dto.getAge())
            .name(dto.getName())
            .phoneNumber(formattedPhone)
            .address(address)
            .joinDate(LocalDateTime.now())
            .status(UserStatus.ACTIVE)
            .role(determineUserRole(dto.getAge()))
            .accountLevel(calculateAccountLevel(dto))
            .emailVerified(false)
            .notificationEnabled(true)
            .marketingConsent(dto.isMarketingConsent())
            .lastLoginDate(null)
            .failedLoginCount(0)
            .build();
        
        // 실제 비즈니스 로직이 어디서 시작하는지 불명확...
        validateUserUniqueness(userVO);
        sendVerificationEmail(userVO);
        saveUserAuditLog(userVO, "REGISTRATION");
        notifyAdminIfPremiumUser(userVO);
        
        return userVO;
    }
    
    private UserRole determineUserRole(int age) {
        return age >= 20 ? UserRole.ADULT : UserRole.MINOR;
    }
    
    private int calculateAccountLevel(UserDTO dto) {
        // 복잡한 로직...
        return dto.getReferralCode() != null ? 2 : 1;
    }
}
```

객체 생성에만 **15줄 이상이 할당**되고, 여러 데이터 정규화 로직이 섞여있어서 **실제 비즈니스 로직의 흐름을 따라가기 어렵습니다.** 코드를 읽는 사람이 "어디가 변환이고 어디가 실제 로직인가?" 헷갈리는 문제가 발생합니다. 이것이 우리가 해결하려는 문제입니다.

---

## 해결책: 팩토리 메소드 패턴

팩토리 메소드를 활용하면 객체 생성 로직을 **의미 있는 이름의 메소드**로 분리할 수 있습니다.

```java
public class UserVO {
    private String email;
    private int age;
    private String name;
    private String phoneNumber;
    private String address;
    private LocalDateTime joinDate;
    private UserStatus status;
    
    // 팩토리 메소드: 의미 있는 이름
    public static UserVO createFromUserDTO(UserDTO dto) {
        return UserVO.builder()
            .email(dto.getEmail())
            .age(dto.getAge())
            .name(dto.getName())
            .phoneNumber(dto.getPhoneNumber())
            .address(dto.getAddress())
            .joinDate(LocalDateTime.now())
            .status(UserStatus.ACTIVE)
            .build();
    }
    
    public static UserVO createFromAdminDTO(AdminDTO dto) {
        return UserVO.builder()
            .email(dto.getEmail())
            .age(dto.getAge())
            .name(dto.getName())
            .phoneNumber(dto.getPhoneNumber())
            .address(null)  // 관리자는 주소 미입력
            .joinDate(LocalDateTime.now())
            .status(UserStatus.ADMIN)
            .build();
    }
}
```

이제 Service 메소드는 훨씬 깔끔해집니다.

```java
public class UserService {
    public void registerUser(UserDTO dto) {
        UserVO userVO = UserVO.createFromUserDTO(dto);
        // 비즈니스 로직에만 집중 가능
        processUserRegistration(userVO);
    }
    
    public void createAdminUser(AdminDTO dto) {
        UserVO adminVO = UserVO.createFromAdminDTO(dto);
        // 의도가 명확함
        grantAdminPrivileges(adminVO);
    }
}
```

---

## 심화: 팩토리 메소드와 검증 로직

팩토리 메소드를 여러 개 만들다 보면 새로운 문제가 생깁니다. **검증 로직 중복**입니다.

### 문제 상황

```java
public static UserVO createFromUserDTO(UserDTO dto) {
    validateEmail(dto.getEmail());      // 검증1
    validateAge(dto.getAge());          // 검증1
    return UserVO.builder()...build();
}

public static UserVO createFromAdminDTO(AdminDTO dto) {
    validateEmail(dto.getEmail());      // 같은 검증 반복
    validateAge(dto.getAge());          // 중복
    return UserVO.builder()...build();
}

public static UserVO createFromExternalAPI(ExternalDTO dto) {
    validateEmail(dto.getEmail());      // 또 반복...
    validateAge(dto.getAge());
    return UserVO.builder()...build();
}
```

### 최적 해결책: 공통 검증 + 팩토리별 추가 검증

**공통 검증**은 VO 내부의 메소드로 분리하고, **팩토리별 추가 검증**은 각 메소드에서 진행합니다.

```java
@Builder
public class UserVO {
    private String email;
    private int age;
    private String password;
    private UserStatus status;
    
    // 공통 검증: 모든 VO에 필수인 것들
    private void validateCommon() {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Invalid age");
        }
    }
}
```

```java
public static UserVO createFromUserDTO(UserDTO dto) {
    // 팩토리별 추가 검증
    if (dto.getPassword() == null || dto.getPassword().length() < 8) {
        throw new IllegalArgumentException("Password must be at least 8 characters");
    }
    
    UserVO vo = UserVO.builder()
        .email(dto.getEmail())
        .age(dto.getAge())
        .password(dto.getPassword())
        .status(UserStatus.ACTIVE)
        .build();
    
    vo.validateCommon();  // 공통 검증 실행
    return vo;
}

public static UserVO createFromAdminDTO(AdminDTO dto) {
    // 관리자 생성은 비밀번호 검증 생략
    if (dto.getAdminLevel() == null) {
        throw new IllegalArgumentException("Admin level is required");
    }
    
    UserVO vo = UserVO.builder()
        .email(dto.getEmail())
        .age(dto.getAge())
        .password("ADMIN_DEFAULT")  // 관리자는 기본 비밀번호
        .status(UserStatus.ADMIN)
        .build();
    
    vo.validateCommon();
    return vo;
}

public static UserVO createFromExternalAPI(ExternalDTO dto) {
    // 외부 API는 최소한의 검증만
    UserVO vo = UserVO.builder()
        .email(dto.getEmail())
        .age(dto.getAge())
        .password("EXTERNAL_USER")
        .status(UserStatus.EXTERNAL)
        .build();
    
    vo.validateCommon();
    return vo;
}
```

이 구조의 장점:

✅ **DRY 원칙** - 공통 로직은 한 번만 구현  
✅ **유연성** - 각 팩토리에서 필요한 검증만 추가  
✅ **명확성** - 어디서 뭐를 검증하는지 분명함  
✅ **유지보수성** - 공통 검증 수정 시 한 곳만 변경

---

## 실무 적용 예제

### 전체 구조

```java
// VO 클래스
@Getter
@Builder
public class ProductVO {
    private Long id;
    private String name;
    private BigDecimal price;
    private int quantity;
    private ProductStatus status;
    
    // 공통 검증
    private void validateCommon() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
    }
    
    // 팩토리 메소드: 신규 상품 등록
    public static ProductVO createNewProduct(CreateProductDTO dto) {
        if (dto.getName() == null || dto.getName().length() < 3) {
            throw new IllegalArgumentException("Product name must be at least 3 characters");
        }
        
        ProductVO vo = ProductVO.builder()
            .name(dto.getName())
            .price(dto.getPrice())
            .quantity(dto.getQuantity())
            .status(ProductStatus.ACTIVE)
            .build();
        
        vo.validateCommon();
        return vo;
    }
    
    // 팩토리 메소드: 재고 조회
    public static ProductVO createFromEntity(Product entity) {
        ProductVO vo = ProductVO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .price(entity.getPrice())
            .quantity(entity.getQuantity())
            .status(entity.getStatus())
            .build();
        
        vo.validateCommon();
        return vo;
    }
    
    // 팩토리 메소드: 외부 마켓플레이스 동기화
    public static ProductVO createFromMarketplace(MarketplaceDTO dto) {
        if (dto.getExternalId() == null) {
            throw new IllegalArgumentException("External ID is required");
        }
        
        ProductVO vo = ProductVO.builder()
            .name(dto.getProductName())
            .price(dto.getMarketPrice())
            .quantity(dto.getMarketInventory())
            .status(ProductStatus.MARKETPLACE)
            .build();
        
        vo.validateCommon();
        return vo;
    }
}
```

```java
// Service 클래스
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    // 신규 상품 등록
    public void registerNewProduct(CreateProductDTO dto) {
        ProductVO productVO = ProductVO.createNewProduct(dto);
        
        // 비즈니스 로직에만 집중
        Product entity = productMapper.toEntity(productVO);
        productRepository.save(entity);
    }
    
    // 상품 조회 및 가공
    public ProductVO getProduct(Long productId) {
        Product entity = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        
        return ProductVO.createFromEntity(entity);
    }
    
    // 마켓플레이스 동기화
    public void syncFromMarketplace(MarketplaceDTO dto) {
        ProductVO productVO = ProductVO.createFromMarketplace(dto);
        
        // 마켓플레이스 특화 로직
        applyMarketplaceDiscount(productVO);
        updateInventory(productVO);
    }
}
```

---

## 정리: 패턴 적용 체크리스트

이 패턴을 프로젝트에 적용할 때 다음을 확인하세요:

- ✅ 팩토리 메소드명은 **의도를 명확히** 표현하는가? (`createFromUserDTO`, `createNewProduct` 등)
- ✅ 공통 검증을 VO 내부 메소드로 분리했는가?
- ✅ 각 팩토리에서 **필요한 추가 검증**만 하는가?
- ✅ Service 메소드에서 **비즈니스 로직**만 남았는가?
- ✅ 변환과 검증 로직이 **한 곳에 모여**있는가?

---

## 마치며

팩토리 메소드 패턴은 단순해 보이지만, **Service 레이어의 가독성을 획기적으로 개선**합니다. 특히 다양한 출처(DTO, Entity, 외부 API)에서 VO를 생성할 때 그 가치가 극대화됩니다.

**당신의 프로젝트에서 이 패턴을 사용하고 있나요? 혹은 다른 방식으로 해결하고 있나요?** 댓글로 경험을 나누어주세요!