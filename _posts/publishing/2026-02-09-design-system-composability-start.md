---
title: "[Publishing] 퍼블리싱 포석 잡기"

tagline: "개인 사이드 프로젝트에서 컴포넌트 중심 디자인 시스템을 빠르게 잡는 초반 방향성"

header:
  overlay_image: /assets/post/publishing/2026-02-09-design-system-composability-start/overlay.png
  overlay_filter: 0.5

categories:
  - Publishing

tags:
  - design-system
  - component
  - composability
  - publishing
  - html-css
  - frontend

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-02-09T23:40:00
---

디자인 시스템 조합성, 컴포넌트 중심으로 초반 방향성 잡기

## 도입
개인 사이드 프로젝트에서 디자인 시스템을 도입할 때 가장 어려운 지점은 “처음부터 완벽하게 하려다 멈추는 것”입니다. 이번 글은 HTML/CSS만 사용하는 1인 운영 기준으로, 디자인 시스템의 조합성을 확보하는 최소 구조와 작업 순서를 정리합니다. 목표는 빠르게 시작하되, 나중에 팀이 커져도 무너지지 않는 기본 뼈대를 만드는 것입니다.

## 토론 요약
디자인 시스템의 조합성을 높이기 위한 핵심은 레이어를 명확히 나누고, 변형과 상태를 규칙화하는 것입니다. 컴포넌트 기반으로 운영하려면 토큰, 프리미티브, 컴포넌트, 패턴의 흐름을 정해두고, 퍼블리싱 단계에서 접근성과 상태를 반드시 챙겨야 합니다.

## 심화 설명: 디자인 시스템 조합성의 레이어 구조
디자인 시스템을 조합성 있게 운영하려면 “레이어”를 나눠야 합니다. 다음 구조는 개인 프로젝트에서도 부담 없이 유지 가능한 최소 단위입니다.

<div class="mermaid mermaid-center">
%%{init: {'theme':'base','themeVariables': { 'background': '#0b0f14', 'primaryColor': '#1f2937', 'primaryTextColor': '#e5e7eb', 'secondaryColor': '#111827', 'tertiaryColor': '#0f172a', 'lineColor': '#94a3b8', 'fontSize': '14px' }}}%%
flowchart TD
  A[Design Tokens] --> B[Primitives]
  B --> C[Components]
  C --> D[Patterns]
  D --> E[Pages]
</div>

- Design Tokens: 색상, 타이포, 간격, 그림자 등 재사용 단위를 CSS 변수로 고정합니다. MDN의 CSS custom properties와 `var()`를 기준으로 토큰을 정의하는 것이 안전합니다. 공식 문서: https://developer.mozilla.org/en-US/docs/Web/CSS/--* , https://developer.mozilla.org/en-US/docs/Web/CSS/var
- Primitives: 버튼, 입력, 카드 같은 최소 UI 단위를 만들고, 상태/변형을 명시합니다.
- Components: 프리미티브를 조합한 상위 컴포넌트를 만듭니다.
- Patterns: 레이아웃/플로우 단위로 패턴을 정의합니다.

## 파일 구조 예시: reset, variables, typography, styles
HTML/CSS만 사용할 때는 파일을 레이어 기준으로 쪼개 두는 것만으로도 조합성이 크게 올라갑니다. 아래는 최소 구성 예시입니다.

```
styles/
  reset.css
  variables.css
  typography.css
  styles.css
```

### reset.css
```css
*, *::before, *::after {
  box-sizing: border-box;
}

html {
  line-height: 1.15;
  -webkit-text-size-adjust: 100%;
}

body {
  margin: 0;
}

h1, h2, h3, h4, h5, h6, p {
  margin: 0;
}

ul, ol {
  margin: 0;
  padding: 0;
  list-style: none;
}

img, svg, video, canvas {
  display: block;
  max-width: 100%;
}

button, input, textarea, select {
  font: inherit;
}

a {
  color: inherit;
  text-decoration: none;
}
```

### variables.css
```css
:root {
  --color-bg: #0b0f14;
  --color-surface: #111827;
  --color-text: #e5e7eb;
  --color-muted: #94a3b8;
  --color-accent: #22c55e;

  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-6: 24px;

  --radius-1: 6px;
  --radius-2: 10px;

  --shadow-1: 0 2px 10px rgba(0, 0, 0, 0.18);
}
```

### typography.css
```css
html {
  font-size: 16px;
}

body {
  font-family: "Pretendard", "Apple SD Gothic Neo", "Noto Sans KR", sans-serif;
  font-size: 1rem;
  line-height: 1.6;
  color: var(--color-text);
  background: var(--color-bg);
}

.t-title {
  font-size: 1.5rem;
  line-height: 1.3;
  font-weight: 700;
}

.t-body {
  font-size: 1rem;
  line-height: 1.6;
}

.t-caption {
  font-size: 0.875rem;
  line-height: 1.4;
  color: var(--color-muted);
}
```

