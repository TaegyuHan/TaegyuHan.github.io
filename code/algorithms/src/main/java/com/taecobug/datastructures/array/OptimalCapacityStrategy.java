package com.taecobug.datastructures.array;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 실무 최적화 전략: ensureCapacity와 trimToSize의 활용
 * 
 * 시나리오:
 * 1. 최종 크기를 아는 경우
 * 2. 예상 크기 ± 오차가 있는 경우 (안전 버퍼)
 * 3. 배치 단위 적재
 * 4. 병렬 적재 후 병합
 */
public class OptimalCapacityStrategy {

    /**
     * ArrayList의 내부 용량을 리플렉션으로 얻기
     * (public capacity() 메서드가 없어서 필요)
     */
    private static int getArrayListCapacity(ArrayList<?> list) {
        try {
            java.lang.reflect.Field field = ArrayList.class.getDeclaredField("elementData");
            field.setAccessible(true);
            Object[] elementData = (Object[]) field.get(list);
            return elementData.length;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return list.size();
        }
    }

    /**
     * 시나리오 1: 최종 크기를 정확히 알 때
     * 최고의 성능 (리사이징 0회)
     */
    public static List<Integer> scenario1_ExactSize(int finalSize) {
        System.out.println("\n=== Scenario 1: 정확한 최종 크기 ===");
        
        // 생성자에서 정확한 크기 지정 (가장 빠름)
        List<Integer> list = new ArrayList<>(finalSize);
        
        long start = System.nanoTime();
        for (int i = 0; i < finalSize; i++) {
            list.add(i);
        }
        long elapsed = System.nanoTime() - start;
        
        System.out.printf("크기: %d개, 시간: %.2f ms%n", finalSize, elapsed / 1_000_000.0);
        System.out.println("특징: 리사이징 0회, 복사 비용 최소");
        
        return list;
    }

    /**
     * 시나리오 2: 예상 크기는 있지만 오차가 있을 때
     * 안전 버퍼 전략 (10~20% 과할당)
     */
    public static List<Integer> scenario2_UncertainSize(int estimatedSize, double bufferPercent) {
        System.out.println("\n=== Scenario 2: 불확실한 크기 (안전 버퍼) ===");
        
        // 예상 크기의 1.2배로 과할당
        int allocatedSize = (int)(estimatedSize * (1.0 + bufferPercent));
        ArrayList<Integer> list = new ArrayList<>();
        list.ensureCapacity(allocatedSize);
        
        long start = System.nanoTime();
        
        // 예상과 다를 수 있는 실제 크기
        int actualSize = estimatedSize + (int)(estimatedSize * 0.15);
        for (int i = 0; i < actualSize; i++) {
            list.add(i);
        }
        
        long elapsed = System.nanoTime() - start;
        
        System.out.printf("예상 크기: %d, 실제 크기: %d, 할당 크기: %d%n", 
            estimatedSize, actualSize, allocatedSize);
        System.out.printf("메모리 낭비: %.1f%%\n", 
            100.0 * (allocatedSize - actualSize) / allocatedSize);
        System.out.printf("시간: %.2f ms%n", elapsed / 1_000_000.0);
        
        // 메모리 정리
        list.trimToSize();
        System.out.println("trimToSize 후: 메모리 정리 완료");
        
        return list;
    }

    /**
     * 시나리오 3: 배치 단위로 데이터 들어올 때
     * 배치 도착 시점에 동적으로 용량 조정
     */
    public static List<Integer> scenario3_BatchLoading() {
        System.out.println("\n=== Scenario 3: 배치 단위 적재 ===");
        
        ArrayList<Integer> list = new ArrayList<>();
        int batchNo = 0;
        int totalElements = 0;
        
        // 배치 데이터 생성 (실제로는 외부 소스에서 올 것)
        int[][] batches = {
            new int[100_000],
            new int[150_000],
            new int[80_000],
            new int[120_000]
        };
        
        long start = System.nanoTime();
        
        for (int[] batch : batches) {
            batchNo++;
            int batchSize = batch.length;
            int currentCapacity = getArrayListCapacity(list);
            
            // 현재 용량이 70% 이상 찼으면 미리 확장
            if (list.size() + batchSize > currentCapacity * 0.7) {
                int newCapacity = currentCapacity + batchSize + 10_000;
                System.out.printf("Batch %d: 용량 확장 필요 → %d로 조정%n", 
                    batchNo, newCapacity);
                list.ensureCapacity(newCapacity);
            }
            
            // 배치 추가
            for (int value : batch) {
                list.add(value);
            }
            totalElements += batchSize;
            
            System.out.printf("Batch %d 완료: %d개 추가, 누적: %d개%n", 
                batchNo, batchSize, totalElements);
        }
        
        long elapsed = System.nanoTime() - start;
        
        System.out.printf("총 시간: %.2f ms%n", elapsed / 1_000_000.0);
        System.out.println("특징: 배치마다 동적 조정으로 리사이징 횟수 최소화");
        
        list.trimToSize();
        return list;
    }

    /**
     * 시나리오 4: 병렬 적재 후 단일 스레드 병합
     * (멀티스레드 환경에서 동기화 회피)
     */
    public static List<Integer> scenario4_ParallelLoadAndMerge(int numThreads, int elementsPerThread) {
        System.out.println("\n=== Scenario 4: 병렬 적재 + 단일 스레드 병합 ===");
        
        // 1단계: 스레드별 리스트 생성
        List<ArrayList<Integer>> threadLists = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            threadLists.add(new ArrayList<>());
        }
        
        // 2단계: 병렬 데이터 적재
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        long startParallel = System.nanoTime();
        
