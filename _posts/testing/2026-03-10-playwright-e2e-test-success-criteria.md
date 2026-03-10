---
title: "[Testing] Playwright E2E 테스트 성공 기준 정의하기 - SSR 환경에서의 API와 페이지 검증 전략"

tagline: "SSR 프로젝트에서 Playwright로 E2E 테스트를 작성할 때 API 응답과 페이지 렌더링의 성공 기준을 어떻게 정의할까?"

header:
  overlay_image: /assets/post/testing/2026-03-10-playwright-e2e-test-success-criteria/overlay.png
  overlay_filter: 0.5

categories:
  - Testing

tags:
  - Playwright
  - E2E Testing
  - Test Strategy
  - SSR
  - API Testing
  - Test Isolation

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-03-10T15:30:00+09:00
---

E2E(End-to-End) 테스트를 작성하다 보면 막막한 순간이 있습니다. 특히 "이 테스트가 성공했다고 판단할 기준이 뭐지?"라는 질문 앞에서요.

SSR(Server-Side Rendering) 환경에서 Playwright로 테스트를 작성하면서 이런 고민들이 생겼습니다:

- API가 200을 반환하면 무조건 성공일까?
- 페이지가 로드됐다는 건 어떻게 확인하지?
- 예외 상황은 어떻게 테스트하지?
- 테스트 간 데이터 충돌은 어떻게 막을까?

이 글에서는 실제 프로젝트에서 마주친 이런 고민들을 하나씩 풀어가며, **실용적인 테스트 성공 기준**을 정의하는 방법을 공유하겠습니다.

## 문제 상황: HTTP 200이 성공을 의미하지 않을 때

많은 API 서버가 **공통 Response 객체 구조**를 사용합니다. 예를 들어:

```java
@Schema(description = "성공 여부", example = "true")
private final Boolean success;

@Schema(description = "HTTP 상태 코드", example = "200")
private final Integer status;

@Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
private final String message;

@Schema(description = "응답 데이터")
private final T data;

@Schema(description = "에러 코드 (실패 시)", example = "ERR_001")
private final String errorCode;

@Schema(description = "응답 시간", example = "2025-11-25T23:16:52")
private final LocalDateTime timestamp;
```

이 구조에서 다음과 같은 상황이 발생할 수 있습니다:

```json
// HTTP 200이지만 비즈니스 로직 실패
{
  "success": false,
  "status": 200,
  "message": "사용자를 찾을 수 없습니다",
  "data": null,
  "errorCode": "USER_NOT_FOUND",
  "timestamp": "2026-03-10T14:30:00"
}
```

**HTTP 상태 코드만 확인하면 이런 실패를 놓치게 됩니다.**

<div class="mermaid mermaid-center">
graph TD
    A[API 호출] --> B{HTTP Status}
    B -->|200| C{success 필드}
    B -->|4xx/5xx| F[❌ 실패: 네트워크/서버 에러]
    C -->|true| D{data 검증}
    C -->|false| G[❌ 실패: 비즈니스 로직 에러]
    D -->|유효| E[✅ 성공]
    D -->|null/invalid| H[❌ 실패: 데이터 없음]
    
    style E fill:#2d5016,stroke:#4a7c2c,stroke-width:2px,color:#e8f5e9
    style F fill:#5c1010,stroke:#8b2020,stroke-width:2px,color:#ffebee
    style G fill:#5c1010,stroke:#8b2020,stroke-width:2px,color:#ffebee
    style H fill:#5c1010,stroke:#8b2020,stroke-width:2px,color:#ffebee
</div>

## API 테스트 성공 기준: 단계별 검증 전략

API 응답을 검증할 때는 **중요도에 따라 검증 깊이를 달리**하는 것이 효율적입니다.

### 1. 기본 검증 (모든 API 공통)

```typescript
test('API 기본 검증', async ({ page }) => {
  const response = await page.request.get('/api/products');
  const json = await response.json();
  
  // 1단계: HTTP 레벨 검증
  expect(response.status()).toBe(200);
  
  // 2단계: 비즈니스 레벨 검증
  expect(json.success).toBe(true);
  expect(json.errorCode).toBeUndefined();
});
```

### 2. 일반 조회 API (가벼운 검증)

단순 목록 조회나 데이터 읽기는 **데이터 존재 여부**만 확인합니다:

