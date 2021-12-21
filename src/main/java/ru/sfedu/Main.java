package ru.sfedu;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import ru.sfedu.api.DataProviderCsv;
import ru.sfedu.api.DataProviderH2;
import ru.sfedu.api.DataProviderXml;
import ru.sfedu.model.Animal;
import ru.sfedu.model.SubjectType;
import ru.sfedu.utils.ConfigurationUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Arrays;

import static ru.sfedu.utils.ConfigurationUtil.getConfigPath;
import static ru.sfedu.utils.ConfigurationUtil.getConfigurationEntry;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {
        /*CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(getAllOptions(), args);
            if (cmd.hasOption("env")){
                String[] arguments = cmd.getOptionValues("env");
                ConfigurationUtil.setConfigPath(arguments[0]);
            }
            if (cmd.hasOption("log")){
                String[] arguments = cmd.getOptionValues("log");
                File file = new File(arguments[0]);
                LoggerContext context = (LoggerContext) LogManager.getContext(false);
                context.setConfigLocation(file.toURI());
                log.info("WADAWDAWDAD");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        Animal animal = new Animal();
        animal.setColor("Red");
        animal.setName("animal");
        animal.setType(SubjectType.ANIMAL);
        DataProviderH2 dataProviderH2 = new DataProviderH2();
        dataProviderH2.deleteAccessBarrierBySubjectAndBarrierId(1,1);
        printAccessBarrierData();
    }

    private static Options getAllOptions() {
        Options options = new Options();

        Option option = new Option("env", true, "Путь до файла environment.properties");
        option.setArgs(1);
        option.setOptionalArg(true);

        Option option1 = new Option("log", true, "Путь до файла log4j2.xml");
        option.setArgs(1);
        option.setOptionalArg(true);

        options.addOption(option);
        options.addOption(option1);
        return options;
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

    private static void printBarrierData() {
        try {
            Class.forName(getConfigurationEntry(Constants.H2_DRIVER)).getDeclaredConstructor().newInstance();
            Connection connection = DriverManager.getConnection(
                    getConfigurationEntry(Constants.H2_CONNECTOR).concat("./").concat(Constants.H2_PATH_FOLDER).concat(Constants.H2_DB_NAME),
                    getConfigurationEntry(Constants.H2_LOGIN),
                    getConfigurationEntry(Constants.H2_PASSWORD));

            ResultSet resultSet = connection.createStatement().executeQuery("select * from ".concat(Constants.SQL_TABLE_NAME_BARRIER));
            while (resultSet.next()) {
                System.out.println(resultSet.getString("id").concat(" ").concat(resultSet.getString("isOpen")));
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printMotionData() {
        try {
            Class.forName(getConfigurationEntry(Constants.H2_DRIVER)).getDeclaredConstructor().newInstance();
            Connection connection = DriverManager.getConnection(
                    getConfigurationEntry(Constants.H2_CONNECTOR).concat("./").concat(Constants.H2_PATH_FOLDER).concat(Constants.H2_DB_NAME),
                    getConfigurationEntry(Constants.H2_LOGIN),
                    getConfigurationEntry(Constants.H2_PASSWORD));

            ResultSet resultSet = connection.createStatement().executeQuery("select * from ".concat(Constants.SQL_TABLE_NAME_MOTION));
            while (resultSet.next()) {
                System.out.println(resultSet.getString("id").concat(" ").concat(resultSet.getString("barrierId")).concat(resultSet.getString("historyId")).concat(resultSet.getString("moveType")));
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printAccessBarrierData() {
        try {
            Class.forName(getConfigurationEntry(Constants.H2_DRIVER)).getDeclaredConstructor().newInstance();
            Connection connection = DriverManager.getConnection(
                    getConfigurationEntry(Constants.H2_CONNECTOR).concat("./").concat(Constants.H2_PATH_FOLDER).concat(Constants.H2_DB_NAME),
                    getConfigurationEntry(Constants.H2_LOGIN),
                    getConfigurationEntry(Constants.H2_PASSWORD));

            ResultSet resultSet = connection.createStatement().executeQuery("select * from ".concat(Constants.SQL_TABLE_NAME_ACCESS_BARRIER));
            while (resultSet.next()) {
                System.out.println(resultSet.getString("id").concat(" ").concat(resultSet.getString("subjectId").concat(resultSet.getString("barrierId"))));
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
