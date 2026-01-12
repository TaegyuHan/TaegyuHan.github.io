package com.taecobug.datastructures.array;

import java.util.*;

/**
 * 동적 배열 기초: ArrayList와 Vector의 차이점 이해
 * - ArrayList: 비동기화, 1.5배 성장률
 * - Vector: 동기화, 2배 또는 capacityIncrement 선택
 */
public class DynamicArrayGuide {

    /**
     * 1. ArrayList의 기본 사용법
     */
    public static void basicArrayListExample() {
        System.out.println("\n=== ArrayList 기본 사용 ===");
        
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");
        
        System.out.println("List: " + list);
        System.out.println("Size: " + list.size());
        System.out.println("Get at index 1: " + list.get(1));
    }

    /**
     * 2. Vector의 기본 사용법
     * (주의: 모든 메서드가 synchronized)
     */
    public static void basicVectorExample() {
        System.out.println("\n=== Vector 기본 사용 (동기화됨) ===");
        
        Vector<String> vector = new Vector<>();
        vector.add("Apple");
        vector.add("Banana");
        vector.add("Cherry");
        
        System.out.println("Vector: " + vector);
        System.out.println("Capacity: " + vector.capacity());
        System.out.println("Size: " + vector.size());
    }

    /**
     * 3. ArrayList의 초기 용량 설정
     * 성능 최적화의 가장 기본 전략
     */
    public static void capacityManagement() {
        System.out.println("\n=== 용량 관리 ===");
        
        // Case 1: 정확한 크기를 알 때 (가장 빠름)
        List<Integer> exactSize = new ArrayList<>(1000);
        System.out.println("정확한 크기: 1000개 예상 시간 최소");
        
        // Case 2: ensureCapacity로 동적 확보
        ArrayList<Integer> dynamic = new ArrayList<>();
        dynamic.ensureCapacity(1000);
        System.out.println("ensureCapacity로 미리 확보: 리사이징 1회로 제한");
        
        // Case 3: trimToSize로 메모리 정리
        for (int i = 0; i < 600; i++) {
            dynamic.add(i);
        }
        dynamic.trimToSize();
        System.out.println("trimToSize 후: 초과 용량 제거");
    }

    /**
     * 4. Vector의 capacityIncrement 설정
     * (거의 사용되지 않음 - ArrayList 권장)
     */
    public static void vectorCapacityIncrement() {
        System.out.println("\n=== Vector의 capacityIncrement ===");
        
        // capacityIncrement = 0 → 2배 성장
        Vector<Integer> doubling = new Vector<>(10, 0);
        System.out.println("capacityIncrement=0: 2배 성장 (10 → 20 → 40...)");
        
        // capacityIncrement = 5 → 선형 성장
        Vector<Integer> linear = new Vector<>(10, 5);
        System.out.println("capacityIncrement=5: 선형 성장 (10 → 15 → 20...)");
        
        System.out.println("→ 대부분 ArrayList + 외부 동기화가 더 나음");
    }

    /**
     * 5. 삽입 연산의 비용
     */
    public static void insertionCost() {
        System.out.println("\n=== 삽입 연산의 비용 ===");
        
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        
        // O(1) - 맨 뒤에 추가
        System.out.println("맨 뒤 추가 (index=size): O(1)");
        list.add(6);
        
        // O(n) - 앞에 삽입 (전체 이동)
        System.out.println("맨 앞 삽입 (index=0): O(n), 비용 높음");
        list.add(0, 0);
        System.out.println("결과: " + list);
        
        // O(n) - 중간 삽입
        System.out.println("중간 삽입 (index=3): O(n)");
        list.add(3, 99);
        System.out.println("결과: " + list);
    }

    /**
     * 6. 삭제 연산의 비용
     */
    public static void removalCost() {
        System.out.println("\n=== 삭제 연산의 비용 ===");
        
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        
        // O(1) - 맨 뒤 삭제
        System.out.println("맨 뒤 삭제: O(1)");
        list.remove(list.size() - 1);
        
        // O(n) - 앞 삭제 (전체 이동)
        System.out.println("맨 앞 삭제: O(n), 비용 높음");
        list.remove(0);
        System.out.println("결과: " + list);
        
        System.out.println("→ 중간 삭제가 빈번하면 LinkedList 고려");
    }

