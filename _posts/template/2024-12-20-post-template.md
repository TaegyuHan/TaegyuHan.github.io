---
title: "[Template] POST 문서 작성 Template 스타일 정의"

tagline: "일관성 있는 문서를 작성하기 위한 첫걸음"

header:
  overlay_image: /assets/post/template/overlay.jpg
  overlay_filter: 0.5
  
categories:
  - Template

tags:
    - post
    - Template
    - markdown

toc: true
show_date: true

last_modified_at: 2024-12-20T11:24:00
---

본 문서는 앞으로의 문서를 작성함에 있어 일관성을 유지할 수 있도록 돕기 위해 작성한 문서 이다. 아래에 정의 되지 않는 문법은 글에 작서하지 않는다.

---
# 제목

제목은 문서의 구조를 명확히 하고 가독성을 높이기 위해서 사용한다. 제목은 `h1 ~ h3`까지 사용한다.

## 규칙
- `h1 ~ h3`만 사용할 수 있다.
- `h1` 전에는 `---`을 사용하여 문장을 구분 한다.

```markdown
# h1 제목
## h2 제목
### h3 제목
```

---
# 목록

목록은 정보를 명확하고 간결하게 전달하기 위해서 사용한다. 

## 규칙
- 순서가 없는 목록은 3번까지 들여쓰기 한다.
- 순서가 있는 목록은 2번까지 들여쓰기 한다.

```markdown
- Markdown의 장점
  - 간단한 문법
  - 높은 가독성
    - 3번까지 가능하다     
  - 다양한 확장성

1. 프로젝트 시작
  1. 아이디어 구상
  2. 요구사항 분석
2. 개발 단계
  1. 설계
  2. 구현
3. 테스트 및 배포
```

---
# 강조

원하는 단어를 강조할 때 사용한다.

```markdown
원하는 글을 **강조** 한다.
```

---
# 인용

인용문은 다른사람의 말이나 글, 또는 문서의 중요한 내용을 직접 가져와 글을 작성할 때 사용한다.

## 규칙
- 인용문을 작성할 때는 반드시 출처 및 링크를 작성해야 한다.
- 링크는 인용 출처에 연결한다.
- 나의 생각을 포스트에 작성할 때도 인용 문법을 사용하여 작성한다.

```markdown
> "돈을 잃는 것은 조금 잃는 것이지만 명예를 잃는 것은 많이 잃는 것이다. 그러나 용기를 잃는 것은 전부를 잃는 것이다."
> 윈스턴 처칠
```

> "성공이란 성공하려는 의지를 절대 꺾지 않는 데 있다."  
> [윈스턴 처칠](https://ko.wikiquote.org/wiki/%EC%9C%88%EC%8A%A4%ED%84%B4_%EC%B2%98%EC%B9%A0)

---
# 이미지

블로그에 이미지를 넣을 때 사용한다.

## 규칙
이미지를

```markdown
{%
  include image-tag.html
  align="align-center"
  path="/post/template/overlay.jpg"
  caption="포스트 배경 이미지"
%}
```

{%
  include image-tag.html
  align="align-center"
  path="/post/template/overlay.jpg"
  caption="포스트 배경 이미지"
%}

---
# 코드

글에서 코드를 작성할 때 ``` 백틱 기로흘 사용하여 코드를 나타낸다.

## 규칙
- 1개: 글 속에서 코드를 작성할 때 사용한다.
- 3개: 하나의 코드 블럭을 사용하여 코드를 작성할 때 사용한다.
