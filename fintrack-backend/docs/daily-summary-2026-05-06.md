# 2026-05-06 작업 정리

## 오늘 한 내용

### 1. 커밋 대상 파일 점검 및 `.gitignore` 보강

- Git 상태를 확인해 커밋에 포함되면 안 되는 파일을 점검했다.
- 로컬 실행 스크립트, 환경변수 파일, 프론트 소스맵을 ignore 대상으로 추가했다.

추가 규칙:

```gitignore
application-local.properties
application-*.local.properties
src/main/resources/static/**/*.map
```


주의 사항:

- `application.properties`에는 JWT secret, DB password 같은 값이 있어 운영/개인 값이면 분리 필요.
- 이미 Git이 추적 중인 파일은 `.gitignore`만으로 제외되지 않음.

### 2. 최근 주도 테마 조회 기능 추가

- `/api/stocks/themes/trending` API를 추가했다.
- 등록/저장된 테마별 종목을 기준으로 가격, 거래량, 검색 관심도 점수를 계산한다.
- 네이버 데이터랩 검색어 트렌드 API를 연동했다.

점수 구조:

```text
총점 =
  검색 관심도 35%
+ 수급 점수 40%
+ 가격 점수 25%
```

설정값:

```properties
app.naver.client-id=${NAVER_CLIENT_ID:}
app.naver.client-secret=${NAVER_CLIENT_SECRET:}
app.stock.trends.request-delay-ms=${STOCK_TRENDS_REQUEST_DELAY_MS:300}
```

### 3. 자동 발견 테마 후보 수집 추가

- `/api/stocks/themes/discover` API를 추가했다.
- 네이버 뉴스 검색 API에서 최근 뉴스 제목/본문을 수집한다.
- `관련주`, `테마주`, `주도주` 패턴과 주요 섹터 키워드로 테마 후보를 추출한다.
- 네이버 뉴스 API 호출 시 401/403/429 상황을 처리하도록 보강했다.

관련 설정:

```properties
app.naver.news.max-requests=${NAVER_NEWS_MAX_REQUESTS:8}
app.naver.news.request-delay-ms=${NAVER_NEWS_REQUEST_DELAY_MS:250}
```

처리한 문제:

- 네이버 데이터랩 API 권한과 검색 API 권한이 별도라 뉴스 후보 수집에서 401이 발생했다.
- 키워드 검색 호출이 많아 429 rate limit이 발생했다.
- 호출 수 제한과 요청 간 지연을 추가했다.
- 검색 결과가 있어도 후보가 0개가 되는 문제를 줄이기 위해 테마 키워드 검색 결과 fallback을 추가했다.

### 4. 발견 테마 후보 DB 저장

- 자동 발견 후보를 서버 메모리가 아니라 DB에 저장하도록 변경했다.
- 서버 재기동 후에도 수집된 후보를 다시 사용할 수 있다.

추가 테이블:

```text
discovered_themes
discovered_theme_stocks
```

추가 API:

```http
GET /api/stocks/themes/discovered?limit=10
```

마이그레이션:

```text
V3_create_discovered_themes.sql
```

### 5. 종목 마스터 DB 추가

- `StockThemeRegistry` 하드코딩 구조를 제거하기 위한 `stock_master` 테이블을 추가했다.
- 앱 시작 시 주요 테마/종목 seed를 저장한다.
- 뉴스 후보 수집 시 `stock_master`의 종목명/테마 키워드 기준으로 종목을 매칭한다.

추가 테이블:

```text
stock_master
```

마이그레이션:

```text
V4_create_stock_master.sql
```

현재 seed 테마:

- AI 반도체
- 2차전지
- 조선
- 제약·바이오
- 전력·로봇
- 원전
- 방산
- 화장품
- 가상자산·보안

### 6. `StockThemeRegistry` 제거

- 기존 하드코딩 테마/종목 목록인 `StockThemeRegistry`를 삭제했다.
- 스크리너와 최근 주도 테마 계산은 이제 DB 기반으로 동작한다.

변경 후 기준:

```text
스크리너 테마 목록
→ stock_master 기준

최근 주도 테마
→ stock_master + discovered_themes 기준

자동 발견 후보
→ stock_master로 종목 매칭 후 discovered_themes에 저장
```

### 7. 발견 테마 스크리너 실행 연결

- 최근 주도 테마에 자동 발견 테마가 `discovered-*` 키로 표시된다.
- 스크리너 실행 시 `discovered-*` 키를 인식해 `discovered_theme_stocks`에서 종목을 조회하도록 수정했다.
- `all` 실행 시 `stock_master` 종목과 발견 테마 종목을 합쳐 중복 제거 후 실행한다.

### 8. 화면 분리

- `/stocks/screener` 화면을 테마 탐색 전용으로 변경했다.
- 기존 화면에 있던 스크리너 실행 컨트롤과 결과 테이블을 제거했다.
- 최근 주도 테마를 클릭하면 별도 실행 페이지로 이동한다.

화면 구조:

