package com.suyu.jdbc;

import com.suyu.jdbc.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 这个类是JDBC流程具体执行的模板，原来的六部曲封装在这里
 * 用默认不写的修饰符保证同包下才能访问----体现封装性
 */
abstract class JdbcTemplate {

    private static Connection conn = null;
    protected static PreparedStatement pstat = null;
    protected static ResultSet rs = null;

    /**
     * 六部曲被封装在此方法中
     * @param sql
     * @param param
     * @return
     */
    protected Object executeJdbc(String sql,Object...param){
        Object result = null;
        try {
            //1.导包
            //2.加载驱动类
            //3.创建连接
            this.createConnection();
            //4.创建状态参数
            this.createPreparedStatement(sql,param);
            //5.执行sql及具体操作
            result = this.executeSql(param);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //6.关闭各种连接通道    放在finally中保证一定执行
            try {
                this.closeAllConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 通过连接池创建连接
     * @return
     */
    private void createConnection(){
        conn = ConnectionPool.getPool().getConnection();
    }

    /**
     * 创建状态参数并预处理sql语句
     * @param sql
     * @throws SQLException
     */
    private void createPreparedStatement(String sql,Object...param) throws SQLException {
        pstat = conn.prepareStatement(sql);
        //传递参数给pstat
        for (int i = 0; i < param.length; i++) {
            pstat.setObject(i+1,param[i]);
        }
    }

    /**
     * 执行sql及具体操作（由于增删改 和 查的流程不太一致，故在此将两个不同流程区分开即另外找两个类帮忙）
     * 具体由子类实现
     */
    protected abstract Object executeSql(Object...param) throws SQLException;

    /**
     * 关闭各种连接
     * @throws SQLException
     */
    private void closeAllConnection() throws SQLException {
        if(rs != null){
            rs.close();
        }
        if(pstat != null){
            pstat.close();
        }
        if(conn != null){
            conn.close();
        }
    }

}
