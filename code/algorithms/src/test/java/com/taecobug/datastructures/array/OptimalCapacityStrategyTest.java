package com.taecobug.datastructures.array;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * 최적화 전략 테스트
 * - ensureCapacity와 trimToSize 활용
 * - 대량 삽입 최적화
 * - 시나리오별 메모리 효율성
 */
@DisplayName("최적화 전략 테스트")
public class OptimalCapacityStrategyTest {

    @Test
    @DisplayName("Scenario 1: 정확한 최종 크기를 아는 경우")
    public void testExactSizeScenario() {
        int finalSize = 100_000;
        List<Integer> list = new ArrayList<>(finalSize);

        for (int i = 0; i < finalSize; i++) {
            list.add(i);
        }

        assertEquals(finalSize, list.size());

        // 정확한 크기 할당 시 리사이징 최소화
        assertTrue(((ArrayList<?>) list).size() <= finalSize);
    }

    @Test
    @DisplayName("Scenario 2: 불확실한 크기에 대한 안전 버퍼 (10% 과할당)")
    public void testUncertainSizeWith10PercentBuffer() {
        int estimatedSize = 100_000;
        int bufferSize = (int)(estimatedSize * 1.1);  // 10% 과할당
        ArrayList<Integer> list = new ArrayList<>();
        list.ensureCapacity(bufferSize);

        // 예상보다 10% 많은 요소 추가
        int actualSize = (int)(estimatedSize * 1.05);
        for (int i = 0; i < actualSize; i++) {
            list.add(i);
        }

        assertEquals(actualSize, list.size());

        // 메모리 낭비는 약 5% 미만
        double wastePercent = 100.0 * (bufferSize - actualSize) / bufferSize;
        assertTrue(wastePercent < 10,
            String.format("메모리 낭비 %.1f%% > 10%%", wastePercent));
    }

    @Test
    @DisplayName("Scenario 2: 불확실한 크기에 대한 안전 버퍼 (20% 과할당)")
    public void testUncertainSizeWith20PercentBuffer() {
        int estimatedSize = 100_000;
        int bufferSize = (int)(estimatedSize * 1.2);  // 20% 과할당
        ArrayList<Integer> list = new ArrayList<>();
        list.ensureCapacity(bufferSize);

        // 예상보다 15% 많은 요소 추가
        int actualSize = (int)(estimatedSize * 1.15);
        for (int i = 0; i < actualSize; i++) {
            list.add(i);
        }

        assertEquals(actualSize, list.size());

        // 메모리 낭비는 약 5% 미만
        double wastePercent = 100.0 * (bufferSize - actualSize) / bufferSize;
        assertTrue(wastePercent < 10,
            String.format("메모리 낭비 %.1f%% > 10%%", wastePercent));
    }

    @Test
    @DisplayName("Scenario 3: 배치 단위 적재")
    public void testBatchLoading() {
        ArrayList<Integer> list = new ArrayList<>();

        // 배치 1: 10,000개
        for (int i = 0; i < 10_000; i++) {
            if (list.size() + 10_000 > list.size() * 0.7) {
                list.ensureCapacity(list.size() + 10_000);
            }
            list.add(i);
        }
        assertEquals(10_000, list.size());

        // 배치 2: 15,000개
        for (int i = 10_000; i < 25_000; i++) {
            if (list.size() + 15_000 > list.size() * 0.7) {
                list.ensureCapacity(list.size() + 15_000);
            }
            list.add(i);
        }
        assertEquals(25_000, list.size());

        // 배치 3: 5,000개
        for (int i = 25_000; i < 30_000; i++) {
            list.add(i);
        }
        assertEquals(30_000, list.size());
    }

    @Test
    @DisplayName("ensureCapacity vs trimToSize의 메모리 효율")
    public void testMemoryEfficiencyWithTrimToSize() {
        // 큰 용량 할당 후 일부만 사용
        ArrayList<Integer> list = new ArrayList<>();
        list.ensureCapacity(100_000);

        // 30,000개만 추가
        for (int i = 0; i < 30_000; i++) {
            list.add(i);
        }

        assertEquals(30_000, list.size());

        // trimToSize 전: 70% 메모리 낭비
        // trimToSize 후: 낭비 제거
        list.trimToSize();

        // trimToSize 후에도 모든 요소 유지
        assertEquals(30_000, list.size());

        // 마지막 요소 확인
        assertEquals(29_999, (int) list.get(29_999));
    }

    @Test
    @DisplayName("배치 처리 시 과도한 리사이징 방지")
    public void testPreventsExcessiveResizing() {
        ArrayList<Integer> list = new ArrayList<>();

        // 10개의 배치, 각 10,000개씩
        int batchCount = 10;
        int batchSize = 10_000;

        for (int batch = 0; batch < batchCount; batch++) {
            // 70% 이상 찬 경우 확장
            if (list.size() > list.size() * 0.7) {
                list.ensureCapacity(list.size() + batchSize);
            }

            for (int i = 0; i < batchSize; i++) {
                list.add(batch * batchSize + i);
            }
        }

        assertEquals(100_000, list.size());

        // 무작위 접근으로 데이터 무결성 확인
        assertEquals(0, (int) list.get(0));
        assertEquals(50_000, (int) list.get(50_000));
        assertEquals(99_999, (int) list.get(99_999));
    }

