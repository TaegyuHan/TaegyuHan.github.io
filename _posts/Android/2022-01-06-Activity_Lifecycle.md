---
title: "[Android] 안드로이드 생명( Activity LifeCycle ) 주기"

categories: "Android"
tag:
  - "Android"
  - "App"

toc: true
toc_sticky: true
toc_label: "Table of contents"
---

안드로이드는 **Activity Lifecycle** 로 작동하게 되는데 정리를 해보기로 했습니다.

<a href="https://drive.google.com/uc?id=17FDM7yWnH9KpsvQ88J5ZLQ78Vjj2E47h" target="_blank">
    <center><img src="https://drive.google.com/uc?id=17FDM7yWnH9KpsvQ88J5ZLQ78Vjj2E47h"></center>
    <center>[그림] 활동 수명 주기를 간략하게 표현한 그림</center>
</a>

위의 그림은 안드로이드 개발자 문서에서 가져온 것입니다. 위으 그림을 기반으로 정리를 시작하겠습니다.

활동 주기는 크게 7가지로 이루어져 있습니다.

- `onCreate()`
- `onStart()`
- `onResume()`
- `onPause()`
- `onStop()`
- `onRestart()`
- `onDestroy()`

---

# onCreate( )

Activity가 실행되었을 때 최초로 실행되는 메소드 입니다. 필수적으로 존재 해야 하며 전체의 주로 사이클 동안 한 번만 발생해야하는 로직을 넣어 실행합니다.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
}
```

실행 순서 : **Activity Launched →** `onCreate()` → `onStart()`

---

# onStart( )

Activity의 `onCreate()`가 실행되고 나서 실행되는 메소드 입니다. 이 메소드 에서는 앱의 UI를 관리하는 코드를 작성합니다.

```java
@Override
protected void onStart() {
    super.onStart();
}
```

실행 순서 : `onStart()` → `onResume()`

---

# onResume( )

앱에서 어떤 이벤트가 발생하여 앱에서 포커스가 떠날 때까지 `onResume()` 상태에 머무릅니다. 방해되는 이벤트가 발생하면 `onPause()`을 호출합니다.

```java
@Override
protected void onResume() {
    super.onResume();
}
```

실행 순서 : `onResume()` → `onPause()`

---

# onPause( )

사용자가 Activity를 떠났을 때 호출됩니다. `onPause()`는 아주 잠깐 실행되기 때문에 저장 작업을 실행하기에는 시간이 부족할 수 있습니다. 그러므로 사용자 데이터를 저장하거나, 네트워크를 호출하거나, 데이터 베이스 트랜잭션을 실행해서는 안됩니다. 위의 작업들은 메서드 실행이 끝나기 전에 완료 못할 수 있습니다.

```java
@Override
protected void onPause() {
    super.onPause();
}
```

실행순서 : `onPause()` → `onResume()`

실행순서 : `onPause()` → `onStop()`

---

# onStop( )

Activity가 더이상 사용자에게 표시되지 않으면 호출됩니다. 예를들어 새로 시작된 Activity가 화면 전체를 차지할 경우 적용됩니다. 기존의 Activity가 실행되면 `onRestart()` 메소드를 실행하여 다시 시행합니다.

```java
@Override
protected void onStop() {
    super.onStop();
}

```

실행순서 : `onStop()` → `onRestart()` → `onStart()`

실행순서 : `onStop()` → `onDestroy()`

---

# **onRestart( )**

Activity가 `onStop()` 메소드를 통해서 정지된후에 다시 Activity를 실행할 떄 호출되는 메소드 입니다.

```java
@Override
protected void onRestart() {
    super.onRestart();
}
```

실행순서 : `onRestart()` → `onStart()`

---

# **onDestroy()**

활동이 소멸되기 전에 호출됩니다. 시스템은 다음 중 하나에 해당할 때 이 콜백을 호출합니다.

1. (사용자가 활동을 완전히 닫거나 활동에서 `finish()`가 호출되어) 활동이 종료되는 경우
2. 구성 변경(예: 기기 회전 또는 멀티 윈도우 모드)으로 인해 시스템이 일시적으로 활동을 소멸시키는 경우

```java
@Override
protected void onDestroy() {
    super.onDestroy();
}
```

실행순서 : `onDestroy()` → **Activity down**

---

위의 글로는 개념이 확실하게 잡히지 않아서 Test를 진행한 블로그를 검색해보게 되었습니다..

테스트는 총 3개를 진행한다.

1. 처음 앱을 실행할 때
2. 다른 액티비티로 이동할 경우
3. 다른 액티비티로 이동 후 다시 돌아옴

---

# 1. 처음 앱을 실행할 때

아래의 코드는 아무것도 존재하지 않는 empty 앱 코드입니다.. 

코드는 Blog URL : [https://stickode.tistory.com/5](https://stickode.tistory.com/5) 을 참고 하였습니다.

앱을 실행시켜보면 다음과 같이 동작하는 것을 알 수 있습니다..

<a href="https://drive.google.com/uc?id=1IfVFInRSnlE8sQcbQxbAHRcAqvqWl6L4" target="_blank">
    <center><img src="https://drive.google.com/uc?id=1IfVFInRSnlE8sQcbQxbAHRcAqvqWl6L4"></center>
    <center>[그림] 앱 실행</center>
</a>

```prolog
2022-01-06 21:20:47.615 8146-8146/com.example.android_practice D/MainActivity: onCreate
2022-01-06 21:20:47.617 8146-8146/com.example.android_practice D/MainActivity: onStart
2022-01-06 21:20:47.618 8146-8146/com.example.android_practice D/MainActivity: onResume
```

실행 순서 : `onCreate()` → `onStart()` → `onResume()`

---

# 2. 다른 액티비티로 이동할 경우

<a href="https://drive.google.com/uc?id=1UV8Saeo9VPezt7mUntyS5jlJauGexvzV" target="_blank">
    <center><img src="https://drive.google.com/uc?id=1UV8Saeo9VPezt7mUntyS5jlJauGexvzV"></center>
    <center>[그림] 액티비티 이동가능한 앱 실행</center>
</a>

```prolog
2022-01-06 21:56:38.359 9502-9502/com.example.android_practice D/MainActivity: onCreate
2022-01-06 21:56:38.362 9502-9502/com.example.android_practice D/MainActivity: onStart
2022-01-06 21:56:38.363 9502-9502/com.example.android_practice D/MainActivity: onResume
```

실행 순서 : `onCreate()` → `onStart()` → `onResume()`

앱을 실행 했을때 동일하게 적용 됩니다.

Move 버튼 클릭

<a href="https://drive.google.com/uc?id=1HVmdbsqojeoLOsmVpnuxkypCrH2b0M_l" target="_blank">
    <center><img src="https://drive.google.com/uc?id=1HVmdbsqojeoLOsmVpnuxkypCrH2b0M_l"></center>
    <center>[그림] 액티비티 이동</center>
</a>

```prolog
2022-01-06 21:56:38.359 9502-9502/com.example.android_practice D/MainActivity: onCreate
2022-01-06 21:56:38.362 9502-9502/com.example.android_practice D/MainActivity: onStart
2022-01-06 21:56:38.363 9502-9502/com.example.android_practice D/MainActivity: onResume

