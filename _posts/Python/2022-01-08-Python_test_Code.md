---
title: "[Python] 테스트 코드 작성하기 ( Test Code )"

categories: "Python"
tag:
  - "Python"

image: /assets/images/python.png
toc: true
toc_sticky: true
toc_label: "Table of contents"
---


이번 포스팅은 `Python` 테스트 코드를 작성하는 법을 정리하려고 합니다.

간단한 사칙연산을 수행하는 코드 작성후 테스트 코드를 작성하는 법을 익히기로 하였습니다.

---

# 계산기 예시 코드

calc.py

```python
class Calc:

    @staticmethod
    def add(x, y):
        """더하기"""
        return x + y

    @staticmethod
    def subtract(x, y):
        """빼기"""
        return x - y

    @staticmethod
    def multiply(x, y):
        """곱하기"""
        return x * y

    @staticmethod
    def divide(x, y):
        """나누기"""
        if y == 0:
            raise ValueError("Can not divide by zero!")
        return x / y
```

위의 코드는 사칙연산을 수행하는 클래스 입니다. 

`Python` 테스트 코드를 작성하기 위해서는 `unittest` 파이썬 내장 프레임워크를 가져와야 합니다.

```python
import unittest
```

위의 코드는 `unittest` 프레임워크를 가져오는 코드 입니다. 이 코드는 자신이 테스트 코드를 작성할 파일에서 불러옵니다.

test_calc.py

```python
import unittest # 프레임 워크 불러옴

class TestCalc(unittest.TestCase): # unittest.TestCase 상속
		
		# TestCase 클래스에 있는 메소드들
    @classmethod
    def setUpClass(cls):
        """테스트를 실행할 때 단 1번 실행 됩니다."""
        print('setupClass')

    @classmethod
    def tearDownClass(cls):
        """테스트를 끝낼 때 단 1번 실행 됩니다."""
        print('teardownClass')

    def setUp(self):
        """각각의 테스트 메소드가 실행될 때 실행 됩니다."""
        print('setUp')

    def tearDown(self):
        """각각의 테스트 메소드가 끝날 때 실행 됩니다."""
        print('tearDown\n')
```

각각의 메소드는 테스트 케이스를 실행했을 때 정확하게 이해 할 수 있어서 1개의 테스트 예시를 들어 설명을 하겠습니다.

`Calc.add()` 메소드가 정확하게 작동하는지 궁금하여 테스트 코드를 작성해 보겠습니다.

test_calc.py

```python
import unittest # 프레임 워크 불러옴
from calc import Calc # 내가 테스트할 코드 불러옴

class TestCalc(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        """테스트를 실행할 때 단 1번 실행 됩니다."""
        print('setupClass')

    @classmethod
    def tearDownClass(cls):
        """테스트를 끝낼 때 단 1번 실행 됩니다."""
        print('teardownClass')

    def setUp(self):
        """각각의 테스트 메소드가 실행될 때 실행 됩니다."""
        print('setUp')

    def tearDown(self):
        """각각의 테스트 메소드가 끝날 때 실행 됩니다."""
        print('tearDown\n')
		
		# 새로 추가한 코드
    def test_add(self):
        """Calc.add 메소드 테스트"""
        # self.assertEqual(Calc.add(<들어갈 값>, <들어갈 값>), <예상한 결과>)
        print("""Calc.add 메소드 테스트""")
				## self.assertEqual(<비교1>, <비교1>)				
        self.assertEqual(Calc.add(1, 1), 2) ## 두 인자가 같은지 비교
				# 1 + 1 을 예상 결과 2

if __name__ == '__main__':
    runner = unittest.TextTestRunner()
```

여기서 중요한 점은 test에 적용할 메소드의 첫 이름이 `test_`로 시작해야한다는 것입니다.

**중요!!!**

test_calc.py

```python
		# 테스트로 동작함
    def test_add(self):
        """Calc.add 메소드 테스트"""
        self.assertEqual(Calc.add(1, 1), 2)

		# 테스트로 동작 안함 !!
    def add_test(self):
        """Calc.add 메소드 테스트"""
        self.assertEqual(Calc.add(1, 1), 2)
```

위의 테스트 클래스가 동작하는 순서는 다음과 같습니다.

<a href="https://drive.google.com/uc?id=1wcMqZalTQzxQRyoivtIQJ7Zf1uYdMQks" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1wcMqZalTQzxQRyoivtIQJ7Zf1uYdMQks"></center>
    <center>[그림] 테스트 클래스가 동작하는 순서</center>
</a>


---
# 테스트 코드 실행

실행 결과 cmd(명령 프롬포트) 에서 실행

