CREATE TABLE stock_master (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    stock_code VARCHAR2(20) NOT NULL,
    stock_name VARCHAR2(100) NOT NULL,
    theme_key VARCHAR2(100) NOT NULL,
    theme_name VARCHAR2(100) NOT NULL,
    keywords VARCHAR2(1000),
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_stock_master_code UNIQUE (stock_code)
);

CREATE INDEX ix_stock_master_name ON stock_master(stock_name);
CREATE INDEX ix_stock_master_theme_key ON stock_master(theme_key);
