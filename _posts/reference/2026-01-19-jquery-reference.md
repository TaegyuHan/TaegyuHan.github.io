---
title: "[Reference] jQuery 명령어 및 사용법 정리"

tagline: "jQuery의 필수 API와 폼 입력→JSON 변환 패턴"

header:
  overlay_image: /assets/post/reference/2026-01-19-jquery-reference/overlay.png
  overlay_filter: 0.5

categories:
  - Reference

tags:
  - jQuery
  - Reference
  - CheatSheet
  - 명령어
  - 단축키
  - 가이드

toc: true
show_date: true

last_modified_at: 2026-01-19T11:10:00+09:00
---

jQuery는 DOM 선택/조작, 이벤트 처리, Ajax를 간결하게 제공하는 JavaScript 라이브러리입니다. 실무에서 폼 입력값을 빠르게 수집하고 JSON 객체로 변환하는 데 유용한 메서드(`.val()`, `.prop()`, `.attr()`, `.serialize()`, `.serializeArray()`)를 중심으로 정리합니다.

# 기본 명령어

| 명령어 | 설명 | 예제 |
|--------|------|------|
| `$()` | CSS 선택자로 DOM을 선택 | `$("#formId")` |
| `.val()` | 입력/선택 요소의 값 읽기/설정 | `$("input[name=username]").val()` |
| `.prop()` | `checked`/`disabled` 등 DOM 속성(Boolean)을 읽기/설정 | `$("#agree").prop("checked", true)` |
| `.attr()` | HTML 속성 읽기/설정 | `$("a").attr("href", "/home")` |
| `:checked` | 체크된 라디오/체크박스/옵션 선택 | `$("input[name=role]:checked")` |
| `.serialize()` | 폼 데이터를 URL-encoded 문자열로 직렬화 | `$("form").serialize()` |
| `.serializeArray()` | 폼 `name`-`value` 배열로 직렬화 | `$("form").serializeArray()` |
| `$.each()` | 배열/객체/jQuery 컬렉션 순회 | `$.each(arr, (i, v) => { ... })` |
| `$.map()` | 배열/컬렉션 변환 | `$.map(arr, v => v * 2)` |
| `.on()` | 이벤트 바인딩/위임 | `$("form").on("submit", fn)` |
| `$.ajax()` | Ajax 요청 | `$.ajax({ url, method, data })` |

## 사용 예제

```html
<!-- 테스트 폼 -->
<form id="userForm">
  <input type="text" name="username" value="taegyu" />
  <input type="number" name="age" value="29" />

  <!-- 체크박스 그룹 (다중값) -->
  <label><input type="checkbox" name="skills" value="js" checked /> JS</label>
  <label><input type="checkbox" name="skills" value="java" /> Java</label>
  <label><input type="checkbox" name="skills" value="sql" checked /> SQL</label>

  <!-- 라디오 그룹 (단일값) -->
  <label><input type="radio" name="role" value="admin" /> Admin</label>
  <label><input type="radio" name="role" value="user" checked /> User</label>

  <!-- select (single/multiple) -->
  <select name="dept">
    <option value="dev" selected>Dev</option>
    <option value="ops">Ops</option>
  </select>

  <select name="tags" multiple>
    <option value="A" selected>A</option>
    <option value="B">B</option>
    <option value="C" selected>C</option>
  </select>

  <textarea name="memo"> hello world </textarea>

  <!-- 파일 입력은 serialize에 포함되지 않음 -->
  <input type="file" name="avatar" />

  <!-- disabled 필드는 serialize에 포함되지 않음 -->
  <input type="text" name="hiddenInfo" value="secret" disabled />
</form>
```

```javascript
// 1) serializeArray로 name-value 배열 → JSON 객체 만들기
function formToJSON($form) {
  const obj = {};
  $form.serializeArray().forEach(({ name, value }) => {
    const v = typeof value === 'string' ? value.trim() : value;
    if (obj[name] !== undefined) {
      if (!Array.isArray(obj[name])) obj[name] = [obj[name]];
      obj[name].push(v);
    } else {
      obj[name] = v;
    }
  });
  return obj;
}

const data1 = formToJSON($("#userForm"));
console.log("data1", data1);
// 출력 예시:
// {
//   username: "taegyu",
//   age: "29",
//   skills: ["js", "sql"],
//   role: "user",
//   dept: "dev",
//   tags: ["A", "C"],
//   memo: "hello world"
// }

// 2) 선택자 기반 수집: 체크박스/라디오/셀렉트 세부 제어
const data2 = {
  username: $("input[name=username]").val()?.trim() ?? null,
  age: $("input[name=age]").val(),
  skills: $("input[name=skills]:checked").map((_, el) => el.value).get(),
  role: $("input[name=role]:checked").val() ?? null,
  dept: $("select[name=dept]").val(),
  tags: $("select[name=tags]").val() || [], // multiple은 배열 or null
  memo: $("textarea[name=memo]").val()?.trim() ?? null,
};
console.log("data2", data2);
```

