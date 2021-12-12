package ru.sfedu.services;

import ru.sfedu.model.Result;
import ru.sfedu.model.Subject;

import java.util.AbstractMap;
import java.util.TreeMap;

public interface IRegistrationService {
    Result<TreeMap<String, String>> objectValidation(Subject subject);

    Result<AbstractMap.SimpleEntry<Subject,TreeMap<String, String>>> objectRegistration(Subject subject);

    void barrierRegistration(Integer barrierFloor);
}
