CREATE TABLE discovered_themes (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    theme_key VARCHAR2(100) NOT NULL,
    theme_name VARCHAR2(100) NOT NULL,
    score INT NOT NULL,
    mention_count INT NOT NULL,
    evidence VARCHAR2(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_discovered_themes_key UNIQUE (theme_key)
);

CREATE INDEX ix_discovered_themes_score ON discovered_themes(score);

CREATE TABLE discovered_theme_stocks (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    theme_id BIGINT NOT NULL,
    stock_code VARCHAR2(20) NOT NULL,
    stock_name VARCHAR2(100) NOT NULL,
    CONSTRAINT fk_discovered_theme_stocks_theme FOREIGN KEY (theme_id) REFERENCES discovered_themes(id),
    CONSTRAINT uk_discovered_theme_stocks_theme_code UNIQUE (theme_id, stock_code)
);
