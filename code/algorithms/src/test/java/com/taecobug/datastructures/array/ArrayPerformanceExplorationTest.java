package com.taecobug.datastructures.array;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * 배열 성능 탐구 - 통합 테스트
 * 문서의 모든 벤치마크를 실행하고 검증
 */
@DisplayName("배열 성능 탐구 벤치마크")
public class ArrayPerformanceExplorationTest {

    @Test
    @DisplayName("1. 원시 배열 vs 객체 배열 메모리 레이아웃 및 캐시 성능")
    void testArrayMemoryLayout() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("테스트 1: 배열 메모리 레이아웃 및 캐시 성능");
        System.out.println("=".repeat(70));
        ArrayPerformance.runAllBenchmarks();
//      ======================================================================
//      테스트 1: 배열 메모리 레이아웃 및 캐시 성능
//      ======================================================================
//      ╔═══════════════════════════════════════════════════╗
//      ║      배열 성능 벤치마크 - 메모리 레이아웃 분석           ║
//      ╚═══════════════════════════════════════════════════╝
//            테스트 크기: 1000000 elements
//
//              === 원시 타입 배열 벤치마크: int[] ===
//            순차 접근 (sequential): 0.24 ms (0.24 ns/elem)
//
//              === 객체 배열 벤치마크: Item[] (AoS) ===
//              순차 접근 (sequential): 1.67 ms (1.67 ns/elem)
//
//              === SoA 벤치마크: 필드별 원시 배열 분리 ===
//            순차 접근 (sequential): 1.26 ms (1.26 ns/elem)
//
//              === 랜덤 접근 벤치마크 ===
//            스트라이드 접근 (stride=256): 0.02 ms
//
//      ═══════════════════════════════════════════════════
//            결론:
//            1. 원시 배열 < SoA < 객체 배열 순서로 성능
//            2. 객체 배열은 간접 참조로 캐시 미스 증가
//            3. SoA는 컬럼형 처리에서 큰 성능 이점
//      ═══════════════════════════════════════════════════
    }

    @Test
    @DisplayName("2. ArrayList 리사이징 성능")
    void testArrayListResizing() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("테스트 2: ArrayList 리사이징 성능");
        System.out.println("=".repeat(70));
        ArrayListResizingBenchmark.runAllBenchmarks();
//      ======================================================================
//      테스트 2: ArrayList 리사이징 성능
//      ======================================================================
//      ╔═══════════════════════════════════════════════════╗
//      ║      ArrayList 리사이징 성능 벤치마크                 ║
//      ╚═══════════════════════════════════════════════════╝
//            테스트 크기: 1000000 elements
//
//            1️⃣  용량 미리 할당 없음 (ArrayList<>())
//            평균 시간: 46.77 ms
//
//            2️⃣  ensureCapacity 사용
//            평균 시간: 18.71 ms
//
//            3️⃣  생성자로 용량 설정 (ArrayList<>(ELEMENTS))
//            평균 시간: 11.46 ms
//
//      ═══════════════════════════════════════════════════
//            성능 비교:
//            미할당 vs ensureCapacity: 2.50배
//            미할당 vs 생성자: 4.08배
//      ═══════════════════════════════════════════════════
    }

    @Test
    @DisplayName("3. ArrayList vs LinkedList vs ArrayDeque 성능 비교")
    void testListImplementations() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("테스트 3: List 구현 성능 비교");
        System.out.println("=".repeat(70));
        ListPerformanceBenchmark.runAllBenchmarks();
