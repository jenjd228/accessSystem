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

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(getAllOptions(), args);
            if (cmd.hasOption(Constants.CLI_ENVIRONMENT_PROPERTIES)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_ENVIRONMENT_PROPERTIES);
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
                List<Subject> subjects = dataProvider.getAllSubjects();
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
                boolean isValid = grantAccessValidation(arguments);
                if (isValid) {
                    Result<Object> result = dataProvider.grantAccess(Integer.parseInt(arguments[0]), Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), Integer.parseInt(arguments[3]), Integer.parseInt(arguments[4]), Integer.parseInt(arguments[5]));
                    analyzeGrantAccessResult(result);
                }
            }
            if (cmd.hasOption(Constants.CLI_PRINT_SUBJECT_ACCESS)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_PRINT_SUBJECT_ACCESS);
                List<AccessBarrier> list = dataProvider.getAccessBarriersBySubjectId(Integer.parseInt(arguments[0]));
                printAccessBarriers(list);
            }
            if (cmd.hasOption(Constants.CLI_DELETE_SUBJECT)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_DELETE_SUBJECT);
                Result<Subject> result = dataProvider.deleteSubjectById(Integer.parseInt(arguments[0]));
                analyzeDeletedSubject(result);
            }
            if (cmd.hasOption(Constants.CLI_DELETE_SUBJECT_ACCESS)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_DELETE_SUBJECT_ACCESS);
                Result<AccessBarrier> accessBarrierResult = dataProvider.deleteAccessBarrierBySubjectAndBarrierId(Integer.parseInt(arguments[0]), Integer.parseInt(arguments[1]));
                analyzeDeletedSubjectAccess(accessBarrierResult);
            }
            if (cmd.hasOption(Constants.CLI_PRINT_HISTORY)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_PRINT_HISTORY);
                Result<TreeMap<History, List<Motion>>> result = dataProvider.getSubjectHistoryBySubjectId(Integer.parseInt(arguments[0]));
                printSubjectHistory(result);
            }
            if (cmd.hasOption(Constants.CLI_GATE_ACTION)) {
                String[] arguments = cmd.getOptionValues(Constants.CLI_GATE_ACTION);
                boolean isSuccessfully = dataProvider.gateAction(Integer.parseInt(arguments[0]), Integer.parseInt(arguments[1]), MoveType.valueOf(arguments[2]));
                if (isSuccessfully) {
                    log.info("Объект успешно прошел барьер");
                } else {
                    log.info("Отказано в доступе");
                }
            }
            if (cmd.hasOption(Constants.CLI_HELP)) {
                printHelp(
                        getAllOptions(), // опции по которым составляем help
                        120, // ширина строки вывода
                        "Options", // строка предшествующая выводу
                        "-- HELP --", // строка следующая за выводом
                        3, // число пробелов перед выводом опции
                        5, // число пробелов перед выводом опцисания опции
                        true, // выводить ли в строке usage список команд
                        System.out // куда производить вывод
                );
            }
        } catch (ParseException e) {
            log.error("Произошла ошибка = {}", e.getMessage());
        }
    }

    private static boolean grantAccessValidation(String[] arguments) {
        try {
            int year = Integer.parseInt(arguments[2]);
            int month = Integer.parseInt(arguments[3]);
            int day = Integer.parseInt(arguments[4]);
            int hours = Integer.parseInt(arguments[5]);

            if (year < 2020 || month < 1 || month > 12 || day < 1 || day > 31 || hours < 0 || hours > 24) {
                log.info("Введенные данные не корректны.");
                return false;
            }
        } catch (Exception e) {
            log.info("Введенные данные не корректны.");
            return false;
        }
        return true;
    }

    private static void analyzeDeletedSubjectAccess(Result<AccessBarrier> accessBarrierResult) {
        if (accessBarrierResult.getCode() == Constants.CODE_ACCESS) {
            log.info("Доступ пользователя удалён: {}", accessBarrierResult.getResult());
        } else {
            log.info("Доступ пользователя не найден: статус код: {}", accessBarrierResult.getCode());
        }
    }

    private static void analyzeDeletedSubject(Result<Subject> result) {
        if (result.getCode() == Constants.CODE_ACCESS) {
            log.info("Пользователь успешно удален: {}", result.getResult());
        } else {
            log.info("Пользователь не найден: статус код = {}", result.getCode());
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

        Option optionEnv = new Option(Constants.CLI_ENVIRONMENT_PROPERTIES, true, Constants.CLI_DESCRIPTION_ENVIRONMENT_PROPERTIES);
        optionEnv.setArgName(Constants.CLI_ARGS_NAME_ENVIRONMENT_PROPERTIES);
        optionEnv.setArgs(1);
        optionEnv.setOptionalArg(true);

        Option optionDataType = new Option(Constants.CLI_DATA_TYPE, true, Constants.CLI_DESCRIPTION_DATA_TYPE);
        optionDataType.setArgName(Constants.CLI_ARGS_NAME_DATA_TYPE);
        optionDataType.setArgs(1);
        optionDataType.setOptionalArg(true);

        Option optionLog = new Option(Constants.CLI_LOG, true, Constants.CLI_DESCRIPTION_LOG);
        optionLog.setArgName(Constants.CLI_ARGS_NAME_LOG);
        optionLog.setArgs(1);
        optionLog.setOptionalArg(true);

        Option optionAnimalRegistration = new Option(Constants.CLI_NEW_ANIMAL, true, Constants.CLI_DESCRIPTION_NEW_ANIMAL);
        optionAnimalRegistration.setArgName(Constants.CLI_ARGS_NAME_NEW_ANIMAL);
        optionAnimalRegistration.setArgs(2);
        optionAnimalRegistration.setOptionalArg(true);

        Option optionTransportRegistration = new Option(Constants.CLI_NEW_TRANSPORT, true, Constants.CLI_DESCRIPTION_NEW_TRANSPORT);
        optionTransportRegistration.setArgName(Constants.CLI_ARGS_NAME_NEW_TRANSPORT);
        optionTransportRegistration.setArgs(2);
        optionTransportRegistration.setOptionalArg(true);

        Option optionHumanRegistration = new Option(Constants.CLI_NEW_HUMAN, true, Constants.CLI_DESCRIPTION_NEW_HUMAN);
        optionHumanRegistration.setArgName(Constants.CLI_ARGS_NAME_NEW_HUMAN);
        optionHumanRegistration.setArgs(7);
        optionHumanRegistration.setOptionalArg(true);

        Option optionPrintSubjects = new Option(Constants.CLI_PRINT_SUBJECTS, false, Constants.CLI_DESCRIPTION_PRINT_SUBJECTS);
        optionPrintSubjects.setOptionalArg(true);

        Option optionBarrierRegistration = new Option(Constants.CLI_NEW_BARRIER, true, Constants.CLI_DESCRIPTION_NEW_BARRIER);
        optionBarrierRegistration.setArgName(Constants.CLI_ARGS_NAME_NEW_BARRIER);
        optionBarrierRegistration.setArgs(1);
        optionBarrierRegistration.setOptionalArg(true);

        Option optionPrintBarrier = new Option(Constants.CLI_PRINT_BARRIERS, false, Constants.CLI_DESCRIPTION_PRINT_BARRIERS);
        optionPrintBarrier.setOptionalArg(true);

        Option optionGrantAccess = new Option(Constants.CLI_GRANT_ACCESS, true, Constants.CLI_DESCRIPTION_GRANT_ACCESS);
        optionGrantAccess.setArgName(Constants.CLI_ARGS_NAME_GRANT_ACCESS);
        optionGrantAccess.setArgs(6);
        optionGrantAccess.setOptionalArg(true);

        Option optionPrintSubjectAccess = new Option(Constants.CLI_PRINT_SUBJECT_ACCESS, true, Constants.CLI_DESCRIPTION_PRINT_SUBJECT_ACCESS);
        optionPrintSubjectAccess.setArgName(Constants.CLI_ARGS_NAME_PRINT_SUBJECT_ACCESS);
        optionPrintSubjectAccess.setArgs(1);
        optionPrintSubjectAccess.setOptionalArg(true);

        Option optionDeleteSubject = new Option(Constants.CLI_DELETE_SUBJECT, true, Constants.CLI_DESCRIPTION_DELETE_SUBJECT);
        optionDeleteSubject.setArgName(Constants.CLI_ARGS_NAME_DELETE_SUBJECT);
        optionDeleteSubject.setArgs(1);
        optionDeleteSubject.setOptionalArg(true);

        Option optionDeleteSubjectAccess = new Option(Constants.CLI_DELETE_SUBJECT_ACCESS, true, Constants.CLI_DESCRIPTION_DELETE_SUBJECT_ACCESS);
        optionDeleteSubjectAccess.setArgName(Constants.CLI_ARGS_NAME_DELETE_SUBJECT_ACCESS);
        optionDeleteSubjectAccess.setArgs(2);
        optionDeleteSubjectAccess.setOptionalArg(true);

        Option optionPrintHistory = new Option(Constants.CLI_PRINT_HISTORY, true, Constants.CLI_DESCRIPTION_PRINT_HISTORY);
        optionPrintHistory.setArgName(Constants.CLI_ARGS_NAME_PRINT_HISTORY);
        optionPrintHistory.setArgs(1);
        optionPrintHistory.setOptionalArg(true);

        Option optionGateAction = new Option(Constants.CLI_GATE_ACTION, true, Constants.CLI_DESCRIPTION_GATE_ACTION);
        optionGateAction.setArgName(Constants.CLI_ARGS_NAME_GATE_ACTION);
        optionGateAction.setArgs(3);
        optionGateAction.setOptionalArg(true);

        Option optionHelp = new Option(Constants.CLI_HELP, false, Constants.CLI_DESCRIPTION_HELP);
        optionHelp.setOptionalArg(true);


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
        options.addOption(optionDeleteSubject);
        options.addOption(optionDeleteSubjectAccess);
        options.addOption(optionPrintHistory);
        options.addOption(optionGateAction);
        options.addOption(optionDataType);
        options.addOption(optionHelp);
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

    private static void printSubjectHistory(Result<TreeMap<History, List<Motion>>> result) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        if (result.getCode() == Constants.CODE_ACCESS) {
            TreeMap<History, List<Motion>> treeMap = result.getResult();
            for (History history : treeMap.keySet()) {
                List<Motion> motions = treeMap.get(history);
                calendar.setTimeInMillis(history.getDate());
                log.info("History: id = {}, subjectId = {}, date = {}", history.getId(), history.getSubjectId(), dateFormat.format(calendar.getTime()));
                motions.forEach(motion -> log.info("Motion: {}", motion));
            }
        } else {
            log.info("История не найдена. Статус код = {}", result.getCode());
        }
    }

    public static void printHelp(
            final Options options,
            final int printedRowWidth,
            final String header,
            final String footer,
            final int spacesBeforeOption,
            final int spacesBeforeOptionDescription,
            final boolean displayUsage,
            final OutputStream out) {
        final String commandLineSyntax = "java -jar accessBarrier.jar";
        final PrintWriter writer = new PrintWriter(out);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                printedRowWidth,
                commandLineSyntax,
                header,
                options,
                spacesBeforeOption,
                spacesBeforeOptionDescription,
                footer,
                displayUsage);
        writer.flush();
    }
}
