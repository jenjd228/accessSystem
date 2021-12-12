package ru.sfedu.model;
import com.opencsv.bean.CsvBindByName;

import java.util.Objects;


public class AccessBarrier {

    public AccessBarrier(){}

    @CsvBindByName
    private Integer id;

    private Integer subjectId;

    @CsvBindByName
    private Integer barrierId;

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

    public Integer getBarrierId() {
        return barrierId;
    }

    public void setBarrierId(Integer barrierId) {
        this.barrierId = barrierId;
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
        if (!(o instanceof AccessBarrier)) return false;
        AccessBarrier that = (AccessBarrier) o;
        return Objects.equals(id, that.id) && Objects.equals(subjectId, that.subjectId) && Objects.equals(barrierId, that.barrierId) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subjectId, barrierId, date);
    }

    @Override
    public String toString() {
        return "AccessBarrier{" +
                "id=" + id +
                ", subjectId=" + subjectId +
                ", barrierId=" + barrierId +
                ", date=" + date +
                '}';
    }
}
