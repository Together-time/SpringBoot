package com.tt.Together_time.exception;

public class BlacklistedTokenException extends RuntimeException{
    public BlacklistedTokenException(String message){
        super(message);
    }
}
