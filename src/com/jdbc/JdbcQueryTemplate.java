package com.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查模板
 */
class JdbcQueryTemplate extends JdbcTemplate {

    public JdbcQueryTemplate(String className, String url, String userName, String password) {
        super(className, url, userName, password);
    }

    @Override
    protected Object four() throws SQLException {
        rs = super.stat.executeQuery();
        //不管查询结果能不能组成对象，Map都适用
        List<Map<String,Object>> rows = new ArrayList<>();
        while(rs.next()){
            Map<String,Object> row = new HashMap<>();
            //rs.getMetaData().getColumnCount()获取结果集里的列数
            for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
                String key = rs.getMetaData().getColumnName(i);
                Object value = rs.getObject(i);
                row.put(key.toLowerCase(),value);
            }
            rows.add(row);
        }
        return rows;
    }
}
