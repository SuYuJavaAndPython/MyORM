package com.suyu.test.dao;

import com.suyu.jdbc.SqlSession;
import com.suyu.test.domain.Car;

public class CarDao {

    /**
     * 经过初步封装已经实现通过创建JdbcUtil工具来实现我们对数据库中数据的增删改查操作
     * 而连接是连接池中获取的   多线程时就能体现出连接池的优越性    性能更高
     * 初步封装将jdbc六部曲的固定流程封装在JdbcTemplate中    我们用util工具实际上的操作是由
     * JdbcTemplate完成的(封装性不让用户看到具体操作)，由于六部曲中增删改和查的具体操作有不同  所以无法将增删改和查封装在一起
     * 于是诞生了另外两个模板      JdbcQueryTemplate和JdbcUpdateTemplate来分别实现具体的查和增删改操作
     * 虽然操作较之前简单了一点 但新的问题又出现了：
     *      eg：下面的insert操作   一般传到dao层的数据都会被包装成对象 我们挨个从car对象中取
     *      在挨个传参（这个操作有点麻烦）
     *      于是我们想直接传一个car对象，让框架帮我们传具体的参数（问题又来了，如何与sql中的？对应呢）
     *      于是我们更改了原来的sql语法（用户使用时的语法，这是我们擅自更改的，实际上数据库可不认识）
     *      让传递的参数位置与？一致
     *      所以又需要框架帮我们解析用户（即使用我们框架的人）传递的sql
     *      用户sql：insert into jdbccar values(#{number},#{name},#{color},#{price})
     *      框架解析成数据库认识的sql：insert into jdbccar values(?,?,?,?)
     */

    /**
     * 优化前返回List<Map<String,Object>>：
     */
//    public void insertOne(){
//        Car car = new Car(null,"劳斯莱斯","玫瑰金",5000000);
//        String sql = "insert into jdbccar values(?,?,?,?)";
//        JdbcUtil util = new JdbcUtil();
//        int row = util.insert(sql,car.getNumber(),car.getName(),car.getColor(),car.getPrice());
//        System.out.println(row);
//    }
//    public void selectOne(){
//        JdbcUtil util = new JdbcUtil();
//        List<Map<String,Object>> cars = util.selectListMap("select * from jdbccar");
//        System.out.println(cars);
//    }

    /**
     * 优化后返回用户指定的对象集(通过策略模式实现)：
     */
//    public void selectAll(){
//        String sql = "select * from jdbccar";
//        JdbcUtil util = new JdbcUtil();
//        //匿名内部类更直观
//        List<Car> cars = util.selectList(sql, new Mapper<Car>() {
//            @Override
//            public Car orm(Map<String, Object> row) {
//                Car car = new Car();
//                car.setNumber((Integer) row.get("number"));
//                car.setName((String) row.get("name"));
//                car.setColor((String) row.get("color"));
//                car.setPrice((Integer) row.get("price"));
//                return car;
//            }
//        });
//        for(Car car : cars){
//            System.out.println(car);
//        }
//    }
//    public void selectOne(){
//        //注意count(*)函数返回值是long类型的
//        String sql = "select count(*) total from jdbccar";
//        JdbcUtil util = new JdbcUtil();
//        Long count = util.selectOne(sql, new Mapper<Long>() {
//            @Override
//            public Long orm(Map<String, Object> row) {
//                return (Long) row.get("total");
//            }
//        });
//        System.out.println(count);
//    }

    /**
     * 优化后返回用户指定的对象集(通过反射实现)：
     */
//    public void selectAll(){
//        String sql = "select * from jdbccar";
//        JdbcUtil util = new JdbcUtil();
//        //匿名内部类更直观
//        List<Car> cars = util.selectList(sql, Car.class);
//        for(Car car : cars){
//            System.out.println(car);
//        }
//    }
//    public void selectOne(){
//        //注意count(*)函数返回值是long类型的
//        String sql = "select count(*) total from jdbccar";
//        JdbcUtil util = new JdbcUtil();
//        Long count = util.selectOne(sql, Long.class);
//        System.out.println(count);
//    }

    /**
     * 进一步优化后实现新的sql语法（我们自己定义的,数据库可不认识需要小弟(SqlHandle)来解析）:
     * 旧语法用JdbcUtil工具   新语法用SqlSession工具
     */
    public void insertOne(){
        Car car = new Car(null,"保时捷","尊贵银",6000000);
        String sql = "insert into jdbccar values(#{number},#{name},#{color},#{price})";
        SqlSession session = new SqlSession();
        int row = session.insert(sql,car);
        System.out.println(row);
    }

/**
 * 经过上面的封装后已经实现了：
 *      1.第一轮优化了获取连接的过程（通过连接池获取）形成了新的六部曲
 *          从总体上看：节省了后期获取连接的时间，提高了效率
 *          （创建连接是一个耗时的过程，连接本身是一个昂贵的资源）
 *      2.第二轮优化了用户（dao层）使用jdbc流程与数据库交互的过程
 *          因为发现流程基本一致，于是将六部曲封装在JdbcTemplate中
 *          实际上用户使用的是JdbcUtil中的insert，delete，update，selectListMap，selectMap
 *          selectList（用户传递策略）           selectList（框架反射自动）
 *          selectOne（用户传递策略）            selectOne（框架反射自动）
 *          实现了不同的返回类型以满足用户的需要
 *      3.第三轮优化了用户传递参数的过程，不需要用户自己将从service层传过来的对象拆开逐一传入方法中
 *          优化后用户只需要传一个参数即可（框架暂时只支持一个参数）
 *          框架自动帮用户将参数拆开按顺序装入sql语句中
 *  这个参数可以是int，Integer，float，Float，double，Double，String，Map，HashMap，domain对象
 *          但是要按顺序传参需要满足新的sql语法 eg：insert into 表名 values(#{某一个参数名},#{}...)
 *          按sql中的某一个参数顺序来传参
 *      4.第四轮进一步优化了用户使用框架的过程
 *          经过前三轮优化，用户使用框架只需要如下几个步骤：
 *              1)创建一个工具（可以是原始sql语法的JdbcUtil，也可以是新sql语法的SqlSession）
 *              2)用户写一行String sql;
 *              3)用户使用工具中的方法将sql和从service层获取的参数传入方法中
 *              4)将执行sql后的结果返回给service层
 *          发现这四步是固定的，于是想到让框架帮我们做：
 *              于是用户只需要是一个接口，将具体执行增删改查的方法上添加对应的注解
 *                  注解内容为我们要执行的sql语句
 *                  方法参数为从service层传过来的参数
 *                  返回值为执行sql后返回的结果
 *              框架动态生成代理类实现用户接口，反射自动执行上面的四个步骤（体现动态代理模式）
 */

}
