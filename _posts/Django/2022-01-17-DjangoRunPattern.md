---
title: "[Django] 장고 수행 단계"
categories: "Django"

tag: 

image: /assets/images/django.png

toc: true
toc_sticky: true
toc_label: "Table of contents"
---


Python 웹 프레임워크중 하나인 Django의 수행 단계를 살펴 보려고 합니다.

# 장고 수행 단계

<!-- file name : Untitled.png -->
<a href = "https://drive.google.com/uc?id=1Y051bHHo2Wt0hTZhcKLZJOB6xcF06KkD" target="_blank">
    <center><img src = "https://drive.google.com/uc?id=1Y051bHHo2Wt0hTZhcKLZJOB6xcF06KkD"></center>
    <center>[그림] 장고 수행 단계</center>
</a>




장고의 수행 단계는 다음과 같습니다.

1. `Client`는 Server `URL`에 요청을 하게 됩니다. 요청은 주로 `GET` 방식과 `POST` 방식으로 진행됩니다.
2. `Sever`에서는 `Client`의 요청을 `urls.py`파일에서 알맞은 `URL`경로 처리를 해줍니다. 경로 처리된 `Client`는 자신이 원하는 동작을 하는 `View`로 이동합니다.
3. `View` 에서는 Model과 Template에서 data와 front end에서 보여질 code를 제공 받습니다.
4. `Model`에서는 고객의 data를 추가 수정 삭제 하는 일을 합니다. DB와 연동 되는 부분 입니다. ORM으로 동작합니다.
5. `Template`에서는 `Client`가 받는 화면을 제작합니다.