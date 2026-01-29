---
title: "[Java] Null-free 객체 설계와 생성 철학 정리"

tagline: "생성자, 정적 팩토리, 불변성, 엔티티 타협점까지"

header:
  overlay_image: /assets/post/java/2026-01-29-null-free-object-philosophy/overlay.png
  overlay_filter: 0.5

categories:
  - Java

tags:
  - 객체지향
  - 방어적프로그래밍
  - 불변성
  - 정적팩토리
  - 빌더패턴
  - 엔티티
  - DTO

toc: true
show_date: true

last_modified_at: 2026-01-29T22:00:00
---

이번 글은 **"애플리케이션 내부에는 null이 없다"** 라는 전제에서 출발한다. 문자열/객체형은 null을 허용하지 않고, **null이 가능한 경우에만 변수명에 `orNull`을 명시**한다. 이 전제를 지키면 팀 내부 규칙이 아주 단순해진다.

- 내부 로직에서는 null 체크를 하지 않는다 (이미 생성 단계에서 막았기 때문이다)
- null이 가능한 데이터는 변수명에서 바로 알 수 있다
- 객체는 생성 직후부터 완전한 상태를 유지한다

즉, **상태를 생성 시점에 고정하고 이후에는 흔들리지 않게 유지**하는 철학이다. 아래에서 원칙을 상세히 설명한다.

---

# 1. 생성자에서 완전한 상태를 보장한다

객체는 생성 직후 **완전하고 유효한 상태**여야 한다. 생성자에서 필수값 검증과 기본값을 확정해야 null 유입을 막을 수 있다. 여기서 중요한 점은 **생성자가 끝났다는 사실 자체가 유효성의 보증**이 되어야 한다는 것이다.

- 생성자를 호출한 쪽은 객체의 유효성을 의심하지 않아도 된다
- null 체크는 생성자 내부에서 끝나고, 외부 로직에서 사라진다
- 테스트도 단순해진다 (생성 실패 케이스만 검증하면 됨)

```java
public final class UserProfile {
    private final String userId;
    private final String email;
    private final String displayName;
    private final LocalDateTime createdAt;
    private final String profileImageUrlOrNull;
    private final Status status;

    private UserProfile(
            String userId,
            String email,
            String displayName,
            LocalDateTime createdAt,
            String profileImageUrlOrNull,
            Status status
    ) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.email = Objects.requireNonNull(email, "email");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.status = Objects.requireNonNull(status, "status");

        // null 허용은 이름으로 명확히 표현
        this.profileImageUrlOrNull = profileImageUrlOrNull;
    }
}
```

---

# 2. 빌더는 생성자 위에서만 사용한다

빌더 패턴은 가독성이 좋지만, **중간 상태가 존재한다는 문제**가 있다. 따라서 빌더를 쓰더라도 **생성자 위에 @Builder를 붙여서** 최종 객체는 생성자를 통해 완전한 상태로만 만들어지도록 한다.

이 방식의 핵심은 다음과 같다.

- 빌더가 있더라도 최종 생성은 생성자에서만 이루어진다
- 생성자 검증 로직이 단일 위치에만 존재한다
- `build()` 시점에만 객체가 완성되므로, 내부 철학과 충돌하지 않는다

```java
@Builder
private UserProfile(
        String userId,
        String email,
        String displayName,
        LocalDateTime createdAt,
        String profileImageUrlOrNull,
        Status status
) {
    // 생성자 검증 로직 동일
}
```

---

# 3. 객체 생성은 정적 팩토리 메서드로 제한한다

생성자는 숨기고, **정적 팩토리 메서드로만 객체를 생성**한다. 입력값 검증과 기본값 초기화를 한곳에서 통제할 수 있다. 여기서는 특히 두 가지 이점이 크다.

1. 이름으로 의도를 표현할 수 있다 (`createNew`, `fromDatabase`, `createAdmin`)
2. 필수/선택값을 깔끔하게 분리할 수 있다 (필수만 받는 메서드 제공)

그리고 무엇보다, **객체 생성 규칙이 밖으로 노출되지 않는다.** 생성자 파라미터 순서나 개수에 대한 의존이 사라진다.

