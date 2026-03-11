---
title: "[Algorithm] 퀵 정렬 (Quick Sort) 완벽 정리"

tagline: "분할 정복으로 평균 최고의 성능을 자랑하는 정렬 알고리즘"

header:
  overlay_image: /assets/post/algorithm/2026-03-11-quick-sort/overlay.png
  overlay_filter: 0.5

categories:
  - Algorithm

tags:
  - 알고리즘
  - Algorithm
  - 퀵정렬
  - QuickSort
  - 정렬
  - Sorting
  - Python
  - Java
  - 분할정복
  - DivideAndConquer

toc: true
show_date: true

last_modified_at: 2026-03-11T23:50:00
---

퀵 정렬(Quick Sort)은 **분할 정복(Divide and Conquer)** 전략을 사용하는 정렬 알고리즘으로, 평균적으로 가장 빠른 성능을 보입니다. 피벗(pivot)을 기준으로 데이터를 분할하고 재귀적으로 정렬하는 방식으로, 실무에서 가장 많이 사용되는 정렬 알고리즘 중 하나입니다.

## 퀵 정렬이란?

퀵 정렬은 **피벗(pivot)이라는 기준 값을 선택하여 피벗보다 작은 값은 왼쪽, 큰 값은 오른쪽으로 분할(partition)** 한 후, 각 부분을 재귀적으로 정렬하는 알고리즘입니다. 찰스 앤터니 리처드 호어(C.A.R. Hoare)가 1960년에 개발했으며, 평균적으로 O(n log n)의 시간 복잡도를 가집니다.

### 동작 원리

1. **피벗 선택**: 배열에서 피벗(기준 값)을 하나 선택
2. **분할(Partition)**: 피벗보다 작은 값은 왼쪽, 큰 값은 오른쪽으로 재배치
3. **재귀 정렬**: 분할된 왼쪽 부분과 오른쪽 부분을 각각 재귀적으로 퀵 정렬
4. **종료 조건**: 부분 배열의 크기가 1 이하면 종료

퀵 정렬의 핵심은 **분할(Partition) 과정**입니다. 피벗을 기준으로 배열을 효율적으로 재배치하는 것이 성능의 관건입니다.

## 인터랙티브 애니메이션

위에서 설명한 퀵 정렬 과정을 직접 눈으로 확인해보세요! 시작 버튼을 눌러 정렬 과정을 단계별로 관찰할 수 있습니다.

{% include algorithm/quick-sort-animation.html %}

- **보라색**: 현재 피벗 (pivot)
- **파란색**: 피벗보다 작은 값 (왼쪽 영역)
- **주황색**: 피벗보다 큰 값 (오른쪽 영역)
- **노란색**: 현재 비교 중인 요소
- **초록색**: 정렬이 완료된 요소
- **회색**: 아직 처리되지 않은 요소
- 속도를 조절하여 분할 과정을 천천히 확인할 수 있습니다.
- 재귀적으로 좌우 부분이 정렬되는 과정을 볼 수 있습니다.

## 복잡도 분석

### 시간 복잡도

| 경우 | 복잡도 | 설명 |
|------|--------|------|
| **최선 (Best)** | O(n log n) | 피벗이 항상 중간값일 때 |
| **평균 (Average)** | O(n log n) | 일반적인 경우 |
| **최악 (Worst)** | O(n²) | 피벗이 항상 최솟값 또는 최댓값일 때 |

#### 계산 과정

**평균 및 최선의 경우 (O(n log n))**:
- 분할 단계: 배열을 피벗 기준으로 2개로 분할 → 재귀 깊이 log n
- 각 단계에서 n개의 원소 비교 → O(n)
- 총 시간 복잡도: O(n) × O(log n) = **O(n log n)**

**최악의 경우 (O(n²))**:
- 이미 정렬된 배열에서 첫 번째 또는 마지막 원소를 피벗으로 선택
- 한쪽으로만 분할되어 재귀 깊이 n
- 총 시간 복잡도: O(n) × O(n) = **O(n²)**
- 예시: [1, 2, 3, 4, 5]에서 항상 마지막 원소를 피벗으로 선택

**중요한 특징**: 
- 평균적으로 병합 정렬보다 **2~3배 빠름** (상수 계수가 작음)
- **피벗 선택 전략**이 성능에 큰 영향을 미침
- 캐시 효율성이 좋아 실전에서 매우 빠름

### 공간 복잡도

- **평균 O(log n)**: 재귀 호출 스택 공간
- **최악 O(n)**: 한쪽으로만 분할될 때 재귀 깊이 n
- 제자리 정렬(in-place sorting)이므로 추가 배열은 불필요
- 병합 정렬(O(n))보다 공간 효율적

## 장단점

### 장점 ✅

1. **평균적으로 가장 빠르다**: O(n log n)으로 실전에서 최고 성능
2. **제자리 정렬**: 추가 메모리가 거의 필요 없음 (O(log n) 스택만)
3. **캐시 효율성**: 참조 지역성(locality of reference)이 좋음
4. **병합 정렬보다 실전에서 빠름**: 상수 계수가 작음
5. **분할 정복**: 병렬화하기 쉬움
6. **범용성**: 다양한 데이터 타입에 적용 가능

### 단점 ❌

1. **최악의 경우 O(n²)**: 피벗 선택이 나쁘면 성능 저하
2. **불안정 정렬(Unstable Sort)**: 같은 값의 순서가 보장되지 않음
3. **재귀적 구현**: 깊은 재귀로 인한 스택 오버플로우 가능
4. **이미 정렬된 데이터에 취약**: 피벗 선택 전략이 중요
5. **작은 배열에 비효율적**: 재귀 오버헤드

