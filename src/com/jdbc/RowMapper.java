package com.jdbc;

import java.util.Map;

/**
 * 这是一个策略规则     指定查询的每条记录组成对应的对象的策略规则
 */
public interface RowMapper<T> {

     /**
      *  将结果集对象中的一条记录 组成 对应的domain对象
      *  切记不要循环结果集
      */
     //每一个策略都实现这个方法
     T mapping(Map<String,Object> row) throws Exception;
}
