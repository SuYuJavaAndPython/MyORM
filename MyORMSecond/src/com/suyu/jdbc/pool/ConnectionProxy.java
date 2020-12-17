package com.suyu.jdbc.pool;

import java.sql.*;

/**
 * 这个类出现的目的是为了充当静态代理，用代理的那个人（JDBC4Connection）的方法
 * 在实现的方法中做一些手脚但还是靠执行JDBC4Connection的方法
 * 同时能包装真实连接和连接的使用状态
 */
public class ConnectionProxy extends AbstractConnection {

    private static String driver;
    private static String url;
    private static String user;
    private static String password;

    //真实连接
    private Connection connection;
    //useStatus使用状态如果为false则代表该未被使用 true代表使用中
    private boolean useStatus = false;

    /**
     * 利用静态块的执行顺序在构造方法前以及只执行一次的特点
     * 读取配置文件中的信息同时加载jdbc驱动类
     */
    static{
        try {
            driver = ConfigReader.getConfigValue("driver");
            url = ConfigReader.getConfigValue("url");
            user = ConfigReader.getConfigValue("user");
            password = ConfigReader.getConfigValue("password");
            //加载驱动类
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 普通块创建连接
     */
    {
        try {
            this.connection = DriverManager.getConnection(url,user,password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //======================================================================
    //重写方法如下    做手脚并执行JDBC4Connection

    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    /**
     * 重写真实连接的close方法
     * 让close关闭连接改为释放连接
     */
    public void close() throws SQLException {
        this.useStatus = false;
    }

    //=============================================================================

    public boolean isUseStatus() {
        return useStatus;
    }

    public void setUseStatus(boolean useStatus) {
        this.useStatus = useStatus;
    }
}
