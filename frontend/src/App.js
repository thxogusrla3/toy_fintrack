import React, {useEffect, useState} from 'react';
import {Route, Routes} from "react-router-dom";
import Test from "./components/Test";
import Sample from "./components/Sample"
import User from "./components/User";
import LoginPage from "./components/LoginPage";
import RegisterPage from "./components/RegisterPage";
import AdminPage from "./components/AdminPage";
import GuestPage from "./components/GuestPage";
function App() {
  return (
      <Routes>
          <Route path='/sample' element={<Sample />} ></Route>
          <Route path='/user' element={<User />} ></Route>
          <Route path='/register' element={<RegisterPage />} ></Route>
          <Route path='/admin' element={<AdminPage />} ></Route>
          <Route path='/guest' element={<GuestPage />} ></Route>
          <Route path='/' element={<LoginPage />} ></Route>
      </Routes>
  );
}

export default App;
/*
* App.js 는 프론트의 Router
* */