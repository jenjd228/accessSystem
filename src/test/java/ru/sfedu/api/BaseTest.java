package ru.sfedu.api;

import ru.sfedu.model.Animal;
import ru.sfedu.model.SubjectType;

public class BaseTest {

    protected Animal createAnimal(Integer id, String color, String nickName) {
        Animal animal = new Animal();
        animal.setType(SubjectType.ANIMAL);
        animal.setColor(color);
        animal.setName(nickName);
        animal.setId(id);
        return animal;
    }
}
