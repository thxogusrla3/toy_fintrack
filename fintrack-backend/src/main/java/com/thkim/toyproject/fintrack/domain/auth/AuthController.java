package com.thkim.toyproject.fintrack.domain.auth;

import com.thkim.toyproject.fintrack.domain.users.User;
import com.thkim.toyproject.fintrack.infrastructure.persistence.InMemoryRefreshTokenStore;
import com.thkim.toyproject.fintrack.infrastructure.security.JwtCookieUtil;
import com.thkim.toyproject.fintrack.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwt;
    private final InMemoryRefreshTokenStore store;

    private final long ACCESS_TTL = 60 * 15;             // 15m
    private final long REFRESH_TTL = 60L * 60 * 24 * 14;  // 14d
    private final String COOKIE_DOMAIN = null;            // 필요 시 "example.com"

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        String access = jwt.createAccessToken(auth.getName(), auth.getAuthorities(), ACCESS_TTL);
        String refresh = jwt.createRefreshToken(auth.getName(), REFRESH_TTL);

        store.saveOrRotate(auth.getName(), refresh);
        ResponseCookie cookie = JwtCookieUtil.refreshCookie(refresh, REFRESH_TTL, COOKIE_DOMAIN);

        // 사용자의 권한(role) 정보를 추출
        String userRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 응답 본문에 액세스 토큰과 함께 권한 정보를 추가
        Map<String, String> body = Map.of(
                "accessToken", access,
                "role", userRole
        );

        return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String refresh = null;
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) if (JwtCookieUtil.REFRESH_COOKIE.equals(c.getName())) refresh = c.getValue();
        }
        if (refresh == null || !store.isValid(refresh)) return ResponseEntity.status(401).body(Map.of("error","No/Invalid refresh"));

        String username = jwt.parseSubject(refresh);
        String newAccess = jwt.createAccessToken(username, jwt.loadAuthorities(username), ACCESS_TTL);
        String newRefresh = jwt.createRefreshToken(username, REFRESH_TTL);
        store.rotate(username, refresh, newRefresh);

        ResponseCookie cookie = JwtCookieUtil.refreshCookie(newRefresh, REFRESH_TTL, COOKIE_DOMAIN);
        return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(Map.of("accessToken", newAccess));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String refresh = null;
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) if (JwtCookieUtil.REFRESH_COOKIE.equals(c.getName())) refresh = c.getValue();
        }
        if (refresh != null) store.revoke(refresh);

        ResponseCookie cookie = JwtCookieUtil.deleteRefreshCookie(COOKIE_DOMAIN);
        return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(Map.of("message","logged out"));
    }
}
