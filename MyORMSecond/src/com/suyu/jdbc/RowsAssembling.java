package com.suyu.jdbc;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * 工具类负责帮我们将rows多行记录组装成多个对象    别人不能用
 */
@SuppressWarnings("all")
public final class RowsAssembling {

    private RowsAssembling(){}

    static <T>T assembling(Map<String,Object> row, Class<T> type) {
        Object obj = null;
        try {
            //这个框架暂时只考虑以下四种情况
            if (type == int.class || type == Integer.class) {
                //获取所有的值
                Collection cs = row.values();
                for (Object c : cs) {
                    //不用 row = (Integer)c 是为了防止出现造型异常(即c可能跟造型的类型不匹配)
                    obj = ((Number) c).intValue();
                }
            } else if (type == long.class || type == Long.class) {
                Collection cs = row.values();
                for (Object c : cs) {
                    obj = ((Number) c).longValue();
                }
            } else if (type == double.class || type == Double.class) {
                Collection cs = row.values();
                for (Object c : cs) {
                    obj = ((Number) c).doubleValue();
                }
            } else if (type == String.class) {
                Collection cs = row.values();
                for (Object c : cs) {
                    obj = (String) c;
                }
            } else {
                //获取type对应的对象
                obj = type.newInstance();
                //注意程序能走到这 证明我们需要将List中的每一个Map组成一个与type类型对应的对象
                //接下来需要通过调用t的set方法给t赋值
                Method[] methods = type.getMethods();
                //注意这里为什么用getMethods方法而不直接获取属性名
                //是因为属性是私有的，虽然可以通过反射获取但破坏了封装性
                //从封装特性的角度而言，更推荐通过set方法，找到对应属性，通过set方法为属性赋值
                for (Method method : methods) {
                    //获取方法名
                    String methodName = method.getName();
                    //如果这个方法是以set开头
                    if (methodName.startsWith("set")) {
                        String fieldName = methodName.substring(3).toLowerCase();
                        //获取map中对应的value
                        Object value = row.get(fieldName);
                        //做一个判断 有可能存在表中的字段与实体类中的属性个数不相等的情况
                        if (value == null) {
                            //当前对象属性没有对应的表数据
                            continue;//继续判断下一个属性
                        }
                        //当前属性有对应的表数据，使用set方法赋值
                        //使用反射调用方法，并赋值。
                        //map中的value都是Object类型 调用方法赋值时需要我们来判断类型并造型
                        //获取方法对应的参数类型   我们自己知道只有一个  但jdk提供的是数组
                        Class par = method.getParameterTypes()[0];
                        if (par == int.class || par == Integer.class) {
                            method.invoke(obj, ((Number) value).intValue());//car.setCno(value); , car.setCname(value)
                        } else if (par == long.class || par == Long.class) {
                            method.invoke(obj, ((Number) value).longValue());//car.setCno(value); , car.setCname(value)
                        } else if (par == double.class || par == Double.class) {
                            method.invoke(obj, ((Number) value).doubleValue());//car.setCno(value); , car.setCname(value)
                        } else if (par == String.class) {
                            method.invoke(obj, (String) value);//car.setCno(value); , car.setCname(value)
                        } else {
                            //如果是其他类型暂时不做判断但也不报错 框架不支持
                            continue;
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return (T) obj;
    }
}
