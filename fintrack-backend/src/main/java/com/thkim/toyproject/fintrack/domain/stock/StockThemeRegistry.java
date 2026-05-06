package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.ThemeCandidate;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class StockThemeRegistry {
    private static final List<ThemeCandidate> CANDIDATES = List.of(
            new ThemeCandidate("ai-semiconductor", "AI 반도체", "005930", "삼성전자"),
            new ThemeCandidate("ai-semiconductor", "AI 반도체", "000660", "SK하이닉스"),
            new ThemeCandidate("ai-semiconductor", "AI 반도체", "042700", "한미반도체"),
            new ThemeCandidate("ai-semiconductor", "AI 반도체", "007660", "이수페타시스"),
            new ThemeCandidate("ai-semiconductor", "AI 반도체", "058470", "리노공업"),

            new ThemeCandidate("shipbuilding", "조선", "329180", "HD현대중공업"),
            new ThemeCandidate("shipbuilding", "조선", "009540", "HD한국조선해양"),
            new ThemeCandidate("shipbuilding", "조선", "042660", "한화오션"),
            new ThemeCandidate("shipbuilding", "조선", "010140", "삼성중공업"),

            new ThemeCandidate("bio", "제약·바이오", "207940", "삼성바이오로직스"),
            new ThemeCandidate("bio", "제약·바이오", "068270", "셀트리온"),
            new ThemeCandidate("bio", "제약·바이오", "000100", "유한양행"),
            new ThemeCandidate("bio", "제약·바이오", "196170", "알테오젠"),

            new ThemeCandidate("power-robot", "전력·로봇", "267260", "HD현대일렉트릭"),
            new ThemeCandidate("power-robot", "전력·로봇", "010120", "LS ELECTRIC"),
            new ThemeCandidate("power-robot", "전력·로봇", "454910", "두산로보틱스"),
            new ThemeCandidate("power-robot", "전력·로봇", "108490", "로보티즈")
    );

    public List<ThemeCandidate> findThemes() {
        return CANDIDATES.stream()
                .collect(java.util.stream.Collectors.toMap(
                        ThemeCandidate::themeKey,
                        candidate -> candidate,
                        (left, right) -> left
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(ThemeCandidate::themeName))
                .toList();
    }

    public List<ThemeCandidate> findCandidates(String themeKey) {
        return CANDIDATES.stream()
                .filter(candidate -> "all".equals(themeKey) || candidate.themeKey().equals(themeKey))
                .toList();
    }
}
