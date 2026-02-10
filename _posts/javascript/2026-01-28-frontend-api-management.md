---
title: "[JavaScript] 프론트엔드 API 관리 전략: 유지보수 가능한 구조 설계하기"

tagline: "백엔드 Controller 구조와 1:1 매핑으로 중복 없는 API 관리 구현하기"

header:
  overlay_image: /assets/post/javascript/2026-01-28-frontend-api-management/overlay.png
  overlay_filter: 0.5

categories:
  - Javascript

tags:
  - API관리
  - 프론트엔드아키텍처
  - OpenAPI
  - TypeScript
  - 클린코드
  - 유지보수

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-28T15:30:00+09:00
---

프론트엔드 프로젝트가 커지면서 가장 먼저 복잡해지는 부분이 API 호출 관리입니다. 처음엔 간단하게 컴포넌트에서 직접 `fetch`나 `axios`를 호출하다가, 어느 순간 같은 API를 여러 페이지에서 중복 호출하고, API 엔드포인트가 변경되면 프로젝트 전체를 뒤져야 하는 상황에 직면합니다.

이 글에서는 **체계적이고 유지보수 가능한 API 관리 전략**을 소개합니다. 단순히 코드를 정리하는 수준이 넘어, 팀 전체의 생산성을 높이고 백엔드와의 협업을 원활하게 만드는 구조를 제시합니다.

## 문제 인식: 왜 API 관리가 어려운가?

### 1. 중복과 분산의 악순환

동일한 API를 여러 페이지에서 호출하는 경우, API 엔드포인트가 변경되면 모든 페이지를 찾아서 수정해야 합니다.

```javascript
// UserListPage.jsx
fetch('/api/v1/users/123')
  .then(res => res.json())
  .then(data => setUser(data));

// UserProfilePage.jsx
fetch('/api/v1/users/123')  // 동일한 API
  .then(res => res.json())
  .then(data => setProfile(data));

// AdminDashboard.jsx
fetch('/api/v1/users/123')  // 또 동일한 API
  .then(res => res.json())
  .then(data => setAdminData(data));
```

API 경로가 `/api/v1/users/{id}`에서 `/api/v2/members/{id}`로 변경되면? 프로젝트 전체를 검색해서 일일이 수정해야 합니다.

### 2. 가독성과 유지보수성의 트레이드오프

API를 변수나 함수로 추출하면 중복은 해결되지만, 새로운 문제가 발생합니다.

```javascript
// api/constants.js
export const USER_API = '/api/v1/users/';

// 컴포넌트에서 사용
fetch(USER_API + userId)
```

이 코드를 보는 개발자는 다음 질문을 하게 됩니다:
- `USER_API`가 정확히 어떤 API인가?
- GET인가 POST인가?
- 어떤 파라미터가 필요한가?
- 응답 구조는 어떻게 생겼나?

결국 `api/constants.js` 파일을 열어봐야 하고, 함수로 추출했다면 함수 정의를 찾아가야 합니다.

### 3. 일관성 없는 에러 처리

각 컴포넌트마다 에러 처리 방식이 다르면, 사용자 경험도 일관되지 않고 디버깅도 어려워집니다.

```javascript
// PageA.jsx - 토스트 표시
catch (error) {
  showToast('에러가 발생했습니다');
}

// PageB.jsx - 콘솔 로그만
catch (error) {
  console.error(error);
}

// PageC.jsx - 에러 페이지로 이동
catch (error) {
  navigate('/error');
}
```

## 해결 전략: 체계적인 API 관리 아키텍처

### 핵심 원칙 1: 백엔드 API 1개 = 프론트엔드 함수 1개

**Single Source of Truth** 원칙을 적용합니다. 백엔드에서 제공하는 API 하나당 프론트엔드에서도 정확히 하나의 함수나 클래스로 정의합니다.

