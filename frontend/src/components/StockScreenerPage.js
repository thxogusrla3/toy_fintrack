import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './StockPage.css';

const today = new Date().toISOString().slice(0, 10);

function defaultFromDate(days = 14) {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date.toISOString().slice(0, 10);
}

function StockScreenerPage() {
  const navigate = useNavigate();
  const [includeSearch, setIncludeSearch] = useState(true);
  const [trendResults, setTrendResults] = useState([]);
  const [discoveredThemes, setDiscoveredThemes] = useState([]);
  const [trendLoading, setTrendLoading] = useState(false);
  const [discoverLoading, setDiscoverLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    loadTrendingThemes();
    loadStoredDiscoveredThemes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadTrendingThemes = async () => {
    setTrendLoading(true);
    try {
      const response = await api.get('/stocks/themes/trending', {
        params: {
          from: defaultFromDate(14),
          to: today,
          limit: 10,
          includeSearch
        },
        timeout: 300000
      });
      setTrendResults(response.data || []);
    } catch (error) {
      setMessage(error.response?.data?.message || '최근 주도 테마를 불러오지 못했습니다.');
    } finally {
      setTrendLoading(false);
    }
  };

  const loadDiscoveredThemes = async () => {
    setDiscoverLoading(true);
    try {
      const response = await api.get('/stocks/themes/discover', {
        params: { limit: 12 },
        timeout: 300000
      });
      setDiscoveredThemes(response.data || []);
      await loadTrendingThemes();
    } catch (error) {
      setMessage(error.response?.data?.message || '자동 테마 후보를 수집하지 못했습니다.');
    } finally {
      setDiscoverLoading(false);
    }
  };

  const loadStoredDiscoveredThemes = async () => {
    try {
      const response = await api.get('/stocks/themes/discovered', {
        params: { limit: 12 },
        timeout: 300000
      });
      setDiscoveredThemes(response.data || []);
    } catch (error) {
      setMessage(error.response?.data?.message || '저장된 테마 후보를 불러오지 못했습니다.');
    }
  };

  const openScreenerRun = (theme) => {
    const params = new URLSearchParams({
      themeKey: theme.themeKey,
      themeName: theme.themeName
    });
    navigate(`/stocks/screener/run?${params.toString()}`);
  };

  return (
    <main className="stock-page">
      <section className="stock-toolbar">
        <div>
          <h1>테마 탐색</h1>
          <p>최근 주도 테마와 자동 발견 후보를 확인하고, 선택한 테마의 스크리너를 실행합니다.</p>
        </div>
        <Link className="stock-nav-link" to="/stocks">종목 상세</Link>
      </section>

      <section className="stock-panel trend-panel">
        <div className="stock-panel-header">
          <div>
            <h2>최근 주도 테마</h2>
            <p>최근 14일 기준 검색 관심도, 거래량 변화, 등락률을 합산한 순위입니다.</p>
          </div>
          <label className="stock-checkbox trend-search-toggle">
            <input
              type="checkbox"
              checked={includeSearch}
              onChange={(event) => setIncludeSearch(event.target.checked)}
            />
            검색 관심도 포함
          </label>
          <button className="trend-refresh-button" type="button" onClick={loadTrendingThemes} disabled={trendLoading}>
            {trendLoading ? '조회 중' : '새로고침'}
          </button>
        </div>

        <div className="trend-list">
          {trendResults.map((theme, index) => (
            <button
              type="button"
              className="trend-item"
              key={theme.themeKey}
              onClick={() => openScreenerRun(theme)}
            >
              <span className="trend-rank">{index + 1}</span>
              <span className="trend-main">
                <strong>{theme.themeName}</strong>
                <span>{theme.reasons?.join(' · ')}</span>
              </span>
              <span className="trend-score">{theme.totalScore}</span>
              <span className="trend-metrics">
                검색 {theme.searchScore} · 수급 {theme.flowScore} · 가격 {theme.priceScore}
              </span>
            </button>
          ))}
          {!trendLoading && trendResults.length === 0 && (
            <p className="stock-empty">최근 주도 테마 결과가 없습니다.</p>
          )}
        </div>
      </section>

      <section className="stock-panel discovery-panel">
        <div className="stock-panel-header">
          <div>
            <h2>자동 발견 테마 후보</h2>
            <p>최근 뉴스에서 반복 언급된 테마 키워드와 매칭 종목을 저장합니다.</p>
          </div>
          <button className="trend-refresh-button" type="button" onClick={loadDiscoveredThemes} disabled={discoverLoading}>
            {discoverLoading ? '수집 중' : '후보 수집'}
          </button>
        </div>

        <div className="discovery-list">
          {discoveredThemes.map((theme) => (
            <article className="discovery-item" key={theme.themeKey}>
              <div className="discovery-header">
                <strong>{theme.themeName}</strong>
                <span>{theme.score}</span>
              </div>
              <p>언급 {theme.mentionCount}회 · 매칭 종목 {theme.matchedStockCount}개</p>
              {theme.matchedStocks?.length > 0 && (
                <p className="discovery-stocks">{theme.matchedStocks.join(', ')}</p>
              )}
              {theme.evidence?.[0] && (
                <p className="discovery-evidence">{theme.evidence[0]}</p>
              )}
            </article>
          ))}
          {!discoverLoading && discoveredThemes.length === 0 && (
            <p className="stock-empty">자동 발견된 테마 후보가 없습니다.</p>
          )}
        </div>
      </section>

      {message && <p className="stock-message screener-message">{message}</p>}
    </main>
  );
}

export default StockScreenerPage;
