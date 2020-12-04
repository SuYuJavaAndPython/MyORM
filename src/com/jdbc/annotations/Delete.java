package com.jdbc.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)             //注解可以用在方法上
@Retention(RetentionPolicy.RUNTIME)     //注解的生命周期可以在jvm中存在  通过反射获得注解信息
@Inherited      //注解可继承 我们在接口方法定义的注解，在实现类的方法上也可以获得
public @interface Delete {

    String value();
}