        List<Future<?>> futures = new ArrayList<>();
        for (int threadId = 0; threadId < numThreads; threadId++) {
            final int tid = threadId;
            futures.add(executor.submit(() -> {
                ArrayList<Integer> threadList = threadLists.get(tid);
                // 스레드별 용량 미리 확보
                threadList.ensureCapacity(elementsPerThread);
                
                for (int i = 0; i < elementsPerThread; i++) {
                    threadList.add(tid * elementsPerThread + i);
                }
            }));
        }
        
        // 모든 스레드 완료 대기
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        long parallelTime = System.nanoTime() - startParallel;
        
        System.out.printf("병렬 적재 완료: %.2f ms%n", parallelTime / 1_000_000.0);
        System.out.printf("각 스레드 적재: %d개씩%n", elementsPerThread);
        
        // 3단계: 단일 스레드에서 최종 리스트 생성 및 병합
        long startMerge = System.nanoTime();
        
        ArrayList<Integer> finalList = new ArrayList<>();
        int totalSize = numThreads * elementsPerThread;
        finalList.ensureCapacity(totalSize);
        
        for (ArrayList<Integer> threadList : threadLists) {
            finalList.addAll(threadList);
        }
        
        long mergeTime = System.nanoTime() - startMerge;
        
        System.out.printf("병합 완료: %.2f ms%n", mergeTime / 1_000_000.0);
        System.out.printf("최종 크기: %d개%n", finalList.size());
        System.out.println("특징: 리사이징 충돌 회피, 높은 확장성");
        
        finalList.trimToSize();
        return finalList;
    }

    /**
     * 성능 비교: 여러 전략의 효율성 측정
     */
    public static void performanceComparison() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("성능 비교: 다양한 용량 관리 전략");
        System.out.println("=".repeat(60));
        
        int testSize = 500_000;
        int iterations = 3;
        
        // 전략 1: 용량 미할당
        long totalNoReserve = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            ArrayList<Integer> list = new ArrayList<>();
            for (int j = 0; j < testSize; j++) {
                list.add(j);
            }
            totalNoReserve += System.nanoTime() - start;
        }
        double avgNoReserve = totalNoReserve / iterations / 1_000_000.0;
        
        // 전략 2: ensureCapacity
        long totalEnsure = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            ArrayList<Integer> list = new ArrayList<>();
            list.ensureCapacity(testSize);
            for (int j = 0; j < testSize; j++) {
                list.add(j);
            }
            totalEnsure += System.nanoTime() - start;
        }
        double avgEnsure = totalEnsure / iterations / 1_000_000.0;
        
        // 전략 3: 생성자 초기화
        long totalConstructor = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            ArrayList<Integer> list = new ArrayList<>(testSize);
            for (int j = 0; j < testSize; j++) {
                list.add(j);
            }
            totalConstructor += System.nanoTime() - start;
        }
        double avgConstructor = totalConstructor / iterations / 1_000_000.0;
        
        // 전략 4: 안전 버퍼 (20% 과할당)
        long totalBuffer = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            ArrayList<Integer> list = new ArrayList<>();
            list.ensureCapacity((int)(testSize * 1.2));
            for (int j = 0; j < testSize; j++) {
                list.add(j);
            }
            totalBuffer += System.nanoTime() - start;
        }
        double avgBuffer = totalBuffer / iterations / 1_000_000.0;
        
        System.out.printf("테스트 크기: %d개, 반복: %d회%n\n", testSize, iterations);
        System.out.printf("1. 용량 미할당:      %.2f ms (기준)%n", avgNoReserve);
        System.out.printf("2. ensureCapacity: %.2f ms (%.1fx 빠름)%n", 
            avgEnsure, avgNoReserve / avgEnsure);
        System.out.printf("3. 생성자 초기화:    %.2f ms (%.1fx 빠름)%n", 
            avgConstructor, avgNoReserve / avgConstructor);
        System.out.printf("4. 안전 버퍼 20%%:   %.2f ms (%.1fx 빠름)%n", 
            avgBuffer, avgNoReserve / avgBuffer);
    }

    /**
     * 메모리 효율성 분석
     */
    public static void memoryAnalysis() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("메모리 효율성 분석");
        System.out.println("=".repeat(60));
        
        int finalSize = 1_000_000;
        int actualElements = 600_000;
        
        System.out.printf("최종 할당: %d개, 실제 요소: %d개%n\n", finalSize, actualElements);
        
        // 경우 1: trimToSize 미사용
        System.out.println("Case 1: trimToSize 미사용");
        double wastePercentage1 = 100.0 * (finalSize - actualElements) / finalSize;
        System.out.printf("메모리 낭비: %.1f%%\n", wastePercentage1);
        System.out.printf("낭비량: 약 %d개 요소 크기\n", finalSize - actualElements);
        
        // 경우 2: trimToSize 사용
        System.out.println("\nCase 2: trimToSize 사용");
        double wastePercentage2 = 100.0 * (actualElements - actualElements) / actualElements;
        System.out.printf("메모리 낭비: %.1f%%\n", wastePercentage2);
        System.out.printf("절약량: 약 %d개 요소 크기\n", finalSize - actualElements);
        System.out.printf("공간 절약율: %.1f%%\n", 
            100.0 * (finalSize - actualElements) / finalSize);
    }

    public static void main(String[] args) {
        // 각 시나리오 실행
        scenario1_ExactSize(1_000_000);
        scenario2_UncertainSize(1_000_000, 0.2); // 20% 과할당
        scenario3_BatchLoading();
        scenario4_ParallelLoadAndMerge(4, 100_000);
        
        // 종합 비교
        performanceComparison();
        memoryAnalysis();
    }
}
