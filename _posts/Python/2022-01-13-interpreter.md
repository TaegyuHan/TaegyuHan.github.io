---
title: "[Python] 인터프리터 언어 파이썬"
categories: "Python"
tag:
    - "Python"
    - "interpreter"

image: /assets/images/python.png
toc: true
toc_sticky: true
toc_label: "Table of contents"
---

category: Python

인터프리터 언어인 `Python`이 Python 코드에서 기계어까지 진행되는 것까지 정리하는 글 입니다.

# 인터프리터 란?

프로그래밍언어의 소스를 실행시킬때 바로 컴퓨터가 이해 할 수 있는 기계어로 변환하여 실행되는 컴퓨터 프로그래밍 환경을 의미합니다.

# 컴파일러 란?

특정 프로그래밍언어를 다른 프로그래밍 언어로 변경해주는 번역 프로그램을 말합니다.

`Python code` → `Python Byte Code`


<!-- file name : Untitled.png -->
<a href = "https://drive.google.com/uc?id=10IjCZMji0HNqxjF6fFJ1b-YD66v5xMqU" target="_blank">
        <center> <img src = "https://drive.google.com/uc?id=10IjCZMji0HNqxjF6fFJ1b-YD66v5xMqU"> </center>
        <center>[그림] Python 코드가 실행되는 원리</center>
</a>


- Editer : 파이썬 코드를 작성하는 IDE
- Source File : 파이썬 코드 파일
- Compiler : 파이썬 코드를 바이트코드로 변경해주는 컴파일러
- Virtual Machine : 파이썬 Byte 코드를 받아서 기계어로 변환해줍니다.
- Running Program : 기계어를 받아서 프로그램을 실행합니다.

위의 그림을 `Python Code` 가 `기계어` 로 변환되는 과정을 설명한 것입니다. `Python code`는 `Pycharm`, `VSC` 같은 `IDE` 에서 코드를 작성하고 `Compiler`로 코드를 넘기게 됩니다. `Compiler`에서는 `Python code`를 `Byte code`로 변역하고 그 그 번역된 `Byte Code`를 `Python Virtual Machine`으로 넘겨 기계어를 받아 냅니다.

# 다양한 Python Compiler 종류

파이썬은 다양한 컴파일러를 가지고 있습니다. 우리가 상용하는 기본적인 컴파일러는 `CPython` 컴파일러 입니다.

<!-- file name : Untitled 1.png -->
<a href = "https://drive.google.com/uc?id=10HqjBRaoVdvUrYE2LmXyJmGF-y-TxSZz" target="_blank">
        <center> <img src = "https://drive.google.com/uc?id=10HqjBRaoVdvUrYE2LmXyJmGF-y-TxSZz"> </center>
        <center>[그림] 컴파일러의 종류</center>
</a>

- CPython
: C언어로 구현되어 있는 표준 `Python` 컴파일러 입니다.
- Jython
: `Java`로 구현되어있는 컴파일러 입니다. Java와 결합성이 좋아 JVM에서 실행이 가능합니다.
- IronPython
: MIcrosofit .NET Framework의 가상머신인 CLR 상에서 구현이 가능 합니다.
- PyPy
: 기존에 작성한 `CPython` 와 다르게 `Python`으로 구현되어 있다고 생각할 수 있습니다.
- PyJS
: `PyJS`는 `JavaScript` 와의 편한 연동을 위해 개발된 컴파일러 입니다.
- Nuitka
: `Python` 코드를 `c++` 언어로 변경해주는 컴파일러 입니다.
- Stackless Python
: `C stack`을 제거한 컴파일러 입니다.

더 자세한 내용은 너무 깊게 들어가는 것 같아서 기본적으로 어떤 것이 있는지만 알고 넘어갔습니다.

---

# Byte Code 확인하기!

`Python`코드를 `Byte`코드로 변환하여 직접 확인해보도록 하겠습니다.

`__code__.co_code` 객체안의 매직 메소드를 이용하여 Byte 코드 확인합니다.

test1.py

```python
def byte_code_test(): # 확인하고 싶은 함수
    print("test")
    return

print(byte_code_test.__code__.co_code)
```