```typescript
test('상품 목록 조회', async ({ page }) => {
  const response = await page.request.get('/api/products');
  const json = await response.json();
  
  expect(json.success).toBe(true);
  expect(json.status).toBe(200);
  
  // 데이터 존재 여부만 확인
  expect(json.data).toBeDefined();
  expect(Array.isArray(json.data.items)).toBe(true);
  expect(json.data.items.length).toBeGreaterThan(0);
});
```

### 3. 핵심 비즈니스 API (상세 검증)

주문, 결제, 인증 같은 중요한 로직은 **핵심 필드를 상세히 검증**합니다:

```typescript
test('주문 생성 API', async ({ page }) => {
  const orderData = {
    productId: 1,
    quantity: 2,
    totalAmount: 50000
  };
  
  const response = await page.request.post('/api/orders', {
    data: orderData
  });
  const json = await response.json();
  
  // 기본 검증
  expect(json.success).toBe(true);
  expect(json.status).toBe(201);
  
  // 핵심 필드 상세 검증
  expect(json.data.orderId).toBeDefined();
  expect(typeof json.data.orderId).toBe('string');
  
  expect(json.data.totalAmount).toBe(orderData.totalAmount);
  expect(json.data.quantity).toBe(orderData.quantity);
  
  expect(json.data.orderStatus).toBe('PENDING');
  expect(json.data.createdAt).toBeDefined();
  
  // timestamp가 최근인지 확인
  const orderTime = new Date(json.data.createdAt);
  const now = new Date();
  expect(now.getTime() - orderTime.getTime()).toBeLessThan(5000); // 5초 이내
});
```

