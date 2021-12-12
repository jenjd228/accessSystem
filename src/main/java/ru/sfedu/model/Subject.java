package ru.sfedu.model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;

public class Subject {

    @CsvBindByPosition(position = 0)
    protected Integer id;

    @CsvBindByPosition(position = 1)
    protected SubjectType type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SubjectType getType() {
        return type;
    }

    public void setType(SubjectType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subject)) return false;
        Subject subject = (Subject) o;
        return Objects.equals(id, subject.id) && type == subject.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id=" + id +
                ", type=" + type +
                '}';
    }
}
