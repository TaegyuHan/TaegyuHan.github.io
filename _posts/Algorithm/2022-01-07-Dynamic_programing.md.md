---
title: "[Algorithm] Dynamic programming (다이나믹프로그래밍) 정리"

categories: "Algorithm"
tag:
  - "Algorithm"
  - "Dynamic programming"

toc: true
toc_sticky: true
toc_label: "Table of contents"
---
<br>

# 개요

다이나믹프로그래밍`Dynamic programming` 줄여서 `DP` 라고도 하며 한국어로는 `동적 계획법`이라 한다.

동적 계획 법은 한자어로 표현하면 [動](https://namu.wiki/w/%E5%8B%95)[的](https://namu.wiki/w/%E7%9A%84) [計](https://namu.wiki/w/%E8%A8%88)[劃](https://namu.wiki/w/%E5%8A%83)[法](https://namu.wiki/w/%E6%B3%95) ( 동적 계획법 ) 이다. 

- 動 : 움직일 동
- 的 : 과녁 적/밝을 적
- 計 : 설 계
- 劃 : 그을 획
- 法 : 법 법

한자어의 뜻을 해석하면 움직이는 목표를 설계한다는 뜻으로 해석할 수 있다. 이름과 비슷하게 다이나믹 프로그래밍은 앞에서 구한 답을 뒤에서도 활용하는 알고리즘이다. 계산을 계속 진행하면서 전에 계산했던 값을 이용하는 알고리즘 이라고 생각할 수 있습니다.

---

# 피보나치 수로 이해하기 ( 재귀 )

피보나치 수열은 첫째 및 둘째 항의 값을 더해서 세번째인 다음항을 구하고 계속 앞으로 나아가는 수열입니다.
<a href="https://drive.google.com/uc?id=1G64cI8V0bLVzIx43P-H5IBTtATkV3EHq" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1G64cI8V0bLVzIx43P-H5IBTtATkV3EHq"></center>
    <center>[그림] : 피보나치수열 설명</center>
</a>



여기서 5번째 오는 수를 code로 구해보면 재귀함수를 통해서 다음과 같음 수를 정의할 수 있습니다.

fibonacci.py

```python
class Ex:

    def __init__(self) -> None:
        self.fun_call_count = 0 # 함수 호출 횟수 저장

    def f(self, i: int) -> int:
        """피보나치 함수"""
        self.fun_call_count += 1
        # print(i)
        if i <= 1:
            return 1
        else:
            return self.f(i - 1) + self.f(i - 2)

if __name__ == '__main__':
    ex = Ex()
    ex.f(5) # 5번째 수 찾기
    print(ex.fun_call_count)
```

위의 코드에서 중요하게 봐야할 부분은 재귀함수 안부분입니다.

```python
if i <= 1:
    return 1
else:
    return self.f(i - 1) + self.f(i - 2)
```

- 1 이하 → 1을 반환
- 1 초과 → `f(n - 1) + f(n - 2)` 함수를 호출 합니다.

이 뜻은 즉 1 또는 0을 만나지 않는 이상 계속 함수를 호출하는다는 뜻과 같습니다. 이렇게 `ex.f(5)` 를 호출 했을 때 동작은 아래와 같이 작동합니다.

<a href="https://drive.google.com/uc?id=1fjrSN8ZjiBgdEAb33_XDY5T5N7g0reZK" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1fjrSN8ZjiBgdEAb33_XDY5T5N7g0reZK"></center>
    <center>[그림] : 함수 실행 과정</center>
</a>


- 초록색 : 처음 호출
- 파란색 : `f(n - 1)` 호출
- 빨간색 : `f(n - 2)` 호출
- 하늘색 : 호출 순서

함수는 즉 15번 실행되게 됩니다.

위의 코드를 Pycharm IDE에서 실행 시켜본 결과입니다.
<a href="https://drive.google.com/uc?id=1dj3qQyBxUZaqBIwRc9c9p5A6I0pBKJuo" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1dj3qQyBxUZaqBIwRc9c9p5A6I0pBKJuo"></center>
    <center>[그림] : 코드 실행 사진</center>
</a>


실행결과에서 나온 값은 다음과 같습니다.

- `ex.f(5)` 5번째의 피보나치 수열 값 : 8
- `ex.fun_call_count` 피보나치 함수 실행 횟수 : 15

피보나치 함수 실행 횟수는 위에서 알 수 있었지만 8의 값이 나오는 이유를 알아보면 다음과 같습니다.
<a href="https://drive.google.com/uc?id=1DZr5oQuXS6YE280VSqVuqe75KyaI1-Lq" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1DZr5oQuXS6YE280VSqVuqe75KyaI1-Lq"></center>
    <center>[그림] : 값이 8이나오는 이유</center>
</a>



위의 그림에서 주목해야 할 곳은 노란 부분 입니다. 재귀함수를 호출하는 과정에서 1과 0을 만났을 때 1을 반환하게 되는데 `if i <= 1:` ( 이부분에서 ) 그 횟수를 전부 더하면 8이 나오게 됩니다.

하지만 재귀함수를 이용해서 구하는 방법은 효율적이지 않습니다. 이유는 아래 그림과 같습니다.
<a href="https://drive.google.com/uc?id=1FCM9GGPvKo-4EkV7UA-jTEFtsSU38z7j" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1FCM9GGPvKo-4EkV7UA-jTEFtsSU38z7j"></center>
    <center>[그림] : 같은 input값으로 호출되는 함수 확인</center>
</a>



위의 그림과 같이 중복되는 함수가 존재하는 것을 알수 있습니다. 피보나치 함수에 큰수를 넣을때마다 함수 호출 횟수가 증가하는 것을 알 수 있습니다.

<a href="https://drive.google.com/uc?id=1Qn7Ym6b9B6KMZ6SpL7D8PmlupGZEq04P" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1Qn7Ym6b9B6KMZ6SpL7D8PmlupGZEq04P"></center>
    <center>[그림] : input값이 커질 때 마다 증가 확인</center>
</a>




위와 같은 경우를 다이나믹 프로그램을 통해서 도 빠르고 간단하게 값을 얻어낼 수 있습니다.

---

# 피보나치 수 ( 다이나믹 프로그래밍 )

다이나믹 프로그래밍의 핵심은 계산했던 값을 저장해서 다시 사용합니다.

대표적인 방법으로는 2가지의 방법이 있습니다.

1. Top-down : 위에서 내려오는 것
2. Bottom-up : 아래에서 올라가는 것

---

## Top-down : 위에서 내려오는 것

Top-down은 큰수부터 해서 위에서 아래로 내려오는 방식이다. 아레의 코드를 보면서 이해 할 수 있습니다.

fibonacci2.py

```python
class Ex:

    def __init__(self, i: int) -> None:
        self.fun_call_count = 0
        self.num_list = [0 for _ in range(i + 1)]

    def f(self, i: int) -> int:
        """피보나치 함수 다이나믹 프로그래밍 Top-down"""
        self.fun_call_count += 1
        # print(i)

        # 1 이하이면 반환
        if i <= 1:
            return i;

        # 1 이미 0이 아닌 값이 저장되어 있으면 반환
        if self.num_list[i] != 0:
            return self.num_list[i]

        self.num_list[i] = self.f(i - 1) + self.f(i - 2)
        return self.num_list[i]

if __name__ == '__main__':
    ex = Ex(6) # 초기 배열길이
    print(ex.f(6))
    print(ex.fun_call_count)
    print(ex.num_list)
```

이 코드는 다음과 같이 동작합니다.
<a href="https://drive.google.com/uc?id=1gtR65FO5-pW6Dgl5sDOlNZ0x_xMSx7nQ" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1gtR65FO5-pW6Dgl5sDOlNZ0x_xMSx7nQ"></center>
    <center>[그림] : fibonacci2.py 동작</center>
</a>


위에서 색이 칠해진 곳만 호출됩니다. 총 11번이 호출되고 여기서 중요한 포인트는 0과 1을 제외한 다른 값은 배열의 값을 확인한 뒤 0이 면 값을 저장하고 0이 아니면 더이상 재귀함수로 들어가지 않고 배열에서 값을 사용한다는 점 입니다. 

그래서 `25`번 호출되어야 하는 함수를 `11`번으로 줄일 수 있습니다.

Pycharm 에서 실행시킨 결과는 다음과 같습니다.

<a href="https://drive.google.com/uc?id=1OMo6X3tGnAKAJ2q8q5XKXpUnDt5xDDDI" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1OMo6X3tGnAKAJ2q8q5XKXpUnDt5xDDDI"></center>
    <center>[그림] : fibonacci2.py 실행 결과</center>
</a>


## Bottom-up : 아래에서 올라가는 것

`Bottom-up` 방법은 `Top-down` 방법과 다르게 작은 곳에서 시작해서 큰 곳으로 올라갑니다.

fibonacci3.py

```python
class Ex:

    def __init__(self, i: int) -> None:
        self.calculation_count = 0
        self.num_list = [1, 1] + [0 for _ in range(i - 1)]
        self.last_position = 1;

    def f(self, i: int) -> int:
        """피보나치 함수 다이나믹 프로그래밍 Bottom-up"""

        # 원하는 번째의 값이 0인지 확인
        if self.num_list[i] == 0:
            # 원하는 번째 까지 값 넣기
            for j in range(self.last_position, i):
                self.calculation_count += 1
                # 뒤의 값 더해서 넣기
                self.num_list[j] = self.num_list[j - 1] + self.num_list[j - 2]

        self.last_position = i;
        return self.num_list[i - 1] # 값 출력

if __name__ == '__main__':
    ex = Ex(6) # 초기 배열길이
    print(ex.f(6))
    print(ex.calculation_count)
    print(ex.num_list)
```

위의 코드 동작은 아래와 같습니다.
<a href="https://drive.google.com/uc?id=1kTKaoiZuk7lQ1LNvuuTavgLnxW6_MuM8" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1kTKaoiZuk7lQ1LNvuuTavgLnxW6_MuM8"></center>
    <center>[그림] : for 문을 이용한 `Bottom-up` 풀이</center>
</a>


<a href="https://drive.google.com/uc?id=1dj3qQyBxUZaqBIwRc9c9p5A6I0pBKJuo" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1dj3qQyBxUZaqBIwRc9c9p5A6I0pBKJuo"></center>
    <center>[그림] : for 문을 이용한 `Bottom-up` 풀이</center>
</a>



기존의 초기값을 배열에 저장하고 값을 계속 추가해주는 방법 입니다. 여기서의 계산은 4번으로 가장 확실하게 줄일 수 있는 것을 확인 할 수 있었습니다.

---

# 정리

- 다이나믹 프로그래밍은 기존의 값을 저장(이용)해서 원하는 값을 도출하는 알고리즘 이다.
- 방법에는 2가지 방법이 있다. `Bottom-u` 아래이서 출발,  `Top-down` 위에서 출발

---

# 연습하기

다이나믹 프로그래밍 알고리즘만 묶어놓은 문제 리스트 링크 입니다.

URL : [다이나믹 프로그래밍](https://www.acmicpc.net/problemset?sort=ac_desc&algo=25)

---

참고

[https://namu.wiki/w/동적 계획법](https://namu.wiki/w/%EB%8F%99%EC%A0%81%20%EA%B3%84%ED%9A%8D%EB%B2%95)