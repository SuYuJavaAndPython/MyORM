package com.dao;

import com.domain.Car;
import com.jdbc.SqlSession;
import com.util.MySqlFactoryUtil;

import java.util.List;

/**
 * 依据MVC分层架构思想  dao层是service层调用的dao层得到的结果要返回给service层
 */
public class NewCarDao {

    public int save(Car car){
        String sql = "insert into jdbcCar values(null,#{name},#{color},#{price})";
        SqlSession session = MySqlFactoryUtil.getFactory().getSession();
        int count = session.insert(sql,car);
        return count;
    }

    public int delete(int number){
        String sql = "delete from jdbcCar where number = #{number}";
        SqlSession session = MySqlFactoryUtil.getFactory().getSession();
        int count = session.delete(sql,number);
        return count;
    }

    public List<Car> findAll(){
        String sql = "select * from jdbcCar";
        SqlSession session = MySqlFactoryUtil.getFactory().getSession();
        List<Car> list = session.selectList(sql,Car.class);
//        for(Car car : list){
//            System.out.println(car);
//        }
        return list;
    }
}
