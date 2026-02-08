---
title: "[Network] 한국 PC에서 미국 PC로 통신하는 과정 완벽 이해하기"

tagline: "DNS부터 HTTP까지, 인터넷 통신의 전체 흐름을 이해하기"

header:
  overlay_image: /assets/post/network/2026-02-09-korea-us-communication/overlay.png
  overlay_filter: 0.5

categories:
  - Network

tags:
  - 네트워크
  - 인터넷통신
  - TCP/IP
  - DNS
  - NAT
  - 라우팅
  - HTTP

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-02-08T23:00:00
---

"www.google.com"을 브라우저에 입력하고 엔터를 누렸을 때, 과연 무슨 일이 일어날까요? 

단순해 보이는 이 행동 하나가 실제로는 **한반도에서 태평양을 건너 미국까지**, 그리고 다시 돌아오는 **엄청나게 복잡한 과정**을 거칩니다. 이 글에서는 그 전체 여정을 차근차근 추적해보겠습니다.

---

# 네트워크 환경 설정하기

먼저 가정을 명확히 해봅시다. 이것이 논의의 기초입니다.

## 내부 네트워크 구성

당신의 집에는:
- **내 PC**: 192.168.0.2 (사설 IP)
- **홈 게이트웨이(라우터)**: 192.168.0.1
- **외부 공인 IP**: 통신사가 할당한 동적 IP (예: 58.123.45.67)

이 구조가 중요한 이유는 **사설 IP와 공인 IP의 이중 역할** 때문입니다. 내부 네트워크에서는 사설 IP로 통신하지만, 외부 인터넷으로 나갈 때는 공인 IP로 변환되어야 합니다.

---

# DNS 조회: "도메인명을 IP 주소로 변환하기"

## 1단계: 로컬 DNS 서버에 첫 질의

브라우저에 www.google.com을 입력하면, 가장 먼저 이뤄지는 일은:

```
내 PC → 로컬 DNS 서버(보통 통신사 DNS) : "www.google.com의 IP를 주세요"
```

로컬 DNS 서버는 캐시에서 이미 알려진 도메인이면 바로 응답하지만, 모르는 도메인이면? **상위 DNS 서버에 다시 물어봅니다.**

## 2단계: 재귀적 DNS 질의

DNS는 계층적 구조로 되어 있습니다:

<div class="mermaid mermaid-center">
graph TD
    A["내 PC<br/>192.168.0.2"]
    B["로컬 DNS 서버<br/>통신사 DNS"]
    C["Root DNS<br/>최상위 도메인"]
    D["TLD DNS<br/>.com 관리"]
    E["Authoritative DNS<br/>google.com 관리"]
    
    A -->|1. www.google.com?| B
    B -->|2. 모르니까 Root에<br/>물어봐야겠군| C
    C -->|3. .com은 여기| D
    B -->|4. TLD에 물어보자| D
    D -->|5. google.com은 여기| E
    B -->|6. Auth DNS에<br/>물어보자| E
    E -->|7. 142.250.196.164| B
    B -->|8. 142.250.196.164| A
    
    style A fill:#4a4a6a
    style B fill:#6a4a6a
    style C fill:#4a6a6a
    style D fill:#6a6a4a
    style E fill:#6a5a4a
</div>

각 DNS 서버는 자신의 책임 영역만 알고, 모르는 것은 상위 서버에 물어봅니다. 결국 **google.com을 직접 관리하는 Authoritative DNS 서버**에 도달해 실제 IP 주소(142.250.196.164)를 얻게 됩니다.

---

# 패킷 생성: OSI 7계층으로 내려가기 (캡슐화)

IP 주소를 알았다면 이제 **실제 데이터를 보낼 차례**입니다. 하지만 한 번에 보내지 않습니다. 각 계층을 거치면서 계층별 헤더가 추가되는 **캡슐화(Encapsulation)** 과정이 일어납니다.

<div class="mermaid mermaid-center">
graph TB
    A["7계층: Application Layer<br/>HTTP GET 요청"]
    B["6계층: Presentation Layer<br/>암호화/인코딩"]
    C["5계층: Session Layer<br/>세션 관리"]
    D["4계층: Transport Layer<br/>TCP 헤더 추가<br/>출발지포트:54321<br/>목적지포트:80"]
    E["3계층: Network Layer<br/>IP 헤더 추가<br/>출발지IP: 192.168.0.2<br/>목적지IP: 142.250.196.164"]
    F["2계층: Data Link Layer<br/>이더넷 헤더 추가<br/>목적지MAC: 게이트웨이MAC"]
    G["1계층: Physical Layer<br/>전기신호로 변환"]
    
    A --> B --> C --> D --> E --> F --> G
    
    style A fill:#4a4a6a
    style D fill:#6a4a6a
    style E fill:#4a6a6a
    style F fill:#6a6a4a
    style G fill:#6a5a4a
