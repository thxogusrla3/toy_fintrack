import axios from 'axios';

export async function bootstrapAuth() {
  try {
    const r = await axios.post("/api/auth/refresh", null, { withCredentials: true });
    tokenStore.set(r.data.accessToken);
    console.log("부트스트랩 성공, 새 Access 발급:", r.data.accessToken);
    return true;
  } catch (e) {
    console.log("부트스트랩 실패, 로그인 필요");
    tokenStore.clear();
    
    return false;
  }
}

let accessToken = null;
export const tokenStore = {
  get: () => accessToken,
  set: (t) => { accessToken = t; },
  clear: () => { accessToken = null; }
};

const api = axios.create({
  baseURL: "/api",         // 백엔드 프리픽스
  withCredentials: true,   // Refresh 쿠키 전달 필수!
  timeout: 10000
});

// 요청 인터셉터: Access 헤더 부착
api.interceptors.request.use((config) => {
  const token = tokenStore.get();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let isRefreshing = false;
let pending = [];

function onRefreshed(newToken) {
  pending.forEach(cb => cb(newToken));
  pending = [];
}

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const { config, response } = error;
    if (!response) return Promise.reject(error); // 네트워크 에러 등

    // 이미 재시도한 요청이면 포기
    if (response.status === 401 && !config.__retry) {
      if (!isRefreshing) {
        isRefreshing = true;
        try {
          const r = await axios.post("/api/auth/refresh", null, { withCredentials: true });
          const newToken = r.data.accessToken;
          tokenStore.set(newToken);
          isRefreshing = false;
          onRefreshed(newToken);
        } catch (e) {
          isRefreshing = false;
          tokenStore.clear();
          window.location.href = "/"; 
          // 필요 시 라우팅 이동 처리
          // window.location.href = "/login";
          return Promise.reject(e);
        }
      }
      // 새 토큰 적용 후 원요청 재시도
      return new Promise((resolve) => {
        pending.push((newToken) => {
          config.headers.Authorization = `Bearer ${newToken}`;
          config.__retry = true;
          resolve(api(config));
        });
      });
    }

    return Promise.reject(error);
  }
);
export default api;