---
title: "[ Tutorial ] html 태그 및 MarkDown 사용법"

header:
  overlay_image: /assets/images/tutorial/tutorial.jpg
  overlay_filter: 0.5

categories:
  - Tutorial
  - Markup

tags:
  - html
  - markdown

toc: true
toc_label: "Table Of Contents"
show_date: true

last_modified_at: 2023-07-30T17:39:00
---

다양한 일반적인 마크업 예시를 보여줍니다.

## Header two

```
## Header two
```

### Header three

```
### Header three
```

#### Header four

```
#### Header four
```

##### Header five

```
##### Header five
```

###### Header six

```
###### Header six
```

---

## Blockquotes

줄로 된 블록인용문:

> Stay hungry. Stay foolish.

```
> Stay hungry. Stay foolish.
```

여러 줄로 된 블록인용문과 인용 출처:

> People think focus means saying yes to the thing you've got to focus on. But that's not what it means at all. It means saying no to the hundred other good ideas that there are. You have to pick carefully. I'm actually as proud of the things we haven't done as the things I have done. Innovation is saying no to 1,000 things.

```
> People think focus means saying yes to the thing you've got to focus on. But that's not what it means at all. It means saying no to the hundred other good ideas that there are. You have to pick carefully. I'm actually as proud of the things we haven't done as the things I have done. Innovation is saying no to 1,000 things.
```

<cite>Steve Jobs</cite> --- Apple Worldwide Developers' Conference, 1997
{: .small}

```
<cite>Steve Jobs</cite> --- Apple Worldwide Developers' Conference, 1997
{: .small}
```

---

## Tables

