package com.suyu.test.dao;

import com.suyu.jdbc.annotations.Delete;
import com.suyu.jdbc.annotations.Insert;
import com.suyu.jdbc.annotations.Select;
import com.suyu.jdbc.annotations.Update;
import com.suyu.test.domain.Car;

import java.util.List;
import java.util.Map;

public interface AnnotationCarDao {

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
    @Insert("insert into jdbccar values(#{number},#{name},#{color},#{price})")
    Integer insertOne(Car car);

    @Delete("delete from jdbccar where number = #{number}")
    Integer deleteOne(int number);

    @Update("update jdbccar set name = #{name} where number = #{number}")
    Integer updateOne(Map<String, Object> param);

    @Select("select * from jdbccar")
    List<Car> selectAll();

    @Select("select * from jdbccar where number = #{number}")
    Car selectOne(int number);
}
