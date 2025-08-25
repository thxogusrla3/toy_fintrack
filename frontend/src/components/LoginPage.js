import React, { useState } from 'react';
import { jwtDecode } from 'jwt-decode';

import { useNavigate } from 'react-router-dom';
import axios from "axios";

function LoginPage() {
    const [userName, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();
    const goToRegister = () => {
        navigate("/register");
    }

    const handleSubmit = async (e) => {
        e.preventDefault();

        const response = await axios.post("/api/auth/login", {
            userName,
            password,
        });

        if(response.status === 200) {
            const token = response.data.token;
            const decoded = jwtDecode(token);

            localStorage.setItem('token', token);

            if (decoded.authorities?.includes("ROLE_ADMIN")) {
                navigate("/admin");
            } else {
                navigate("/guest");
            }
        }
    };


    return (
        <form onSubmit={handleSubmit}>
            <div>
                <label>Id: </label>
                <input
                    type="text"
                    value={userName}
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