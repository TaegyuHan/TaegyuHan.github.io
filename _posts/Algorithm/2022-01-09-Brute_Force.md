---
title: "[Algorithm] [Algorithm] 부르트 포스(Brute Force) 알고리즘"
categories: "Algorithm"
tag:
toc: true
toc_sticky: true
toc_label: "Table of contents"
---
<br>


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

<a href = "https://s3.us-west-2.amazonaws.com/secure.notion-static.com/a135ac5f-a473-469e-9c21-e7dcd52d23e8/Untitled.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20220109%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20220109T102102Z&X-Amz-Expires=86400&X-Amz-Signature=a49360e0f7632250a93329d61867af94dc74cb39f7b1ccade91b6ab8bc777205&X-Amz-SignedHeaders=host&x-id=GetObject" target="_blank">
        <center> <img src = "https://s3.us-west-2.amazonaws.com/secure.notion-static.com/a135ac5f-a473-469e-9c21-e7dcd52d23e8/Untitled.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20220109%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20220109T102102Z&X-Amz-Expires=86400&X-Amz-Signature=a49360e0f7632250a93329d61867af94dc74cb39f7b1ccade91b6ab8bc777205&X-Amz-SignedHeaders=host&x-id=GetObject"> </center>
        <center>[그림] 백준 1075번 문제 설명</center>
</a>

처음 부터 마지막 까지 나올 수 있는 모든 경우의 수를 확인 하는 것 입니다.

<a href = "https://s3.us-west-2.amazonaws.com/secure.notion-static.com/05a9587f-5ae1-47f7-8f80-46aab1fa1952/Untitled.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20220109%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20220109T102102Z&X-Amz-Expires=86400&X-Amz-Signature=877dc24b90cf85781de8cd6ad49291512b698c79a6cf310ee4a8906ad80c5fcf&X-Amz-SignedHeaders=host&x-id=GetObject" target="_blank">
        <center> <img src = "https://s3.us-west-2.amazonaws.com/secure.notion-static.com/05a9587f-5ae1-47f7-8f80-46aab1fa1952/Untitled.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20220109%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20220109T102102Z&X-Amz-Expires=86400&X-Amz-Signature=877dc24b90cf85781de8cd6ad49291512b698c79a6cf310ee4a8906ad80c5fcf&X-Amz-SignedHeaders=host&x-id=GetObject"> </center>
        <center>[그림] 경우의 수</center>
</a>

모든 경우의 수는 `00 ~ 99` 까지 나올 수 있게 됩니다. 그중에서 첫번째로 나오는 `00` 이 나눠지기 때문에 답은 `00` 입니다.

- 답 : `00`

<a href = "https://s3.us-west-2.amazonaws.com/secure.notion-static.com/e0f2cb22-b2a0-4b75-a026-9edebfd571f8/Untitled.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20220109%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20220109T102103Z&X-Amz-Expires=86400&X-Amz-Signature=7d03454388fd434456030db5b979089aeed31fe7a0e100772203db9fc9ac0fab&X-Amz-SignedHeaders=host&x-id=GetObject" target="_blank">
        <center> <img src = "https://s3.us-west-2.amazonaws.com/secure.notion-static.com/e0f2cb22-b2a0-4b75-a026-9edebfd571f8/Untitled.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20220109%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20220109T102103Z&X-Amz-Expires=86400&X-Amz-Signature=7d03454388fd434456030db5b979089aeed31fe7a0e100772203db9fc9ac0fab&X-Amz-SignedHeaders=host&x-id=GetObject"> </center>
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