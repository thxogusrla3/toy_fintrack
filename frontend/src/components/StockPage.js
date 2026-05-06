import React, { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import api from '../services/api';
import './StockPage.css';

const today = new Date().toISOString().slice(0, 10);

function defaultFromDate() {
  const date = new Date();
  date.setDate(date.getDate() - 60);
  return date.toISOString().slice(0, 10);
}

function StockPage() {
  const [searchParams] = useSearchParams();
  const [stockCode, setStockCode] = useState(searchParams.get('code') || '000660');
  const [from, setFrom] = useState(defaultFromDate());
  const [to, setTo] = useState(today);
  const [limit, setLimit] = useState(60);
  const [collectResult, setCollectResult] = useState(null);
  const [signalResult, setSignalResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const normalizedCode = stockCode.trim();

  const collectCandles = async (event) => {
    event.preventDefault();
    if (!normalizedCode) {
      setMessage('종목코드를 입력하세요.');
      return;
    }

    setLoading(true);
    setMessage('일봉 데이터를 수집 중입니다.');

    try {
      const response = await api.post(`/stocks/${normalizedCode}/candles/collect`, null, {
        params: { from, to },
        timeout: 120000
      });
      setCollectResult(response.data);
      setMessage('일봉 데이터를 수집했습니다.');
    } catch (error) {
      setMessage(error.response?.data?.message || '일봉 데이터 수집에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const analyzeStoredCandles = async () => {
    if (!normalizedCode) {
      setMessage('종목코드를 입력하세요.');
      return;
    }

    setLoading(true);
    setMessage('');

    try {
      const response = await api.get(`/stocks/${normalizedCode}/signals`, {
        params: { limit }
      });
      setSignalResult(response.data);
      setMessage('저장된 일봉 데이터로 신호를 분석했습니다.');
    } catch (error) {
      setMessage(error.response?.data?.message || '신호 분석에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const collectAndAnalyze = async (event) => {
    await collectCandles(event);
    if (normalizedCode) {
      await analyzeStoredCandles();
    }
  };

  return (
    <main className="stock-page">
      <section className="stock-toolbar">
        <div>
          <h1>주식 데이터</h1>
          <p>종목코드로 일봉을 수집하고 저장된 데이터의 매매 신호를 확인합니다.</p>
        </div>
        <Link className="stock-nav-link" to="/stocks/screener">테마 스크리너</Link>
      </section>

      <section className="stock-grid">
        <form className="stock-panel stock-form" onSubmit={collectCandles}>
          <label>
            종목코드
            <input
              value={stockCode}
              onChange={(event) => setStockCode(event.target.value)}
              placeholder="예: 000660"
              maxLength={20}
            />
          </label>

          <div className="stock-form-row">
            <label>
              시작일
              <input type="date" value={from} onChange={(event) => setFrom(event.target.value)} />
            </label>
            <label>
              종료일
              <input type="date" value={to} onChange={(event) => setTo(event.target.value)} />
            </label>
          </div>

          <label>
            분석 캔들 수
            <input
              type="number"
              min="20"
              max="60"
              value={limit}
              onChange={(event) => setLimit(Number(event.target.value))}
            />
          </label>

          <div className="stock-actions">
            <button type="submit" disabled={loading}>수집</button>
            <button type="button" disabled={loading} onClick={analyzeStoredCandles}>분석</button>
            <button type="button" disabled={loading} onClick={collectAndAnalyze}>수집 후 분석</button>
          </div>

          {message && <p className="stock-message">{message}</p>}
        </form>

        <section className="stock-panel">
          <h2>수집 결과</h2>
          {collectResult ? (
            <dl className="stock-result">
              <div>
                <dt>종목코드</dt>
                <dd>{collectResult.stockCode}</dd>
              </div>
              <div>
                <dt>종목명</dt>
                <dd>{collectResult.stockName || '-'}</dd>
              </div>
              <div>
                <dt>이번 수집</dt>
                <dd>{collectResult.collectedCount.toLocaleString()}건</dd>
              </div>
              <div>
                <dt>전체 저장</dt>
                <dd>{collectResult.totalCount.toLocaleString()}건</dd>
              </div>
            </dl>
          ) : (
            <p className="stock-empty">아직 수집 결과가 없습니다.</p>
          )}
        </section>

        <section className="stock-panel stock-signal-panel">
          <div className="stock-panel-header">
            <h2>분석 결과</h2>
            {signalResult?.signal && <span className={`signal-badge ${signalResult.signal.toLowerCase()}`}>{signalResult.signal}</span>}
          </div>

          {signalResult ? (
            <ul className="stock-reasons">
              {signalResult.reasons.map((reason, index) => (
                <li key={`${reason}-${index}`}>{reason}</li>
              ))}
            </ul>
          ) : (
            <p className="stock-empty">저장된 캔들을 분석하면 결과가 표시됩니다.</p>
          )}
        </section>
      </section>
    </main>
  );
}

export default StockPage;
