package com.tt.Together_time.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message){
        super(message);
    }
}