<!-- file name : Untitled 2.png -->
<a href = "https://drive.google.com/uc?id=10BqXU8fwWBKyeeECXO8Elym_aPH6z3OH" target="_blank">
        <center> <img src = "https://drive.google.com/uc?id=10BqXU8fwWBKyeeECXO8Elym_aPH6z3OH"> </center>
        <center>[그림] 코드 실행 결과</center>
</a>


`byte_code_test` 함수의 바이트코드 입니다.

```python
b't\x00d\x01\x83\x01\x01\x00d\x00S\x00'
```

이렇게 나온 결과를 list에 넣으면 정수형으로 변환되어 나옵니다.

```python
print(list(byte_code_test.__code__.co_code))
```

<!-- file name : Untitled 3.png -->
<a href = "https://drive.google.com/uc?id=1136dRfBwZd_mho13Xg2K0WG47o9w20Ao" target="_blank">
        <center> <img src = "https://drive.google.com/uc?id=1136dRfBwZd_mho13Xg2K0WG47o9w20Ao"> </center>
        <center>[그림] 코드 실행 결과</center>
</a>




<!-- file name : Untitled 4.png -->
<a href = "https://drive.google.com/uc?id=10rAWUWjoa_VFNuhbvz519nwIW8CjemeC" target="_blank">
        <center><img src = "https://drive.google.com/uc?id=10rAWUWjoa_VFNuhbvz519nwIW8CjemeC"></center>
        <center>[그림] byte코드가 정수 리스트로 변경되는 과정</center>
</a>


이 바이트 리스트에 의미하는 정수 값은 dis 모듈을 통해서 자세히 알아보겠습니다.

## dis 모듈을 이용해서 Byte Code 이해하기

`bytecode` 를 사람이 그대로 이해하기는 어렵습니다. `dis` 모듈을 사용하면 사람이 보기 편한 어셈블리어와 비슷한 형식의 글을 확인할 수 있습니다.

test2.py

```python
import dis # byte코드 확인 내장 모듈

def byte_code_test(): # 확인하고 싶은 함수
    print("test")
    return

dis.dis(byte_code_test) # 확인
```

<!-- file name : Untitled 5.png -->
<a href = "https://drive.google.com/uc?id=10jwSppm5ZuiNdtRqh8rn0kqSbVcb3qe-" target="_blank">
        <center> <img src = "https://drive.google.com/uc?id=10jwSppm5ZuiNdtRqh8rn0kqSbVcb3qe-"> </center>
        <center>[그림] 코드 실행 결과</center>
</a>



dis 모듈을 이용하어 나온결과를 해석하면 다음과 같습니다.

<!-- file name : Untitled 6.png -->
<a href = "https://drive.google.com/uc?id=10iiPBzTmWjktjU5QSu1xK9bfAYH_vIZw" target="_blank">
        <center><img src = "https://drive.google.com/uc?id=10iiPBzTmWjktjU5QSu1xK9bfAYH_vIZw"></center>
        <center>[그림] 코드 실행 결과 정리하기</center>
</a>


- line : 실제 코드의 줄
- bytecode sequence : 바이트 코드의 결과에 몇번째에 존재하는지 알려주는 index
- operation name : 연산자 이름
- arg.index : 인자의 인덱스 값
- argument value : 인자의 값

이 결과는 `[116, 0, 100, 1, 131, 1, 1, 0, 100, 0, 83, 0]` 결과로 나온 값과 비교해보면 이렇게 해석할 수 있습니다.


<!-- file name : Untitled 7.png -->
<a href = "https://drive.google.com/uc?id=10f-vcXPoY4IRVHftdQ388EjpWku37A73" target="_blank">
        <center><img src = "https://drive.google.com/uc?id=10f-vcXPoY4IRVHftdQ388EjpWku37A73"></center>
        <center>[그림] 코드 실행 결과 정리하기</center>
</a>