### 선택자 기반 수집 집중 정리 (가독성 우선)

| 입력 유형 | 선택자/패턴 | 설명 | 예제 |
|-----------|-------------|------|------|
| 텍스트 | `$("input[name=username]").val()?.trim() ?? null` | 좌우 공백 제거, 비어있으면 `null` | 사용자명 수집 |
| 숫자 | `parseFloat($("input[name=age]").val() || "") || null` | 숫자 변환 실패 시 `null` | 나이 수집 |
| 체크박스 그룹 | `$("input[name=skills]:checked").map((_, el) => el.value).get()` | 체크된 값만 배열로 수집 | 스킬 배열 |
| 라디오 그룹 | `$("input[name=role]:checked").val() ?? null` | 선택된 단일 값 수집 | 역할 수집 |
| 셀렉트(단일) | `$("select[name=dept]").val()` | 선택된 옵션 값 | 부서 수집 |
| 셀렉트(다중) | `$("select[name=tags]").val() || []` | 선택된 옵션 배열, 없으면 빈 배열 | 태그 배열 |
| 텍스트영역 | `$("textarea[name=memo]").val()?.trim() ?? null` | 공백 제거 | 메모 수집 |
| 파일 | `$("input[name=avatar]")[0]?.files?.[0] ?? null` | 첫 파일 객체 취득 | 아바타 파일 |
| 비활성 필드 | `const $el = $("[name=hiddenInfo]"); $el.prop("disabled") ? null : $el.val()` | disabled이면 제외 | 숨김정보 |

#### 유틸리티: 선언형 필드 매핑으로 수집

```javascript
// 선언형 매핑으로 각 필드의 수집 로직을 명확히
const fieldGetters = {
  username: () => $("input[name=username]").val()?.trim() ?? null,
  age: () => {
    const v = $("input[name=age]").val();
    const n = v === undefined ? NaN : parseFloat(v);
    return Number.isFinite(n) ? n : null;
  },
  skills: () => $("input[name=skills]:checked").map((_, el) => el.value).get(),
  role: () => $("input[name=role]:checked").val() ?? null,
  dept: () => $("select[name=dept]").val(),
  tags: () => $("select[name=tags]").val() || [],
  memo: () => $("textarea[name=memo]").val()?.trim() ?? null,
  avatar: () => $("input[name=avatar]")[0]?.files?.[0] ?? null,
};

function collectBySelectors(getters) {
  const entries = Object.entries(getters).map(([key, fn]) => [key, fn()]);
  return Object.fromEntries(entries);
}

const dataSelectors = collectBySelectors(fieldGetters);
console.log("dataSelectors", dataSelectors);
```

#### 필수값 검증과 에러 핸들링 패턴

```javascript
function validateRequired(obj, requiredKeys) {
  const errors = [];
  requiredKeys.forEach(k => {
    const v = obj[k];
    const emptyArray = Array.isArray(v) && v.length === 0;
    const emptyString = typeof v === 'string' && v.trim() === '';
    if (v === null || v === undefined || emptyArray || emptyString) errors.push(k);
  });
  return errors;
}

const requiredErrors = validateRequired(dataSelectors, ["username", "role", "dept"]);
if (requiredErrors.length) {
  console.warn("필수값 누락:", requiredErrors);
}
```

#### 팁

- `.val()` 반환 타입: `select[multiple]`에서 `.val()`은 배열을 반환, 단일 `select`는 문자열.
- 라디오/체크박스: `:checked`를 반드시 사용하여 선택된 요소만 대상으로 값 추출.
- 파일: 직렬화에 포함되지 않으므로 업로드는 `FormData` 사용.
- disabled: UI에서 비활성화된 필드는 보통 전송 대상이 아니므로 수집 시 제외 처리.


// 3) 값 타입 보정: 숫자/불리언으로 변환
```javascript
function coerceTypes(obj) {
  const out = {};
  $.each(obj, (key, value) => {
    const coerce = v => {
      if (v === null || v === undefined) return null;
      if (v === "") return null; // 빈 문자열 → null
      if (/^\d+$/.test(v)) return Number(v); // 정수
      if (/^\d+\.\d+$/.test(v)) return Number(v); // 실수
      if (v === "true" || v === true) return true;
      if (v === "false" || v === false) return false;
      return v;
    };
    if (Array.isArray(value)) out[key] = value.map(coerce);
    else out[key] = coerce(value);
  });
  return out;
}

const data3 = coerceTypes(data1);
console.log("data3", data3);
// 숫자/불리언으로 변환된 값 확인

// 4) 파일 포함 제출: FormData 사용 (serialize는 파일을 포함하지 않음)
$("#userForm").on("submit", function (e) {
  e.preventDefault();
  const fd = new FormData(this); // disabled 제외, 파일 포함
  // 필요 시 JSON과 FormData를 병합해 전송
  const json = formToJSON($(this));

  $.ajax({
    url: "/api/users",
    method: "POST",
    data: fd,
    processData: false, // FormData 처리
    contentType: false,
    // 헤더에 JSON 메타 전달 등 상황에 따라 추가
    // headers: { "X-JSON": JSON.stringify(json) },
    success: resp => console.log("OK", resp),
    error: err => console.error("ERR", err)
  });
});
```

