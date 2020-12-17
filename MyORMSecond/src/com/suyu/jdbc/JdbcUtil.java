package com.suyu.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 这个类出现的目的是充当jdbc工具
 *    (工具对外只提供五个方法insert delete update selectListMap（查询多行记录） selectMap（查询一行记录）)
 * 原来：1.导包 2.加载驱动类 3.创建连接 4.创建状态参数 5.执行sql及具体操作 6.关闭连接通道
 * 现在：（通过jdbc连接池）1.导包 2.获取连接池对象 3.获取连接 4.创建状态参数 5.执行sql及具体操作 6.关闭连接通道
 * 根据MVC分层架构的思想
 *          V   view视图层（html，css，js，jsp...）
 *          C   controller控制层（Servlet）
 *          M   model模型层
 *                          service（业务层）处理业务逻辑
 *                          dao（dao层）与数据库进行数据交互
 *                          domain（杜曼实体对象也叫javaBean）一般用于存储数据库中的一行记录，不作为一个层次
 *                          DB（DataBase数据库）
 * 我们原来通过原始的jdbc六部曲完成与数据库交互的过程
 * 现再通过封装的连接池来与数据库进行数据的交互   大大提高了底层性能
 * （因为对于连接Connection来说，创建连接是一个耗时的过程，连接本身是一个昂贵的资源）
 *      我们发现dao层每次与数据库进行数据交互的过程  其流程基本一致，于是我们想把这个交互数据的流程封装起来
 *      于是就产生了ORM框架（Object Relation Mapping）
 */
@SuppressWarnings("all")
public class JdbcUtil {

    //JdbcUtil需要JdbcQueryTemplate和JdbcUpdateTemplate来支持执行JdbcTemplate模板中封装起来的六部曲
    private static JdbcQueryTemplate queryTemplate = new JdbcQueryTemplate();
    private static JdbcUpdateTemplate updateTemplate = new JdbcUpdateTemplate();

    /**
     * 执行数据库的增操作    返回受影响的行数
     * @param sql
     * @param param
     * @return
     */
    public int insert(String sql,Object...param){
        if(sql.substring(0,6).equalsIgnoreCase("insert")){
            return (int) updateTemplate.executeJdbc(sql,param);
        }
        //程序走到这代表用户传的语句不是一条inset语句但是却调用了insert方法    抛出自定义异常提示用户
        throw new SqlFormatException("not a insert statement { " + sql + " }");
    }

    /**
     * 执行数据库的删操作    返回受影响的行数
     * @param sql
     * @param param
     * @return
     */
    public int delete(String sql,Object...param){
        if(sql.substring(0,6).equalsIgnoreCase("delete")){
            return (int) updateTemplate.executeJdbc(sql,param);
        }
        //程序走到这代表用户传的语句不是一条inset语句但是却调用了insert方法    抛出自定义异常提示用户
        throw new SqlFormatException("not a delete statement { " + sql + " }");
    }

    /**
     * 执行数据库的增操作    返回受影响的行数
     * @param sql
     * @param param
     * @return
     */
    public int update(String sql,Object...param){
        if(sql.substring(0,6).equalsIgnoreCase("update")){
            return (int) updateTemplate.executeJdbc(sql,param);
        }
        //程序走到这代表用户传的语句不是一条inset语句但是却调用了insert方法    抛出自定义异常提示用户
        throw new SqlFormatException("not a update statement { " + sql + " }");
    }

    //============================================================================

    //原来的查询
    /**
     * 执行数据库的查操作    返回查询结果
     * 注意引用类型的动态参数数组如果不传也会给你创建这个空间  不会为null
     * @param sql
     * @param param
     * @return
     */
    public List<Map<String,Object>> selectListMap(String sql,Object...param){
        if(sql.substring(0,6).equalsIgnoreCase("select")){
            return (List<Map<String,Object>>)queryTemplate.executeJdbc(sql,param);
        }
        //程序走到这代表用户传的语句不是一条inset语句但是却调用了insert方法    抛出自定义异常提示用户
        throw new SqlFormatException("not a select statement { " + sql + " }");
    }

    /**
     * 执行数据库的查操作    返回查询结果
     * @param sql
     * @param param
     * @return
     */
    public Map<String,Object> selectMap(String sql,Object...param){
        //查询一条对象记录
        List<Map<String,Object>> rows = this.selectListMap(sql,param);
        if(rows != null && rows.size() == 1){
            return rows.get(0);
        }else if(rows == null || rows.size() == 0){
            return null;
        }else{
            //走到这代表用户传了一条查询多条记录的sql语句却调用了查询单条记录的方法  此时抛出自定义异常提示用户
            throw new RowCountException("need query one result but { " + sql + " }");
        }
    }

    //==================================================================================

    //现在的查询
    /**
     * 查询方法实现可以通过
     * ORM--Object Relation Mapping(将对象中的数据存入数据库或者将数据库中的数据读出来组成对象)
     * 1，策略模式实现不同的对象组成（策略模式实现ORM）
     *       优点：针对任意情况，非对象也可
     *       缺点：用户还需要传递策略，操作较麻烦
     * 2，也可以通过反射实现（反射实现ORM）
     *       优点：用户的操作变简单了
     *       缺点：只能针对对象来实现
     * 对于不同的查询操作    我们需要实现不同的返回结果，返回结果随着sql语句的不同返回结果不同
     * 因此想到用泛型  返回值为T   和List<T>    两种即可表示所有返回种类
     */

    /**
     * 根据用户提供的策略来将从数据库中得到的一行行记录组成一个个T   最后装进List返回
     * 怎么组装是用户提供的策略     框架执行用户提供的策略即可
     * @param sql
     * @param strategy
     * @param param
     * @param <T>
     * @return
     */
    public <T>List<T> selectList(String sql, Mapper<T> strategy, Object...param){
        List<T> result = new ArrayList<>();
        List<Map<String,Object>> rows = this.selectListMap(sql,param);
        for(Map<String,Object> row : rows){
            T obj = strategy.orm(row);
            result.add(obj);
        }
        return result;
    }
    public <T>T selectOne(String sql, Mapper<T> strategy, Object...param){
        T result = null;
        Map<String,Object> row = this.selectMap(sql,param);
        result = strategy.orm(row);
        return result;
    }

    /**
     * 根据用户传递的类型type来将结果组装成type类型返回 具体怎么组装还需要框架来完成
     * @param sql
     * @param type
     * @param param
     * @param <T>
     * @return
     */
    public <T>List<T> selectList(String sql, Class<T> type, Object...param){
        List<T> result = new ArrayList<>();
        List<Map<String,Object>> rows = this.selectListMap(sql,param);
        for (Map<String, Object> row : rows) {
            T obj = RowsAssembling.assembling(row, type);
            result.add((T) obj);
        }
        return result;
    }
    public <T>T selectOne(String sql, Class<T> type, Object...param){
        T result = null;
        List<T> rows = this.selectList(sql,type,param);
        if(rows != null && rows.size() == 1){
            result = rows.get(0);
        }else if(rows != null && rows.size() > 1){
            //走到这代表用户传了一条查询多条记录的sql语句却调用了查询单条记录的方法  此时抛出自定义异常提示用户
            throw new RowCountException("need query one result but { " + sql + " }");
        }
        return result;
    }

}
