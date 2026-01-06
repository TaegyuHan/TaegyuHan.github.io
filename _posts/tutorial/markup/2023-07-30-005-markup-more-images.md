---
title: "[ Tutorial ] 여러개의 이미지 올리기"

header:
  overlay_image: /assets/images/tutorial/tutorial.jpg
  overlay_filter: 0.5

categories:
  - Tutorial
  - Markup

tags:
  - image
  - multi

toc: true
toc_label: "Table Of Contents"
show_date: true

last_modified_at: 2023-07-30T18:06:00
---

여러개의 이미지를 한번에 올릴 수 있는 방법을 정리하였습니다.

### Figures (for images or video)

#### One Up

이미지 1개를 올리는 방법

<figure>
	<a href="http://farm9.staticflickr.com/8426/7758832526_cc8f681e48_b.jpg"><img src="http://farm9.staticflickr.com/8426/7758832526_cc8f681e48_c.jpg"></a>
	<figcaption><a href="http://www.flickr.com/photos/80901381@N04/7758832526/" title="Morning Fog Emerging From Trees by A Guy Taking Pictures, on Flickr">Morning Fog Emerging From Trees by A Guy Taking Pictures, on Flickr</a>.</figcaption>
</figure>

```
<figure>
	<a href="http://farm9.staticflickr.com/8426/7758832526_cc8f681e48_b.jpg"><img src="http://farm9.staticflickr.com/8426/7758832526_cc8f681e48_c.jpg"></a>
	<figcaption><a href="http://www.flickr.com/photos/80901381@N04/7758832526/" title="Morning Fog Emerging From Trees by A Guy Taking Pictures, on Flickr">Morning Fog Emerging From Trees by A Guy Taking Pictures, on Flickr</a>.</figcaption>
</figure>
```

---

#### Two Up

이미지 2개를 올리는 방법

```html
<figure class="half">
    <a href="/assets/images/image-filename-1-large.jpg"><img src="/assets/images/image-filename-1.jpg"></a>
    <a href="/assets/images/image-filename-2-large.jpg"><img src="/assets/images/image-filename-2.jpg"></a>
    <figcaption>Caption describing these two images.</figcaption>
</figure>
```

<figure class="half">
	<a href="http://placehold.it/1200x600.JPG"><img src="http://placehold.it/600x300.jpg"></a>
	<a href="http://placehold.it/1200x600.jpeg"><img src="http://placehold.it/600x300.jpg"></a>
	<figcaption>Two images.</figcaption>
</figure>

---

#### Three Up

이미지를 3개 올리는 방법

```html
<figure class="third">
	<img src="/images/image-filename-1.jpg">
	<img src="/images/image-filename-2.jpg">
	<img src="/images/image-filename-3.jpg">
	<figcaption>Caption describing these three images.</figcaption>
</figure>
```

<figure class="third">
	<a href="http://placehold.it/600x300.jpg"><img src="http://placehold.it/600x300.jpg"></a>
	<a href="http://placehold.it/600x300.jpg"><img src="http://placehold.it/600x300.jpg"></a>
	<a href="http://placehold.it/600x300.jpg"><img src="http://placehold.it/600x300.jpg"></a>
	<figcaption>Three images.</figcaption>
</figure>