# 고급 활용

- 다중 값 처리: 동일한 `name`을 가진 체크박스/멀티 셀렉트는 배열로 수집합니다.
- 비활성/숨김 필드: `.serialize()`/`.serializeArray()`는 `disabled` 필드와 체크되지 않은 체크박스/라디오를 제외합니다.
- 속성 제어: 선택/활성화 등 상태 값은 `.attr()`가 아닌 `.prop()`을 사용합니다.
- 네임 규칙: `user[name]` 같은 브래킷 표기 네임은 기본 직렬화가 평면 구조로 나오므로 서버/클라이언트에서 파싱 규칙을 맞춥니다.
- 성능: 이벤트 위임(`.on('click', '.class', handler)`)과 구체적 선택자를 사용해 불필요한 DOM 탐색을 줄입니다.

## 커스텀 직렬화(트림/타입 변환 포함)

```javascript
function formToJSONStrict($form) {
  const obj = formToJSON($form);
  return coerceTypes(obj); // 위 예제의 타입 보정 재사용
}

const strict = formToJSONStrict($("#userForm"));
console.log(strict);
```

# 실무 활용 예시

## 1. 파일 업로드 + JSON 데이터 함께 전송

FormData를 사용하여 파일과 JSON 데이터를 함께 전송하는 패턴입니다.

```html
<form id="profileForm">
  <input type="text" name="username" placeholder="사용자명" />
  <input type="email" name="email" placeholder="이메일" />
  
  <!-- 단일 파일 -->
  <label>프로필 사진:
    <input type="file" name="avatar" accept="image/*" />
  </label>
  
  <!-- 다중 파일 -->
  <label>첨부 문서:
    <input type="file" name="documents" multiple accept=".pdf,.doc,.docx" />
  </label>
  
  <button type="submit">제출</button>
</form>
```

```javascript
$("#profileForm").on("submit", function(e) {
  e.preventDefault();
  
  const $form = $(this);
  const formData = new FormData(this); // 모든 필드 포함 (파일 포함)
  
  // 추가 JSON 데이터를 FormData에 추가
  const metadata = {
    timestamp: new Date().toISOString(),
    source: "web",
    version: "1.0"
  };
  formData.append("metadata", JSON.stringify(metadata));
  
  // 또는 JSON을 별도 Blob으로 추가
  const jsonBlob = new Blob([JSON.stringify(formToJSON($form))], {
    type: "application/json"
  });
  formData.append("formData", jsonBlob);
  
  $.ajax({
    url: "/api/profile",
    method: "POST",
    data: formData,
    processData: false, // jQuery가 데이터를 처리하지 않도록
    contentType: false, // jQuery가 Content-Type을 설정하지 않도록
    xhr: function() {
      const xhr = new window.XMLHttpRequest();
      // 업로드 진행률 표시
      xhr.upload.addEventListener("progress", function(e) {
        if (e.lengthComputable) {
          const percent = Math.round((e.loaded / e.total) * 100);
          console.log(`업로드 진행률: ${percent}%`);
          $("#progress").text(`${percent}%`);
        }
      });
      return xhr;
    },
    success: function(response) {
      console.log("업로드 성공:", response);
      alert("프로필이 업데이트되었습니다!");
    },
    error: function(xhr, status, error) {
      console.error("업로드 실패:", error);
      alert("업로드 중 오류가 발생했습니다.");
    }
  });
});

// 여러 파일 정보 수집하기
function getFileInfo(inputName) {
  const files = $(`input[name="${inputName}"]`)[0]?.files || [];
  return Array.from(files).map(file => ({
    name: file.name,
    size: file.size,
    type: file.type,
    lastModified: new Date(file.lastModified)
  }));
}

const avatarInfo = getFileInfo("avatar");
const documentsInfo = getFileInfo("documents");
console.log("아바타:", avatarInfo);
console.log("문서들:", documentsInfo);
```

## 2. 동적 폼 필드 추가/제거

사용자가 필요에 따라 입력 필드를 추가하거나 제거할 수 있는 패턴입니다.

