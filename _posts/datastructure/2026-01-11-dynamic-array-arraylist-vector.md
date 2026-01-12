---
title: "[Data Structure] 동적 배열(Dynamic Array)"

tagline: "ArrayList의 성장 전략, ensureCapacity 최적화, Vector와의 차이점, 그리고 실무 적용 사례까지 깊이 있게 분석합니다."

header:
  overlay_image: /assets/post/datastructure/2026-01-11-dynamic-array-arraylist-vector/overlay.png
  overlay_filter: 0.5

categories:
  - datastructure

tags:
  - 자료구조
  - ArrayList
  - Vector
  - Dynamic Array
  - 동적 배열
  - ensureCapacity
  - 성능 최적화
  - Java Collections

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-01-12T22:12:00
---

고정 배열(static array)은 생성 시 크기가 결정되어 선택의 폭이 좁습니다. 얼마나 많은 요소를 저장할지 미리 알 수 없을 때 동적 배열(dynamic array)이 필요합니다. Java에서는 **ArrayList**와 **Vector**가 동적 배열의 역할을 하지만, 내부 구조와 성능 특성이 다릅니다.

이 글에서는 두 클래스의 차이점, 내부 동작 원리, 실무에서의 최적화 전략까지 깊이 있게 다룹니다.

---

## 1. ArrayList와 Vector: 무엇이 같고 무엇이 다른가?

### 1.1 공통점: 모두 내부적으로 Object[] 배열 사용

ArrayList와 Vector는 모두 내부에 **Object[] elementData**라는 동적 배열을 유지합니다. 요소가 추가될 때마다 용량이 부족하면 새로운 배열을 할당하고 기존 데이터를 복사합니다.

<div class="mermaid mermaid-center">
graph TB
    A["ArrayList / Vector"]
    B["Object[] elementData<br/>(내부 배열)"]
    C["int size<br/>(현재 요소 개수)"]
    D["int capacity<br/>(내부 배열 크기)"]
    
    A --> B
    A --> C
    A --> D
    
    style A fill:#4a90e2,stroke:#357abd,stroke-width:2px,color:#fff
    style B fill:#50c878,stroke:#2d7a4a,stroke-width:2px,color:#fff
    style C fill:#f39c12,stroke:#a86904,stroke-width:2px,color:#fff
    style D fill:#e74c3c,stroke:#a93226,stroke-width:2px,color:#fff
</div>

### 1.2 주요 차이점: 동기화와 성장률

| 특성 | ArrayList | Vector |
|------|-----------|--------|
| **동기화** | 비동기화 (Unsynchronized) | 전체 메서드 동기화 (Synchronized) |
| **성장률** | 1.5배 (newCap = old + old>>1) | capacityIncrement 지정 가능 |
| **데이터 구조 | 순환 | 순환 |
| **기본 초기 용량** | 10 (첫 add 시) | 10 |
| **성능 (단일 스레드)** | 빠름 | 느림 (락 오버헤드) |
| **멀티스레드 안전** | 아님 | 함 (낮은 확장성) |
| **도입 시기** | JDK 1.2 (컬렉션 프레임워크) | JDK 1.0 (레거시) |


## 2. ArrayList의 내부 동작: 용량 확장 메커니즘

### 2.1 성장률 1.5배의 의미

ArrayList가 추가할 요소를 위한 공간이 없을 때, `grow()` 메서드가 호출되어 **현재 용량의 1.5배**로 확장합니다.

```
초기 상태: capacity = 10
1번째 확장: capacity = 10 + (10 >> 1) = 15
2번째 확장: capacity = 15 + (15 >> 1) = 22
3번째 확장: capacity = 22 + (22 >> 1) = 33
...
```

**왜 1.5배일까?**
- 2배(벡터의 기본값)보다는 메모리 낭비가 적음 (메모리 효율)
- 동시에 리사이징 횟수도 적절히 제어 (O(n) 복사 오버헤드 최소화)
- 이론적으로, 기하급수적 성장 시 총 복사 비용은 상수 배수 범위 내 유지: **총 복사량 ≤ 3n**