    /**
     * 7. 대량 삽입 시 성능 차이
     */
    public static void bulkInsertionPerformance() {
        System.out.println("\n=== 대량 삽입 성능 비교 ===");
        
        int count = 100_000;
        
        // 방법 1: 용량 미리 할당 안 함
        long start = System.nanoTime();
        List<Integer> noReserve = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            noReserve.add(i);
        }
        long noReserveTime = System.nanoTime() - start;
        
        // 방법 2: ensureCapacity로 미리 할당
        start = System.nanoTime();
        ArrayList<Integer> withEnsure = new ArrayList<>();
        withEnsure.ensureCapacity(count);
        for (int i = 0; i < count; i++) {
            withEnsure.add(i);
        }
        long ensureTime = System.nanoTime() - start;
        
        // 방법 3: 생성자에서 초기 용량 지정 (가장 빠름)
        start = System.nanoTime();
        List<Integer> withConstructor = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            withConstructor.add(i);
        }
        long constructorTime = System.nanoTime() - start;
        
        System.out.printf("용량 미할당:        %.2f ms%n", noReserveTime / 1_000_000.0);
        System.out.printf("ensureCapacity:   %.2f ms (%.1fx 빠름)%n", 
            ensureTime / 1_000_000.0, 
            (double) noReserveTime / ensureTime);
        System.out.printf("생성자 초기화:      %.2f ms (%.1fx 빠름)%n", 
            constructorTime / 1_000_000.0, 
            (double) noReserveTime / constructorTime);
    }

    /**
     * 8. 메모리 효율성: trimToSize 효과
     */
    public static void memoryEfficiency() {
        System.out.println("\n=== 메모리 효율성: trimToSize ===");
        
        ArrayList<Integer> list = new ArrayList<>(1000);
        
        // 500개만 추가
        for (int i = 0; i < 500; i++) {
            list.add(i);
        }
        
        System.out.println("trimToSize 전: 내부 배열 크기 ~1000");
        System.out.println("실제 요소: 500개");
        System.out.println("메모리 낭비: ~50%");
        
        list.trimToSize();
        System.out.println("\ntrimToSize 후: 내부 배열 크기 500");
        System.out.println("메모리 낭비: 0%");
        System.out.println("(대신 이후 add 시 리사이징 발생 가능)");
    }

    /**
     * 9. 읽기 전용 리스트로 변환
     * (구조적 수정 방지)
     */
    public static void readOnlyTransformation() {
        System.out.println("\n=== 읽기 전용 리스트 ===");
        
        ArrayList<String> mutableList = new ArrayList<>(Arrays.asList("A", "B", "C"));
        
        // Collections.unmodifiableList: 래핑으로 수정 차단
        List<String> readOnly = Collections.unmodifiableList(mutableList);
        System.out.println("unmodifiableList: " + readOnly);
        
        // List.copyOf (JDK 10+): 복사본 생성, 원본과 독립적
        List<String> copyOf = List.copyOf(mutableList);
        System.out.println("List.copyOf: " + copyOf);
        
        System.out.println("→ 대량 적재 후 읽기 전용: trimToSize + unmodifiableList/copyOf");
    }

    /**
     * 10. Vector와 ArrayList의 동기화 비용
     */
    public static void synchronizationOverhead() {
        System.out.println("\n=== 동기화 오버헤드 ===");
        
        int iterations = 1_000_000;
        
        // ArrayList (비동기화)
        long start = System.nanoTime();
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            list.add(i);
        }
        long arrayListTime = System.nanoTime() - start;
        
        // Vector (동기화됨, 모든 메서드에 synchronized)
        start = System.nanoTime();
        Vector<Integer> vector = new Vector<>();
        for (int i = 0; i < iterations; i++) {
            vector.add(i);
        }
        long vectorTime = System.nanoTime() - start;
        
        System.out.printf("ArrayList: %.2f ms%n", arrayListTime / 1_000_000.0);
        System.out.printf("Vector:    %.2f ms (%.1fx 느림)%n", 
            vectorTime / 1_000_000.0, 
            (double) vectorTime / arrayListTime);
        
        System.out.println("\n→ Vector 사용 피할 것! 필요하면 ArrayList + 외부 동기화 권장");
    }

    public static void main(String[] args) {
        basicArrayListExample();
        basicVectorExample();
        capacityManagement();
        vectorCapacityIncrement();
        insertionCost();
        removalCost();
        bulkInsertionPerformance();
        memoryEfficiency();
        readOnlyTransformation();
        synchronizationOverhead();
    }
}
