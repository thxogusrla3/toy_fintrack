import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './StockPage.css';

const today = new Date().toISOString().slice(0, 10);
const signalOptions = [
  { value: '', label: '전체 신호' },
  { value: 'BUY', label: 'BUY' },
  { value: 'HOLD', label: 'HOLD' },
  { value: 'DANGER', label: 'DANGER' },
  { value: 'NONE', label: 'NONE' }
];

function defaultFromDate(days = 90) {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date.toISOString().slice(0, 10);
}

function StockThemeSignalPage() {
  const navigate = useNavigate();
  const [themes, setThemes] = useState([]);
  const [themeKey, setThemeKey] = useState('all');
  const [signal, setSignal] = useState('');
  const [from, setFrom] = useState(defaultFromDate());
  const [to, setTo] = useState(today);
  const [limit, setLimit] = useState(60);
  const [collect, setCollect] = useState(false);
  const [results, setResults] = useState([]);
  const [loadingThemes, setLoadingThemes] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    loadThemes();
  }, []);

  const themeOptions = useMemo(() => {
    const uniqueThemes = new Map();
    themes.forEach((theme) => {
      if (theme.themeKey && !uniqueThemes.has(theme.themeKey)) {
        uniqueThemes.set(theme.themeKey, theme.themeName || theme.themeKey);
      }
    });
    return Array.from(uniqueThemes.entries()).map(([value, label]) => ({ value, label }));
  }, [themes]);

  const loadThemes = async () => {
    setLoadingThemes(true);
    try {
      const response = await api.get('/stocks/screener/themes', { timeout: 120000 });
      const nextThemes = response.data || [];
      setThemes(nextThemes);
      setThemeKey((current) => {
        if (current !== 'all') {
          return current;
        }
        return nextThemes.length === 1 ? nextThemes[0].themeKey : 'all';
      });
    } catch (error) {
      setMessage(error.response?.data?.message || '테마 목록을 불러오지 못했습니다.');
    } finally {
      setLoadingThemes(false);
    }
  };

  const searchSignals = async (event) => {
    if (event) {
      event.preventDefault();
    }
    setLoading(true);
    setMessage('테마 종목 신호를 조회 중입니다.');

    try {
      const params = { themeKey, from, to, limit, collect };
      if (signal) {
        params.signal = signal;
      }
      const response = await api.get('/stocks/screener/theme-signals', {
        params,
        timeout: 300000
      });
      setResults(response.data || []);
      setMessage('테마 종목 신호 조회가 완료되었습니다.');
    } catch (error) {
      setMessage(error.response?.data?.message || '테마 종목 신호 조회에 실패했습니다.');
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
          <h1>테마 종목 신호 조회</h1>
          <p>저장된 테마 전체 또는 특정 테마의 종목을 신호 조건으로 필터링합니다.</p>
        </div>
        <div className="stock-toolbar-actions">
          <Link className="stock-nav-link" to="/stocks/screener">테마 탐색</Link>
          <Link className="stock-nav-link" to="/stocks">종목 상세</Link>
        </div>
      </section>

      <form className="stock-panel theme-signal-controls" onSubmit={searchSignals}>
        <label>
          테마
          <select value={themeKey} onChange={(event) => setThemeKey(event.target.value)} disabled={loadingThemes}>
            <option value="all">전체 테마</option>
            {themeOptions.map((theme) => (
              <option key={theme.value} value={theme.value}>{theme.label}</option>
            ))}
          </select>
        </label>

        <label>
          신호
          <select value={signal} onChange={(event) => setSignal(event.target.value)}>
            {signalOptions.map((option) => (
              <option key={option.value || 'all'} value={option.value}>{option.label}</option>
            ))}
          </select>
        </label>

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
          조회 시 KIS 데이터 수집
        </label>

        <button type="submit" disabled={loading || loadingThemes}>{loading ? '조회 중' : '조회'}</button>
      </form>

      {message && <p className="stock-message screener-message">{message}</p>}

      <section className="stock-panel screener-table-panel theme-signal-results">
        <div className="stock-panel-header">
          <h2>종목 리스트</h2>
          <span className="result-count">{results.length.toLocaleString()}건</span>
        </div>
        <table className="screener-table">
          <thead>
            <tr>
              <th>테마</th>
              <th>종목</th>
              <th>신호</th>
              <th>점수</th>
              <th>수집</th>
              <th>주요 사유</th>
            </tr>
          </thead>
          <tbody>
            {results.map((item) => (
              <tr key={`${item.themeKey}-${item.stockCode}`} onClick={() => openDetail(item.stockCode)}>
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
                <td colSpan="6" className="screener-empty">조회 결과가 없습니다.</td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </main>
  );
}

export default StockThemeSignalPage;
