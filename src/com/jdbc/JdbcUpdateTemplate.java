package com.jdbc;

import java.sql.SQLException;

/**
 * 增删改模板
 */
class JdbcUpdateTemplate extends JdbcTemplate{

    public JdbcUpdateTemplate(String className, String url, String userName, String password) {
        super(className, url, userName, password);
    }

    @Override
    protected Object four() throws SQLException {
        return super.stat.executeUpdate();
    }
}
