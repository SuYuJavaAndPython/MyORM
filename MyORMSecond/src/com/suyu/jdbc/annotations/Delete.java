package com.suyu.jdbc.annotations;

import java.lang.annotation.*;

/**
 * 添加元注解描述我们自己定义的注解有什么作用能干什么事
 */
@Target(ElementType.METHOD)     //注解可以用在方法上
@Retention(RetentionPolicy.RUNTIME)     //注解的生命周期存在运行时
@Inherited                      //注解可以被继承  我们在接口方法定义的注解，在实现类的方法上也可以获得
public @interface Delete {

    /**
     * String作为返回值实际上其作用是存储注解信息
     * 因此String value();也可以抽象的认为是属性     但写法上还是叫抽象方法
     * @return
     */
    String value();
}
