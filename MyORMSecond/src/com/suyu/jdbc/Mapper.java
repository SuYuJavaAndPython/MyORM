package com.suyu.jdbc;

import java.util.Map;

/**
 * 此类的出现体现了策略设计模式
 * dao是我们框架的使用者     dao用util工具执行查询操作时需要告知util给dao组成什么返回
 * 于是需要提供一个策略   告知util怎么去给我组装结果返回给dao
 */
public interface Mapper<T> {

    /**
     * 将一行记录组成用户提供的T返回
     * @return
     */
    T orm(Map<String, Object> row);
}
