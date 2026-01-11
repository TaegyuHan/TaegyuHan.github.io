package com.taecobug.datastructures.array;

/**
 * 배열 성능 벤치마크 기반 클래스
 * - 원시 배열 vs 객체 배열의 메모리 레이아웃 및 캐시 성능 차이
 * - 순차 접근 vs 랜덤 접근 성능 비교
 */
public class ArrayPerformance {

    private static final int ARRAY_SIZE = 1_000_000;
    private static final int WARMUP_ITERATIONS = 10;
    private static final int MEASUREMENT_ITERATIONS = 10;

    /**
     * 원시 타입 배열: int[] - 값이 연속으로 저장됨
     */
    static class PrimitiveArrayBenchmark {
        public static void benchmark() {
            System.out.println("\n=== 원시 타입 배열 벤치마크: int[] ===");
            
            int[] arr = new int[ARRAY_SIZE];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = i % 256;  // 0-255 범위의 값
            }

            // 워밍업
            for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                long sum = 0;
                for (int i = 0; i < arr.length; i++) {
                    sum += arr[i];
                }
            }

            // 순차 접근 성능 측정
            long[] times = new long[MEASUREMENT_ITERATIONS];
            for (int m = 0; m < MEASUREMENT_ITERATIONS; m++) {
                long sum = 0;
                long start = System.nanoTime();
                for (int i = 0; i < arr.length; i++) {
                    sum += arr[i];
                }
                long elapsed = System.nanoTime() - start;
                times[m] = elapsed;
                // 컴파일러 최적화 방지
                if (sum == Long.MIN_VALUE) System.out.println("unreachable");
            }

