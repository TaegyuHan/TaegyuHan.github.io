---
title: "[JavaScript] jQuery + Proxy로 구현하는 리액티브 상태 관리"

tagline: "Proxy와 jQuery의 조합으로 React/Vue처럼 상태 변경 시 자동으로 UI가 갱신되는 리액티브 패턴을 jQuery에서도 구현해보세요."

header:
  overlay_image: /assets/post/javascript/2026-01-26-jquery-proxy-reactive-state/overlay.png
  overlay_filter: 0.5

categories:
  - javascript

tags:
  - jQuery
  - Proxy
  - 상태관리
  - 리액티브
  - 데이터바인딩
  - ES6

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-26T10:00:00+09:00
---

2010년대 초반 jQuery는 웹 개발의 표준이었습니다. 하지만 시간이 지나면서 React, Vue, Angular 같은 현대적 프레임워크들이 등장했고, 그들은 **"상태가 변경되면 화면이 자동으로 업데이트된다"**는 혁명적인 개념을 제시했습니다.

그렇다면 질문이 생깁니다: **jQuery만 사용해도 이런 현대적인 상태 관리를 구현할 수 있을까요?**

답은 **YES**입니다. ES6의 **Proxy**와 jQuery의 DOM 조작 능력을 결합하면, React/Vue 수준의 리액티브 패턴을 구현할 수 있습니다.

---

## 기존 jQuery의 문제점

먼저 기존의 jQuery 방식을 살펴봅시다. 장바구니 예제로 설명하겠습니다.

### ❌ 기존 방식: 수동 업데이트

```javascript
// 상태 객체
const cart = {
  quantity: 1,
  unitPrice: 10000
};

// jQuery로 이벤트 처리
$('#quantity').on('change', function() {
  cart.quantity = $(this).val();
  
  // 총액을 일일이 계산해서 업데이트해야 함
  const total = cart.quantity * cart.unitPrice;
  $('#total').text(total.toLocaleString() + '원');
  
  // 상태가 복잡해질수록 이런 코드가 계속 증가...
  updateCart();
  recalculateDiscount();
  validateInventory();
});
```

**문제점:**
- 상태 변경 시마다 수동으로 UI 업데이트 코드 작성
- 상태가 많아질수록 코드가 복잡해짐
- UI 업데이트 로직이 여러 곳에 분산됨
- 변경점을 놓치기 쉬움

---

## React/Vue는 어떻게 다를까?

```javascript
// React 방식
const [quantity, setQuantity] = useState(1);

// quantity만 변경하면 관련된 모든 UI가 자동으로 업데이트됨
setQuantity(5);
```

```javascript
// Vue 방식
data() {
  return {
    quantity: 1
  }
}

// quantity만 변경하면 자동 업데이트
this.quantity = 5;
```

**핵심은:** 상태 변경만 신경 쓰고, UI 업데이트는 프레임워크가 자동으로 처리한다는 것입니다.

---

## 해결책: Proxy + jQuery 조합

이제 jQuery에서도 이런 방식을 구현할 수 있습니다. 핵심은 **ES6 Proxy**입니다.

### Proxy란?

Proxy는 객체 앞단에 위치하는 **"인터셉터"**입니다. 네트워크 통신의 필터/인터셉터처럼, 객체의 모든 속성 접근을 가로챌 수 있습니다.

```javascript
// 기본 Proxy 개념
const handler = {
  set(target, property, value) {
    // property를 변경하려는 모든 시도가 여기를 거쳐감
    console.log(`${property}이 ${value}로 변경됩니다!`);
    target[property] = value;
    return true;
  }
};

const state = new Proxy({count: 0}, handler);

state.count = 5;
// 콘솔 출력: "count이 5로 변경됩니다!"
```

---

## 아키텍처: 데이터 바인딩 플로우

리액티브 상태 관리의 흐름을 시각화하면 다음과 같습니다:

