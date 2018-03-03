package com.subticket;

public class ClientErrorException extends RuntimeException {
    public ClientErrorException(String message) {
        super(message);
    }
    public ClientErrorException(String message, Exception cause) {
        super(message, cause);
    }
}