```html
<form id="dynamicForm">
  <div id="emailContainer">
    <h3>이메일 주소</h3>
    <div class="email-row">
      <input type="email" name="emails[]" placeholder="이메일 입력" />
      <button type="button" class="remove-email">삭제</button>
    </div>
  </div>
  <button type="button" id="addEmail">이메일 추가</button>
  
  <hr/>
  
  <div id="phoneContainer">
    <h3>전화번호</h3>
    <div class="phone-row">
      <select name="phoneTypes[]">
        <option value="mobile">휴대폰</option>
        <option value="home">집</option>
        <option value="work">회사</option>
      </select>
      <input type="tel" name="phoneNumbers[]" placeholder="전화번호" />
      <button type="button" class="remove-phone">삭제</button>
    </div>
  </div>
  <button type="button" id="addPhone">전화번호 추가</button>
  
  <button type="submit">제출</button>
</form>
```

```javascript
// 이메일 필드 추가
$("#addEmail").on("click", function() {
  const $row = $(`
    <div class="email-row">
      <input type="email" name="emails[]" placeholder="이메일 입력" />
      <button type="button" class="remove-email">삭제</button>
    </div>
  `);
  $("#emailContainer").append($row);
});

// 이메일 필드 삭제 (이벤트 위임)
$("#emailContainer").on("click", ".remove-email", function() {
  const $container = $("#emailContainer");
  if ($container.find(".email-row").length > 1) {
    $(this).closest(".email-row").remove();
  } else {
    alert("최소 1개의 이메일은 필요합니다.");
  }
});

// 전화번호 필드 추가
$("#addPhone").on("click", function() {
  const $row = $(`
    <div class="phone-row">
      <select name="phoneTypes[]">
        <option value="mobile">휴대폰</option>
        <option value="home">집</option>
        <option value="work">회사</option>
      </select>
      <input type="tel" name="phoneNumbers[]" placeholder="전화번호" />
      <button type="button" class="remove-phone">삭제</button>
    </div>
  `);
  $("#phoneContainer").append($row);
});

// 전화번호 필드 삭제
$("#phoneContainer").on("click", ".remove-phone", function() {
  const $container = $("#phoneContainer");
  if ($container.find(".phone-row").length > 1) {
    $(this).closest(".phone-row").remove();
  } else {
    alert("최소 1개의 전화번호는 필요합니다.");
  }
});

// 동적 필드 데이터 수집
$("#dynamicForm").on("submit", function(e) {
  e.preventDefault();
  
  const emails = $("input[name='emails[]']")
    .map((_, el) => $(el).val().trim())
    .get()
    .filter(v => v !== "");
  
  const phones = $(".phone-row").map((_, row) => ({
    type: $(row).find("select[name='phoneTypes[]']").val(),
    number: $(row).find("input[name='phoneNumbers[]']").val().trim()
  })).get().filter(p => p.number !== "");
  
  const data = { emails, phones };
  console.log("수집된 데이터:", data);
  
  // Ajax 전송
  $.ajax({
    url: "/api/contacts",
    method: "POST",
    contentType: "application/json",
    data: JSON.stringify(data),
    success: resp => console.log("저장 성공:", resp),
    error: err => console.error("저장 실패:", err)
  });
});
```

## 3. 중첩된 객체 구조로 변환

`user[profile][name]` 같은 브래킷 네이밍을 중첩 객체로 파싱합니다.

```html
<form id="nestedForm">
  <input type="text" name="user[profile][name]" value="홍길동" />
  <input type="email" name="user[profile][email]" value="hong@example.com" />
  <input type="number" name="user[profile][age]" value="30" />
  
  <input type="text" name="user[address][city]" value="서울" />
  <input type="text" name="user[address][zipcode]" value="12345" />
  
  <input type="text" name="items[0][name]" value="사과" />
  <input type="number" name="items[0][quantity]" value="5" />
  
  <input type="text" name="items[1][name]" value="바나나" />
  <input type="number" name="items[1][quantity]" value="3" />
  
  <input type="text" name="tags[]" value="fruit" />
  <input type="text" name="tags[]" value="healthy" />
</form>
```

```javascript
function parseNestedForm($form) {
  const result = {};
  
  $form.serializeArray().forEach(({ name, value }) => {
    // "user[profile][name]" → ["user", "profile", "name"]
    const keys = name.replace(/\]/g, '').split('[');
    let current = result;
    
    keys.forEach((key, index) => {
      const isLast = index === keys.length - 1;
      const isArrayKey = key === '';
      
      if (isArrayKey) {
        // 배열 케이스: tags[]
        const parentKey = keys[index - 1];
        if (!Array.isArray(current[parentKey])) {
          current[parentKey] = [];
        }
        current[parentKey].push(value);
      } else if (isLast) {
        // 마지막 키: 값 할당
        const numValue = parseFloat(value);
        current[key] = isNaN(numValue) ? value : numValue;
      } else {
        // 중간 키: 객체 또는 배열 생성
        const nextKey = keys[index + 1];
        const isNextNumeric = /^\d+$/.test(nextKey);
        
        if (!current[key]) {
          current[key] = isNextNumeric ? [] : {};
        }
        current = current[key];
      }
    });
  });
  
  return result;
}

const nested = parseNestedForm($("#nestedForm"));
console.log("중첩 객체:", nested);
/*
{
  user: {
    profile: { name: "홍길동", email: "hong@example.com", age: 30 },
    address: { city: "서울", zipcode: "12345" }
  },
  items: [
    { name: "사과", quantity: 5 },
    { name: "바나나", quantity: 3 }
  ],
  tags: ["fruit", "healthy"]
}
*/
```