    @Test
    @DisplayName("생성자 초기화 vs ensureCapacity 비교")
    public void testConstructorVsEnsuresize() {
        int testSize = 50_000;

        // 방법 1: 생성자에서 초기 용량 설정
        ArrayList<Integer> list1 = new ArrayList<>(testSize);
        for (int i = 0; i < testSize; i++) {
            list1.add(i);
        }

        // 방법 2: ensureCapacity 사용
        ArrayList<Integer> list2 = new ArrayList<>();
        list2.ensureCapacity(testSize);
        for (int i = 0; i < testSize; i++) {
            list2.add(i);
        }

        // 둘 다 동일한 결과
        assertEquals(testSize, list1.size());
        assertEquals(testSize, list2.size());

        // 내용 비교
        for (int i = 0; i < testSize; i++) {
            assertEquals(list1.get(i), list2.get(i));
        }
    }

    @Test
    @DisplayName("trimToSize 후 재추가 시 리사이징 발생")
    public void testResizingAfterTrimToSize() {
        ArrayList<Integer> list = new ArrayList<>(100);

        // 50개만 추가
        for (int i = 0; i < 50; i++) {
            list.add(i);
        }

        // trimToSize로 용량 정리
        list.trimToSize();
        assertEquals(50, list.size());

        // 51번째 추가 시 리사이징 발생 (50 → 75)
        list.add(50);
        assertEquals(51, list.size());

        // 모든 요소 유지 확인
        for (int i = 0; i <= 50; i++) {
            assertEquals(i, (int) list.get(i));
        }
    }

    @Test
    @DisplayName("컬렉션 addAll 성능")
    public void testAddAllPerformance() {
        ArrayList<Integer> source = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            source.add(i);
        }

        // 미리 용량 확보
        ArrayList<Integer> target = new ArrayList<>();
        target.ensureCapacity(10_000 + 5_000);  // 여유있게

        // addAll로 일괄 추가
        target.addAll(source);

        assertEquals(10_000, target.size());

        // 추가 데이터
        for (int i = 10_000; i < 15_000; i++) {
            target.add(i);
        }

        assertEquals(15_000, target.size());
    }

    @Test
    @DisplayName("Collections.unmodifiableList와 List.copyOf의 메모리 차이")
    public void testUnmodifiableVsCopyOf() {
        ArrayList<Integer> original = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            original.add(i);
        }

        // Collections.unmodifiableList: 래핑만 함 (원본 참조)
        List<Integer> unmodifiable = Collections.unmodifiableList(original);
        assertEquals(10_000, unmodifiable.size());

        // List.copyOf: 복사본 생성 (독립적)
        List<Integer> immutable = List.copyOf(original);
        assertEquals(10_000, immutable.size());

        // 원본 수정 시 unmodifiableList는 영향 받음
        original.add(10_000);
        assertEquals(10_001, unmodifiable.size());  // 래핑된 리스트도 영향
        assertEquals(10_000, immutable.size());    // 복사본은 영향 없음
    }

    @Test
    @DisplayName("containsAll과 retainAll의 성능")
    public void testBulkOperations() {
        ArrayList<Integer> list1 = new ArrayList<>();
        ArrayList<Integer> list2 = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            list1.add(i);
            if (i % 2 == 0) {
                list2.add(i);  // 짝수만
            }
        }

        // containsAll: list1이 list2의 모든 요소를 포함하는가?
        assertTrue(list1.containsAll(list2));

        // retainAll: list1에서 list2에 있는 것만 남김
        ArrayList<Integer> copy = new ArrayList<>(list1);
        copy.retainAll(list2);

        assertEquals(500, copy.size());  // 짝수 500개만 남음
        for (int value : copy) {
            assertEquals(0, value % 2);  // 모두 짝수
        }
    }

    @Test
    @DisplayName("ArrayList의 clear와 성능")
    public void testClearOperation() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            list.add(i);
        }

        assertEquals(100_000, list.size());

        // clear: 빠른 제거 (용량은 유지)
        list.clear();
        assertEquals(0, list.size());

        // 다시 추가할 때 리사이징 없음
        for (int i = 0; i < 50_000; i++) {
            list.add(i);
        }
        assertEquals(50_000, list.size());
    }

    @Test
    @DisplayName("ArrayList의 subList 뷰")
    public void testSubListView() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        // subList: 뷰 반환 (새로운 복사가 아님)
        List<Integer> subList = list.subList(2, 7);  // 인덱스 2~6
        assertEquals(5, subList.size());

        // 뷰를 통한 수정이 원본에 영향
        subList.set(0, 99);
        assertEquals(99, (int) list.get(2));

        // subList 내에서만 작동하는 연산
        subList.clear();
        assertEquals(5, list.size());  // 원본도 5개로 감소
    }

    @Test
    @DisplayName("ArrayList와 LinkedList의 성능 비교 (무작위 접근)")
    public void testArrayListVsLinkedListRandomAccess() {
        int size = 10_000;
        ArrayList<Integer> arrayList = new ArrayList<>();
        LinkedList<Integer> linkedList = new LinkedList<>();

        for (int i = 0; i < size; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        // ArrayList: O(1) 접근
        long startArray = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            int idx = (i * 97) % size;
            arrayList.get(idx);
        }
        long timeArray = System.nanoTime() - startArray;

        // LinkedList: O(n) 접근
        long startLinked = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            int idx = (i * 97) % size;
            linkedList.get(idx);
        }
        long timeLinked = System.nanoTime() - startLinked;

        // ArrayList가 무작위 접근에서 훨씬 빠름
        assertTrue(timeArray < timeLinked,
            String.format("ArrayList(%.0f ns) >= LinkedList(%.0f ns)",
                timeArray / 100.0, timeLinked / 100.0));
    }
}
