---
title: "[MariaDB] SELECT 쿼리는 정확히 어떻게 실행될까? - 소스 코드로 파헤친 실행 흐름"

tagline: "블랙박스였던 MariaDB 내부를 디버거로 추적하며 발견한 쿼리 실행의 모든 것"

header:
  overlay_image: /assets/post/sql/2026-03-19-mariadb-query-execution-flow/overlay.png
  overlay_filter: 0.5

categories:
  - SQL

tags:
  - MariaDB
  - 데이터베이스
  - 쿼리실행흐름
  - 소스코드분석
  - CLion
  - 디버깅

toc: true
show_date: true
mermaid: true

last_modified_at: 2026-03-19T15:30:00+09:00
---

평소에 당연하게 실행하던 SQL 쿼리, 혹시 이런 생각 해보셨나요?

```sql
SELECT * FROM users WHERE id = 1;
```

이 한 줄이 DBMS 내부에서 정확히 어떻게 처리되는지 궁금하지 않으셨나요? 저도 그랬습니다.

"어차피 추상화된 계층이니까 몰라도 되겠지"라고 생각했지만, **DB 내부 구조가 정말 궁금해서** MariaDB 소스 코드를 직접 디버거로 추적해봤습니다. 그리고 예상치 못한 놀라운 발견들을 했습니다.

이 글에서는 **CLion으로 실제 MariaDB 소스를 디버깅하며 배운 쿼리 실행 흐름**을 공유합니다. 이론이 아닌, 코드에서 직접 확인한 **생생한 경험**을 담았습니다.

---

## 왜 소스 코드까지 파고들었나?

처음엔 단순한 호기심이었습니다. 

"쿼리가 어떻게 파싱되고, 최적화되고, 실행되는지" 공식 문서만으로는 알 수 없는 **구체적인 흐름**이 궁금했죠. 

