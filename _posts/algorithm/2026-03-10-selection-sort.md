---
title: "[Algorithm] 선택 정렬 (Selection Sort) 완벽 정리"

tagline: "최소값을 찾아 정렬하는 직관적인 알고리즘, 선택 정렬의 원리와 구현"

header:
  overlay_image: /assets/post/algorithm/2026-03-10-selection-sort/overlay.png
  overlay_filter: 0.5

categories:
  - Algorithm

tags:
  - 알고리즘
  - Algorithm
  - 선택정렬
  - SelectionSort
  - 정렬
  - Sorting
  - Python
  - Java

toc: true
show_date: true

last_modified_at: 2026-03-10T21:20:00+09:00
---

선택 정렬(Selection Sort)은 가장 직관적인 정렬 알고리즘 중 하나입니다. 이름처럼 배열에서 최소값을 **선택**하여 정렬되지 않은 부분의 맨 앞으로 이동시키는 방식입니다. 구현이 간단하고 동작 원리가 명확하여 알고리즘 학습의 기초로 많이 사용됩니다.

## 선택 정렬이란?

선택 정렬은 **정렬되지 않은 부분에서 최소값을 찾아 맨 앞으로 이동**시키는 알고리즘입니다. 배열을 정렬된 부분과 정렬되지 않은 부분으로 나누고, 정렬되지 않은 부분에서 가장 작은 원소를 찾아 정렬된 부분의 끝에 추가합니다.

### 동작 원리

1. 배열에서 최소값을 찾음
2. 최소값을 정렬되지 않은 부분의 맨 앞 원소와 교환
3. 정렬된 부분을 제외한 나머지 부분에서 1~2 과정을 반복
4. 모든 원소가 정렬될 때까지 반복

## 인터랙티브 애니메이션

위에서 설명한 선택 정렬 과정을 직접 눈으로 확인해보세요! 시작 버튼을 눌러 정렬 과정을 단계별로 관찰할 수 있습니다.

{% include algorithm/selection-sort-animation.html %}

- **회색**: 정렬되지 않은 기본 상태
- **노란색**: 현재 최소값을 찾기 위해 비교 중인 요소
- **파란색**: 현재까지 찾은 최소값
- **빨간색**: 교환이 발생하는 요소 (위치 이동 애니메이션)
- **초록색**: 정렬이 완료된 요소
- 속도를 조절하여 천천히 또는 빠르게 확인할 수 있습니다.

## 복잡도 분석

### 시간 복잡도

| 경우 | 복잡도 | 설명 |
|------|--------|------|
| **최선 (Best)** | O(n²) | 이미 정렬된 경우에도 모든 비교 수행 |
| **평균 (Average)** | O(n²) | 일반적인 경우 |
| **최악 (Worst)** | O(n²) | 역순으로 정렬된 경우 |

#### 계산 과정
- 비교 횟수: (n-1) + (n-2) + ... + 2 + 1 = n(n-1)/2
- 교환 횟수: **최대 n-1번** (버블 정렬보다 적음)
- 따라서 시간 복잡도는 **O(n²)**

**특징**: 선택 정렬은 데이터의 초기 상태와 관계없이 항상 O(n²)의 비교 연산을 수행합니다.

### 공간 복잡도

- **O(1)**: 제자리 정렬(in-place sorting)
- 추가 메모리를 거의 사용하지 않음 (교환용 임시 변수만 필요)

## 장단점

### 장점 ✅

1. **구현이 매우 간단하다**: 코드가 직관적이고 이해하기 쉬움
2. **추가 메모리가 거의 필요 없다**: 공간 복잡도 O(1)
3. **교환 횟수가 적다**: 최대 n-1번의 교환만 발생 (비용이 큰 교환 연산에 유리)
4. **작은 데이터에 효율적**: 데이터가 적을 때는 구현이 간단하여 유용
5. **데이터 이동 비용이 클 때 유리**: 교환 횟수가 적어 데이터 이동이 비싼 경우 적합

### 단점 ❌

1. **느린 속도**: 시간 복잡도 O(n²)로 대용량 데이터에 부적합
2. **불안정 정렬(Unstable Sort)**: 같은 값의 순서가 보장되지 않음
3. **최선의 경우에도 O(n²)**: 이미 정렬된 배열도 모든 비교를 수행
4. **실무에서 사용하지 않음**: 더 빠른 알고리즘 사용

