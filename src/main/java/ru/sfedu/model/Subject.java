package ru.sfedu.model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;

public class Subject extends OnlyId {

    @CsvBindByPosition(position = 1)
    protected SubjectType type;

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
        if (!super.equals(o)) return false;
        Subject subject = (Subject) o;
        return type == subject.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id=" + id +
                ", type=" + type +
                '}';
    }
}