## 피벗 선택 전략

피벗 선택은 퀵 정렬의 성능을 결정하는 가장 중요한 요소입니다.

### 1. 첫 번째/마지막 원소 (단순 방식)

```python
def quick_sort_last_pivot(arr, low, high):
    """마지막 원소를 피벗으로 선택 (가장 단순)"""
    if low < high:
        pivot_idx = partition(arr, low, high)  # 마지막 원소 사용
        quick_sort_last_pivot(arr, low, pivot_idx - 1)
        quick_sort_last_pivot(arr, pivot_idx + 1, high)
```

**장점**: 구현이 간단  
**단점**: 정렬되거나 역순 배열에서 O(n²)

### 2. 랜덤 피벗 (Random Pivot)

```python
import random

def partition_random(arr, low, high):
    """랜덤하게 피벗 선택"""
    # 랜덤 인덱스 선택하여 마지막과 교환
    random_idx = random.randint(low, high)
    arr[random_idx], arr[high] = arr[high], arr[random_idx]
    return partition(arr, low, high)
```

**장점**: 최악의 경우 확률적으로 회피  
**단점**: 랜덤 함수 호출 오버헤드

### 3. 중간값 3개 (Median-of-Three)

```python
def median_of_three(arr, low, high):
    """
    첫 번째, 중간, 마지막 원소 중 중간값을 피벗으로 선택
    가장 실용적인 방법
    """
    mid = (low + high) // 2
    
    # 세 값 정렬
    if arr[low] > arr[mid]:
        arr[low], arr[mid] = arr[mid], arr[low]
    if arr[low] > arr[high]:
        arr[low], arr[high] = arr[high], arr[low]
    if arr[mid] > arr[high]:
        arr[mid], arr[high] = arr[high], arr[mid]
    
    # 중간값을 high-1 위치로 이동
    arr[mid], arr[high - 1] = arr[high - 1], arr[mid]
    return arr[high - 1]
```

**장점**: 좋은 피벗 선택 확률 높음  
**단점**: 약간의 추가 비교 연산

### 4. 중간값 5개/9개 (Median-of-Medians)

```python
def median_of_medians(arr, low, high):
    """
    5개씩 그룹으로 나누어 중간값들의 중간값 선택
    최악의 경우도 O(n log n) 보장
    """
    # 구현 복잡도가 높아 실전에서는 잘 사용하지 않음
    # 주로 이론적 관심사
    pass
```

**장점**: 최악의 경우도 O(n log n) 보장  
**단점**: 구현 복잡, 실전에서 오버헤드가 큼

**실무 권장사항**: **Median-of-Three**가 가장 실용적입니다!

## 분할(Partition) 기법

### 1. Lomuto Partition (단순하지만 느림)

```python
def lomuto_partition(arr, low, high):
    """
    Lomuto 분할 기법 (교재에서 많이 사용)
    피벗: arr[high]
    """
    pivot = arr[high]
    i = low - 1  # 작은 값들의 마지막 인덱스
    
    for j in range(low, high):
        if arr[j] <= pivot:
            i += 1
            arr[i], arr[j] = arr[j], arr[i]
    
    # 피벗을 올바른 위치로
    arr[i + 1], arr[high] = arr[high], arr[i + 1]
    return i + 1
```

**특징**:
- 구현이 직관적
- 중복 값이 많으면 비효율적
- 불필요한 교환이 많음

### 2. Hoare Partition (원조, 효율적)

```python
def hoare_partition(arr, low, high):
    """
    Hoare 분할 기법 (원조, 더 효율적)
    양쪽에서 동시에 탐색
    """
    pivot = arr[low]
    i = low - 1
    j = high + 1
    
    while True:
        # 왼쪽에서 피벗보다 큰 값 찾기
        i += 1
        while arr[i] < pivot:
            i += 1
        
        # 오른쪽에서 피벗보다 작은 값 찾기
        j -= 1
        while arr[j] > pivot:
            j -= 1
        
        # 교차하면 종료
        if i >= j:
            return j
        
        # 교환
        arr[i], arr[j] = arr[j], arr[i]
```

**특징**:
- 교환 횟수가 적음 (Lomuto의 약 1/3)
- 구현이 약간 더 복잡
- 실전에서 더 빠름

### 3. Three-Way Partition (중복 값 처리)

```python
def three_way_partition(arr, low, high):
    """
    3-way 분할 (Dijkstra's Dutch National Flag)
    중복 값이 많을 때 효율적
    [low...lt-1] < pivot, [lt...gt] == pivot, [gt+1...high] > pivot
    """
    pivot = arr[low]
    lt = low       # less than
    i = low + 1    # current
    gt = high      # greater than
    
    while i <= gt:
        if arr[i] < pivot:
            arr[lt], arr[i] = arr[i], arr[lt]
            lt += 1
            i += 1
        elif arr[i] > pivot:
            arr[i], arr[gt] = arr[gt], arr[i]
            gt -= 1
        else:  # arr[i] == pivot
            i += 1
    
    return lt, gt
```

**사용**:
```python
def quick_sort_3way(arr, low, high):
    """3-way 퀵 정렬"""
    if low < high:
        lt, gt = three_way_partition(arr, low, high)
        quick_sort_3way(arr, low, lt - 1)
        quick_sort_3way(arr, gt + 1, high)
```

**장점**: 중복 값이 많은 배열에서 성능 향상  
**실제 사용**: Java의 `Arrays.sort()`는 Dual-Pivot Quicksort 사용