//      ======================================================================
//            테스트 3: List 구현 성능 비교
//      ======================================================================
//      ╔════════════════════════════════════════════════════════╗
//      ║   List 구현 성능 비교: ArrayList/LinkedList/ArrayDeque   ║
//      ╚════════════════════════════════════════════════════════╝
//      기본 크기: 100000 elements
//      반복 연산: 10000 operations
//
//
//      === Head 삽입 (index 0에 삽입) ===
//      - LinkedList: O(1) 포인터 조작
//      - ArrayList: O(n) 배열 시프팅
//      - ArrayDeque: O(1) 원형 버퍼
//
//      ArrayList:  157.52 ms
//      LinkedList: 1.78 ms (0.01배)
//      ArrayDeque: 3.54 ms (0.02배)
//
//      === 양끝 연산 (Queue 패턴) ===
//      - ArrayDeque: O(1) 원형 버퍼 (최적화)
//      - LinkedList: O(1) 포인터 조작 (메모리 간접 참조 비용)
//      - ArrayList: 지원 안함
//
//      ArrayDeque: 2.62 ms
//      LinkedList: 5.58 ms (2.13배)
//
//      === 랜덤 접근 (get by index) ===
//      - ArrayList: O(1) 인덱스 직접 접근
//      - LinkedList: O(n) 선형 탐색 (포인터 따라가기)
//
//      ArrayList:  1.21 ms
//      LinkedList: 612.64 ms (505.02배)
//
//      === 중간 삽입 (random position) ===
//      - ArrayList: O(n) 배열 시프팅 (상수 계수 작음)
//      - LinkedList: O(n) 노드 탐색 + O(1) 포인터 조작
//
//      ArrayList:  96.34 ms
//      LinkedList: 1145.14 ms (11.89배)
//
//      === 순회 반복 (iteration) ===
//      - ArrayList: 캐시 친화적 (연속 메모리)
//      - LinkedList: 캐시 미스 (포인터 따라가기)
//
//      ArrayList:  5.40 ms
//      LinkedList: 29.85 ms (5.53배)
//      ═══════════════════════════════════════════════════
//            결론:
//      ✓ 랜덤 접근 주도: ArrayList
//      ✓ 양끝 큐/덱 연산: ArrayDeque (LinkedList보다 실제 성능 우수)
//      ✓ 첫 위치만 지속 삽입: LinkedList (but 임의 위치가 섞이면 비추천)
//      ✓ 중간 삽입/삭제가 많으면: 알고리즘 재설계 필요
//      ═══════════════════════════════════════════════════
    }

    @Test
    @DisplayName("4. BitSet vs boolean[] 성능 및 메모리 효율성")
    void testBitSetPerformance() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("테스트 4: BitSet 성능");
        System.out.println("=".repeat(70));
        BitSetBenchmark.runAllBenchmarks();
//      ======================================================================
//            테스트 4: BitSet 성능
//      ======================================================================
//      ╔═══════════════════════════════════════════════════╗
//      ║    BitSet 벤치마크 - 부울값 대량 처리                 ║
//      ╚═══════════════════════════════════════════════════╝
//      테스트 크기: 10000000 bits
//
//
//      === boolean[] 벤치마크 ===
//      설정: 26.47 ms
//      카운팅: 22.07 ms (true 개수: 5000000)
//      토글: 24.56 ms
//      메모리 사용: ~10.00 MB (실제로는 패딩으로 더 많음)
//
//      === BitSet 벤치마크 ===
//      설정: 35.54 ms
//      카운팅: 7.45 ms (true 개수: 5000000)
//      토글: 4.80 ms
//      메모리 사용: ~1.25 MB (8배 효율)
//
//      === 집합 연산 성능 (BitSet 최적화) ===
//      합집합 (OR): 4.88 ms (결과: 4666667)
//      교집합 (AND): 10.85 ms (결과: 666667)
//      차집합 (AND NOT): 6.20 ms (결과: 2666667)
//
//      === 에라토스테네스의 체: BitSet vs boolean[] ===
//      boolean[]: 75.99 ms (소수 개수: 664579)
//      BitSet:    88.98 ms (소수 개수: 664579)
//      속도 비율: 0.85배
//      메모리: boolean[] ~10.00 MB vs BitSet ~1.25 MB
//
//      === 비트마스크 DP 최적화 예제 ===
//      문제: n=20인 부분집합 문제 (2^20 = 1,048,576 상태)
//
//      방법 1: boolean[] 사용
//      메모리: 2^20 * 1바이트 = ~1MB
//      접근: arr[mask] = true
//
//      방법 2: BitSet 사용
//      메모리: 2^20 / 8 = ~128KB
//      접근: bitset.get(mask)
//      장점: 8배 메모리 절감, 집합 연산 최적화
//
//      방법 3: long[] 비트마스크 (가장 빠름, 64bit)
//      메모리: 2^20 / 64 = ~16KB
//      접근: (arr[mask / 64] & (1L << (mask % 64))) != 0
//      장점: 가장 빠르지만 수동 비트 연산
//
//      ═══════════════════════════════════════════════════
//      결론:
//      ✓ 대량 부울값: BitSet이 8배 메모리 효율, 비슷한 성능
//      ✓ 집합 연산: BitSet의 or/and/xor 최적화
//      ✓ 최고 성능: long[] 비트마스크 (수동 최적화)
//      ✓ 프로그래밍 편의: BitSet API 사용 권장
//      ═══════════════════════════════════════════════════
    }
}