<div class="mermaid mermaid-center">
graph LR
    A["사용자 입력<br/>(input 변경)"] -->|jQuery 이벤트| B["상태 객체<br/>변경<br/>(state.property = value)"]
    B -->|Proxy 감지| C["Proxy Handler<br/>set() 호출"]
    C -->|업데이트 함수| D["updateBoundUI<br/>호출"]
    D -->|jQuery 선택자| E["[data-bind] 요소<br/>찾기"]
    E -->|선택적 업데이트| F["UI 갱신<br/>(필요한 부분만)"]
    
    style A fill:#2d5f8d,stroke:#4a8fbe,color:#fff
    style B fill:#2d5f8d,stroke:#4a8fbe,color:#fff
    style C fill:#5b3e7a,stroke:#8b5fb8,color:#fff
    style D fill:#5b3e7a,stroke:#8b5fb8,color:#fff
    style E fill:#6b4423,stroke:#9b6e3d,color:#fff
    style F fill:#2d6b3f,stroke:#4a9b5f,color:#fff
</div>

---

## 구현: 단계별 코드

### Step 1: HTML에 데이터 바인딩 표시

```html
<!-- data-bind 속성으로 상태와 UI 요소를 연결 -->
<div class="cart">
  <input id="quantity" type="number" value="1" data-bind="quantity">
  <span id="unitPrice" data-bind="unitPrice">10,000원</span>
  <span id="total" data-bind="total">10,000원</span>
  <button id="decreaseBtn">-</button>
  <button id="increaseBtn">+</button>
</div>
```

**핵심: `data-bind="property이름"`으로 상태와 연결**

### Step 2: UI 업데이트 함수 구현

```javascript
/**
 * Proxy가 감지한 변경사항을 UI에 반영
 * @param {string} property - 변경된 속성명
 * @param {*} value - 변경된 값
 */
function updateBoundUI(property, value) {
  // 1. data-bind="property"인 모든 요소 선택
  const $elements = $(`[data-bind="${property}"]`);
  
  // 2. 요소 타입에 따라 다르게 업데이트
  $elements.each(function() {
    const $el = $(this);
    
    // input/textarea는 val()로 업데이트
    if ($el.is('input, textarea')) {
      $el.val(value);
    }
    // 다른 요소는 text()로 업데이트
    else {
      $el.text(value);
    }
  });
}
```

### Step 3: Proxy를 이용한 상태 객체 생성

```javascript
// 초기 상태
const initialState = {
  quantity: 1,
  unitPrice: 10000,
  get total() {
    return this.quantity * this.unitPrice;
  }
};

// Proxy Handler 정의
const handler = {
  set(target, property, value) {
    // 실제 값 할당
    target[property] = value;
    
    // 변경된 속성에 해당하는 UI만 업데이트
    updateBoundUI(property, value);
    
    return true;
  }
};

// Proxy 객체 생성
const state = new Proxy(initialState, handler);
```

### Step 4: 양방향 바인딩 설정

```javascript
/**
 * 사용자 입력 이벤트를 감시하여 상태 업데이트
 */
function setupTwoWayBinding(state) {
  $('[data-bind]').on('input change', function() {
    const $el = $(this);
    const property = $el.attr('data-bind');
    const value = $el.is('input[type="number"]') 
      ? parseInt($el.val()) 
      : $el.val();
    
    // 상태 변경 -> Proxy 감지 -> updateBoundUI 호출
    state[property] = value;
  });
}

// 양방향 바인딩 초기화
setupTwoWayBinding(state);
```

### Step 5: 이벤트 핸들러 연결

```javascript
// 증가/감소 버튼
$('#increaseBtn').on('click', function() {
  state.quantity += 1;  // <- 상태만 변경하면 UI가 자동 갱신!
});

$('#decreaseBtn').on('click', function() {
  state.quantity = Math.max(1, state.quantity - 1);
});
```

---

## 완전한 동작 예제

다음은 전체 코드를 한 파일로 통합한 예제입니다:

