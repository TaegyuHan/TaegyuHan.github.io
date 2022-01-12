---
title: "[Algorithm] 부르트 포스(Brute Force) 알고리즘"
categories: "Algorithm"
tag:
  - "Algorithm"
  - "Brute Force"

image: /assets/images/algorithm.jpg
toc: true
toc_sticky: true
toc_label: "Table of contents"
---
Brute Force(부르트 포스) 알고리즘 정리글 입니다.

# 개요

부르트 포스 알고리즘 `Brute Force` 는 모든 경우의 수를 확인하는 알고리즘 입니다.

---

# Brute Force 이해하기

[백준 1075번](https://www.acmicpc.net/problem/1075) 문제를 통해서 `Brute Force` 알고리즘을 정리 해보기로 했습니다.

문제는 다음과 같습니다.

- 두 정수 N과 F가 주어진다.
- 정수 N의 가장 뒤 두 자리를 적절히 바꿔서 N을 F로 나누어 떨어지게 만들려고 한다.
- 뒤 두 자리를 가능하면 작게 만들려고 한다.
- 뒤의 두 자리는?

예시

`N=275` 가 주어지고 `F=5`가 주여졌을 때 `F` 로 나누어지는 수를 찾는 방법은 다음과 같습니다.

<a href="https://drive.google.com/uc?id=1-ywbaspx3xGgjLB3S8blCXVsogmj22i8" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1-ywbaspx3xGgjLB3S8blCXVsogmj22i8"></center>
    <center>[그림] 백준 1075번 문제 설명</center>
</a>

처음 부터 마지막 까지 나올 수 있는 모든 경우의 수를 확인 하는 것 입니다.

<a href="https://drive.google.com/uc?id=104c_lWO4sRVm_4WqJthV9h0WE0lK0_E-" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=104c_lWO4sRVm_4WqJthV9h0WE0lK0_E-"></center>
    <center>[그림] 경우의 수</center>
</a>
1
모든 경우의 수는 `00 ~ 99` 까지 나올 수 있게 됩니다. 그중에서 첫번째로 나오는 `00` 이 나눠지기 때문에 답은 `00` 입니다.

- 답 : `00`

<a href="https://drive.google.com/uc?id=1-zjj9VgeDPhLyxwIWdkwVmPlFmVk4VAx" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1-zjj9VgeDPhLyxwIWdkwVmPlFmVk4VAx"></center>
    <center>[그림] 부르트 포스로 찾은 답</center>
</a>

위의 예시가 너무 극단적이여서 설명이 부족했을 수도 있습니다. 그러나 나올 수 있는 모든 경우를 전부 고려해서 알고리즘을 만든다고 이해하시면 됩니다. 

---

# 정리

- 부르트 포스 알고리즘은 모든 경우의 수를 전부 계산해보는 알고리즘 입니다.

---

# 연습하기

부르트 포스 알고리즘 문제 입니다.

URL : [부르트 포스](https://www.acmicpc.net/problemset?sort=ac_desc&algo=125)