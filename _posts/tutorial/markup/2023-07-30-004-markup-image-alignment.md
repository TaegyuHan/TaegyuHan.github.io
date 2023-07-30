---
title: "[ Tutorial ] 이미지 정렬"

header:
  overlay_image: /assets/images/tutorial/tutorial.jpg
  overlay_filter: 0.5

categories:
  - tutorial
  - markup

tags:
  - image
  - alignment

toc: true
toc_label: "Table Of Contents"

last_modified_at: 2023-07-30T18:06:00
---

이미지를 정렬하는 방법을 정리한 글입니다.

---

## 마크다운 이용

이미지를 **중앙** 정렬하는 방법 입니다.

![image-center]({{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-580x300.jpg){: .align-center}

```
![image-center]( 이미지 경로 입력 ){: .align-center}
```

---

![image-left]({{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-150x150.jpg){: .align-left} 이미지를 **왼쪽 정렬**하는 방법 입니다. 당신은 놀라운 개발자입니다! 소프트웨어의 세계에서 창조적인 마법을 부릴 수 있는 특별한 능력을 가지고 있습니다. 당신이 개발하는 소프트웨어들은 사람들의 삶을 편리하게 만들고, 세상을 더 나은 곳으로 바꾸는 데 기여하고 있습니다. 어떤 언어를 사용하든지, 어떤 도구를 선택하든지, 당신은 자신만의 아름다운 코드를 작성하고 문제를 해결하는데 열정을 가지고 있습니다. 끊임없이 배우고 성장하며, 새로운 기술과 도전을 받아들이는 모습은 감탄을 자아냅니다.

```
![image-left]( 이미지 경로 입력 ){: .align-left}
```

---

정렬이 없는 이미지 큰 이미지는 정렬이 필요 없습니다.

![no-alignment]({{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-1200x4002.jpg)

```
![no-alignment]( 이미지 경로 입력 ){: .align-left}
```

---

![image-right]({{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-300x200.jpg){: .align-right}
**오른쪽으로 정렬**하는 방법 입니다. 당신은 놀라운 개발자입니다! 소프트웨어의 세계에서 창조적인 마법을 부릴 수 있는 특별한 능력을 가지고 있습니다. 당신이 개발하는 소프트웨어들은 사람들의 삶을 편리하게 만들고, 세상을 더 나은 곳으로 바꾸는 데 기여하고 있습니다.

어떤 언어를 사용하든지, 어떤 도구를 선택하든지, 당신은 자신만의 아름다운 코드를 작성하고 문제를 해결하는데 열정을 가지고 있습니다. 끊임없이 배우고 성장하며, 새로운 기술과 도전을 받아들이는 모습은 감탄을 자아냅니다.

---

## Html 태그 이용

가운데 정렬 입니다.

<figure class="align-center">
  <img src="{{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-580x300.jpg" alt="">
  <figcaption>이미지를 설명합니다.</figcaption>
</figure> 

```
<figure class="align-center">
  <img src="{{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-580x300.jpg" alt="">
  <figcaption>이미지를 설명합니다.</figcaption>
</figure> 
```

---

왼쪽 정렬 입니다.

<figure style="width: 150px" class="align-left">
  <img src="{{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-150x150.jpg" alt="">
  <figcaption>Itty-bitty caption.</figcaption>
</figure> 

The rest of this paragraph is filler for the sake of seeing the text wrap around the 150×150 image, which is **left aligned**.

As you can see the should be some space above, below, and to the right of the image. The text should not be creeping on the image. Creeping is just not right. Images need breathing room too. Let them speak like you words. Let them do their jobs without any hassle from the text. In about one more sentence here, we'll see that the text moves from the right of the image down below the image in seamless transition. Again, letting the do it's thing. Mission accomplished!

```
<figure style="width: 150px" class="align-left">
  <img src="{{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-150x150.jpg" alt="">
  <figcaption>Itty-bitty caption.</figcaption>
</figure> 
```

---

직접 사진의 크기를 정합니다.

<figure style="width: 1200px">
  <img src="{{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-1200x4002.jpg" alt="">
  <figcaption>Massive image comment for your eyeballs.</figcaption>
</figure> 

```
<figure style="width: 1200px">
  <img src="{{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-1200x4002.jpg" alt="">
  <figcaption>Massive image comment for your eyeballs.</figcaption>
</figure> 

```

---

오른쪽 정렬 입니다.

<figure style="width: 300px" class="align-right">
  <img src="{{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-300x200.jpg" alt="">
  <figcaption>Feels good to be right all the time.</figcaption>
</figure> 

And now we're going to shift things to the **right align**. Again, there should be plenty of room above, below, and to the left of the image. Just look at him there --- Hey guy! Way to rock that right side. I don't care what the left aligned image says, you look great. Don't let anyone else tell you differently.

In just a bit here, you should see the text start to wrap below the right aligned image and settle in nicely. There should still be plenty of room and everything should be sitting pretty. Yeah --- Just like that. It never felt so good to be right.

And that's a wrap, yo! You survived the tumultuous waters of alignment. Image alignment achievement unlocked!

```
<figure style="width: 300px" class="align-right">
  <img src="{{ site.url }}{{ site.baseurl }}/assets/images/tutorial/image-alignment/image-alignment-300x200.jpg" alt="">
  <figcaption>Feels good to be right all the time.</figcaption>
</figure> 
```