<div class="mermaid mermaid-center">
graph TD
    A["사용자가 add 호출<br/>(용량 초과)"] -->|ensureCapacityInternal| B["현재 크기 < 목표 크기?"]
    B -->|Yes| C["grow 호출"]
    B -->|No| D["그대로 진행"]
    C -->|newCap = oldCap + oldCap>>1| E["새 배열 할당"]
    E -->|System.arraycopy| F["기존 요소 복사<br/>O(n) 비용"]
    F -->|elementData 참조 변경| G["새 배열로 교체"]
    G --> H["요소 추가"]
    D --> H
</div>

### 2.2 복사 비용 분석

동적 배열이 n개의 요소를 추가할 때, 1.5배 성장으로 인한 **총 복사 횟수**를 추정할 수 있습니다.

**기하급수적 성장의 총 이동량 (상한값):**

$$\text{Total\_Copies} = \sum_{i=0}^{\log_{1.5}(n)} 1.5^i \cdot \text{copy\_cost} \approx 3n$$

즉, **최악의 경우에도 각 요소가 평균 3번 복사**됩니다. 이는 매우 효율적인 값입니다.

**구체적 예시:**
- **용량 예약 없이 1,000,000개 추가**: ~30회 리사이징 (각 O(n))
- **ensureCapacity로 미리 할당**: 1회 리사이징만 발생
- **생성자로 초기 용량 지정**: 리사이징 0회 (가장 빠름)

---

## 3. ensureCapacity와 trimToSize: 실무 최적화 도구

### 3.1 ensureCapacity: 대량 삽입 전 용량 미리 확보

대량의 요소를 한 번에 추가할 예정이면, **ensureCapacity**로 미리 용량을 확보합니다.

```java
ArrayList<Integer> list = new ArrayList<>();

// 미리 용량 확보 (한 번의 System.arraycopy만 발생)
list.ensureCapacity(1_000_000);

// 이후 추가 시 리사이징 없음
for (int i = 0; i < 1_000_000; i++) {
    list.add(i);
}

// 성능: 약 2배 이상 빠름 (리사이징 오버헤드 제거)
```

**원리:**
- `ensureCapacity(minCapacity)`를 호출하면, 현재 용량이 `minCapacity`보다 작으면 `grow(minCapacity)`가 호출됨
- 충분한 크기의 배열이 한 번에 할당되므로 이후 리사이징이 발생하지 않음
- 대량 삽입 시 **총 O(n) 복사가 1회**로 줄어듦

### 3.2 trimToSize: 메모리 정리

리스트에 데이터를 대량으로 적재한 후, 거의 읽기만 할 예정이면 **trimToSize**로 초과 용량을 제거합니다.

```java
// 대량 삽입 완료
ArrayList<Integer> list = new ArrayList<>();
list.ensureCapacity(1_000_000);
// ... 대량 삽입 ...
list.size(); // 500_000 (실제 요소)

// 초과 용량 제거 (메모리 절약)
list.trimToSize();
// 내부 배열 크기: 1_000_000 → 500_000

// 읽기 전용으로 변환 (수정 불가)
List<Integer> readOnly = Collections.unmodifiableList(list);
```

**성능 트레이드오프:**
- 장점: 힙 메모리 50% 절약, GC 루트 스캔 비용 감소
- 단점: 이후 추가 삽입 필요 시 다시 리사이징 발생

---

## 4. 실무 시나리오별 최적화 전략

### 4.1 시나리오 1: 최종 크기를 아는 경우

```java
// Case 1: 생성자에서 정확한 크기 지정 (가장 빠름)
List<Integer> list = new ArrayList<>(1_000_000);
for (int i = 0; i < 1_000_000; i++) {
    list.add(i);
}

// Case 2: ensureCapacity 사용
ArrayList<Integer> list = new ArrayList<>();
list.ensureCapacity(1_000_000);
// ... 삽입 로직 ...
```

**비용:**
- 생성자: 리사이징 0회, 복사 1회 (초기화)
- ensureCapacity: 리사이징 1회, 복사 1회

---

### 4.2 시나리오 2: 최종 크기를 모르는 경우 (안전 버퍼 전략)

