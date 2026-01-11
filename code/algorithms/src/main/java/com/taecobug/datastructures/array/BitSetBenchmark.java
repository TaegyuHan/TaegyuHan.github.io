package com.taecobug.datastructures.array;

import java.util.*;

/**
 * BitSet 성능 벤치마크
 * - boolean[] vs BitSet 메모리 효율성
 * - 비트 연산 성능
 * - 대량 부울값 처리에서의 최적화
 */
public class BitSetBenchmark {

    private static final int SIZE = 10_000_000;  // 1천만 비트
    private static final int ITERATIONS = 100;

    /**
     * boolean[] 사용:
     * - 각 boolean이 1바이트(패딩 포함) 사용
     * - 총 메모리: ~10MB
     */
    static class BooleanArrayBenchmark {
        public static void run() {
            System.out.println("\n=== boolean[] 벤치마크 ===");
            
            boolean[] arr = new boolean[SIZE];
            
            // 패턴 1: 설정
            long startSet = System.nanoTime();
            for (int i = 0; i < SIZE; i++) {
                arr[i] = (i % 2) == 0;
            }
            long timeSet = System.nanoTime() - startSet;
            System.out.printf("설정: %.2f ms\n", timeSet / 1e6);

            // 패턴 2: 읽기 및 카운팅
            long startCount = System.nanoTime();
            int count = 0;
            for (int i = 0; i < SIZE; i++) {
                if (arr[i]) count++;
            }
            long timeCount = System.nanoTime() - startCount;
            System.out.printf("카운팅: %.2f ms (true 개수: %d)\n", timeCount / 1e6, count);

            // 패턴 3: 토글
            long startToggle = System.nanoTime();
            for (int i = 0; i < SIZE; i++) {
                arr[i] = !arr[i];
            }
            long timeToggle = System.nanoTime() - startToggle;
            System.out.printf("토글: %.2f ms\n", timeToggle / 1e6);
            
            System.out.printf("메모리 사용: ~%.2f MB (실제로는 패딩으로 더 많음)\n", SIZE / 1e6);
        }
    }

    /**
     * BitSet 사용:
     * - 8개의 비트가 1바이트에 패킹됨
     * - 총 메모리: ~1.25MB (8배 절감)
     */
    static class BitSetBenchmarkInner {
        public static void run() {
            System.out.println("\n=== BitSet 벤치마크 ===");
            
            BitSet bitSet = new BitSet(SIZE);
            
            // 패턴 1: 설정
            long startSet = System.nanoTime();
            for (int i = 0; i < SIZE; i++) {
                if ((i % 2) == 0) {
                    bitSet.set(i);
                }
            }
            long timeSet = System.nanoTime() - startSet;
            System.out.printf("설정: %.2f ms\n", timeSet / 1e6);

            // 패턴 2: 읽기 및 카운팅
            long startCount = System.nanoTime();
            int count = bitSet.cardinality();  // 최적화된 popcount
            long timeCount = System.nanoTime() - startCount;
            System.out.printf("카운팅: %.2f ms (true 개수: %d)\n", timeCount / 1e6, count);

            // 패턴 3: 토글
            long startToggle = System.nanoTime();
            bitSet.flip(0, SIZE);
            long timeToggle = System.nanoTime() - startToggle;
            System.out.printf("토글: %.2f ms\n", timeToggle / 1e6);
            
            System.out.printf("메모리 사용: ~%.2f MB (8배 효율)\n", SIZE / 8.0 / 1e6);
        }
    }

    /**
     * 집합 연산: BitSet의 강점
     */
    static class SetOperationsBenchmark {
        public static void run() {
            System.out.println("\n=== 집합 연산 성능 (BitSet 최적화) ===");
            
            BitSet set1 = new BitSet(SIZE);
            BitSet set2 = new BitSet(SIZE);
            
            // 초기화
            for (int i = 0; i < SIZE; i += 3) {
                set1.set(i);
            }
            for (int i = 0; i < SIZE; i += 5) {
                set2.set(i);
            }

            // 합집합(OR)
            long startOr = System.nanoTime();
            BitSet union = (BitSet) set1.clone();
            union.or(set2);
            long timeOr = System.nanoTime() - startOr;
            System.out.printf("합집합 (OR): %.2f ms (결과: %d)\n", timeOr / 1e6, union.cardinality());

            // 교집합(AND)
            long startAnd = System.nanoTime();
            BitSet intersection = (BitSet) set1.clone();
            intersection.and(set2);
            long timeAnd = System.nanoTime() - startAnd;
            System.out.printf("교집합 (AND): %.2f ms (결과: %d)\n", timeAnd / 1e6, intersection.cardinality());

            // 차집합(AND NOT)
            long startAndNot = System.nanoTime();
            BitSet difference = (BitSet) set1.clone();
            difference.andNot(set2);
            long timeAndNot = System.nanoTime() - startAndNot;
            System.out.printf("차집합 (AND NOT): %.2f ms (결과: %d)\n", timeAndNot / 1e6, difference.cardinality());
        }
    }

