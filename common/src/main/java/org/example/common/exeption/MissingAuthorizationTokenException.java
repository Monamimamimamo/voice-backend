package org.example.common.exeption;

public class MissingAuthorizationTokenException extends RuntimeException {
    public MissingAuthorizationTokenException(String message) {
        super(message);
    }
}
