package ru.sfedu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.api.DataProviderXml;
import ru.sfedu.model.*;

import java.util.ArrayList;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Animal animal = new Animal();
        animal.setType(SubjectType.ANIMAL);
        animal.setColor("Red");
        animal.setNickName("Dog");

        Transport transport = new Transport();
        transport.setType(SubjectType.TRANSPORT);
        transport.setColor("Black");
        transport.setNumber("number");

        Human human = new Human();
        ArrayList<Integer> list = new ArrayList();
        list.add(1);
        list.add(2);
        list.add(3);
        human.setType(SubjectType.USER);
        human.setEmail("ekocaba2@mail.ru");
        human.setLogin("jenjd22");
        human.setPassword("15032002K");
        human.setName("Maxxx");
        human.setSurname("MaxSurname");
        human.setPatronymic("MaxPatronymic");

        /*DataProviderXml dataProviderXml = new DataProviderXml();
        dataProviderXml.barrierRegistration(3);*/

        DataProviderXml dataProviderXml = new DataProviderXml();
        dataProviderXml.gateAction(1,1,MoveType.OUT);
    }
}