</div>

## 중요한 포인트: MAC 주소는 누구의 것인가?

여기서 흥미로운 질문이 생깁니다: **"목적지가 미국의 구글 서버인데, 왜 MAC 주소는 게이트웨이의 MAC일까?"**

그 이유는:
- **MAC 주소는 같은 네트워크 세그먼트에서만 유효**합니다
- 내 PC와 게이트웨이는 같은 192.168.0.x 대역이지만, 미국 구글은 완전히 다른 네트워크
- 따라서 일단 **게이트웨이(다음 홉)의 MAC 주소**로 보내고, 게이트웨이가 받아서 전달해야 합니다

이를 위해 **ARP(Address Resolution Protocol)**를 사용해 게이트웨이의 MAC 주소를 먼저 알아냅니다.

---

# NAT: 사설 IP를 공인 IP로 변환하기

게이트웨이에 도착한 패킷에는 출발지 IP가 **192.168.0.2**(사설 IP)입니다. 

**문제**: 외부 인터넷에는 사설 IP 대역이 없습니다. 구글 서버가 192.168.0.2로 응답을 보낼 수 없습니다!

**해결책**: **NAT (Network Address Translation)**

<div class="mermaid mermaid-center">
graph LR
    A["내부 패킷<br/>Src: 192.168.0.2:54321<br/>Dst: 142.250.196.164:443"]
    B["게이트웨이<br/>NAT 변환"]
    C["외부 패킷<br/>Src: 58.123.45.67:54321<br/>Dst: 142.250.196.164:443"]
    D["NAT 테이블에 저장<br/>192.168.0.2:54321<br/>↔<br/>58.123.45.67:54321"]
    
    A --> B --> C
    B --> D
    
    style A fill:#4a4a6a
    style B fill:#6a4a6a
    style C fill:#4a6a6a
    style D fill:#6a5a4a
</div>

게이트웨이는:
1. 출발지 IP **192.168.0.2** → **58.123.45.67** (자신의 공인 IP)로 변환
2. 이 매핑을 **NAT 테이블**에 기록
3. 나중에 응답이 오면 역으로 변환

이것이 바로 **당신의 내부 PC가 외부에서 직접 접근이 안 되는 이유**입니다. 내부에서 먼저 요청해야만 응답을 받을 수 있거든요!

---

# 라우팅: 수천 개의 라우터를 거쳐 미국까지 가기

이제 패킷이 게이트웨이를 떠나 인터넷 본선으로 나갑니다. 한국의 인터넷 백본에서 시작해, 해저 케이블을 타고 태평양을 건너 미국까지 가야 합니다.

## 각 라우터의 역할

경로상의 **모든 라우터**는:

1. 패킷의 **목적지 IP(142.250.196.164)** 확인
2. 자신의 **라우팅 테이블** 검색
3. 가장 일치하는 네트워크 대역 찾기
4. **다음 홉(Next Hop)** 결정

<div class="mermaid mermaid-center">
graph LR
    A["내 PC<br/>192.168.0.2"]
    B["게이트웨이<br/>58.123.45.67"]
    C["한국 ISP<br/>라우터"]
    D["아시아 리전<br/>라우터"]
    E["태평양<br/>해저케이블"]
    F["미국 ISP<br/>라우터"]
    G["구글 서버<br/>142.250.196.164"]
    
    A -->|NAT 변환| B --> C --> D --> E --> F --> G
    
    style A fill:#4a4a6a
    style B fill:#6a4a6a
    style C fill:#4a6a6a
    style D fill:#6a6a4a
    style E fill:#6a5a6a
    style F fill:#6a4a7a
    style G fill:#7a4a6a
</div>

## "최단 경로"가 항상 물리적 거리는 아니다

많은 사람들이 착각하는 부분: 라우팅이 무조건 "가장 짧은 물리적 거리"를 선택하는 것이 아닙니다.

**BGP(Border Gateway Protocol)**라는 복잡한 알고리즘이:
- 각 ISP 간의 비용 계약
- 네트워크 혼잡도 (실시간)
- 링크의 신뢰성
- 정책적 결정

등을 종합적으로 고려해 경로를 결정합니다. 심지어 **응답이 올 때는 다른 경로**로 올 수도 있습니다! (비대칭 라우팅)

