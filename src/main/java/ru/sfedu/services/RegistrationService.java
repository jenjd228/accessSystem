package ru.sfedu.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.api.IDataProvider;
import ru.sfedu.model.*;
import ru.sfedu.utils.Constants;

import java.util.AbstractMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationService implements IRegistrationService {

    private final Logger log = LogManager.getLogger(RegistrationService.class.getName());

    private final IDataProvider dataProvider;

    public RegistrationService(IDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public Result<TreeMap<String, String>> objectValidation(Subject subject) {
        log.info("objectValidation [1]");
        SubjectType eSubjectType = subject.getType();
        Result<TreeMap<String, String>> result = new Result<>(null, Constants.CODE_ERROR, null);
        switch (eSubjectType) {
            case ADMIN, USER -> result = humanValidation((Human) subject);
            case ANIMAL -> result = animalValidation((Animal) subject);
            case TRANSPORT -> result = transportValidation((Transport) subject);
            case UNDEFINED -> log.info("objectValidation [2]: UNDEFINED subject");
            default -> log.error("objectValidation [3]: error there is no such SubjectType");
        }
        return result;
    }

    @Override
    public Result<AbstractMap.SimpleEntry<Subject, TreeMap<String, String>>> objectRegistration(Subject subject) {
        log.info("objectRegistration [1]: object {}", subject);

        Result<TreeMap<String, String>> validationResult = objectValidation(subject);
        Result<Subject> saveResult = new Result<>(null, Constants.CODE_ERROR, subject);

        if (validationResult.getCode() == Constants.CODE_ACCESS) {
            if (validationResult.getResult().isEmpty()) {
                saveResult = dataProvider.saveOrUpdateSubject(subject);
            }
        }

        return new Result<>(saveResult.getMessage(), saveResult.getCode(), new AbstractMap.SimpleEntry<>(saveResult.getResult(), validationResult.getResult()));
    }

    @Override
    public void barrierRegistration(Integer barrierFloor) {
        dataProvider.barrierRegistration(barrierFloor);
    }

    private Result<TreeMap<String, String>> transportValidation(Transport transport) {
        log.info("transportValidation [1]: object: {}", transport);
        TreeMap<String, String> errors = new TreeMap<>();
        Result<TreeMap<String, String>> result = new Result<>(null, Constants.CODE_ACCESS, errors);

        if (!checkByPattern(transport.getColor(), Constants.REGEX_SHORT_STRING)) {
            errors.put(Constants.KEY_COLOR, Constants.NOT_VALID_COLOR);
        }

        if (!checkByPattern(transport.getNumber(), Constants.REGEX_TRANSPORT_NUMBER)) {
            errors.put(Constants.KEY_NUMBER, Constants.NOT_VALID_NUMBER);
        }

        if (!errors.isEmpty()) {
            result.setCode(Constants.CODE_INVALID_DATA);
        }

        log.info("transportValidation [2]: isValid: {}", errors.size() == 0);
        return result;
    }

    private Result<TreeMap<String, String>> humanValidation(Human human) {
        log.info("humanValidation [1]: object: {}", human);
        TreeMap<String, String> errors = new TreeMap<>();
        Result<TreeMap<String, String>> result = new Result<>(null, Constants.CODE_ACCESS, errors);

        if (!checkByPattern(human.getName(), Constants.REGEX_FIO_STRING)) {
            errors.put(Constants.KEY_NAME, Constants.NOT_VALID_NAME);
        }

        if (!checkByPattern(human.getSurname(), Constants.REGEX_FIO_STRING)) {
            errors.put(Constants.KEY_SURNAME, Constants.NOT_VALID_SURNAME);
        }

        if (!checkByPattern(human.getPatronymic(), Constants.REGEX_PATRONYMIC)) {
            errors.put(Constants.KEY_PATRONYMIC, Constants.NOT_VALID_PATRONYMIC);
        }

        if (!checkByPattern(human.getLogin(), Constants.REGEX_LOGIN)) {
            errors.put(Constants.KEY_LOGIN, Constants.NOT_VALID_LOGIN);
        }

        if (!checkByPattern(human.getPassword(), Constants.REGEX_PASSWORD)) {
            errors.put(Constants.KEY_PASSWORD, Constants.NOT_VALID_PASSWORD);
        }

        if (!checkByPattern(human.getEmail(), Constants.REGEX_EMAIL)) {
            errors.put(Constants.KEY_EMAIL, Constants.NOT_VALID_EMAIL);
        }

        if (!errors.isEmpty()) {
            result.setCode(Constants.CODE_INVALID_DATA);
        }

        log.info("humanValidation [2]: isValid: {}", errors.size() == 0);
        return result;
    }

    private Result<TreeMap<String, String>> animalValidation(Animal animal) {
        log.info("animalValidation [1]: object: {}", animal);
        TreeMap<String, String> errors = new TreeMap<>();
        Result<TreeMap<String, String>> result = new Result<>(null, Constants.CODE_ACCESS, errors);

        if (!checkByPattern(animal.getColor(), Constants.REGEX_SHORT_STRING)) {
            errors.put(Constants.KEY_COLOR, Constants.NOT_VALID_COLOR);
        }

        if (!checkByPattern(animal.getNickName(), Constants.REGEX_SHORT_STRING)) {
            errors.put(Constants.KEY_NICKNAME, Constants.NOT_VALID_NICKNAME);
        }

        if (!errors.isEmpty()) {
            result.setCode(Constants.CODE_INVALID_DATA);
        }

        log.info("animalValidation [2]: isValid: {}", errors.size() == 0);
        return result;
    }

    private boolean checkByPattern(String string, String pattern) {
        if (string == null) {
            return false;
        }
        Pattern pattern2 = Pattern.compile(pattern);
        Matcher matcher = pattern2.matcher(string);
        return matcher.matches();
    }
}
