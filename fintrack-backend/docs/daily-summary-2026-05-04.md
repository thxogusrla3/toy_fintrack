# 2026-05-04 작업 정리

## 오늘 한 내용

### 1. KIS 일봉 응답에서 종목명 저장

- KIS 일봉 조회 응답의 `output1.hts_kor_isnm` 값을 `stockName`으로 매핑했다.
- `stock_candles` 엔티티에 `stock_name` 컬럼을 추가했다.
- 캔들 신규 저장/업데이트 시 종목명을 함께 저장하도록 수정했다.
- 수집 API 응답에도 `stockName`을 포함하도록 변경했다.

예상 응답:

```json
{
  "stockCode": "000660",
  "stockName": "SK하이닉스",
  "collectedCount": 60,
  "totalCount": 120
}
```

### 2. 주식 상세 화면 추가

- React에 `/stocks` 화면을 추가했다.
- 종목코드, 시작일, 종료일, 분석 캔들 수를 입력할 수 있게 했다.
- 버튼 기능:
  - 일봉 수집
  - 저장 데이터 분석
  - 수집 후 분석
- 수집 결과와 분석 결과를 화면에 표시한다.
- 스크리너에서 넘어온 종목코드는 `/stocks?code=000660` 형태로 받아 기본값으로 세팅된다.

### 3. React 정적 배포 연결

- React build 결과를 백엔드 `src/main/resources/static`에 반영했다.
- Spring Boot에서 React 라우트를 직접 접근할 수 있도록 SPA forward 컨트롤러를 추가했다.
- `/stocks`, `/stocks/screener` 같은 경로를 새로고침해도 `index.html`로 연결되도록 처리했다.
- `/api/**` 보호 규칙은 유지하고, 프론트 정적 라우트는 접근 가능하도록 Security 설정을 조정했다.

### 4. 테마 스크리너 추가

- React에 `/stocks/screener` 화면을 추가했다.
- 백엔드에 스크리너 API를 추가했다.

API:

```http
GET /api/stocks/screener/themes
POST /api/stocks/screener/run
```

스크리너 기준 테마:

- AI 반도체
- 조선
- 제약·바이오
- 전력·로봇

스크리너 동작:

- 선택한 테마의 후보 종목을 조회한다.
- 실행 시 KIS 일봉 데이터를 수집한다.
- 저장된 캔들 데이터로 `BUY`, `HOLD`, `DANGER`, `NONE` 신호를 분석한다.
- `BUY`, `HOLD`는 통과 종목으로 표시한다.
- 점수순으로 정렬해서 리스트를 보여준다.
- 리스트 행을 클릭하면 `/stocks?code=종목코드` 상세 화면으로 이동한다.

### 5. KIS 초당 거래건수 제한 대응

- 스크리너가 여러 종목을 연속 조회하면서 KIS 초당 거래건수 제한에 걸리는 문제가 있었다.
- 종목별 KIS 요청 사이에 기본 1.2초 지연을 추가했다.
- rate limit 관련 오류가 발생하면 잠깐 대기 후 재시도하도록 했다.

설정값:

```properties
app.stock.screener.request-delay-ms=${STOCK_SCREENER_REQUEST_DELAY_MS:1200}
app.stock.screener.max-retries=${STOCK_SCREENER_MAX_RETRIES:2}
```

필요 시 실행 환경에서 조정 가능:

```powershell
$env:STOCK_SCREENER_REQUEST_DELAY_MS="2000"
$env:STOCK_SCREENER_MAX_RETRIES="3"
```

### 6. 화면 커넥션 끊김 대응

- 스크리너 실행 중 DB insert와 KIS 수집 시간이 길어지면서 화면 요청이 먼저 timeout 되는 문제가 있었다.
- 기존 Axios timeout이 10초라 전체 스크리너 실행에는 부족했다.
- timeout을 늘렸다.

변경:

- 공통 API timeout: 10초에서 120초
- 스크리너 실행 요청 timeout: 300초
- 단일 종목 수집 요청 timeout: 120초
- 실행 중 안내 메시지 추가

