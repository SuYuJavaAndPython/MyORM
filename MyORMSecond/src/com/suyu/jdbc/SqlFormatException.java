package com.suyu.jdbc;

public class SqlFormatException extends RuntimeException {

    public SqlFormatException() {}

    public SqlFormatException(String message) {
        super(message);
    }
}