예상 평균 크기 $\hat{n}$이 있을 때, **10~20% 과할당**으로 리사이징 횟수를 줄이면서 메모리 낭비를 최소화합니다.

```java
// 예상 크기: 1,000,000개, 실제 최대: 1,200,000개
int estimatedSize = 1_000_000;
int safeBuffer = (int)(estimatedSize * 1.2); // 20% 과할당

ArrayList<Integer> list = new ArrayList<>();
list.ensureCapacity(safeBuffer); // 1,200,000으로 미리 확보

// 데이터 적재
for (int i = 0; i < actualSize; i++) {
    list.add(data[i]);
}

// 메모리 정리
list.trimToSize();
```

**효과:**
- 메모리 낭비: 20% (예측 가능, 관리 가능)
- 리사이징 횟수: 1회 이하 (대부분 0회)
- 총 복사 비용: **약 1.2n** (예약 없을 때의 3n 대비 60% 절감)

---

### 4.3 시나리오 3: 배치 단위 적재

데이터가 배치(batch)로 들어올 때, 각 배치 도착 시점에 용량을 동적으로 조정합니다.

```java
ArrayList<Integer> list = new ArrayList<>();
int currentBatchSize = 0;

// 배치별 적재
while (moreDataAvailable()) {
    int batchSize = readBatchSize();
    
    // 현재 용량이 70% 이상 찼으면 미리 확장
    if (list.size() + batchSize > list.capacity() * 0.7) {
        int newCapacity = list.capacity() + batchSize + 10000; // 배치 크기 + 마진
        list.ensureCapacity(newCapacity);
    }
    
    // 배치 추가
    for (int i = 0; i < batchSize; i++) {
        list.add(readData());
    }
}

// 완료 후 메모리 정리
list.trimToSize();
```

---

### 4.4 시나리오 4: 병렬 적재 (멀티스레드)

여러 스레드에서 동시에 데이터를 수집한 뒤, 단일 스레드에서 병합합니다.

```java
// 스레드별 리스트 생성
List<ArrayList<Integer>> threadLists = new ArrayList<>();
for (int i = 0; i < numThreads; i++) {
    threadLists.add(new ArrayList<>());
}

// 각 스레드에서 독립적으로 데이터 적재 (동기화 불필요)
// executor.submit(() -> {
//     ArrayList<Integer> threadList = threadLists.get(threadId);
//     for (...)  threadList.add(...);
// });

// 모든 스레드 완료 대기
executor.shutdown();
executor.awaitTermination(...);

// 단일 스레드에서 최종 리스트 생성
ArrayList<Integer> finalList = new ArrayList<>();
int totalSize = 0;
for (ArrayList<Integer> tl : threadLists) {
    totalSize += tl.size();
}

// 한 번에 충분한 용량 확보 후 병합
finalList.ensureCapacity(totalSize);
for (ArrayList<Integer> tl : threadLists) {
    finalList.addAll(tl);
}

// 메모리 정리
finalList.trimToSize();
```

**장점:**
- 리사이징 충돌 회피 (각 스레드는 독립적)
- 최종 병합 시 한 번의 리사이징만 발생
- 불필요한 동기화 없음 (확장성 우수)

---

## 5. ArrayList vs Vector: 언제 어떤 것을 쓸까?

### 5.1 ArrayList를 쓰는 경우 (대부분의 상황)

**기본 선택지입니다.**

```java
// 1. 단일 스레드 환경
List<String> names = new ArrayList<>();

// 2. 멀티스레드지만 외부 동기화로 제어
List<String> names = new ArrayList<>();
Lock lock = new ReentrantLock();
// lock으로 감싼 후 사용

// 3. 읽기 전용 버전 필요
List<String> immutable = Collections.unmodifiableList(new ArrayList<>(data));

// 4. 복사 기반 동시성 필요
List<String> copyOnWrite = new CopyOnWriteArrayList<>(data);
```

**이유:**
- 락 오버헤드 없음 (빠름)
- 외부 동기화로 세밀한 제어 가능 (확장성)
- 메모리 효율적

### 5.2 Vector를 쓰는 경우 (거의 없음)

**레거시 호환이 필요한 경우에만.**

