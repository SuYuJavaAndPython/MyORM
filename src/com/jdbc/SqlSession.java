package com.jdbc;

import com.jdbc.annotations.Delete;
import com.jdbc.annotations.Insert;
import com.jdbc.annotations.Update;
import com.jdbc.annotations.Select;
import com.jdbc.pool.ConnectionPool;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

/**
 * 实现jdbc交互
 * 与JdbcUtil作用相同(注意我们实际操作时用的还是JdbcUtil,只不过我们将传进来的参数做了一个解析)
 * 当前对象针对于一种新的sql语法
 * insert into t_car values(null,#{cname},#{color},#{price})
 */
public class SqlSession {

    //本质还是用util的方法
    private JdbcUtil util;

    public SqlSession(String driver,String url,String userName,String password,ConnectionPool pool) {
        this.util = new JdbcUtil(driver,url,userName,password);
        this.util.setPool(pool);
    }

    /**
     * 注意SqlSession代表的是一种新的自定义sql语法     所以我们设计的方法参数是 Object param
     * 将我们需要传递的参数组成对象传入方法中进行解析  但实际上我们可能不传参数 所以用户就还得传个null当param
     * 这样影响用户体验 因此我们给每个方法都设计一个重载方法
     * (这个重载方法实际上就是调用的JdbcUtil（动态参数列表）的方法)
     */

    public int insert(String sql, Object param){
        /*
        insert into t_car values(null,#{cname},#{color},#{price})
        -->insert into t_car values(null,?,?,?)
        将sql做一个处理，找SqlHandler小弟来解析sql并对参数做一个处理
        处理结果得到一个SqlAndParam对象，里面包含解析后带？的sql，以及参数数组
         */
        SqlAndParam sp = SqlHandler.execute(sql, param);
        return this.util.insert(sp.sql,sp.params.toArray());
    }
    public int insert(String sql){
        return this.util.insert(sql);
    }

    public int update(String sql , Object param){
        SqlAndParam sp = SqlHandler.execute(sql,param) ;
        return util.update(sp.sql,sp.params.toArray());
    }
    public int update(String sql){
        return util.update(sql);
    }

    public int delete(String sql , Object param){
        SqlAndParam sp = SqlHandler.execute(sql,param) ;
        return util.delete(sp.sql,sp.params.toArray());
    }
    public int delete(String sql){
        return util.delete(sql);
    }

    public List<Map<String,Object>> selectListMap(String sql , Object param){
        SqlAndParam sp = SqlHandler.execute(sql,param) ;
        return util.selectListMap(sp.sql,sp.params.toArray());
    }
    public List<Map<String,Object>> selectListMap(String sql){
        return util.selectListMap(sql);
    }

    public Map<String,Object> selectMap(String sql , Object param){
        SqlAndParam sp = SqlHandler.execute(sql,param) ;
        return util.selectMap(sp.sql,sp.params.toArray());
    }
    public Map<String,Object> selectMap(String sql){
        return util.selectMap(sql);
    }

    public <T> List<T> selectList(String sql , Object param , Class<T> type){
        SqlAndParam sp = SqlHandler.execute(sql,param) ;
        return util.selectList(sp.sql,type,sp.params.toArray());
    }
    public <T> List<T> selectList(String sql, Class<T> type){
        return util.selectList(sql,type);
    }

    public <T> T selectOne(String sql , Object param , Class<T> type){
        SqlAndParam sp = SqlHandler.execute(sql,param) ;
        return util.selectOne(sp.sql,type,sp.params.toArray());
    }
    public <T> T selectOne(String sql, Class<T> type){
        return util.selectOne(sql,type);
    }

    /**
     * 根据指定的dao接口规则，由动态代理机制创建其对应的实现类
     * @param daoInterface
     * @param <T>
     * @return
     */
    public <T> T createDaoImplClass(Class<T> daoInterface){
        ClassLoader classLoader = daoInterface.getClassLoader();
        //使用接口类时
        Class[] classes = new Class[]{daoInterface};
        //是使用实现类时   这里如果用这种方法会发生造型异常
        //Exception in thread "main" java.lang.ClassCastException:
        //com.sun.proxy.$Proxy0 cannot be cast to com.dao.EasyCarDao
//        Class[] classes = daoInterface.getInterfaces();
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                System.out.println(method.getName());
//                System.out.println(args);
                //我们的dao接口方法上我们自己知道就一个注解
                Annotation annotation = method.getAnnotations()[0];
                Class annotationClass = annotation.getClass();
                Method annotationMethod = annotationClass.getMethod("value");
                String sql = (String)annotationMethod.invoke(annotation);
                //获取本次方法调用时传递的参数    框架规定只能传1个或者0个参数
                //如果想要传多个参数     使用者需要组成1个对象在进行参数传递
                Object param = (null == args) ? null : args[0];
                Object result = null;
                if(annotation.annotationType() == Insert.class){
                    if(param == null){
                        result = SqlSession.this.insert(sql);
                    }else {
                        result = SqlSession.this.insert(sql, param);
                    }
                }else if(annotation.annotationType() == Delete.class){
                    if(param == null){
                        result = SqlSession.this.delete(sql);
                    }else {
                        result = SqlSession.this.delete(sql, param);
                    }
                }else if(annotation.annotationType() == Update.class){
                    if(param == null){
                        result = SqlSession.this.update(sql);
                    }else {
                        result = SqlSession.this.update(sql, param);
                    }
                }else if(annotation.annotationType() == Select.class){
                    //根据返回类型判断调用哪一个select方法
                    Class returnClass = method.getReturnType();     //这个方法获取的返回类型不包括泛型
                    if(returnClass == List.class) {
                        //查询结果组成的类型应该是方法返回类型中的泛型    List<Car>-->Car
                        Type intactType = method.getGenericReturnType();  //获得完整的返回类型（包括泛型）
                        //ParameterizedType 所有的泛型实现他
                        // ---> 他又继承她 --->
                        //Type 所有的类型(返回类型，参数类型...)实现她
                        //Type 是所有类型的爹  需要强转成可以获得泛型的类型
                        ParameterizedType genericsType = (ParameterizedType)intactType;
                        //获取List集合中的那一个泛型参数
                        Class genericsClass = (Class)genericsType.getActualTypeArguments()[0];
                        if(genericsClass == Map.class){
                            if(param == null){
                                result = SqlSession.this.selectListMap(sql);
                            }else {
                                result = SqlSession.this.selectListMap(sql, param);
                            }
                        }else{
                            if(param == null){
                                result = SqlSession.this.selectList(sql,genericsClass);
                            }else {
                                result = SqlSession.this.selectList(sql, param, genericsClass);
                            }
                        }
                    }else{
                        if(returnClass == Map.class){
                            if(param == null){
                                result = SqlSession.this.selectMap(sql);
                            }else {
                                result = SqlSession.this.selectMap(sql,param);
                            }
                        }else {
                            if(param == null){
                                result = SqlSession.this.selectOne(sql,returnClass);
                            }else {
                                result = SqlSession.this.selectOne(sql, param, returnClass);
                            }
                        }
                    }
                }
                return result;
            }
        };
        T daoProxy = (T)Proxy.newProxyInstance(classLoader,classes,invocationHandler);
        return daoProxy;
    }
}
