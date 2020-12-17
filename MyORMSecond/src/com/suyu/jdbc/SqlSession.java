package com.suyu.jdbc;

import com.suyu.jdbc.annotations.Delete;
import com.suyu.jdbc.annotations.Insert;
import com.suyu.jdbc.annotations.Select;
import com.suyu.jdbc.annotations.Update;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

/**
 * 也是跟JdbcUtil一样的jdbc工具 实际上还是用的util工具
 * 不过实现了我们自己定义的新sql语法
 */
public class SqlSession {

    //SqlSession的具体实现还是靠util实现
    private JdbcUtil util = new JdbcUtil();

    /**
     * 因为实现的是新语法    所以用户传参的过程就变简单了   eg：直接传一个对象 因此参数不需要动态参数
     * 注意SqlSession代表的是一种新的自定义sql语法     所以我们设计的方法参数是 Object param
     * 将我们需要传递的参数组成对象传入方法中进行解析  但实际上我们可能不传参数 所以用户就还得传个null当param
     * 这样影响用户体验 因此我们给每个方法都设计一个重载方法
     * (这个重载方法实际上就是调用的JdbcUtil（动态参数列表）的方法)
     * @param sql
     * @param param
     * @return
     */
    public int insert(String sql, Object param){
        SqlAndParam sqlAndParam = SqlHandle.execute(sql,param);
        return this.util.insert(sqlAndParam.sql, sqlAndParam.params.toArray());
    }
    public int insert(String sql){
        return this.util.insert(sql);
    }

    public int delete(String sql, Object param){
        SqlAndParam sqlAndParam = SqlHandle.execute(sql,param);
        return this.util.delete(sqlAndParam.sql, sqlAndParam.params.toArray());
    }
    public int delete(String sql){
        return this.util.delete(sql);
    }

    public int update(String sql, Object param){
        SqlAndParam sqlAndParam = SqlHandle.execute(sql,param);
        return this.util.update(sqlAndParam.sql, sqlAndParam.params.toArray());
    }
    public int update(String sql){
        return this.util.update(sql);
    }

    public List<Map<String,Object>> selectListMap(String sql, Object param){
        SqlAndParam sqlAndParam = SqlHandle.execute(sql,param);
        return this.util.selectListMap(sqlAndParam.sql,sqlAndParam.params.toArray());
    }
    public List<Map<String,Object>> selectListMap(String sql){
        return this.util.selectListMap(sql);
    }

    public Map<String,Object> selectMap(String sql, Object param){
        SqlAndParam sqlAndParam = SqlHandle.execute(sql,param);
        return this.util.selectMap(sqlAndParam.sql,sqlAndParam.params.toArray());
    }
    public Map<String,Object> selectMap(String sql){
        return this.util.selectMap(sql);
    }

    public <T> List<T> selectList(String sql, Class<T> type, Object param){
        SqlAndParam sqlAndParam = SqlHandle.execute(sql,param);
        return this.util.selectList(sqlAndParam.sql,type,sqlAndParam.params);
    }
    public <T> List<T> selectList(String sql, Class<T> type){
        return this.util.selectList(sql,type);
    }

    public <T> T selectOne(String sql, Class<T> type, Object param){
        SqlAndParam sqlAndParam = SqlHandle.execute(sql,param);
        return this.util.selectOne(sqlAndParam.sql,type,sqlAndParam.params);
    }
    public <T> T selectOne(String sql, Class<T> type){
        return this.util.selectOne(sql,type);
    }

    /**
     * 根据指定的dao接口规则，由动态代理机制创建其对应的实现类
     * 实现类中的invoke方法就是要执行的接口中的那个方法（只不过换成能执行的）
     * @param daoInterface
     * @param <T>
     * @return
     */
    public <T> T createDaoImplClass(Class<T> daoInterface){
        ClassLoader classLoader = daoInterface.getClassLoader();
        Class[] classes = new Class[]{daoInterface};
        InvocationHandler invocationHandler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //假定使用框架的用户只能在方法上添加一个注解     把注解当做我们平时正常写的类会好理解一点
                Annotation annotation = method.getAnnotations()[0];
                //获取注解的类型
                Class annotationClass = annotation.getClass();
                //获取注解中的value方法类型
                Method annotationMethod = annotationClass.getMethod("value");
                //执行注解中的value方法获取方法脑瓜顶上的注解内容sql
                String sql = (String) annotationMethod.invoke(annotation);
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
                                result = SqlSession.this.selectList(sql, genericsClass);
                            }else {
                                result = SqlSession.this.selectList(sql, genericsClass, param);
                            }
                        }
                    }else{
                        if(returnClass == Map.class){
                            if(param == null){
                                result = SqlSession.this.selectMap(sql);
                            }else {
                                result = SqlSession.this.selectMap(sql, param);
                            }
                        }else {
                            if(param == null){
                                result = SqlSession.this.selectOne(sql, returnClass);
                            }else {
                                result = SqlSession.this.selectOne(sql, returnClass, param);
                            }
                        }
                    }
                }
                return result;
            }
        };
        T daoProxy = (T) Proxy.newProxyInstance(classLoader,classes,invocationHandler);
        return daoProxy;
    }
}