```javascript
// ============ 설정 ============
const initialState = {
  quantity: 1,
  unitPrice: 10000,
  get total() {
    return this.quantity * this.unitPrice;
  }
};

// ============ UI 업데이트 함수 ============
function updateBoundUI(property, value) {
  const $elements = $(`[data-bind="${property}"]`);
  $elements.each(function() {
    const $el = $(this);
    if ($el.is('input, textarea')) {
      $el.val(value);
    } else {
      $el.text(value);
    }
  });
}

// ============ Proxy 생성 ============
const state = new Proxy(initialState, {
  set(target, property, value) {
    target[property] = value;
    updateBoundUI(property, value);
    return true;
  }
});

// ============ 양방향 바인딩 ============
function setupTwoWayBinding(state) {
  $('[data-bind]').on('input change', function() {
    const $el = $(this);
    const property = $el.attr('data-bind');
    const value = $el.is('input[type="number"]') 
      ? parseInt($el.val()) 
      : $el.val();
    state[property] = value;
  });
}

// ============ DOM Ready ============
$(function() {
  setupTwoWayBinding(state);
  
  $('#increaseBtn').on('click', function() {
    state.quantity += 1;
  });
  
  $('#decreaseBtn').on('click', function() {
    state.quantity = Math.max(1, state.quantity - 1);
  });
});
```

**동작 흐름:**
1. 사용자가 input에 숫자 입력 → `setupTwoWayBinding`이 감지
2. `state.quantity` 변경 → Proxy의 `set` 핸들러 호출
3. `updateBoundUI` 실행 → `[data-bind="quantity"]` 요소들만 업데이트
4. 동시에 getter `total`도 자동 계산되어 `[data-bind="total"]` 업데이트

---

## 핵심 장점

### ✅ 1. 코드 간결성
```javascript
// Before (jQuery 기존 방식)
$('#quantity').on('change', function() {
  cart.quantity = $(this).val();
  $('#total').text((cart.quantity * cart.unitPrice).toLocaleString() + '원');
  $('#display').text(cart.quantity);
});

// After (Proxy 방식)
state.quantity = 5;  // 끝! UI는 자동으로 갱신됨
```

### ✅ 2. 성능 최적화
- **전체 페이지 리렌더링 NO**
- **변경된 속성과 연결된 요소만 업데이트**
- 불필요한 DOM 조작 제거

### ✅ 3. 유지보수성
- 상태와 UI의 매핑이 명확 (`data-bind`)
- UI 업데이트 로직이 중앙화
- 새로운 바인딩 추가 시 HTML만 수정

### ✅ 4. React/Vue로의 마이그레이션 용이
- 현대적 패턴을 먼저 경험
- 프레임워크 전환 시 개념 이해가 쉬움

---

## 심화: 계산된 속성과 워처

### 계산된 속성 (Getter)

위의 예제에서 이미 사용했습니다:

```javascript
const state = {
  quantity: 1,
  unitPrice: 10000,
  get total() {
    return this.quantity * this.unitPrice;
  }
};
```

`total`은 `quantity`나 `unitPrice`가 변경될 때마다 자동으로 재계산됩니다.

### 워처: 특정 속성 변경 감시

더 복잡한 로직이 필요하면 Proxy를 확장할 수 있습니다:

```javascript
const state = new Proxy(initialState, {
  set(target, property, value) {
    const oldValue = target[property];
    target[property] = value;
    
    // 특정 속성 변경 시 추가 로직
    if (property === 'quantity') {
      if (value > 100) {
        console.warn('대량 구매: 할인 적용됨!');
        target.discount = 0.1;  // 10% 할인
      }
    }
    
    updateBoundUI(property, value);
    return true;
  }
});
```

---

## 실무 활용 사례

### 1. 실시간 폼 검증

```html
<input id="email" type="email" data-bind="email">
<span id="emailError" data-bind="emailError"></span>
```

```javascript
const state = new Proxy({
  email: '',
  emailError: ''
}, {
  set(target, property, value) {
    target[property] = value;
    
    if (property === 'email') {
      const isValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
      target.emailError = isValid ? '' : '유효한 이메일을 입력하세요';
    }
    
    updateBoundUI(property, value);
    return true;
  }
});
```

