package ru.sfedu.model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;

public class Transport extends Subject {

    public Transport() {
    }

    @CsvBindByPosition(position = 2)
    private String number;

    @CsvBindByPosition(position = 3)
    private String color;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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
        if (!(o instanceof Transport)) return false;
        if (!super.equals(o)) return false;
        Transport transport = (Transport) o;
        return Objects.equals(number, transport.number) && Objects.equals(color, transport.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), number, color);
    }

    @Override
    public String toString() {
        return "Transport{" +
                "id=" + id +
                ", type=" + type +
                ", number='" + number + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