## 4. 실시간 유효성 검증

입력하는 동안 실시간으로 에러 메시지를 표시합니다.

```html
<style>
.field-error { border-color: red; }
.error-message { color: red; font-size: 0.9em; display: none; }
.field-success { border-color: green; }
</style>

<form id="validationForm">
  <div class="form-group">
    <label>사용자명 (3-15자):</label>
    <input type="text" name="username" id="username" />
    <span class="error-message" id="username-error"></span>
  </div>
  
  <div class="form-group">
    <label>이메일:</label>
    <input type="email" name="email" id="email" />
    <span class="error-message" id="email-error"></span>
  </div>
  
  <div class="form-group">
    <label>비밀번호 (최소 8자, 영문+숫자):</label>
    <input type="password" name="password" id="password" />
    <span class="error-message" id="password-error"></span>
  </div>
  
  <div class="form-group">
    <label>비밀번호 확인:</label>
    <input type="password" name="passwordConfirm" id="passwordConfirm" />
    <span class="error-message" id="passwordConfirm-error"></span>
  </div>
  
  <button type="submit">회원가입</button>
</form>
```

```javascript
// 검증 규칙 정의
const validationRules = {
  username: {
    required: true,
    minLength: 3,
    maxLength: 15,
    pattern: /^[a-zA-Z0-9_]+$/,
    messages: {
      required: "사용자명은 필수입니다.",
      minLength: "사용자명은 최소 3자 이상이어야 합니다.",
      maxLength: "사용자명은 15자를 초과할 수 없습니다.",
      pattern: "사용자명은 영문, 숫자, 언더스코어만 가능합니다."
    }
  },
  email: {
    required: true,
    pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    messages: {
      required: "이메일은 필수입니다.",
      pattern: "유효한 이메일 주소를 입력하세요."
    }
  },
  password: {
    required: true,
    minLength: 8,
    pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]+$/,
    messages: {
      required: "비밀번호는 필수입니다.",
      minLength: "비밀번호는 최소 8자 이상이어야 합니다.",
      pattern: "비밀번호는 영문과 숫자를 포함해야 합니다."
    }
  },
  passwordConfirm: {
    required: true,
    matchField: "password",
    messages: {
      required: "비밀번호 확인은 필수입니다.",
      matchField: "비밀번호가 일치하지 않습니다."
    }
  }
};

// 필드 검증 함수
function validateField(fieldName, value) {
  const rules = validationRules[fieldName];
  if (!rules) return { valid: true };
  
  // required 체크
  if (rules.required && (!value || value.trim() === "")) {
    return { valid: false, message: rules.messages.required };
  }
  
  // minLength 체크
  if (rules.minLength && value.length < rules.minLength) {
    return { valid: false, message: rules.messages.minLength };
  }
  
  // maxLength 체크
  if (rules.maxLength && value.length > rules.maxLength) {
    return { valid: false, message: rules.messages.maxLength };
  }
  
  // pattern 체크
  if (rules.pattern && !rules.pattern.test(value)) {
    return { valid: false, message: rules.messages.pattern };
  }
  
  // matchField 체크
  if (rules.matchField) {
    const matchValue = $(`#${rules.matchField}`).val();
    if (value !== matchValue) {
      return { valid: false, message: rules.messages.matchField };
    }
  }
  
  return { valid: true };
}

// 실시간 검증 바인딩
Object.keys(validationRules).forEach(fieldName => {
  $(`#${fieldName}`).on("input blur", function() {
    const value = $(this).val();
    const result = validateField(fieldName, value);
    const $error = $(`#${fieldName}-error`);
    
    if (result.valid) {
      $(this).removeClass("field-error").addClass("field-success");
      $error.text("").hide();
    } else {
      $(this).removeClass("field-success").addClass("field-error");
      $error.text(result.message).show();
    }
  });
});