## 다른 정렬 알고리즘과의 비교

퀵 정렬과 다른 주요 정렬 알고리즘의 특징을 비교해봅시다:

| 특성 | 퀵 정렬 | 병합 정렬 | 힙 정렬 | 삽입 정렬 |
|------|---------|-----------|---------|-----------|
| **평균 시간** | O(n log n) | O(n log n) | O(n log n) | O(n²) |
| **최악 시간** | O(n²) | O(n log n) | O(n log n) | O(n²) |
| **공간 복잡도** | O(log n) | O(n) | O(1) | O(1) |
| **안정성** | ❌ 불안정 | ✅ 안정 | ❌ 불안정 | ✅ 안정 |
| **제자리 정렬** | ✅ | ❌ | ✅ | ✅ |
| **캐시 효율성** | 높음 | 중간 | 낮음 | 높음 |
| **병렬화** | 쉬움 | 쉬움 | 어려움 | 어려움 |
| **실전 성능** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐ |

### 왜 퀵 정렬이 실전에서 가장 빠른가?

1. **캐시 효율성**: 연속된 메모리 접근으로 CPU 캐시 히트율 높음
2. **상수 계수**: 병합 정렬보다 실제로 2~3배 빠름
3. **제자리 정렬**: 추가 메모리 할당 최소화
4. **분기 예측**: 현대 CPU의 분기 예측에 유리

```
실제 성능 (1억 개 정수 정렬):
- 퀵 정렬:   2.5초
- 병합 정렬: 5.8초
- 힙 정렬:   8.2초
```

## 실무에서의 활용

### 1. 표준 라이브러리에서의 사용

퀵 정렬은 많은 프로그래밍 언어의 표준 정렬 함수의 기반입니다:

#### C/C++ `qsort()`, `std::sort()`
```cpp
// Introsort 사용 (퀵 정렬 + 힙 정렬 하이브리드)
std::sort(arr.begin(), arr.end());
```

#### Java `Arrays.sort()`
```java
// 기본 타입: Dual-Pivot Quicksort
// 객체: Timsort (병합 정렬 + 삽입 정렬)
Arrays.sort(arr);
```

#### Python `sorted()`, `list.sort()`
```python
# Timsort (병합 정렬 + 삽입 정렬)
# 안정 정렬이 필요하여 퀵 정렬 대신 Timsort 사용
sorted(arr)
```

### 2. Introsort (Introspective Sort)

실전에서 가장 많이 사용되는 하이브리드 알고리즘:

```python
import math

def introsort(arr):
    """
    Introsort: 퀵 정렬 + 힙 정렬 + 삽입 정렬
    C++ std::sort()의 실제 구현
    """
    max_depth = 2 * math.floor(math.log2(len(arr)))
    introsort_helper(arr, 0, len(arr) - 1, max_depth)

def introsort_helper(arr, low, high, max_depth):
    size = high - low + 1
    
    # 작은 배열: 삽입 정렬
    if size <= 16:
        insertion_sort_range(arr, low, high)
        return
    
    # 재귀 깊이 초과: 힙 정렬
    if max_depth == 0:
        heapsort_range(arr, low, high)
        return
    
    # 기본: 퀵 정렬 (Median-of-Three)
    pivot_idx = partition_median_of_three(arr, low, high)
    introsort_helper(arr, low, pivot_idx - 1, max_depth - 1)
    introsort_helper(arr, pivot_idx + 1, high, max_depth - 1)
```

**특징**:
- 퀵 정렬의 빠른 평균 성능
- 힙 정렬로 최악의 경우 방어 → **O(n log n) 보장**
- 삽입 정렬로 작은 배열 최적화
- C++ STL의 `std::sort()` 실제 구현

### 3. k번째 작은 원소 찾기 (Quickselect)

퀵 정렬의 분할을 이용한 선택 알고리즘:

```python
def quickselect(arr, k):
    """
    배열에서 k번째로 작은 원소 찾기
    평균 O(n), 최악 O(n²)
    """
    def select(arr, low, high, k):
        if low == high:
            return arr[low]
        
        # 분할
        pivot_idx = partition(arr, low, high)
        
        # k번째 원소의 위치 확인
        if k == pivot_idx:
            return arr[k]
        elif k < pivot_idx:
            return select(arr, low, pivot_idx - 1, k)
        else:
            return select(arr, pivot_idx + 1, high, k)
    
    return select(arr, 0, len(arr) - 1, k)

# 사용 예시
arr = [7, 10, 4, 3, 20, 15]
print(f"3번째로 작은 원소: {quickselect(arr, 2)}")  # 7 (0-indexed)
```

**응용**:
- 중앙값(median) 찾기: `quickselect(arr, len(arr) // 2)`
- 백분위수(percentile) 계산
- Top-K 문제

## 최적화 기법

### 1. 작은 배열은 삽입 정렬

```python
CUTOFF = 10  # 임계값

def hybrid_quick_sort(arr, low, high):
    """작은 부분 배열은 삽입 정렬 사용"""
    if high - low + 1 <= CUTOFF:
        insertion_sort_range(arr, low, high)
    elif low < high:
        pivot_idx = partition(arr, low, high)
        hybrid_quick_sort(arr, low, pivot_idx - 1)
        hybrid_quick_sort(arr, pivot_idx + 1, high)
```

**효과**: 재귀 오버헤드 감소, 5~15% 성능 향상

### 2. 꼬리 재귀 최적화 (Tail Recursion)