<!-- 추가 동작 로그 -->
2022-01-06 21:58:47.263 9502-9502/com.example.android_practice D/MainActivity: onPause
2022-01-06 21:58:47.310 9502-9502/com.example.android_practice D/MoveActivity: onCreate
2022-01-06 21:58:47.313 9502-9502/com.example.android_practice D/MoveActivity: onStart
2022-01-06 21:58:47.313 9502-9502/com.example.android_practice D/MoveActivity: onResume
2022-01-06 21:58:47.778 9502-9502/com.example.android_practice D/MainActivity: onStop
```

실행 순서 : `onResume()` → `onPause()` → `onCreate()` → `onStart()`→ `onResume()` → `onStop()`

여기서 주의 해야 할점은 `MainActivity` 에서 `MoveActivity`로 이동할 때 `MoveActivity`가 생성되고 `onResume()` 상태에서 대기할 때 `MainActivity` 가 종료 됩니다..

뒤로 가기 버튼 클릭

<a href="https://drive.google.com/uc?id=17qxCpfY4GPh1A4EJepD9CS_kwOourgBx" target="_blank">
    <center><img src="https://drive.google.com/uc?id=17qxCpfY4GPh1A4EJepD9CS_kwOourgBx"></center>
    <center>[그림] 액티비티 돌아가기</center>
</a>

```prolog
2022-01-06 21:56:38.359 9502-9502/com.example.android_practice D/MainActivity: onCreate
2022-01-06 21:56:38.362 9502-9502/com.example.android_practice D/MainActivity: onStart
2022-01-06 21:56:38.363 9502-9502/com.example.android_practice D/MainActivity: onResume
2022-01-06 21:58:47.263 9502-9502/com.example.android_practice D/MainActivity: onPause
2022-01-06 21:58:47.310 9502-9502/com.example.android_practice D/MoveActivity: onCreate
2022-01-06 21:58:47.313 9502-9502/com.example.android_practice D/MoveActivity: onStart
2022-01-06 21:58:47.313 9502-9502/com.example.android_practice D/MoveActivity: onResume
2022-01-06 21:58:47.778 9502-9502/com.example.android_practice D/MainActivity: onStop

<!-- 추가 동작 로그 -->
2022-01-06 22:02:50.736 9502-9502/com.example.android_practice D/MoveActivity: onPause
2022-01-06 22:02:50.742 9502-9502/com.example.android_practice D/MainActivity: onRestart
2022-01-06 22:02:50.745 9502-9502/com.example.android_practice D/MainActivity: onStart
2022-01-06 22:02:50.746 9502-9502/com.example.android_practice D/MainActivity: onResume
2022-01-06 22:02:51.261 9502-9502/com.example.android_practice D/MoveActivity: onStop
2022-01-06 22:02:51.262 9502-9502/com.example.android_practice D/MoveActivity: onDestroy
```

실행 순서 : `onResume()` → `onPause()` → `onRestart()` → `onStart()`→ `onResume()` → `onStop()` → `onDestroy()`

여기에서도 `MoveActivity`의 `onResume()`가 이벤트를 대기하고 있다가 뒤로가기 버튼을 클릭하면 전에 실행되었던 `MainActivity`가 `onRestart()`로 다시 실행하게 된다. 그뒤에  `onStart(), onResume()`가 연속으로 실행되고 `MoveActivit`는 동작을 멈추게 됩니다.. ``

---

참고

[https://developer.android.com/reference/android/app/Activity?hl=ko](https://developer.android.com/reference/android/app/Activity?hl=ko)

[https://developer.android.com/guide/components/activities/activity-lifecycle?hl=ko](https://developer.android.com/guide/components/activities/activity-lifecycle?hl=ko)

[https://kairo96.gitbooks.io/android/content/ch2.4.1.html](https://kairo96.gitbooks.io/android/content/ch2.4.1.html)

[https://mydevromance.tistory.com/21](https://mydevromance.tistory.com/21)

[https://stickode.tistory.com/5](https://stickode.tistory.com/5)