package com.socialnexus.auth;

public class UnauthorizedSessionException extends RuntimeException {

    public UnauthorizedSessionException(String message) {
        super(message);
    }
}
