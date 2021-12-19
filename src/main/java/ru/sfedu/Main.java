package ru.sfedu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.api.DataProviderH2;
import ru.sfedu.model.Animal;
import ru.sfedu.model.Human;
import ru.sfedu.model.SubjectType;
import ru.sfedu.model.Transport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;

import static ru.sfedu.utils.ConfigurationUtil.getConfigurationEntry;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Animal animal = new Animal();
        animal.setType(SubjectType.ANIMAL);
        animal.setColor("Redd");
        animal.setName("DogDog");

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
        dataProviderXml.gateAction(1,1,MoveType.OUT);*/

        printSubjectData();

        DataProviderH2 dataProviderH2 = new DataProviderH2();
        dataProviderH2.subjectRegistration(animal);

        printSubjectData();
    }

    private static void printSubjectData() {
        try {
            Class.forName(getConfigurationEntry(Constants.H2_DRIVER)).getDeclaredConstructor().newInstance();
            Connection connection = DriverManager.getConnection(
                    getConfigurationEntry(Constants.H2_CONNECTOR).concat("./").concat(Constants.H2_PATH_FOLDER).concat(Constants.H2_DB_NAME),
                    getConfigurationEntry(Constants.H2_LOGIN),
                    getConfigurationEntry(Constants.H2_PASSWORD));

            ResultSet resultSet = connection.createStatement().executeQuery("select * from subject");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("name"));
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
