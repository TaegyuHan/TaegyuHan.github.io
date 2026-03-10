---
title: "[Algorithm] 버블 정렬 (Bubble Sort) 완벽 정리"

tagline: "가장 간단하지만 기본이 되는 정렬 알고리즘, 버블 정렬의 원리와 구현"

header:
  overlay_image: /assets/post/algorithm/2026-03-10-bubble-sort/overlay.png
  overlay_filter: 0.5

categories:
  - Algorithm

tags:
  - 알고리즘
  - Algorithm
  - 버블정렬
  - BubbleSort
  - 정렬
  - Sorting
  - Python
  - Java

toc: true
show_date: true

last_modified_at: 2026-03-10T20:53:00+09:00
---

버블 정렬(Bubble Sort)은 가장 기본적인 정렬 알고리즘 중 하나입니다. 이름처럼 큰 값이 물속의 거품처럼 배열의 끝으로 올라가는(이동하는) 모습에서 유래했습니다. 코딩 테스트나 면접에서 자주 등장하는 기본 알고리즘이므로, 원리와 구현을 확실히 이해해두는 것이 좋습니다.

## 버블 정렬이란?

버블 정렬은 **인접한 두 원소를 비교하여 정렬**하는 알고리즘입니다. 배열을 순회하면서 인접한 두 원소의 크기를 비교하고, 정렬 순서가 잘못되어 있으면 위치를 교환합니다.

### 동작 원리

1. 배열의 첫 번째 원소부터 시작하여 인접한 원소와 비교
2. 만약 순서가 잘못되어 있으면 두 원소의 위치를 교환
3. 배열의 끝까지 이 과정을 반복하면 가장 큰 원소가 맨 뒤로 이동
4. 정렬되지 않은 부분에 대해 1~3 과정을 반복

## 인터랙티브 애니메이션

위에서 설명한 버블 정렬 과정을 직접 눈으로 확인해보세요! 시작 버튼을 눌러 정렬 과정을 단계별로 관찰할 수 있습니다.

{% include algorithm/bubble-sort-animation.html %}

- **회색**: 정렬되지 않은 기본 상태
- **주황색**: 현재 비교 중인 두 요소
- **빨간색**: 교환이 발생하는 요소 (위치 이동 애니메이션)
- **초록색**: 정렬이 완료된 요소
- 속도를 조절하여 천천히 또는 빠르게 확인할 수 있습니다.
- 두 요소가 실제로 위치를 바꾸는 애니메이션을 확인할 수 있습니다.

## 복잡도 분석

### 시간 복잡도

| 경우 | 복잡도 | 설명 |
|------|--------|------|
| **최선 (Best)** | O(n) | 이미 정렬된 경우 (최적화 버전) |
| **평균 (Average)** | O(n²) | 일반적인 경우 |
| **최악 (Worst)** | O(n²) | 역순으로 정렬된 경우 |

#### 계산 과정
- 비교 횟수: (n-1) + (n-2) + ... + 2 + 1 = n(n-1)/2
- 교환 횟수: 최악의 경우 비교 횟수와 동일
- 따라서 시간 복잡도는 **O(n²)**

### 공간 복잡도

- **O(1)**: 제자리 정렬(in-place sorting)
- 추가 메모리를 거의 사용하지 않음 (교환용 임시 변수만 필요)

## 장단점

### 장점 ✅

1. **구현이 간단하다**: 코드가 직관적이고 이해하기 쉬움
2. **추가 메모리가 거의 필요 없다**: 공간 복잡도 O(1)
3. **안정 정렬(Stable Sort)**: 같은 값의 순서가 유지됨
4. **교육용으로 적합**: 정렬 알고리즘의 기본 개념 학습에 좋음

### 단점 ❌

1. **느린 속도**: 시간 복잡도 O(n²)로 대용량 데이터에 부적합
2. **비효율적**: 다른 O(n²) 알고리즘(삽입 정렬, 선택 정렬)보다도 느림
3. **실무에서 사용하지 않음**: 더 빠른 알고리즘(퀵, 병합, 힙 정렬 등) 사용

## 최적화 기법

### 1. 조기 종료 (Early Exit)

