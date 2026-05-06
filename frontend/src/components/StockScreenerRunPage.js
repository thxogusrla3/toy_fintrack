import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import api from '../services/api';
import './StockPage.css';

const today = new Date().toISOString().slice(0, 10);

function defaultFromDate(days = 90) {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date.toISOString().slice(0, 10);
}

function StockScreenerRunPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const themeKey = searchParams.get('themeKey') || 'all';
  const themeName = searchParams.get('themeName') || '전체';
  const [from, setFrom] = useState(defaultFromDate());
  const [to, setTo] = useState(today);
  const [limit, setLimit] = useState(60);
  const [collect, setCollect] = useState(true);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    runScreener();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [themeKey]);

  const runScreener = async (event) => {
    if (event) {
      event.preventDefault();
    }
    setLoading(true);
    setMessage(`${themeName} 스크리너를 실행 중입니다.`);

    try {
      const response = await api.post('/stocks/screener/run', null, {
        params: { themeKey, from, to, limit, collect },
        timeout: 300000
      });
      setResults(response.data || []);
      setMessage(`${themeName} 스크리너를 실행했습니다.`);
    } catch (error) {
      setMessage(error.response?.data?.message || '스크리너 실행에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const openDetail = (stockCode) => {
    navigate(`/stocks?code=${stockCode}`);
  };

  return (
    <main className="stock-page">
      <section className="stock-toolbar">
        <div>
          <h1>{themeName} 스크리너</h1>
          <p>선택한 테마의 종목을 수집하고 매매 신호 기준으로 정렬합니다.</p>
        </div>
        <div className="stock-toolbar-actions">
          <Link className="stock-nav-link" to="/stocks/screener">테마 탐색</Link>
          <Link className="stock-nav-link" to="/stocks">종목 상세</Link>
        </div>
      </section>

      <form className="stock-panel screener-run-controls" onSubmit={runScreener}>
        <label>
          시작일
          <input type="date" value={from} onChange={(event) => setFrom(event.target.value)} />
        </label>

        <label>
          종료일
          <input type="date" value={to} onChange={(event) => setTo(event.target.value)} />
        </label>

        <label>
          분석 캔들 수
          <input type="number" min="20" max="60" value={limit} onChange={(event) => setLimit(Number(event.target.value))} />
        </label>

        <label className="stock-checkbox">
          <input type="checkbox" checked={collect} onChange={(event) => setCollect(event.target.checked)} />
          실행 전 KIS 데이터 수집
        </label>

        <button type="submit" disabled={loading}>{loading ? '실행 중' : '다시 실행'}</button>
      </form>

      {message && <p className="stock-message screener-message">{message}</p>}

      <section className="stock-panel screener-table-panel">
        <table className="screener-table">
          <thead>
            <tr>
              <th>통과</th>
              <th>테마</th>
              <th>종목</th>
              <th>신호</th>
              <th>점수</th>
              <th>수집</th>
              <th>주요 이유</th>
            </tr>
          </thead>
          <tbody>
            {results.map((item) => (
              <tr key={`${item.themeKey}-${item.stockCode}`} onClick={() => openDetail(item.stockCode)}>
                <td>{item.passed ? 'Y' : '-'}</td>
                <td>{item.themeName}</td>
                <td>
                  <strong>{item.stockName}</strong>
                  <span>{item.stockCode}</span>
                </td>
                <td><span className={`signal-badge ${item.signal.toLowerCase()}`}>{item.signal}</span></td>
                <td>{item.score}</td>
                <td>{item.collectedCount}</td>
                <td>{item.reasons?.[0] || '-'}</td>
              </tr>
            ))}
            {results.length === 0 && (
              <tr>
                <td colSpan="7" className="screener-empty">스크리너 실행 결과가 없습니다.</td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </main>
  );
}

export default StockScreenerRunPage;
