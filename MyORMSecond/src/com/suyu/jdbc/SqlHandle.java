package com.suyu.jdbc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  这个类的作用是为了在执行具体jdbc流程前处理用户传过来的sql和param
 *  解析成数据库认识符合语法的形式
 */
public class SqlHandle {

    /**
     * sql:   insert into jdbccar values(#{number},#{name},#{color},#{price})
     * @param sql
     * @param param
     * @return
     */
    public static SqlAndParam execute(String sql, Object param){
        SqlAndParam sqlAndParam = new SqlAndParam();
        try {
            List<String> names = new ArrayList<>();
            while (true) {
                int first = sql.indexOf("#{");
                int second = sql.indexOf("}");
                if (first > second || first == -1 || second == -1) {
                    //sql中没有成对的#{},处理完毕了
                    break;
                }
                //获取#{}中的参数名字
                String paramName = sql.substring(first + 2, second).trim();
                //将参数名字装入names
                names.add(paramName);
                //将#{}以及其中的内容整体替换为？
                if (second == sql.length() - 1) {
                    //已经到达结尾
                    sql = sql.substring(0, first) + "?";
                    break;
                } else {
                    sql = sql.substring(0, first) + "?" + sql.substring(second + 1);
                    continue;
                }
            }
            //通过names和用户传递的param参数反射获取对应类型
            //按顺序存入参数
            /**
             * 判断传递的参数类型
             * 如果传递是一个简单的类型(int,string,double),表示sql中应该只有一个#{}
             *          select("select * from t_car where cno = #{cno}",1001);
             *          如果传递是一个对象，就需要反射根据key获得属性值
             */
            List<Object> params = new ArrayList<>();
            Class paramClass = param.getClass();
            if(paramClass == Integer.class || paramClass == int.class ||
                    paramClass == Double.class || paramClass == double.class ||
                    paramClass == Float.class || paramClass == float.class ||
                    paramClass == String.class
            ){
                //证明就一个参数
                params.add(param);
            }else if(paramClass == Map.class || paramClass == HashMap.class){
                for(String name : names){
                    //循环将map集合中的值按顺序存入params集合中
                    Map map = (Map) param;
                    Object value = map.get(name);
                    params.add(value);
                }
            } else{
                //到这认为用户传递的param是一个domain对象
                for(String name : names){
                    //拼接domain属性的get方法名     如果属性名就一个长度
                    String getMethodName = "";
                    if(name.length() > 1) {
                        getMethodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
                    }else{
                        getMethodName = "get" + name.toUpperCase();
                    }
                    //获取属性名对应的get方法
                    Method getMethod = paramClass.getMethod(getMethodName);
                    //让属性名对应的get方法执行    同时将获取的值装入params集合中
                    params.add(getMethod.invoke(param));
                }
            }
            sqlAndParam.sql = sql;
            sqlAndParam.params = params;
        }catch (Exception e){
            e.printStackTrace();
        }
        return sqlAndParam;
    }
}
