package com.thkim.toyproject.fintrack.infrastructure.security;

import org.springframework.http.ResponseCookie;

public class JwtCookieUtil {
    public static final String REFRESH_COOKIE = "REFRESH_TOKEN";

    public static ResponseCookie refreshCookie(String value, long maxAgeSeconds, String domain) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(true)            // HTTPS에서만 전송
                .sameSite("None")        // 크로스 사이트 허용(프론트/백 도메인·포트 다르면 필수)
                .path("/api/auth")       // 범위 최소화
                .maxAge(maxAgeSeconds);
        if (domain != null) b.domain(domain);
        return b.build();
    }

    public static ResponseCookie deleteRefreshCookie(String domain) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true).secure(true).sameSite("None")
                .path("/api/auth")
                .maxAge(0);
        if (domain != null) b.domain(domain);
        return b.build();
    }
}
