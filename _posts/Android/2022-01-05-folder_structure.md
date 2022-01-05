---
title: "[Android] 안드로이드 프로젝트 폴더 구조"

categories: "Android"
tag:
  - "Android"
  - "App"
  - "Folder structure"

toc: true
toc_sticky: true
toc_label: "Table of contents"
---


안드로이드 프로젝트를 시작하면 생성되는 폴더들의 구조와 사용법을 알기 위해서 정리해보기로 했다.

프로젝트가 생성되고 만들어진 (파일, 폴더)는 다음과 같습니다.

<center><img src="https://drive.google.com/uc?id=1hWWDxbeNImqGl1HxkgztznT2LLusTtwt"></center>

- 사용 IDE : Android Studio

간단 요약

```
├─manifests
│       └─AndroidManifest.xml # 앱에 관한 필수 정보
│ 
├─java
│  └─com.example.android_practice
│                   ├─ MainActivity.xml # 앱이 실행되는 파일 입니다.
│                   ├─ ExampleInstrumentedTest.xml # 테스트 코드를 작성하는 파일 입니다.
│                   └─ ExampleUnitTest.xml # 유닛 테스트코드를 작성하는 파일 입니다.
└─res
    ├─drawable # 폴더에는 앱에서 사용하는 이미지를 저장합니다.
    │
    ├─layout # 화면의 레이아웃을 설정합니다.
    │
    ├─mipmap # 앱의 아이콘을 저장하는 폴더 입니다.
    │
    └─values # 폴더에는 앱에서 자주 사용하는 변수를 설정합니다.
        │    
        ├─ color.xml : 색을 설정 합니다.
        ├─ string.xml  : 문자를 설정 합니다.
        │
        └─ themes # 앱의 테마를 설정 합니다.
```

---

# 1. manifests

<center><img src="https://drive.google.com/uc?id=1yr0-mOG5kc6sSd93VQJWyloRDzRkOSt1"></center>

`manifests` 폴더에는 `AndroidManifest.xml` 파일이 존재한다.

파일의 용도는 Android빌드 도구, Android 운영체제 및 Google Play에 앱에 관한 필수 정보를 설명합니다.

AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?> XML 문서임을 선언
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.android_practice"> 프로젝트 앱 패키지명

		앱의 구성요소를 등록하기 위한태그
    <application
        android:allowBackup="true"
				애플리케이션이 백업 및 복원 인프라에 참여 하도록 허용할지 여부

        android:icon="@mipmap/ic_launcher"
				앱의 아이콘 설정

        android:label="@string/app_name"
				앱의 라벨 설정 설정

        android:roundIcon="@mipmap/ic_launcher_round"
				앱의 아이콘 원형 아이콘 설정

        android:supportsRtl="true"
				앱이 RTL(right-to-left)를 지원해 주는 여부를 선언한다.

        android:theme="@style/Theme.Android_practice">
				앱의 기본 테마를 정의한다.

				애플리케이션의 시각적 사용자 인터페이스 요소를 구현하는 
				액티비티(Activity 하위 클래스)를 선언합니다.
        <activity
            android:name=".MainActivity"
						앱이 시작될 때 가장 먼저 실행되는 클래스

            android:exported="true">
						시스템이 액티비티를 인스턴스화할 수 있는지 여부를 나타냅니다.

						활동, 서비스, broadcast receiver가 응답할 수 있는 인텐트의 유형을 지정합니다.
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>
</manifest>
```

---

# 2. java

<center><img src="https://drive.google.com/uc?id=1z9_PHpJ_l5dS-SMBOdn2v9Moil_E80WY"></center>

`java` 폴더에는 3개의 패키지가 존재하고 그 안에 각각 1개의 파일이 존재 합니다. 안에 있는 파일의 용도는 다음과 같습니다.

`MainActivity` 앱이 실행되는 파일 입니다.

MainActivity.xml

```java
package com.example.android_practice;

