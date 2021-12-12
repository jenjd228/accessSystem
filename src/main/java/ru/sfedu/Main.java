package ru.sfedu;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.model.*;
import ru.sfedu.services.ControlService;
import ru.sfedu.utils.Constants;
import ru.sfedu.utils.CsvUtil;
import ru.sfedu.utils.SubjectUtil;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Animal animal = new Animal();
        animal.setType(SubjectType.ANIMAL);
        animal.setColor("Red");
        animal.setNickName("Dog");

        Transport transport = new Transport();
        transport.setType(SubjectType.TRANSPORT);
        transport.setColor("Red");
        transport.setNumber("eqwe212");

        Human human = new Human();
        ArrayList<Integer> list = new ArrayList();
        list.add(1);
        list.add(2);
        list.add(3);
        human.setType(SubjectType.USER);
        human.setId(2);
        human.setEmail("ekocaba2@mail.ru");
        human.setLogin("jenjd2");
        human.setPassword("15032002K");
        human.setName("Maxx");
        human.setSurname("MaxSurname");
        human.setPatronymic("MaxPatronymic");

        ControlService controlService = new ControlService();
        //log.info(controlService.objectRegistration(transport));

        controlService.gateAction(1,2,MoveType.OUT);
    }
}
