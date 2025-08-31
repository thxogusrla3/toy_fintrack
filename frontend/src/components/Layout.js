import React from "react";
import { Outlet, useNavigate } from "react-router-dom";
import axios from "axios";

const Layout = () => {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await axios.post("/api/auth/logout", {}, { withCredentials: true });
            localStorage.removeItem("token");
            navigate("/login");
        } catch (error) {
            console.error("로그아웃 실패:", error);
        }
    };

    return (
        <div>
            <header style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "#eee" }}>
                <h2>FinTrack</h2>
                <button onClick={handleLogout}>로그아웃</button>
            </header>

            <main style={{ padding: "20px" }}>
                <Outlet /> {/* 여기에 각 페이지가 들어감 */}
            </main>
        </div>
    );
};

export default Layout;