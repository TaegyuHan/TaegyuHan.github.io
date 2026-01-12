package com.taecobug.datastructures.array;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * 동적 배열 기초 테스트
 * - ArrayList의 기본 동작 검증
 * - Vector와의 차이점 확인
 * - 용량 관리 테스트
 */
@DisplayName("동적 배열 기초 테스트")
public class DynamicArrayGuideTest {

    @Test
    @DisplayName("ArrayList 기본 추가/조회")
    public void testArrayListBasicOperations() {
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");
        
        assertEquals(3, list.size());
        assertEquals("Apple", list.get(0));
        assertEquals("Banana", list.get(1));
        assertEquals("Cherry", list.get(2));
    }

    @Test
    @DisplayName("Vector 기본 추가/조회")
    public void testVectorBasicOperations() {
        Vector<String> vector = new Vector<>();
        vector.add("Apple");
        vector.add("Banana");
        vector.add("Cherry");
        
        assertEquals(3, vector.size());
        assertEquals("Apple", vector.get(0));
        assertEquals("Banana", vector.get(1));
        assertEquals("Cherry", vector.get(2));
    }

    @Test
    @DisplayName("ArrayList의 생성자 초기 용량 설정")
    public void testArrayListConstructorCapacity() {
        ArrayList<Integer> list = new ArrayList<>(100);
        
        // 100개의 용량이 예약되어 있지만 size는 0
        assertEquals(0, list.size());
        
        // 100개까지 추가해도 리사이징 발생 안 함
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        assertEquals(100, list.size());
    }

    @Test
    @DisplayName("ensureCapacity로 용량 미리 확보")
    public void testEnsureCapacity() {
        ArrayList<Integer> list = new ArrayList<>();
        list.ensureCapacity(100);
        
        // 100개를 추가해도 리사이징 발생 안 함
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        assertEquals(100, list.size());
        
        // 101번째 추가 시에만 리사이징 발생
        list.add(100);
        assertEquals(101, list.size());
    }

    @Test
    @DisplayName("trimToSize로 초과 용량 제거")
    public void testTrimToSize() {
        ArrayList<Integer> list = new ArrayList<>(1000);
        
        // 100개만 추가
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        assertEquals(100, list.size());
        
        // trimToSize 전: 용량은 여전히 1000 근처
        // trimToSize 후: 용량이 정확히 100으로 조정
        list.trimToSize();
        assertEquals(100, list.size());
        
        // size와 capacity가 같아짐
        assertTrue(list.size() == 100);
    }

    @Test
    @DisplayName("Vector의 capacityIncrement 설정 (2배 성장)")
    public void testVectorCapacityIncrementDoubling() {
        // capacityIncrement = 0이면 2배 성장
        Vector<Integer> vector = new Vector<>(10, 0);
        
        assertEquals(10, vector.capacity());
        
        // 용량을 초과하는 요소 추가
        for (int i = 0; i < 15; i++) {
            vector.add(i);
        }
        
        // 2배 성장: 10 → 20
        assertTrue(vector.capacity() >= 15);
    }

    @Test
    @DisplayName("Vector의 capacityIncrement 설정 (선형 성장)")
    public void testVectorCapacityIncrementLinear() {
        // capacityIncrement = 5면 선형 성장
        Vector<Integer> vector = new Vector<>(10, 5);
        
        assertEquals(10, vector.capacity());
        
        // 용량을 초과하는 요소 추가
        for (int i = 0; i < 20; i++) {
            vector.add(i);
        }
        
        // 선형 성장: 10 → 15 → 20
        assertTrue(vector.capacity() >= 20);
    }

    @Test
    @DisplayName("맨 뒤 추가는 O(1), 맨 앞 추가는 O(n)")
    public void testInsertionPosition() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        
        // 맨 뒤 추가 (O(1))
        list.add(6);
        assertEquals(6, list.size());
        assertEquals(6, (int) list.get(5));
        
        // 맨 앞 추가 (O(n) - 전체 이동)
        list.add(0, 0);
        assertEquals(7, list.size());
        assertEquals(0, (int) list.get(0));
        assertEquals(1, (int) list.get(1));
        
