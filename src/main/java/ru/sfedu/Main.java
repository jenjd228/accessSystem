package ru.sfedu;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import ru.sfedu.api.DataProviderCsv;
import ru.sfedu.api.DataProviderH2;
import ru.sfedu.api.DataProviderXml;
import ru.sfedu.api.IDataProvider;
import ru.sfedu.model.*;
import ru.sfedu.utils.ConfigurationUtil;
import ru.sfedu.utils.SubjectUtil;
import ru.sfedu.utils.TImeUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.sfedu.utils.ConfigurationUtil.getConfigurationEntry;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(getAllOptions(), args);
            if (cmd.hasOption(Constants.CLI_ENVIRONMENT_PROPERTIES)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_ENVIRONMENT_PROPERTIES);
                log.info(arguments[0]);
                ConfigurationUtil.setConfigPath(arguments[0]);
            }
            if (cmd.hasOption(Constants.CLI_LOG)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_LOG);
                File file = new File(arguments[0]);
                LoggerContext context = (LoggerContext) LogManager.getContext(false);
                context.setConfigLocation(file.toURI());
            }
            IDataProvider dataProvider = new DataProviderH2();
            if (cmd.hasOption(Constants.CLI_DATA_TYPE)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_DATA_TYPE);
                switch (arguments[0]) {
                    case "XML" -> dataProvider = new DataProviderXml();
                    case "CSV" -> dataProvider = new DataProviderCsv();
                    case "H2" -> dataProvider = new DataProviderH2();
                    default -> log.info("Такого типа данных нет.");
                }
                log.info("Установлен {} дата провайдер.", dataProvider.getClass().getName());
            }
            if (cmd.hasOption(Constants.CLI_NEW_ANIMAL)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_NEW_ANIMAL);
                log.info("Регистрация животного: ".concat(Arrays.toString(arguments)));
                Result<Object> result = dataProvider.subjectRegistration(SubjectUtil.createAnimal(null, arguments[0], arguments[1]));
                analyzeRegistrationResult(result);
            }
            if (cmd.hasOption(Constants.CLI_NEW_TRANSPORT)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_NEW_TRANSPORT);
                log.info("Регистрация транспорта: ".concat(Arrays.toString(arguments)));
                Result<Object> result = dataProvider.subjectRegistration(SubjectUtil.createTransport(null, arguments[0], arguments[1]));
                analyzeRegistrationResult(result);
            }
            if (cmd.hasOption(Constants.CLI_NEW_HUMAN)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_NEW_HUMAN);
                log.info("Регистрация человека: ".concat(Arrays.toString(arguments)));
                SubjectType subjectType;
                switch (arguments[0]) {
                    case "ADMIN" -> subjectType = SubjectType.ADMIN;
                    case "USER" -> subjectType = SubjectType.USER;
                    default -> subjectType = SubjectType.UNDEFINED;
                }
                Result<Object> result = dataProvider.subjectRegistration(SubjectUtil.createHuman(null, subjectType, arguments[5], arguments[4], arguments[1], arguments[2], arguments[3], arguments[6]));
                analyzeRegistrationResult(result);
            }
            if (cmd.hasOption(Constants.CLI_PRINT_SUBJECTS)) {
                List<Subject> subjects = dataProvider.getAllUsers();
                printData(subjects);
            }
            if (cmd.hasOption(Constants.CLI_NEW_BARRIER)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_NEW_BARRIER);
                log.info("Регистрация барьера: ".concat(Arrays.toString(arguments)));
                boolean isSuccessfully = dataProvider.barrierRegistration(Integer.parseInt(arguments[0]));
                if (isSuccessfully) {
                    log.info("Создание барьера прошло успешно");
                } else {
                    log.info("Произошла ошибка при создании барьера");
                }
            }
            if (cmd.hasOption(Constants.CLI_PRINT_BARRIERS)) {
                List<Barrier> barriers = dataProvider.getAllBarriers();
                printData(barriers);
            }
            if (cmd.hasOption(Constants.CLI_GRANT_ACCESS)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_GRANT_ACCESS);
                Result<Object> result = dataProvider.grantAccess(Integer.parseInt(arguments[0]), Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), Integer.parseInt(arguments[3]), Integer.parseInt(arguments[4]), Integer.parseInt(arguments[5]));
                analyzeGrantAccessResult(result);
            }
            if (cmd.hasOption(Constants.CLI_PRINT_SUBJECT_ACCESS)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_PRINT_SUBJECT_ACCESS);
                List<AccessBarrier> list = dataProvider.getAccessBarriersBySubjectId(Integer.parseInt(arguments[0]));
                printAccessBarriers(list);
            }
        } catch (ParseException e) {
            log.error("Произошла ошибка = {}", e.getMessage());
        }
    }

    private static void analyzeGrantAccessResult(Result<Object> result) {
        if (result.getCode() == Constants.CODE_ACCESS) {
            log.info("Права на вход успешно выданы");
        } else if (result.getCode() == Constants.CODE_INVALID_DATA) {
            TreeMap<String, String> errors = (TreeMap<String, String>) result.getResult();
            for (String key : errors.keySet()) {
                log.info("Ошибка - {}", errors.get(key));
            }
        } else {
            log.error("Произошла ошибка. Статус ошибки = {}, сообщение = {}", result.getCode(), result.getMessage());
        }
    }

    private static void analyzeRegistrationResult(Result<Object> result) {
        if (result.getCode() == Constants.CODE_INVALID_DATA) {
            AbstractMap.SimpleEntry<Subject, TreeMap<String, String>> errors = (AbstractMap.SimpleEntry<Subject, TreeMap<String, String>>) result.getResult();
            for (String key : errors.getValue().keySet()) {
                log.info("Ошибка - {}", errors.getValue().get(key));
            }
        } else if (result.getCode() == Constants.CODE_ACCESS) {
            log.info("Регистрация прошла успешно");
        } else {
            log.error("Произошла ошибка. Статус ошибки = {}, сообщение = {}", result.getCode(), result.getMessage());
        }
    }

    private static Options getAllOptions() {
        Options options = new Options();

        Option optionEnv = new Option(Constants.CLI_ENVIRONMENT_PROPERTIES, true, "Путь до файла environment.properties");
        optionEnv.setArgs(1);
        optionEnv.setOptionalArg(true);

        Option optionLog = new Option(Constants.CLI_LOG, true, "Путь до файла log4j2.xml");
        optionLog.setArgs(1);
        optionLog.setOptionalArg(true);

        Option optionAnimalRegistration = new Option(Constants.CLI_NEW_ANIMAL, true, "Создание нового пользователя (животное)");
        optionAnimalRegistration.setArgs(2);
        optionAnimalRegistration.setOptionalArg(true);

        Option optionTransportRegistration = new Option(Constants.CLI_NEW_TRANSPORT, true, "Создание нового пользователя (транспорт)");
        optionTransportRegistration.setArgs(2);
        optionTransportRegistration.setOptionalArg(true);

        Option optionHumanRegistration = new Option(Constants.CLI_NEW_HUMAN, true, "Создание нового пользователя (человек)");
        optionHumanRegistration.setArgs(7);
        optionHumanRegistration.setOptionalArg(true);

        Option optionPrintSubjects = new Option(Constants.CLI_PRINT_SUBJECTS, false, "Создание нового пользователя (транспорт)");
        optionPrintSubjects.setOptionalArg(true);

        Option optionBarrierRegistration = new Option(Constants.CLI_NEW_BARRIER, true, "Создание нового пользователя (транспорт)");
        optionBarrierRegistration.setArgs(1);
        optionBarrierRegistration.setOptionalArg(true);

        Option optionPrintBarrier = new Option(Constants.CLI_PRINT_BARRIERS, false, "Создание нового пользователя (транспорт)");
        optionPrintBarrier.setOptionalArg(true);

        Option optionGrantAccess = new Option(Constants.CLI_GRANT_ACCESS, true, "Создание нового пользователя (человек)");
        optionGrantAccess.setArgs(6);
        optionGrantAccess.setOptionalArg(true);

        Option optionPrintSubjectAccess = new Option(Constants.CLI_PRINT_SUBJECT_ACCESS, true, "Создание нового пользователя (человек)");
        optionPrintSubjectAccess.setArgs(1);
        optionPrintSubjectAccess.setOptionalArg(true);


        options.addOption(optionEnv);
        options.addOption(optionLog);
        options.addOption(optionAnimalRegistration);
        options.addOption(optionTransportRegistration);
        options.addOption(optionHumanRegistration);
        options.addOption(optionPrintSubjects);
        options.addOption(optionBarrierRegistration);
        options.addOption(optionPrintBarrier);
        options.addOption(optionGrantAccess);
        options.addOption(optionPrintSubjectAccess);
        return options;
    }

    private static <T> void printData(List<T> list) {
        list.forEach(log::info);
    }

    private static void printAccessBarriers(List<AccessBarrier> accessBarriers) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        accessBarriers.forEach(it -> {
            calendar.setTimeInMillis(it.getDate());
            System.out.println(calendar.getTime());
            log.info("id = {}, subjectId = {}, barrierId = {}, date = {}", it.getId(), it.getSubjectId(), it.getBarrierId(), dateFormat.format(calendar.getTime()));
        });
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
