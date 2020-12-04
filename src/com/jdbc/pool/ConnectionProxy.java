package com.jdbc.pool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 静态代理类    我们希望在原来的Connection基础上添加一些操作（做一些手脚）
 * 但实际上我们使用的ConnectionProxy还是原生的Connection
 */
public class ConnectionProxy extends AbstractConnection{

    boolean closeFlag = false;  //false为不关闭 释放连接    true为关闭连接
    boolean useFlag = false;    //false空闲       true为被使用中

    public ConnectionProxy(Connection conn){
        super.conn = conn;
    }

    @Override
    public void close() throws SQLException {
        if(closeFlag){
            conn.close();
        }else{
            //释放连接
            useFlag = false;
        }
    }

}