## 버블 정렬과의 비교

선택 정렬과 버블 정렬은 모두 O(n²) 알고리즘이지만 중요한 차이가 있습니다:

| 특성 | 선택 정렬 | 버블 정렬 |
|------|-----------|-----------|
| **비교 방식** | 최소값을 찾아 교환 | 인접한 원소를 비교하여 교환 |
| **교환 횟수** | 최대 n-1번 | 최대 n(n-1)/2번 |
| **안정성** | 불안정 정렬 | 안정 정렬 |
| **최선의 경우** | O(n²) | O(n) (최적화 시) |
| **실제 성능** | 일반적으로 버블 정렬보다 빠름 | 교환이 많아 느림 |

**언제 선택 정렬이 더 나을까?**
- 교환 연산의 비용이 비교 연산보다 훨씬 클 때
- 메모리 쓰기 작업이 비쌀 때 (예: Flash 메모리)
- 코드 단순성이 최우선일 때

## 안정성 문제

선택 정렬은 **불안정 정렬**입니다. 같은 값을 가진 원소들의 상대적 순서가 유지되지 않습니다.

```python
# 예시: [4a, 2, 4b, 1] 정렬
# a, b는 같은 값이지만 구분을 위한 표시

# 1단계: 최소값 1을 찾아 맨 앞과 교환
# [1, 2, 4b, 4a]  <- 4a와 1이 교환되면서 4a와 4b의 순서가 바뀜

# 결과: [1, 2, 4b, 4a] 
# 원래 순서(4a -> 4b)가 바뀜 (4b -> 4a)
```

안정성이 필요한 경우에는 **삽입 정렬**이나 **병합 정렬**을 사용해야 합니다.

## 최적화 기법

### 1. 양방향 선택 정렬

한 번의 순회에서 최소값과 최대값을 동시에 찾아 양쪽 끝에 배치합니다.

```python
def bidirectional_selection_sort(arr):
    """
    양방향 선택 정렬
    최소값과 최대값을 동시에 찾아 배치
    """
    left = 0
    right = len(arr) - 1
    
    while left < right:
        min_idx = left
        max_idx = right
        
        # 최소값과 최대값을 동시에 찾기
        for i in range(left, right + 1):
            if arr[i] < arr[min_idx]:
                min_idx = i
            if arr[i] > arr[max_idx]:
                max_idx = i
        
        # 최소값을 왼쪽 끝으로
        arr[left], arr[min_idx] = arr[min_idx], arr[left]
        
        # 최대값이 left에 있었다면 min_idx로 이동했으므로 조정
        if max_idx == left:
            max_idx = min_idx
        
        # 최대값을 오른쪽 끝으로
        arr[right], arr[max_idx] = arr[max_idx], arr[right]
        
        left += 1
        right -= 1
    
    return arr
```

이 최적화는 비교 횟수는 동일하지만 교환 횟수를 절반으로 줄일 수 있습니다.

### 2. 힙 선택 정렬

선택 정렬의 아이디어를 발전시켜 힙 자료구조를 사용하면 **힙 정렬(Heap Sort)**이 됩니다. 이는 O(n log n)의 시간 복잡도를 가집니다.

## 언제 사용해야 할까?

### 적합한 경우 ✅
- 데이터가 매우 작을 때 (10~20개 이하)
- 교환 연산이 비교 연산보다 훨씬 비쌀 때
- 메모리 쓰기가 제한적인 환경 (Flash 메모리 등)
- 코드의 단순성이 최우선일 때
- 알고리즘 학습 목적

### 부적합한 경우 ❌
- 대용량 데이터 (100개 이상)
- 안정 정렬이 필요한 경우
- 성능이 중요한 실무 프로젝트
- 이미 정렬된 데이터 (최적화 불가)

## 다른 정렬 알고리즘과 비교