## 검증

프론트 빌드:

```powershell
npm run build
```

결과: 성공

백엔드 테스트:

```powershell
.\gradlew.bat test
```

결과: 성공

## 주요 추가/수정 파일

백엔드:

- `application/api/FrontendController.java`
- `application/api/stock/StockScreenerController.java`
- `domain/stock/StockScreenerService.java`
- `domain/stock/StockThemeRegistry.java`
- `domain/stock/StockCandleCollectService.java`
- `domain/stock/StockPriceProvider.java`
- `domain/stock/model/StockCandleEntity.java`
- `domain/stock/model/StockCandleSeries.java`
- `domain/stock/model/StockCandleCollectResult.java`
- `domain/stock/model/ThemeCandidate.java`
- `domain/stock/model/ThemeScreenerResult.java`
- `infrastructure/stock/KisStockPriceProvider.java`
- `infrastructure/security/SecurityConfig.java`
- `application.properties`

프론트:

- `frontend/src/components/StockPage.js`
- `frontend/src/components/StockPage.css`
- `frontend/src/components/StockScreenerPage.js`
- `frontend/src/components/TransactionList.js`
- `frontend/src/App.js`
- `frontend/src/services/api.js`

## 다음에 할 일

### 1. 스크리너를 백그라운드 Job 구조로 변경

현재 스크리너는 HTTP 요청 하나가 끝날 때까지 화면이 기다리는 동기 방식이다.

다음 구조로 바꾸는 것이 좋다.

```http
POST /api/stocks/screener/jobs
GET /api/stocks/screener/jobs/{jobId}
GET /api/stocks/screener/jobs/{jobId}/results
```

기대 효과:

- insert 중 화면 커넥션이 끊기지 않는다.
- 긴 작업을 안정적으로 처리할 수 있다.
- 진행률을 별도 조회할 수 있다.

### 2. 진행률 표시 추가

화면에서 다음 정보를 표시한다.

- 전체 후보 종목 수
- 완료 종목 수
- 현재 처리 중인 종목
- 실패 종목
- 성공/실패 메시지

예:

```text
17개 중 5개 완료
현재 처리 중: SK하이닉스(000660)
```

### 3. 저장된 캔들 조회 API 추가

상세 화면에서 실제 저장된 일봉 데이터를 볼 수 있게 한다.

예상 API:

```http
GET /api/stocks/{stockCode}/candles?limit=60
```

표시 데이터:

- 날짜
- 시가
- 고가
- 저가
- 종가
- 거래량

이후 차트 화면으로 확장할 수 있다.

### 4. 테마/후보 종목을 DB 관리로 변경

현재 테마 후보군은 `StockThemeRegistry`에 하드코딩되어 있다.

다음 단계:

- `themes` 테이블 추가
- `theme_stocks` 테이블 추가
- 화면에서 테마별 후보 종목 관리
- 최근 테마 후보를 수동/자동으로 업데이트

### 5. 스크리너 기준 고도화

현재는 이동평균과 거래량 기반의 기존 `StockSignalService`를 사용한다.

추가할 수 있는 기준:

- 최근 N일 신고가
- 거래량 급증률
- 5일선/20일선 골든크로스
- 전고점 돌파
- 하락 추세 제외
- 거래대금 기준 필터
- 위험 종목 제외 조건

### 6. KIS 호출 캐싱 및 중복 수집 방지

이미 오늘 수집한 종목은 다시 KIS API를 호출하지 않고 DB 데이터를 사용하도록 한다.

기대 효과:

- KIS 초당 거래건수 제한 완화
- 스크리너 실행 시간 단축
- 불필요한 DB upsert 감소

## 우선순위

1. 스크리너 백그라운드 Job 구조
2. 진행률 표시
3. 저장된 캔들 조회 API 및 상세 화면 표/차트
4. 오늘 수집 여부 체크로 KIS 중복 호출 방지
5. 테마/종목 후보 DB 관리
