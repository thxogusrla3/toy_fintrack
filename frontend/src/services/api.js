import axios from 'axios';

const api = axios.create({
  baseURL: '/api', // 백엔드 API의 기본 URL
  withCredentials: true, // 모든 요청에 쿠키를 자동으로 포함
});

export default api;