    /**
     * 실제 사용 사례: 소수 판별 (Sieve of Eratosthenes)
     * BitSet vs boolean[] 성능 비교
     */
    static class SieveOfEratosthenesBenchmark {
        public static void run() {
            System.out.println("\n=== 에라토스테네스의 체: BitSet vs boolean[] ===");
            
            // boolean[] 사용
            long startBool = System.nanoTime();
            boolean[] isPrime = new boolean[SIZE];
            Arrays.fill(isPrime, true);
            sieveBooleanArray(isPrime);
            long timeBool = System.nanoTime() - startBool;
            int countBool = 0;
            for (boolean b : isPrime) if (b) countBool++;

            // BitSet 사용
            long startBit = System.nanoTime();
            BitSet bitSetPrime = new BitSet(SIZE);
            bitSetPrime.set(0, SIZE);  // 모두 true로 설정
            sieveBitSet(bitSetPrime);
            long timeBit = System.nanoTime() - startBit;
            int countBit = bitSetPrime.cardinality();

            System.out.printf("boolean[]: %.2f ms (소수 개수: %d)\n", timeBool / 1e6, countBool);
            System.out.printf("BitSet:    %.2f ms (소수 개수: %d)\n", timeBit / 1e6, countBit);
            System.out.printf("속도 비율: %.2f배\n", (double) timeBool / timeBit);
            System.out.printf("메모리: boolean[] ~%.2f MB vs BitSet ~%.2f MB\n", 
                SIZE / 1e6, SIZE / 8.0 / 1e6);
        }

        private static void sieveBooleanArray(boolean[] isPrime) {
            isPrime[0] = isPrime[1] = false;
            for (int i = 2; i * i < SIZE; i++) {
                if (isPrime[i]) {
                    for (int j = i * i; j < SIZE; j += i) {
                        isPrime[j] = false;
                    }
                }
            }
        }

        private static void sieveBitSet(BitSet bitSet) {
            bitSet.clear(0);
            bitSet.clear(1);
            for (int i = 2; i * i < SIZE; i++) {
                if (bitSet.get(i)) {
                    for (int j = i * i; j < SIZE; j += i) {
                        bitSet.clear(j);
                    }
                }
            }
        }
    }

    /**
     * 비트마스크 DP 최적화 예제
     * BitSet으로 상태 집합을 효율적으로 표현
     */
    static class BitMaskDPExample {
        public static void analyze() {
            System.out.println("\n=== 비트마스크 DP 최적화 예제 ===");
            System.out.println("문제: n=20인 부분집합 문제 (2^20 = 1,048,576 상태)");
            System.out.println();
            System.out.println("방법 1: boolean[] 사용");
            System.out.printf("  메모리: 2^20 * 1바이트 = ~1MB\n");
            System.out.println("  접근: arr[mask] = true");
            System.out.println();
            System.out.println("방법 2: BitSet 사용");
            System.out.printf("  메모리: 2^20 / 8 = ~128KB\n");
            System.out.println("  접근: bitset.get(mask)");
            System.out.println("  장점: 8배 메모리 절감, 집합 연산 최적화");
            System.out.println();
            System.out.println("방법 3: long[] 비트마스크 (가장 빠름, 64bit)");
            System.out.printf("  메모리: 2^20 / 64 = ~16KB\n");
            System.out.println("  접근: (arr[mask / 64] & (1L << (mask % 64))) != 0");
            System.out.println("  장점: 가장 빠르지만 수동 비트 연산");
        }
    }

    public static void runAllBenchmarks() {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║    BitSet 벤치마크 - 부울값 대량 처리              ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println("테스트 크기: " + SIZE + " bits\n");

        BooleanArrayBenchmark.run();
        BitSetBenchmarkInner.run();
        SetOperationsBenchmark.run();
        SieveOfEratosthenesBenchmark.run();
        BitMaskDPExample.analyze();

        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("결론:");
        System.out.println("✓ 대량 부울값: BitSet이 8배 메모리 효율, 비슷한 성능");
        System.out.println("✓ 집합 연산: BitSet의 or/and/xor 최적화");
        System.out.println("✓ 최고 성능: long[] 비트마스크 (수동 최적화)");
        System.out.println("✓ 프로그래밍 편의: BitSet API 사용 권장");
        System.out.println("═══════════════════════════════════════════════════\n");
    }
}