그러다가 [MariaDB Server GitHub 저장소](https://github.com/mariadb/server)를 발견하고, CLion으로 디버깅 환경을 구축했습니다. 브레이크포인트를 찍고, 변수를 추적하고, 콜 스택을 따라가면서... **"아, 이렇게 동작하는구나!"** 하는 순간들이 있었습니다.

특히 가장 놀라웠던 건 **handler 추상화 계층**이었습니다. InnoDB든 MyISAM이든 RocksDB든, SQL 레이어는 전혀 신경 쓰지 않고 동일한 인터페이스로 호출하더군요. 소프트웨어 설계의 교과서 같은 구조였습니다.

---

## 전체 쿼리 실행 흐름 한눈에 보기

먼저 **SELECT 쿼리가 실제로 거치는 전체 경로**를 시각화해보겠습니다.

<div class="mermaid mermaid-center">
graph TD
    Client[클라이언트<br/>mysql CLI / 앱] -->|MySQL Protocol<br/>TCP| A[do_command]
    A -->|네트워크 패킷 수신<br/>명령 종류 판별| B[dispatch_command]
    B -->|COM_QUERY| C[mysql_parse]
    C -->|① lex_start| C1[LEX 초기화]
    C -->|② query_cache 조회| C2{캐시 HIT?}
    C2 -->|YES| Return[바로 반환]
    C2 -->|NO| C3[parse_sql]
    C3 -->|Bison 파서| C4[MYSQLparse]
    C4 -->|파싱 결과| LEX[thd->lex 구조체]
    LEX --> D[mysql_execute_command]
    D -->|switch sql_command| E{명령 종류?}
    E -->|SQLCOM_SELECT| F[handle_select]
    E -->|SQLCOM_INSERT| G[mysql_insert]
    E -->|SQLCOM_UPDATE| H[mysql_update]
    F --> I[JOIN::optimize]
    I -->|통계 분석<br/>비용 계산| I1[make_join_statistics]
    I -->|인덱스 vs 풀스캔| I2[best_access_path]
    I -->|ORDER BY / GROUP BY| I3[optimize_stage2]
    I1 & I2 & I3 --> J[JOIN::exec]
    J --> K[do_select]
    K --> L[sub_select]
    L --> M[rr_sequential<br/>Full Table Scan]
    M --> N[handler::ha_rnd_next]
    N -->|virtual rnd_next| O{스토리지 엔진?}
    O -->|InnoDB| P1[ha_innobase::rnd_next]
    O -->|MyISAM| P2[ha_myisam::rnd_next]
    O -->|RocksDB| P3[ha_rocksdb::rnd_next]
    P1 --> Q[row_search_mvcc<br/>MVCC 체크]
    Q -->|버퍼 풀 조회<br/>디스크 I/O| R[행 데이터 반환]
    R --> S[send_data_with_check]
    S -->|WHERE 조건 체크| T[클라이언트로 전송]
    T --> U{더 읽을 행?}
    U -->|YES| M
    U -->|NO| V[완료]

    style A fill:#2e4057,stroke:#66d9ff,stroke-width:2px,color:#e0e0e0
    style D fill:#2e4057,stroke:#66d9ff,stroke-width:2px,color:#e0e0e0
    style I fill:#2e4057,stroke:#66d9ff,stroke-width:2px,color:#e0e0e0
    style N fill:#4a5f7f,stroke:#ffd966,stroke-width:3px,color:#e0e0e0
    style O fill:#4a5f7f,stroke:#ffd966,stroke-width:3px,color:#e0e0e0
    style Q fill:#2e4057,stroke:#66d9ff,stroke-width:2px,color:#e0e0e0
</div>

복잡해 보이지만, 크게 **5단계**로 나눌 수 있습니다:

1. **네트워크 수신** (`do_command` → `dispatch_command`)
2. **파싱** (`mysql_parse` → `parse_sql`)
3. **실행 계획 수립** (`JOIN::optimize`)
4. **데이터 읽기** (`handler` → 스토리지 엔진)
5. **결과 전송** (`send_data_with_check`)

---

## 핵심 개념: 알아야 할 4가지 구조체

소스 코드를 따라가다 보면 반복적으로 등장하는 핵심 구조체들이 있습니다.

### 1. THD (Thread Handler)

```cpp
class THD {
    LEX *lex;                  // 파싱 결과
    NET net;                   // 네트워크 연결
    Security_context security_ctx; // 사용자 권한
    Transaction_ctx transaction;   // 트랜잭션 상태
    // ...
};
```

**클라이언트 연결 하나 = THD 하나**

모든 쿼리 실행의 중심입니다. 현재 사용자, 트랜잭션 상태, 네트워크 연결 정보를 모두 담고 있습니다.

### 2. LEX (파싱 결과 바구니)

```cpp
struct LEX {
    enum_sql_command sql_command;  // SQLCOM_SELECT 등
    SELECT_LEX *select_lex;        // SELECT 관련 정보
    Item *where;                   // WHERE 조건
    // ...
};
```

SQL 문자열이 파싱되면 **구조화된 데이터**로 변환됩니다. 이것이 바로 LEX입니다.

예를 들어:
```sql
SELECT name, age FROM users WHERE age > 20;
```

이 쿼리는 아래처럼 변환됩니다:
- `sql_command` = `SQLCOM_SELECT`
- `select_lex->item_list` = `[name, age]`
- `select_lex->table_list` = `users`
- `where` = `age > 20`

### 3. JOIN 객체

```cpp
class JOIN {
    TABLE_LIST *tables;
    uint        table_count;
    JOIN_TAB   *join_tab;
    double      best_read;  // 예상 비용
    // ...
};
```

**SELECT 쿼리 하나 = JOIN 객체 하나**

놀랍게도, 테이블 1개만 조회하는 쿼리도 내부적으로 JOIN 객체를 사용합니다. "혼자 하는 JOIN"인 셈이죠.

이 객체에서 두 가지 핵심 작업이 일어납니다:
- `optimize()` : 실행 계획 수립 (어떤 인덱스? 어떤 순서?)
- `exec()` : 실제 데이터 실행

### 4. handler (추상화 계층)

```cpp
class handler {
public:
    virtual int rnd_next(uchar *buf) = 0;  // 다음 행 읽기
    virtual int index_read(uchar *buf, const uchar *key) = 0; // 인덱스로 읽기
    // ...
};

class ha_innobase : public handler {
    int rnd_next(uchar *buf) override;  // InnoDB 구현
};

class ha_myisam : public handler {
    int rnd_next(uchar *buf) override;  // MyISAM 구현
};
```

이것이 바로 제가 **가장 감탄한 부분**입니다.

SQL 레이어는 이렇게만 호출합니다:
```cpp
table->file->ha_rnd_next(buf);
```

이 한 줄이 InnoDB든 MyISAM이든 RocksDB든 **모두 작동**합니다. 스토리지 엔진을 교체해도 SQL 레이어 코드는 단 한 줄도 수정할 필요가 없죠.

<div class="mermaid mermaid-center">
graph TD
    SQL[SQL 레이어<br/>sql/sql_select.cc] -->|table->file->ha_rnd_next| Handler[handler 추상 클래스<br/>sql/handler.h]
    Handler -->|virtual rnd_next| InnoDB[ha_innobase<br/>InnoDB 엔진]
    Handler -->|virtual rnd_next| MyISAM[ha_myisam<br/>MyISAM 엔진]
    Handler -->|virtual rnd_next| RocksDB[ha_rocksdb<br/>RocksDB 엔진]
    InnoDB --> BufPool[버퍼 풀]
    InnoDB --> Disk1[디스크 .ibd]
    MyISAM --> Disk2[디스크 .MYD]
    RocksDB --> LSM[LSM Tree]

    style Handler fill:#4a5f7f,stroke:#ffd966,stroke-width:3px,color:#e0e0e0
    style SQL fill:#2e4057,stroke:#66d9ff,stroke-width:2px,color:#e0e0e0
    style InnoDB fill:#2e4057,stroke:#66d9ff,stroke-width:2px,color:#e0e0e0
    style MyISAM fill:#2e4057,stroke:#66d9ff,stroke-width:2px,color:#e0e0e0
    style RocksDB fill:#2e4057,stroke:#66d9ff,stroke-width:2px,color:#e0e0e0
</div>

---

## 단계별 상세 분석

이제 각 단계를 코드와 함께 자세히 살펴보겠습니다.

### 1단계: 네트워크 수신 (`do_command`)

**위치**: `sql/sql_parse.cc:1220`

```cpp
dispatch_command_return do_command(THD *thd, bool blocking) {
    char *packet;
    ulong packet_length;
    enum enum_server_command command;
    
    // 이전에 중단된 명령이 있으면 복원 (비동기 재개)
    if (thd->async_state.m_state == RESUMED) {
        command = thd->async_state.m_command;
        packet = thd->async_state.m_packet.str;
        packet_length = thd->async_state.m_packet.length;
        goto resume;
    }
    
    // 에러와 진단 정보 초기화
    thd->clear_error(1);
    
    // 네트워크 타임아웃 설정
    my_net_set_read_timeout(net, thd->get_net_wait_timeout());
    
    // TCP 소켓에서 패킷 수신
    packet_length = my_net_read_packet(net, 1);
    
    if (packet_length == packet_error) {
        // 네트워크 에러 발생
        return DISPATCH_COMMAND_CLOSE_CONNECTION;
    }
    
    packet = (char*) net->read_pos;
    
    // 명령 종류 추출 (COM_QUERY, COM_PING, COM_QUIT 등)
    command = fetch_command(thd, packet);
    
    // 네트워크 타임아웃 복원
    my_net_set_read_timeout(net, thd->variables.net_read_timeout);
    
resume:  // ← 비동기 재개 시 여기서 시작
    // 실제 명령 처리
    return_value = dispatch_command(command, thd, packet+1, 
                                   (uint)(packet_length-1), blocking);
    
    if (return_value == DISPATCH_COMMAND_WOULDBLOCK) {
        // 비동기: 아직 안 끝남 (threadpool 모드)
        // 현재 상태 저장하고 나중에 resume에서 재개
        thd->async_state.m_command = command;
        thd->async_state.m_packet = {packet, packet_length};
        return return_value;
    }
    
    return return_value;
}
```

**핵심 포인트**:
- `my_net_read_packet()`: TCP 소켓에서 바이너리 데이터 수신
- `command`: COM_QUERY (쿼리 실행), COM_PING (핑), COM_QUIT (종료) 등
- `resume:` 라벨: 비동기 명령이 WOULDBLOCK으로 중단되었다가 다시 재개될 때 사용
- 비동기 모드: threadpool 환경에서 긴 쿼리가 블로킹되지 않도록 상태를 저장하고 재개

### 2단계: 파싱 (`mysql_parse`)

**위치**: `sql/sql_parse.cc:7859`

```cpp
void mysql_parse(THD *thd, char *rawbuf, uint length,
                 Parser_state *parser_state) {
    // ① LEX 초기화 (파싱 전에 반드시 필요)
    lex_start(thd);
    
    // ② THD 상태 리셋 (이전 명령 정보 정리)
    thd->reset_for_next_command();
    
    // ③ 쿼리 캐시 조회
    // 주의: lex_start()를 먼저 호출해야 캐시가 제대로 작동
    if (query_cache_send_result_to_client(thd, rawbuf, length) <= 0) {
        // 캐시 미스(MISS) 또는 비활성화 → 파싱 필요
        LEX *lex = thd->lex;
        
        // ④ 실제 파싱 (Bison 문법 파서)
        bool err = parse_sql(thd, parser_state, NULL, true);
        
        if (likely(!err)) {
            // 파싱 성공
            
            // ⑤ 세미콜론 처리 (다중 쿼리 대응)
            const char *found_semicolon = parser_state->m_lip.found_semicolon;
            if (found_semicolon && (ulong)(found_semicolon - thd->query())) {
                // 세미콜론 이전까지만 쿼리로 인식
                thd->set_query(thd->query(),
                             (uint32)(found_semicolon - thd->query() - 1),
                             thd->charset());
                lex->safe_to_cache_query = 0;  // 다중 쿼리는 캐시 안 함
            }
            
            // ⑥ 실제 실행
            int error = mysql_execute_command(thd);
            
        } else {
            // 파싱 에러 발생
            query_cache_abort(thd, &thd->query_cache_tls);
        }
        
        // ⑦ 정리 작업
        thd->end_statement();
        thd->cleanup_after_query();
        
    } else {
        // 캐시 HIT! 파싱/실행 생략
        thd->lex->sql_command = SQLCOM_SELECT;
        thd->update_stats();
    }
}
```

**핵심 포인트**:

1. **lex_start() 먼저 호출**: 쿼리 캐시가 `lex->safe_to_cache_query`와 `thd->server_status`에 의존하므로 초기화가 선행되어야 함
2. **쿼리 캐시 체크**: `≤ 0`이면 캐시 미스, `> 0`이면 캐시에서 바로 결과 반환
3. **세미콜론 처리**: 다중 쿼리 실행 시 첫 번째 쿼리만 처리하고 나머지는 `SERVER_MORE_RESULTS_EXISTS` 플래그 설정
4. **캐시 정책**: 다중 쿼리(세미콜론 포함)는 `safe_to_cache_query = 0`으로 설정하여 캐시하지 않음

**파싱 과정 (`parse_sql`)**:

`parse_sql()` 내부에서는 **Bison 파서**가 동작합니다:

**위치**: `sql/sql_parse.cc:10313`

```cpp
bool parse_sql(THD *thd, Parser_state *parser_state,
               Object_creation_ctx *creation_ctx, bool do_pfs_digest) {
    
    // ① Parser state 설정
    thd->m_parser_state = parser_state;
    
    // ② Performance Schema Digest 설정 (쿼리 성능 모니터링용)
    parser_state->m_digest_psi = NULL;
    parser_state->m_lip.m_digest = NULL;
    
    if (do_pfs_digest) {
        // Digest 계산 시작 (쿼리 해시값 생성)
        parser_state->m_digest_psi = MYSQL_DIGEST_START(thd->m_statement_psi);
        
        if (parser_state->m_digest_psi != NULL) {
            parser_state->m_lip.m_digest = thd->m_digest;
            parser_state->m_lip.m_digest->m_digest_storage.m_charset_number = 
                thd->charset()->number;
        }
    }
    
    // ③ 실제 파싱: SQL 모드에 따라 다른 파서 호출
    bool mysql_parse_status = (thd->variables.sql_mode & MODE_ORACLE)
                              ? ORAparse(thd)    // Oracle 호환 모드
                              : MYSQLparse(thd); // 일반 MySQL 문법
    
    if (mysql_parse_status) {
        // 파싱 실패: LEX 정리 (side effect 제거)
        LEX::cleanup_lex_after_parse_error(thd);
    }
    
    // ④ current_select 포인터 복원
    thd->lex->current_select = thd->lex->first_select_lex();
    
    // ⑤ Parser state 리셋
    thd->m_parser_state = NULL;
    
    // ⑥ 반환: 파싱 실패 || 치명적 에러
    bool ret_value = mysql_parse_status || thd->is_fatal_error;
    
    if (ret_value == 0 && parser_state->m_digest_psi != NULL) {
        // 파싱 성공: Performance Schema에 Digest 기록
        MYSQL_DIGEST_END(parser_state->m_digest_psi,
                        &thd->m_digest->m_digest_storage);
    }
    
    return ret_value;
}
```

**핵심 포인트**:

1. **SQL 모드별 파서**: `MODE_ORACLE` 여부에 따라 `ORAparse()` 또는 `MYSQLparse()` 호출
2. **Bison 파서**: `MYSQLparse()`는 `sql/sql_yacc.yy`에서 생성된 문법 파서 (LALR 파서)
3. **Digest 계산**: Performance Schema에서 쿼리 성능 추적용 해시값 생성
4. **에러 처리**: 파싱 실패 시 `LEX::cleanup_lex_after_parse_error()`로 부작용 제거

**파싱 결과**는 `thd->lex`에 구조화된 형태로 저장됩니다.

### 3단계: 명령 분기 (`mysql_execute_command`)

**위치**: `sql/sql_parse.cc:3489`

```cpp
int mysql_execute_command(THD *thd, bool is_called_from_prepared_stmt) {
    int res = 0;
    LEX *lex = thd->lex;
    SELECT_LEX *select_lex = lex->first_select_lex();
    TABLE_LIST *first_table = select_lex->table_list.first;
    TABLE_LIST *all_tables = lex->query_tables;
    
    // 보안, 권한, 트랜잭션 처리...
    
    // 명령 타입에 따른 분기
    switch (lex->sql_command) {
    
    case SQLCOM_SELECT:
    case SQLCOM_SHOW_STATUS:
    case SQLCOM_SHOW_DATABASES:
    case SQLCOM_SHOW_TABLES:
        // 권한 체크
        privilege_t privileges_requested = lex->exchange ? 
            SELECT_ACL | FILE_ACL : SELECT_ACL;
        
        if (all_tables)
            res = check_table_access(thd, privileges_requested, 
                                    all_tables, FALSE, UINT_MAX, FALSE);
        
        if (!res)
            res = execute_sqlcom_select(thd, all_tables);
        break;
    
    case SQLCOM_INSERT:
    case SQLCOM_REPLACE:
        res = mysql_insert(thd, all_tables, lex->field_list, 
                          lex->many_values, ...);
        break;
    
    case SQLCOM_UPDATE:
    case SQLCOM_UPDATE_MULTI:
        DBUG_ASSERT(lex->m_sql_cmd != NULL);
        res = lex->m_sql_cmd->execute(thd);
        break;
    
    case SQLCOM_DELETE:
    case SQLCOM_DELETE_MULTI:
        DBUG_ASSERT(lex->m_sql_cmd != NULL);
        res = lex->m_sql_cmd->execute(thd);
        break;
    
    case SQLCOM_CREATE_TABLE:
    case SQLCOM_ALTER_TABLE:
    case SQLCOM_DROP_TABLE:
        DBUG_ASSERT(lex->m_sql_cmd != NULL);
        res = lex->m_sql_cmd->execute(thd);
        break;
    
    // ... 100개 이상의 case문 (SQLCOM_BEGIN, SQLCOM_COMMIT, etc.)
    
    default:
        DBUG_ASSERT(0);  // 불가능한 경우
        my_ok(thd);
        break;
    }
    
    // 정리 작업
    thd->update_stats();
    return res || thd->is_error();
}
```

**핵심 포인트**:
- 100개 이상의 `case`문으로 모든 SQL 명령 처리
- `execute_sqlcom_select()` → `handle_select()` 호출
- 일부 명령은 `lex->m_sql_cmd->execute()`로 위임

**여기서 SELECT 경로**로 들어가봅시다.

### 4단계: SELECT 최적화 (`JOIN::optimize`)

**위치**: `sql/sql_select.cc:1988`

```cpp
int JOIN::optimize() {
    int res = 0;
    
    if (select_lex->pushdown_select) {
        // 외부 스토리지 엔진으로 푸시다운 (예: Spider 엔진)
        if (optimization_state == JOIN::OPTIMIZATION_DONE)
            return 0;
        
        fields = &select_lex->item_list;
        if (!(select_options & SELECT_DESCRIBE)) {
            res = select_lex->pushdown_select->prepare();
        }
        with_two_phase_optimization = false;
    } else {
        if (optimization_state != JOIN::NOT_OPTIMIZED)
            return FALSE;
        
        optimization_state = JOIN::OPTIMIZATION_IN_PROGRESS;
        res = optimize_inner();
    }
    
    if (!with_two_phase_optimization) {
        if (!res && have_query_plan != QEP_DELETED)
            res = build_explain();
        optimization_state = JOIN::OPTIMIZATION_DONE;
    }
    
    // 쿼리 비용을 사용자 변수에 저장
    if (select_lex->select_number == 1)
        thd->status_var.last_query_cost = best_read;
    
    return res;
}

bool JOIN::optimize_inner() {
    // ① 테이블 통계, 인덱스 분석
    make_join_statistics(this, tables_list, &conds, &keyuse_array);
    
    // ② 풀스캔 vs 인덱스 비용 계산
    best_access_path(this, s, ...);
    
    // ③ ORDER BY / GROUP BY 처리 방식 결정
    if (optimize_stage2(this)) {
        return true;
    }
    
    return false;
}
```

**비용 계산 예시** (공식 문서 기준):

MariaDB 11.0부터 [Optimizer Cost Model](https://github.com/mariadb-corporation/mariadb-docs/blob/main/general-resources/development-articles/mariadb-internals/mariadb-internals-documentation-query-optimizer/the-optimizer-cost-model-from-mariadb-11-0.md)이 도입되었습니다.

```sql
-- InnoDB Full Scan 비용
COST = ROW_COUNT * ROW_COPY_COST + TABLE_SCAN_COST

-- Index Range Scan 비용
COST = KEY_LOOKUP_COST * ESTIMATED_ROWS + KEY_COMPARE_COST * log2(ROWS)
```

`best_read` 변수에 **최종 예상 비용**이 저장됩니다.

### 5단계: 실행 (`JOIN::exec`)

**위치**: `sql/sql_select.cc:4902`

```cpp
int JOIN::exec() {
    int res;
    DBUG_ASSERT(optimization_state == OPTIMIZATION_DONE);
    
    // EXPLAIN을 위한 프로브 포인트
    DBUG_EXECUTE_IF("show_explain_probe_join_exec_start", 
                    if (dbug_user_var_equals_int(thd, 
                                                 "show_explain_probe_select_id", 
                                                 select_lex->select_number))
                        dbug_serve_apcs(thd, 1);
                   );
    
    ANALYZE_START_TRACKING(thd, &explain->time_tracker);
    res = exec_inner();
    ANALYZE_STOP_TRACKING(thd, &explain->time_tracker);
    
    return res;
}

int JOIN::exec_inner() {
    // 컬럼 헤더 전송 (Field A, Field B 등)
    result->send_result_set_metadata(...);
    
    // 실제 데이터 읽기
    do_select(this);
    
    return 0;
}
```

### 6단계: 행 읽기 (`rr_sequential`)

**위치**: `sql/records.cc:506`

```cpp
int rr_sequential(READ_RECORD *info) {
    int tmp;
    
    // handler를 통해 다음 행 읽기
    while ((tmp = info->table->file->ha_rnd_next(info->record()))) {
        // 에러 처리 (삭제된 레코드, 잠금 등)
        tmp = rr_handle_error(info, tmp);
        break;
    }
    
    return tmp;
}
```

**행 읽기 방식은 여러 가지**:

| 함수 | 사용 시점 | 설명 |
|------|---------|-----|
| `rr_sequential()` | Full Table Scan | 순차적으로 모든 행 읽기 |
| `join_read_always_key()` | Index Range Scan | 인덱스로 범위 검색 |
| `join_read_key()` | Primary Key | PK 단건 조회 |
| `join_read_const()` | Const Table | 상수 조건 (결과 1건 확정) |

### 7단계: 스토리지 엔진 호출 (`handler::ha_rnd_next`)

**위치**: `sql/handler.cc:4027`

```cpp
int handler::ha_rnd_next(uchar *buf) {
    int result;
    DBUG_ASSERT(table_share->tmp_table != NO_TMP_TABLE ||
                m_lock_type != F_UNLCK);
    DBUG_ASSERT(inited == RND);
    
    do {
        // ★ 핵심: 가상 함수 호출 (다형성)
        TABLE_IO_WAIT(tracker, PSI_TABLE_FETCH_ROW, MAX_KEY, result,
            { result = rnd_next(buf); })
        
        if (result != HA_ERR_RECORD_DELETED)
            break;
        
        status_var_increment(table->in_use->status_var.ha_read_rnd_deleted_count);
    } while (!table->in_use->check_killed(1));
    
    if (result == HA_ERR_RECORD_DELETED)
        result = HA_ERR_ABORTED_BY_USER;
    else {
        if (!result) {
            update_rows_read();
            // 가상 컬럼 업데이트
            if (table->vfield && buf == table->record[0])
                table->update_virtual_fields(this, VCOL_UPDATE_FOR_READ);
        }
        increment_statistics(&SSV::ha_read_rnd_next_count);
    }
    
    table->status = result ? STATUS_NOT_FOUND : 0;
    return result;
}
```

`rnd_next()`는 **순수 가상 함수**입니다. 각 엔진이 구현합니다.

### 8단계: InnoDB 엔진 (`ha_innobase::rnd_next`)

**위치**: `storage/innobase/handler/ha_innodb.cc:9420`

```cpp
int ha_innobase::rnd_next(uchar* buf) {
    int error;
    
    if (m_start_of_scan) {
        // 첫 번째 행: 인덱스 처음부터 시작
        error = index_first(buf);
        
        if (error == HA_ERR_KEY_NOT_FOUND) {
            error = HA_ERR_END_OF_FILE;
        }
        
        m_start_of_scan = false;
    } else {
        // 이후 행들: 다음 행 가져오기
        error = general_fetch(buf, ROW_SEL_NEXT, 0);
    }
    
    return error;
}
```

### 9단계: MVCC 처리 (`row_search_mvcc`)

**위치**: `storage/innobase/row/row0sel.cc` (InnoDB 내부)

```cpp
dberr_t row_search_mvcc(
    byte* buf,
    page_cur_mode_t mode,
    row_prebuilt_t* prebuilt,
    ulint match_mode,
    ulint direction
) {
    dict_index_t* index = prebuilt->index;
    trx_t* trx = prebuilt->trx;
    const rec_t* rec;
    dberr_t err = DB_SUCCESS;
    
    // ① 테이블 상태 확인
    if (!prebuilt->table->space) {
        return DB_TABLESPACE_DELETED;
    } else if (!prebuilt->table->is_readable()) {
        return DB_CORRUPTION;
    } else if (prebuilt->index->is_corrupted()) {
        return DB_CORRUPTION;
    }
    
    // ② 트랜잭션 격리 수준 확인 및 Read View 체크
    if (trx->isolation_level == TRX_ISO_READ_UNCOMMITTED
        || !trx->read_view.is_open()) {
        // READ UNCOMMITTED: 최신 버전 읽기
    } else if (trx_id_t bulk_trx_id = index->table->bulk_trx_id) {
        if (prebuilt->select_lock_type == LOCK_NONE
            && !trx->read_view.changes_visible(bulk_trx_id)) {
            // 테이블이 비어있음
            return DB_END_OF_INDEX;
        }
    }
    
rec_loop:
    // ③ 레코드 가져오기
    rec = btr_pcur_get_rec(pcur);
    
    // ④ InnoDB 버퍼 풀에서 페이지 검색 (간략화)
    offsets = rec_get_offsets(rec, index, offsets, 
                             index->n_core_fields,
                             ULINT_UNDEFINED, &heap);
    
    // ⑤ MVCC: 현재 트랜잭션이 볼 수 있는 버전인지 체크
    if (index == clust_index) {
        err = row_sel_clust_sees(rec, *index, offsets, trx->read_view);
        
        if (err == DB_SUCCESS_LOCKED_REC) {
            // 현재 버전이 보이지 않음 → 이전 버전 찾기 (UNDO 로그)
            rec_t* old_vers;
            err = row_sel_build_prev_vers_for_mysql(
                prebuilt, clust_index,
                rec, &offsets, &heap, &old_vers,
                need_vrow ? &vrow : nullptr, &mtr);
            
            if (err != DB_SUCCESS) {
                goto lock_wait_or_error;
            }
            
            if (old_vers == NULL) {
                // 행이 Read View에 존재하지 않음
                goto next_rec;
            }
            
            rec = old_vers;
        }
    }
    
    // ⑥ 삭제 마크된 레코드 스킵
    if (rec_get_deleted_flag(rec, comp)) {
        goto next_rec;
    }
    }
    
    // ④ 행 데이터를 buf에 복사
    rec_copy(buf, rec);
    
    return DB_SUCCESS;
}
```

**MVCC (Multi-Version Concurrency Control)**:
- `READ COMMITTED`: 커밋된 최신 데이터 읽기
- `REPEATABLE READ`: 트랜잭션 시작 시점 스냅샷 읽기
- 읽기와 쓰기가 서로 블로킹하지 않음

### 10단계: 결과 전송 (`send_data_with_check`)

**위치**: `sql/sql_class.h:3299` → `sql_class.cc`

```cpp
int select_result_sink::send_data_with_check(
    List<Item> &items,
    SELECT_LEX_UNIT *u,
    ha_rows sent
) {
    // LIMIT 오프셋 체크
    if (u->lim.check_offset(sent))
        return 0;
    
    // 쿼리가 중단되었는지 체크
    if (u->thd->killed == ABORT_QUERY)
        return 0;
    
    // 실제 데이터 전송
    int rc = send_data(items);
    
    // 복잡한 데이터 타입 정리
    if (thd->stmt_arena->with_complex_data_types())
        thd->stmt_arena->expr_event_handler_for_free_list(thd,
                    expr_event_t::DESTRUCT_RESULT_SET_ROW_FIELD);
    
    return rc;
}
```

**실제 네트워크 전송**은 `Protocol` 클래스를 통해 일어납니다:

```cpp
// sql/protocol.cc
bool Protocol::send_result_set_row(List<Item> *row_items) {
    List_iterator_fast<Item> it(*row_items);
    ValueBuffer<MAX_FIELD_WIDTH> value_buffer;
    
    for (Item *item = it++; item; item = it++) {
        value_buffer.reset_buffer();
        
        // 각 컬럼 값 직렬화
        if (item->send(this, &value_buffer)) {
            // 메모리 부족 시 복구 시도
            this->free();
            return TRUE;
        }
        
        // 에러 발생 시 중단
        if (unlikely(thd->is_error()))
            return TRUE;
    }
    
    return FALSE;
}
```

---

## 실무에 어떻게 활용할까?

이런 내부 구조를 알면 **실무에서 어떤 도움이 될까요?**

### 1. 쿼리 최적화 인사이트

```sql
EXPLAIN SELECT * FROM orders WHERE customer_id = 123;
```

EXPLAIN 결과에서 `type: ALL`이 나오면 **Full Table Scan**입니다.

이제 우리는 알 수 있습니다:
- `rr_sequential()` 함수가 호출됩니다
- `ha_rnd_next()`가 **모든 행**을 하나씩 읽습니다
- 테이블이 클수록 느려집니다

**해결책**: 인덱스 추가
```sql
CREATE INDEX idx_customer ON orders(customer_id);
```

이제 `join_read_always_key()`로 변경되고, **로그 시간 복잭도**로 빠르게 검색됩니다.

### 2. 스토리지 엔진 선택 기준

handler 추상화 덕분에 엔진을 자유롭게 교체할 수 있습니다.

| 엔진 | 특징 | 적합한 용도 |
|------|------|-----------|
| **InnoDB** | MVCC, 트랜잭션, 외래키 | 일반적인 OLTP |
| **MyISAM** | 테이블 락, INSERT 빠름 | 로그 테이블, 읽기 전용 |
| **RocksDB** | LSM Tree, 압축 우수 | 대용량 데이터, 쓰기 많음 |
| **Aria** | Crash-safe MyISAM | 임시 테이블 |

예를 들어, **로그 수집 테이블**이라면:
```sql
CREATE TABLE access_logs (
    log_time DATETIME,
    user_id INT,
    url VARCHAR(255)
) ENGINE=MyISAM;  -- INSERT가 빠르고 트랜잭션 불필요
```

### 3. 트랜잭션 격리 수준 이해

`row_search_mvcc()`를 알면 격리 수준을 깊이 이해할 수 있습니다.

```sql
-- 세션 A
START TRANSACTION;
SELECT balance FROM accounts WHERE id = 1;  -- 100원
-- ... 시간 경과 ...

-- 세션 B (다른 커넥션)
UPDATE accounts SET balance = 200 WHERE id = 1;
COMMIT;

-- 세션 A (계속)
SELECT balance FROM accounts WHERE id = 1;  -- ?????
```

**격리 수준에 따라**:
- `READ COMMITTED`: 200원 (최신 커밋 데이터)
- `REPEATABLE READ` (기본값): 100원 (트랜잭션 시작 시점 스냅샷)

이제 **왜 그런지** 알 수 있습니다. `row_search_mvcc()`가 `trx->read_view`를 체크하여 **UNDO 로그에서 이전 버전**을 가져오기 때문이죠.

### 4. 쿼리 캐시 활용 (MariaDB 10.x)

⚠️ **주의**: MariaDB 10.11부터 쿼리 캐시가 제거되었습니다.

하지만 10.x 버전을 쓴다면:
```sql
-- 캐시 활성화
SET GLOBAL query_cache_type = ON;
SET GLOBAL query_cache_size = 64M;

-- 캐시 무효화 (데이터 변경 시)
UPDATE users SET name = 'Alice';  -- users 테이블 관련 캐시 자동 삭제
```

`mysql_parse()`에서 캐시를 먼저 확인하므로, 파싱조차 생략됩니다.

---

## CLion 디버깅 실습 가이드

직접 따라 해보고 싶다면, 이렇게 시작하세요.

### 환경 구축

```bash
# 1. 소스 다운로드
git clone https://github.com/mariadb/server.git
cd server

# 2. 빌드 (Ubuntu 기준)
sudo apt install build-essential cmake libncurses5-dev bison
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Debug
make -j$(nproc)

# 3. CLion에서 열기
# File → Open → server 폴더 선택
```

### 브레이크포인트 추천 위치

| 순서 | 파일 | 라인 | 함수 | 확인할 변수 |
|------|------|------|------|-----------|
| 1 | `sql/sql_parse.cc` | 1432 | `do_command` 내 `resume:` | `command`, `packet` |
| 2 | `sql/sql_parse.cc` | 7882 | `mysql_parse` | `rawbuf` (SQL 원본) |
| 3 | `sql/sql_parse.cc` | 3953 | `case SQLCOM_SELECT:` | `thd->lex->sql_command` |
| 4 | `sql/sql_select.cc` | 2216 | `JOIN::optimize_inner` | `join->best_read` |
| 5 | `sql/records.cc` | 508 | `rr_sequential` | `info->record()` |
| 6 | `sql/handler.cc` | 4047 | `ha_rnd_next` | `buf` (전후 비교) |
| 7 | `storage/innobase/handler/ha_innodb.cc` | 9420 | `ha_innobase::rnd_next` | - |

### 실습 쿼리

```sql
-- 간단한 SELECT로 시작
SELECT * FROM mysql.user LIMIT 1;

-- WHERE 조건 추가
SELECT * FROM mysql.user WHERE user = 'root';

-- JOIN 쿼리
SELECT u.user, d.db 
FROM mysql.user u 
JOIN mysql.db d ON u.user = d.user 
LIMIT 5;
```

각 쿼리마다 **콜 스택 깊이**와 **호출 횟수**가 달라집니다.

---

## 핵심 파일 정리

| 파일 | 역할 | 주요 함수 |
|------|------|---------|
| `sql/sql_parse.cc` | 네트워크 수신, 파싱, 명령 분기 | `do_command`, `mysql_parse`, `mysql_execute_command` |
| `sql/sql_select.cc` | SELECT 최적화 & 실행 | `handle_select`, `JOIN::optimize`, `JOIN::exec` |
| `sql/records.cc` | 행 읽기 방식 구현 | `rr_sequential`, `rr_from_cache` |
| `sql/handler.cc` | 스토리지 엔진 추상화 | `handler::ha_rnd_next`, `handler::ha_index_read` |
| `sql/handler.h` | handler 인터페이스 정의 | 가상 함수 선언 |
| `storage/innobase/handler/ha_innodb.cc` | InnoDB 엔진 구현 | `ha_innobase::rnd_next`, `general_fetch` |
| `storage/innobase/row/row0sel.cc` | InnoDB 행 검색 & MVCC | `row_search_mvcc` |

**빌드 규모** (참고):
- 전체 `.cc` 파일: **1,402개** / 55.2 MB
- `sql/` 폴더만: **249개** / 17.8 MB
- 최초 빌드: **20~30분** / 수정 후 재빌드: **1~3분**

---

## 마치며: 블랙박스를 열어보세요

처음엔 "소스 코드까지 봐야 하나?" 싶었습니다. 하지만 직접 디버거로 추적하면서 **DB가 어떻게 동작하는지** 깊이 이해하게 되었고, 이는 쿼리 최적화와 문제 해결에 큰 도움이 되었습니다.

특히 **handler 추상화 계층**처럼, 단순히 "이렇게 되어 있다"를 넘어서 **"왜 이렇게 설계했는지"**를 느낄 수 있었던 건 큰 수확이었습니다.

### 핵심 요약

✅ **쿼리 실행은 5단계**: 네트워크 수신 → 파싱 → 최적화 → 데이터 읽기 → 결과 전송  
✅ **handler 추상화**: SQL 레이어는 스토리지 엔진을 몰라도 됨 (다형성의 힘)  
✅ **MVCC**: 읽기와 쓰기가 블로킹 없이 동시 실행 가능  
✅ **CLion 디버깅**: 브레이크포인트로 실제 흐름 확인 가능  

### 다음 단계는?

1. **직접 디버깅 해보기**: [MariaDB GitHub](https://github.com/mariadb/server)에서 클론
2. **EXPLAIN 분석 연습**: 실제 쿼리의 실행 계획 읽기
3. **스토리지 엔진 비교**: InnoDB vs MyISAM vs RocksDB 벤치마킹

---

**여러분은 MariaDB/MySQL 내부를 얼마나 이해하고 계신가요?**  
**쿼리 최적화 경험이나 궁금한 점이 있다면 댓글로 공유해주세요!**

---

## 참고 자료

- [MariaDB Server GitHub Repository](https://github.com/mariadb/server)
- [MariaDB ColumnStore Architecture](https://github.com/mariadb-corporation/mariadb-docs/blob/main/analytics/mariadb-columnstore/architecture/columnstore-architectural-overview.md)
- [MariaDB Optimizer Costs Documentation](https://github.com/mariadb/server/blob/main/Docs/optimizer_costs.txt)
