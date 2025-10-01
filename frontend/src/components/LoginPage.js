import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api, {tokenStore} from '../services/api'; // 생성한 axios 인스턴스 import
import commonStyle from "../styles/commonStyles";

function LoginPage({ onLoginSuccess }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();
    const goToRegister = () => {
        navigate("/register");
    }

    const handleSubmit = async (e) => {
        e.preventDefault();

        const response = await api.post("/auth/login", {
            username,
            password,
        });

        if(response.status === 200) {
            tokenStore.set(response.data.accessToken);
            if (onLoginSuccess) { 
                onLoginSuccess();
            }
            navigate("/transaction/list");
        }
    };


    return (
      <div style={commonStyle.container}>
        <h2>로그인</h2>
        <form id='login-form' onSubmit={handleSubmit} style={commonStyle.form}>
                <input
                    type="text"
                    placeholder="아이디"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    style={commonStyle.input}
                />
                <input
                    type="password"
                    placeholder="비밀번호"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    style={commonStyle.input}
                />
            <button type="submit" style={commonStyle.button}>Login</button>

            <p onClick={goToRegister} style={{cursor: 'pointer', color: 'blue'}}>
                회원가입
            </p>
        </form>
      </div>
    );
}

export default LoginPage