package com.jdbc;

import com.jdbc.pool.ConnectionPool;

import java.sql.*;

abstract class JdbcTemplate {

    private String className;
    private String url;
    private String userName;
    private String password;

    public JdbcTemplate(String className, String url, String userName, String password) {
        this.className = className;
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    private Connection conn;
    protected PreparedStatement stat;
    protected ResultSet rs;

    public Object executeJdbc(String sql,Object[] param){
        try {
            this.one();
            this.two();
            //如果用户没传参   这里也不会发生空指针异常
            this.three(sql,param);
            Object result = this.four();
            return result;
        }catch(Exception e){
            e.printStackTrace();
        } finally{
            try {
                this.five();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private void one() throws ClassNotFoundException {
        //加载驱动类 原来要我们自己加载驱动类    现在连接池帮我们加载过了
//        Class.forName(className);
    }
    private void two() throws SQLException, InterruptedException {
        //获取连接  原来我们的连接是自己向程序申请的
//        this.conn = DriverManager.getConnection(url,userName,password);
        //现在我们的连接是向连接池申请的
        this.conn = this.pool.getConnection();
    }
    private void three(String sql,Object[] param) throws SQLException {
        //创建状态参数
        this.stat = conn.prepareStatement(sql);
        for(int i = 0; i < param.length; i++){
            this.stat.setObject(i+1,param[i]);
        }
    }
    //执行数据库操作(增删改)
    protected abstract Object four() throws SQLException;
    private void five() throws SQLException {
        //关闭流通道
        if(rs != null){
            rs.close();
        }
        if(stat != null){
            stat.close();
        }
        if(conn != null){
            //从连接池拿到的连接关闭方法已经被我们做了手脚
            //在不改变closeFlag状态的情况下，这里的close方法实际其作用是释放连接
            conn.close();
            //这个效果一台机器很难演示
            System.out.println(conn);
//            synchronized (pool.getCleaner()){
//                Thread cleaner = new Thread(pool.getCleaner());
//                cleaner.notify();
//            }
        }
    }

    //通过这种方式将共厂创建的连接池依次传递到模板里
    private ConnectionPool pool;
    public void setPool(ConnectionPool pool) {
        this.pool = pool;
    }
}