package ru.sfedu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.api.DataProviderCsv;
import ru.sfedu.api.DataProviderXml;
import ru.sfedu.model.Animal;
import ru.sfedu.model.Human;
import ru.sfedu.model.SubjectType;
import ru.sfedu.model.Transport;
import ru.sfedu.services.ControlService;

import java.util.ArrayList;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Animal animal = new Animal();
        animal.setType(SubjectType.ANIMAL);
        animal.setColor("Red");
        animal.setNickName("Dog");

        Transport transport = new Transport();
        transport.setId(2);
        transport.setType(SubjectType.TRANSPORT);
        transport.setColor("Black");
        transport.setNumber("number");

        Human human = new Human();
        ArrayList<Integer> list = new ArrayList();
        list.add(1);
        list.add(2);
        list.add(3);
        human.setType(SubjectType.USER);
        //human.setId(2);
        human.setEmail("ekocaba2@mail.ru");
        human.setLogin("jenjd2");
        human.setPassword("15032002K");
        human.setName("Maxx");
        human.setSurname("MaxSurname");
        human.setPatronymic("MaxPatronymic");

        ControlService controlService = new ControlService(new DataProviderXml());
        log.info(controlService.objectRegistration(transport));

        //controlService.gateAction(1,2,MoveType.OUT);
    }
}
