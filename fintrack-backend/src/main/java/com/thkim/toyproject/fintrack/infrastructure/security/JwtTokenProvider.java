package com.thkim.toyproject.fintrack.infrastructure.security;

import com.thkim.toyproject.fintrack.domain.users.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/*
* 이 파일의 역할은 token
* */
@Component
@RequiredArgsConstructor //final 또는 @NotNull 이 붙은 경우 사용됨.
public class JwtTokenProvider {

    @Value("${app.jwt.secret-base64}")
    private String secretBase64;

    private SecretKey key;
    private final CustomUserDetailsService userDetailsService;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64));
    }

    public String createAccessToken(String username, Collection<? extends GrantedAuthority> authorities, long ttlSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .setHeaderParam("typ", "JWT")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String username, long ttlSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .setHeaderParam("typ", "JWT")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String parseSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateTokenMatchesUser(String token, String username) {
        try {
            var claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return username.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public List<GrantedAuthority> loadAuthorities(String username) {
        var ud = userDetailsService.loadUserByUsername(username);
        return ud.getAuthorities().stream().map(a -> new SimpleGrantedAuthority(a.getAuthority())).collect(Collectors.toList());
    }
}
