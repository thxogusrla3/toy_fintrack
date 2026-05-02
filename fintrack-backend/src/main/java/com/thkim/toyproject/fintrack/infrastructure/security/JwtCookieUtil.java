package com.thkim.toyproject.fintrack.infrastructure.security;

import org.springframework.http.ResponseCookie;

public class JwtCookieUtil {
    public static final String REFRESH_COOKIE = "REFRESH_TOKEN";

    public static ResponseCookie refreshCookie(String value, long maxAgeSeconds, String domain, boolean secure, String sameSite) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api/auth")       // 범위 최소화
                .maxAge(maxAgeSeconds);
        if (domain != null) b.domain(domain);
        return b.build();
    }

    public static ResponseCookie deleteRefreshCookie(String domain, boolean secure, String sameSite) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api/auth")
                .maxAge(0);
        if (domain != null) b.domain(domain);
        return b.build();
    }
}
