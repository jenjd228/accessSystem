package ru.sfedu.model;

import com.opencsv.bean.CsvBindByName;

import java.util.Objects;


public class History {

    public History() {
    }

    @CsvBindByName
    private Integer id;

    @CsvBindByName
    private Integer subjectId;

    @CsvBindByName
    private Long date;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
        History history = (History) o;
        return Objects.equals(id, history.id) && Objects.equals(subjectId, history.subjectId) && Objects.equals(date, history.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subjectId, date);
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
