package com.taecobug.datastructures.array;

import java.util.*;

/**
 * ArrayList vs LinkedList vs ArrayDeque 성능 비교
 * - 삽입/삭제 위치별 성능 차이
 * - 양끝 연산 최적화
 * - 랜덤 접근 vs 순차 접근
 */
public class ListPerformanceBenchmark {

    private static final int ELEMENT_COUNT = 100_000;
    private static final int OPERATIONS = 10_000;

    /**
     * 첫 위치(head) 삽입: LinkedList O(1), ArrayList O(n)
     */
    static class HeadInsertionBenchmark {
        public static void run() {
            System.out.println("\n=== Head 삽입 (index 0에 삽입) ===");
            System.out.println("- LinkedList: O(1) 포인터 조작");
            System.out.println("- ArrayList: O(n) 배열 시프팅");
            System.out.println("- ArrayDeque: O(1) 원형 버퍼\n");

            long timeArrayList = benchmarkHeadInsertion(new ArrayList<>());
            long timeLinkedList = benchmarkHeadInsertion(new LinkedList<>());
            long timeArrayDeque = benchmarkHeadInsertion(new ArrayDeque<>());

            System.out.printf("ArrayList:  %.2f ms\n", timeArrayList / 1e6);
            System.out.printf("LinkedList: %.2f ms (%.2f배)\n", timeLinkedList / 1e6, (double) timeLinkedList / timeArrayList);
            System.out.printf("ArrayDeque: %.2f ms (%.2f배)\n", timeArrayDeque / 1e6, (double) timeArrayDeque / timeArrayList);
        }

        private static long benchmarkHeadInsertion(Collection<Integer> collection) {
            // 초기 데이터 채우기
            for (int i = 0; i < ELEMENT_COUNT; i++) {
                collection.add(i);
            }

            long start = System.nanoTime();
            for (int i = 0; i < OPERATIONS; i++) {
                if (collection instanceof List) {
                    ((List<Integer>) collection).add(0, i);  // Head 삽입
                } else {
                    ((Deque<Integer>) collection).addFirst(i);  // Deque Head 삽입
                }
            }
            long elapsed = System.nanoTime() - start;

            return elapsed;
        }
    }

    /**
     * 양끝 연산: ArrayDeque > LinkedList > ArrayList
     */
    static class DequeOperationsBenchmark {
        public static void run() {
            System.out.println("\n=== 양끝 연산 (Queue 패턴) ===");
            System.out.println("- ArrayDeque: O(1) 원형 버퍼 (최적화)");
            System.out.println("- LinkedList: O(1) 포인터 조작 (메모리 간접 참조 비용)");
            System.out.println("- ArrayList: 지원 안함\n");

            long timeArrayDeque = benchmarkDequeOps(new ArrayDeque<>());
            long timeLinkedList = benchmarkDequeOps(new LinkedList<>());

            System.out.printf("ArrayDeque: %.2f ms\n", timeArrayDeque / 1e6);
            System.out.printf("LinkedList: %.2f ms (%.2f배)\n", timeLinkedList / 1e6, (double) timeLinkedList / timeArrayDeque);
        }

        private static long benchmarkDequeOps(Deque<Integer> deque) {
            // 초기 데이터
            for (int i = 0; i < ELEMENT_COUNT; i++) {
                deque.add(i);
            }

            long start = System.nanoTime();
            for (int i = 0; i < OPERATIONS; i++) {
                deque.addLast(i);      // O(1)
                deque.removeFirst();   // O(1)
                deque.addFirst(i);     // O(1)
                deque.removeLast();    // O(1)
            }
            long elapsed = System.nanoTime() - start;

            return elapsed;
        }
    }

    /**
     * 랜덤 접근: ArrayList >> LinkedList
     */
    static class RandomAccessBenchmark {
        public static void run() {
            System.out.println("\n=== 랜덤 접근 (get by index) ===");
            System.out.println("- ArrayList: O(1) 인덱스 직접 접근");
            System.out.println("- LinkedList: O(n) 선형 탐색 (포인터 따라가기)\n");

            int[] indices = generateRandomIndices();

            long timeArrayList = benchmarkRandomAccess(new ArrayList<>(), indices);
            long timeLinkedList = benchmarkRandomAccess(new LinkedList<>(), indices);

            System.out.printf("ArrayList:  %.2f ms\n", timeArrayList / 1e6);
            System.out.printf("LinkedList: %.2f ms (%.2f배)\n", timeLinkedList / 1e6, (double) timeLinkedList / timeArrayList);
        }

