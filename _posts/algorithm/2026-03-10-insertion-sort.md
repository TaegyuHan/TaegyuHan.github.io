---
title: "[Algorithm] 삽입 정렬 (Insertion Sort) 완벽 정리"

tagline: "부분적으로 정렬된 데이터에 강한 효율적인 정렬 알고리즘"

header:
  overlay_image: /assets/post/algorithm/2026-03-10-insertion-sort/overlay.png
  overlay_filter: 0.5

categories:
  - Algorithm

tags:
  - 알고리즘
  - Algorithm
  - 삽입정렬
  - InsertionSort
  - 정렬
  - Sorting
  - Python
  - Java

toc: true
show_date: true

last_modified_at: 2026-03-10T21:33:00+09:00
---

삽입 정렬(Insertion Sort)은 정렬된 부분에 새로운 원소를 적절한 위치에 **삽입**하는 방식의 정렬 알고리즘입니다. 카드 게임에서 손에 든 카드를 정렬하는 방식과 유사하여 직관적으로 이해하기 쉽고, 부분적으로 정렬된 데이터에 대해 매우 효율적입니다.

## 삽입 정렬이란?

삽입 정렬은 **배열을 정렬된 부분과 정렬되지 않은 부분으로 나누고, 정렬되지 않은 부분의 원소를 정렬된 부분의 적절한 위치에 삽입**하는 알고리즘입니다. 마치 카드를 한 장씩 받아서 순서대로 정리하는 것과 같습니다.

### 동작 원리

1. 배열의 두 번째 원소부터 시작 (첫 번째 원소는 이미 정렬된 것으로 간주)
2. 현재 원소를 선택하여 정렬된 부분의 적절한 위치를 찾음
3. 적절한 위치를 찾기 위해 정렬된 부분을 역순으로 탐색
4. 현재 원소보다 큰 값들은 오른쪽으로 이동
5. 적절한 위치에 현재 원소를 삽입
6. 모든 원소에 대해 2~5 과정 반복

## 인터랙티브 애니메이션

위에서 설명한 삽입 정렬 과정을 직접 눈으로 확인해보세요! 시작 버튼을 눌러 정렬 과정을 단계별로 관찰할 수 있습니다.

{% include algorithm/insertion-sort-animation.html %}

- **회색**: 정렬되지 않은 기본 상태
- **주황색**: 현재 삽입할 요소 (key)
- **노란색**: 정렬된 부분에서 비교 중인 요소
- **빨간색**: 오른쪽으로 이동하는 요소
- **초록색**: 정렬이 완료된 요소
- 속도를 조절하여 천천히 또는 빠르게 확인할 수 있습니다.
- 요소가 정렬된 부분에 삽입되는 과정을 명확히 볼 수 있습니다.

## 복잡도 분석

### 시간 복잡도

| 경우 | 복잡도 | 설명 |
|------|--------|------|
| **최선 (Best)** | O(n) | 이미 정렬된 경우 |
| **평균 (Average)** | O(n²) | 일반적인 경우 |
| **최악 (Worst)** | O(n²) | 역순으로 정렬된 경우 |

#### 계산 과정
- 비교 횟수: 최악의 경우 1 + 2 + ... + (n-1) = n(n-1)/2
- 교환 횟수: 비교 횟수와 동일
- 최선의 경우: 이미 정렬되어 있으면 n-1번의 비교만 수행 → **O(n)**
- 따라서 평균 시간 복잡도는 **O(n²)**

**중요한 특징**: 삽입 정렬은 데이터가 **거의 정렬된 상태**에서는 매우 빠르게 동작합니다!

### 공간 복잡도

- **O(1)**: 제자리 정렬(in-place sorting)
- 추가 메모리를 거의 사용하지 않음 (임시 변수만 필요)

## 장단점

### 장점 ✅

1. **구현이 간단하다**: 코드가 직관적이고 이해하기 쉬움
2. **안정 정렬(Stable Sort)**: 같은 값의 순서가 유지됨
3. **추가 메모리가 거의 필요 없다**: 공간 복잡도 O(1)
4. **거의 정렬된 데이터에 효율적**: 최선의 경우 O(n)
5. **작은 데이터셋에 효율적**: 오버헤드가 적음
6. **온라인 알고리즘**: 데이터를 받는 동시에 정렬 가능