```java
// 아주 오래된 API와의 호환성 필요
@Deprecated("ArrayList를 쓰세요")
void processData(Vector<String> data) {
    // 기존 코드...
}
```

**왜 추천하지 않나?**
- 모든 메서드가 synchronized → 락 경합 비용 높음
- capacityIncrement 정책도 대부분 필요 없음
- ArrayList + 외부 동기화가 훨씬 효율적

---

## 6. 성장률 1.5배의 복사 비용 추정 및 의사결정

### 6.1 예측 불가능한 크기에서의 전략

**3단계 의사결정:**

```
Step 1: 최댓값을 추정할 수 있는가?
  ├─ Yes → ensureCapacity(maxValue) + add + trimToSize
  └─ No  → Step 2

Step 2: 예상 평균값 * 1.2를 과할당할 여유가 있는가?
  ├─ Yes (메모리 넉넉함) → ensureCapacity(estimatedSize * 1.2)
  ├─ Maybe (빡빡함)      → ensureCapacity(estimatedSize * 1.1)
  └─ No (매우 제약)      → ensureCapacity만 최소 1회

Step 3: 데이터가 배치로 들어오는가?
  ├─ Yes → 배치마다 용량 체크 및 미리 확장
  └─ No  → 일괄 처리
```

### 6.2 메모리 vs 속도 트레이드오프 표

| 전략 | 메모리 낭비 | 리사이징 횟수 | 복사 비용 | 추천 상황 |
|------|-----------|------------|---------|----------|
| **과할당 없음** | 0% | ~log(n) | 3n | 메모리 극도로 제약 |
| **10% 과할당** | ~10% | 2~5회 | ~1.2n | 균형잡힌 상황 |
| **20% 과할당** | ~20% | 1~2회 | ~1.2n | 메모리 충분 |
| **정확한 예측** | 0% | 1회 | n | 최적 (가능하면 추천) |

---

## 7. 제거(remove) 연산의 O(n) 비용

ArrayList의 중간 요소 제거 시 뒷부분 전체를 앞으로 이동해야 합니다.

```java
ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

// 인덱스 2 제거 (요소 3 제거)
list.remove(2);
// 내부: System.arraycopy(elementData, 3, elementData, 2, 2)
// 비용: O(n) = O(5-2) = O(3)
```

**성능 특성:**

| 연산 | 복잡도 | 이유 |
|------|--------|------|
| **add(size, element)** | O(1) | 맨 뒤에만 추가 |
| **add(0, element)** | O(n) | 전체를 뒤로 밀어야 함 |
| **remove(size-1)** | O(1) | 맨 뒤만 제거 |
| **remove(0)** | O(n) | 전체를 앞으로 당겨야 함 |

**LinkedList를 선택할 기준:**
- 중간/앞쪽 위치에서의 빈번한 삽입/삭제 필요 → LinkedList 고려
- 무작위 접근과 삭제가 섞여 있음 → ArrayList + indexed removal 피하기

---

## 8. 결론 및 실무 체크리스트

### 체크리스트

- ✅ **최종 크기를 아는가?** → 생성자 또는 ensureCapacity(size) 사용
- ✅ **대량 삽입 후 읽기 전용인가?** → trimToSize + Collections.unmodifiableList
- ✅ **멀티스레드인가?** → ArrayList + 외부 Lock, 또는 CopyOnWriteArrayList
- ✅ **중간 삽입/삭제가 빈번한가?** → LinkedList 고려
- ✅ **메모리가 극도로 제약인가?** → 과할당 최소화, 정기적 trimToSize
- ✅ **Vector 사용 중인가?** → ArrayList로 마이그레이션 검토

### 성능 개선 우선순위

1. **높음**: `ensureCapacity` 도입 (리사이징 횟수 ~90% 감소)
2. **중간**: 정확한 크기 예측 및 생성자 활용 (리사이징 0회)
3. **낮음**: 세밀한 과할당 튜닝 (5~10% 개선)

---

**다음 시간에 읽을 자료:**
- 동적 배열의 역순 반복과 캐시 친화성
- CopyOnWriteArrayList와 멀티스레드 동시성 전략
- 박싱 오버헤드와 원시 배열 래퍼의 대안
