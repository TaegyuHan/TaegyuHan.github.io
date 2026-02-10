---
title: "[JavaScript] 프론트엔드 API 호출의 변천사: Form Submit에서 Fetch API까지"

tagline: "30년의 웹 개발 역사 속에서 API 호출이 어떻게 진화했는지 알아보자"

header:
  overlay_image: /assets/post/javascript/2026-01-28-frontend-api-call-evolution/overlay.png
  overlay_filter: 0.5

categories:
  - Javascript

tags:
  - API
  - XMLHttpRequest
  - FetchAPI
  - Axios
  - 웹개발
  - 변천사

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-28T12:00:00+09:00
---

현대의 웹 개발자라면 당연히 사용하는 API 호출. 하지만 이 기술이 어떻게 진화해왔는지, 그리고 **왜** 현재의 형태로 정착하게 됐는지 생각해본 적 있나요?

30년이 넘는 웹 개발 역사 속에서 API 호출 방식은 단순한 기술 변화를 넘어, **웹의 근본적인 사용자 경험과 개발자 경험의 변화를 반영**하고 있습니다. 

오늘은 Form Submit의 시대부터 오늘날의 Fetch API와 Axios까지, 프론트엔드 API 호출의 흥미로운 변천사를 따라가보겠습니다.

---

## 1990년대: "페이지 새로고침이 전부였던 시대"

### 1995년 - Form Submit: 웹의 시작

```html
<form method="POST" action="/api/submit">
  <input type="text" name="username" />
  <button type="submit">전송</button>
</form>
```

초기 웹에서 유일한 데이터 전송 방식은 HTML Form Submit이었습니다.

**문제점:**
- ❌ 요청할 때마다 **전체 페이지가 새로고침**됨
- ❌ 사용자의 입력 상태가 모두 초기화됨
- ❌ 끔찍한 사용자 경험

하지만 이것이 유일한 방법이었기에, 모든 웹 개발자는 이 방식을 받아들여야 했습니다.

### 1996년 - Image Pixel Tracking: 은폐된 통신

```html
<img src="/track?event=pageView&userId=123" 
     width="1" height="1" 
     style="display:none;" />
```

개발자들은 창의적이었습니다. 눈에 보이지 않는 1×1 픽셀 이미지 태그를 이용해 백그라운드에서 몰래 데이터를 전송하는 방식을 고안해냈습니다.

**특징:**
- ✅ 페이지 새로고침 없음
- ❌ 하지만 이미지 로드 방식이라 양방향 통신 불가능
- ❌ 데이터를 받아올 수 없음

---

## 2000년대: Ajax 혁명의 시작

이 시기가 웹 개발의 진정한 변곡점이었습니다.

### 1999-2006년 - XMLHttpRequest: 게임 체인저

```javascript
// 당시 코드의 예시
var xhr = new XMLHttpRequest();
xhr.open('GET', '/api/data', true);
xhr.onload = function() {
  if (xhr.status === 200) {
    console.log(xhr.responseText);
  }
};
xhr.send();
```

**XMLHttpRequest의 등장이 변화시킨 것:**

1. **백그라운드 통신 가능**: 페이지 새로고침 없이 데이터 요청 가능
2. **양방향 통신**: 서버로부터 데이터를 받아올 수 있음
3. **사용자 경험의 급진적 개선**: Gmail, Google Maps 같은 혁신적 웹 앱 가능

하지만 **실무의 고통**은 여전했습니다:

```javascript
// XMLHttpRequest의 복잡한 에러 처리
var xhr = new XMLHttpRequest();
xhr.open('POST', '/api/submit', true);
xhr.setRequestHeader('Content-Type', 'application/json');

xhr.onreadystatechange = function() {
  if (xhr.readyState === 4) {
    if (xhr.status === 200) {
      try {
        var data = JSON.parse(xhr.responseText);
        console.log(data);
      } catch(e) {
        console.error('JSON 파싱 에러');
      }
    } else if (xhr.status === 401) {
      // 토큰 갱신 처리
    } else {
      console.error('요청 실패');
    }
  }
};

xhr.onerror = function() {
  console.error('네트워크 에러');
};

xhr.send(JSON.stringify({ name: 'John' }));
```

**XMLHttpRequest의 문제점:**
- ❌ **비직관적인 API**: `readyState` 확인 필요
- ❌ **복잡한 에러 처리**: 상태 코드별 수동 처리
- ❌ **콜백 지옥**: 중첩된 콜백으로 가독성 악화
- ❌ **개발자 경험 최악**: 간단한 요청도 보일러플레이트 코드 필요

### 2006년 - jQuery Ajax: 대중화의 시작

```javascript
// jQuery가 감싼 방식 - 훨씬 간단했음
$.ajax({
  url: '/api/data',
  type: 'GET',
  success: function(data) {
    console.log(data);
  },
  error: function() {
    console.error('요청 실패');
  }
});
```