```java
public static UserProfile createNew(String email, String displayName) {
    return UserProfile.builder()
            .userId(generateUserId())
            .email(email)
            .displayName(displayName)
            .createdAt(LocalDateTime.now())
            .profileImageUrlOrNull(null)
            .status(Status.ACTIVE)
            .build();
}
```

---

# 4. getter/setter는 사용하지 않는다

getter/setter는 **행위의 의미를 숨긴다.** 특히 setter는 내부 상태를 외부가 임의로 변경하게 만든다. 이 철학에서는 **상태 변경의 의도**를 반드시 메서드명에 드러내야 한다.

- `setEmail()`은 단순 대입인지, 검증인지, 알림까지 포함인지 알 수 없다
- `changeEmailAndVerify()`는 의도가 즉시 드러난다

```java
// 모호한 행위
user.setEmail("a@b.com");

// 의도가 명확한 행위
user.changeEmailAndVerify("a@b.com");
```

조회도 목적이 드러나는 이름을 사용한다.

```java
public String fetchDisplayName() {
    return displayName;
}
```

---

# 5. 불변 객체는 변경 대신 새 객체를 만든다

객체 내부 상태를 바꾸지 않고, **새로운 객체를 생성**한다. 예측 가능성과 안정성이 극대화된다. 이 방식을 선택하면 다음이 보장된다.

- 동일 객체는 언제나 동일한 상태를 가진다
- 멀티스레드 환경에서도 안전하다
- "이 객체가 언제 바뀌었지?" 같은 디버깅이 사라진다

```java
public UserProfile updateProfileImage(String imageUrl) {
    return UserProfile.builder()
            .userId(this.userId)
            .email(this.email)
            .displayName(this.displayName)
            .createdAt(this.createdAt)
            .profileImageUrlOrNull(imageUrl)
            .status(this.status)
            .build();
}
```

---

# 6. 엔티티는 타협이 필요하다

JPA/Hibernate는 **동일 인스턴스 변경을 추적(Dirty Checking)** 하기 때문에, 엔티티는 완전 불변 객체로 만들기 어렵다. 프레임워크가 요구하는 구조를 무시하면 업데이트가 누락되거나 의도치 않은 merge가 발생할 수 있다.

- **setter는 금지**
- **의도 기반 변경 메서드만 허용**

```java
@Entity
class UserEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String email;

    protected UserEntity() {}

    public static UserEntity create(String email) {
        UserEntity e = new UserEntity();
        e.id = "USER_" + UUID.randomUUID();
        e.email = email;
        return e;
    }

    // 의도 기반 변경 메서드
    public void changeEmail(String newEmail) {
        this.email = newEmail;
    }
}
```

즉, **엔티티는 불변성 철학을 완벽하게 적용할 수 없고**,
대신 변경을 통제하는 방식으로 타협한다. 핵심은 **setter를 없애되, 의도 기반 메서드로 변경을 강제**하는 것이다.

---

# 7. DTO는 getter 없이도 JSON 출력 가능하다

API 응답용 DTO는 일반적으로 getter가 필요하다고 알려져 있지만, **Jackson 설정과 레코드**를 활용하면 getter 없이도 직렬화가 가능하다. 즉, DTO 역시 의미 없는 getter를 강제할 필요가 없다.

```java
// Java record (getter 없이 직렬화 가능)
public record UserDto(String email, String name) {}
```

또는 다음 방식도 가능하다.

```java
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UserDto {
    private final String email;
    private final String name;
}
```

---

# 정리

- **null은 내부에서 허용하지 않는다**
- **null 가능성은 `orNull`로 명시한다**
- **생성자는 완전한 상태를 보장해야 한다**
- **정적 팩토리로 생성만 허용한다**
- **getter/setter 대신 의도 기반 메서드를 사용한다**
- **불변 객체는 변경 대신 새 객체를 만든다**
- **엔티티는 의도 기반 변경 메서드로 통제한다**
- **DTO는 getter 없이도 직렬화 가능하다**

이 철학을 지키면 코드가 명확해지고, 상태 관리가 쉬워진다. 이제 마지막으로 **일반 객체(불변 VO)와 엔티티 객체**를 전체 예시로 정리한다.

---

# 최종 예시: 일반 객체(불변) vs 엔티티(변경 통제)

## 1) 일반 객체 (불변 VO)

