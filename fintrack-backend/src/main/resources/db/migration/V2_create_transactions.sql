CREATE TABLE transactions(
    id          BIGINT  AUTO_INCREMENT NOT NULL PRIMARY KEY,
    user_id     BIGINT  NOT NULL,
    type        VARCHAR2(10) NOT NULL,
    category    VARCHAR2(50) NOT NULL,
    amount      DECIMAL(19, 2) NOT NULL,
    date        DATE    NOT NULL,
    memo        VARCHAR2(255),
    account     VARCHAR2(64),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users(id)
);