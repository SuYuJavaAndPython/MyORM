package com.jdbc;

import com.jdbc.pool.ConnectionPool;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 产生这个类的原因是因为增删改查一共四个操作，但是我们只提供了两个模板
 * 我们做的封装工具是给别人用的，别人不知道为什么四个操作就两个模板，因此把四个操作再细分出来
 * 这个工具类对外可以使用  其他类用默认不写（同包下才可访问）的修饰符来修饰保证封装性
 */
@SuppressWarnings("all")
public class JdbcUtil {

    private String driver;
    private String url;
    private String userName;
    private String password;

    public JdbcUtil(String driver, String url, String userName, String password) {
        this.driver = driver;
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    /**
     * 补充动态参数列表：
     * JDK1.5出现   ...指代0~n个参数（0也可以）
     * 动态参数列表本质上就是数组
     * 动态参数列表的方法不能与相同意义的数组类型方法构成重载（本质上是一样的）
     * 动态参数列表的方法可以不传参数（相当于0个）
     * 数组的方法必须传参   就算传0个也得传一个null占位
     * 另外注意动态参数列表的位置只能放方法参数的最后一个（不然放前面会产生歧义）
     */
    public int insert(String sql,Object...param){
        //加一个判断防止传错sql也执行了程序
        //equalsIgnoreCase忽略大小写判断
        //trim清除前后的空格
        if(sql.trim().substring(0,6).equalsIgnoreCase("insert")) {
            //这样写看着舒服
            JdbcUpdateTemplate insert = new JdbcUpdateTemplate(
                    driver,
                    url,
                    userName,
                    password
            );
            /**
             * 其实有了pool 上面这个模板的四个参数就用不着传了，因为pool已经加载过了
             * 但是为了不破坏原来的理解就保留下这种写法
             */
            insert.setPool(pool);
            return (int) insert.executeJdbc(sql,param);
        }else{
            //告诉用户你的sql传错了地方
            throw new SqlFormatException("not a insert sql {"+sql+"}");
        }
    }
    public int delete(String sql,Object...param){
        if(sql.trim().substring(0,6).equalsIgnoreCase("delete")) {
            //这样写看着舒服
            JdbcUpdateTemplate delete = new JdbcUpdateTemplate(
                    driver,
                    url,
                    userName,
                    password
            );
            delete.setPool(pool);
            //这里如果不传参   param相当于Object[] param = new Object[]{};
            //里面没内容,数组长度为0  但param也不为null
            return (int) delete.executeJdbc(sql,param);
        }else{
            //告诉用户你的sql传错了地方
            throw new SqlFormatException("not a delete sql {"+sql+"}");
        }
    }
    public int update(String sql,Object...param){
        if(sql.trim().substring(0,6).equalsIgnoreCase("update")) {
            //这样写看着舒服
            JdbcUpdateTemplate update = new JdbcUpdateTemplate(
                    driver,
                    url,
                    userName,
                    password
            );
            update.setPool(pool);
            return (int) update.executeJdbc(sql,param);
        }else{
            //告诉用户你的sql传错了地方
            throw new SqlFormatException("not a update sql {"+sql+"}");
        }
    }

    /**
     * 查询方法实现可以通过
     * ORM--Object Relation Mapping(将对象中的数据存入数据库或者将数据库中的数据读出来组成对象)
     * 1，策略模式实现不同的对象组成（策略模式实现ORM）
     *       优点：针对任意情况，非对象也可
     *       缺点：用户还需要传递策略，操作较麻烦
     * 2，也可以通过反射实现（反射实现ORM）
     *       优点：用户的操作变简单了
     *       缺点：只能针对对象来实现
     */
    //方式一   通过用户传递不同的策略来执行不同的操作
    public <T> List<T> selectList(String sql, RowMapper<T> strategy, Object...param) {
        if(sql.trim().substring(0,6).equalsIgnoreCase("select")) {
            //这样写看着舒服
            JdbcQueryTemplate select = new JdbcQueryTemplate(
                    driver,
                    url,
                    userName,
                    password
            );
            select.setPool(pool);
            //手动ORM
            List<Map<String,Object>> rs = (List<Map<String, Object>>) select.executeJdbc(sql,param);
            List<T> rows = new ArrayList<T>();
            try {
                //ORM 将查询到的表数据装载到java实体类中
                for (Map<String, Object> map : rs) {
                    T t = strategy.mapping(map);
                    rows.add(t);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return rows;
        }else{
            //告诉用户你的sql传错了地方
            throw new SqlFormatException("not a select sql {"+sql+"}");
        }
    }
    public <T> T selectOne(String sql, RowMapper<T> strategy, Object...param){
        //查询一条对象记录
        List<T> rows = this.selectList(sql,strategy,param);
        if(rows == null || rows.size() == 0){
            return null;
        }else{
            return rows.get(0);
        }
    }
    //查询结果不能组成对象的情况
    public List<Map<String,Object>> selectListMap(String sql, Object...param) {
        if(sql.trim().substring(0,6).equalsIgnoreCase("select")) {
            //这样写看着舒服
            JdbcQueryTemplate select = new JdbcQueryTemplate(
                    driver,
                    url,
                    userName,
                    password
            );
            select.setPool(pool);
            //手动ORM
            List<Map<String,Object>> rs = (List<Map<String, Object>>) select.executeJdbc(sql,param);
            return rs;
        }else{
            //告诉用户你的sql传错了地方
            throw new SqlFormatException("not a select sql {"+sql+"}");
        }
    }
    //查询一条Map记录
    public Map<String,Object> selectMap(String sql, Object...param){
        List<Map<String,Object>> rows = this.selectListMap(sql,param);
        if(rows == null || rows.size() == 0){
            return null;
        }else{
            return rows.get(0);
        }
    }
//=========================================================================================

    /**
     * selectList("select * from jdbcCar",Car.class);
     * selectList("select count(*) from jdbcCar",Integer.class);
     * selectList("select cname from jdbcCar",String.class);
     */
    //方式二  通过反射实现对任意对象的重组
    public <T> List<T> selectList(String sql, Class<T> type, Object...param) {
        if(sql.trim().substring(0,6).equalsIgnoreCase("select")) {
            //这样写看着舒服
            JdbcQueryTemplate select = new JdbcQueryTemplate(
                    driver,
                    url,
                    userName,
                    password
            );
            //手动ORM
            select.setPool(pool);
            List<Map<String,Object>> rs = (List<Map<String, Object>>) select.executeJdbc(sql,param);
            List<T> rows = new ArrayList<T>();
            try {
                //ORM 将查询到的表数据装载到java实体类中
                for (Map<String, Object> map : rs) {
                    Object row = null;
                    //这个框架暂时只考虑以下四种情况
                    if(type == int.class || type == Integer.class){
                        //获取所有的值
                        Collection cs = map.values();
                        for(Object c : cs){
                            //不用 row = (Integer)c 是为了防止出现造型异常(即c可能跟造型的类型不匹配)
                            row = ((Number)c).intValue();
                        }
                    }else if(type == long.class || type == Long.class){
                        Collection cs = map.values();
                        for(Object c : cs){
                            row = ((Number)c).longValue();
                        }
                    }else if(type == double.class || type == Double.class){
                        Collection cs = map.values();
                        for(Object c : cs){
                            row = ((Number)c).doubleValue();
                        }
                    }else if(type == String.class){
                        Collection cs = map.values();
                        for(Object c : cs){
                            row = (String)c;
                        }
                    }else{
                        //获取type对应的对象
                        row = type.newInstance();
                        //注意程序能走到这 证明我们需要将List中的每一个Map组成一个与type类型对应的对象
                        //接下来需要通过调用t的set方法给t赋值
                        Method[] methods = type.getMethods();
                        //注意这里为什么用getMethods方法而不直接获取属性名
                        //是因为属性是私有的，虽然可以通过反射获取但破坏了封装性
                        //从封装特性的角度而言，更推荐通过set方法，找到对应属性，通过set方法为属性赋值
                        for(Method method : methods){
                            //获取方法名
                            String methodName = method.getName();
                            //如果这个方法是以set开头
                            if(methodName.startsWith("set")){
                                String fieldName = methodName.substring(3).toLowerCase();
                                //获取map中对应的value
                                Object value = map.get(fieldName);
                                //做一个判断 有可能存在表中的字段与实体类中的属性个数不相等的情况
                                if(value == null){
                                    //当前对象属性没有对应的表数据
                                    continue ;//继续判断下一个属性
                                }else {
                                    //当前属性有对应的表数据，使用set方法赋值
                                    //使用反射调用方法，并赋值。
                                    //map中的value都是Object类型 调用方法赋值时需要我们来判断类型并造型
                                    //获取方法对应的参数类型   我们自己知道只有一个  但jdk提供的是数组
                                    Class par = method.getParameterTypes()[0];
                                    if(par == int.class || par == Integer.class){
                                        method.invoke(row,((Number)value).intValue()) ;//car.setCno(value); , car.setCname(value)
                                    }else if(par == long.class || par == Long.class){
                                        method.invoke(row,((Number)value).longValue()) ;//car.setCno(value); , car.setCname(value)
                                    }else if(par == double.class || par == Double.class){
                                        method.invoke(row,((Number)value).doubleValue()) ;//car.setCno(value); , car.setCname(value)
                                    }else if(par == String.class ){
                                        method.invoke(row,(String)value) ;//car.setCno(value); , car.setCname(value)
                                    }
                                }
                            }
                        }
                    }
                    rows.add((T) row);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return rows;
        }else{
            //告诉用户你的sql传错了地方
            throw new SqlFormatException("not a select sql {"+sql+"}");
        }
    }
    public <T> T selectOne(String sql ,Class<T> type, Object...param){
        List<T> rows = selectList(sql,type,param);
        if(rows == null || rows.size() == 0){
            return null ;
        }else{
            return rows.get(0) ;
        }
    }

    private ConnectionPool pool;
    public void setPool(ConnectionPool pool) {
        this.pool = pool;
    }
}