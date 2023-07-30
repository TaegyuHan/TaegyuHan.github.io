---
title: "[Baek Joon] Java 데이터를 입력 받는 방법"
header:
  overlay_image: /assets/images/algorithms/baek-joon/baek-joon.png
  caption: "[**BAEKJOON**](https://www.acmicpc.net/)"
  # actions:
  #   - label: "Problem Link"
  #     url: "https://www.acmicpc.net/problem/1000"
categories:
  - algorithms
  - baek-joon
tags:
  - 알고리즘
  - 백준
  - 기초
  - 데이터 입력

toc: true
toc_label: "Table Of Contents"
show_date: true


last_modified_at: 2023-07-10T12:52:00
---

Java를 이용한 데이터 입력 정리글.

백준 문제를 풀이하기 위해서는 데이터를 입력 받는 방법을 우선적으로 알아야 문제를 해결할 수 있습니다.

이번 포스트를 통해서 백준의 다양한 문제의 입력방법을 어떻게 Java의 언어로 받을 수 있는지 정리해 보기로 했습니다.


[백준 1000번](https://www.acmicpc.net/problem/1000) 문제를 이용해서 확인해 보기로 하였습니다.

input 값은 아래와 동일합니다.
``` text
1 2
```
두 정수 A와 B를 입력받은 다음, A+B를 출력하는 프로그램을 작성하는 문제 입니다.

---

# 1. `java.util.Scanner`

가장 간단한 기본적인 방법은 `java.util.Scanner`을 이용하는 것 입니다. 이 방법은 적은양의 데이터를 받을 때 사용합니다.

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int A = sc.nextInt();
        int B = sc.nextInt();

        System.out.println(A + B);

        sc.close();
    }
}

// 결과
// 3
```

---

# 2. `java.io.BufferedReader`

대량의 데이터를 입출력할때는 `java.io.BufferedReader`와 `java.io.InputStreamReader`을 이용하여 데이터를 입력받는것이 더 효율적입니다.
이 둘은 특히 대량의 데이터를 처리해야 할 때 Scanner보다 더 빠르게 동작하므로, 복잡하고 큰 데이터에 유용합니다.

```java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        String[] str = br.readLine().split(" ");
        int A = Integer.parseInt(str[0]);
        int B = Integer.parseInt(str[1]);
        
        System.out.println(A + B);
    }
}

// 결과
// 3
```

---

# 3. `java.io.FileReader`

백준 문제를 풀고 확인하는 과정에서 `Main` 클래스를 실행하고 입력해야 하는 데이터를 넣는 과정은 생각보다 시간을 많이 소비할 수 있습니다. 
따라서 `.txt` 파일에 작성하고 그것을 read 하는 형식으로 Test를 진행하는것을 추천합니다.

```text
# test.txt 파일생성
1 2
```

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // test 시작 경로는 프로젝트 경로 입니다.
            BufferedReader br = new BufferedReader(new FileReader("./src/main/java/test.txt"));
            
            String[] str = br.readLine().split(" ");
            int A = Integer.parseInt(str[0]);
            int B = Integer.parseInt(str[1]);
            
            System.out.println(A + B);

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// 결과
// 3
```

위의 3가지를 이용하면 백준 문제에서 제공하는 입력 값은 모두 받을 수 있습니다.
까다로운 형태의 입력 값도 존재 하지만 for문을 이용하면 데이터를 받을 수 있습니다.