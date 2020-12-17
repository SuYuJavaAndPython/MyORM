package com.suyu.test;

import com.suyu.jdbc.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

public class TestThread extends Thread {

    public void run() {
        ConnectionPool pool = ConnectionPool.getPool();
        Connection conn = pool.getConnection();
        System.out.println(conn);
        try {
            Thread.sleep(3000);
            //释放连接
            conn.close();
        } catch (InterruptedException | SQLException e) {
            e.printStackTrace();
        }
    }
}