```python
def quick_sort_tail_optimized(arr, low, high):
    """
    꼬리 재귀 최적화로 스택 사용량 감소
    항상 작은 부분부터 재귀, 큰 부분은 반복
    """
    while low < high:
        # 분할
        pivot_idx = partition(arr, low, high)
        
        # 작은 부분을 재귀 (스택 사용 최소화)
        if pivot_idx - low < high - pivot_idx:
            quick_sort_tail_optimized(arr, low, pivot_idx - 1)
            low = pivot_idx + 1
        else:
            quick_sort_tail_optimized(arr, pivot_idx + 1, high)
            high = pivot_idx - 1
```

**효과**: 
- 스택 깊이를 O(log n)으로 보장
- 스택 오버플로우 방지

### 3. 반복적 구현 (Iterative)

```python
def quick_sort_iterative(arr):
    """
    스택을 이용한 반복적 퀵 정렬
    재귀 대신 명시적 스택 사용
    """
    stack = [(0, len(arr) - 1)]
    
    while stack:
        low, high = stack.pop()
        
        if low < high:
            pivot_idx = partition(arr, low, high)
            
            # 양쪽 부분을 스택에 추가
            stack.append((low, pivot_idx - 1))
            stack.append((pivot_idx + 1, high))
    
    return arr
```

**장점**: 재귀 호출 오버헤드 없음, 스택 오버플로우 없음

## 언제 사용해야 할까?

### 적합한 경우 ✅

- **대용량 데이터**: 1000개 이상
- **평균 성능이 중요**: 일반적인 데이터
- **메모리가 제한적**: 제자리 정렬 필요
- **실전 속도 우선**: 캐시 효율성 중요
- **병렬 처리**: 멀티코어 활용 가능
- **재귀 깊이 문제없음**: 스택 크기 충분

### 부적합한 경우 ❌

- **최악의 경우 방어 필수**: 병합 정렬이나 힙 정렬 사용
- **안정 정렬 필요**: 병합 정렬이나 Timsort 사용
- **거의 정렬된 데이터**: 삽입 정렬이나 Timsort가 더 빠름
- **작은 데이터**: 삽입 정렬이 더 간단하고 빠름
- **재귀 금지 환경**: 반복적 구현 또는 다른 알고리즘

### 알고리즘 선택 가이드

```
데이터 크기와 조건에 따른 권장사항:

1. 매우 작음 (< 10개)      → 삽입 정렬
2. 작음 (10~50개)          → 삽입 정렬 or 퀵 정렬
3. 중간 (50~1000개)        → 퀵 정렬
4. 큼 (> 1000개)           → Introsort (퀵+힙+삽입)
5. 거의 정렬됨             → Timsort or 삽입 정렬
6. 안정 정렬 필요          → 병합 정렬 or Timsort
7. 최악 케이스 방어 필요   → 힙 정렬 or 병합 정렬
8. 메모리 제한             → 퀵 정렬 or 힙 정렬
```

## 실전 문제 연습

퀵 정렬은 코딩 테스트에서 구현 문제뿐만 아니라 응용 문제로도 자주 출제됩니다.

### 예제 1: 색깔별 정렬 (Dutch National Flag)

```python
def sort_colors(nums):
    """
    0(빨강), 1(흰색), 2(파랑)을 정렬
    LeetCode 75. Sort Colors
    """
    red = 0     # 빨강 영역의 끝
    white = 0   # 현재 처리 중
    blue = len(nums) - 1  # 파랑 영역의 시작
    
    while white <= blue:
        if nums[white] == 0:
            nums[red], nums[white] = nums[white], nums[red]
            red += 1
            white += 1
        elif nums[white] == 1:
            white += 1
        else:  # nums[white] == 2
            nums[white], nums[blue] = nums[blue], nums[white]
            blue -= 1
    
    return nums

# 테스트
print(sort_colors([2, 0, 2, 1, 1, 0]))  # [0, 0, 1, 1, 2, 2]
```

### 예제 2: k번째로 큰 원소

```python
def find_kth_largest(nums, k):
    """
    k번째로 큰 원소 찾기
    LeetCode 215. Kth Largest Element
    """
    def partition(arr, low, high):
        pivot = arr[high]
        i = low - 1
        
        for j in range(low, high):
            if arr[j] >= pivot:  # 내림차순
                i += 1
                arr[i], arr[j] = arr[j], arr[i]
        
        arr[i + 1], arr[high] = arr[high], arr[i + 1]
        return i + 1
    
    def quickselect(arr, low, high, k):
        if low <= high:
            pivot_idx = partition(arr, low, high)
            
            if pivot_idx == k - 1:
                return arr[pivot_idx]
            elif pivot_idx > k - 1:
                return quickselect(arr, low, pivot_idx - 1, k)
            else:
                return quickselect(arr, pivot_idx + 1, high, k)
    
    return quickselect(nums, 0, len(nums) - 1, k)

# 테스트
print(find_kth_largest([3, 2, 1, 5, 6, 4], 2))  # 5
```

### 예제 3: Top K 빈도수 원소