<a href="https://drive.google.com/uc?id=1gzsi4knZmXGBaLzCYWtohzikSOoBqiVA" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1gzsi4knZmXGBaLzCYWtohzikSOoBqiVA"></center>
    <center>[그림] 테스트에 성공한 cmd 창</center>
</a>


위의 그림은 테스트에 성공한 그림 입니다.

test_calc.py

```python
    def test_add(self):
        """Calc.add 메소드 테스트"""
        # self.assertEqual(Calc.add(<들어갈 값>, <들어갈 값>), <예상한 결과>)
        print("""Calc.add 메소드 테스트""")
        self.assertEqual(Calc.add(1, 1), 3) # 코드 결과 값을 수정!!!!
```

<a href="https://drive.google.com/uc?id=1CYyyi5PQOe8kC9a9G3utWl-TQW5plXyB" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1CYyyi5PQOe8kC9a9G3utWl-TQW5plXyB"></center>
    <center>[그림] 테스트에 실패한 cmd 창</center>
</a>


테스트 실패 정보

```python
======================================================================
FAIL: test_add (test_calc.TestCalc) # 실패한 메소드
Calc.add 메소드 테스트 # 메소드 DOC
----------------------------------------------------------------------
Traceback (most recent call last):
  File "C:\Users\gksxo\Desktop\Folder\github\Blog_code\python\testCode\test_calc.py", line 28, in test_add
    self.assertEqual(Calc.add(1, 1), 3) # 메소드안의 test한 케이스
AssertionError: 2 != 3 # 테스트가 틀린 이유
```

최종 테스트 코드 작성

test_calc.py

```python
import unittest # 프레임 워크 불러옴
from calc import Calc # 내가 테스트할 코드 불러옴

class TestCalc(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        """테스트를 실행할 때 단 1번 실행 됩니다."""
        print('setupClass')

    @classmethod
    def tearDownClass(cls):
        """테스트를 끝낼 때 단 1번 실행 됩니다."""
        print('teardownClass')

    def setUp(self):
        """각각의 테스트 메소드가 실행될 때 실행 됩니다."""
        print('setUp')

    def tearDown(self):
        """각각의 테스트 메소드가 끝날 때 실행 됩니다."""
        print('tearDown\n')

    def test_add(self):
        """Calc.add 메소드 테스트"""
        print("""Calc.add 메소드 테스트""")
        self.assertEqual(Calc.add(1, 1), 2)
        self.assertEqual(Calc.add(2, 2), 4)

    def test_subtract(self):
        """Calc.subtract 메소드 테스트"""
        print("""Calc.subtract 메소드 테스트""")
        self.assertEqual(Calc.subtract(1, 1), 0)

    def test_multiply(self):
        """Calc.multiply 메소드 테스트"""
        print("""Calc.multiply 메소드 테스트""")
        self.assertEqual(Calc.multiply(2, 3), 6)

    def test_divide(self):
        """Calc.divide 메소드 테스트"""
        print("""Calc.divide 메소드 테스트""")
        self.assertEqual(Calc.divide(1, 1), 1)

if __name__ == '__main__':
    runner = unittest.TextTestRunner()
```

테스트 코드 결과

<a href="https://drive.google.com/uc?id=1gThrys_geGWuMfaSeOV-T2GVmEqulI27" target='_blank'>
    <center><img src="https://drive.google.com/uc?id=1gThrys_geGWuMfaSeOV-T2GVmEqulI27"></center>
    <center>[그림] 모든 계산 테스트 코드 실행</center>
</a>



# 자주 사용하는 테스트 코드

| 메소드 | 확인 | 사용 가능 버전 |
| ----------- | ----------- | ----------- |
| assertEqual(a, b) | a == b | |
| assertNotEqual(a, b) | a != b | |
| assertTrue(x) | bool(x) is True | |
| assertFalse(x) | bool(x) is False | |
| assertIs(a, b) | a is b | 3.1 |
| assertIsNot(a, b) | a is not b | 3.1 |
| assertIsNone(x) | x is None | 3.1 |
| assertIsNotNone(x) | x is not None | 3.1 |
| assertIn(a, b) | a in b | 3.1 |
| assertNotIn(a, b) | a not in b | 3.1 |
| assertIsInstance(a, b) | isinstance(a, b) | 3.2 |
| assertNotIsInstance(a, b) | not isinstance(a, b) | 3.2 |


더 자세한 테스트 메소드를 확인 하고 싶으시면 
URL : [github code](https://github.com/python/cpython/blob/d2245cf190c36a6d74fe947bf133ce09d3313a6f/Lib/unittest/case.py#L308) 를 참조

---

참고

[https://docs.python.org/3/library/unittest.html#assert-methods](https://docs.python.org/3/library/unittest.html#assert-methods)