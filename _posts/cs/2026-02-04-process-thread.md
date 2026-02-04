---
title: "[Computer Science] 프로세스와 스레드: 기초부터 동기화까지 완벽 정리"

tagline: "멀티스레드 프로그래밍의 핵심 개념과 실무 적용 방법을 알아봅니다"

header:
  overlay_image: /assets/post/cs/2026-02-04-process-thread/overlay.png
  overlay_filter: 0.5

categories:
  - Computer Science

tags:
  - 프로세스
  - 스레드
  - 멀티스레드
  - 동시성
  - 동기화
  - Synchronized
  - ReentrantLock
  - Computer Science

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-02-04T23:34:00
---

멀티코어 시대, 성능 최적화를 위해서는 프로세스와 스레드를 제대로 이해하는 것이 필수입니다. 이 글에서는 기초 개념부터 실무 적용까지, 실제 사례와 함께 자세히 알아보겠습니다.

## 프로세스와 스레드, 무엇이 다를까?

### 프로세스(Process)란?

프로세스는 **실행 중인 프로그램**을 의미합니다. 더 정확히는 **독립적인 메모리 공간을 가진 실행 단위**입니다.

**프로세스의 특징:**
- 독립된 메모리 공간 보유 (Code, Data, Heap, Stack)
- 자체 Process Control Block (PCB) 소유
- 다른 프로세스와 메모리 격리
- 생성/종료 비용이 높음
- 프로세스 간 통신(IPC)이 복잡

### 스레드(Thread)란?

스레드는 **프로세스 내의 경량 실행 단위**입니다. 같은 프로세스 내 스레드들은 메모리를 공유합니다.

**스레드의 특징:**
- 프로세스의 메모리 공유 (Code, Data, Heap 영역)
- 각자의 Stack과 Program Counter 보유
- 자체 Thread Control Block (TCB) 소유
- 생성/종료 비용이 낮음
- 스레드 간 통신이 간단 (메모리 공유)

<div class="mermaid mermaid-center">
graph TB
    subgraph Process["프로세스 메모리 구조"]
        Code["Code 영역<br/>(공유)"]
        Data["Data 영역<br/>(공유)"]
        Heap["Heap 영역<br/>(공유)"]
        Stack1["Stack 1<br/>(Thread 1 전용)"]
        Stack2["Stack 2<br/>(Thread 2 전용)"]
        Stack3["Stack 3<br/>(Thread 3 전용)"]
    end
    
    Thread1["Thread 1<br/>PC: 0x1234"]
    Thread2["Thread 2<br/>PC: 0x5678"]
    Thread3["Thread 3<br/>PC: 0x9ABC"]
    
    Thread1 -.-> Stack1
    Thread2 -.-> Stack2
    Thread3 -.-> Stack3
    
    Thread1 -.-> Code
    Thread2 -.-> Code
    Thread3 -.-> Code
    
    style Code fill:#1a1a2e,stroke:#16213e,stroke-width:2px,color:#eee
    style Data fill:#1a1a2e,stroke:#16213e,stroke-width:2px,color:#eee
    style Heap fill:#1a1a2e,stroke:#16213e,stroke-width:2px,color:#eee
    style Stack1 fill:#0f3460,stroke:#16213e,stroke-width:2px,color:#eee
    style Stack2 fill:#0f3460,stroke:#16213e,stroke-width:2px,color:#eee
    style Stack3 fill:#0f3460,stroke:#16213e,stroke-width:2px,color:#eee
    style Thread1 fill:#533483,stroke:#16213e,stroke-width:2px,color:#eee
    style Thread2 fill:#533483,stroke:#16213e,stroke-width:2px,color:#eee
    style Thread3 fill:#533483,stroke:#16213e,stroke-width:2px,color:#eee
    style Process fill:#0a0a0a,stroke:#16213e,stroke-width:3px,color:#eee
</div>

### 핵심 차이점 비교

| 구분 | 프로세스 | 스레드 |
|------|---------|--------|
| **메모리** | 독립적 | 공유 (Stack만 독립) |
| **통신** | IPC 필요 (복잡) | 메모리 공유 (간단) |
| **컨텍스트 스위칭** | 느림 (높은 비용) | 빠름 (낮은 비용) |
| **안정성** | 높음 (격리됨) | 낮음 (동기화 필요) |
| **생성 비용** | 높음 | 낮음 |