```python
from collections import Counter

def top_k_frequent(nums, k):
    """
    가장 빈도가 높은 k개의 원소
    LeetCode 347. Top K Frequent Elements
    """
    # 빈도수 계산
    count = Counter(nums)
    unique = list(count.keys())
    
    def partition(arr, low, high):
        pivot_freq = count[arr[high]]
        i = low - 1
        
        for j in range(low, high):
            if count[arr[j]] >= pivot_freq:  # 빈도수 내림차순
                i += 1
                arr[i], arr[j] = arr[j], arr[i]
        
        arr[i + 1], arr[high] = arr[high], arr[i + 1]
        return i + 1
    
    def quickselect(arr, low, high, k):
        if low <= high:
            pivot_idx = partition(arr, low, high)
            
            if pivot_idx == k - 1:
                return
            elif pivot_idx > k - 1:
                quickselect(arr, low, pivot_idx - 1, k)
            else:
                quickselect(arr, pivot_idx + 1, high, k)
    
    quickselect(unique, 0, len(unique) - 1, k)
    return unique[:k]

# 테스트
print(top_k_frequent([1, 1, 1, 2, 2, 3], 2))  # [1, 2]
```

### 예제 4: 배열 분할 (Partition Labels)

```python
def wiggle_sort(nums):
    """
    배열을 지그재그로 정렬: nums[0] <= nums[1] >= nums[2] <= nums[3]...
    LeetCode 280. Wiggle Sort
    """
    for i in range(len(nums) - 1):
        if (i % 2 == 0 and nums[i] > nums[i + 1]) or \
           (i % 2 == 1 and nums[i] < nums[i + 1]):
            nums[i], nums[i + 1] = nums[i + 1], nums[i]
    
    return nums

# 테스트
print(wiggle_sort([3, 5, 2, 1, 6, 4]))  # [3, 5, 1, 6, 2, 4] (가능한 답 중 하나)
```

## 디버깅 팁

### 1. 단계별 출력

```python
def quick_sort_debug(arr, low, high, depth=0):
    """정렬 과정을 출력하는 디버깅 버전"""
    indent = "  " * depth
    
    if low < high:
        print(f"{indent}정렬 범위: arr[{low}:{high+1}] = {arr[low:high+1]}")
        
        # 분할
        pivot_idx = partition_debug(arr, low, high, depth)
        print(f"{indent}피벗 위치: {pivot_idx}, 값: {arr[pivot_idx]}")
        print(f"{indent}분할 후: {arr[low:high+1]}")
        
        # 재귀
        quick_sort_debug(arr, low, pivot_idx - 1, depth + 1)
        quick_sort_debug(arr, pivot_idx + 1, high, depth + 1)
    else:
        print(f"{indent}종료: arr[{low}:{high+1}] = {arr[low:high+1] if low <= high else []}")

def partition_debug(arr, low, high, depth):
    """분할 과정을 출력"""
    indent = "  " * depth
    pivot = arr[high]
    print(f"{indent}피벗: {pivot}")
    
    i = low - 1
    for j in range(low, high):
        if arr[j] <= pivot:
            i += 1
            arr[i], arr[j] = arr[j], arr[i]
            print(f"{indent}  교환: {arr[i]} ↔ {arr[j]}")
    
    arr[i + 1], arr[high] = arr[high], arr[i + 1]
    return i + 1

# 테스트
arr = [5, 3, 8, 4, 2]
print("=== 퀵 정렬 디버깅 ===")
quick_sort_debug(arr, 0, len(arr) - 1)
print(f"\n최종 결과: {arr}")
```

### 2. 경계 조건 체크

```python
def quick_sort_safe(arr, low, high):
    """안전한 경계 조건 체크"""
    # 경계 확인
    if arr is None:
        raise ValueError("배열이 None입니다")
    if low < 0 or high >= len(arr):
        raise IndexError(f"인덱스 범위 오류: low={low}, high={high}, len={len(arr)}")
    
    if low < high:
        pivot_idx = partition(arr, low, high)
        quick_sort_safe(arr, low, pivot_idx - 1)
        quick_sort_safe(arr, pivot_idx + 1, high)
```

## 마무리

퀵 정렬은 실무에서 가장 널리 사용되는 정렬 알고리즘입니다. 평균적으로 O(n log n)의 빠른 성능과 제자리 정렬의 메모리 효율성을 갖추고 있어, 대부분의 프로그래밍 언어 표준 라이브러리에서 기본 정렬 알고리즘으로 채택되고 있습니다.

### 핵심 요약

1. **분할 정복** 전략으로 평균 O(n log n) 성능
2. **피벗 선택**이 성능의 핵심 (Median-of-Three 권장)
3. **제자리 정렬**로 공간 복잡도 O(log n)
4. **불안정 정렬**이지만 실전에서 가장 빠름
5. **최악의 경우 O(n²)** 방어가 중요 (Introsort)
6. **Quickselect**: k번째 원소 찾기에 응용
7. **하이브리드 알고리즘**: 작은 배열은 삽입 정렬
8. **실무 표준**: C++ STL, Java Arrays.sort() 등에서 사용

### 다음 학습 추천

퀵 정렬을 이해했다면, 다음 주제로:
- **병합 정렬(Merge Sort)**: 안정적인 O(n log n), 분할 정복
- **힙 정렬(Heap Sort)**: 최악의 경우에도 O(n log n) 보장
- **Introsort**: 퀵+힙+삽입 하이브리드 (C++ STL 실제 구현)
- **Timsort**: 병합+삽입 하이브리드 (Python 기본 정렬)
- **기수 정렬(Radix Sort)**: O(nk) 비-비교 정렬

을 학습하여 정렬 알고리즘의 전체적인 이해도를 높이는 것을 추천합니다!

## 코드 구현

### Python 구현