## TTL: 무한 루프 방지

그런데 만약 라우팅이 꼬여서 패킷이 계속 회전한다면? 이를 방지하기 위해 **TTL(Time To Live)** 값이 사용됩니다.

- 각 라우터를 거칠 때마다 TTL 값이 1씩 감소
- TTL이 0이 되면 패킷은 버려집니다
- 기본값은 보통 64 또는 128

---

# TCP 3-Way Handshake: 안정적인 연결 수립하기

드디어 구글 서버의 IP에 도달했습니다. 하지만 바로 HTTP 요청을 보낼 수 없습니다. 먼저 **신뢰성 있는 TCP 연결**을 수립해야 합니다.

## 3-Way Handshake 과정

<div class="mermaid mermaid-center">
sequenceDiagram
    participant PC as 내 PC<br/>192.168.0.2:54321
    participant Server as 구글 서버<br/>142.250.196.164:443
    
    PC->>Server: 1. SYN<br/>Seq=1000
    Note over PC,Server: "연결하고 싶어요!"
    
    Server->>PC: 2. SYN-ACK<br/>Seq=2000, Ack=1001
    Note over PC,Server: "좋아요! 나는 2000부터 시작할게요"
    
    PC->>Server: 3. ACK<br/>Seq=1001, Ack=2001
    Note over PC,Server: "확인했어요!"
    
    Note over PC,Server: 연결 수립 완료!
</div>

각 단계의 의미:
1. **SYN**: "동기화 해주세요" + 시퀀스 번호(1000) 전송
2. **SYN-ACK**: "좋음" + 내 시퀀스 번호(2000) + 당신의 다음 번호 확인(1001)
3. **ACK**: "확인함" + 서버의 다음 번호 확인(2001)

이제 **양방향 신뢰성 있는 통신이 준비**되었습니다!

---

# HTTP 요청 및 응답

연결이 수립되었으니, 이제 실제 데이터를 주고받습니다.

## 우리의 요청:

```http
GET / HTTP/1.1
Host: www.google.com
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)
Accept: text/html,application/xhtml+xml
Accept-Language: ko-KR,ko;q=0.9
Accept-Encoding: gzip, deflate, br
Connection: keep-alive
```

## 구글의 응답:

```http
HTTP/1.1 200 OK
Content-Type: text/html; charset=UTF-8
Content-Length: 15234
Set-Cookie: NID=...; expires=...
Server: gws

<!DOCTYPE html>
<html>
<head><title>Google</title></head>
<body>
  <!-- HTML 컨텐츠 -->
</body>
</html>
```

**200 OK**: 요청 성공!

---

# 역방향 여행: 응답이 돌아오다

이제 구글의 응답이 다시 한국으로 돌아와야 합니다.

## 흥미로운 사실: 갈 때와 다른 경로!

인터넷의 특성상, 응답이 **완전히 다른 경로**로 돌아올 수 있습니다.

예를 들어:
- **간 길**: 한국 → 일본 → 미국 서부 → 구글
- **온 길**: 구글 → 미국 동부 → 유럽 → 한국

왜? 
- 네트워크 혼잡도가 실시간으로 변함
- BGP 정책이 방향에 따라 다를 수 있음
- ISP 간의 상호 연결 방식이 비대칭적일 수 있음

심지어 **같은 HTTP 응답의 여러 TCP 패킷들이 각각 다른 경로**로 올 수도 있습니다! 그래서 TCP 시퀀스 번호로 순서를 재조립하는 거죠.

## 게이트웨이에서의 역방향 NAT

응답 패킷이 게이트웨이(58.123.45.67)에 도착했습니다:
- 목적지: 58.123.45.67:54321
- 게이트웨이가 **NAT 테이블 조회**:
  ```
  192.168.0.2:54321 ←→ 58.123.45.67:54321
  ```
- 패킷의 목적지를 **192.168.0.2:54321로 변환**
- 내부 PC로 전달

만약 NAT 테이블에 해당 항목이 없으면? 패킷을 **버립니다**! 이것이 외부에서 무단으로 접근할 수 없는 이유입니다.

---

# 역캡슐화: 계층을 올라가며 디코딩하기

응답 패킷이 내 PC(192.168.0.2)에 도착했습니다. 이제 역순으로 헤더를 제거하면서 **최종 데이터**를 추출합니다.

