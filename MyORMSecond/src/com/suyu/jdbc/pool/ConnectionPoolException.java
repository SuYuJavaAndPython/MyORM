package com.suyu.jdbc.pool;

public class ConnectionPoolException extends RuntimeException {

    public ConnectionPoolException() {}

    public ConnectionPoolException(String message) {
        super(message);
    }
}