### 단점 ❌

1. **평균적으로 느림**: 시간 복잡도 O(n²)로 대용량 데이터에 부적합
2. **역순 정렬 시 비효율적**: 최악의 경우 O(n²)
3. **선택/버블 정렬보다 교환이 많음**: 데이터 이동이 빈번

## 다른 O(n²) 정렬과의 비교

삽입 정렬은 버블 정렬, 선택 정렬과 함께 O(n²) 정렬 알고리즘이지만 중요한 차이가 있습니다:

| 특성 | 삽입 정렬 | 선택 정렬 | 버블 정렬 |
|------|-----------|-----------|-----------|
| **정렬 방식** | 적절한 위치에 삽입 | 최소값 선택 후 교환 | 인접 원소 교환 |
| **비교 횟수** | 평균 n²/4 | 항상 n²/2 | 평균 n²/2 |
| **교환 횟수** | 평균 n²/4 | 최대 n-1 | 평균 n²/2 |
| **안정성** | 안정 정렬 | 불안정 정렬 | 안정 정렬 |
| **최선의 경우** | O(n) | O(n²) | O(n) (최적화 시) |
| **적응성** | 높음 (거의 정렬된 데이터에 강함) | 없음 | 중간 |

**언제 삽입 정렬이 최고일까?**
- 데이터가 거의 정렬되어 있을 때
- 작은 데이터셋 (10~50개)
- 온라인으로 데이터를 받으면서 정렬해야 할 때
- 안정 정렬이 필요할 때

## 실무에서의 활용

삽입 정렬은 다음과 같은 경우에 실제로 사용됩니다:

### 1. 하이브리드 정렬 알고리즘

많은 고급 정렬 알고리즘들이 **작은 부분 배열**에 대해 삽입 정렬을 사용합니다:

```python
def hybrid_quick_sort(arr, low, high, threshold=10):
    """
    퀵 정렬과 삽입 정렬의 하이브리드
    작은 부분 배열에 삽입 정렬 사용
    """
    if high - low + 1 <= threshold:
        # 작은 배열은 삽입 정렬 사용
        insertion_sort_range(arr, low, high)
    elif low < high:
        # 큰 배열은 퀵 정렬 사용
        pivot_idx = partition(arr, low, high)
        hybrid_quick_sort(arr, low, pivot_idx - 1, threshold)
        hybrid_quick_sort(arr, pivot_idx + 1, high, threshold)
```

**실제 사용 예시:**
- Java의 `Arrays.sort()`: DualPivotQuicksort에서 작은 배열에 삽입 정렬 사용
- Python의 Timsort: 머지 정렬 기반이지만 작은 런(run)에 삽입 정렬 사용

### 2. 온라인 정렬

데이터가 연속적으로 들어오는 상황에서 유용합니다:

```python
def online_insertion_sort(sorted_list, new_value):
    """
    이미 정렬된 리스트에 새로운 값을 삽입
    """
    # 적절한 위치 찾기
    i = len(sorted_list) - 1
    sorted_list.append(new_value)
    
    while i >= 0 and sorted_list[i] > new_value:
        sorted_list[i + 1] = sorted_list[i]
        i -= 1
    
    sorted_list[i + 1] = new_value
    return sorted_list

# 사용 예시: 실시간 랭킹 시스템
rankings = [100, 85, 70, 60]
new_score = 75
online_insertion_sort(rankings, new_score)
print(rankings)  # [100, 85, 75, 70, 60]
```

### 3. 부분 정렬된 데이터

로그 파일, 센서 데이터 등 대부분 정렬되어 있지만 일부만 섞인 데이터에 효율적입니다.

## 최적화 기법

### 1. 이진 삽입 정렬 (Binary Insertion Sort)

삽입 위치를 찾을 때 이진 탐색을 사용하여 비교 횟수를 줄입니다:

```python
def binary_insertion_sort(arr):
    """
    이진 탐색을 이용한 삽입 정렬
    비교 횟수는 줄지만 교환 횟수는 동일
    """
    for i in range(1, len(arr)):
        key = arr[i]
        
        # 이진 탐색으로 삽입 위치 찾기
        left, right = 0, i - 1
        while left <= right:
            mid = (left + right) // 2
            if arr[mid] > key:
                right = mid - 1
            else:
                left = mid + 1
        
        # 삽입 위치는 left
        # left부터 i-1까지 오른쪽으로 이동
        for j in range(i, left, -1):
            arr[j] = arr[j - 1]
        
        arr[left] = key
    
    return arr
```

**장점**: 비교 횟수 O(n log n)으로 감소  
**단점**: 여전히 이동은 O(n²)이므로 전체 성능 개선은 제한적

### 2. 셸 정렬 (Shell Sort)

삽입 정렬의 개선된 버전으로, 먼 거리의 원소들을 먼저 정렬합니다:

```python
def shell_sort(arr):
    """
    셸 정렬: 삽입 정렬의 일반화
    간격을 점점 줄여가며 삽입 정렬 수행
    """
    n = len(arr)
    gap = n // 2
    
    while gap > 0:
        # gap 간격으로 삽입 정렬
        for i in range(gap, n):
            temp = arr[i]
            j = i
            
            while j >= gap and arr[j - gap] > temp:
                arr[j] = arr[j - gap]
                j -= gap
            
            arr[j] = temp
        
        gap //= 2
    
    return arr
```

**성능**: 평균 O(n log n) ~ O(n^1.5), 최악 O(n²)

## 언제 사용해야 할까?

### 적합한 경우 ✅
- 데이터가 작을 때 (10~50개)
- 데이터가 **거의 정렬되어 있을 때** (매우 효율적!)
- 안정 정렬이 필요할 때
- 온라인 정렬이 필요할 때
- 간단한 구현이 필요할 때
- 하이브리드 알고리즘의 일부로 사용

### 부적합한 경우 ❌
- 대용량 데이터 (1000개 이상)
- 완전히 무작위 또는 역순 데이터
- 성능이 중요한 대규모 시스템

## 다른 정렬 알고리즘과 비교

| 알고리즘 | 최선 | 평균 | 최악 | 공간 | 안정성 | 특징 |
|----------|------|------|------|------|--------|------|
| 삽입 정렬 | O(n) | O(n²) | O(n²) | O(1) | ✅ | 거의 정렬된 데이터에 강함 |
| 선택 정렬 | O(n²) | O(n²) | O(n²) | O(1) | ❌ | 교환 횟수 적음 |
| 버블 정렬 | O(n) | O(n²) | O(n²) | O(1) | ✅ | 가장 느림 |
| 셸 정렬 | O(n log n) | O(n^1.5) | O(n²) | O(1) | ❌ | 삽입 정렬의 개선 |
| 퀵 정렬 | O(n log n) | O(n log n) | O(n²) | O(log n) | ❌ | 평균 가장 빠름 |
| 병합 정렬 | O(n log n) | O(n log n) | O(n log n) | O(n) | ✅ | 안정적 성능 |
| 힙 정렬 | O(n log n) | O(n log n) | O(n log n) | O(1) | ❌ | 공간 효율적 |

## 실전 문제 연습

삽입 정렬은 코딩 테스트에서 직접 구현하거나 원리를 응용하는 문제로 출제됩니다.

### 예제 문제

1. **링크드 리스트 삽입 정렬**: 연결 리스트를 삽입 정렬로 정렬
2. **역순 쌍 개수**: 삽입 정렬 과정에서 역순 쌍(inversion) 개수 계산
3. **K번째 삽입 후 상태**: K번째 원소 삽입 후 배열 상태 출력