<div class="mermaid mermaid-center">
graph BT
    A["1계층: Physical Layer<br/>전기신호 수신"]
    B["2계층: Data Link Layer<br/>이더넷 헤더 제거<br/>IP 패킷 추출"]
    C["3계층: Network Layer<br/>IP 헤더 제거<br/>TCP 세그먼트 추출"]
    D["4계층: Transport Layer<br/>TCP 헤더 제거<br/>HTTP 데이터 추출"]
    E["7계층: Application Layer<br/>HTTP 응답 파싱<br/>브라우저 렌더링"]
    
    A --> B --> C --> D --> E
    
    style A fill:#6a5a4a
    style B fill:#6a6a4a
    style C fill:#4a6a6a
    style D fill:#6a4a6a
    style E fill:#4a4a6a
</div>

각 계층에서:
1. 자신의 헤더 검증 및 제거
2. 데이터를 상위 계층으로 전달
3. 문제 발생 시 NAK(부정 응답) 또는 패킷 재전송

---

# 추가 리소스 요청: HTML 파싱 후의 과정

HTML 응답을 받은 브라우저가 이를 파싱하면, 다양한 참조를 찾습니다:

```html
<link rel="stylesheet" href="https://www.google.com/style.css">
<img src="https://www.google.com/logo.png">
<script src="https://www.google.com/app.js"></script>
```

브라우저는 **각각의 리소스마다 새로운 HTTP 요청**을 보냅니다!

실제로 Google 메인 페이지 하나를 완전히 로드하려면:
- HTML 요청 1개
- CSS 파일 여러 개
- 이미지 파일들
- JavaScript 파일들
- 분석 스크립트들
- 광고 서버들...

**수십 개 이상의 HTTP 요청**이 병렬로 이루어집니다.

## 효율화 기법들

그런데 매번 TCP 3-Way Handshake를 다시 할까요? 아니면 DNS를 다시 조회할까요?

**아니요!** 브라우저는:
- **HTTP Keep-Alive**: 같은 서버면 TCP 연결 재사용
- **DNS 캐싱**: 한 번 알아낸 IP는 메모리에 저장
- **HTTP/2**: 하나의 TCP 연결에서 여러 요청 동시 처리

이런 최적화들이 있어야 웹 브라우징이 실시간으로 느껴집니다!

---

# 실무적 이해의 중요성

이제 당신은 다음과 같은 상황들을 이해할 수 있습니다:

## Why는?

**Q: 집에서 외부 사람이 내 PC에 직접 접근할 수 없는 이유는?**
- A: NAT 때문. 내부에서만 요청을 허용하는 단방향 필터

**Q: 간혹 외국 사이트가 느린 이유는?**
- A: 해저 케이블 용량 부족, ISP 간 상호 연결 병목, BGP 최적화 부재 등

**Q: VPN을 써서 다른 나라 IP로 접속하면 뭐가 달라지나?**
- A: 라우팅 경로가 달라지고, 해당 국가의 라우터들을 거쳐가게 됨

**Q: DNS가 느리면 전체 웹이 느려지는 이유는?**
- A: DNS 조회는 모든 HTTP 요청의 전제 조건. 1회용이 아니라 반복됨

---

# 마치며

한국의 PC에서 미국의 서버로 통신하는 과정은:

1. **DNS 조회**: 도메인명을 IP로 변환 (계층적, 재귀적)
2. **캡슐화**: 각 계층에서 헤더 추가
3. **NAT**: 사설 IP를 공인 IP로 변환
4. **라우팅**: 수천 개의 라우터를 거쳐 최적 경로 선택
5. **TCP 3-Way Handshake**: 신뢰성 있는 연결 수립
6. **HTTP 요청/응답**: 실제 데이터 송수신
7. **역캡슐화**: 계층을 올라가며 데이터 추출
8. **렌더링**: 브라우저가 HTML/CSS/JS 해석

단순한 '클릭' 하나가 이렇게 많은 계층들을 거쳐 이루어집니다!

이 과정을 이해하는 것이 바로:
- ✅ 네트워크 문제 해결의 첫걸음
- ✅ 웹 애플리케이션 성능 최적화의 기초
- ✅ 네트워크 보안의 핵심

다음에는 이 과정에서 **보안 위협들(도청, man-in-the-middle, DNS 스푸핑)**과 **HTTPS/TLS 암호화**에 대해 살펴보겠습니다!

---

# 궁금한 점 나누기

혹시 이 과정에서 이해가 안 가는 부분이 있나요? 또는 실제로 `traceroute` 명령어로 경로를 확인해본 경험이 있으신가요? 댓글로 경험을 나누어주세요!