| 알고리즘 | 최선 | 평균 | 최악 | 공간 | 안정성 | 교환 횟수 |
|----------|------|------|------|------|--------|-----------|
| 선택 정렬 | O(n²) | O(n²) | O(n²) | O(1) | ❌ | O(n) |
| 버블 정렬 | O(n) | O(n²) | O(n²) | O(1) | ✅ | O(n²) |
| 삽입 정렬 | O(n) | O(n²) | O(n²) | O(1) | ✅ | O(n²) |
| 퀵 정렬 | O(n log n) | O(n log n) | O(n²) | O(log n) | ❌ | O(n log n) |
| 병합 정렬 | O(n log n) | O(n log n) | O(n log n) | O(n) | ✅ | O(n log n) |
| 힙 정렬 | O(n log n) | O(n log n) | O(n log n) | O(1) | ❌ | O(n log n) |

**선택 정렬의 장점: 교환 횟수가 가장 적음 (최대 n-1번)**

## 실전 문제 연습

선택 정렬은 코딩 테스트에서 직접 구현하거나 원리를 응용하는 문제로 출제됩니다.

### 예제 문제

1. **정렬 과정 출력하기**: 선택 정렬의 각 단계를 출력하는 프로그램 작성
2. **K번째 선택 결과**: K번 선택 후 배열의 상태 출력
3. **최소 교환 횟수**: 선택 정렬이 필요로 하는 교환 횟수 계산
4. **K번째 작은 수 찾기**: 선택 정렬의 원리를 이용하여 K번째 작은 수 찾기

```python
def selection_sort_with_steps(arr):
    """정렬 과정을 출력하는 선택 정렬"""
    n = len(arr)
    print(f"초기 상태: {arr}")
    
    for i in range(n - 1):
        min_idx = i
        
        # 최소값 찾기
        for j in range(i + 1, n):
            if arr[j] < arr[min_idx]:
                min_idx = j
        
        # 교환이 필요한 경우
        if min_idx != i:
            arr[i], arr[min_idx] = arr[min_idx], arr[i]
            print(f"{i + 1}회차: 인덱스 {i}와 {min_idx} 교환 -> {arr}")
        else:
            print(f"{i + 1}회차: 교환 없음 (이미 최소값) -> {arr}")
    
    return arr


def find_kth_smallest(arr, k):
    """
    선택 정렬의 원리를 이용한 K번째 작은 수 찾기
    (완전히 정렬할 필요 없이 K번만 수행)
    """
    n = len(arr)
    
    for i in range(k):
        min_idx = i
        for j in range(i + 1, n):
            if arr[j] < arr[min_idx]:
                min_idx = j
        arr[i], arr[min_idx] = arr[min_idx], arr[i]
    
    return arr[k - 1]


# 실행 예시
numbers = [5, 3, 8, 4, 2]
print("=== 정렬 과정 ===")
selection_sort_with_steps(numbers.copy())

print("\n=== K번째 작은 수 찾기 ===")
numbers2 = [5, 3, 8, 4, 2, 9, 1]
k = 3
result = find_kth_smallest(numbers2.copy(), k)
print(f"{k}번째 작은 수: {result}")
```

## 마무리

선택 정렬은 직관적이고 구현이 간단한 정렬 알고리즘입니다. 실무에서는 거의 사용되지 않지만, 알고리즘의 기본 개념을 이해하는 데 매우 유용합니다.

### 핵심 요약

1. **최소값을 선택하여 맨 앞으로 이동**하는 직관적인 알고리즘
2. 시간 복잡도는 **모든 경우 O(n²)**, 공간 복잡도는 **O(1)**
3. **불안정 정렬**이지만 **교환 횟수가 적음** (최대 n-1번)
4. 버블 정렬보다 실제로는 빠르게 동작함
5. 교환 비용이 비쌀 때 유리

선택 정렬을 이해했다면, 다음 단계로:
- **삽입 정렬(Insertion Sort)**: 부분적으로 정렬된 데이터에 효율적
- **힙 정렬(Heap Sort)**: 선택 정렬의 발전형으로 O(n log n)
- **퀵 정렬(Quick Sort)**: 실무에서 가장 많이 사용되는 O(n log n) 알고리즘

을 학습하는 것을 추천합니다.

## 코드 구현

### Python 구현

