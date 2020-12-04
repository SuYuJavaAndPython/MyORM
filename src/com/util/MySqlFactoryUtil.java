package com.util;

import com.jdbc.JdbcFactory;

/**
 * 为防止执行一次操作就new一个工厂来造工具的代码冗余及浪费空间这种情况
 * 我们把MySQL工厂做成单例（单实例管理机制）  以后执行MySQL操作时用的都是同一个工厂
 */
public class MySqlFactoryUtil {

    private static JdbcFactory factory;
    static {
        factory = new JdbcFactory("mysql.properties");
    }
    public static JdbcFactory getFactory(){
        return factory;
    }

}
