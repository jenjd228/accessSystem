package ru.sfedu.model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;

public class OnlyId {

    @CsvBindByPosition(position = 0)
    protected Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OnlyId)) return false;
        OnlyId onlyId = (OnlyId) o;
        return Objects.equals(id, onlyId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OnlyId{" +
                "id=" + id +
                '}';
    }
}
