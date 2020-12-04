package com.jdbc;

import com.jdbc.pool.ConnectionPool;

import java.io.*;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 工厂模式
 * 负责创建JdbcUtil工具的
 * 注意工厂之所以不写成单例的是因为我们的项目不一定只跟MySQL数据库交互也可能跟Oracle数据库交互
 * 不同的交互需要不同的工厂
 * @author Administrator
 */
public class JdbcFactory {

    private String driver;
    private String url;
    private String userName;
    private String password;

    //连接池最大装载数量 若不设置则默认为100
    private Integer maxTotal;
    //业务线程来访问连接池时若找不到可用的连接  等待的最大时间若不设置则默认为2秒
    private Integer maxWait;
    //连接池中连接不足时判断空闲连接数若少于minIdle则开始造连接
    private Integer minIdle;
    //每次造连接时默认造的数量为createConnectionTotal
    private Integer createConnectionTotal;

    public JdbcFactory(){
        //默认读src根目录的文件路径    构造器之间的调用(同级用this    父子用super)
        this("mysql.properties");
    }

    /**
     * 人为规定的读取的文件都放置在src下
     * new JdbcFactory("mysql.properties");
     * @param fileName
     */
    public JdbcFactory(String fileName){
        //简单理解为获取的是src根目录下的className文件路径
        //实际上获取的是out（classPath）目录下的文件路径
        String path = Thread.currentThread().getContextClassLoader()
                .getResource(fileName).getPath();
        File file = new File(path);
        this.readFile(file);
    }

    /**
     * 使用者根据程序，自己获得配置文件。交给工厂读取
     * File file = new File("d:/z/mysql.properties");
     * new JdbcFactory(file);
     * @param file
     */
    public JdbcFactory(File file){
        this.readFile(file);
    }

    private void readFile(File file){
        try {
            InputStream is = new FileInputStream(file);
            //Hashtable早期版本1.0  线程安全  同步    key value不能为null
            //HashMap1.2版本 非线程同步    key value可以为null
            Properties properties = new Properties();
            //将is里的数据以key value形式加载到properties中
            properties.load(is);
            driver = properties.getProperty("driver");
            url = properties.getProperty("url");
            userName = properties.getProperty("userName");
            password = properties.getProperty("password");

            String maxTotal = properties.getProperty("maxTotal");
            String maxWait = properties.getProperty("maxWait");
            String minIdle = properties.getProperty("minIdle");
            String createConnectionTotal = properties.getProperty("createConnectionTotal");

            if(maxTotal != null && !"".equals(maxTotal)){
                //当成员变量与局部变量重名时要用this以示区分
                this.maxTotal = Integer.parseInt(maxTotal);
            }
            if(maxWait != null && !"".equals(maxWait)){
                //当成员变量与局部变量重名时要用this以示区分
                this.maxWait = Integer.parseInt(maxWait);
            }
            if(minIdle != null && !"".equals(minIdle)){
                //当成员变量与局部变量重名时要用this以示区分
                this.minIdle = Integer.parseInt(minIdle);
            }
            if(createConnectionTotal != null && !"".equals(createConnectionTotal)){
                //当成员变量与局部变量重名时要用this以示区分
                this.createConnectionTotal = Integer.parseInt(createConnectionTotal);
            }

            //默认创建连接池
            this.createConnectionPool();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 工厂里能创建两个工具JdbcUtil(原有的语法，代码写法会多)和SqlSession（自定义sql语法，简化代码）
     * @return
     */
    public JdbcUtil getUtil(){
        JdbcUtil util = new JdbcUtil(driver,url,userName,password);
        //将工厂里创建的pool传递给JdbcUtil
        util.setPool(pool);
        return util;
    }
    public SqlSession getSession(){
        /**注意这种写法会产生空指针异常    因为方法的调用顺序而产生的
         * new SqlSession时pool还没传给util  但SqlSession构造方法里util就要用到pool
         */
//        SqlSession session = new SqlSession(driver,url,userName,password);
//        //将工厂里创建的pool传递给SqlSession
//        session.setPool(pool);
//        return session;
        //将工厂里创建的pool传递给SqlSession
        SqlSession session = new SqlSession(driver,url,userName,password,pool);

        return session;
    }

    private ConnectionPool pool;

    /**
     * 经过分析我们知道一个工厂就一个连接池   二者是共生关系，共厂一出现连接池就创建好了(单实例管理)
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private void createConnectionPool() throws SQLException, ClassNotFoundException {
        pool = new ConnectionPool(driver,url,userName,password);
        pool.setMaxTotal(maxTotal);
        pool.setMaxWait(maxWait);
        pool.setMinIdle(minIdle);
        pool.setCreateConnectionTotal(createConnectionTotal);
    }
}