#### 기본 버전
```python
def selection_sort(arr):
    """
    선택 정렬 기본 구현
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 정렬된 리스트
    """
    n = len(arr)
    
    # 정렬되지 않은 부분에서 최소값 찾기
    for i in range(n - 1):
        min_idx = i  # 최소값의 인덱스
        
        # i+1부터 끝까지 최소값 찾기
        for j in range(i + 1, n):
            if arr[j] < arr[min_idx]:
                min_idx = j
        
        # 최소값을 현재 위치와 교환
        arr[i], arr[min_idx] = arr[min_idx], arr[i]
    
    return arr


# 사용 예시
numbers = [5, 3, 8, 4, 2]
print(f"정렬 전: {numbers}")
selection_sort(numbers)
print(f"정렬 후: {numbers}")
```

#### 내림차순 버전
```python
def selection_sort_descending(arr):
    """
    선택 정렬 내림차순 구현
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 내림차순으로 정렬된 리스트
    """
    n = len(arr)
    
    for i in range(n - 1):
        max_idx = i  # 최대값의 인덱스
        
        # i+1부터 끝까지 최대값 찾기
        for j in range(i + 1, n):
            if arr[j] > arr[max_idx]:
                max_idx = j
        
        # 최대값을 현재 위치와 교환
        arr[i], arr[max_idx] = arr[max_idx], arr[i]
    
    return arr


# 사용 예시
numbers = [5, 3, 8, 4, 2]
print(f"정렬 전: {numbers}")
selection_sort_descending(numbers)
print(f"정렬 후 (내림차순): {numbers}")
```

#### 제네릭 버전 (비교 함수 사용)
```python
def selection_sort_generic(arr, key=None, reverse=False):
    """
    제네릭 선택 정렬 구현
    
    Args:
        arr (list): 정렬할 리스트
        key (function): 비교 기준 함수
        reverse (bool): True면 내림차순, False면 오름차순
    
    Returns:
        list: 정렬된 리스트
    """
    n = len(arr)
    
    for i in range(n - 1):
        selected_idx = i
        
        for j in range(i + 1, n):
            # key 함수가 있으면 적용
            val_j = key(arr[j]) if key else arr[j]
            val_selected = key(arr[selected_idx]) if key else arr[selected_idx]
            
            # reverse에 따라 비교
            if reverse:
                if val_j > val_selected:
                    selected_idx = j
            else:
                if val_j < val_selected:
                    selected_idx = j
        
        arr[i], arr[selected_idx] = arr[selected_idx], arr[i]
    
    return arr


# 사용 예시
# 1. 문자열 길이로 정렬
words = ["apple", "pie", "banana", "cat"]
selection_sort_generic(words, key=len)
print(f"길이 순 정렬: {words}")

# 2. 튜플의 두 번째 요소로 정렬
data = [(1, 5), (3, 2), (2, 8), (4, 1)]
selection_sort_generic(data, key=lambda x: x[1])
print(f"두 번째 요소 순 정렬: {data}")

# 3. 절댓값으로 정렬
numbers = [-5, 3, -8, 4, -2]
selection_sort_generic(numbers, key=abs)
print(f"절댓값 순 정렬: {numbers}")
```

### Java 구현

#### 기본 버전
```java
public class SelectionSort {
    
    /**
     * 선택 정렬 기본 구현
     * 
     * @param arr 정렬할 배열
     */
    public static void selectionSort(int[] arr) {
        int n = arr.length;
        
        // 정렬되지 않은 부분에서 최소값 찾기
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;  // 최소값의 인덱스
            
            // i+1부터 끝까지 최소값 찾기
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                }
            }
            
            // 최소값을 현재 위치와 교환
            int temp = arr[i];
            arr[i] = arr[minIdx];
            arr[minIdx] = temp;
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
        
        selectionSort(numbers);
        
        System.out.print("정렬 후: ");
        printArray(numbers);
    }
}
```

