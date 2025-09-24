CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name   VARCHAR2(64) NOT NULL UNIQUE,
    password    VARCHAR2(100) NOT NULL,
    email       VARCHAR2(255) NOT NULL,
    role        VARCHAR2(20) NOT NULL,
    create_at   DATETIME NOT NULL,
    primary key (id),
    CONSTRAINT uk_users_username UNIQUE  (user_name),
    CONSTRAINT uk_users_email UNIQUE (email)
);