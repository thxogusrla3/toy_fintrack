import React, {useEffect, useState} from 'react';
import {Route, Routes, useNavigate} from "react-router-dom";
import Sample from "./components/Sample"
import User from "./components/User";
import LoginPage from "./components/LoginPage";
import RegisterPage from "./components/RegisterPage";
import AdminPage from "./components/AdminPage";
import TransactionList from "./components/TransactionList";
import { bootstrapAuth } from './services/api';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isBootstrapping, setIsBootstrapping] = useState(true);

  useEffect(() => {
    // 페이지 새로고침 시 기존 로그인 상태 유지 처리
    bootstrapAuth().then((loggedIn) => {
      setIsLoggedIn(loggedIn);
      setIsBootstrapping(false);
    });
  }, []);

  const handleLoginSuccess = () => {
    setIsLoggedIn(true);
  }

  if(isBootstrapping) {
    return <div style={{padding: '20px', textAlign: 'center'}}>Loading...</div>; 
  }

  return (
      <Routes>
          <Route path='/sample' element={<Sample />} ></Route>
          <Route path='/user' element={<User />} ></Route>
          <Route path='/register' element={<RegisterPage />} ></Route>
          <Route path='/admin' element={<AdminPage />} ></Route>
          <Route path='/transaction/list' element={<TransactionList />} ></Route>

          <Route path='/' element={<ProtectedRouteForRoot isLoggedIn={isLoggedIn} onLoginSuccess={handleLoginSuccess} />} ></Route>
      </Routes>
  );
}

function ProtectedRouteForRoot({ isLoggedIn, onLoginSuccess }) {
  const navigate = useNavigate();

  useEffect(() => {
    if(isLoggedIn) {
      navigate("/transaction/list", {replace: true });
    }
  }, [isLoggedIn, navigate]);
  
  return !isLoggedIn ? <LoginPage onLoginSuccess={onLoginSuccess} /> : null;
}
export default App;
/*
* App.js 는 프론트의 Router
* */