#### 기본 버전 (Lomuto Partition)
```python
def quick_sort(arr):
    """
    퀵 정렬 기본 구현 (Lomuto Partition)
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 정렬된 리스트
    """
    def partition(arr, low, high):
        """배열을 피벗 기준으로 분할"""
        pivot = arr[high]  # 마지막 원소를 피벗으로
        i = low - 1        # 작은 값들의 마지막 인덱스
        
        for j in range(low, high):
            if arr[j] <= pivot:
                i += 1
                arr[i], arr[j] = arr[j], arr[i]
        
        # 피벗을 올바른 위치로
        arr[i + 1], arr[high] = arr[high], arr[i + 1]
        return i + 1
    
    def quick_sort_helper(arr, low, high):
        """재귀적으로 정렬"""
        if low < high:
            # 분할
            pivot_idx = partition(arr, low, high)
            
            # 좌우 재귀 정렬
            quick_sort_helper(arr, low, pivot_idx - 1)
            quick_sort_helper(arr, pivot_idx + 1, high)
    
    quick_sort_helper(arr, 0, len(arr) - 1)
    return arr


# 사용 예시
numbers = [5, 3, 8, 4, 2, 7, 1, 10]
print(f"정렬 전: {numbers}")
quick_sort(numbers)
print(f"정렬 후: {numbers}")
```

#### Hoare Partition 버전
```python
def quick_sort_hoare(arr):
    """
    퀵 정렬 (Hoare Partition)
    더 효율적인 분할 방식
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 정렬된 리스트
    """
    def hoare_partition(arr, low, high):
        """Hoare의 분할 방식"""
        pivot = arr[low]
        i = low - 1
        j = high + 1
        
        while True:
            # 왼쪽에서 피벗보다 큰 값 찾기
            i += 1
            while arr[i] < pivot:
                i += 1
            
            # 오른쪽에서 피벗보다 작은 값 찾기
            j -= 1
            while arr[j] > pivot:
                j -= 1
            
            # 교차하면 종료
            if i >= j:
                return j
            
            # 교환
            arr[i], arr[j] = arr[j], arr[i]
    
    def quick_sort_helper(arr, low, high):
        if low < high:
            pivot_idx = hoare_partition(arr, low, high)
            quick_sort_helper(arr, low, pivot_idx)
            quick_sort_helper(arr, pivot_idx + 1, high)
    
    quick_sort_helper(arr, 0, len(arr) - 1)
    return arr


# 사용 예시
numbers = [5, 3, 8, 4, 2, 7, 1, 10]
print(f"정렬 전: {numbers}")
quick_sort_hoare(numbers)
print(f"정렬 후: {numbers}")
```

#### 3-Way Partition 버전
```python
def quick_sort_3way(arr):
    """
    3-way 퀵 정렬
    중복 값이 많을 때 효율적
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 정렬된 리스트
    """
    def three_way_partition(arr, low, high):
        """
        3-way 분할
        < pivot, == pivot, > pivot으로 분할
        """
        pivot = arr[low]
        lt = low       # less than
        i = low + 1    # current
        gt = high      # greater than
        
        while i <= gt:
            if arr[i] < pivot:
                arr[lt], arr[i] = arr[i], arr[lt]
                lt += 1
                i += 1
            elif arr[i] > pivot:
                arr[i], arr[gt] = arr[gt], arr[i]
                gt -= 1
            else:  # arr[i] == pivot
                i += 1
        
        return lt, gt
    
    def quick_sort_helper(arr, low, high):
        if low < high:
            lt, gt = three_way_partition(arr, low, high)
            quick_sort_helper(arr, low, lt - 1)
            quick_sort_helper(arr, gt + 1, high)
    
    quick_sort_helper(arr, 0, len(arr) - 1)
    return arr


# 사용 예시 (중복 값 많음)
numbers = [5, 3, 8, 3, 2, 3, 1, 3, 10, 3]
print(f"정렬 전: {numbers}")
quick_sort_3way(numbers)
print(f"정렬 후: {numbers}")
```

#### Median-of-Three 버전
```python
def quick_sort_median_of_three(arr):
    """
    퀵 정렬 (Median-of-Three)
    가장 실용적인 피벗 선택 방법
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 정렬된 리스트
    """
    def median_of_three(arr, low, high):
        """첫 번째, 중간, 마지막 중 중간값 선택"""
        mid = (low + high) // 2
        
        # 세 값 정렬
        if arr[low] > arr[mid]:
            arr[low], arr[mid] = arr[mid], arr[low]
        if arr[low] > arr[high]:
            arr[low], arr[high] = arr[high], arr[low]
        if arr[mid] > arr[high]:
            arr[mid], arr[high] = arr[high], arr[mid]
        
        # 중간값을 high-1 위치로
        arr[mid], arr[high - 1] = arr[high - 1], arr[mid]
        return high - 1
    
    def partition(arr, low, high, pivot_idx):
        pivot = arr[pivot_idx]
        arr[pivot_idx], arr[high] = arr[high], arr[pivot_idx]
        
        i = low - 1
        for j in range(low, high):
            if arr[j] <= pivot:
                i += 1
                arr[i], arr[j] = arr[j], arr[i]
        
        arr[i + 1], arr[high] = arr[high], arr[i + 1]
        return i + 1
    
    def quick_sort_helper(arr, low, high):
        if low < high:
            if high - low >= 2:
                pivot_idx = median_of_three(arr, low, high)
            else:
                pivot_idx = high
            
            pivot_idx = partition(arr, low, high, pivot_idx)
            quick_sort_helper(arr, low, pivot_idx - 1)
            quick_sort_helper(arr, pivot_idx + 1, high)
    
    quick_sort_helper(arr, 0, len(arr) - 1)
    return arr


# 사용 예시
numbers = [5, 3, 8, 4, 2, 7, 1, 10, 9, 6]
print(f"정렬 전: {numbers}")
quick_sort_median_of_three(numbers)
print(f"정렬 후: {numbers}")
```