앞서 소개한 `swapped` 플래그를 이용한 방법입니다. 한 번의 순회에서 교환이 전혀 발생하지 않으면 이미 정렬이 완료된 것이므로 알고리즘을 종료합니다.

```python
# 이미 정렬된 배열: [1, 2, 3, 4, 5]
# 최적화 없이: O(n²) = 25번 비교
# 최적화 후: O(n) = 4번 비교 후 종료
```

### 2. 양방향 버블 정렬 (Cocktail Shaker Sort)

한 방향으로만 순회하지 않고 양방향으로 번갈아가며 정렬합니다.

```python
def cocktail_sort(arr):
    """
    칵테일 쉐이커 정렬 (양방향 버블 정렬)
    """
    n = len(arr)
    start = 0
    end = n - 1
    swapped = True
    
    while swapped:
        swapped = False
        
        # 왼쪽에서 오른쪽으로
        for i in range(start, end):
            if arr[i] > arr[i + 1]:
                arr[i], arr[i + 1] = arr[i + 1], arr[i]
                swapped = True
        
        if not swapped:
            break
        
        swapped = False
        end -= 1
        
        # 오른쪽에서 왼쪽으로
        for i in range(end - 1, start - 1, -1):
            if arr[i] > arr[i + 1]:
                arr[i], arr[i + 1] = arr[i + 1], arr[i]
                swapped = True
        
        start += 1
    
    return arr
```

## 언제 사용해야 할까?

### 적합한 경우 ✅
- 데이터가 매우 작을 때 (10개 이하)
- 거의 정렬된 데이터
- 알고리즘 학습 목적
- 코드 단순성이 중요한 경우

### 부적합한 경우 ❌
- 대용량 데이터 (100개 이상)
- 성능이 중요한 경우
- 실무 프로젝트

## 다른 정렬 알고리즘과 비교

| 알고리즘 | 최선 | 평균 | 최악 | 공간 | 안정성 |
|----------|------|------|------|------|--------|
| 버블 정렬 | O(n) | O(n²) | O(n²) | O(1) | ✅ |
| 선택 정렬 | O(n²) | O(n²) | O(n²) | O(1) | ❌ |
| 삽입 정렬 | O(n) | O(n²) | O(n²) | O(1) | ✅ |
| 퀵 정렬 | O(n log n) | O(n log n) | O(n²) | O(log n) | ❌ |
| 병합 정렬 | O(n log n) | O(n log n) | O(n log n) | O(n) | ✅ |
| 힙 정렬 | O(n log n) | O(n log n) | O(n log n) | O(1) | ❌ |

## 실전 문제 연습

버블 정렬은 코딩 테스트에서 직접 구현을 요구하기보다는, 원리를 이해하고 있는지 확인하는 문제가 많습니다.

### 예제 문제

1. **정렬 과정 출력하기**: 버블 정렬의 각 단계를 출력하는 프로그램 작성
2. **K번째 순회 결과**: K번 순회 후 배열의 상태 출력
3. **최소 교환 횟수**: 배열을 정렬하기 위해 필요한 최소 교환 횟수 계산

```python
def bubble_sort_with_steps(arr):
    """정렬 과정을 출력하는 버블 정렬"""
    n = len(arr)
    print(f"초기 상태: {arr}")
    
    for i in range(n - 1):
        swapped = False
        for j in range(n - 1 - i):
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
                swapped = True
        
        if swapped:
            print(f"{i + 1}회차 순회: {arr}")
        else:
            print(f"{i + 1}회차 순회: 변화 없음 (정렬 완료)")
            break
    
    return arr

# 실행
numbers = [5, 3, 8, 4, 2]
bubble_sort_with_steps(numbers)
```

## 마무리

버블 정렬은 가장 기본적인 정렬 알고리즘으로, 실무에서는 거의 사용되지 않지만 알고리즘 학습의 시작점으로서 중요한 의미를 가집니다.

### 핵심 요약

1. **인접한 원소를 비교하며 정렬**하는 단순한 알고리즘
2. 시간 복잡도는 **O(n²)**, 공간 복잡도는 **O(1)**
3. **안정 정렬**이며 **제자리 정렬**
4. 조기 종료 최적화로 최선의 경우 **O(n)** 가능
5. 실무보다는 **교육 목적**에 적합

