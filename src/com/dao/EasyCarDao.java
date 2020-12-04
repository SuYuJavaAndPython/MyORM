package com.dao;

import com.domain.Car;
import com.jdbc.annotations.Delete;
import com.jdbc.annotations.Insert;
import com.jdbc.annotations.Select;

import java.util.List;

public interface EasyCarDao {

    /**
     * 注意这里的方法尽量用引用类型，eg：基本类型的包装类
     * 因为通过反编译我们发现如果invoke将结果返回
     * （注意invoke方法本身返回值类型就是Object，因此不包含基本数据类型）
     * eg：这个save方法如果写int    假设返回null
     * 同时编译器识别到不是引用类型就会做一个类型强转((Integer)null).intValue()
     * 此时就会出现空指针异常
     * @param car
     * @return
     */
    @Insert("insert into jdbcCar values(null,#{name},#{color},#{price})")
    Integer save(Car car);

    @Delete("delete from jdbcCar where number = #{number}")
    Integer delete(int number);

    @Select("select * from jdbcCar")
    List<Car> findAll();

    @Select("select * from jdbcCar where number = #{number}")
    Car findById(int number);
}