[출처: Playwright API Testing 공식 문서](https://playwright.dev/docs/api-testing)

## 페이지 렌더링 검증: SSR 환경의 특징

SSR 환경에서는 서버가 이미 데이터를 포함한 HTML을 내려주기 때문에, 페이지 검증 전략이 CSR과 다릅니다.

### 정적 콘텐츠로 렌더링 성공 확인

**동적 데이터는 변할 수 있으므로, 정적인 UI 요소를 검증**합니다:

```typescript
test('상품 목록 페이지 렌더링', async ({ page }) => {
  await page.goto('/products');
  
  // ✅ 좋은 예: 정적 레이블 확인
  await expect(page.getByRole('heading', { name: '상품 목록' })).toBeVisible();
  await expect(page.getByText('가격')).toBeVisible();
  await expect(page.getByText('재고')).toBeVisible();
  
  // ❌ 나쁜 예: 동적 데이터 확인 (테스트가 불안정해짐)
  // await expect(page.getByText('노트북 - 1,500,000원')).toBeVisible();
});
```

### 여러 assertion 조합하기

페이지가 제대로 렌더링되었는지 확인하려면 **다양한 assertion을 조합**합니다:

```typescript
test('로그인 페이지 검증', async ({ page }) => {
  await page.goto('/login');
  
  // 페이지 제목 확인
  await expect(page).toHaveTitle(/로그인/);
  
  // URL 확인
  await expect(page).toHaveURL(/\/login/);
  
  // 필수 UI 요소 확인
  await expect(page.getByLabel('이메일')).toBeVisible();
  await expect(page.getByLabel('비밀번호')).toBeVisible();
  await expect(page.getByRole('button', { name: '로그인' })).toBeEnabled();
  
  // 링크 속성 확인
  await expect(page.getByRole('link', { name: '회원가입' }))
    .toHaveAttribute('href', '/register');
});
```

[출처: Playwright Assertions 공식 문서](https://playwright.dev/docs/test-assertions)

## 실패 케이스 테스트의 중요성

**해피 패스만 테스트하는 것은 절반만 하는 것입니다.** 예외 상황에서 시스템이 어떻게 반응하는지 검증하는 것이 더 중요할 수 있습니다.

### 일반적인 실패 시나리오

```typescript
// 1. 존재하지 않는 리소스 조회
test('404 에러 처리', async ({ page }) => {
  const response = await page.request.get('/api/product/999999');
  const json = await response.json();
  
  expect(response.status()).toBe(404);
  expect(json.success).toBe(false);
  expect(json.errorCode).toBe('PRODUCT_NOT_FOUND');
  expect(json.message).toContain('상품을 찾을 수 없습니다');
});

// 2. 잘못된 요청 파라미터
test('400 에러 처리', async ({ page }) => {
  const response = await page.request.post('/api/orders', {
    data: {
      productId: 'invalid', // 숫자여야 하는데 문자열
      quantity: -1          // 음수 불가
    }
  });
  const json = await response.json();
  
  expect(response.status()).toBe(400);
  expect(json.success).toBe(false);
  expect(json.errorCode).toBe('INVALID_REQUEST');
});

// 3. 권한 없는 접근
test('403 에러 처리', async ({ page }) => {
  // 로그인 없이 보호된 리소스 접근
  const response = await page.request.get('/api/admin/users');
  const json = await response.json();
  
  expect(response.status()).toBe(403);
  expect(json.success).toBe(false);
  expect(json.errorCode).toBe('UNAUTHORIZED');
});

// 4. 비즈니스 로직 실패 (재고 부족 등)
test('재고 부족 에러', async ({ page }) => {
  const response = await page.request.post('/api/orders', {
    data: {
      productId: 1,
      quantity: 1000 // 재고보다 많은 수량
    }
  });
  const json = await response.json();
  
  // HTTP는 200이지만 비즈니스 로직 실패
  expect(response.status()).toBe(200);
  expect(json.success).toBe(false);
  expect(json.errorCode).toBe('INSUFFICIENT_STOCK');
});
```

### 페이지에서 에러 메시지 검증

```typescript
test('로그인 실패 시 에러 메시지 표시', async ({ page }) => {
  await page.goto('/login');
  
  await page.getByLabel('이메일').fill('wrong@example.com');
  await page.getByLabel('비밀번호').fill('wrongpassword');
  await page.getByRole('button', { name: '로그인' }).click();
  
  // 에러 메시지가 표시되는지 확인
  await expect(page.getByRole('alert')).toBeVisible();
  await expect(page.getByRole('alert')).toContainText('이메일 또는 비밀번호가 일치하지 않습니다');
  
  // 로그인 페이지에 그대로 있는지 확인
  await expect(page).toHaveURL(/\/login/);
});
```

## 테스트 독립성: 순서에 구애받지 않는 테스트

**테스트는 실행 순서와 관계없이 항상 같은 결과를 내야 합니다.** 그래야 유지보수가 쉽고 신뢰할 수 있습니다.

### 나쁜 예: 테스트 간 의존성

```typescript
// ❌ 나쁜 예: 순서에 의존
let createdProductId: string;

test('상품 생성', async ({ page }) => {
  const response = await page.request.post('/api/products', {
    data: { name: 'Test Product' }
  });
  createdProductId = response.data.id; // 다음 테스트에서 사용
});

test('상품 조회', async ({ page }) => {
  // 위 테스트가 먼저 실행되지 않으면 실패
  const response = await page.request.get(`/api/products/${createdProductId}`);
  expect(response.success).toBe(true);
});

test('상품 삭제', async ({ page }) => {
  // 위 테스트들에 의존
  await page.request.delete(`/api/products/${createdProductId}`);
});
```

### 좋은 예: 독립적인 테스트

```typescript
// ✅ 좋은 예: 테스트 내부에서 필요한 데이터 생성
test('상품 생성 후 조회', async ({ page }) => {
  // 1. 데이터 생성
  const createResponse = await page.request.post('/api/products', {
    data: {
      name: `Test Product ${Date.now()}`, // 고유한 이름
      price: 10000,
      stock: 100
    }
  });
  
  expect(createResponse.json().success).toBe(true);
  const productId = createResponse.json().data.id;
  
  // 2. 생성한 데이터 조회
  const getResponse = await page.request.get(`/api/products/${productId}`);
  const product = getResponse.json().data;
  
  expect(product.id).toBe(productId);
  expect(product.name).toContain('Test Product');
});

test('상품 삭제 기능', async ({ page }) => {
  // 이 테스트도 독립적으로 데이터 생성
  const createResponse = await page.request.post('/api/products', {
    data: {
      name: `Delete Test ${Date.now()}`,
      price: 5000,
      stock: 50
    }
  });
  
  const productId = createResponse.json().data.id;
  
  // 삭제 테스트
  const deleteResponse = await page.request.delete(`/api/products/${productId}`);
  expect(deleteResponse.json().success).toBe(true);
  
  // 삭제 확인
  const getResponse = await page.request.get(`/api/products/${productId}`);
  expect(getResponse.status()).toBe(404);
});
```

<div class="mermaid mermaid-center">
graph LR
    A[테스트 1] --> B[data.sql<br/>초기 데이터]
    C[테스트 2] --> B
    D[테스트 3] --> B
    
    A --> E[필요시<br/>추가 데이터<br/>생성]
    C --> F[필요시<br/>추가 데이터<br/>생성]
    D --> G[필요시<br/>추가 데이터<br/>생성]
    
    E --> H[독립 실행]
    F --> I[독립 실행]
    G --> J[독립 실행]
    
    style B fill:#2d4a5c,stroke:#4a7c9c,stroke-width:2px,color:#e3f2fd
    style H fill:#2d5016,stroke:#4a7c2c,stroke-width:2px,color:#e8f5e9
    style I fill:#2d5016,stroke:#4a7c2c,stroke-width:2px,color:#e8f5e9
    style J fill:#2d5016,stroke:#4a7c2c,stroke-width:2px,color:#e8f5e9
</div>

### 초기 데이터 관리: data.sql

테스트가 항상 같은 초기 상태에서 시작하도록 `data.sql` 파일로 기본 데이터를 관리합니다:

```sql
-- data.sql (H2 Database 초기 데이터)
INSERT INTO users (id, username, email, password)
VALUES (1, 'testuser', 'test@example.com', 'hashed_password');

INSERT INTO products (id, name, price, stock)
VALUES 
  (1, 'BaseProduct1', 10000, 50),
  (2, 'BaseProduct2', 20000, 30),
  (3, 'BaseProduct3', 15000, 100);

INSERT INTO categories (id, name)
VALUES 
  (1, 'Electronics'),
  (2, 'Books');
```

이 초기 데이터는:
- ✅ 모든 테스트가 공통으로 사용할 수 있는 안정적인 기반
- ✅ 테스트마다 DB가 재시작되므로 항상 같은 상태 보장
- ✅ 필요한 추가 데이터는 각 테스트에서 생성

## 병렬 실행 vs 순차 실행: 데이터 충돌 문제

Playwright는 기본적으로 **병렬 실행**을 지원하지만, 같은 데이터베이스를 공유하면 충돌이 발생할 수 있습니다.

### 문제 상황

```typescript
// 동시에 실행되면 충돌 가능
Test A: UPDATE product SET stock=5 WHERE id=1
Test B: DELETE product WHERE id=1
→ Test A 실패 가능 (데이터가 사라짐)

Test A: INSERT INTO users (username) VALUES ('testuser')
Test B: INSERT INTO users (username) VALUES ('testuser')
→ 유니크 제약 위반
```

### 해결책 1: 순차 실행 (간단하고 안전)

```typescript
// playwright.config.ts
import { defineConfig } from '@playwright/test';

export default defineConfig({
  workers: 1, // 한 번에 하나의 테스트만 실행
  retries: 2,
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
  },
});
```

**장점:**
- ✅ 구현이 간단함
- ✅ 데이터 충돌 걱정 없음
- ✅ 디버깅이 쉬움

**단점:**
- ❌ 느린 실행 속도

[출처: Playwright Test Configuration](https://playwright.dev/docs/test-configuration)

### 해결책 2: 고유 데이터 사용 (병렬 가능)

```typescript
// 각 테스트가 고유한 데이터 생성
test('상품 생성 1', async ({ page }) => {
  const uniqueName = `product_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  const response = await page.request.post('/api/products', {
    data: { name: uniqueName, price: 10000 }
  });
  
  expect(response.json().success).toBe(true);
});

test('상품 생성 2', async ({ page }) => {
  // 다른 고유 이름 사용
  const uniqueName = `product_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  const response = await page.request.post('/api/products', {
    data: { name: uniqueName, price: 20000 }
  });
  
  expect(response.json().success).toBe(true);
});
```

**장점:**
- ✅ 병렬 실행으로 빠른 속도
- ✅ 데이터 충돌 방지

**단점:**
- ❌ 테스트 코드가 복잡해질 수 있음
- ❌ 특정 데이터에 의존하는 테스트는 어려움

### 해결책 3: Worker별 격리 (고급)

```typescript
// fixtures/db-fixture.ts
import { test as baseTest } from '@playwright/test';

export const test = baseTest.extend<{}, { dbUserName: string }>({
  // Worker마다 고유한 사용자 생성
  dbUserName: [async ({ }, use) => {
    const userName = `user_worker_${baseTest.info().workerIndex}`;
    
    // DB에 사용자 생성
    await createUserInTestDatabase(userName);
    
    await use(userName);
    
    // 테스트 후 정리
    await deleteUserFromTestDatabase(userName);
  }, { scope: 'worker' }],
});

// 테스트에서 사용
import { test, expect } from './fixtures/db-fixture';

test('워커별 격리 테스트', async ({ page, dbUserName }) => {
  // 각 워커가 고유한 사용자로 테스트
  console.log(`Using user: ${dbUserName}`);
  
  await page.goto('/profile');
  await expect(page.getByText(dbUserName)).toBeVisible();
});
```

[출처: Playwright Test Fixtures - Parallel Execution](https://playwright.dev/docs/test-parallel#worker-index)

<div class="mermaid mermaid-center">
graph TB
    subgraph "순차 실행 (workers: 1)"
        A1[Test 1] --> A2[Test 2]
        A2 --> A3[Test 3]
        A3 --> A4[Test 4]
    end
    
    subgraph "병렬 실행 (workers: 4)"
        B1[Test 1<br/>Worker 1]
        B2[Test 2<br/>Worker 2]
        B3[Test 3<br/>Worker 3]
        B4[Test 4<br/>Worker 4]
    end
    
    C[H2 DB<br/>공유 데이터]
    
    A1 --> C
    A2 --> C
    A3 --> C
    A4 --> C
    
    B1 -.충돌 가능.-> C
    B2 -.충돌 가능.-> C
    B3 -.충돌 가능.-> C
    B4 -.충돌 가능.-> C
    
    style C fill:#2d4a5c,stroke:#4a7c9c,stroke-width:2px,color:#e3f2fd
    style A1 fill:#2d5016,stroke:#4a7c2c,stroke-width:2px,color:#e8f5e9
    style A2 fill:#2d5016,stroke:#4a7c2c,stroke-width:2px,color:#e8f5e9
    style A3 fill:#2d5016,stroke:#4a7c2c,stroke-width:2px,color:#e8f5e9
    style A4 fill:#2d5016,stroke:#4a7c2c,stroke-width:2px,color:#e8f5e9
    style B1 fill:#5c4210,stroke:#8b6820,stroke-width:2px,color:#fff3e0
    style B2 fill:#5c4210,stroke:#8b6820,stroke-width:2px,color:#fff3e0
    style B3 fill:#5c4210,stroke:#8b6820,stroke-width:2px,color:#fff3e0
    style B4 fill:#5c4210,stroke:#8b6820,stroke-width:2px,color:#fff3e0
</div>

## Mock API는 언제 사용할까?

실제 프로젝트에서는 **Mock API를 거의 사용하지 않았습니다.** 다음 이유 때문입니다:

1. **H2 DB가 초기 상태로 시작** → 실제 백엔드로 대부분의 시나리오 재현 가능
2. **실제 API 테스트가 더 신뢰성 높음** → 프로덕션 환경과 동일
3. **Mock은 유지보수 비용 증가** → API 스펙이 바뀌면 Mock도 수정 필요

하지만 다음 상황에서는 Mock이 유용할 수 있습니다:

### Mock이 필요한 경우

```typescript
// 1. 외부 서비스 의존성 제거 (결제, 메일 발송 등)
test('결제 프로세스 테스트', async ({ page }) => {
  // 실제 결제 API 호출 대신 Mock
  await page.route('**/api/payment/process', route => {
    route.fulfill({
      status: 200,
      body: JSON.stringify({
        success: true,
        data: { transactionId: 'MOCK_TX_123', status: 'COMPLETED' }
      })
    });
  });
  
  await page.goto('/checkout');
  await page.getByRole('button', { name: '결제하기' }).click();
  
  await expect(page.getByText('결제가 완료되었습니다')).toBeVisible();
});

// 2. 특정 에러 상황 재현
test('네트워크 타임아웃 처리', async ({ page }) => {
  await page.route('**/api/products', route => {
    // 타임아웃 시뮬레이션
    return new Promise(() => {}); // 영원히 대기
  });
  
  await page.goto('/products');
  
  // 로딩 스피너가 계속 표시되는지 확인
  await expect(page.getByTestId('loading-spinner')).toBeVisible();
});

// 3. 서버가 아직 구현되지 않은 API
test('신규 기능 프론트엔드 테스트', async ({ page }) => {
  await page.route('**/api/new-feature', route => {
    route.fulfill({
      status: 200,
      body: JSON.stringify({
        success: true,
        data: { message: '신규 기능 데이터' }
      })
    });
  });
  
  // 프론트엔드 동작만 테스트
});
```

[출처: Playwright Network Mocking](https://playwright.dev/docs/mock)

## 실전 예제: 완성된 테스트 스위트

지금까지 논의한 모든 개념을 종합한 실전 예제입니다:

```typescript
// tests/e2e/product.spec.ts
import { test, expect } from '@playwright/test';

test.describe('상품 관리 E2E 테스트', () => {
  
  // 페이지 렌더링 테스트
  test('상품 목록 페이지가 정상적으로 렌더링된다', async ({ page }) => {
    await page.goto('/products');
    
    // 정적 UI 요소 검증
    await expect(page).toHaveTitle(/상품 목록/);
    await expect(page.getByRole('heading', { name: '상품 목록' })).toBeVisible();
    await expect(page.getByText('상품명')).toBeVisible();
    await expect(page.getByText('가격')).toBeVisible();
  });
  
  // API 기본 검증
  test('상품 목록 API가 정상 응답한다', async ({ page }) => {
    const response = await page.request.get('/api/products');
    const json = await response.json();
    
    expect(response.status()).toBe(200);
    expect(json.success).toBe(true);
    expect(json.data.items.length).toBeGreaterThan(0);
  });
  
  // 핵심 비즈니스 로직 상세 검증
  test('상품 생성 후 조회할 수 있다', async ({ page }) => {
    // 고유한 데이터 생성 (병렬 실행 대비)
    const uniqueName = `테스트상품_${Date.now()}`;
    
    // 1. 상품 생성
    const createResponse = await page.request.post('/api/products', {
      data: {
        name: uniqueName,
        price: 29900,
        stock: 100,
        categoryId: 1
      }
    });
    const createJson = await createResponse.json();
    
    expect(createResponse.status()).toBe(201);
    expect(createJson.success).toBe(true);
    expect(createJson.data.id).toBeDefined();
    
    const productId = createJson.data.id;
    
    // 2. 생성된 상품 조회
    const getResponse = await page.request.get(`/api/products/${productId}`);
    const getJson = await getResponse.json();
    
    expect(getResponse.status()).toBe(200);
    expect(getJson.success).toBe(true);
    
    // 핵심 필드 검증
    expect(getJson.data.id).toBe(productId);
    expect(getJson.data.name).toBe(uniqueName);
    expect(getJson.data.price).toBe(29900);
    expect(getJson.data.stock).toBe(100);
  });
  
  // 실패 케이스: 존재하지 않는 상품
  test('존재하지 않는 상품 조회 시 404 에러를 반환한다', async ({ page }) => {
    const response = await page.request.get('/api/products/999999');
    const json = await response.json();
    
    expect(response.status()).toBe(404);
    expect(json.success).toBe(false);
    expect(json.errorCode).toBe('PRODUCT_NOT_FOUND');
    expect(json.message).toContain('상품을 찾을 수 없습니다');
  });
  
  // 실패 케이스: 잘못된 요청
  test('잘못된 파라미터로 상품 생성 시 400 에러를 반환한다', async ({ page }) => {
    const response = await page.request.post('/api/products', {
      data: {
        name: '', // 빈 이름
        price: -1000, // 음수 가격
        stock: 'invalid' // 잘못된 타입
      }
    });
    const json = await response.json();
    
    expect(response.status()).toBe(400);
    expect(json.success).toBe(false);
    expect(json.errorCode).toBe('INVALID_REQUEST');
  });
  
  // 사용자 플로우 테스트
  test('상품 생성 → 페이지에서 확인 → 수정 플로우', async ({ page }) => {
    const uniqueName = `플로우테스트_${Date.now()}`;
    
    // 1. 백엔드에서 상품 생성
    const createResponse = await page.request.post('/api/products', {
      data: { name: uniqueName, price: 50000, stock: 10 }
    });
    const productId = createResponse.json().data.id;
    
    // 2. 페이지에서 생성된 상품 확인
    await page.goto('/products');
    await page.getByPlaceholder('상품 검색').fill(uniqueName);
    await page.getByRole('button', { name: '검색' }).click();
    
    await expect(page.getByText(uniqueName)).toBeVisible();
    await expect(page.getByText('50,000원')).toBeVisible();
    
    // 3. 상품 수정 페이지로 이동
    await page.getByRole('link', { name: '수정' }).click();
    await expect(page).toHaveURL(new RegExp(`/products/${productId}/edit`));
    
    // 4. 수정 폼 입력
    await page.getByLabel('가격').fill('45000');
    await page.getByRole('button', { name: '저장' }).click();
    
    // 5. 수정 확인
    await expect(page.getByText('상품이 수정되었습니다')).toBeVisible();
    
    // 6. API로 수정 확인
    const getResponse = await page.request.get(`/api/products/${productId}`);
    const product = getResponse.json().data;
    
    expect(product.price).toBe(45000);
  });
});
```

## 결론: 실용적인 테스트 성공 기준 정리

지금까지 논의한 내용을 정리하면:

### API 테스트 성공 기준

1. **기본 검증 (모든 API)**
   - HTTP status 확인
   - `success` 필드 확인
   - `errorCode` 없음 확인

2. **일반 조회 (가벼운 검증)**
   - 데이터 존재 여부 (`data.length > 0`)
   - 배열/객체 타입 확인

3. **핵심 비즈니스 (상세 검증)**
   - 핵심 필드 상세 검증
   - 데이터 타입 확인
   - 생성 시간 등 메타데이터 검증

### 페이지 테스트 성공 기준

1. **SSR 렌더링 확인**
   - 정적 UI 요소 가시성
   - 페이지 제목과 URL
   - 필수 버튼/링크 존재 여부

2. **동적 데이터는 검증하지 않음**
   - 테스트 안정성 확보
   - 유지보수 용이성

### 테스트 격리 전략

1. **독립성 보장**
   - 각 테스트가 필요한 데이터 직접 생성
   - 실행 순서와 무관하게 성공
   - `data.sql`로 공통 초기 데이터 관리

2. **병렬 실행 선택**
   - 간단한 프로젝트: `workers: 1` (순차 실행)
   - 대규모 프로젝트: 고유 데이터 전략 + 병렬 실행

3. **실패 케이스 필수**
   - 404, 400, 403 등 HTTP 에러
   - 비즈니스 로직 실패 (`success: false`)
   - 권한, 제약 조건 위반

### 실무 팁

⚠️ **Mock API는 최소한으로**
- 실제 백엔드 테스트가 더 신뢰성 높음
- 외부 서비스나 재현 불가능한 상황에만 사용

✅ **정적 콘텐츠로 검증**
- 동적 데이터는 테스트를 불안정하게 만듦
- UI 레이블, 버튼 같은 고정된 요소 검증

✅ **고유한 데이터 생성**
```typescript
const uniqueName = `test_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
```

✅ **테스트 이름은 명확하게**
```typescript
test('존재하지 않는 상품 조회 시 404 에러를 반환한다', ...);
// 무엇을(What), 언제(When), 기대결과(Then)를 명확히
```

## 마치며

E2E 테스트의 성공 기준을 정의하는 것은 **비즈니스 요구사항과 기술적 제약의 균형**을 맞추는 과정입니다.

완벽한 테스트보다는:
- ✅ **유지보수 가능한** 테스트
- ✅ **신뢰할 수 있는** 테스트
- ✅ **실패 원인을 명확히 알려주는** 테스트

가 더 중요합니다.

여러분의 프로젝트에서는 어떤 기준으로 테스트를 작성하고 계신가요? 어떤 어려움을 겪고 계신가요? 댓글로 경험을 공유해주세요!

---

### 참고 자료

- [Playwright 공식 문서 - API Testing](https://playwright.dev/docs/api-testing)
- [Playwright 공식 문서 - Test Assertions](https://playwright.dev/docs/test-assertions)
- [Playwright 공식 문서 - Network Mocking](https://playwright.dev/docs/mock)
- [Playwright 공식 문서 - Parallelism and Sharding](https://playwright.dev/docs/test-parallel)
- [Playwright 공식 문서 - Test Configuration](https://playwright.dev/docs/test-configuration)