버블 정렬을 완벽히 이해했다면, 다음 단계로 **선택 정렬(Selection Sort)**이나 **삽입 정렬(Insertion Sort)** 같은 다른 O(n²) 정렬 알고리즘을 학습하거나, 더 효율적인 **퀵 정렬(Quick Sort)**이나 **병합 정렬(Merge Sort)** 같은 O(n log n) 알고리즘으로 나아가는 것을 추천합니다.

## 코드 구현

### Python 구현

#### 기본 버전
```python
def bubble_sort(arr):
    """
    버블 정렬 기본 구현
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 정렬된 리스트
    """
    n = len(arr)
    
    # 전체 순회 (n-1번)
    for i in range(n - 1):
        # 인접한 원소 비교 (정렬된 부분 제외)
        for j in range(n - 1 - i):
            # 앞의 원소가 더 크면 교환
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
    
    return arr


# 사용 예시
numbers = [5, 3, 8, 4, 2]
print(f"정렬 전: {numbers}")
bubble_sort(numbers)
print(f"정렬 후: {numbers}")
```

#### 최적화 버전 (조기 종료)
```python
def bubble_sort_optimized(arr):
    """
    최적화된 버블 정렬 구현
    이미 정렬된 경우 조기 종료
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 정렬된 리스트
    """
    n = len(arr)
    
    for i in range(n - 1):
        swapped = False  # 교환 발생 여부 플래그
        
        for j in range(n - 1 - i):
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
                swapped = True
        
        # 한 번의 순회에서 교환이 없었다면 이미 정렬됨
        if not swapped:
            break
    
    return arr


# 사용 예시
numbers = [1, 2, 3, 4, 5]  # 이미 정렬된 배열
print(f"정렬 전: {numbers}")
bubble_sort_optimized(numbers)  # 1번의 순회만 수행
print(f"정렬 후: {numbers}")
```

### Java 구현

#### 기본 버전
```java
public class BubbleSort {
    
    /**
     * 버블 정렬 기본 구현
     * 
     * @param arr 정렬할 배열
     */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        
        // 전체 순회 (n-1번)
        for (int i = 0; i < n - 1; i++) {
            // 인접한 원소 비교 (정렬된 부분 제외)
            for (int j = 0; j < n - 1 - i; j++) {
                // 앞의 원소가 더 크면 교환
                if (arr[j] > arr[j + 1]) {
                    // swap
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
    
    /**
     * 배열 출력
     */
    public static void printArray(int[] arr) {
        for (int num : arr) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
    
    public static void main(String[] args) {
        int[] numbers = {5, 3, 8, 4, 2};
        
        System.out.print("정렬 전: ");
        printArray(numbers);
        
        bubbleSort(numbers);
        
        System.out.print("정렬 후: ");
        printArray(numbers);
    }
}
```

#### 최적화 버전
```java
public class BubbleSortOptimized {
    
    /**
     * 최적화된 버블 정렬 구현
     * 
     * @param arr 정렬할 배열
     */
    public static void bubbleSortOptimized(int[] arr) {
        int n = arr.length;
        
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;  // 교환 발생 여부 플래그
            
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    // swap
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            
            // 교환이 없었다면 이미 정렬됨
            if (!swapped) {
                break;
            }
        }
    }
    
    public static void main(String[] args) {
        int[] numbers = {1, 2, 3, 4, 5};  // 이미 정렬된 배열
        
        System.out.print("정렬 전: ");
        printArray(numbers);
        
        bubbleSortOptimized(numbers);  // 1번의 순회만 수행
        
        System.out.print("정렬 후: ");
        printArray(numbers);
    }
}
```

## 참고 자료

- [위키백과 - 버블 정렬](https://ko.wikipedia.org/wiki/%EA%B1%B0%ED%92%88_%EC%A0%95%EB%A0%AC)
- [VisuAlgo - 정렬 시각화](https://visualgo.net/en/sorting)
- Introduction to Algorithms (CLRS)