// 폼 제출 시 전체 검증
$("#validationForm").on("submit", function(e) {
  e.preventDefault();
  
  let isValid = true;
  const errors = [];
  
  Object.keys(validationRules).forEach(fieldName => {
    const value = $(`#${fieldName}`).val();
    const result = validateField(fieldName, value);
    
    if (!result.valid) {
      isValid = false;
      errors.push({ field: fieldName, message: result.message });
      $(`#${fieldName}`).addClass("field-error");
      $(`#${fieldName}-error`).text(result.message).show();
    }
  });
  
  if (isValid) {
    console.log("검증 통과!");
    const data = formToJSON($(this));
    // Ajax 전송
    $.ajax({
      url: "/api/register",
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify(data),
      success: resp => {
        console.log("회원가입 성공:", resp);
        alert("회원가입이 완료되었습니다!");
      },
      error: err => {
        console.error("회원가입 실패:", err);
        alert("회원가입 중 오류가 발생했습니다.");
      }
    });
  } else {
    console.log("검증 실패:", errors);
    alert("입력값을 확인해주세요.");
  }
});
```

## 5. 자동완성/검색 필터링

Ajax로 서버에서 데이터를 가져와서 select 옵션을 동적으로 생성하고 검색 필터링을 적용합니다.

```html
<form id="searchForm">
  <div class="form-group">
    <label>도시 검색:</label>
    <input type="text" id="citySearch" placeholder="도시명 입력..." />
    <select id="citySelect" name="city" size="5">
      <option value="">도시를 검색하세요</option>
    </select>
  </div>
  
  <div class="form-group">
    <label>사용자 자동완성:</label>
    <input type="text" id="userAutocomplete" name="username" placeholder="사용자명 입력..." />
    <ul id="suggestions" style="display:none; border: 1px solid #ccc;"></ul>
  </div>
  
  <button type="submit">검색</button>
</form>
```

```javascript
// 도시 데이터 (실제로는 Ajax로 가져옴)
let cities = [];

// 서버에서 도시 목록 가져오기
function loadCities() {
  $.ajax({
    url: "/api/cities",
    method: "GET",
    success: function(data) {
      cities = data; // [{id: 1, name: "서울"}, {id: 2, name: "부산"}, ...]
      populateCitySelect(cities);
    },
    error: function(err) {
      console.error("도시 로딩 실패:", err);
    }
  });
}

// select에 옵션 채우기
function populateCitySelect(cityList) {
  const $select = $("#citySelect");
  $select.empty().append('<option value="">선택하세요</option>');
  
  cityList.forEach(city => {
    $select.append(`<option value="${city.id}">${city.name}</option>`);
  });
}

// 도시 검색 필터링
$("#citySearch").on("input", function() {
  const query = $(this).val().toLowerCase().trim();
  
  if (query === "") {
    populateCitySelect(cities);
    return;
  }
  
  const filtered = cities.filter(city => 
    city.name.toLowerCase().includes(query)
  );
  
  populateCitySelect(filtered);
});

// 페이지 로드 시 도시 목록 가져오기
$(document).ready(function() {
  loadCities();
});

// 사용자 자동완성 (디바운스 적용)
let autocompleteTimer;

$("#userAutocomplete").on("input", function() {
  const query = $(this).val().trim();
  
  clearTimeout(autocompleteTimer);
  
  if (query.length < 2) {
    $("#suggestions").hide();
    return;
  }
  
  // 디바운스: 300ms 후 검색
  autocompleteTimer = setTimeout(() => {
    $.ajax({
      url: "/api/users/search",
      method: "GET",
      data: { q: query },
      success: function(users) {
        const $suggestions = $("#suggestions");
        $suggestions.empty();
        
        if (users.length === 0) {
          $suggestions.html('<li style="padding: 5px;">결과 없음</li>').show();
          return;
        }
        
        users.forEach(user => {
          const $li = $(`<li style="padding: 5px; cursor: pointer;">${user.username} (${user.email})</li>`);
          $li.on("click", function() {
            $("#userAutocomplete").val(user.username);
            $suggestions.hide();
          });
          $suggestions.append($li);
        });
        
        $suggestions.show();
      },
      error: function(err) {
        console.error("자동완성 실패:", err);
      }
    });
  }, 300);
});

