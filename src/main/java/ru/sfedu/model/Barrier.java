package ru.sfedu.model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;

public class Barrier extends OnlyId {

    public Barrier() {
    }

    @CsvBindByPosition(position = 1)
    private Integer barrierFloor;

    @CsvBindByPosition(position = 2)
    private boolean isOpen;

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
        if (!super.equals(o)) return false;
        Barrier barrier = (Barrier) o;
        return isOpen == barrier.isOpen && Objects.equals(barrierFloor, barrier.barrierFloor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), barrierFloor, isOpen);
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