```text
/stocks/screener
→ 최근 주도 테마
→ 자동 발견 테마 후보

/stocks/screener/run?themeKey=...&themeName=...
→ 선택한 테마 스크리너 자동 실행
```

추가 프론트 파일:

```text
frontend/src/components/StockScreenerRunPage.js
```

## 주요 API

```http
GET /api/stocks/themes/trending
GET /api/stocks/themes/discover
GET /api/stocks/themes/discovered
GET /api/stocks/screener/themes
POST /api/stocks/screener/run
```

## 주요 추가/수정 파일

백엔드:

- `application/api/stock/StockThemeTrendController.java`
- `domain/stock/StockThemeTrendService.java`
- `domain/stock/StockThemeDiscoveryService.java`
- `domain/stock/StockMasterService.java`
- `domain/stock/StockScreenerService.java`
- `domain/stock/SearchTrendProvider.java`
- `domain/stock/ThemeDiscoveryProvider.java`
- `domain/stock/model/DiscoveredTheme.java`
- `domain/stock/model/DiscoveredThemeEntity.java`
- `domain/stock/model/DiscoveredThemeStock.java`
- `domain/stock/model/DiscoveredThemeStockEntity.java`
- `domain/stock/model/StockMasterEntity.java`
- `domain/stock/model/ThemeTrendResult.java`
- `domain/stock/model/ThemeTrendStockFlow.java`
- `domain/stock/repository/DiscoveredThemeRepository.java`
- `domain/stock/repository/StockMasterRepository.java`
- `infrastructure/stock/NaverSearchTrendProvider.java`
- `infrastructure/stock/NaverNewsThemeDiscoveryProvider.java`
- `infrastructure/stock/StockMasterSeeder.java`
- `exception/GlobalExceptionHandler.java`
- `resources/db/migration/V3_create_discovered_themes.sql`
- `resources/db/migration/V4_create_stock_master.sql`
- `resources/application.properties`

프론트:

- `frontend/src/App.js`
- `frontend/src/components/StockScreenerPage.js`
- `frontend/src/components/StockScreenerRunPage.js`
- `frontend/src/components/StockPage.css`

삭제:

- `domain/stock/StockThemeRegistry.java`

## 검증

백엔드 컴파일:

```powershell
.\gradlew.bat compileJava
```

결과: 성공

백엔드 테스트:

```powershell
.\gradlew.bat test
```

결과: 성공

프론트 빌드:

```powershell
npm run build
```

결과: 성공

## 현재 한계

### 1. 종목 마스터가 아직 seed 기반

`stock_master`는 DB 테이블이지만, 현재 데이터는 `StockMasterSeeder`의 주요 테마 seed로 채운다.

다음 단계는 KIS 또는 KRX 기반 전체 국내 종목 마스터 동기화다.

### 2. 테마 매핑은 아직 키워드 기반

KIS API로 종목 목록은 가져올 수 있지만, "이 종목이 어떤 테마에 속하는가"는 별도 가공이 필요하다.

현재는 다음 기준으로 매핑한다.

- 뉴스 제목/본문의 종목명
- 뉴스 제목/본문의 테마 키워드
- `stock_master.keywords`

### 3. 후보 수집은 네이버 API rate limit 영향을 받음

뉴스 검색 API 호출 수를 줄였지만, 짧은 시간에 여러 번 누르면 429가 날 수 있다.

현재 기본값:

```properties
app.naver.news.max-requests=8
app.naver.news.request-delay-ms=250
```

## 다음에 할 일

### 1. KIS/공공 데이터 기반 종목 마스터 동기화

현재 seed 기반인 `stock_master`를 전체 국내 종목으로 확장한다.

예상 구조:

```http
POST /api/stocks/master/sync
GET /api/stocks/master
```

저장 항목:

- 종목코드
- 종목명
- 시장구분
- ETF/ETN/스팩 제외 여부
- 상장 상태

### 2. 관련주 검색 기반 종목 매핑 고도화

자동 발견 테마에 대해:

```text
테마명 + 관련주 검색
→ 기사에서 종목명 추출
→ stock_master에서 종목코드 매칭
→ discovered_theme_stocks 저장
```

### 3. 스크리너 백그라운드 Job 구조

현재는 HTTP 요청 하나에서 여러 종목을 순차 수집/분석한다.

다음 구조가 필요하다.

```http
POST /api/stocks/screener/jobs
GET /api/stocks/screener/jobs/{jobId}
GET /api/stocks/screener/jobs/{jobId}/results
```

### 4. KIS 중복 수집 방지

이미 오늘 수집한 종목은 KIS API를 다시 호출하지 않고 DB 데이터를 사용하도록 개선한다.

기대 효과:

- KIS rate limit 완화
- 스크리너 실행 시간 단축
- 중복 upsert 감소

### 5. 발견 테마 관리 화면

저장된 발견 테마를 화면에서 관리할 수 있게 한다.

- 후보 삭제
- 후보 고정
- 종목 추가/제외
- 테마명 수정
- 신뢰도 점수 조정
