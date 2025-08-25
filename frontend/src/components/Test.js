import React, {useEffect, useState} from 'react';
import axios from 'axios';

function Test() {

    const [data, setData] = useState('')

    useEffect(() => {
        axios.get('/home')
            .then(res => setData(res.data))
            .catch(err => console.log(err))
    }, []);


    return (
        <div className="login-container">
            <h2>로그인</h2>
            <form >
                <div>
                    <label htmlFor="username">사용자 이름:</label>
                    <input
                        type="text"
                        id="username"
                    />
                </div>
                <div>
                    <label htmlFor="password">비밀번호:</label>
                    <input
                        type="password"
                        id="password"
                        required
                    />
                </div>
                <button type="submit">로그인</button>
            </form>
        </div>
    );
}

export default Test;
