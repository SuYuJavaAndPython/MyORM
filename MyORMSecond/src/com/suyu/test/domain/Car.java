package com.suyu.test.domain;

public class Car {

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

    public String toString(){
        StringBuilder builder = new StringBuilder("{ ");
        builder.append(number);
        builder.append(" : ");
        builder.append(name);
        builder.append(" : ");
        builder.append(color);
        builder.append(" : ");
        builder.append(price);
        builder.append(" }");
        return builder.toString();
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
}