import androidx.appcompat.app.AppCompatActivity; // 앱 실행의 기본 클래스 입니다.
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
		
	 // android.app.FragmentActivity 에 있는 메소드 오버라이딩
   // 아래의 메소드는 안드로이드의 활동 수명 주기를 이해해야 어떻게 동작하는지
	 // 정확하게 알 수 있습니다.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // 메소드 상속
        setContentView(R.layout.activity_main); // XML 화면 설정
    }
}
```

`ExampleInstrumentedTest` 테스트 코드를 작성하는 파일 입니다.

ExampleInstrumentedTest.xml

```java
package com.example.android_practice;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

// 테스트 코드 프레임 워크 junit
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.android_practice", appContext.getPackageName());
    }
}
```

`ExampleUnitTest` 유닛 테스트코드를 작성하는 파일 입니다.

ExampleUnitTest.xml

```java
package com.example.android_practice;

// 테스트 코드 프레임 워크 junit
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}
```

테스트 코드를 작성하는 방법은 다른 POST에서 설명하도록 하겠습니다.

---

# 3. res

<center><img src="https://drive.google.com/uc?id=1alXjsnfymzWhGFWbz0N9elwhlIyeIQCC"></center>

각종 리소스( 자원 ) 앱에 사용되는 이미지, 자료 등을 저장하는 폴더 입니다.

## 3.1 res.drawable

`drawable` 폴더에는 앱에서 사용하는 이미지를 저장합니다.

이미지의 확장자는 XML 형식으로 되어 있습니다.

<center><img src="https://drive.google.com/uc?id=1f2KPX9OARYWEV-PzfosmfkKZARBdQiCB"></center>

## 3.2 res.layout

`layout` 화면의 레이아웃을 설정합니다. 앱이 사용자에게 보여지는 파일을 저장하는 폴더 입니다.

<center><img src="https://drive.google.com/uc?id=1rWYIdKS5N9ysH5PnotvlDvsqDca1uQoS"></center>

## 3.3 res.mipmap

`mipmap` 앱의 아이콘을 저장하는 폴더 입니다.

폴더 안에는

`ic_launcher` `ic_launcher_round` 두가지의 폴더가 있는데 사각형의 아이콘과, 둥근 형태의 아이콘이 들어있습니다.

res.mipmap.lc_launcher, res.mipmap.lc_launcher_round

<center><img src="https://drive.google.com/uc?id=1su5ViMMf-IYdXU5tbMr4bVeMEAq-81WJ"></center>

---

## 3.4 res.values

`values` 폴더에는 앱에서 자주 사용하는 변수를 설정합니다.

`color.xml` : 색을 설정 합니다.

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
```

`string.xml`  : 문자를 설정 합니다.

```xml
<resources>
    <string name="app_name">Android_practice</string>
</resources>
```

### 3.4.1 res.mipmap.values.themes

`themes` 앱의 테마를 설정 합니다.

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Base application theme. -->
    <style name="Theme.Android_practice" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
        <!-- Primary brand color. -->
        <item name="colorPrimary">@color/purple_500</item>
        <item name="colorPrimaryVariant">@color/purple_700</item>
        <item name="colorOnPrimary">@color/white</item>
        <!-- Secondary brand color. -->
        <item name="colorSecondary">@color/teal_200</item>
        <item name="colorSecondaryVariant">@color/teal_700</item>
        <item name="colorOnSecondary">@color/black</item>
        <!-- Status bar color. -->
        <item name="android:statusBarColor" tools:targetApi="l">?attr/colorPrimaryVariant</item>
        <!-- Customize your theme here. -->
    </style>
</resources>
```

---


참고

[Organizing your Source Files CodePath Android Cliffnotes](https://guides.codepath.com/android/Organizing-your-Source-Files)

[AndroidManifest.xml이란? (velog.io)](https://velog.io/@jjung/AndroidManifest.xml-%EC%9D%B4%EB%9E%80-cczwkwxi)

[안드로이드 android:exported 설명 : 네이버 블로그 (naver.com)](https://m.blog.naver.com/websearch/221668354461)