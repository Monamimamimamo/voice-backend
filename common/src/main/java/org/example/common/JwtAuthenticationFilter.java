package org.example.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

//public class JwtAuthenticationFilter extends OncePerRequestFilter {

//    private final String secretKey = "your_secret_key";
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        Enumeration<String> headerNames = request.getHeaderNames(); // Получаем все имена заголовков
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            String headerValue = request.getHeader(headerName); // Получаем значение каждого заголовка
//            System.out.println(headerName + ": " + headerValue);
//        }
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
//            throw new ServletException("Missing or invalid Authorization header");
//        }
//
//        String token = authHeader.substring(7);
//        // Добавьте здесь логику для обработки JWT, например, проверку подписи токена
//
//        // Если токен действителен, продолжаем выполнение запроса
//        filterChain.doFilter(request, response);
//    }
//}
