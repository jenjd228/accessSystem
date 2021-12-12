package ru.sfedu.model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;


public class Animal extends Subject {

    public Animal() {
    }

    @CsvBindByPosition(position = 2)
    private String nickName;

    @CsvBindByPosition(position = 3)
    private String color;


    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animal)) return false;
        if (!super.equals(o)) return false;
        Animal animal = (Animal) o;
        return Objects.equals(nickName, animal.nickName) && Objects.equals(color, animal.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nickName, color);
    }

    @Override
    public String toString() {
        return "Animal{" +
                "nickName='" + nickName + '\'' +
                ", color='" + color + '\'' +
                ", id=" + id +
                ", type=" + type +
                '}';
    }
}
