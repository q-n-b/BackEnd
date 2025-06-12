package qnb.user.JWT;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 256비트 키 생성
    private final long accessTokenValidTime = 1000L * 60 * 30; // 30분
    private final long refreshTokenValidTime = 1000L * 60 * 60 * 24 * 7; // 7일

    // Access Token 생성
    public String createAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) //subject에 userId 저장
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidTime))
                .signWith(key)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String userEmail) {
        return Jwts.builder()
                .setSubject(String.valueOf(userEmail)) //subject에 이메일 저장
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidTime))
                .signWith(key)
                .compact();
    }

    // 토큰에서 userId 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