`[116, 0, 100, 1, 131, 1, 1, 0, 100, 0, 83, 0]` 리스트의 인덱스 값이 `bytecode sequence` 로 들어가고 그 위치에 있는 리스트 값이 `operation name`  으로 들어가게 됩니다. `operation name` 의 값은 [code(click)](https://github.com/python/cpython/blob/main/Lib/opcode.py) 를 통해서 확인이 가능합니다. `0번째 index` 인 `116`의 값은 `LOAD_GLOBAL` 를 의미하고 `1번째 index` 의 값인 0과 쌍을 이뤄서 `arg.index` 를 가집니다.

# Byte Code 이해하기

그럼이제 결과로 출력된 byte 코드를 이해해보기로 하겠습니다.

<!-- file name : Untitled 8.png -->
<a href = "https://drive.google.com/uc?id=10bk5pm09zyT_47EP_wp3H21S6Rpr018l" target="_blank">
        <center><img src = "https://drive.google.com/uc?id=10bk5pm09zyT_47EP_wp3H21S6Rpr018l"></center>
        <center>[그림] 코드 확인</center>
</a>


```python
4           0 LOAD_GLOBAL              0 (print)
            2 LOAD_CONST               1 ('test')
            4 CALL_FUNCTION            1
            6 POP_TOP

5           8 LOAD_CONST               0 (None)
           10 RETURN_VALUE
```

여기서 해석은 `operation name` 를 이해하면 수월하게 이해가 가능합니다.
 더 많은 연산자를 원하시면 [(click)](http://unpyc.sourceforge.net/Opcodes.html) 을 참고해 주세요

사용한 연산자들

- LOAD_GLOBAL : 전역 에서의 이름을 스택에 넣기
- LOAD_CONST : 상수를 스택에 넣기
- CALL_FUNCTION : 함수 호출
- POP_TOP : 스택의 맨위 항목을 제거합니다.
- RETURN_VALUE : 반환

사용한 연산자를 통해 위의 내용을 정리하면 다음과 같습니다.

1. `print`를 스택에 넣기
2. `'test``상수를 스택에 넣기
3. 함수 호출
4. 나오기
5. `0` 상수를 스택에 넣기
6. 결과 반환

# Byte Code 다른 단위로 확인하기

클래스 단위로 확인

```python
import dis

class Test:

    def byte_code_test(self):
        print("test")
        return

dis.dis(Test)
```

<!-- file name : Untitled 9.png -->
<a href = "https://drive.google.com/uc?id=10beOMPZ4njG5HEZQHvWuHayoVJ2zaJ4S" target="_blank">
        <center> <img src = "https://drive.google.com/uc?id=10beOMPZ4njG5HEZQHvWuHayoVJ2zaJ4S"> </center>
        <center>[그림] 클래스 단위 실행</center>
</a>


모듈 단위 실행하기

```python
# import dis

class Test:

    def byte_code_test(self):
        print("test")
        return

# $ dis.dis(Test)
```

<!-- file name : Untitled 10.png -->
<a href = "https://drive.google.com/uc?id=10VTIAMTk9j6Vb0xhvMyENlJGkkgCmQ2T" target="_blank">
        <center> <img src = "https://drive.google.com/uc?id=10VTIAMTk9j6Vb0xhvMyENlJGkkgCmQ2T"> </center>
        <center>Python을 더 깊게 이해하는 시간이었습니다!! </center>
</a>


---

참고

[컴파일러 - 위키백과, 우리 모두의 백과사전 (wikipedia.org)](https://ko.wikipedia.org/wiki/%EC%BB%B4%ED%8C%8C%EC%9D%BC%EB%9F%AC#:~:text=%EC%9B%90%EC%8B%9C%20%EC%BD%94%EB%93%9C%EC%97%90%EC%84%9C%20%EB%AA%A9%EC%A0%81%20%EC%BD%94%EB%93%9C,%3A%20%EC%BD%A4%ED%8C%8C%EC%9D%BC)

[https://towardsdatascience.com/understanding-python-bytecode-e7edaae8734d#:~:text=The bytecode can be thought,which is called the oparg](https://towardsdatascience.com/understanding-python-bytecode-e7edaae8734d#:~:text=The%20bytecode%20can%20be%20thought,which%20is%20called%20the%20oparg).

[http://unpyc.sourceforge.net/Opcodes.html](http://unpyc.sourceforge.net/Opcodes.html)

[https://github.com/python/cpython/blob/main/Lib/opcode.py](https://github.com/python/cpython/blob/main/Lib/opcode.py)

[https://towardsdatascience.com/understanding-python-bytecode-e7edaae8734d](https://towardsdatascience.com/understanding-python-bytecode-e7edaae8734d)