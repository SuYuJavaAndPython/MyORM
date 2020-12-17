package com.suyu.test.service;

import com.suyu.jdbc.SqlSession;
import com.suyu.test.dao.AnnotationCarDao;
import com.suyu.test.domain.Car;

import java.util.List;

/**
 * 这是Car的业务层类 模拟真实项目中的场景
 * 故将主函数写在这调用dao层的方法测试
 */
public class CarService {

    public static void main(String[] args){
        SqlSession sqlSession = new SqlSession();
        AnnotationCarDao dao = sqlSession.createDaoImplClass(AnnotationCarDao.class);

        List<Car> carList = dao.selectAll();
        for (Car car : carList){
            System.out.println(car);
        }

//        HashMap<String,Object> map = new HashMap<>();
//        map.put("number",1);
//        map.put("name","qq");
//        int row = dao.updateOne(map);
//        System.out.println(row);
    }
}
