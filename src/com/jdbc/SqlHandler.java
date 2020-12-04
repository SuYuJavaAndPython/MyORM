package com.jdbc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  * 为sqlSession对象执行的新版sql进行处理
 *  * 将其处理成原生sql
 *  * 额外处理参数
 *  *  将带#{cname}SQL语句，处理成?的sql语句
 *  *      insert into t_car values(null,#{cname},#{color},#{price})
 *  *      ->insert into t_car values(null,?,?,?)
 *  *  将对象参数，处理成数组参数
 *  *      car
 *  *      -> Object[]{car.cname,car.color,car.price}
 */
public class SqlHandler {

    public static SqlAndParam execute(String sql,Object param){
        try{
            //将#{}中的内容按顺序存入的List中，然后再去param中找对应的参数
            List<String> keys = new ArrayList<>();
            while(true) {
                int i1 = sql.indexOf("#{");
                int i2 = sql.indexOf("}");
                if (i1 > i2 || i1 == -1 || i2 == -1) {
                    //sql中没有成对的#{},处理完毕了
                    break;
                }
                //获得一个#{key}中的key
                String key = sql.substring(i1 + 2, i2).trim();
                //将#{}中的内容存入keys中
                keys.add(key);
                //将#{}以及其中的内容整体替换为？
                if (i2 == sql.length() - 1) {
                    //已经到达结尾
                    sql = sql.substring(0, i1) + "?";
                    break;
                } else {
                    sql = sql.substring(0, i1) + "?" + sql.substring(i2 + 1);
                    continue;
                }
            }
            //经过上面这个循环后keys已经装载完毕
            //按顺序存入参数
            /*
                判断传递的参数类型
                如果传递是一个简单的类型(int,string,double),表示sql中应该只有一个#{}
                select("select * from t_car where cno = #{cno}",1001);
                如果传递是一个对象，就需要反射根据key获得属性值
            */
            List<Object> params = new ArrayList<>();
            Class c = param.getClass();
            if (
                c == int.class ||
                c == Integer.class ||
                c == double.class ||
                c == Double.class ||
                c == String.class
            ) {
                //表示sql中只有一个?   那么直接装进params中即可
                params.add(param);
            } else if (c == Map.class || c == HashMap.class) {
                for (String key : keys) {
                    //key = cno .    map.get("cno");
                    Map map = (Map) param;
                    Object value = map.get(key);
                    params.add(value);
                }
            } else {
                //是一个对象，需要使用反射，根据keys 依次获得每一个key对应的属性值
                for (String key : keys) {
                    //key = cno     get + C + no -> getCno
                    String methodName = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
                    Method method = c.getMethod(methodName);
                    Object value = method.invoke(param);//car.getCno();
                    params.add(value);
                }
            }
            SqlAndParam sp = new SqlAndParam();
            sp.sql = sql;
            sp.params = params;
            return sp;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null ;
    }
}
