package ru.sfedu.model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;

public class Motion extends OnlyId {

    public Motion() {
    }

    @CsvBindByPosition(position = 1)
    private Integer barrierId;

    @CsvBindByPosition(position = 2)
    private Integer historyId;

    @CsvBindByPosition(position = 3)
    private MoveType moveType;

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

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Motion)) return false;
        if (!super.equals(o)) return false;
        Motion motion = (Motion) o;
        return Objects.equals(barrierId, motion.barrierId) && Objects.equals(historyId, motion.historyId) && moveType == motion.moveType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), barrierId, historyId, moveType);
    }

    @Override
    public String toString() {
        return "Motion{" +
                "id=" + id +
                ", barrierId=" + barrierId +
                ", historyId=" + historyId +
                ", eMoveType=" + moveType +
                '}';
    }
}
