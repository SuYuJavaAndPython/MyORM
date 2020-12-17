package com.suyu.jdbc;

import java.util.List;

/**
 * 这个类只是为了将SqlHandle解析后获得的sql语句和List参数集合包装成对象
 * 因为SqlHandle解析完后无法通过一个返回值返回两个东西   而包装成对象返回可以而且可读性好
 * 同包下才可访问
 */
public class SqlAndParam {

    String sql;
    List<Object> params;
}
