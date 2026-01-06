---
title: "[Copilot] Github Copilot 사용자 정의 지침 정리"

tagline: "Github Copilot을 효과적으로 활용하기 위한 사용자 정의 지침"

header:
  overlay_image: /assets/post/copilot/overlay.jpg
  overlay_filter: 0.5

categories:
  - Copilot

tags:
  - copilot
  - github
  - 설정
  - 생산성
  - 업무효율

toc: true
show_date: true

last_modified_at: 2025-06-24T18:19:00
---

## 들어가며
최근에 Github Copilot을 사용하면서, 사용자 정의 지침(Custom Instructions)을 설정하는 것이, 개발 생산성을 크게 향상시킬 수 있다는 것을 깨달았다. 이 글에서는 Copilot의 사용자 정의 지침을 설정하는 방법과 그 효과를 정리해보려 한다.

해당 문서는 아래의 링크를 참고하여 작성하였다.
- [GitHub Copilot 사용자 정의 지침](https://code.visualstudio.com/docs/copilot/copilot-customization#_create-an-instructions-file)


---

현재 `2025-06-24`일 문서에서 제안하는 설정방법은 크게 3가지로 나뉜다.

1. **.github/copilot-instructions.md** 파일을 생성하여 사용자 정의 지침을 설정하는 방법
2. **.instructions.md** 파일을 생성하여 사용자 정의 지침을 설정하는 방법
3. **.github/prompts**를 사용하여 사용자 정의 지침을 설정하는 방법

---

## **.github/copilot-instructions.md** 설정 방법

사용자 지침을 `.github/copilot-instructions.md` 파일에 작성하여 Copilot의 동작 방식을 조정할 수 있다. 이 파일은 다음과 같은 특징을 가진다:

- 모든 코드 생성 지침은 `markdown` 형식으로 작성된 하나의 파일에 통합되어 있으며, 워크스페이스 내에 저장된다.
- 이 지침 파일은 모든 채팅 요청에 자동으로 포함됩니다.
- `Copilot`을 지원하는 모든 에디터 및 IDE에서 적용됩니다.
- 이 파일을 통해 다음과 같은 내용을 정의할 수 있습니다:
  - 일반적인 코딩 관례
  - 선호하는 기술스택
  - 프로젝트 전반에 적용되는 요구사항

---

## **.instructions.md** 설정 방법

특정 작업에 맞는 맞춤형 코드 생성 지침을 설정하려면 `.instructions.md` 파일을 사용한다. 이 파일은 다음과 같은 특징을 가진다:

- Markdown 파일에 코드 생성 규칙(스타일, 제한, 선호 등)을 작성한다.
- .instructions.md 파일은 하나 이상 만들 수 있고, 워크스페이스(프로젝트 폴더) 또는 사용자 프로필(전역 설정)에 저장할 수 있다.
- `*.ts`, `**/*.controller.ts` 같은 glob 패턴을 이용해 전체 파일 또는 특정 파일에만 지침을 자동 적용할 수 있다.
- 이 기능은 VS Code에서 지원됩니다. (현재 GitHub Copilot 채팅 포함)
- 특정 작업(task)별로 코드 생성 규칙을 정의하고, 어떤 지침을 어떤 요청에 포함할지 더 세밀하게 제어하고 싶다면 이 파일을 사용한다.

### 사용 예시 
{% highlight markdown linenos %}
---
description: 이 지침 파일에 대한 간단한 설명
applyTo: "*.js"  ← 자동 적용할 파일 경로 패턴 (glob)
---
{% endhighlight %}

### **.instructions.md** 파일 직접 설정하기

`settings.json`을 통해 GitHub Copilot Chat의 다양한 작업(코드 생성, 테스트 작성 등)에 대해
맞춤형 지침(custom instructions) 을 지정할 수 있다.

지침 설정 방식 2가지

- `text`: 지침 내용을 직접 문자열로 설정
- `file`: 외부 .md 파일 경로 지정


### 예시
```json
{
  "github.copilot.chat.codeGeneration.instructions": [
    {
      "text": "항상 주석으로 코파일럿이 생성한 코드임을 명시하세요."
    },
    {
      "text": "TypeScript에서는 항상 private 필드 이름에 언더스코어를 사용하세요."
    },
    {
      "file": "general.instructions.md" // import instructions from file `general.instructions.md`
    },
    {
      "file": "db.instructions.md" // import instructions from file `db.instructions.md`
    }
  ]
}
```

### custom 지침 작성 팁

- 각 지침은 하나의 문장으로, 간단하고 자족적(self-contained) 이게 좋아.
- 여러 가지 조건을 한 문장에 쓰지 말고, 지침을 나눠서 여러 줄로 작성해.

1. 지침은 짧고 명확하게 작성한다.

예시 (좋음):
- "함수는 camelCase로 작성한다."
- "JSDoc 주석을 사용한다."

예시 (나쁨):
- "함수는 camelCase로 작성하고, JSDoc 주석도 포함하며 테스트도 작성한다."

2. 외부 리소스를 지침에 참조하지 말 것

예: “Google Java Style Guide를 따르세요” ❌
이유: 외부 기준은 Copilot이 직접 접근할 수 없기 때문에, 지침은 자체 설명 가능해야 함

3. 지침을 파일별로 나눠서 관리하라
기능별, 언어별, 프레임워크별로 지침을 여러 개의 .instructions.md 파일로 분리해 두는 것이 좋음

4. 팀 또는 프로젝트 간 공유를 쉽게 하기

- 지침 파일을 프로젝트 내에 저장하고 Git으로 버전 관리하면 팀원들과 공유가 쉬워짐
- 프로젝트 간 재사용도 편리함

5. 지침 파일의 `applyTo` 속성 사용하기

- `applyTo` 속성을 사용하여 특정 파일 또는 디렉토리에 지침을 적용할 수 있다.
- 예를 들어, `applyTo: '**/*.test.js'` 라고 하면 테스트 파일에만 지침이 적용됨

---

## **.github/prompts** 설정 방법

프롬프트 파일은 자주 수행하는 작업(예: 코드 생성, 보안 리뷰 등)을 위해 재사용 가능한 프롬프트(지시문) 를 정의한 Markdown 파일이다.

VS Code는 프롬프트 파일에 대해 두 가지 유형의 범위를 지원한다.

- **작업공간 프롬프트 파일**: 작업 공간 내에서만 사용할 수 있으며 .github/prompts 폴더에 저장된다.
- **사용자 프롬프트 파일**: 여러 작업 공간에서 사용할 수 있으며 현재 VS Code 프로필에 저장된다.

### 프롬프트 파일 예시
React 컴포넌트 생성 프롬프트 파일 예시:
```markdown
---
mode: 'agent'
tools: ['githubRepo', 'codebase']
description: '리엑트 컴포넌트를 생성하는 프롬프트'
---
당신의 목표는 #githubRepo contoso/react-templates에 있는 템플릿을 기반으로 새로운 React 폼 컴포넌트를 생성하는 것입니다.

폼 이름이나 필드 정보가 제공되지 않은 경우, 사용자에게 먼저 해당 정보를 요청하세요.

Requirements for the form:
- design-system/Form.md에 정의된 디자인 시스템의 폼 컴포넌트를 사용하세요.
- react-hook-form 을 사용하여 폼 상태를 관리하세요.
- 폼 데이터를 위한 TypeScript 타입을 반드시 정의하세요.
- 컴포넌트는 가능한 한 uncontrolled 방식(register 사용)을 선호합니다.
- defaultValues 를 사용하여 불필요한 리렌더링을 방지하세요.
- 유효성 검사는 yup 라이브러리를 사용하세요:
- 재사용 가능한 검증 스키마는 별도의 파일로 분리해서 관리하세요.
- TypeScript 타입을 사용하여 타입 안정성을 보장하세요.
- 사용자 친화적인 UX 중심의 검증 규칙을 정의하세요.
```

해당 내용은 실험중이라 완벽하게 구현되면 추후 업데이트하겠습니다.