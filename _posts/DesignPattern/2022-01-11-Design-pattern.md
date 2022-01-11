---
title: "[Design Pattern] 디자인 패턴 정리"
categories: "Design Pattern"
tag:
    - "Design Pattern"

image: /assets/images/DesignPattern.jpg
toc: true
toc_sticky: true
toc_label: "Table of contents"
---

디자인 패턴을 정리 해보기로 했습니다.

# 1. 디자인 패턴을 사용하는 이유

사용하는 이유는 객체 지향 프로그래밍을 설계하는 과정에서 발생하는 문제를 디자인 패턴을 적용하여 문제를 더 쉽게 풀기위해 디자인 패턴을 사용합니다.


---
<br>

# 2. 정리를 하면서 중요하게 봐야할 점!!

- 왜 이러한 디자인 패턴을 사용해야 하는가?
- 이 디자인 패턴을 사용해서 얻는 이점은 무엇인가?
- UML( **Unified Modeling Language** ) 그려보기
- Java, Python 코드로 정리해보기

---
<br>


# 3. 디자인 패턴의 종류

- 생성 패턴 : 객체를 생성하는 것과 관련된 패턴으로, 객체의 생성과 변경이 전체 시스템에 미치는 영향을 최소화하도록 만들어주어 유연성을 높일 수 있고 코드를 유지하기가 쉬운 편이다.
- 구초 패턴 : 프로그램 내의 자료구조나 인터페이스 구조 등 프로그램의 구조를 설계하는 데 많이 활용될 수 있는 패턴이다.
- 행동 패턴 : 반복적으로 사용되는 객체들의 상호작용을 패턴화한 것으로, 클래스나 객체들이 상호작용하는 방법과 책임을 분산하는 방법을 정의한다.


| 생성 패턴<br>(Creational Patterns) | 구조 패턴<br>(Structural Patterns) | 행동 패턴<br>(Behavioral Patterns) |
| --- | --- | --- |
| 싱글톤<br>(Singleton) | 어댑터<br>(Adapter or Wrapper) | 옵저버<br>(Observer) |
| 추상 팩토리<br>(Abstract Factory) | 브리지<br>(Bridge) | 상태<br>(State) |
| 빌더<br>(Builder) | 데코레이터<br>(Decorator) | 스트레이트지<br>(Strategy) |
| 팩토리 메서드<br>(Factory Method) | 퍼사드<br>(Facade) | 템플릿<br>(Template) |
| 프로토타입<br>(Prototype) | 프록시<br>(Proxy) | 비지터<br>(Visitor) |
|  | 컴포지트 <br>(Composite) | 역할 사슬<br>(Chain of Responsibility) |
|  | 플라이웨이트<br>(Flyweight) | 커맨드<br>(Command) |
|  |  | 인터프리터<br>(Interpreter) |
|  |  | 이터레이터<br>(Iterator) |
|  |  | 미디에이터<br>(Mediator) |


<center>[표] 다양한 디자인 패턴의 종류</center>


---

참고

[http://wiki.hash.kr/index.php/생성패턴](http://wiki.hash.kr/index.php/%EC%83%9D%EC%84%B1%ED%8C%A8%ED%84%B4)