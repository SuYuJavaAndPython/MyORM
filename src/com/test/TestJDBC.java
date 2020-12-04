package com.test;

import com.dao.CarDao;
import com.dao.EasyCarDao;
import com.dao.NewCarDao;
import com.domain.Car;
import com.util.MySqlFactoryUtil;

import java.util.List;
import java.util.Scanner;

public class TestJDBC {

    public static void main(String[] args){

        //JDBC未封装前的六部曲测试样例
//        Car car = new Car(null,"宝马","浅蓝色",500000);
//        CarDao dao = new CarDao();
//        dao.save(car);

////        JDBC初步封装后的测试样例
//        Car car = new Car(null,"劳斯莱斯幻影","浅蓝色",8000000);
//        CarDao dao = new CarDao();
//        System.out.println(dao.save4(car));

//        CarDao dao = new CarDao();
//        String sql = "delete from jdbcCar where number = 4";
//        System.out.println(dao.deleteInformation(sql));

//        CarDao dao = new CarDao();
//        dao.selectAll();
//        dao.selectCount();
//        dao.selectAll2();

//        NewCarDao newDao = new NewCarDao();
//        newDao.findAll();

        EasyCarDao dao = MySqlFactoryUtil.getFactory().getSession().createDaoImplClass(EasyCarDao.class);
//        Car car = new Car(null,"劳斯莱斯","玫瑰金",9999999);
//        System.out.println(dao.save(car));
        for(int i = 1; i <= 100; i++) {
            List<Car> list = dao.findAll();
            for (Car car1 : list) {
                System.out.println(car1);
            }
            System.out.println(i + "===================================");
        }

    }
}