            long avgTime = getAverageTime(times);
            System.out.printf("순차 접근 (sequential): %.2f ms (%.2f ns/elem)\n", 
                avgTime / 1e6, (double) avgTime / ARRAY_SIZE);
        }
    }

    /**
     * 객체 배열: Item[] - 참조만 연속, 실제 객체는 분산
     */
    static class Item {
        public int id;
        public double price;
        public long timestamp;

        public Item(int id, double price, long timestamp) {
            this.id = id;
            this.price = price;
            this.timestamp = timestamp;
        }
    }

    static class ObjectArrayBenchmark {
        public static void benchmark() {
            System.out.println("\n=== 객체 배열 벤치마크: Item[] (AoS) ===");
            
            Item[] items = new Item[ARRAY_SIZE];
            for (int i = 0; i < items.length; i++) {
                items[i] = new Item(i, (i % 256) * 1.5, System.nanoTime());
            }

            // 워밍업
            for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                double sum = 0;
                for (int i = 0; i < items.length; i++) {
                    sum += items[i].price;
                }
            }

            // 순차 접근 성능 측정
            long[] times = new long[MEASUREMENT_ITERATIONS];
            for (int m = 0; m < MEASUREMENT_ITERATIONS; m++) {
                double sum = 0;
                long start = System.nanoTime();
                for (int i = 0; i < items.length; i++) {
                    sum += items[i].price;  // 객체 참조 + 필드 접근
                }
                long elapsed = System.nanoTime() - start;
                times[m] = elapsed;
                if (sum == Double.NEGATIVE_INFINITY) System.out.println("unreachable");
            }

            long avgTime = getAverageTime(times);
            System.out.printf("순차 접근 (sequential): %.2f ms (%.2f ns/elem)\n", 
                avgTime / 1e6, (double) avgTime / ARRAY_SIZE);
        }
    }

    /**
     * SoA (Struct of Arrays): 필드별로 원시 배열 분리
     * - 각 필드가 연속 메모리에 저장
     * - 컬럼형 처리에 최적화
     */
    static class StructOfArrays {
        public int[] ids;
        public double[] prices;
        public long[] timestamps;

        public StructOfArrays(int size) {
            ids = new int[size];
            prices = new double[size];
            timestamps = new long[size];
        }

        public void fill(int size) {
            for (int i = 0; i < size; i++) {
                ids[i] = i;
                prices[i] = (i % 256) * 1.5;
                timestamps[i] = System.nanoTime();
            }
        }
    }

    static class StructOfArraysBenchmark {
        public static void benchmark() {
            System.out.println("\n=== SoA 벤치마크: 필드별 원시 배열 분리 ===");
            
            StructOfArrays soa = new StructOfArrays(ARRAY_SIZE);
            soa.fill(ARRAY_SIZE);

            // 워밍업
            for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                double sum = 0;
                for (int i = 0; i < ARRAY_SIZE; i++) {
                    sum += soa.prices[i];
                }
            }

            // 순차 접근 성능 측정
            long[] times = new long[MEASUREMENT_ITERATIONS];
            for (int m = 0; m < MEASUREMENT_ITERATIONS; m++) {
                double sum = 0;
                long start = System.nanoTime();
                for (int i = 0; i < ARRAY_SIZE; i++) {
                    sum += soa.prices[i];  // 연속 배열 직접 접근
                }
                long elapsed = System.nanoTime() - start;
                times[m] = elapsed;
                if (sum == Double.NEGATIVE_INFINITY) System.out.println("unreachable");
            }

            long avgTime = getAverageTime(times);
            System.out.printf("순차 접근 (sequential): %.2f ms (%.2f ns/elem)\n", 
                avgTime / 1e6, (double) avgTime / ARRAY_SIZE);
        }
    }

    /**
     * 랜덤 접근 성능: 캐시 미스 증가
     */
    static class RandomAccessBenchmark {
        public static void benchmark() {
            System.out.println("\n=== 랜덤 접근 벤치마크 ===");
            
            // 실제로는 접근 패턴을 무작위로 생성하는 것이 이상적이지만,
            // 컴파일러 최적화를 피하기 위해 특정 패턴 사용
            int[] arr = new int[ARRAY_SIZE];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = i % 256;
            }

            // 스트라이드 패턴: 캐시 라인 크기(64바이트) 무시
            int stride = 256;  // int는 4바이트, stride=256 -> 1KB 점프
            
            // 워밍업
            for (int w = 0; w < WARMUP_ITERATIONS; w++) {
                long sum = 0;
                for (int i = 0; i < arr.length; i += stride) {
                    sum += arr[i];
                }
            }

            // 스트라이드 접근 성능 측정
            long[] times = new long[MEASUREMENT_ITERATIONS];
            for (int m = 0; m < MEASUREMENT_ITERATIONS; m++) {
                long sum = 0;
                long start = System.nanoTime();
                for (int i = 0; i < arr.length; i += stride) {
                    sum += arr[i];  // 캐시 라인을 건너뜀 -> 캐시 미스
                }
                long elapsed = System.nanoTime() - start;
                times[m] = elapsed;
                if (sum == Long.MIN_VALUE) System.out.println("unreachable");
            }

            long avgTime = getAverageTime(times);
            System.out.printf("스트라이드 접근 (stride=%d): %.2f ms\n", 
                stride, avgTime / 1e6);
        }
    }

    private static long getAverageTime(long[] times) {
        long sum = 0;
        for (long t : times) {
            sum += t;
        }
        return sum / times.length;
    }

    public static void runAllBenchmarks() {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║      배열 성능 벤치마크 - 메모리 레이아웃 분석     ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println("테스트 크기: " + ARRAY_SIZE + " elements");

        PrimitiveArrayBenchmark.benchmark();
        ObjectArrayBenchmark.benchmark();
        StructOfArraysBenchmark.benchmark();
        RandomAccessBenchmark.benchmark();

        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("결론:");
        System.out.println("1. 원시 배열 < SoA < 객체 배열 순서로 성능");
        System.out.println("2. 객체 배열은 간접 참조로 캐시 미스 증가");
        System.out.println("3. SoA는 컬럼형 처리에서 큰 성능 이점");
        System.out.println("═══════════════════════════════════════════════════\n");
    }
}