        private static int[] generateRandomIndices() {
            Random random = new Random(42);
            int[] indices = new int[OPERATIONS];
            for (int i = 0; i < OPERATIONS; i++) {
                indices[i] = random.nextInt(ELEMENT_COUNT);
            }
            return indices;
        }

        private static long benchmarkRandomAccess(List<Integer> list, int[] indices) {
            // 초기 데이터
            for (int i = 0; i < ELEMENT_COUNT; i++) {
                list.add(i);
            }

            long start = System.nanoTime();
            long sum = 0;
            for (int idx : indices) {
                sum += list.get(idx);  // O(1) for ArrayList, O(n) for LinkedList
            }
            long elapsed = System.nanoTime() - start;

            if (sum == Long.MIN_VALUE) System.out.println("unreachable");
            return elapsed;
        }
    }

    /**
     * 중간 삽입/삭제: 모두 O(n) 이지만 실제 성능은 다름
     */
    static class MiddleInsertionBenchmark {
        public static void run() {
            System.out.println("\n=== 중간 삽입 (random position) ===");
            System.out.println("- ArrayList: O(n) 배열 시프팅 (상수 계수 작음)");
            System.out.println("- LinkedList: O(n) 노드 탐색 + O(1) 포인터 조작\n");

            int[] positions = generateRandomPositions();

            long timeArrayList = benchmarkMiddleInsertion(new ArrayList<>(), positions);
            long timeLinkedList = benchmarkMiddleInsertion(new LinkedList<>(), positions);

            System.out.printf("ArrayList:  %.2f ms\n", timeArrayList / 1e6);
            System.out.printf("LinkedList: %.2f ms (%.2f배)\n", timeLinkedList / 1e6, (double) timeLinkedList / timeArrayList);
        }

        private static int[] generateRandomPositions() {
            Random random = new Random(42);
            int[] positions = new int[OPERATIONS];
            for (int i = 0; i < OPERATIONS; i++) {
                positions[i] = random.nextInt(ELEMENT_COUNT / 2);  // 중간 어딘가
            }
            return positions;
        }

        private static long benchmarkMiddleInsertion(List<Integer> list, int[] positions) {
            // 초기 데이터
            for (int i = 0; i < ELEMENT_COUNT; i++) {
                list.add(i);
            }

            long start = System.nanoTime();
            for (int pos : positions) {
                list.add(pos, -1);  // 중간에 삽입
            }
            long elapsed = System.nanoTime() - start;

            return elapsed;
        }
    }

    /**
     * 반복 순회: ArrayList >= LinkedList (캐시 지역성)
     */
    static class IterationBenchmark {
        public static void run() {
            System.out.println("\n=== 순회 반복 (iteration) ===");
            System.out.println("- ArrayList: 캐시 친화적 (연속 메모리)");
            System.out.println("- LinkedList: 캐시 미스 (포인터 따라가기)\n");

            long timeArrayList = benchmarkIteration(new ArrayList<>());
            long timeLinkedList = benchmarkIteration(new LinkedList<>());

            System.out.printf("ArrayList:  %.2f ms\n", timeArrayList / 1e6);
            System.out.printf("LinkedList: %.2f ms (%.2f배)\n", timeLinkedList / 1e6, (double) timeLinkedList / timeArrayList);
        }

        private static long benchmarkIteration(List<Integer> list) {
            // 초기 데이터
            for (int i = 0; i < ELEMENT_COUNT; i++) {
                list.add(i);
            }

            long start = System.nanoTime();
            long sum = 0;
            for (int value : list) {
                sum += value;
            }
            long elapsed = System.nanoTime() - start;

            if (sum == Long.MIN_VALUE) System.out.println("unreachable");
            return elapsed;
        }
    }

    public static void runAllBenchmarks() {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║   List 구현 성능 비교: ArrayList/LinkedList/ArrayDeque   ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println("기본 크기: " + ELEMENT_COUNT + " elements");
        System.out.println("반복 연산: " + OPERATIONS + " operations\n");

        HeadInsertionBenchmark.run();
        DequeOperationsBenchmark.run();
        RandomAccessBenchmark.run();
        MiddleInsertionBenchmark.run();
        IterationBenchmark.run();

        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("결론:");
        System.out.println("✓ 랜덤 접근 주도: ArrayList");
        System.out.println("✓ 양끝 큐/덱 연산: ArrayDeque (LinkedList보다 실제 성능 우수)");
        System.out.println("✓ 첫 위치만 지속 삽입: LinkedList (but 임의 위치가 섞이면 비추천)");
        System.out.println("✓ 중간 삽입/삭제가 많으면: 알고리즘 재설계 필요");
        System.out.println("═══════════════════════════════════════════════════\n");
    }
}