```python
def insertion_sort_with_steps(arr):
    """정렬 과정을 출력하는 삽입 정렬"""
    n = len(arr)
    print(f"초기 상태: {arr}")
    
    for i in range(1, n):
        key = arr[i]
        j = i - 1
        
        print(f"\n{i}회차: key = {key}")
        
        # 적절한 위치 찾기
        moves = 0
        while j >= 0 and arr[j] > key:
            arr[j + 1] = arr[j]
            j -= 1
            moves += 1
        
        arr[j + 1] = key
        
        if moves > 0:
            print(f"  → {moves}번 이동하여 인덱스 {j+1}에 삽입")
        else:
            print(f"  → 이동 없음 (이미 정렬된 위치)")
        
        print(f"  결과: {arr}")
    
    return arr


def count_inversions_insertion_sort(arr):
    """
    삽입 정렬을 이용한 역순 쌍 개수 계산
    역순 쌍: arr[i] > arr[j]인데 i < j인 경우
    """
    inversions = 0
    n = len(arr)
    
    for i in range(1, n):
        key = arr[i]
        j = i - 1
        
        while j >= 0 and arr[j] > key:
            arr[j + 1] = arr[j]
            j -= 1
            inversions += 1  # 이동할 때마다 역순 쌍 발견
        
        arr[j + 1] = key
    
    return inversions, arr


# 실행 예시
print("=== 정렬 과정 ===")
numbers = [5, 3, 8, 4, 2]
insertion_sort_with_steps(numbers.copy())

print("\n=== 역순 쌍 개수 ===")
numbers2 = [5, 3, 8, 4, 2]
count, sorted_arr = count_inversions_insertion_sort(numbers2.copy())
print(f"역순 쌍 개수: {count}")
print(f"정렬된 배열: {sorted_arr}")
```

### 링크드 리스트 삽입 정렬

```python
class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val
        self.next = next


def insertion_sort_linked_list(head):
    """
    연결 리스트를 삽입 정렬로 정렬
    """
    if not head or not head.next:
        return head
    
    # 더미 노드 생성
    dummy = ListNode(0)
    current = head
    
    while current:
        # 다음 노드 저장
        next_node = current.next
        
        # 삽입 위치 찾기
        prev = dummy
        while prev.next and prev.next.val < current.val:
            prev = prev.next
        
        # 삽입
        current.next = prev.next
        prev.next = current
        
        current = next_node
    
    return dummy.next
```

## 마무리

삽입 정렬은 단순하지만 실용적인 정렬 알고리즘입니다. 특히 거의 정렬된 데이터나 작은 데이터셋에 대해 매우 효율적이며, 실제로 많은 고급 정렬 알고리즘에서 보조 알고리즘으로 사용됩니다.

### 핵심 요약

1. **정렬된 부분에 원소를 삽입**하는 직관적인 알고리즘
2. 시간 복잡도는 **최선 O(n), 평균과 최악 O(n²)**
3. **안정 정렬**이며 **제자리 정렬**
4. **거의 정렬된 데이터에 매우 효율적** (adaptive)
5. 실무에서 **하이브리드 알고리즘의 일부**로 활용
6. **온라인 정렬** 가능 (데이터를 받으면서 정렬)

삽입 정렬을 이해했다면, 다음 단계로:
- **셸 정렬(Shell Sort)**: 삽입 정렬의 개선 버전
- **병합 정렬(Merge Sort)**: 안정적인 O(n log n) 알고리즘
- **퀵 정렬(Quick Sort)**: 평균적으로 가장 빠른 O(n log n) 알고리즘
- **Timsort**: Python의 기본 정렬 알고리즘 (삽입 정렬 + 병합 정렬)

을 학습하는 것을 추천합니다.

## 코드 구현

### Python 구현

#### 기본 버전
```python
def insertion_sort(arr):
    """
    삽입 정렬 기본 구현
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 정렬된 리스트
    """
    n = len(arr)
    
    # 두 번째 원소부터 시작
    for i in range(1, n):
        key = arr[i]  # 현재 삽입할 원소
        j = i - 1     # 정렬된 부분의 마지막 인덱스
        
        # key보다 큰 원소들을 오른쪽으로 이동
        while j >= 0 and arr[j] > key:
            arr[j + 1] = arr[j]
            j -= 1
        
        # 적절한 위치에 key 삽입
        arr[j + 1] = key
    
    return arr


# 사용 예시
numbers = [5, 3, 8, 4, 2]
print(f"정렬 전: {numbers}")
insertion_sort(numbers)
print(f"정렬 후: {numbers}")
```

