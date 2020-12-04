package com.domain;

import java.io.Serializable;

/**
 * 凡是用来装数据的类似于杜曼实体类 养成习惯都实现序列化接口
 * 就算现在用不上，以后也可能会用上(自己或者别人)
 */
public class Car implements Serializable {
    private Integer number;
    private String name;
    private String color;
    private Integer price;

    public Car() {}

    public Car(Integer number, String name, String color, Integer price) {
        this.number = number;
        this.name = name;
        this.color = color;
        this.price = price;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Car{" +
                "number=" + number +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", price=" + price +
                '}';
    }
}
