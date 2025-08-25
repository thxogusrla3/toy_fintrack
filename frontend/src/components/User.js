import {useEffect, useState} from "react";
import axios from "axios";

function User() {
    const [userName, setUserName] = useState("");
    const [findUserName, setFindUserName] = useState("");
    const [allUser, setAllUser] = useState([]);

    const onChangeUserName = (e) => {
        e.preventDefault();
        setUserName(e.target.value);
    }
    const onClickButton = async (e) => {
        await axios.get("/api/users/find_user", {
            params: {
                userName: userName
            }
        })
            .then(res => {
                setFindUserName(res.data);
            })
    }

    useEffect(() => {
        // 백엔드 API 호출
        axios.get('http://localhost:8080/api/users/all')
            .then(res => {
                console.log(res);
                setAllUser(res.data);
            })
            .catch(err => {
                console.error('Error fetching users:', err);
            });
    }, []);



    return (
        <div className="userInfo">
            <div className="search">
                <input type="text" value={userName} onChange={onChangeUserName}></input>
                <button type="submit" onClick={onClickButton}>클릭!</button>
            </div>
            <div className="targetUser">
                조회한사람 = {userName};
            </div>
            <div className="findUser">
                찾은사람 = {findUserName};
            </div>

            <div className="p-4">
                <h2 className="text-xl font-bold mb-4">사용자 목록</h2>
                <table className="w-full border">
                    <thead className="bg-gray-100">
                    <tr>
                        <th className="p-2 border">ID</th>
                        <th className="p-2 border">이름</th>
                        <th className="p-2 border">이메일</th>
                    </tr>
                    </thead>
                    <tbody>
                    {allUser.map(user => (
                        <tr key={user.id}>
                            <td className="p-2 border">{user.id}</td>
                            <td className="p-2 border">{user.userName}</td>
                            <td className="p-2 border">{user.email}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>



    )
}

export default User;