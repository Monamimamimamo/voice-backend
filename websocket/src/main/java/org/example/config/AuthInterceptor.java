package org.example.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AuthInterceptor implements HandshakeInterceptor {
    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String query = request.getURI().getQuery();
        Map<String, String> mapQuery = new HashMap<>();
        for(String param : query.split("&")){
            mapQuery.put(param.split("=")[0], param.split("=")[1]);
        }
        String jwtToken = mapQuery.get("token");
        if (jwtToken == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            log.info("Хэдэр авторизации отсутствует");
            return false;
        }
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(jwtToken)
                    .getBody();
            String userName = claims.get("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name", String.class);
        } catch (Exception e){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            log.info("Хэдэр авторизации не прошёл валидацию");
            return false;
        }
        return true;

    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