## 생성부터 종료까지: 라이프사이클

### 스레드 라이프사이클

<div class="mermaid mermaid-center">
stateDiagram-v2
    [*] --> New: Thread 객체 생성
    New --> Runnable: start() 호출
    Runnable --> Running: 스케줄러 선택
    Running --> Runnable: yield() 또는<br/>우선순위 낮아짐
    Running --> Blocked: I/O 대기<br/>Lock 대기
    Blocked --> Runnable: I/O 완료<br/>Lock 획득
    Running --> Terminated: 실행 완료<br/>예외 발생
    Terminated --> [*]
    
    style New fill:#1a1a2e,stroke:#16213e,stroke-width:2px,color:#eee
    style Runnable fill:#0f3460,stroke:#16213e,stroke-width:2px,color:#eee
    style Running fill:#533483,stroke:#16213e,stroke-width:2px,color:#eee
    style Blocked fill:#8b0000,stroke:#16213e,stroke-width:2px,color:#eee
    style Terminated fill:#2d4a2b,stroke:#16213e,stroke-width:2px,color:#eee
</div>

**Java 스레드 생성 예제:**

```java
// 1. Thread 클래스 상속
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread running: " + Thread.currentThread().getName());
    }
}

// 2. Runnable 인터페이스 구현 (권장)
class MyTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Task running: " + Thread.currentThread().getName());
    }
}

// 사용
MyThread thread1 = new MyThread();
thread1.start();  // run()이 아닌 start() 호출!

Thread thread2 = new Thread(new MyTask());
thread2.start();

// 스레드 종료 대기
thread1.join();
thread2.join();
```

