package org.example.common.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.common.exeption.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    public String extractUserName(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Unauthorized request");
            throw new UnauthorizedException("Токен авторизации отсутствует или не удовлетворяет формату \"Bearer ...\"");
        }
        return getNameFromAuthToken(authHeader);
    }

    public String getNameFromAuthToken(String authHeader){
        String token = authHeader.substring(7);
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody();
        String userName = claims.get("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name", String.class);
        log.info("Имя пользователя: " + userName);
        return userName;
    }
}
