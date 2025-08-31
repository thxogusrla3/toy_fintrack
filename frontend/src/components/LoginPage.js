import React, { useState } from 'react';

import { useNavigate } from 'react-router-dom';
import api from '../services/api'; // 생성한 axios 인스턴스 import

function LoginPage() {
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
            const userRole = response.data.role;
            if (userRole.includes("ROLE_ADMIN")) {
                navigate("/admin");
            } else {
                navigate("/guest");
            }

            alert('로그인 성공!');
        }
    };


    return (
        <form onSubmit={handleSubmit}>
            <div>
                <label>Id: </label>
                <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
            </div>
            <div>
                <label>Password: </label>
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
            </div>
            <button type="submit">Login</button>

            <p onClick={goToRegister} style={{cursor: 'pointer', color: 'blue'}}>
                회원가입
            </p>
        </form>
    );
}

export default LoginPage