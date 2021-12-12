package ru.sfedu.model;

import com.opencsv.bean.CsvBindByName;

import java.util.Objects;

public class Barrier {

    public Barrier() {
    }

    @CsvBindByName
    private Integer id;

    @CsvBindByName
    private Integer barrierFloor;

    @CsvBindByName
    public boolean isOpen;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBarrierFloor() {
        return barrierFloor;
    }

    public void setBarrierFloor(Integer barrierFloor) {
        this.barrierFloor = barrierFloor;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Barrier)) return false;
        Barrier barrier = (Barrier) o;
        return isOpen == barrier.isOpen && Objects.equals(id, barrier.id) && Objects.equals(barrierFloor, barrier.barrierFloor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, barrierFloor, isOpen);
    }

    @Override
    public String toString() {
        return "Barrier{" +
                "id=" + id +
                ", barrierFloor=" + barrierFloor +
                ", isOpen=" + isOpen +
                '}';
    }
}