#### 내림차순 버전
```python
def insertion_sort_descending(arr):
    """
    삽입 정렬 내림차순 구현
    
    Args:
        arr (list): 정렬할 리스트
    
    Returns:
        list: 내림차순으로 정렬된 리스트
    """
    n = len(arr)
    
    for i in range(1, n):
        key = arr[i]
        j = i - 1
        
        # key보다 작은 원소들을 오른쪽으로 이동
        while j >= 0 and arr[j] < key:
            arr[j + 1] = arr[j]
            j -= 1
        
        arr[j + 1] = key
    
    return arr


# 사용 예시
numbers = [5, 3, 8, 4, 2]
print(f"정렬 전: {numbers}")
insertion_sort_descending(numbers)
print(f"정렬 후 (내림차순): {numbers}")
```

#### 범위 지정 삽입 정렬
```python
def insertion_sort_range(arr, start, end):
    """
    배열의 특정 범위만 삽입 정렬
    하이브리드 정렬 알고리즘에서 사용
    
    Args:
        arr (list): 정렬할 리스트
        start (int): 시작 인덱스
        end (int): 끝 인덱스 (포함)
    
    Returns:
        list: 정렬된 리스트
    """
    for i in range(start + 1, end + 1):
        key = arr[i]
        j = i - 1
        
        while j >= start and arr[j] > key:
            arr[j + 1] = arr[j]
            j -= 1
        
        arr[j + 1] = key
    
    return arr


# 사용 예시
numbers = [9, 5, 3, 8, 4, 2, 1]
print(f"정렬 전: {numbers}")
insertion_sort_range(numbers, 1, 5)  # 인덱스 1~5만 정렬
print(f"부분 정렬 후: {numbers}")
```

#### 제네릭 버전 (비교 함수 사용)
```python
def insertion_sort_generic(arr, key=None, reverse=False):
    """
    제네릭 삽입 정렬 구현
    
    Args:
        arr (list): 정렬할 리스트
        key (function): 비교 기준 함수
        reverse (bool): True면 내림차순, False면 오름차순
    
    Returns:
        list: 정렬된 리스트
    """
    n = len(arr)
    
    for i in range(1, n):
        current = arr[i]
        current_key = key(current) if key else current
        j = i - 1
        
        while j >= 0:
            compare_key = key(arr[j]) if key else arr[j]
            
            # reverse에 따라 비교 방향 결정
            if reverse:
                if compare_key < current_key:
                    break
            else:
                if compare_key <= current_key:
                    break
            
            arr[j + 1] = arr[j]
            j -= 1
        
        arr[j + 1] = current
    
    return arr


# 사용 예시
# 1. 문자열 길이로 정렬
words = ["apple", "pie", "banana", "cat"]
insertion_sort_generic(words, key=len)
print(f"길이 순 정렬: {words}")

# 2. 튜플의 두 번째 요소로 정렬
data = [(1, 5), (3, 2), (2, 8), (4, 1)]
insertion_sort_generic(data, key=lambda x: x[1])
print(f"두 번째 요소 순 정렬: {data}")

# 3. 절댓값으로 정렬
numbers = [-5, 3, -8, 4, -2]
insertion_sort_generic(numbers, key=abs)
print(f"절댓값 순 정렬: {numbers}")

# 4. 내림차순 정렬
numbers2 = [5, 3, 8, 4, 2]
insertion_sort_generic(numbers2, reverse=True)
print(f"내림차순 정렬: {numbers2}")
```

### Java 구현

#### 기본 버전
```java
public class InsertionSort {
    
    /**
     * 삽입 정렬 기본 구현
     * 
     * @param arr 정렬할 배열
     */
    public static void insertionSort(int[] arr) {
        int n = arr.length;
        
        // 두 번째 원소부터 시작
        for (int i = 1; i < n; i++) {
            int key = arr[i];  // 현재 삽입할 원소
            int j = i - 1;     // 정렬된 부분의 마지막 인덱스
            
            // key보다 큰 원소들을 오른쪽으로 이동
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            
            // 적절한 위치에 key 삽입
            arr[j + 1] = key;
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
        
        insertionSort(numbers);
        
        System.out.print("정렬 후: ");
        printArray(numbers);
    }
}
```

