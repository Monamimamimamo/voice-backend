package org.example.common.auth;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class JwtExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String JWT_EXPIRED_ERROR_MESSAGE = "JWT has expired";

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Object> handleExpiredJwtException() {
        return new ResponseEntity<>(JWT_EXPIRED_ERROR_MESSAGE, HttpStatus.UNAUTHORIZED);
    }
}