#### 제네릭 버전
```java
import java.util.Comparator;

public class SelectionSortGeneric {
    
    /**
     * 제네릭 선택 정렬 구현
     * 
     * @param arr 정렬할 배열
     * @param comparator 비교자 (null이면 자연 순서)
     */
    public static <T extends Comparable<T>> void selectionSort(
            T[] arr, Comparator<T> comparator) {
        int n = arr.length;
        
        for (int i = 0; i < n - 1; i++) {
            int selectedIdx = i;
            
            for (int j = i + 1; j < n; j++) {
                // Comparator가 있으면 사용, 없으면 자연 순서
                int cmp = (comparator != null) 
                    ? comparator.compare(arr[j], arr[selectedIdx])
                    : arr[j].compareTo(arr[selectedIdx]);
                
                if (cmp < 0) {
                    selectedIdx = j;
                }
            }
            
            // 교환
            T temp = arr[i];
            arr[i] = arr[selectedIdx];
            arr[selectedIdx] = temp;
        }
    }
    
    /**
     * 자연 순서로 정렬 (오름차순)
     */
    public static <T extends Comparable<T>> void selectionSort(T[] arr) {
        selectionSort(arr, null);
    }
    
    public static void main(String[] args) {
        // Integer 배열 정렬
        Integer[] numbers = {5, 3, 8, 4, 2};
        System.out.print("정렬 전: ");
        printArray(numbers);
        
        selectionSort(numbers);
        
        System.out.print("정렬 후: ");
        printArray(numbers);
        
        // 내림차순 정렬
        Integer[] numbers2 = {5, 3, 8, 4, 2};
        selectionSort(numbers2, Comparator.reverseOrder());
        
        System.out.print("내림차순: ");
        printArray(numbers2);
        
        // 문자열 길이로 정렬
        String[] words = {"apple", "pie", "banana", "cat"};
        selectionSort(words, Comparator.comparingInt(String::length));
        
        System.out.print("길이 순: ");
        printArray(words);
    }
    
    private static <T> void printArray(T[] arr) {
        for (T item : arr) {
            System.out.print(item + " ");
        }
        System.out.println();
    }
}
```

#### 정렬 과정 시각화 버전
```java
public class SelectionSortVisualized {
    
    /**
     * 선택 정렬 과정을 시각화하여 출력
     * 
     * @param arr 정렬할 배열
     */
    public static void selectionSortWithVisualization(int[] arr) {
        int n = arr.length;
        
        System.out.println("초기 상태:");
        printArrayWithHighlight(arr, -1, -1);
        System.out.println();
        
        for (int i = 0; i < n - 1; i++) {
            System.out.println("--- " + (i + 1) + "회차 ---");
            int minIdx = i;
            
            // 최소값 찾기
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                }
            }
            
            // 교환 전 상태
            System.out.println("최소값 찾음: arr[" + minIdx + "] = " + arr[minIdx]);
            
            // 교환
            if (minIdx != i) {
                System.out.println("교환: arr[" + i + "] ↔ arr[" + minIdx + "]");
                int temp = arr[i];
                arr[i] = arr[minIdx];
                arr[minIdx] = temp;
            } else {
                System.out.println("교환 없음 (이미 최소값)");
            }
            
            // 교환 후 상태
            printArrayWithHighlight(arr, i, -1);
            System.out.println();
        }
        
        System.out.println("정렬 완료!");
        printArrayWithHighlight(arr, n - 1, -1);
    }
    
    /**
     * 배열을 출력하며 특정 위치를 강조
     */
    private static void printArrayWithHighlight(int[] arr, int sortedUntil, int current) {
        for (int i = 0; i < arr.length; i++) {
            if (i <= sortedUntil) {
                System.out.print("[" + arr[i] + "]");  // 정렬된 부분
            } else if (i == current) {
                System.out.print("(" + arr[i] + ")");  // 현재 비교 중
            } else {
                System.out.print(" " + arr[i] + " ");   // 정렬 안 된 부분
            }
            System.out.print(" ");
        }
        System.out.println();
    }
    
    public static void main(String[] args) {
        int[] numbers = {5, 3, 8, 4, 2};
        selectionSortWithVisualization(numbers);
    }
}
```

## 참고 자료

- [위키백과 - 선택 정렬](https://ko.wikipedia.org/wiki/%EC%84%A0%ED%83%9D_%EC%A0%95%EB%A0%AC)
- [VisuAlgo - 정렬 시각화](https://visualgo.net/en/sorting)
- Introduction to Algorithms (CLRS)
- [GeeksforGeeks - Selection Sort](https://www.geeksforgeeks.org/selection-sort/)
