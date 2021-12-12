package ru.sfedu.services;

import ru.sfedu.model.MoveType;

public interface IAccessService {
    boolean checkPermission(Integer subjectId, Integer barrierId);

    void openOrCloseBarrier(Integer barrierId, boolean flag);

    void motionRegistration(Integer subjectId, Integer barrierId, MoveType moveType);

    void grantAccess(Integer subjectId, Integer barrierId, Integer year, Integer month, Integer day, Integer hours);
}