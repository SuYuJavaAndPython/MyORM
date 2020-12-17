package com.suyu.jdbc;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 此类出现的目的是为了执行查询的sql及具体操作
 * 用默认不写的修饰符保证同包下才能访问----体现封装性
 */
class JdbcQueryTemplate extends JdbcTemplate {

    /**
     * 数据库中的一对列名 + 列值数据可以对应我们一条map数据    一行数据可以对应一个map集合
     * 多行数据于是就可以对应一个list集合（用list方便遍历）
     * 故以List<map<String,Object>>的形式赋值给Object充当返回值
     * @param param
     * @return
     */
    @Override
    protected Object executeSql(Object... param) throws SQLException {
        //充当返回值
        List<Map<String,Object>> rows = new ArrayList<>();
        JdbcTemplate.rs = JdbcTemplate.pstat.executeQuery();
        //获取元数据集合
        ResultSetMetaData metaDataCollection = rs.getMetaData();
        //外层循环控制行数
        while(rs.next()){
            //内层循环将一行元数据集合中的数据装入row
            HashMap<String,Object> row = new HashMap<>();
            for(int i = 1; i <= metaDataCollection.getColumnCount(); i++){
                //获取一行记录中某个列的列名
                String listName = metaDataCollection.getColumnName(i);
                //获取列名对应的那个列值
                Object listValue = rs.getObject(listName);
                //约定我们装载的列名均为小写（类似于约定优于配置）
                //用户获取时get("小写")
                // 但是要更完美具有健壮性可以效仿连接池封装中重写Connection中的方法
                //我们重写HashMap中的get方法(忽略大小写)
                row.put(listName.toLowerCase(),listValue);
            }
            rows.add(row);
        }
        return rows;
    }
}