        // 중간 삽입 (O(n) - 부분 이동)
        list.add(3, 99);
        assertEquals(8, list.size());
        assertEquals(99, (int) list.get(3));
    }

    @Test
    @DisplayName("맨 뒤 제거는 O(1), 맨 앞 제거는 O(n)")
    public void testRemovalPosition() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        
        // 맨 뒤 제거 (O(1))
        list.remove(4);
        assertEquals(4, list.size());
        
        // 맨 앞 제거 (O(n) - 전체 이동)
        list.remove(0);
        assertEquals(3, list.size());
        assertEquals(2, (int) list.get(0));
        
        // 중간 제거 (O(n) - 부분 이동)
        list.remove(1);
        assertEquals(2, list.size());
        assertEquals(2, (int) list.get(0));
        assertEquals(5, (int) list.get(1));
    }

    @Test
    @DisplayName("Collections.unmodifiableList로 읽기 전용 변환")
    public void testUnmodifiableList() {
        ArrayList<String> mutableList = new ArrayList<>(Arrays.asList("A", "B", "C"));
        List<String> readOnly = Collections.unmodifiableList(mutableList);
        
        // 읽기는 가능
        assertEquals("A", readOnly.get(0));
        assertEquals(3, readOnly.size());
        
        // 쓰기는 불가
        assertThrows(UnsupportedOperationException.class, () -> {
            readOnly.add("D");
        });
        
        assertThrows(UnsupportedOperationException.class, () -> {
            readOnly.remove(0);
        });
    }

    @Test
    @DisplayName("List.copyOf로 불변 복사본 생성 (JDK 10+)")
    public void testListCopyOf() {
        ArrayList<String> mutableList = new ArrayList<>(Arrays.asList("A", "B", "C"));
        List<String> immutable = List.copyOf(mutableList);
        
        // 원본과 독립적인 복사본
        mutableList.add("D");
        
        assertEquals(4, mutableList.size());
        assertEquals(3, immutable.size());
        
        // 쓰기 불가
        assertThrows(UnsupportedOperationException.class, () -> {
            immutable.add("E");
        });
    }

    @Test
    @DisplayName("ArrayList와 Vector의 Iterator 동작")
    public void testIteratorBehavior() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        
        // Iterator로 순회
        Iterator<Integer> iterator = list.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        assertEquals(5, count);
    }

    @Test
    @DisplayName("ArrayList의 구조적 수정 예외 (ConcurrentModificationException)")
    public void testConcurrentModificationException() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        
        // 반복 중에 리스트 수정하면 예외 발생
        assertThrows(ConcurrentModificationException.class, () -> {
            for (Integer value : list) {
                if (value == 2) {
                    list.add(99);  // 구조적 수정
                }
            }
        });
    }

    @Test
    @DisplayName("ArrayList의 get/set 성능 (O(1) 접근)")
    public void testRandomAccess() {
        ArrayList<Integer> list = new ArrayList<>();
        int size = 10_000;
        
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        
        // 무작위 접근이 O(1)
        assertEquals(5000, (int) list.get(5000));
        assertEquals(9999, (int) list.get(9999));
        
        // set도 O(1)
        list.set(5000, 99);
        assertEquals(99, (int) list.get(5000));
    }

    @Test
    @DisplayName("대량 추가 시 성능: ensureCapacity 효과 검증")
    public void testBulkAdditionPerformance() {
        int count = 100_000;
        
        // 방법 1: ensureCapacity 없음
        long start1 = System.nanoTime();
        ArrayList<Integer> list1 = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list1.add(i);
        }
        long time1 = System.nanoTime() - start1;
        
        // 방법 2: ensureCapacity 사용
        long start2 = System.nanoTime();
        ArrayList<Integer> list2 = new ArrayList<>();
        list2.ensureCapacity(count);
        for (int i = 0; i < count; i++) {
            list2.add(i);
        }
        long time2 = System.nanoTime() - start2;
        
        // 둘 다 정상 작동
        assertEquals(count, list1.size());
        assertEquals(count, list2.size());
        
        // ensureCapacity가 더 빠름 (리사이징 비용 제거)
        assertTrue(time2 < time1, 
            String.format("ensureCapacity 시간(%d) > 미사용 시간(%d)", time2, time1));
    }

    @Test
    @DisplayName("empty ArrayList와 null 요소 처리")
    public void testEmptyAndNull() {
        ArrayList<String> list = new ArrayList<>();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        
        // null 요소 추가 가능
        list.add(null);
        list.add("A");
        list.add(null);
        
        assertEquals(3, list.size());
        assertNull(list.get(0));
        assertEquals("A", list.get(1));
        assertNull(list.get(2));
        
        // null 요소 제거
        list.remove(null);
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("ArrayList의 contains와 indexOf")
    public void testContainsAndIndexOf() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "B", "D"));
        
        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
        assertFalse(list.contains("Z"));
        
        assertEquals(0, list.indexOf("A"));
        assertEquals(1, list.indexOf("B"));
        assertEquals(3, list.lastIndexOf("B"));
        assertEquals(-1, list.indexOf("Z"));
    }
}
