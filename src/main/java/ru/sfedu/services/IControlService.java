package ru.sfedu.services;

import ru.sfedu.model.MoveType;
import ru.sfedu.model.Result;
import ru.sfedu.model.Subject;

import java.util.AbstractMap;
import java.util.TreeMap;

public interface IControlService {
    Result<AbstractMap.SimpleEntry<Subject, TreeMap<String, String>>> objectRegistration(Subject subject);

    void gateAction(Integer subjectId, Integer barrierId, MoveType moveType);

    void barrierRegistration(Integer barrierFloor);

    void grantAccess(Integer subjectId, Integer barrierId, Integer year, Integer month, Integer day, Integer hours);
}