#### 반복적 구현
```python
def quick_sort_iterative(arr):
    """
    반복적 퀵 정렬
    스택을 이용하여 재귀 제거
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 정렬된 리스트
    """
    def partition(arr, low, high):
        pivot = arr[high]
        i = low - 1
        
        for j in range(low, high):
            if arr[j] <= pivot:
                i += 1
                arr[i], arr[j] = arr[j], arr[i]
        
        arr[i + 1], arr[high] = arr[high], arr[i + 1]
        return i + 1
    
    # 스택 초기화
    stack = [(0, len(arr) - 1)]
    
    while stack:
        low, high = stack.pop()
        
        if low < high:
            pivot_idx = partition(arr, low, high)
            
            # 양쪽 부분을 스택에 추가
            stack.append((low, pivot_idx - 1))
            stack.append((pivot_idx + 1, high))
    
    return arr


# 사용 예시
numbers = [5, 3, 8, 4, 2, 7, 1, 10]
print(f"정렬 전: {numbers}")
quick_sort_iterative(numbers)
print(f"정렬 후: {numbers}")
```

#### 하이브리드 버전 (퀵 + 삽입)
```python
def quick_sort_hybrid(arr):
    """
    하이브리드 퀵 정렬
    작은 배열은 삽입 정렬 사용
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 정렬된 리스트
    """
    CUTOFF = 10  # 임계값
    
    def insertion_sort_range(arr, low, high):
        """범위 지정 삽입 정렬"""
        for i in range(low + 1, high + 1):
            key = arr[i]
            j = i - 1
            
            while j >= low and arr[j] > key:
                arr[j + 1] = arr[j]
                j -= 1
            
            arr[j + 1] = key
    
    def partition(arr, low, high):
        pivot = arr[high]
        i = low - 1
        
        for j in range(low, high):
            if arr[j] <= pivot:
                i += 1
                arr[i], arr[j] = arr[j], arr[i]
        
        arr[i + 1], arr[high] = arr[high], arr[i + 1]
        return i + 1
    
    def quick_sort_helper(arr, low, high):
        # 작은 배열은 삽입 정렬
        if high - low + 1 <= CUTOFF:
            insertion_sort_range(arr, low, high)
        elif low < high:
            pivot_idx = partition(arr, low, high)
            quick_sort_helper(arr, low, pivot_idx - 1)
            quick_sort_helper(arr, pivot_idx + 1, high)
    
    quick_sort_helper(arr, 0, len(arr) - 1)
    return arr


# 사용 예시
numbers = [5, 3, 8, 4, 2, 7, 1, 10, 9, 6]
print(f"정렬 전: {numbers}")
quick_sort_hybrid(numbers)
print(f"정렬 후: {numbers}")
```

#### 랜덤 피벗 버전
```python
import random

def quick_sort_random(arr):
    """
    랜덤 피벗 퀵 정렬
    최악의 경우를 확률적으로 회피
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 정렬된 리스트
    """
    def partition_random(arr, low, high):
        # 랜덤하게 피벗 선택하여 마지막과 교환
        random_idx = random.randint(low, high)
        arr[random_idx], arr[high] = arr[high], arr[random_idx]
        
        pivot = arr[high]
        i = low - 1
        
        for j in range(low, high):
            if arr[j] <= pivot:
                i += 1
                arr[i], arr[j] = arr[j], arr[i]
        
        arr[i + 1], arr[high] = arr[high], arr[i + 1]
        return i + 1
    
    def quick_sort_helper(arr, low, high):
        if low < high:
            pivot_idx = partition_random(arr, low, high)
            quick_sort_helper(arr, low, pivot_idx - 1)
            quick_sort_helper(arr, pivot_idx + 1, high)
    
    quick_sort_helper(arr, 0, len(arr) - 1)
    return arr


# 사용 예시
numbers = [5, 3, 8, 4, 2, 7, 1, 10]
print(f"정렬 전: {numbers}")
quick_sort_random(numbers)
print(f"정렬 후: {numbers}")
```

### Java 구현

#### 기본 버전
```java
public class QuickSort {
    
    /**
     * 퀵 정렬 기본 구현
     * 
     * @param arr 정렬할 배열
     */
    public static void quickSort(int[] arr) {
        if (arr == null || arr.length == 0) {
            return;
        }
        quickSortHelper(arr, 0, arr.length - 1);
    }
    
    private static void quickSortHelper(int[] arr, int low, int high) {
        if (low < high) {
            // 분할
            int pivotIdx = partition(arr, low, high);
            
            // 좌우 재귀 정렬
            quickSortHelper(arr, low, pivotIdx - 1);
            quickSortHelper(arr, pivotIdx + 1, high);
        }
    }
    
    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];  // 마지막 원소를 피벗으로
        int i = low - 1;        // 작은 값들의 마지막 인덱스
        
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                // 교환
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        
        // 피벗을 올바른 위치로
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        
        return i + 1;
    }
    
    // 사용 예시
    public static void main(String[] args) {
        int[] numbers = {5, 3, 8, 4, 2, 7, 1, 10};
        
        System.out.print("정렬 전: ");
        printArray(numbers);
        
        quickSort(numbers);
        
        System.out.print("정렬 후: ");
        printArray(numbers);
    }
    
    private static void printArray(int[] arr) {
        for (int num : arr) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
}
```

