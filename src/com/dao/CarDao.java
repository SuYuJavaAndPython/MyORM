package com.dao;

import com.domain.Car;
import com.jdbc.JdbcFactory;
import com.jdbc.JdbcUtil;
import com.jdbc.RowMapper;
import com.jdbc.SqlSession;
import com.util.MySqlFactoryUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CarDao {
    private String driver = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/test?characterEncoding=utf8&serverTimezone=CST";
    private String userName = "root";
    private String password = "rootc";

    //JDBC未封装前的六部曲
    public void save(Car car){
        // 1. 导包
        // 2. 加载驱动类
        // 3. 获取连接
        // 4. 创建状态参数
        // 5. 执行数据库操作
        // 6. 关闭流通道
        String sql = "insert into jdbcCar values(null,?,?,?)";
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url,userName,password);
            PreparedStatement stat = conn.prepareStatement(sql);
            stat.setString(1,car.getName());
            stat.setString(2,car.getColor());
            stat.setInt(3,car.getPrice());
            stat.executeUpdate();
            stat.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //JDBC初步封装后的增删改方法
    public int save2(Car car){
        String sql = "insert into jdbcCar values(null,?,?,?)";
        JdbcUtil util = new JdbcUtil(
                driver,
                url,
                userName,
                password
        );
        return util.insert(sql,new Object[]{car.getName(),car.getColor(),car.getPrice()});
    }
    //JDBC初步封装后的 增 删 改方法
    public int save3(Car car){
        String sql = "insert into jdbcCar(name,color,price) values(?,?,?)";
        JdbcFactory factory = new JdbcFactory();
        JdbcUtil util = factory.getUtil();
        return util.insert(sql,car.getName(),car.getColor(),car.getPrice());
    }
    //JDBC初步封装后的增删改方法
    public int save4(Car car){
        String sql = "insert into jdbcCar(name,color,price) values(#{name},#{color},#{price})";
        JdbcFactory factory = new JdbcFactory();
        SqlSession session = factory.getSession();
        return session.insert(sql,car);
    }
    //JDBC初步封装后的增删改方法
    public int deleteInformation(String sql){
        JdbcUtil util = new JdbcUtil(
                driver,
                url,
                userName,
                password
        );
        //动态参数列表的好处体现出来了
        return util.delete(sql);
    }
    //JDBC初步封装后的查方法
    public void selectAll(){
        String sql = "select * from jdbcCar";
        JdbcUtil util = new JdbcUtil(
                driver,
                url,
                userName,
                password
        );
        List<Car> list = null;
//            list = util.selectList(sql,new CarMapper());
        list = util.selectList(sql,Car.class);
        for(Car car : list){
            System.out.println(car);
        }
    }
    public void selectAll2(){
        String sql = "select * from jdbcCar";
        JdbcFactory factory = MySqlFactoryUtil.getFactory();
        JdbcUtil util = factory.getUtil();
        List<Car> list = util.selectList(sql,Car.class);
        for(Car car : list){
            System.out.println(car);
        }
    }
    //JDBC初步封装后的查方法
    public void selectCount(){
        String sql = "select count(*) from jdbcCar";
        JdbcUtil util = new JdbcUtil(
                driver,
                url,
                userName,
                password
        );
        List<Integer> list = util.selectList(sql,Integer.class);
        System.out.println(list);
    }
}

/**
 * 装载Car的策略
 */
class CarMapper implements RowMapper<Car> {

    @Override
    //String 对应列名   Object 对应列值
    public Car mapping(Map<String, Object> row) throws Exception {
        Car car = new Car();
        car.setNumber((Integer) row.get("number"));
        car.setName((String)row.get("name"));
        car.setColor((String)row.get("color"));
        car.setPrice((Integer)row.get("price"));
        return car;
    }
}