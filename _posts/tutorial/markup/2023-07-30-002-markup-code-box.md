---
title: "[ Tutorial ] 코드 박스 사용 방법"

header:
  overlay_image: /assets/images/tutorial/tutorial.jpg
  overlay_filter: 0.5

categories:
  - tutorial
  - markup

tags:
  - code box
  - markup

toc: true
toc_label: "Table Of Contents"
show_date: true

last_modified_at: 2023-07-30T17:12:00
---

`GFM(GitHub Flavored Markdown)`, `Jekyll Highlight Tag`, `GitHub Gist Embed` 3가지의 코드 입력 방법을 설명합니다.

---

# GFM Code Blocks
GFM(GitHub Flavored Markdown)은 GitHub에서 사용되는 Markdown의 확장 버전으로, 코드 블록(Code Blocks)에 대해 특별한 기능과 구문 강조를 지원합니다

``` css
#container {
  float: left;
  margin: 0 -240px 0 0;
  width: 100%;
}
```

``` css
.highlight {
  margin: 0;
  padding: 1em;
  font-family: $monospace;
  font-size: $type-size-7;
  line-height: 1.8;
}
```

---

# Code Blocks in Lists
리스트에 코드 박스를 넣는 방법 입니다. 
1. list 1
2. list 2
  ``` python
    print("hello word")
  ```

---

# Jekyll Highlight Tag
Jekyll 테마에서 코드 블록을 강조 표시하고 구문 강조(Syntax Highlighting)를 적용하기 위해 사용되는 Liquid 태그입니다.
{% highlight java linenos %}

public class Main {

    private final static String TEST_PATH = "/p20364/input/3.txt";

    public static void main(String[] args) throws IOException {
        System.setIn(new FileInputStream(TREE_PATH + TEST_PATH));
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String[] data = br.readLine().split(" ");

        int size = Integer.parseInt(data[0]);
        int duckCount = Integer.parseInt(data[1]);

        Tree tree = new Tree(size);
//        System.out.println(tree);
        for (int i = 0; i < duckCount; i++) {
            int number = Integer.parseInt(br.readLine());
            Duck duck = new Duck(number);
            tree.goDuck(duck);
        }

        br.close();
    }
}

{% endhighlight %}

---

# GitHub Gist Embed
GitHub Gist Embed는 GitHub의 Gist 서비스를 활용하여 코드 스니펫을 다른 웹사이트나 블로그에 삽입하는 기능을 제공하는 것을 말합니다.

<script src="https://gist.github.com/mmistakes/77c68fbb07731a456805a7b473f47841.js"></script>