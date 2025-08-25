# 라우트 설정
- npm install react-router-dom (roter 기능 추가)
- (react경로기준) src/App.js

```javascript
import React, {useEffect, useState} from 'react';
import {Route, Routes} from "react-router-dom";
import Test from "./components/Test";
import Sample from "./components/Sample"
import User from "./components/User";
import LoginPage from "./components/LoginPage";
function App() {
  return (
      <Routes>
          <Route path='/sample' element={<Sample />} ></Route>
          <Route path='/user' element={<User />} ></Route>
          <Route path='/login' element={<LoginPage />} ></Route>
      </Routes>
  );
}

export default App;
```

# 화면 추가
- src/components 생성 후 해당 경로에 .js 파일 생성