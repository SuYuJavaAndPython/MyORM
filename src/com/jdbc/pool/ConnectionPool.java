package com.jdbc.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 连接池  负责创建连接  管理连接    分配连接    释放连接
 */
public class ConnectionPool {

    //连接池存储连接的地方
    private List<ConnectionProxy> connections;
    //连接生成器
    private ConnectionGenerator generator;
    //连接清理器
    private ConnectionCleaner cleaner;

    private String url;
    private String userName;
    private String password;

    //连接池最大装载数量 若不设置则默认为100
    private Integer maxTotal = 100;
    //业务线程来访问连接池时若找不到可用的连接  等待的最大时间若不设置则默认为2秒
    private Integer maxWait = 2000;
    //连接池中连接不足时判断空闲连接数若少于minIdle则开始造连接
    private Integer minIdle = 2;
    //每次造连接时默认造的数量为createConnectionTotal
    private Integer createConnectionTotal = 10;


    public ConnectionPool(String driver,String url,String userName,String password) throws ClassNotFoundException, SQLException {
        this.url = url;
        this.userName = userName;
        this.password = password;
        //创建连接池时    就自动创建一组初始连接对象
        connections = new ArrayList<>(10);
        Class.forName(driver);
        for(int i = 1; i <= 10; i++){
            Connection conn = DriverManager.getConnection(url,userName,password);
            ConnectionProxy connectionProxy = new ConnectionProxy(conn);
            connections.add(connectionProxy);
        }
        this.generator = new ConnectionGenerator();
        this.cleaner = new ConnectionCleaner();
        /**
         * 将线程设置为精灵线程(守护线程)，随着主线程的关闭而关闭
         * 补充：线程分两类（用户线程也叫主线程   守护线程也叫精灵线程）
         * 我们新创建的线程若不做设置则默认为用户线程
         */
        this.generator.setDaemon(true);
        this.generator.start();

        Thread cleaner = new Thread(this.cleaner);
        cleaner.setDaemon(true);
        cleaner.start();
    }

    //连接池提供获取连接的方法
    public Connection getConnection() throws InterruptedException {
        //找到空闲连接，改变使用状态，返回连接对象
        int wait_time = 0;
        wait:while(true) {
            find:for (ConnectionProxy connectionProxy : connections) {
                //双重检查锁定模式
                if (!connectionProxy.useFlag) {   //useFlag == false
                    synchronized(connectionProxy) {
                        if(!connectionProxy.useFlag) {
                            connectionProxy.useFlag = true;
                            //理论上来说 当我们要返回一个连接时我们做一个检测
                            //但实际上  我们假设一次创建10个连接，这个过程相对来说比较慢
                            //而此时恰巧有其他业务线程来访问线程池，我们不能让他等着我们造完连接
                            //再去访问连接池拿连接    因为我们假设连接池初始10个连接
                            //当连接池中剩余连接数小于等于2时我们就需要检测   但此时连接池还有两个
                            //如果我们去检测造连接，那么此时外来的业务线程就需要等着
                            //这样首先性能上来说慢    其次不符合现实生活逻辑（不合理）
                            //所以我们想到了用线程来检测造连接  即返回连接和检测造连接这两个过程同时进行
                            //注意这里的返回连接这个过程依托于主线程（即也是一个线程，故能实现线程异步(现实中的同步)）
//                            this.generator.checkAndCreate();

                            //释放一个资源    通过synchronized定位到那个被拿着的资源，然后notify
                            synchronized (generator) {
                                generator.notify();
                            }
                            return connectionProxy;
                        }else{
                            //注意有可能此时返回的是连接池中的最后一个连接，那么第二个线程进入访问时就拿不到了
                            //但是有可能在这个过程中前面其他线程使用结束的连接返回给连接池，那就不能结束循环
                            //应该从第一个重新开始找
                            continue wait;
                        }
                    }
                }
            }
            //如果经过上面的循环没找到空闲的连接，我们让业务程序等一会
            wait_time += 100;
            Thread.sleep(100);
            if(wait_time == this.maxWait){
                throw new ConnectionPoolException("connect time out");
            }else {
                continue wait;
            }
        }
    }