jQuery Ajax의 등장으로:
- ✅ XMLHttpRequest의 복잡성이 추상화됨
- ✅ 대중적인 웹 개발 도구가 됨
- ✅ 프론트엔드 개발자들의 생산성 대폭 향상

2010년대까지 jQuery Ajax는 **실무의 표준**이 되었습니다.

---

## 2010년대: 다양한 통신 방식의 등장

웹이 복잡해지면서, 단순한 요청-응답 방식만으로는 부족해졌습니다.

### 2011년 - WebSocket: 실시간 통신의 필요성

```javascript
// WebSocket으로 실시간 양방향 통신
const ws = new WebSocket('ws://server.com/stream');

ws.onmessage = function(event) {
  console.log('서버로부터 메시지:', event.data);
};

ws.send('클라이언트 메시지');
```

**등장 이유:**
- 기존 HTTP의 요청-응답 구조로는 실시간 알림, 라이브 채팅 구현 어려움
- 주식 시세 같은 실시간 데이터 필요성 증가

### 2012년 - Server-Sent Events (SSE)

```javascript
// 서버에서 클라이언트로 단방향 스트림
const eventSource = new EventSource('/api/stream');

eventSource.onmessage = function(event) {
  console.log('서버로부터:', event.data);
};
```

WebSocket의 양방향보다는 단순한 **서버 → 클라이언트 단방향 푸시**가 필요한 경우가 많았습니다.

### 2014년 - Navigator.sendBeacon: 분석 데이터 전송

```javascript
// 페이지를 떠날 때도 데이터 전송 보장
window.addEventListener('beforeunload', function() {
  navigator.sendBeacon('/api/analytics', JSON.stringify({
    sessionId: '123',
    duration: 3600
  }));
});
```

사용자가 페이지를 떠나는 순간에도 **신뢰성 있게 데이터를 전송**해야 하는 분석 도구들의 요구사항에서 등장했습니다.

---

## 2015년: Fetch API의 등장 - 표준화의 시작

### Fetch API: "XMLHttpRequest를 대체하자"

```javascript
// 모던한 Promise 기반 API
fetch('/api/data')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('요청 실패:', error));
```

**Fetch API가 해결하려던 문제:**

1. **Promise 기반**: 콜백 지옥에서 탈출
2. **직관적인 인터페이스**: 하나의 함수로 명확한 의도 표현
3. **표준 API**: 라이브러리 없이 네이티브로 지원

하지만 현실은 복잡했습니다:

```javascript
// Fetch API의 숨겨진 문제 1: 에러 처리
fetch('/api/data')
  .then(response => {
    // 404, 500도 reject되지 않음!
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  })
  .catch(error => console.error(error));

// Fetch API의 숨겨진 문제 2: 요청 취소 불가능
// (AbortController는 2017년 이후에 추가됨)

// Fetch API의 숨겨진 문제 3: 타임아웃 없음
// 무한 대기할 수 있음

// Fetch API의 숨겨진 문제 4: 인터셉터 기능 없음
// 모든 요청에 토큰을 붙여야 함
fetch('/api/data', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
```

**실무에서 만난 Fetch API의 부족함:**

| 기능 | Fetch API | 필요성 |
|------|-----------|--------|
| 요청 취소 | ❌ (나중에 AbortController 추가) | ✅ 높음 - 중복 요청 방지 |
| 타임아웃 | ❌ | ✅ 높음 - 무한 대기 방지 |
| 인터셉터 | ❌ | ✅ 높음 - 글로벌 에러 처리 |
| 에러 처리 | 반직관적 | ✅ 높음 - 명확한 실패 판단 |
| 요청 재시도 | 수동 구현 필요 | ✅ 중간 |

---

## 2016년: Axios의 등장 - 실무의 표준

```javascript
// Axios의 우아함
axios.get('/api/data')
  .then(response => console.log(response.data))
  .catch(error => console.error(error.message));

// 요청 취소
const source = axios.CancelToken.source();
axios.get('/api/data', { cancelToken: source.token });
source.cancel('요청 취소됨');

// 타임아웃
axios.get('/api/data', { timeout: 5000 });

// 인터셉터 - Fetch API에서는 이걸 직접 구현해야 함
axios.interceptors.request.use(
  config => {
    config.headers.Authorization = `Bearer ${token}`;
    return config;
  }
);

axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // 토큰 갱신 처리
    }
    return Promise.reject(error);
  }
);
```

**Axios가 Fetch API를 대체하게 된 이유:**

1. **배터리 포함 솔루션**: 실무에 필요한 모든 기능이 포함됨
2. **일관된 에러 처리**: 모든 에러를 Promise reject로 통일
3. **인터셉터**: 글로벌 로직 처리 용이
4. **요청 취소**: CancelToken으로 깔끔하게 처리
5. **개발자 경험**: 직관적인 API 설계

---

## 변화의 흐름을 한눈에 보기