```typescript
// ❌ 잘못된 예: 같은 API를 여러 곳에서 정의
// userList.ts
export const fetchUser = (id) => axios.get(`/api/v1/users/${id}`);

// userProfile.ts
export const getUser = (id) => axios.get(`/api/v1/users/${id}`);  // 중복!

// ✅ 올바른 예: 한 곳에서만 정의
// api/user.ts
export const getUser = (id: number) => 
  apiClient.get<User>(`/users/${id}`);

// 모든 컴포넌트에서 동일하게 사용
import { getUser } from '@/api/user';
```

### 핵심 원칙 2: 백엔드 Controller 구조와 1:1 매핑

프론트엔드의 API 파일 구조를 백엔드의 Controller 구조와 동일하게 유지합니다.

<div class="mermaid mermaid-center">
graph LR
    A[백엔드 Controller] --> B[프론트엔드 API 파일]
    
    A1[UserController] --> B1[user.ts]
    A2[ProductController] --> B2[product.ts]
    A3[OrderController] --> B3[order.ts]
    
    style A fill:#2d3748,stroke:#4a5568,color:#e2e8f0
    style B fill:#2d3748,stroke:#4a5568,color:#e2e8f0
    style A1 fill:#1a365d,stroke:#2c5282,color:#bee3f8
    style A2 fill:#1a365d,stroke:#2c5282,color:#bee3f8
    style A3 fill:#1a365d,stroke:#2c5282,color:#bee3f8
    style B1 fill:#1a365d,stroke:#2c5282,color:#bee3f8
    style B2 fill:#1a365d,stroke:#2c5282,color:#bee3f8
    style B3 fill:#1a365d,stroke:#2c5282,color:#bee3f8
</div>

**이점:**
- API 엔드포인트를 찾기 쉬움
- 백엔드 개발자와 소통할 때 동일한 용어 사용
- 백엔드 변경사항을 프론트엔드에 빠르게 반영 가능

### 핵심 원칙 3: 라이브러리 의존성 격리

HTTP 클라이언트 라이브러리(axios, fetch 등)를 추상화 레이어로 감싸서, 나중에 라이브러리를 교체해도 컴포넌트 코드는 변경하지 않도록 합니다.

```typescript
// api/config.ts - 라이브러리 의존성 격리
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 인터셉터로 공통 로직 처리
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      await refreshToken();
      return apiClient(error.config); // 재시도
    }
    throw error;
  }
);
```

