package ru.sfedu;

import ru.sfedu.utils.ConfigurationUtil;

public class Constants {
    public static final int CODE_ERROR = 500;
    public static final int CODE_ACCESS = 200;
    public static final int CODE_NOT_FOUND = 404;
    public static final int CODE_INVALID_DATA = 422;

    public static final String TEST_MAIN_FOLDER_PATH = "src/test/testFolder/testActualDataFolder/";

    public static final char CSV_DEFAULT_SEPARATOR = ';';
    public static final String CSV_PATH_FOLDER = "csv/";
    public static final String CSV_FILE_TYPE = ".csv";

    public static final String XML_FILE_TYPE = ".xml";
    public static final String XML_PATH_FOLDER = "xml/";

    public static final String SUBJECT_FILENAME = "subjects";
    public static final String ACCESSIBLE_BARRIERS_FILENAME = "accessibleBarriers";
    public static final String MOTIONS_FILENAME = "motions";
    public static final String HISTORY_FILENAME = "history";
    public static final String BARRIERS_FILENAME = "barriers";

    public static final String ENV_TRANSPORT_NUMBER_MIN_LENGTH = "ru.sfedu.transport.number.min.length";
    public static final String ENV_TRANSPORT_NUMBER_MAX_LENGTH = "ru.sfedu.transport.number.max.length";
    public static final String ENV_PASSWORD_MIN_LENGTH = "ru.sfedu.password.min.length";
    public static final String ENV_PASSWORD_MAX_LENGTH = "ru.sfedu.password.max.length";

    public static final String REGEX_TRANSPORT_NUMBER = String.format("^[a-zA-Z0-9- ]{%s,%s}$", ConfigurationUtil.getConfigurationEntry(ENV_TRANSPORT_NUMBER_MIN_LENGTH), ConfigurationUtil.getConfigurationEntry(ENV_TRANSPORT_NUMBER_MAX_LENGTH));
    public static final String REGEX_SHORT_STRING = "^[a-zA-Zа-яА-ЯёЁ]{3,20}$";
    public static final String REGEX_FIO_STRING = "^[a-zA-Zа-яА-ЯёЁ]{2,25}$";
    public static final String REGEX_PATRONYMIC = "^[a-zA-Zа-яА-ЯёЁ]{0,25}$";
    public static final String REGEX_PASSWORD = String.format("^[a-zA-Z0-9_()*]{%s,%s}$", ConfigurationUtil.getConfigurationEntry(ENV_PASSWORD_MIN_LENGTH), ConfigurationUtil.getConfigurationEntry(ENV_PASSWORD_MAX_LENGTH));
    public static final String REGEX_LOGIN = "^[a-zA-Z0-9_]{3,25}$";
    public static final String REGEX_EMAIL = "^.+@.+\\..+$";

    public static final String KEY_NAME = "name";
    public static final String KEY_SURNAME = "surname";
    public static final String KEY_PATRONYMIC = "patronymic";
    public static final String KEY_LOGIN = "login";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_COLOR = "color";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_SUBJECT = "subject";
    public static final String KEY_BARRIER = "barrier";

    public static final String NOT_VALID_NAME = "Имя должно состоять из только букв латинского, длиною от 2 до 25 символов.";
    public static final String NOT_VALID_SURNAME = "Фамилия должна состоять из только букв латинского, длиною от 2 до 25 символов.";
    public static final String NOT_VALID_PATRONYMIC = "Отчество должно состоять из только букв латинского алфавита, длиною от 0 до 25 символов.";
    public static final String NOT_VALID_LOGIN = "Логин должен состоять только букв латинского алфавита, цифр и знака нижнего подчеркивания, длиною от 3 до 25 символов.";
    public static final String NOT_VALID_PASSWORD = String.format("Пароль должен состоять только букв латинского алфавита, цифр и знаков _()*, длиною от %s до %s символов.", ConfigurationUtil.getConfigurationEntry(ENV_PASSWORD_MIN_LENGTH), ConfigurationUtil.getConfigurationEntry(ENV_PASSWORD_MAX_LENGTH));
    public static final String NOT_VALID_EMAIL = "Невалидный email";
    public static final String NOT_VALID_NUMBER = String.format("Номер должен состоять только из букв латинского алфавита, цифр, пробелов и знака тире, длиною от %s до %s", ConfigurationUtil.getConfigurationEntry(ENV_TRANSPORT_NUMBER_MIN_LENGTH), ConfigurationUtil.getConfigurationEntry(ENV_TRANSPORT_NUMBER_MAX_LENGTH));
    public static final String NOT_VALID_COLOR = "Цвет должен состоять из только букв латинского, длиною от 3 до 20 символов.";
    public static final String NOT_VALID_NICKNAME = "Кличка должена состоять из только букв латинского, длиною от 3 до 20 символов.";

    public static final String NOT_FOUND_BARRIER = "Такого входа нет в базе данных.";
    public static final String NOT_FOUND_SUBJECT = "Такого пользователя нет в базе данных.";
}
