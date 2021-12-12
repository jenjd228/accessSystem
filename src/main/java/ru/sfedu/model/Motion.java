package ru.sfedu.model;

import com.opencsv.bean.CsvBindByName;

import java.util.Objects;

public class Motion {

    public Motion(){}

    @CsvBindByName
    private Integer id;

    @CsvBindByName
    private Integer barrierId;

    private Integer historyId;

    @CsvBindByName
    private MoveType eMoveType;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBarrierId() {
        return barrierId;
    }

    public void setBarrierId(Integer barrierId) {
        this.barrierId = barrierId;
    }

    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }

    public MoveType geteMoveType() {
        return eMoveType;
    }

    public void seteMoveType(MoveType eMoveType) {
        this.eMoveType = eMoveType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Motion)) return false;
        Motion motion = (Motion) o;
        return Objects.equals(id, motion.id) && Objects.equals(barrierId, motion.barrierId) && Objects.equals(historyId, motion.historyId) && eMoveType == motion.eMoveType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, barrierId, historyId, eMoveType);
    }

    @Override
    public String toString() {
        return "Motion{" +
                "id=" + id +
                ", barrierId=" + barrierId +
                ", historyId=" + historyId +
                ", eMoveType=" + eMoveType +
                '}';
    }
}
