package ru.sfedu.api;

import ru.sfedu.model.*;

public interface IDataProvider {
    Result<Subject> saveOrUpdateSubject(Subject subject);

    Result<Subject> getSubjectById(Integer id);

    boolean isSubjectHasAccess(Integer subjectId, Integer barrierId);

    void saveMotion(Integer subjectId, Integer barrierId, MoveType moveType);

    Result<History> createAndSaveHistory(Integer subjectId);

    boolean barrierRegistration(Integer barrierFloor);

    boolean grantAccess(Integer subjectId,Integer barrierId,Long date);

    void openOrCloseBarrier(Integer barrierId, boolean flag);
}