### 2. 필터링 목록

```html
<input id="searchInput" data-bind="searchTerm">
<ul id="resultList" data-bind="filteredItems"></ul>
```

```javascript
const state = new Proxy({
  items: ['Apple', 'Banana', 'Cherry'],
  searchTerm: '',
  get filteredItems() {
    return this.items.filter(item => 
      item.toLowerCase().includes(this.searchTerm.toLowerCase())
    ).join(', ');
  }
}, handler);
```

### 3. 장바구니 관리

```javascript
const cartState = new Proxy({
  items: [],
  get itemCount() {
    return this.items.length;
  },
  get totalPrice() {
    return this.items.reduce((sum, item) => sum + item.price, 0);
  },
  addItem(item) {
    this.items.push(item);
    // 이 코드는 수작업 필요 (배열 참조 문제)
  }
}, handler);
```

**⚠️ 주의:** 배열 조작은 Proxy가 자동으로 감지하지 못합니다. 객체 래퍼를 사용하거나 명시적으로 상태를 갱신해야 합니다.

---

## 주의사항 및 한계

### 1. 배열 조작 감지 문제

```javascript
// ❌ 이건 Proxy가 감지하지 못함
state.items.push(newItem);

// ✅ 이렇게 새 배열을 할당해야 감지됨
state.items = [...state.items, newItem];
```

### 2. 깊은 객체 바인딩

```javascript
const state = new Proxy({
  user: { name: 'John', age: 30 }
}, handler);

// ❌ 중첩된 속성 변경은 감지 안 됨
state.user.name = 'Jane';

// ✅ 객체 전체를 재할당해야 함
state.user = { ...state.user, name: 'Jane' };
```

### 3. 성능

매우 큰 규모의 애플리케이션에서는 React/Vue 같은 프레임워크가 더 최적화되어 있습니다. 하지만 중소 규모 프로젝트에서는 충분합니다.

---

## React/Vue와의 비교

| 항목 | jQuery + Proxy | React | Vue |
|------|---|---|---|
| 번들 크기 | jQuery + 사용자 코드 | ~40KB | ~30KB |
| 학습 곡선 | 낮음 | 중간 | 낮음 |
| 기능성 | 중간 | 높음 | 높음 |
| 성능 | 중간 | 높음 | 높음 |
| 레거시 코드 호환성 | 우수 | 낮음 | 낮음 |
| 커뮤니티 | 소형 (이 패턴) | 매우 큼 | 큼 |

---

## 결론 및 실무 팁

jQuery는 과거의 도구지만, **올바른 패턴**을 적용하면 여전히 강력합니다. **Proxy + jQuery 조합**은:

✅ **기존 jQuery 코드의 현대화**
✅ **간단한 프로젝트의 빠른 구현**
✅ **프레임워크 학습 전 좋은 디딤돌**
✅ **성능과 유지보수성의 균형**

을 제공합니다.

### 실무 적용 팁:

1. **작은 모듈부터 시작** - 전체 페이지가 아닌 특정 컴포넌트부터 적용
2. **data-bind 네이밍 규칙 수립** - 팀 내 일관성 유지
3. **복잡도 모니터링** - 상태가 너무 복잡해지면 프레임워크 도입 고려
4. **에러 핸들링 추가** - Proxy 핸들러에 try-catch 추가

---

## 당신의 생각은?

지금까지 배운 이 패턴을 실제 프로젝트에 적용해본 경험이 있나요? 아니면 처음 접하는 개념인가요?

💬 댓글로 당신의 의견을 공유해주세요!
- jQuery 프로젝트에서 이 패턴이 도움될 만한 부분이 있나요?
- 더 알고 싶은 심화 내용이 있나요?
- 다른 방식의 상태 관리 패턴에 관심 있나요?

당신의 피드백이 다음 글의 주제가 될 수 있습니다! 🚀
