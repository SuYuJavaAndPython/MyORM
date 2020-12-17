package com.suyu.jdbc;

public class RowCountException extends RuntimeException {

    public RowCountException() {}

    public RowCountException(String message) {
        super(message);
    }
}
