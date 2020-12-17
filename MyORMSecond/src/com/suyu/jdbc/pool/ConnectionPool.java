package com.suyu.jdbc.pool;

import java.sql.Connection;
import java.util.ArrayList;

public class ConnectionPool {

    //双重检测模式实现连接池的单例模式
    private ConnectionPool(){}
    //使用volatile修饰防止指令重排序
    private static volatile ConnectionPool pool;
    /**
     * 对外提供获取连接池的方法  用static修饰可以通过类名直接访问
     * 因为构造方法已经私有，此时外部无法创建连接池对象来调用方法
     * @return
     */
    public static ConnectionPool getPool(){
        if(pool == null){
            synchronized(ConnectionPool.class){
                if(pool == null){
                    //这一个new操作底层计算机实际有多个指令去实现这一个操作
                    pool = new ConnectionPool();
                }
            }
        }
        return pool;
    }

    //list集合充当连接池
    private static ArrayList<Connection> connList = new ArrayList<>();
    //连接自身的属性   创建连接池时初始化的最小连接数量
    //可能还有最大连接数量maxConnectionCount（比如扩容的上限）
    private static Integer minConnectionCount = Integer.parseInt(ConfigReader.getConfigValue("minConnectionCount"));
    //设置获取连接的用户等待的最长时间
    private static Integer wait_time = Integer.parseInt(ConfigReader.getConfigValue("wait_time")) * 10;

    /**
     * 连接池初始化装载minConnectionCount个连接
     */
    static {
        for(int i = 0; i < minConnectionCount; i++){
            connList.add(new ConnectionProxy());
        }
    }

    /**
     * 连接池提供对自己的获取连接的方法
     * 其实synchronized放方法上也成，因为调用此方法耗时是0.几毫秒级的
     * 放下面也提高不了多少性能 但推荐还是放下面
     * @return
     */
    private Connection getMC(){
        Connection result = null;
        for(Connection conn : connList){
            ConnectionProxy mc = (ConnectionProxy) conn;
            if(!mc.isUseStatus()){
                //判断此连接未被使用那就锁住连接池不让别人获取
                synchronized(this){
                    if(!mc.isUseStatus()){
                        mc.setUseStatus(true);
                        result = mc;
                    }
                }
                break;
            }
        }
        return result;
    }

    public Connection getConnection(){
        Connection result = this.getMC();
        //记录循环的次数
        int count = 0;
        while(result == null && count < wait_time){
            try {
                Thread.sleep(100);
                result = this.getMC();
                count++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(result == null){
            //超过5秒还是没找到可用的连接
            throw new ConnectionPoolException("connect time out");
        }
        return result;
    }

}
