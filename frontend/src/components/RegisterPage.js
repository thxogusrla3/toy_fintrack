import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from "axios";


function  RegisterPage() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        userName: "",
        password: "",
        email: ""
    });
    const goToHome = () => {
        navigate("/");
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const res = await fetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData),
            });

            if (res.ok) {
                alert('회원가입 성공!');
                goToHome();
                // 로그인 페이지로 이동하거나 다른 행동
            } else {
                const errorMessage = await res.text();
                alert('회원가입 실패:' + errorMessage);
            }
        } catch (error) {
            console.error('회원가입 중 오류:', error);
        }
    };


    const handleChange = (e) => {
        console.log(e.target)
        const {name, value} = e.target;
        setFormData(prev => ({...prev, [name]: value}));
    }


    return (
        <div style={styles.container}>
            <h2>회원가입</h2>
            <form onSubmit={handleSubmit} style={styles.form}>
                <input
                    type="text"
                    name="userName"
                    placeholder="아이디"
                    value={formData.userName}
                    onChange={handleChange}
                    required
                    style={styles.input}
                />
                <input
                    type="password"
                    name="password"
                    placeholder="비밀번호"
                    value={formData.password}
                    onChange={handleChange}
                    required
                    style={styles.input}
                />
                <input
                    type="email"
                    name="email"
                    placeholder="이메일"
                    value={formData.email}
                    onChange={handleChange}
                    required
                    style={styles.input}
                />
                <button type="submit" style={styles.button}>회원가입</button>
            </form>
        </div>
    );
};

const styles = {
    container: {
        maxWidth: '400px',
        margin: '50px auto',
        padding: '20px',
        border: '1px solid #ccc',
        borderRadius: '10px',
        textAlign: 'center'
    },
    form: {
        display: 'flex',
        flexDirection: 'column',
        gap: '10px'
    },
    input: {
        padding: '10px',
        fontSize: '16px'
    },
    button: {
        padding: '10px',
        fontSize: '16px',
        backgroundColor: '#007bff',
        color: 'white',
        border: 'none',
        borderRadius: '5px'
    }
};
export default RegisterPage;