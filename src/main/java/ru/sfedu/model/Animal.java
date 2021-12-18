package ru.sfedu.model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;


public class Animal extends Subject {

    public Animal() {
    }

    @CsvBindByPosition(position = 2)
    private String name;

    @CsvBindByPosition(position = 3)
    private String color;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animal)) return false;
        if (!super.equals(o)) return false;
        Animal animal = (Animal) o;
        return Objects.equals(name, animal.name) && Objects.equals(color, animal.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, color);
    }

    @Override
    public String toString() {
        return "Animal{" +
                "nickName='" + name + '\'' +
                ", color='" + color + '\'' +
                ", id=" + id +
                ", type=" + type +
                '}';
    }
}
