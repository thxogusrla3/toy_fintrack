# H2 database 생성
1) 우측 아이콘 h2 우클릭
2) create database ..
3) C:\Users\thxog\OneDrive\바탕 화면\스프링개발\h2\bin ; 해당 경로로 파일 생성

# H2 - Spring 연결
1) application.properties
```text
spring.datasource.url=jdbc:h2:tcp://localhost/~/db명 -> spring.datasource.url=jdbc:h2:file:C:/h2/bin/fintrack
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=설정값
spring.datasource.password=설정값
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.sql.init.platform=h2
```

# React 연동
- src/main 경로로 이동
- npx create-react-app 폴더명
- "proxy": "http://localhost:8080", 추가
- cd 폴더명 -> npm start
- npm install react-router-dom (roter 기능 추가)
- src/폴더명/package.json 에 dependencies에 react-router-dom 생겼는지 확인할 것


# Security init ID / Password
- id: user
- password:(서버콘솔확인) Using generated security password: 7f3a2a8e-xxxx-xxxx-xxxx-xxxxxxxxxxxx