| Employee         | Salary |                                                              |
| --------         | ------ | ------------------------------------------------------------ |
| [John Doe](#)    | $1     | Because that's all Steve Jobs needed for a salary.           |
| [Jane Doe](#)    | $100K  | For all the blogging she does.                               |
| [Fred Bloggs](#) | $100M  | Pictures are worth a thousand words, right? So Jane × 1,000. |
| [Jane Bloggs](#) | $100B  | With hair like that?! Enough said.                           |

```
| Employee         | Salary |                                                              |
| --------         | ------ | ------------------------------------------------------------ |
| [John Doe](#)    | $1     | Because that's all Steve Jobs needed for a salary.           |
| [Jane Doe](#)    | $100K  | For all the blogging she does.                               |
| [Fred Bloggs](#) | $100M  | Pictures are worth a thousand words, right? So Jane × 1,000. |
| [Jane Bloggs](#) | $100B  | With hair like that?! Enough said.                           |
```

| Header1 | Header2 | Header3 |
|:--------|:-------:|--------:|
| cell1   | cell2   | cell3   |
| cell4   | cell5   | cell6   |
|-----------------------------|
| cell1   | cell2   | cell3   |
| cell4   | cell5   | cell6   |
|=============================|
| Foot1   | Foot2   | Foot3   |

```
| Header1 | Header2 | Header3 |
|:--------|:-------:|--------:|
| cell1   | cell2   | cell3   |
| cell4   | cell5   | cell6   |
|-----------------------------|
| cell1   | cell2   | cell3   |
| cell4   | cell5   | cell6   |
|=============================|
| Foot1   | Foot2   | Foot3   |
```

---

## List 사용하기

정의 목록 제목
:   정의 목록 구분.

```
정의 목록 제목
:   정의 목록 구분.
```

창업(Startup)
:   창업 기업 또는 스타트업은 반복 가능하고 확장 가능한 비즈니스 모델을 찾기 위해 설립된 회사 또는 임시적인 조직이다.

Do It Live
:   I'll let Bill O'Reilly [explain](https://www.youtube.com/watch?v=O_HyZ5aW76c "We'll Do It Live") this one.

---

## 순서 없는 목록

  * List item one 
      * List item one 
          * List item one
          * List item two
          * List item three
          * List item four
      * List item two
      * List item three
      * List item four
  * List item two
  * List item three
  * List item four

```
  * List item one 
      * List item one 
          * List item one
          * List item two
          * List item three
          * List item four
      * List item two
      * List item three
      * List item four
  * List item two
  * List item three
  * List item four
```

---

## 순서 있는 목록

  1. List item one 
      1. List item one 
          1. List item one
          2. List item two
          3. List item three
          4. List item four
      2. List item two
      3. List item three
      4. List item four
  2. List item two
  3. List item three
  4. List item four

```
  1. List item one 
      1. List item one 
          1. List item one
          2. List item two
          3. List item three
          4. List item four
      2. List item two
      3. List item three
      4. List item four
  2. List item two
  3. List item three
  4. List item four
```

---

## Forms

<form>
  <fieldset>
    <legend>Personalia:</legend>
    Name: <input type="text" size="30"><br>
    Email: <input type="text" size="30"><br>
    Date of birth: <input type="text" size="10">
  </fieldset>
</form>

```
<form>
  <fieldset>
    <legend>Personalia:</legend>
    Name: <input type="text" size="30"><br>
    Email: <input type="text" size="30"><br>
    Date of birth: <input type="text" size="10">
  </fieldset>
</form>
```

---

## 버튼
`.btn` 클래스를 적용할 때 링크를 더 강조하도록 만들어주세요.

```html
<a href="#" class="btn--success">Success Button</a>
```

[Default Button](#){: .btn}
[Primary Button](#){: .btn .btn--primary}
[Success Button](#){: .btn .btn--success}
[Warning Button](#){: .btn .btn--warning}
[Danger Button](#){: .btn .btn--danger}
[Info Button](#){: .btn .btn--info}
[Inverse Button](#){: .btn .btn--inverse}
[Light Outline Button](#){: .btn .btn--light-outline}

```markdown
[Default Button Text](#link){: .btn}
[Primary Button Text](#link){: .btn .btn--primary}
[Success Button Text](#link){: .btn .btn--success}
[Warning Button Text](#link){: .btn .btn--warning}
[Danger Button Text](#link){: .btn .btn--danger}
[Info Button Text](#link){: .btn .btn--info}
[Inverse Button](#link){: .btn .btn--inverse}
[Light Outline Button](#link){: .btn .btn--light-outline}
```

[X-Large Button](#){: .btn .btn--primary .btn--x-large}
[Large Button](#){: .btn .btn--primary .btn--large}
[Default Button](#){: .btn .btn--primary }
[Small Button](#){: .btn .btn--primary .btn--small}

```markdown
[X-Large Button](#link){: .btn .btn--primary .btn--x-large}
[Large Button](#link){: .btn .btn--primary .btn--large}
[Default Button](#link){: .btn .btn--primary }
[Small Button](#link){: .btn .btn--primary .btn--small}
```

---

## Notices

**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice}` class.
{: .notice}

```
**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice}` class.
{: .notice}
```

**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice--primary}` class.
{: .notice--primary}

```
**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice--primary}` class.
{: .notice--primary}
```

**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice--info}` class.
{: .notice--info}

```
**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice--info}` class.
{: .notice--info}
```

**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice--warning}` class.
{: .notice--warning}

```
**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice--warning}` class.
{: .notice--warning}
```

**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice--success}` class.
{: .notice--success}

```
**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice--success}` class.
{: .notice--success}
```

**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice--danger}` class.
{: .notice--danger}

```
**Watch out!** This paragraph of text has been [emphasized](#) with the `{: .notice--danger}` class.
{: .notice--danger}
```

---

## HTML Tags

### 주소 Tag
<address>
  Namdong-gu <br/>Incheon <br/>South Korea
</address>

```
<address>
  Namdong-gu <br/>Incheon <br/>South Korea
</address>
```

---

### Anchor Tag (aka. Link)

This is an example of a [link](http://apple.com "Apple").

```
This is an example of a [link](http://apple.com "Apple").
```

---

### 설명 문구 Tag

The abbreviation CSS stands for "Cascading Style Sheets".

*[CSS]: Cascading Style Sheets

```
*[CSS]: Cascading Style Sheets
```

---

### 사이트 Tag

"Code is poetry." ---<cite>Automattic</cite>

```
"Code is poetry." ---<cite>Automattic</cite>
```

---

### 코드 Tag

You will learn later on in these tests that `word-wrap: break-word;` will be your best friend.

```
You will learn later on in these tests that `word-wrap: break-word;` will be your best friend.
```

---

### Strike Tag

This tag will let you <strike>strikeout text</strike>.

```
This tag will let you <strike>strikeout text</strike>.
```

---

### Emphasize Tag

The emphasize tag should _italicize_ text.

```
The emphasize tag should _italicize_ text.
```

---

### 밑줄 Tag

This tag should denote <ins>inserted</ins> text.

```
This tag should denote <ins>inserted</ins> text.
```

---

### Keyboard Tag

This scarcely known tag emulates <kbd>keyboard text</kbd>, which is usually styled like the `<code>` tag.

```
This scarcely known tag emulates <kbd>keyboard text</kbd>, which is usually styled like the `<code>` tag.
```

---

### Preformatted Tag

This tag styles large blocks of code.

<pre>
.post-title {
	margin: 0 0 5px;
	font-weight: bold;
	font-size: 38px;
	line-height: 1.2;
	and here's a line of some really, really, really, really long text, just to see how the PRE tag handles it and to find out how it overflows;
}
</pre>

```
<pre>
.post-title {
	margin: 0 0 5px;
	font-weight: bold;
	font-size: 38px;
	line-height: 1.2;
	and here's a line of some really, really, really, really long text, just to see how the PRE tag handles it and to find out how it overflows;
}
</pre>
```

---

### Quote Tag

<q>Developers, developers, developers&#8230;</q> &#8211;Steve Ballmer

```
<q>Developers, developers, developers&#8230;</q> &#8211;Steve Ballmer
```

---

### Strong Tag

This tag shows **bold text**.

```
This tag shows **bold text**.
```

---

### Subscript Tag

Getting our science styling on with H<sub>2</sub>O, which should push the "2" down.

```
Getting our science styling on with H<sub>2</sub>O, which should push the "2" down.
```

---

### Superscript Tag

Still sticking with science and Albert Einstein's E = MC<sup>2</sup>, which should lift the 2 up.

```
Still sticking with science and Albert Einstein's E = MC<sup>2</sup>, which should lift the 2 up.
```

---

### Variable Tag

This allows you to denote <var>variables</var>.
```
This allows you to denote <var>variables</var>.
```