**참고:** [GeeksforGeeks - Java Thread Creation](https://www.geeksforgeeks.org/java/java-multithreading-tutorial)

⚠️ **주의사항:**
- `run()` 직접 호출: 새 스레드 생성 안 됨 (일반 메서드 호출)
- `start()` 호출: JVM이 새 스레드 생성 + `run()` 실행
- 스레드는 한 번 종료되면 재시작 불가능

**Python 스레드 생성 예제:**

```python
import threading
import time

def task():
    print(f"Thread running: {threading.current_thread().name}")
    time.sleep(1)
    print("Task completed")

# 스레드 생성 및 시작
thread = threading.Thread(target=task)
thread.start()

# 스레드 종료 대기
thread.join()
print("Main thread continues")
```

### 프로세스 라이프사이클

프로세스도 스레드와 유사한 라이프사이클을 가지지만, **메인 스레드를 포함한 모든 non-daemon 스레드가 종료**되어야 프로세스가 종료됩니다.

```python
from multiprocessing import Process
import time

def worker():
    print("Worker process started")
    time.sleep(1)
    print("Worker process completed")

# 프로세스 생성 및 시작
process = Process(target=worker)
process.start()

# 프로세스 종료 대기
process.join()
print(f"Process exit code: {process.exitcode}")
```

## 컨텍스트 스위칭: 왜 비용이 발생할까?

**컨텍스트 스위칭(Context Switching)**은 CPU가 현재 실행 중인 스레드의 상태를 저장하고, 다른 스레드의 상태를 불러와 실행하는 과정입니다.

### 컨텍스트 스위칭 발생 원인

<div class="mermaid mermaid-center">
graph TD
    A["CPU 실행 중<br/>Thread A"] --> B{컨텍스트 스위칭<br/>발생 조건}
    
    B -->|"1. 자발적"| C["I/O 대기<br/>Lock 대기<br/>sleep() 호출"]
    B -->|"2. 비자발적"| D["높은 우선순위<br/>스레드 깨어남"]
    B -->|"3. 시간 소진"| E["타임 슬라이스<br/>소진 (10ms)"]
    
    C --> F["Thread A 상태 저장<br/>(레지스터, PC 등)"]
    D --> F
    E --> F
    
    F --> G["Thread B 상태 복원"]
    G --> H["CPU 실행 중<br/>Thread B"]
    
    style A fill:#533483,stroke:#16213e,stroke-width:2px,color:#eee
    style B fill:#0f3460,stroke:#16213e,stroke-width:2px,color:#eee
    style C fill:#1a1a2e,stroke:#16213e,stroke-width:2px,color:#eee
    style D fill:#1a1a2e,stroke:#16213e,stroke-width:2px,color:#eee
    style E fill:#1a1a2e,stroke:#16213e,stroke-width:2px,color:#eee
    style F fill:#8b0000,stroke:#16213e,stroke-width:2px,color:#eee
    style G fill:#8b0000,stroke:#16213e,stroke-width:2px,color:#eee
    style H fill:#533483,stroke:#16213e,stroke-width:2px,color:#eee
</div>

### 프로세스 vs 스레드 컨텍스트 스위칭 비용

**프로세스 컨텍스트 스위칭이 더 비싼 이유:**

1. **메모리 공간 전환**
   - 가상 메모리 주소 공간 변경
   - TLB(Translation Lookaside Buffer) 플러시
   - 캐시 무효화

2. **더 많은 상태 정보**
   - PCB(Process Control Block) 전체 저장/복원
   - 파일 디스크립터, 시그널 핸들러 등

3. **커널 개입**
   - 프로세스 간 전환은 커널 모드 진입 필요

**스레드 컨텍스트 스위칭:**
- 같은 메모리 공간 사용 (주소 공간 유지)
- TCB(Thread Control Block)만 저장/복원
- 레지스터, 스택 포인터, PC만 변경

## 실무 적용: 언제 무엇을 사용할까?

### Python의 GIL과 선택 전략

Python의 **GIL(Global Interpreter Lock)**은 한 번에 하나의 스레드만 Python 바이트코드를 실행할 수 있게 제한합니다.

<div class="mermaid mermaid-center">
graph LR
    subgraph "CPU 집약 작업"
        CPU1["Thread 1"] -.->|GIL 경쟁| GIL1[GIL]
        CPU2["Thread 2"] -.->|대기| GIL1
        CPU3["Thread 3"] -.->|대기| GIL1
        GIL1 -.->|병목| Result1["느린 실행"]
    end
    
    subgraph "I/O 집약 작업"
        IO1["Thread 1"] -->|I/O 시 GIL 해제| Wait1[I/O 대기]
        IO2["Thread 2"] -->|실행 가능| GIL2[GIL]
        IO3["Thread 3"] -->|실행 가능| GIL2
        GIL2 -->|효율적| Result2["빠른 실행"]
    end
    
    style CPU1 fill:#8b0000,stroke:#16213e,stroke-width:2px,color:#eee
    style CPU2 fill:#4d0000,stroke:#16213e,stroke-width:2px,color:#eee
    style CPU3 fill:#4d0000,stroke:#16213e,stroke-width:2px,color:#eee
    style GIL1 fill:#1a1a2e,stroke:#16213e,stroke-width:2px,color:#eee
    style Result1 fill:#8b0000,stroke:#16213e,stroke-width:2px,color:#eee
    
    style IO1 fill:#0f3460,stroke:#16213e,stroke-width:2px,color:#eee
    style IO2 fill:#0f3460,stroke:#16213e,stroke-width:2px,color:#eee
    style IO3 fill:#0f3460,stroke:#16213e,stroke-width:2px,color:#eee
    style Wait1 fill:#1a1a2e,stroke:#16213e,stroke-width:2px,color:#eee
    style GIL2 fill:#1a1a2e,stroke:#16213e,stroke-width:2px,color:#eee
    style Result2 fill:#2d4a2b,stroke:#16213e,stroke-width:2px,color:#eee
</div>

**선택 기준:**

| 작업 유형 | 선택 | 이유 |
|----------|------|------|
| **CPU 집약적** | 멀티프로세스 | GIL 우회, 각 CPU 코어 활용 |
| **I/O 집약적** | 멀티스레드 | I/O 대기 시 GIL 해제, 낮은 생성 비용 |
| **혼합형** | 프로세스 + 스레드 | 프로세스 풀 + 각 프로세스 내 스레드 풀 |

**실전 예제:**

```python
import threading
import multiprocessing
import time

# CPU 집약적 작업 - 프로세스 사용
def cpu_bound_task(n):
    count = 0
    for i in range(n):
        count += i * i
    return count

# I/O 집약적 작업 - 스레드 사용
def io_bound_task(url):
    import requests
    response = requests.get(url)
    return len(response.content)

# CPU 작업: 멀티프로세스
if __name__ == '__main__':
    with multiprocessing.Pool(processes=4) as pool:
        results = pool.map(cpu_bound_task, [10000000] * 4)
    
    # I/O 작업: 멀티스레드
    urls = ['https://example.com'] * 10
    threads = []
    for url in urls:
        t = threading.Thread(target=io_bound_task, args=(url,))
        threads.append(t)
        t.start()
    
    for t in threads:
        t.join()
```

### 웹 서버의 멀티스레드 전략

웹 서버는 대부분 **I/O 집약적 작업**(DB 쿼리, 네트워크 통신)을 처리하므로 멀티스레드 방식을 선호합니다.

**Tomcat의 스레드 풀 방식:**

```java
// Tomcat 설정 예시 (server.xml)
<Connector port="8080" 
           maxThreads="200"        // 최대 스레드 수
           minSpareThreads="10"    // 최소 유휴 스레드 수
           maxConnections="10000"  // 최대 동시 연결 수
           acceptCount="100"/>     // 대기 큐 크기
```

**장점:**
- 요청마다 스레드 할당 → 동시 처리 가능
- 스레드 풀로 생성/종료 비용 최소화
- 메모리 공유로 세션 관리 용이

**Node.js의 이벤트 루프 방식:**

Node.js는 단일 스레드 + 비동기 I/O 모델을 사용합니다.

```javascript
// 비동기 I/O - 스레드 블로킹 없음
const fs = require('fs');

fs.readFile('large-file.txt', (err, data) => {
    if (err) throw err;
    console.log('File read completed');
});

// I/O 대기 중에도 다른 요청 처리 가능
console.log('This runs immediately');
```

**비교:**

| 방식 | Tomcat (멀티스레드) | Node.js (이벤트 루프) |
|------|-------------------|---------------------|
| **동시성** | 스레드 기반 | 비동기 I/O 기반 |
| **확장성** | 스레드 수 제한 | 단일 스레드, 높은 동시성 |
| **CPU 작업** | 가능 (별도 스레드) | 블로킹 (Worker 필요) |
| **메모리** | 높음 (스레드당 Stack) | 낮음 (단일 스레드) |

### 메모리 공유의 양날의 검

**장점:**
- ✅ 빠른 데이터 접근 (같은 메모리 공간)
- ✅ 스레드 간 통신 비용 최소화
- ✅ 캐시 효율성 증가

**단점:**
- ❌ Race Condition 위험
- ❌ 동기화 오버헤드
- ❌ 디버깅 어려움

**베스트 프랙티스:**

```java
// 1. 불변 객체 사용 (공유해도 안전)
public final class ImmutableConfig {
    private final String value;
    
    public ImmutableConfig(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;  // 변경 불가능
    }
}

// 2. 스레드 로컬 변수 사용
public class ThreadLocalExample {
    private static ThreadLocal<Integer> threadLocal = 
        ThreadLocal.withInitial(() -> 0);
    
    public void increment() {
        threadLocal.set(threadLocal.get() + 1);  // 스레드별 독립 값
    }
}

// 3. 결과만 공유 (작업 분리)
ExecutorService executor = Executors.newFixedThreadPool(4);
List<Future<Integer>> futures = new ArrayList<>();

// 각 스레드가 독립적으로 작업
for (int i = 0; i < 10; i++) {
    final int value = i;
    futures.add(executor.submit(() -> value * value));
}

// 결과만 수집
for (Future<Integer> future : futures) {
    int result = future.get();  // 안전하게 결과 수집
}
```

## 동기화 메커니즘: Monitor와 Lock

### Monitor: Java의 내장 동기화

Java의 **모든 객체는 Monitor**(모니터)를 가지고 있습니다. 이는 JVM이 자동으로 제공하는 **intrinsic lock**(내재 락)입니다.

**Monitor 동작 원리:**

<div class="mermaid mermaid-center">
sequenceDiagram
    participant T1 as Thread 1
    participant M as Monitor (객체)
    participant T2 as Thread 2
    
    T1->>M: synchronized 메서드 호출
    M->>M: 모니터 획득
    Note over M: Thread 1이 모니터 소유
    
    T2->>M: synchronized 메서드 호출
    M-->>T2: 대기 (BLOCKED)
    
    T1->>T1: 메서드 실행
    T1->>M: 메서드 종료
    M->>M: 모니터 자동 반납
    
    M->>T2: 모니터 획득
    Note over M: Thread 2가 모니터 소유
    T2->>T2: 메서드 실행
    T2->>M: 메서드 종료
    M->>M: 모니터 반납
</div>

**참고:** [GeeksforGeeks - Monitor in Java](https://www.geeksforgeeks.org/java/difference-between-lock-and-monitor-in-java-concurrency)

**Synchronized 키워드 사용:**

```java
class BankAccount {
    private int balance = 1000;
    
    // 메서드 레벨 동기화
    public synchronized void withdraw(int amount) {
        if (balance >= amount) {
            balance -= amount;
            System.out.println("출금: " + amount + ", 잔액: " + balance);
        }
    }
    
    // 블록 레벨 동기화 (더 세밀한 제어)
    public void deposit(int amount) {
        synchronized(this) {  // this 객체의 모니터 사용
            balance += amount;
            System.out.println("입금: " + amount + ", 잔액: " + balance);
        }
    }
}

// 사용 예
BankAccount account = new BankAccount();

Thread t1 = new Thread(() -> account.withdraw(100));
Thread t2 = new Thread(() -> account.deposit(200));

t1.start();
t2.start();
```

**핵심 개념:**
- ✅ JVM이 자동으로 모니터 획득/반납 관리
- ✅ 예외 발생 시에도 자동 반납 (안전)
- ✅ 같은 스레드는 재진입 가능 (Reentrant)
- ❌ 타임아웃 설정 불가
- ❌ 공정성(Fairness) 보장 안 됨

### ReentrantLock: 명시적 동기화

**ReentrantLock**은 `synchronized`보다 더 유연한 기능을 제공합니다.

```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

class Counter {
    private int count = 0;
    private final Lock lock = new ReentrantLock();  // Fair Lock도 가능
    
    public void increment() {
        lock.lock();  // 명시적 획득
        try {
            count++;
        } finally {
            lock.unlock();  // 반드시 finally에서 해제!
        }
    }
    
    // 타임아웃과 함께 시도
    public boolean tryIncrementWithTimeout() {
        try {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {  // 1초 대기
                try {
                    count++;
                    return true;
                } finally {
                    lock.unlock();
                }
            }
            return false;  // 획득 실패
        } catch (InterruptedException e) {
            return false;
        }
    }
    
    public int getCount() {
        return count;
    }
}
```

**Synchronized vs ReentrantLock:**

| 기능 | Synchronized | ReentrantLock |
|------|-------------|---------------|
| **사용 편의성** | 간단 (자동 관리) | 복잡 (수동 관리) |
| **타임아웃** | 불가능 | `tryLock(timeout)` |
| **인터럽트** | 불가능 | `lockInterruptibly()` |
| **공정성** | 불공정 | 공정/불공정 선택 가능 |
| **조건 변수** | `wait()/notify()` | `Condition` 객체 |
| **성능** | 약간 빠름 | 약간 느림 |

### Python의 Lock

```python
import threading

counter = 0
lock = threading.Lock()

def increment():
    global counter
    
    # 방법 1: 수동 acquire/release
    lock.acquire()
    try:
        counter += 1
    finally:
        lock.release()
    
    # 방법 2: Context Manager (권장)
    with lock:
        counter += 1  # 자동으로 acquire/release

threads = [threading.Thread(target=increment) for _ in range(100)]
for t in threads:
    t.start()
for t in threads:
    t.join()

print(f"Counter: {counter}")  # 200 (안전하게 증가)
```

## 위험한 함정: Deadlock

**Deadlock(교착 상태)**는 두 개 이상의 스레드가 서로가 가진 자원을 기다리며 무한 대기하는 상황입니다.

### Deadlock 발생 예제

```java
class Resource {
    private final String name;
    
    public Resource(String name) {
        this.name = name;
    }
    
    public synchronized void useWith(Resource other) {
        System.out.println(Thread.currentThread().getName() + 
                          ": " + name + " 획득");
        
        try {
            Thread.sleep(100);  // 시뮬레이션
        } catch (InterruptedException e) {}
        
        // 다른 자원의 락 필요!
        other.doSomething();
    }
    
    public synchronized void doSomething() {
        System.out.println(Thread.currentThread().getName() + 
                          ": " + name + " 사용");
    }
}

// Deadlock 발생!
Resource r1 = new Resource("Resource 1");
Resource r2 = new Resource("Resource 2");

Thread t1 = new Thread(() -> r1.useWith(r2), "Thread-1");
Thread t2 = new Thread(() -> r2.useWith(r1), "Thread-2");

t1.start();
t2.start();

// Thread-1: r1 획득 → r2 대기
// Thread-2: r2 획득 → r1 대기
// → 둘 다 영원히 대기! 💀
```

<div class="mermaid mermaid-center">
graph LR
    T1["Thread 1"] -->|보유| R1["Resource 1<br/>Lock"]
    T1 -.->|대기| R2["Resource 2<br/>Lock"]
    
    T2["Thread 2"] -->|보유| R2
    T2 -.->|대기| R1
    
    R1 -.->|필요| T2
    R2 -.->|필요| T1
    
    style T1 fill:#533483,stroke:#16213e,stroke-width:2px,color:#eee
    style T2 fill:#533483,stroke:#16213e,stroke-width:2px,color:#eee
    style R1 fill:#8b0000,stroke:#16213e,stroke-width:3px,color:#eee
    style R2 fill:#8b0000,stroke:#16213e,stroke-width:3px,color:#eee
</div>

### Deadlock 방지 전략

**1. Lock 순서 정하기 (가장 효과적)**

```java
// 모든 스레드가 같은 순서로 락 획득
class SafeResource {
    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();
    
    public void safeOperation() {
        // 항상 lock1 → lock2 순서
        synchronized(lock1) {
            synchronized(lock2) {
                // 작업 수행
            }
        }
    }
}
```

**2. Nested Lock 피하기**

```java
// 나쁜 예
synchronized void method1() {
    synchronized(otherObject) {  // Nested lock!
        // 위험
    }
}

// 좋은 예
void method1() {
    Object temp;
    synchronized(this) {
        temp = getData();
    }
    // Lock 해제 후 다른 작업
    processData(temp);
}
```

**3. Timeout 사용**

```java
Lock lock1 = new ReentrantLock();
Lock lock2 = new ReentrantLock();

public boolean safeOperationWithTimeout() {
    try {
        if (lock1.tryLock(1, TimeUnit.SECONDS)) {
            try {
                if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        // 작업 수행
                        return true;
                    } finally {
                        lock2.unlock();
                    }
                }
            } finally {
                lock1.unlock();
            }
        }
        return false;  // Deadlock 회피
    } catch (InterruptedException e) {
        return false;
    }
}
```

**4. Lock-Free 자료구조 사용**

```java
import java.util.concurrent.atomic.AtomicInteger;

// Lock 없이 안전한 증가
AtomicInteger counter = new AtomicInteger(0);

// 여러 스레드에서 동시 호출해도 안전
counter.incrementAndGet();  // 원자적 연산
```

## 실전 예제: 안전한 멀티스레드 카운터

### 문제: Race Condition

```java
// 위험한 코드!
class UnsafeCounter {
    private int count = 0;
    
    public void increment() {
        count++;  // Read-Modify-Write (비원자적!)
    }
    
    public int getCount() {
        return count;
    }
}

// 결과: 기대값 2000, 실제값 1500 (예측 불가)
UnsafeCounter counter = new UnsafeCounter();

Thread t1 = new Thread(() -> {
    for (int i = 0; i < 1000; i++) counter.increment();
});
Thread t2 = new Thread(() -> {
    for (int i = 0; i < 1000; i++) counter.increment();
});

t1.start();
t2.start();
t1.join();
t2.join();

System.out.println(counter.getCount());  // 2000이 아님!
```

**참고:** [GeeksforGeeks - Atomic Variables](https://www.geeksforgeeks.org/java/atomic-variables-in-java-with-examples)

### 해결책 1: Synchronized

```java
class SynchronizedCounter {
    private int count = 0;
    
    public synchronized void increment() {
        count++;  // 모니터로 보호됨
    }
    
    public synchronized int getCount() {
        return count;
    }
}
```

### 해결책 2: ReentrantLock

```java
class LockedCounter {
    private int count = 0;
    private final Lock lock = new ReentrantLock();
    
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
    
    public int getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}
```

### 해결책 3: AtomicInteger (최고 성능)

```java
import java.util.concurrent.atomic.AtomicInteger;

class AtomicCounter {
    private final AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet();  // Lock-free!
    }
    
    public int getCount() {
        return count.get();
    }
}
```

**성능 비교:**

| 방식 | 10,000회 증가 시간 | 특징 |
|------|------------------|------|
| Unsafe | ~5ms | ❌ 잘못된 결과 |
| Synchronized | ~50ms | ✅ 안전, 느림 |
| ReentrantLock | ~55ms | ✅ 유연, 약간 느림 |
| AtomicInteger | ~15ms | ✅ 안전, 빠름, Lock-free |

## 핵심 정리

### 언제 무엇을 사용할까?

**프로세스 선택:**
- ✅ CPU 집약적 작업 (Python의 경우)
- ✅ 완전한 격리 필요
- ✅ 안정성이 최우선
- ❌ 메모리 공유 빈번
- ❌ 빠른 생성/종료 필요

**스레드 선택:**
- ✅ I/O 집약적 작업
- ✅ 메모리 공유 필요
- ✅ 빠른 컨텍스트 스위칭 필요
- ✅ 웹 서버, 네트워크 애플리케이션
- ❌ CPU 집약적 작업 (Python GIL)

**동기화 메커니즘 선택:**

| 상황 | 권장 방식 |
|------|---------|
| 간단한 동기화 | `synchronized` |
| 타임아웃 필요 | `ReentrantLock` |
| 고성능 카운터 | `AtomicInteger` |
| 대기/알림 패턴 | `wait()/notify()` 또는 `Condition` |
| 읽기 많음 | `ReadWriteLock` |

### 베스트 프랙티스

1. **공유 최소화**
   - 불변 객체 사용
   - Thread Local 변수 활용
   - 결과만 공유

2. **Lock 범위 최소화**
   ```java
   // 나쁜 예
   synchronized void bigMethod() {
       doA();
       doB();  // 동기화 불필요한 부분도 포함
       doC();
   }
   
   // 좋은 예
   void bigMethod() {
       doA();
       synchronized(this) {
           doB();  // 필요한 부분만
       }
       doC();
   }
   ```

3. **Deadlock 방지**
   - Lock 순서 일관성 유지
   - Nested Lock 피하기
   - Timeout 설정

4. **적절한 동시성 도구 사용**
   - `ExecutorService`로 스레드 풀 관리
   - `CompletableFuture`로 비동기 작업
   - `ConcurrentHashMap` 같은 동시성 컬렉션

## 마무리하며

프로세스와 스레드는 현대 소프트웨어 개발의 핵심 개념입니다. 올바른 선택과 사용법을 익히면:

- 🚀 **성능 최적화**: 멀티코어를 효과적으로 활용
- 🛡️ **안정성 향상**: 동기화 문제 예방
- 💡 **확장성 확보**: 대용량 트래픽 처리

하지만 잘못 사용하면 Deadlock, Race Condition 같은 디버깅 악몽에 빠질 수 있습니다. 기초를 탄탄히 하고, 상황에 맞는 도구를 선택하는 것이 중요합니다.

여러분의 프로젝트에서는 어떤 전략을 사용하고 계신가요? 겪었던 동시성 문제나 해결 경험을 댓글로 공유해주세요! 🙌