    //连接池提供检测连接池中连接数量并可以添加的方法

    /**
     * 从类的关系上来看这个类应该是属于连接池的，即连接池有一个连接生成器 has a
     * 这一个连接生成器     连接不足时造连接
     * 注意这里采用私有成员内部类的写法的原因：
     * 逻辑上这个连接生成器只给连接池自己用   所以写成私有成员内部类
     * 语法上连接生成器造出来的连接要添加进连接池    private List<ConnectionProxy> connections;
     * 而只有内部类才能直接访问外部类的私有成员
     */
    private class ConnectionGenerator extends Thread{
        //重写run方法
        public void run(){
            try {
                this.checkAndCreate();
            } catch (SQLException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        //检测连接  发现不足就创建连接
        public void checkAndCreate() throws SQLException, InterruptedException {
            //生命不息，检测不止
            while(true) {
                //记录连接池中剩余连接的数量
                int count = 0;
                //循环找到未被使用的连接   内部类使用外部类的私有成员
                for (ConnectionProxy proxy : connections) {
                    if(!proxy.useFlag){
                        count++;
                    }
                }
                if(count <= minIdle){
                    //连接不足  造连接但要有一个上限，不能一直造    默认造10个
                    if(connections.size() + createConnectionTotal > maxTotal){
                        createConnectionTotal = maxTotal - connections.size();
                    }
                    for(int i = 1; i <= createConnectionTotal; i++){
                        Connection connection = DriverManager.getConnection(url,userName,password);
                        ConnectionProxy proxy = new ConnectionProxy(connection);
                        connections.add(proxy);
                    }
                }
                //锁一个资源     先用synchronized拿一个资源，然后wait
                synchronized(generator){
                    this.wait();
                }
            }
        }
    }

    /** 思考：
     * 如果一波(大量)连接应用过去后
     * 连接池就会产生多个空闲连接(100)
     * 连接池应用平稳后，可能不需要这么多连接
     * 关闭部分连接
     * 关闭连接时，连接池也可以正常工作。
     * 如何真正关闭连接
     *
     * 我们假定当空闲的连接数超过50个就认为产生了过剩连接
     * 此时需要一个小弟来帮我们清理这些过剩的连接    但同样的，清理过程和返回连接过程和检测造连接过程
     * 互不干扰，故此小弟也应为线程
     * 那么如何清除过剩连接呢？
     * 首先将过剩连接的closeFlag更改为true然后调用代理的close方法
     * 其次将连接池中的过剩连接remove
     */
    private class ConnectionCleaner implements Runnable{
        @Override
        public void run() {
            try {
                this.checkAndClear();
            } catch (SQLException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        //设计一个方法    用来检测并清理过剩连接
        public void checkAndClear() throws SQLException, InterruptedException {
            clear: while(true){
                //记录空闲连接的个数
                int count = 0;
                for(ConnectionProxy proxy : connections){
                    if(!proxy.useFlag){
                        count++;
                    }
                }
                //当空闲连接数大于20个我们就清理
                if(count > 20){
                    for(ConnectionProxy proxy : connections){
                        if (!proxy.useFlag) {
                            synchronized (proxy) {
                                if (!proxy.useFlag) {
                                    proxy.useFlag = true;
                                    proxy.closeFlag = true;
                                    proxy.close();
                                    connections.remove(proxy);
                                    //可能在清理过剩连接的过程中，有的过剩连接被利用起来了，不满足超过50个的条件
                                    //所以要重新检测一下
                                    System.out.println(connections.size());
                                    continue clear;
                                }
                            }
                        }
                    }
                }
                //不能一直做检测清理，否则浪费资源（内存）  我们只在必要的时候做检测清理
                //那么问题来了，必要的时候是什么时候？
                //那就是当连接调用代理的close后
//                synchronized (cleaner){
//                    Thread thread = new Thread(cleaner);
//                    thread.wait();
//                }
            }
        }
    }

    public ConnectionCleaner getCleaner() {
        return cleaner;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public void setCreateConnectionTotal(Integer createConnectionTotal) {
        this.createConnectionTotal = createConnectionTotal;
    }
}