**참고:** Axios의 인터셉터는 요청/응답 전후에 공통 로직을 처리할 수 있는 강력한 기능입니다. 자세한 내용은 [Axios 공식 문서 - Interceptors](https://axios-http.com/docs/interceptors)를 참조하세요.

나중에 axios를 fetch로 교체하고 싶다면? `api/config.ts` 파일만 수정하면 됩니다. 모든 컴포넌트는 `userApi.getUser()`를 그대로 사용할 수 있습니다.

### 핵심 원칙 4: 컴포넌트에서 비즈니스 로직 처리

API 호출은 공통이지만, 호출 후의 비즈니스 로직은 컴포넌트마다 다릅니다. 따라서 에러 처리나 로딩 상태는 각 컴포넌트에서 개별적으로 처리합니다.

```typescript
// 프로필 페이지
const ProfilePage = () => {
  const [user, setUser] = useState<User | null>(null);
  
  useEffect(() => {
    const loadUser = async () => {
      try {
        const { data } = await userApi.getUser(userId);
        setUser(data);
        // 프로필 페이지만의 로직: 폼에 데이터 채우기
        fillFormData(data);
      } catch (error) {
        // 프로필 페이지만의 에러 처리
        showToast('사용자 정보를 불러올 수 없습니다');
      }
    };
    loadUser();
  }, [userId]);
};

// 관리자 페이지
const AdminPage = () => {
  const [users, setUsers] = useState<User[]>([]);
  
  const addUser = async (userId: number) => {
    try {
      const { data } = await userApi.getUser(userId);
      // 관리자 페이지만의 로직: 목록에 추가
      setUsers(prev => [...prev, data]);
    } catch (error) {
      // 관리자 페이지만의 에러 처리
      navigate('/admin/error');
    }
  };
};
```

## 실무 적용: 구체적인 구조

### 백엔드 구조 (Spring Boot)

```java
// UserController.java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        // 사용자 조회 로직
        return ResponseEntity.ok(userService.getUser(id));
    }
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
        @RequestBody CreateUserRequest request
    ) {
        // 사용자 생성 로직
        return ResponseEntity.ok(userService.createUser(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
        @PathVariable Long id, 
        @RequestBody UpdateUserRequest request
    ) {
        // 사용자 수정 로직
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // 사용자 삭제 로직
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

// ProductController.java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page
    ) {
        // 상품 목록 조회 로직
        return ResponseEntity.ok(productService.getProducts(category, page));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        // 상품 상세 조회 로직
        return ResponseEntity.ok(productService.getProduct(id));
    }
}
```

### 프론트엔드 구조 (TypeScript + Axios)

#### 1. 프로젝트 폴더 구조

```
src/
├── api/
│   ├── config.ts           # Axios 설정 및 인터셉터
│   ├── types.ts            # 공통 타입 정의
│   ├── user.ts             # UserController 대응
│   ├── product.ts          # ProductController 대응
│   └── order.ts            # OrderController 대응
├── generated/              # OpenAPI 자동 생성 파일
│   └── api-types.ts        # 백엔드 스키마 기반 타입
├── components/
│   ├── UserProfile.tsx
│   └── ProductList.tsx
└── pages/
    ├── UserListPage.tsx
    └── ProductDetailPage.tsx
```

#### 2. API 설정 파일

```typescript
// api/config.ts
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 요청 인터셉터: 인증 토큰 추가
apiClient.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// 응답 인터셉터: 에러 처리 및 토큰 갱신
apiClient.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;
    
    // 401 에러 시 토큰 갱신 시도
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const { data } = await axios.post('/api/v1/auth/refresh', {
          refreshToken
        });
        
        localStorage.setItem('accessToken', data.accessToken);
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        
        return apiClient(originalRequest);
      } catch (refreshError) {
        // 토큰 갱신 실패 시 로그인 페이지로
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);
```

#### 3. 타입 정의

```typescript
// api/types.ts
export interface User {
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
}

export interface UpdateUserRequest {
  name?: string;
  email?: string;
}

export interface Product {
  id: number;
  name: string;
  price: number;
  category: string;
  stock: number;
  description: string;
}

export interface ApiResponse<T> {
  data: T;
  message: string;
  timestamp: string;
}
```

#### 4. API 함수 정의 (도메인별)

```typescript
// api/user.ts
import { apiClient } from './config';
import { User, CreateUserRequest, UpdateUserRequest, ApiResponse } from './types';

export const userApi = {
  /**
   * 사용자 조회
   * GET /api/v1/users/:id
   */
  getUser: (id: number) => 
    apiClient.get<ApiResponse<User>>(`/users/${id}`),

  /**
   * 사용자 생성
   * POST /api/v1/users
   */
  createUser: (data: CreateUserRequest) => 
    apiClient.post<ApiResponse<User>>('/users', data),

  /**
   * 사용자 수정
   * PUT /api/v1/users/:id
   */
  updateUser: (id: number, data: UpdateUserRequest) => 
    apiClient.put<ApiResponse<User>>(`/users/${id}`, data),

  /**
   * 사용자 삭제
   * DELETE /api/v1/users/:id
   */
  deleteUser: (id: number) => 
    apiClient.delete<void>(`/users/${id}`)
};
```

```typescript
// api/product.ts
import { apiClient } from './config';
import { Product, ApiResponse } from './types';

export const productApi = {
  /**
   * 상품 목록 조회
   * GET /api/v1/products
   */
  getProducts: (params?: { category?: string; page?: number }) => 
    apiClient.get<ApiResponse<Product[]>>('/products', { params }),

  /**
   * 상품 상세 조회
   * GET /api/v1/products/:id
   */
  getProduct: (id: number) => 
    apiClient.get<ApiResponse<Product>>(`/products/${id}`)
};
```

#### 5. 컴포넌트에서 사용

```typescript
// components/UserProfile.tsx
import React, { useEffect, useState } from 'react';
import { userApi } from '@/api/user';
import { User } from '@/api/types';

interface UserProfileProps {
  userId: number;
}

export const UserProfile: React.FC<UserProfileProps> = ({ userId }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchUser = async () => {
      setLoading(true);
      setError(null);
      
      try {
        const { data } = await userApi.getUser(userId);
        setUser(data.data);
      } catch (err) {
        setError('사용자 정보를 불러올 수 없습니다');
        console.error('Failed to fetch user:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [userId]);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!user) return null;

  return (
    <div className="user-profile">
      <h2>{user.name}</h2>
      <p>{user.email}</p>
      <span className="role">{user.role}</span>
    </div>
  );
};
```

```typescript
// pages/ProductDetailPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productApi } from '@/api/product';
import { Product } from '@/api/types';

export const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [product, setProduct] = useState<Product | null>(null);

  useEffect(() => {
    const loadProduct = async () => {
      if (!id) return;
      
      try {
        const { data } = await productApi.getProduct(Number(id));
        setProduct(data.data);
      } catch (error) {
        alert('상품을 찾을 수 없습니다');
        navigate('/products');
      }
    };

    loadProduct();
  }, [id, navigate]);

  if (!product) return <div>상품 정보를 불러오는 중...</div>;

  return (
    <div className="product-detail">
      <h1>{product.name}</h1>
      <p className="price">₩{product.price.toLocaleString()}</p>
      <p className="category">{product.category}</p>
      <p className="description">{product.description}</p>
      <p className="stock">재고: {product.stock}개</p>
    </div>
  );
};
```

### 구조 다이어그램

<div class="mermaid mermaid-center">
graph TB
    subgraph Backend["백엔드 (Spring Boot)"]
        UC[UserController]
        PC[ProductController]
        OC[OrderController]
    end
    
    subgraph Frontend["프론트엔드 (TypeScript)"]
        subgraph API["API 레이어"]
            CONFIG[config.ts<br/>Axios 설정]
            TYPES[types.ts<br/>타입 정의]
            USER_API[user.ts<br/>UserController 대응]
            PRODUCT_API[product.ts<br/>ProductController 대응]
            ORDER_API[order.ts<br/>OrderController 대응]
        end
        
        subgraph Components["컴포넌트 레이어"]
            PROFILE[UserProfile.tsx]
            LIST[ProductList.tsx]
            DETAIL[OrderDetail.tsx]
        end
    end
    
    UC -.->|1:1 매핑| USER_API
    PC -.->|1:1 매핑| PRODUCT_API
    OC -.->|1:1 매핑| ORDER_API
    
    CONFIG --> USER_API
    CONFIG --> PRODUCT_API
    CONFIG --> ORDER_API
    
    TYPES --> USER_API
    TYPES --> PRODUCT_API
    TYPES --> ORDER_API
    
    USER_API --> PROFILE
    PRODUCT_API --> LIST
    ORDER_API --> DETAIL
    
    style Backend fill:#1a202c,stroke:#2d3748,color:#e2e8f0
    style Frontend fill:#1a202c,stroke:#2d3748,color:#e2e8f0
    style API fill:#2d3748,stroke:#4a5568,color:#e2e8f0
    style Components fill:#2d3748,stroke:#4a5568,color:#e2e8f0
    style UC fill:#2c5282,stroke:#3182ce,color:#bee3f8
    style PC fill:#2c5282,stroke:#3182ce,color:#bee3f8
    style OC fill:#2c5282,stroke:#3182ce,color:#bee3f8
    style USER_API fill:#2c5282,stroke:#3182ce,color:#bee3f8
    style PRODUCT_API fill:#2c5282,stroke:#3182ce,color:#bee3f8
    style ORDER_API fill:#2c5282,stroke:#3182ce,color:#bee3f8
</div>

## 게임 체인저: OpenAPI 자동 생성

수동으로 타입을 정의하는 것도 좋지만, **백엔드 스키마에서 자동으로 TypeScript 타입을 생성**하면 두 가지 이점이 있습니다:

1. **중복 작업 제거**: 백엔드 개발자가 API 스펙을 정의하면, 프론트엔드는 타입만 생성하면 됨
2. **항상 동기화**: 백엔드 스펙이 변경되면 타입을 재생성하여 즉시 컴파일 에러로 감지

### OpenAPI Generator 설정

#### 1. 설치

```bash
npm install --save-dev @openapitools/openapi-generator-cli
```

#### 2. 설정 파일 생성

```json
// package.json
{
  "scripts": {
    "generate:api": "openapi-generator-cli generate -i http://localhost:8080/v3/api-docs -g typescript-axios -o ./src/generated --additional-properties=withSeparateModelsAndApi=true,supportsES6=true"
  }
}
```

**참고:** OpenAPI Generator는 OpenAPI 스펙에서 클라이언트 코드와 타입을 자동 생성합니다. 자세한 옵션은 [OpenAPI Generator 공식 문서](https://github.com/openapitools/openapi-generator)를 참조하세요.

#### 3. 타입 생성

```bash
npm run generate:api
```

생성된 파일:
```
src/generated/
├── api.ts          # API 함수들
├── models.ts       # 타입 정의들
└── configuration.ts # 설정
```

#### 4. 생성된 타입 사용

```typescript
// api/user.ts
import { apiClient } from './config';
import { User, CreateUserRequest, UpdateUserRequest } from '@/generated/models';

export const userApi = {
  getUser: (id: number) => 
    apiClient.get<User>(`/users/${id}`),

  createUser: (data: CreateUserRequest) => 
    apiClient.post<User>('/users', data),

  updateUser: (id: number, data: UpdateUserRequest) => 
    apiClient.put<User>(`/users/${id}`, data),

  deleteUser: (id: number) => 
    apiClient.delete<void>(`/users/${id}`)
};
```

### 타입 동기화 자동화

백엔드 API가 변경되면 자동으로 타입을 업데이트하도록 CI/CD에 통합할 수 있습니다.

```yaml
# .github/workflows/sync-api-types.yml
name: Sync API Types

on:
  schedule:
    - cron: '0 0 * * *'  # 매일 자정
  workflow_dispatch:      # 수동 실행 가능

jobs:
  sync:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Generate API types
        run: npm run generate:api
      
      - name: Check for changes
        id: git-check
        run: |
          git diff --exit-code || echo "changed=true" >> $GITHUB_OUTPUT
      
      - name: Create Pull Request
        if: steps.git-check.outputs.changed == 'true'
        uses: peter-evans/create-pull-request@v5
        with:
          commit-message: 'chore: Update API types from backend'
          title: '🔄 API 타입 자동 업데이트'
          body: '백엔드 스키마 변경사항을 반영한 타입 업데이트입니다.'
          branch: auto-update-api-types
```

이렇게 하면 백엔드 API가 변경될 때마다 자동으로 PR이 생성되어, 프론트엔드 팀이 변경사항을 리뷰하고 머지할 수 있습니다.

### 장점 정리

<div class="mermaid mermaid-center">
graph LR
    A[백엔드 API 스펙<br/>OpenAPI/Swagger] -->|자동 생성| B[TypeScript 타입]
    B --> C[컴파일 타임<br/>타입 체크]
    C --> D[런타임 에러 방지]
    
    A -->|변경 발생| E[타입 재생성]
    E -->|컴파일 에러| F[즉시 감지]
    F --> G[안전한 리팩토링]
    
    style A fill:#1a365d,stroke:#2c5282,color:#bee3f8
    style B fill:#1a365d,stroke:#2c5282,color:#bee3f8
    style C fill:#1a365d,stroke:#2c5282,color:#bee3f8
    style D fill:#22543d,stroke:#2f855a,color:#c6f6d5
    style E fill:#742a2a,stroke:#9b2c2c,color:#fed7d7
    style F fill:#742a2a,stroke:#9b2c2c,color:#fed7d7
    style G fill:#22543d,stroke:#2f855a,color:#c6f6d5
</div>

## 실전 팁과 주의사항

### 1. 환경별 API URL 관리

```typescript
// api/config.ts
const getBaseURL = () => {
  const env = import.meta.env.MODE;
  
  switch (env) {
    case 'production':
      return 'https://api.example.com/v1';
    case 'staging':
      return 'https://staging-api.example.com/v1';
    case 'development':
    default:
      return 'http://localhost:8080/api/v1';
  }
};

export const apiClient = axios.create({
  baseURL: getBaseURL(),
  timeout: 10000
});
```

### 2. API 버전 관리

```typescript
// api/config.ts
export const createApiClient = (version: 'v1' | 'v2' = 'v1') => {
  return axios.create({
    baseURL: `/api/${version}`,
    timeout: 10000
  });
};

// api/user.ts
import { createApiClient } from './config';

const apiV1 = createApiClient('v1');
const apiV2 = createApiClient('v2');

export const userApi = {
  // 레거시 API
  getUserV1: (id: number) => apiV1.get(`/users/${id}`),
  
  // 새로운 API
  getUserV2: (id: number) => apiV2.get(`/members/${id}`)
};
```

### 3. 타입 가드 활용

```typescript
// api/types.ts
export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, any>;
}

export function isApiError(error: any): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'code' in error &&
    'message' in error
  );
}

// 컴포넌트에서 사용
try {
  const { data } = await userApi.getUser(userId);
  setUser(data);
} catch (error) {
  if (isApiError(error)) {
    console.error(`API Error [${error.code}]: ${error.message}`);
  } else {
    console.error('Unknown error:', error);
  }
}
```

### 4. 주의사항

⚠️ **과도한 추상화는 피하기**
- API 레이어는 단순하게 유지
- 비즈니스 로직을 API 레이어에 넣지 말 것

⚠️ **타입 생성 시점 명확히 하기**
- 개발 서버 시작 시 자동 생성?
- PR 올릴 때 생성?
- 수동으로 필요할 때만?
- 팀과 합의된 규칙 정하기

⚠️ **생성된 코드는 수정하지 않기**
- `generated/` 폴더는 `.gitignore`에 추가하거나
- 버전 관리는 하되 직접 수정 금지
- 수정이 필요하면 wrapper 함수 사용

## 마치며

프론트엔드 API 관리는 단순히 코드를 정리하는 것을 넘어, **팀 전체의 생산성과 코드 품질을 결정하는 핵심 요소**입니다.

이 글에서 소개한 전략을 요약하면:

✅ **백엔드 API 1개 = 프론트엔드 함수 1개** (Single Source of Truth)  
✅ **백엔드 Controller 구조와 1:1 매핑** (일관성과 찾기 쉬움)  
✅ **라이브러리 의존성 격리** (유연한 교체 가능)  
✅ **컴포넌트에서 비즈니스 로직 처리** (각자의 맥락에 맞게)  
✅ **OpenAPI 자동 생성** (중복 작업 제거, 항상 동기화)

이 구조를 도입하면:
- API 엔드포인트 변경 시 한 곳만 수정
- 백엔드 스펙 변경 시 컴파일 에러로 즉시 감지
- 새로운 팀원도 API 구조를 빠르게 파악
- 백엔드 팀과의 소통이 원활해짐

여러분의 프로젝트에는 어떤 API 관리 전략을 사용하고 계신가요? 이 글에서 소개한 방법을 적용해보시거나, 더 나은 방법이 있다면 댓글로 공유해주세요!