### styles.css
```css
.btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-1);
  border: 1px solid transparent;
  background: var(--color-surface);
  color: var(--color-text);
}

.btn--primary {
  background: var(--color-accent);
  color: #0b0f14;
}

.card {
  padding: var(--space-4);
  border-radius: var(--radius-2);
  background: var(--color-surface);
  box-shadow: var(--shadow-1);
}
```

실제 프로젝트에서는 reset, variables, typography를 먼저 로드하고, 마지막에 styles를 로드하면 안전합니다.

### 토큰 설계의 최소 규칙
CSS 변수는 디자인 시스템의 조합성을 확보하는 가장 작은 단위입니다. 변수만 고정하면 컴포넌트는 얼마든지 조합할 수 있습니다.

```css
:root {
  --color-bg: #0b0f14;
  --color-surface: #111827;
  --color-text: #e5e7eb;
  --color-accent: #22c55e;

  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;

  --radius-1: 6px;
  --radius-2: 10px;
}
```

이 방식은 MDN의 CSS custom properties 문서에 정의된 방식과 동일하게 동작합니다. 공식 문서: https://developer.mozilla.org/en-US/docs/Web/CSS/--* , https://developer.mozilla.org/en-US/docs/Web/CSS/var

### 상태와 접근성: :focus-visible, prefers-reduced-motion
퍼블리싱 단계에서 조합성이 깨지는 가장 흔한 이유는 상태 규칙이 빠지는 것입니다.

- 키보드 접근성을 위해 `:focus-visible` 스타일을 반드시 포함합니다. 공식 문서: https://developer.mozilla.org/en-US/docs/Web/CSS/:focus-visible
- 모션을 줄여야 하는 사용자를 위해 `prefers-reduced-motion`을 고려합니다. 공식 문서: https://developer.mozilla.org/en-US/docs/Web/CSS/@media/prefers-reduced-motion, https://developer.mozilla.org/en-US/docs/Web/CSS/@media

```css
.button:focus-visible {
  outline: 2px solid var(--color-accent);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.001ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.001ms !important;
  }
}
```

## 실무 적용 예제: HTML/CSS만으로 조합성 있는 컴포넌트 만들기
아래는 “버튼 프리미티브 → 카드 컴포넌트”의 최소 구조 예시입니다.

```html
<button class="btn btn--primary">Save</button>
<button class="btn btn--ghost">Cancel</button>

<div class="card">
  <h3 class="card__title">Title</h3>
  <p class="card__desc">Description</p>
  <div class="card__actions">
    <button class="btn btn--primary">Action</button>
  </div>
</div>
```

```css
.btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-1);
  border: 1px solid transparent;
  background: var(--color-surface);
  color: var(--color-text);
}

.btn--primary {
  background: var(--color-accent);
  color: #0b0f14;
}

.btn--ghost {
  background: transparent;
  border-color: var(--color-text);
}

.card {
  padding: var(--space-4);
  border-radius: var(--radius-2);
  background: var(--color-surface);
  color: var(--color-text);
}
```

이 구조는 “토큰 → 프리미티브 → 컴포넌트” 흐름을 명확히 보여줍니다. 컴포넌트를 늘리더라도 동일한 규칙을 반복하기 때문에 조합성이 유지됩니다.

## 주의할 점: 퍼블리싱에서 흔히 깨지는 포인트
아래 내용은 개인 프로젝트 경험을 바탕으로 정리한 팁이며, 공식 문서 근거가 없는 경험적 가이드입니다.

- 상태 정의 누락: hover/focus/disabled/active를 한 번에 작성하지 않으면 컴포넌트가 쉽게 분열됩니다.
- 네이밍 규칙 불일치: 클래스 네임 규칙이 흔들리면 조합성이 빠르게 망가집니다.
- 레이아웃 책임 혼재: 컴포넌트 내부에서 레이아웃까지 책임지면 재사용성이 낮아집니다.
- 반응형 기준 부재: 브레이크포인트 단위가 고정되지 않으면 패턴이 무너집니다.

## 결론/팁
디자인 시스템은 “완성”이 아니라 “규칙의 최소화”에서 시작됩니다. 개인 사이드 프로젝트라면 토큰과 프리미티브까지만 고정해도 조합성은 충분히 확보할 수 있습니다. 이후 컴포넌트와 패턴을 늘리면서 규칙을 확장하면 됩니다.

마지막으로 질문 하나 드립니다. 여러분은 디자인 시스템을 만들 때 “토큰 설계”와 “컴포넌트 규칙화” 중 어디에서 가장 막히셨나요? 경험을 공유해 주세요.
