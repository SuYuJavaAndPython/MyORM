package com.suyu.jdbc;

import java.sql.SQLException;

/**
 * 此类出现的目的是为了执行增删改的sql及具体操作
 * 用默认不写的修饰符保证同包下才能访问----体现封装性
 */
class JdbcUpdateTemplate extends JdbcTemplate {

    /**
     * 增删改操作返回数据库中被更改的行数（受影响的行数）int自动拆装箱成Integer赋值给Object类型的返回值
     * @param param
     * @return
     * @throws SQLException
     */
    @Override
    protected Object executeSql(Object... param) throws SQLException {
        return JdbcTemplate.pstat.executeUpdate();
    }
}
