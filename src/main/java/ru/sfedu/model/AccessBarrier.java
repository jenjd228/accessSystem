package ru.sfedu.model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;


public class AccessBarrier extends OnlyId {

    public AccessBarrier() {
    }

    @CsvBindByPosition(position = 1)
    private Integer subjectId;

    @CsvBindByPosition(position = 2)
    private Integer barrierId;

    @CsvBindByPosition(position = 3)
    private Long date;

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
        if (!super.equals(o)) return false;
        AccessBarrier that = (AccessBarrier) o;
        return Objects.equals(subjectId, that.subjectId) && Objects.equals(barrierId, that.barrierId) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subjectId, barrierId, date);
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