```java
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class UserProfile {
    private final String userId;
    private final String email;
    private final String displayName;
    private final LocalDateTime createdAt;
    private final String profileImageUrlOrNull;
    private final Status status;

    @Builder
    private UserProfile(
            String userId,
            String email,
            String displayName,
            LocalDateTime createdAt,
            String profileImageUrlOrNull,
            Status status
    ) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.email = Objects.requireNonNull(email, "email");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.status = Objects.requireNonNull(status, "status");
        this.profileImageUrlOrNull = profileImageUrlOrNull; // null 허용
        validateEmail(email);
        validateDisplayName(displayName);
    }

    // 정적 팩토리
    public static UserProfile createNew(String email, String displayName) {
        return UserProfile.builder()
                .userId("USER_" + UUID.randomUUID())
                .email(email)
                .displayName(displayName)
                .createdAt(LocalDateTime.now())
                .profileImageUrlOrNull(null)
                .status(Status.ACTIVE)
                .build();
    }

    // 의도 기반 조회
    public String fetchUserId() { return userId; }
    public String fetchEmail() { return email; }
    public String fetchDisplayName() { return displayName; }
    public LocalDateTime fetchCreatedAt() { return createdAt; }
    public Optional<String> fetchProfileImageUrl() {
        return Optional.ofNullable(profileImageUrlOrNull);
    }
    public Status fetchStatus() { return status; }

    // 변경은 새 객체로
    public UserProfile updateProfileImage(String imageUrl) {
        validateImageUrl(imageUrl);
        return UserProfile.builder()
                .userId(this.userId)
                .email(this.email)
                .displayName(this.displayName)
                .createdAt(this.createdAt)
                .profileImageUrlOrNull(imageUrl)
                .status(this.status)
                .build();
    }

    public UserProfile deactivate() {
        if (this.status == Status.DEACTIVATED) {
            throw new IllegalStateException("이미 비활성화 상태입니다.");
        }
        return UserProfile.builder()
                .userId(this.userId)
                .email(this.email)
                .displayName(this.displayName)
                .createdAt(this.createdAt)
                .profileImageUrlOrNull(this.profileImageUrlOrNull)
                .status(Status.DEACTIVATED)
                .build();
    }

    private static void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("유효하지 않은 이메일: " + email);
        }
    }

    private static void validateDisplayName(String name) {
        if (name.isBlank() || name.length() > 50) {
            throw new IllegalArgumentException("표시명은 1~50자");
        }
    }

    private static void validateImageUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("이미지 URL은 공백일 수 없음");
        }
    }

    public enum Status {
        ACTIVE, DEACTIVATED
    }
}
```

## 2) 엔티티 객체 (변경 통제)

```java
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private String profileImageUrlOrNull;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    protected UserEntity() {}

    // 생성은 정적 팩토리
    public static UserEntity create(String email, String displayName) {
        UserEntity e = new UserEntity();
        e.id = "USER_" + UUID.randomUUID();
        e.email = requireNonNull(email, "email");
        e.displayName = requireNonNull(displayName, "displayName");
        e.createdAt = LocalDateTime.now();
        e.profileImageUrlOrNull = null;
        e.status = Status.ACTIVE;
        validateEmail(email);
        validateDisplayName(displayName);
        return e;
    }

    // 의도 기반 변경만 허용
    public void changeEmail(String newEmail) {
        validateEmail(newEmail);
        this.email = newEmail;
    }

    public void updateProfileImage(String imageUrl) {
        validateImageUrl(imageUrl);
        this.profileImageUrlOrNull = imageUrl;
    }

    public void removeProfileImage() {
        this.profileImageUrlOrNull = null;
    }

    public void deactivate() {
        if (this.status == Status.DEACTIVATED) {
            throw new IllegalStateException("이미 비활성화 상태입니다.");
        }
        this.status = Status.DEACTIVATED;
    }

    private static String requireNonNull(String value, String name) {
        return Objects.requireNonNull(value, name + " cannot be null");
    }

    private static void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("유효하지 않은 이메일: " + email);
        }
    }

    private static void validateDisplayName(String name) {
        if (name.isBlank() || name.length() > 50) {
            throw new IllegalArgumentException("표시명은 1~50자");
        }
    }

    private static void validateImageUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("이미지 URL은 공백일 수 없음");
        }
    }

    public enum Status {
        ACTIVE, DEACTIVATED
    }
}
```
