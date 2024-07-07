package org.example.common;

public class MissingAuthorizationTokenException extends RuntimeException {
    public MissingAuthorizationTokenException(String message) {
        super(message);
    }
}
