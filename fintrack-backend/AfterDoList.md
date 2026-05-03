# After Do List

## 현재 상태

- 주식 매매 기준 문서 저장 완료
  - `docs/stock-trading-rule.md`
- 주식 시그널 엔진 구현 완료
  - `StockSignalService`
  - `StockCandle`
  - `TradeSignal`
  - `SignalType`
- 시그널 직접 분석 API 구현 완료
  - `POST /api/stocks/signals/analyze`
- 일봉 수집/저장/분석 흐름 구현 완료
  - `POST /api/stocks/{stockCode}/candles/collect`
  - `GET /api/stocks/{stockCode}/signals`
- `stock_candles` 테이블 저장 구조 구현 완료
  - 같은 `stockCode + date`는 중복 저장하지 않고 업데이트
- Mock provider 기준 로컬 H2 저장 확인 완료
  - `000660`
  - `collectedCount = 21`
  - `totalCount = 21`
  - `signal = BUY`
- KIS provider 구현 완료
  - access token 발급
  - 국내주식 일봉 조회 API 호출
  - KIS 응답을 `StockCandle`로 변환
- 전체 테스트 통과 확인 완료
  - `.\gradlew.bat test`

## 다음에 할 일

### 1. KIS API 키 발급

한국투자증권 Open API 포털에서 API 키를 발급받는다.

- https://apiportal.koreainvestment.com/

필요한 값:

- `APP_KEY`
- `APP_SECRET`
- `ACCOUNT_NO` (나중에 주문/잔고 조회까지 할 경우 필요)

### 2. 로컬 환경변수 설정

PowerShell에서 아래 값을 설정한다.

```powershell
$env:STOCK_PROVIDER="kis"
$env:KIS_APP_KEY="발급받은_APP_KEY"
$env:KIS_APP_SECRET="발급받은_APP_SECRET"
$env:KIS_ACCOUNT_NO="12345678-01"
```

주의:

- API 키는 Git에 커밋하지 않는다.
- `application.properties`에 직접 키를 적지 않는다.

### 3. 서버 실행

```powershell
.\gradlew.bat bootRun
```

### 4. 실제 KIS 데이터 수집 테스트

로그인 후 access token을 받은 상태에서 아래 API를 호출한다.

```http
POST /api/stocks/000660/candles/collect?from=2026-01-01&to=2026-01-31
```

예상 결과:

```json
{
  "stockCode": "000660",
  "collectedCount": 0,
  "totalCount": 0
}
```

`collectedCount`, `totalCount`는 실제 KIS 응답에 따라 달라진다.

### 5. 저장된 데이터 기반 시그널 확인

```http
GET /api/stocks/000660/signals
```

응답 예:

```json
{
  "signal": "BUY",
  "reasons": []
}
```

### 6. 확인할 포인트

- KIS access token 발급 성공 여부
- 일봉 조회 API 응답이 정상인지
- `stock_candles` 테이블에 실제 데이터가 저장되는지
- 저장된 데이터로 시그널이 계산되는지
- 날짜 범위가 휴장일/주말을 포함할 때 응답이 어떻게 오는지

## 이후 개발 후보

- 실제 KIS 데이터 수집 성공 후 응답 로그 정리
- 종목명/종목코드 관리 테이블 추가
- 여러 종목 일괄 수집 API 추가
- 스케줄러로 장마감 후 자동 수집
- 시그널 결과 저장 테이블 추가
- 프론트에서 종목코드 입력 후 수집/분석하는 화면 추가
- PostgreSQL 전환 검토
- Kafka는 실시간 체결/호가 또는 알림 기능이 필요해질 때 도입 검토
