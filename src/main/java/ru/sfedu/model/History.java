package ru.sfedu.model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;


public class History extends OnlyId {

    public History() {
    }

    @CsvBindByPosition(position = 1)
    private Integer subjectId;

    @CsvBindByPosition(position = 2)
    private Long date;

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof History)) return false;
        if (!super.equals(o)) return false;
        History history = (History) o;
        return Objects.equals(subjectId, history.subjectId) && Objects.equals(date, history.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subjectId, date);
    }

    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", subjectId=" + subjectId +
                ", date=" + date +
                '}';
    }
}
