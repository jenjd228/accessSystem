package ru.sfedu.api;

import ru.sfedu.model.Animal;
import ru.sfedu.model.SubjectType;

import java.util.Calendar;

public class BaseTest {

    protected Animal createAnimal(Integer id, String color, String nickName) {
        Animal animal = new Animal();
        animal.setType(SubjectType.ANIMAL);
        animal.setColor(color);
        animal.setName(nickName);
        animal.setId(id);
        return animal;
    }

    protected Long getUtcTimeInMillis(Integer year, Integer month, Integer day, Integer hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return calendar.getTimeInMillis();
    }

}