#### 제네릭 버전
```java
import java.util.Comparator;

public class InsertionSortGeneric {
    
    /**
     * 제네릭 삽입 정렬 구현
     * 
     * @param arr 정렬할 배열
     * @param comparator 비교자 (null이면 자연 순서)
     */
    public static <T extends Comparable<T>> void insertionSort(
            T[] arr, Comparator<T> comparator) {
        int n = arr.length;
        
        for (int i = 1; i < n; i++) {
            T key = arr[i];
            int j = i - 1;
            
            // key보다 큰 원소들을 오른쪽으로 이동
            while (j >= 0) {
                int cmp = (comparator != null) 
                    ? comparator.compare(arr[j], key)
                    : arr[j].compareTo(key);
                
                if (cmp <= 0) break;
                
                arr[j + 1] = arr[j];
                j--;
            }
            
            arr[j + 1] = key;
        }
    }
    
    /**
     * 자연 순서로 정렬 (오름차순)
     */
    public static <T extends Comparable<T>> void insertionSort(T[] arr) {
        insertionSort(arr, null);
    }
    
    /**
     * 배열의 특정 범위만 정렬
     */
    public static <T extends Comparable<T>> void insertionSortRange(
            T[] arr, int start, int end, Comparator<T> comparator) {
        for (int i = start + 1; i <= end; i++) {
            T key = arr[i];
            int j = i - 1;
            
            while (j >= start) {
                int cmp = (comparator != null) 
                    ? comparator.compare(arr[j], key)
                    : arr[j].compareTo(key);
                
                if (cmp <= 0) break;
                
                arr[j + 1] = arr[j];
                j--;
            }
            
            arr[j + 1] = key;
        }
    }
    
    public static void main(String[] args) {
        // Integer 배열 정렬
        Integer[] numbers = {5, 3, 8, 4, 2};
        System.out.print("정렬 전: ");
        printArray(numbers);
        
        insertionSort(numbers);
        
        System.out.print("정렬 후: ");
        printArray(numbers);
        
        // 내림차순 정렬
        Integer[] numbers2 = {5, 3, 8, 4, 2};
        insertionSort(numbers2, Comparator.reverseOrder());
        
        System.out.print("내림차순: ");
        printArray(numbers2);
        
        // 문자열 길이로 정렬
        String[] words = {"apple", "pie", "banana", "cat"};
        insertionSort(words, Comparator.comparingInt(String::length));
        
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
public class InsertionSortVisualized {
    
    /**
     * 삽입 정렬 과정을 시각화하여 출력
     * 
     * @param arr 정렬할 배열
     */
    public static void insertionSortWithVisualization(int[] arr) {
        int n = arr.length;
        
        System.out.println("초기 상태:");
        printArrayWithHighlight(arr, -1, -1);
        System.out.println();
        
        for (int i = 1; i < n; i++) {
            System.out.println("--- " + i + "회차 ---");
            int key = arr[i];
            int j = i - 1;
            
            System.out.println("삽입할 key: " + key);
            
            int moves = 0;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
                moves++;
            }
            
            arr[j + 1] = key;
            
            if (moves > 0) {
                System.out.println(moves + "번 이동하여 인덱스 " + (j + 1) + "에 삽입");
            } else {
                System.out.println("이동 없음 (이미 정렬된 위치)");
            }
            
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
                System.out.print("(" + arr[i] + ")");  // 현재 삽입 중
            } else {
                System.out.print(" " + arr[i] + " ");   // 정렬 안 된 부분
            }
            System.out.print(" ");
        }
        System.out.println();
    }
    
    public static void main(String[] args) {
        int[] numbers = {5, 3, 8, 4, 2};
        insertionSortWithVisualization(numbers);
    }
}
```

## 참고 자료

- [위키백과 - 삽입 정렬](https://ko.wikipedia.org/wiki/%EC%82%BD%EC%9E%85_%EC%A0%95%EB%A0%AC)
- [VisuAlgo - 정렬 시각화](https://visualgo.net/en/sorting)
- Introduction to Algorithms (CLRS)
- [GeeksforGeeks - Insertion Sort](https://www.geeksforgeeks.org/insertion-sort/)
- [Timsort - Python의 기본 정렬](https://en.wikipedia.org/wiki/Timsort)