#### Hoare Partition 버전
```java
public class QuickSortHoare {
    
    /**
     * 퀵 정렬 (Hoare Partition)
     * 
     * @param arr 정렬할 배열
     */
    public static void quickSort(int[] arr) {
        if (arr == null || arr.length == 0) {
            return;
        }
        quickSortHelper(arr, 0, arr.length - 1);
    }
    
    private static void quickSortHelper(int[] arr, int low, int high) {
        if (low < high) {
            int pivotIdx = hoarePartition(arr, low, high);
            quickSortHelper(arr, low, pivotIdx);
            quickSortHelper(arr, pivotIdx + 1, high);
        }
    }
    
    private static int hoarePartition(int[] arr, int low, int high) {
        int pivot = arr[low];
        int i = low - 1;
        int j = high + 1;
        
        while (true) {
            // 왼쪽에서 피벗보다 큰 값 찾기
            do {
                i++;
            } while (arr[i] < pivot);
            
            // 오른쪽에서 피벗보다 작은 값 찾기
            do {
                j--;
            } while (arr[j] > pivot);
            
            // 교차하면 종료
            if (i >= j) {
                return j;
            }
            
            // 교환
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }
    
    // 사용 예시
    public static void main(String[] args) {
        int[] numbers = {5, 3, 8, 4, 2, 7, 1, 10};
        
        System.out.print("정렬 전: ");
        printArray(numbers);
        
        quickSort(numbers);
        
        System.out.print("정렬 후: ");
        printArray(numbers);
    }
    
    private static void printArray(int[] arr) {
        for (int num : arr) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
}
```

#### 3-Way Partition 버전
```java
public class QuickSort3Way {
    
    /**
     * 3-way 퀵 정렬
     * 중복 값이 많을 때 효율적
     * 
     * @param arr 정렬할 배열
     */
    public static void quickSort(int[] arr) {
        if (arr == null || arr.length == 0) {
            return;
        }
        quickSortHelper(arr, 0, arr.length - 1);
    }
    
    private static void quickSortHelper(int[] arr, int low, int high) {
        if (low < high) {
            int[] bounds = threeWayPartition(arr, low, high);
            quickSortHelper(arr, low, bounds[0] - 1);
            quickSortHelper(arr, bounds[1] + 1, high);
        }
    }
    
    private static int[] threeWayPartition(int[] arr, int low, int high) {
        int pivot = arr[low];
        int lt = low;       // less than
        int i = low + 1;    // current
        int gt = high;      // greater than
        
        while (i <= gt) {
            if (arr[i] < pivot) {
                // 교환 arr[lt] <-> arr[i]
                int temp = arr[lt];
                arr[lt] = arr[i];
                arr[i] = temp;
                lt++;
                i++;
            } else if (arr[i] > pivot) {
                // 교환 arr[i] <-> arr[gt]
                int temp = arr[i];
                arr[i] = arr[gt];
                arr[gt] = temp;
                gt--;
            } else {  // arr[i] == pivot
                i++;
            }
        }
        
        return new int[]{lt, gt};
    }
    
    // 사용 예시
    public static void main(String[] args) {
        int[] numbers = {5, 3, 8, 3, 2, 3, 1, 3, 10, 3};
        
        System.out.print("정렬 전: ");
        printArray(numbers);
        
        quickSort(numbers);
        
        System.out.print("정렬 후: ");
        printArray(numbers);
    }
    
    private static void printArray(int[] arr) {
        for (int num : arr) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
}
```

#### 제네릭 버전
```java
import java.util.Comparator;

public class QuickSortGeneric {
    
    /**
     * 제네릭 퀵 정렬
     * 
     * @param arr 정렬할 배열
     * @param comparator 비교자
     */
    public static <T> void quickSort(T[] arr, Comparator<T> comparator) {
        if (arr == null || arr.length == 0) {
            return;
        }
        quickSortHelper(arr, 0, arr.length - 1, comparator);
    }
    
    private static <T> void quickSortHelper(T[] arr, int low, int high, 
                                            Comparator<T> comparator) {
        if (low < high) {
            int pivotIdx = partition(arr, low, high, comparator);
            quickSortHelper(arr, low, pivotIdx - 1, comparator);
            quickSortHelper(arr, pivotIdx + 1, high, comparator);
        }
    }
    
    private static <T> int partition(T[] arr, int low, int high, 
                                     Comparator<T> comparator) {
        T pivot = arr[high];
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (comparator.compare(arr[j], pivot) <= 0) {
                i++;
                // 교환
                T temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        
        // 피벗을 올바른 위치로
        T temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        
        return i + 1;
    }
    
    // 사용 예시
    public static void main(String[] args) {
        // Integer 배열 정렬
        Integer[] numbers = {5, 3, 8, 4, 2, 7, 1, 10};
        quickSort(numbers, Comparator.naturalOrder());
        System.out.print("오름차순: ");
        printArray(numbers);
        
        // 내림차순 정렬
        Integer[] numbers2 = {5, 3, 8, 4, 2, 7, 1, 10};
        quickSort(numbers2, Comparator.reverseOrder());
        System.out.print("내림차순: ");
        printArray(numbers2);
        
        // String 배열 길이로 정렬
        String[] words = {"apple", "pie", "banana", "cat"};
        quickSort(words, Comparator.comparingInt(String::length));
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

---

**참고 자료**:
- "Introduction to Algorithms" (CLRS) - Thomas H. Cormen
- "Algorithms" (4th Edition) - Robert Sedgewick
- Wikipedia: Quicksort
- GeeksforGeeks: Quick Sort