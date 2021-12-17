package ru.sfedu.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.Constants;
import ru.sfedu.model.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.sfedu.utils.TImeUtil.getCurrentUtcTimeInMillis;

public class SubjectUtil {

    private static final Logger log = LogManager.getLogger(SubjectUtil.class.getName());

    public static String[] getAllSubjectFields(Subject subject) {
        List<Field> mainClassFields = Arrays.stream(subject.getClass().getDeclaredFields().clone()).toList();
        List<Field> superClassFields = Arrays.stream(subject.getClass().getSuperclass().getDeclaredFields().clone()).toList();
        List<Field> idField = Arrays.stream(subject.getClass().getSuperclass().getSuperclass().getDeclaredFields().clone()).toList();
        List<Field> allFields = new ArrayList<>();
        allFields.addAll(idField);
        allFields.addAll(superClassFields);
        allFields.addAll(mainClassFields);

        String[] columns = new String[allFields.size()];
        for (int i = 0; i < allFields.size(); i++) {
            columns[i] = allFields.get(i).getName();
        }
        return columns;
    }

    public static <T> String[] getAllObjectFields(T object) {
        List<Field> mainClassFields = Arrays.stream(object.getClass().getDeclaredFields().clone()).toList();
        List<Field> idField = Arrays.stream(object.getClass().getSuperclass().getDeclaredFields().clone()).toList();
        List<Field> allFields = new ArrayList<>();
        allFields.addAll(idField);
        allFields.addAll(mainClassFields);

        String[] columns = new String[allFields.size()];
        for (int i = 0; i < allFields.size(); i++) {
            columns[i] = allFields.get(i).getName();
        }
        return columns;
    }

    public static Result<TreeMap<String, String>> objectValidation(Subject subject) {
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

    private static Result<TreeMap<String, String>> transportValidation(Transport transport) {
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

    private static Result<TreeMap<String, String>> humanValidation(Human human) {
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

    private static Result<TreeMap<String, String>> animalValidation(Animal animal) {
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

    private static boolean checkByPattern(String string, String pattern) {
        if (string == null) {
            return false;
        }
        Pattern pattern2 = Pattern.compile(pattern);
        Matcher matcher = pattern2.matcher(string);
        return matcher.matches();
    }

    public static History createHistory(Integer subjectId, Integer historyId) {
        History history = new History();
        history.setDate(getCurrentUtcTimeInMillis());
        history.setSubjectId(subjectId);
        history.setId(historyId);
        return history;
    }

    public static Motion createMotion(Integer barrierId, MoveType moveType) {
        Motion motion = new Motion();
        motion.setBarrierId(barrierId);
        motion.seteMoveType(moveType);
        return motion;
    }

    public static Subject createTransport(Integer id, String number, String color) {
        Transport transport = new Transport();
        setCommonFields(transport, id, SubjectType.TRANSPORT);
        transport.setNumber(number);
        transport.setColor(color);
        return transport;
    }

    public static Subject createAnimal(Integer id, String nickName, String color) {
        Animal animal = new Animal();
        setCommonFields(animal, id, SubjectType.ANIMAL);
        animal.setNickName(nickName);
        animal.setColor(color);
        return animal;
    }

    public static void setCommonFields(Subject subject, Integer id, SubjectType subjectType) {
        subject.setId(id);
        subject.setType(subjectType);
    }

    public static Subject createHuman(Integer id, SubjectType subjectType, String password, String login, String name, String surname, String patronymic, String email) {
        Human human = new Human();
        setCommonFields(human, id, subjectType);
        human.setPassword(password);
        human.setLogin(login);
        human.setName(name);
        human.setSurname(surname);
        human.setPatronymic(patronymic);
        human.setEmail(email);
        return human;
    }

    public static Barrier createBarrier(Integer id, Integer barrierFloor, boolean isOpen) {
        Barrier barrier = new Barrier();
        barrier.setId(id);
        barrier.setBarrierFloor(barrierFloor);
        barrier.setOpen(isOpen);
        return barrier;
    }

    public static AccessBarrier createAccessBarrier(Integer id, Integer subjectId, Integer barrierId, Long date) {
        AccessBarrier accessBarrier = new AccessBarrier();
        accessBarrier.setId(id);
        accessBarrier.setSubjectId(subjectId);
        accessBarrier.setBarrierId(barrierId);
        accessBarrier.setDate(date);
        return accessBarrier;
    }

}