// 외부 클릭 시 자동완성 숨기기
$(document).on("click", function(e) {
  if (!$(e.target).closest("#userAutocomplete, #suggestions").length) {
    $("#suggestions").hide();
  }
});
```

## 6. 폼 상태 관리

로딩 중 상태 표시, 버튼 비활성화, 제출 후 폼 리셋 및 성공 메시지를 관리합니다.

```html
<style>
.loading { opacity: 0.5; pointer-events: none; }
.spinner { display: inline-block; width: 20px; height: 20px; 
           border: 3px solid #f3f3f3; border-top: 3px solid #3498db;
           border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.success-message { color: green; padding: 10px; background: #d4edda; 
                  border: 1px solid #c3e6cb; border-radius: 4px; display: none; }
</style>

<form id="stateForm">
  <input type="text" name="title" placeholder="제목" required />
  <textarea name="content" placeholder="내용" required></textarea>
  
  <button type="submit" id="submitBtn">
    <span class="btn-text">제출</span>
    <span class="spinner" style="display:none;"></span>
  </button>
  
  <button type="button" id="resetBtn">초기화</button>
  
  <div class="success-message" id="successMsg"></div>
</form>
```

```javascript
// 폼 상태 관리 클래스
class FormStateManager {
  constructor(formId) {
    this.$form = $(`#${formId}`);
    this.$submitBtn = this.$form.find('[type="submit"]');
    this.$spinner = this.$submitBtn.find('.spinner');
    this.$btnText = this.$submitBtn.find('.btn-text');
  }
  
  // 로딩 시작
  startLoading() {
    this.$form.addClass('loading');
    this.$submitBtn.prop('disabled', true);
    this.$btnText.text('처리중...');
    this.$spinner.show();
  }
  
  // 로딩 종료
  stopLoading() {
    this.$form.removeClass('loading');
    this.$submitBtn.prop('disabled', false);
    this.$btnText.text('제출');
    this.$spinner.hide();
  }
  
  // 성공 메시지 표시
  showSuccess(message, duration = 3000) {
    const $msg = $('#successMsg');
    $msg.text(message).fadeIn();
    setTimeout(() => $msg.fadeOut(), duration);
  }
  
  // 폼 리셋
  reset() {
    this.$form[0].reset();
  }
  
  // 필드 비활성화
  disableFields(fieldNames) {
    fieldNames.forEach(name => {
      this.$form.find(`[name="${name}"]`).prop('disabled', true);
    });
  }
  
  // 필드 활성화
  enableFields(fieldNames) {
    fieldNames.forEach(name => {
      this.$form.find(`[name="${name}"]`).prop('disabled', false);
    });
  }
}

const formManager = new FormStateManager('stateForm');

$("#stateForm").on("submit", function(e) {
  e.preventDefault();
  
  formManager.startLoading();
  
  const data = formToJSON($(this));
  
  $.ajax({
    url: "/api/posts",
    method: "POST",
    contentType: "application/json",
    data: JSON.stringify(data),
    success: function(response) {
      console.log("성공:", response);
      formManager.showSuccess("게시글이 등록되었습니다!");
      formManager.reset();
    },
    error: function(xhr, status, error) {
      console.error("실패:", error);
      alert("오류가 발생했습니다: " + (xhr.responseJSON?.message || error));
    },
    complete: function() {
      formManager.stopLoading();
    }
  });
});

// 초기화 버튼
$("#resetBtn").on("click", function() {
  if (confirm("입력한 내용을 모두 지우시겠습니까?")) {
    formManager.reset();
  }
});
```

## 7. 로컬스토리지 연동

작성 중인 폼 데이터를 자동 저장하고 페이지 재방문 시 복원합니다.

```html
<form id="draftForm">
  <input type="text" name="title" placeholder="제목" />
  <textarea name="content" placeholder="내용을 입력하세요..." rows="5"></textarea>
  <input type="email" name="email" placeholder="이메일" />
  
  <div>
    <button type="submit">제출</button>
    <button type="button" id="clearDraft">임시저장 삭제</button>
    <span id="autoSaveStatus"></span>
  </div>
</form>
```

```javascript
// 로컬스토리지 키
const DRAFT_KEY = "form_draft_data";
const DRAFT_TIMESTAMP_KEY = "form_draft_timestamp";

// 폼 데이터 저장
function saveDraft($form) {
  const data = formToJSON($form);
  const timestamp = new Date().toISOString();
  
  try {
    localStorage.setItem(DRAFT_KEY, JSON.stringify(data));
    localStorage.setItem(DRAFT_TIMESTAMP_KEY, timestamp);
    
    $("#autoSaveStatus").text("✓ 자동저장됨").css("color", "green");
    setTimeout(() => $("#autoSaveStatus").text(""), 2000);
    
    return true;
  } catch (e) {
    console.error("저장 실패:", e);
    return false;
  }
}

// 폼 데이터 불러오기
function loadDraft($form) {
  try {
    const savedData = localStorage.getItem(DRAFT_KEY);
    const timestamp = localStorage.getItem(DRAFT_TIMESTAMP_KEY);
    
    if (!savedData) return null;
    
    const data = JSON.parse(savedData);
    const savedDate = new Date(timestamp);
    
    // 저장된 데이터를 폼에 채우기
    Object.entries(data).forEach(([name, value]) => {
      const $field = $form.find(`[name="${name}"]`);
      
      if ($field.length) {
        if ($field.is(':checkbox') || $field.is(':radio')) {
          if (Array.isArray(value)) {
            value.forEach(v => {
              $field.filter(`[value="${v}"]`).prop('checked', true);
            });
          } else {
            $field.filter(`[value="${value}"]`).prop('checked', true);
          }
        } else {
          $field.val(value);
        }
      }
    });
    
    return { data, timestamp: savedDate };
  } catch (e) {
    console.error("불러오기 실패:", e);
    return null;
  }
}

// 임시저장 삭제
function clearDraft() {
  localStorage.removeItem(DRAFT_KEY);
  localStorage.removeItem(DRAFT_TIMESTAMP_KEY);
  $("#autoSaveStatus").text("임시저장 삭제됨").css("color", "orange");
  setTimeout(() => $("#autoSaveStatus").text(""), 2000);
}

// 페이지 로드 시 임시저장 복원
$(document).ready(function() {
  const $form = $("#draftForm");
  const draft = loadDraft($form);
  
  if (draft) {
    const timeAgo = Math.floor((new Date() - draft.timestamp) / 1000 / 60);
    const message = timeAgo < 1 
      ? "방금 전 임시저장된 내용을 불러왔습니다."
      : `${timeAgo}분 전 임시저장된 내용을 불러왔습니다.`;
    
    if (confirm(message + "\n계속 작성하시겠습니까?")) {
      $("#autoSaveStatus").text("✓ 임시저장 복원됨").css("color", "blue");
    } else {
      clearDraft();
      $form[0].reset();
    }
  }
});

// 자동저장 (디바운스 적용)
let autoSaveTimer;

$("#draftForm :input").on("input change", function() {
  clearTimeout(autoSaveTimer);
  
  autoSaveTimer = setTimeout(() => {
    saveDraft($("#draftForm"));
  }, 1000); // 1초 후 저장
});

// 폼 제출 시 임시저장 삭제
$("#draftForm").on("submit", function(e) {
  e.preventDefault();
  
  const data = formToJSON($(this));
  
  $.ajax({
    url: "/api/submit",
    method: "POST",
    contentType: "application/json",
    data: JSON.stringify(data),
    success: function(response) {
      console.log("제출 성공:", response);
      clearDraft(); // 제출 성공 시 임시저장 삭제
      $("#draftForm")[0].reset();
      alert("제출이 완료되었습니다!");
    },
    error: function(err) {
      console.error("제출 실패:", err);
      alert("제출 중 오류가 발생했습니다.");
    }
  });
});

// 임시저장 삭제 버튼
$("#clearDraft").on("click", function() {
  if (confirm("임시저장된 내용을 삭제하시겠습니까?")) {
    clearDraft();
    $("#draftForm")[0].reset();
  }
});

// 페이지 이탈 시 경고 (변경사항이 있을 경우)
let formModified = false;

$("#draftForm :input").on("input change", function() {
  formModified = true;
});

$("#draftForm").on("submit", function() {
  formModified = false;
});

$(window).on("beforeunload", function(e) {
  if (formModified) {
    const message = "작성 중인 내용이 있습니다. 페이지를 떠나시겠습니까?";
    e.returnValue = message;
    return message;
  }
});
```

---

# 자주 사용하는 패턴

- 체크박스 배열: `$("input[name=skills]:checked").map((_, el) => el.value).get()`
- 라디오 단일값: `$("input[name=role]:checked").val() ?? null`
- 멀티 셀렉트: `$("select[name=tags]").val() || []`
- 토글 상태: `$("#agree").prop("checked", !$("#agree").prop("checked"))`
- 제출 중 비활성화: `$("button[type=submit]").prop("disabled", true)` → 완료 후 `false`

---

# 팁 & 주의사항

- ⚠️ `.prop()` vs `.attr()`: 체크 상태, 비활성화 등은 `.prop('checked' | 'disabled')`로 관리합니다. `.attr()`는 HTML 속성 문자열을 다룹니다.
- ⚠️ 직렬화 제한: 파일 입력(`input[type=file]`), `disabled` 필드, 체크되지 않은 라디오/체크박스는 `.serialize()`/`.serializeArray()`에 포함되지 않습니다. 파일은 `FormData`를 사용하세요.
- ✅ 이벤트 위임: 동적 요소가 많은 경우 `.on('event', 'selector', handler)`로 위임 처리하면 성능과 유지보수에 유리합니다.
- ✅ JSON 파싱: `$.parseJSON`은 폐지되었습니다. `JSON.parse`를 사용하세요.

---

# 공식 문서 링크
  - `.val()`: [https://api.jquery.com/val/](https://api.jquery.com/val/)
  - `.prop()`: [https://api.jquery.com/prop/](https://api.jquery.com/prop/)
  - `.attr()`: [https://api.jquery.com/attr/](https://api.jquery.com/attr/)
  - `:checked` selector: [https://api.jquery.com/checked-selector/](https://api.jquery.com/checked-selector/)
  - `.serialize()`: [https://api.jquery.com/serialize/](https://api.jquery.com/serialize/)
  - `.serializeArray()`: [https://api.jquery.com/serializeArray/](https://api.jquery.com/serializeArray/)
  - `$.each()`: [https://api.jquery.com/jQuery.each/](https://api.jquery.com/jQuery.each/)
  - `$.map()`: [https://api.jquery.com/jQuery.map/](https://api.jquery.com/jQuery.map/)
  - `.on()`: [https://api.jquery.com/on/](https://api.jquery.com/on/)
  - `$.ajax()`: [https://api.jquery.com/jQuery.ajax/](https://api.jquery.com/jQuery.ajax/)

---