<div class="mermaid mermaid-center">
graph LR
    A["1995<br/>Form Submit<br/>❌ 페이지 새로고침"] -->|"문제 해결"| B["1996<br/>Image Pixel<br/>⚠️ 편향통신만"]
    B -->|"혁명"| C["1999<br/>XMLHttpRequest<br/>⭐ Ajax 시작"]
    C -->|"복잡함"| D["2006<br/>jQuery Ajax<br/>✅ 대중화"]
    D -->|"필요성 증가"| E["2011-2014<br/>WebSocket/SSE<br/>📡 실시간"]
    E -->|"표준화"| F["2015<br/>Fetch API<br/>🔷 Promise 기반"]
    F -->|"부족함"| G["2016<br/>Axios<br/>🚀 실무 표준"]
    
    style A fill:#e8f4f8,stroke:#4a7c7e,color:#000
    style B fill:#e8f4f8,stroke:#4a7c7e,color:#000
    style C fill:#d4e6f1,stroke:#2c5282,color:#fff
    style D fill:#d4e6f1,stroke:#2c5282,color:#fff
    style E fill:#f0e6d2,stroke:#6b5344,color:#000
    style F fill:#d4e6f1,stroke:#2c5282,color:#fff
    style G fill:#d4e6f1,stroke:#2c5282,color:#fff
</div>

---

## 2020년대: "Fetch + Axios 양강 구도"의 의미

현재 프론트엔드 개발 현장에서 벌어지는 일:

```javascript
// 진영 1: "Fetch API로 충분하다" - 신규 프로젝트
fetch('/api/data')
  .then(r => r.json())
  .catch(e => console.error(e));

// 진영 2: "Axios가 실무의 현실" - 기존 프로젝트
axios.get('/api/data')
  .catch(e => console.error(e));

// 진영 3: "Fetch + 커스텀 래퍼" - 절충
const api = {
  async get(url, options = {}) {
    const response = await fetch(url, {
      timeout: options.timeout || 5000,
      ...options
    });
    if (!response.ok) throw new Error(response.statusText);
    return response.json();
  }
};
```

**왜 이런 양강 구도가 생겼나?**

1. **Fetch API의 진화**: AbortController, 개선된 에러 처리로 부족함 해결
2. **TypeScript와 번들러의 발전**: 라이브러리 선택에 더 신중해짐
3. **프로젝트의 규모에 따른 선택**:
   - 간단한 프로젝트: Fetch API만으로 충분
   - 복잡한 엔터프라이즈: Axios의 DX가 중요
   - 새로운 기술을 원하는 팀: Fetch + 커스텀

---

## 실무 관점: 어떤 것을 선택해야 할까?

### 1. Fetch API를 선택하는 경우

```javascript
// ✅ 언제 적합한가?
// - 요청이 단순한 프로젝트
// - 번들 크기가 중요한 경우
// - 최신 브라우저만 지원
// - 복잡한 인터셉터 로직이 불필요

const fetchWithTimeout = (url, timeout = 5000) => {
  const controller = new AbortController();
  const id = setTimeout(() => controller.abort(), timeout);
  
  return fetch(url, { signal: controller.signal })
    .finally(() => clearTimeout(id));
};
```

### 2. Axios를 선택하는 경우

```javascript
// ✅ 언제 적합한가?
// - 엔터프라이즈급 복잡도
// - 글로벌 인터셉터가 필수
// - 기존 프로젝트 유지보수
// - 요청 재시도, 취소 등 기능 필요

axios.defaults.timeout = 5000;

// 자동 재시도
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status >= 500) {
      return retryRequest(error.config);
    }
    return Promise.reject(error);
  }
);
```

---

## 결론: 변천사가 말해주는 것

프론트엔드 API 호출의 30년 역사는 단순히 기술의 변화가 아닙니다.

**각 단계는 웹 개발이 마주한 현실적 문제를 보여줍니다:**

1. **1995**: "페이지가 자꾸 새로고침된다" → Form Submit의 한계
2. **1999**: "백그라운드에서 통신하고 싶다" → XMLHttpRequest의 탄생
3. **2006**: "코드가 너무 복잡하다" → jQuery Ajax의 추상화
4. **2011-2015**: "다양한 통신 방식이 필요하다" → WebSocket, Fetch API
5. **2016**: "실무에서 필요한 것이 빠진다" → Axios의 확산

**앞으로의 방향:**
- Fetch API는 계속 진화할 것 (AbortController, 인터셉터 표준화?)
- Axios는 안정적인 선택지로 남아있을 것
- **개발자의 판단**: 프로젝트의 규모와 복잡도에 따라 올바른 선택이 있다

---

## 당신의 선택은?

현재 진행 중인 프로젝트에서는 Fetch API와 Axios 중 어느 것을 사용하고 있나요? 그 선택의 이유는 무엇인가요? 

댓글로 공유해주시면 좋겠습니다. 실무의 다양한 경험들이 모여 더 나은 선택의 기준을 만들 수 있을 테니까요.
