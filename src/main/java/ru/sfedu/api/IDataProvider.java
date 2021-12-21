package ru.sfedu.api;

import ru.sfedu.model.MoveType;
import ru.sfedu.model.Result;
import ru.sfedu.model.Subject;

import java.util.List;

public interface IDataProvider {
    Result<Object> subjectRegistration(Subject subject);

    boolean barrierRegistration(Integer barrierFloor);

    Result<Object> grantAccess(Integer subjectId, Integer barrierId, Integer year, Integer month, Integer day, Integer hours);

    boolean gateAction(Integer subjectId, Integer barrierId, MoveType moveType);

    List<Subject> getAllUsers();

    Result<Subject> deleteSubjectById(Integer subjectId);
}
