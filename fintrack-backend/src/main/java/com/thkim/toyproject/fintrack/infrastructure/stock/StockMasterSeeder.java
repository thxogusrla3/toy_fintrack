package com.thkim.toyproject.fintrack.infrastructure.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.StockMasterEntity;
import com.thkim.toyproject.fintrack.domain.stock.repository.StockMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StockMasterSeeder implements CommandLineRunner {
    private final StockMasterRepository stockMasterRepository;

    @Override
    public void run(String... args) {
        for (SeedStock stock : seeds()) {
            stockMasterRepository.findByStockCode(stock.stockCode())
                    .ifPresentOrElse(
                            entity -> entity.update(stock.stockName(), stock.themeKey(), stock.themeName(), stock.keywords()),
                            () -> stockMasterRepository.save(StockMasterEntity.of(
                                    stock.stockCode(),
                                    stock.stockName(),
                                    stock.themeKey(),
                                    stock.themeName(),
                                    stock.keywords()
                            ))
                    );
        }
    }

    private List<SeedStock> seeds() {
        return List.of(
                stock("005930", "삼성전자", "ai-semiconductor", "AI 반도체", "AI 반도체", "HBM", "반도체", "온디바이스 AI"),
                stock("000660", "SK하이닉스", "ai-semiconductor", "AI 반도체", "AI 반도체", "HBM", "반도체"),
                stock("042700", "한미반도체", "ai-semiconductor", "AI 반도체", "HBM", "반도체 장비", "AI 반도체"),
                stock("007660", "이수페타시스", "ai-semiconductor", "AI 반도체", "PCB", "AI 서버", "반도체"),
                stock("058470", "리노공업", "ai-semiconductor", "AI 반도체", "반도체 테스트", "AI 반도체"),

                stock("373220", "LG에너지솔루션", "battery", "2차전지", "2차전지", "배터리", "전기차"),
                stock("006400", "삼성SDI", "battery", "2차전지", "2차전지", "배터리", "전고체"),
                stock("247540", "에코프로비엠", "battery", "2차전지", "양극재", "2차전지", "배터리"),
                stock("086520", "에코프로", "battery", "2차전지", "2차전지", "양극재", "배터리"),
                stock("003670", "포스코퓨처엠", "battery", "2차전지", "양극재", "음극재", "배터리"),

                stock("329180", "HD현대중공업", "shipbuilding", "조선", "조선", "LNG선", "수주"),
                stock("009540", "HD한국조선해양", "shipbuilding", "조선", "조선", "LNG선", "수주"),
                stock("042660", "한화오션", "shipbuilding", "조선", "조선", "방산 선박", "LNG선"),
                stock("010140", "삼성중공업", "shipbuilding", "조선", "조선", "LNG선", "수주"),

                stock("207940", "삼성바이오로직스", "bio", "제약·바이오", "바이오", "제약", "신약"),
                stock("068270", "셀트리온", "bio", "제약·바이오", "바이오", "제약", "신약"),
                stock("000100", "유한양행", "bio", "제약·바이오", "제약", "신약", "비만치료제"),
                stock("196170", "알테오젠", "bio", "제약·바이오", "바이오", "신약"),

                stock("267260", "HD현대일렉트릭", "power-robot", "전력·로봇", "전력기기", "변압기", "전력망"),
                stock("010120", "LS ELECTRIC", "power-robot", "전력·로봇", "전력기기", "전력망", "변압기"),
                stock("454910", "두산로보틱스", "power-robot", "전력·로봇", "로봇", "휴머노이드"),
                stock("108490", "로보티즈", "power-robot", "전력·로봇", "로봇", "휴머노이드"),

                stock("034020", "두산에너빌리티", "nuclear", "원전", "원전", "SMR", "전력"),
                stock("052690", "한전기술", "nuclear", "원전", "원전", "SMR"),
                stock("051600", "한전KPS", "nuclear", "원전", "원전", "전력"),
                stock("112610", "씨에스윈드", "nuclear", "원전", "에너지", "전력"),

                stock("012450", "한화에어로스페이스", "defense", "방산", "방산", "우주항공", "항공"),
                stock("079550", "LIG넥스원", "defense", "방산", "방산", "미사일"),
                stock("047810", "한국항공우주", "defense", "방산", "방산", "우주항공", "항공"),
                stock("064350", "현대로템", "defense", "방산", "방산", "방위산업"),

                stock("090430", "아모레퍼시픽", "cosmetics", "화장품", "화장품", "중국 소비", "K뷰티"),
                stock("051900", "LG생활건강", "cosmetics", "화장품", "화장품", "K뷰티"),
                stock("161890", "한국콜마", "cosmetics", "화장품", "화장품", "ODM", "K뷰티"),
                stock("192820", "코스맥스", "cosmetics", "화장품", "화장품", "ODM", "K뷰티"),

                stock("035720", "카카오", "crypto-security", "가상자산·보안", "가상자산", "핀테크", "보안"),
                stock("035420", "NAVER", "crypto-security", "가상자산·보안", "보안", "핀테크", "AI"),
                stock("053800", "안랩", "crypto-security", "가상자산·보안", "보안", "사이버보안"),
                stock("064260", "다날", "crypto-security", "가상자산·보안", "가상자산", "결제", "핀테크")
        );
    }

    private SeedStock stock(String stockCode, String stockName, String themeKey, String themeName, String... keywords) {
        return new SeedStock(stockCode, stockName, themeKey, themeName, List.of(keywords));
    }

    record SeedStock(
            String stockCode,
            String stockName,
            String themeKey,
            String themeName,
            List<String> keywords
    ) {
    }
}
