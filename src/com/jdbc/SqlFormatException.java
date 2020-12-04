package com.jdbc;

/**
 * 注意着里为什么继承RuntimeException而不是Exception
 * RuntimeException用于不经常出现且不怎么重要的运行时异常
 * Exception用于经常出现且重要的异常
 */
public class SqlFormatException extends RuntimeException {

    public SqlFormatException() {}

    public SqlFormatException(String message) {
        super(message);
    }
}
