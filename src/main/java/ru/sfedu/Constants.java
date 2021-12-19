package ru.sfedu;

public class Constants {
    public static final int CODE_ERROR = 500;
    public static final int CODE_ACCESS = 200;
    public static final int CODE_NOT_FOUND = 404;
    public static final int CODE_INVALID_DATA = 422;

    public static final String TEST_MAIN_FOLDER_PATH = "src/test/testFolder/testActualDataFolder/";

    public static final String H2_PATH_FOLDER = "h2/";
    public static final String H2_DB_NAME = "h2db";
    public static final String H2_DRIVER = "ru.sfedu.h2.Driver";
    public static final String H2_PASSWORD = "ru.sfedu.h2.password";
    public static final String H2_LOGIN = "ru.sfedu.h2.login";
    public static final String H2_CONNECTOR = "ru.sfedu.h2.connector";

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

    public static final String REGEX_TRANSPORT_NUMBER = "^[a-zA-Z0-9- ]{3,20}$";
    public static final String REGEX_SHORT_STRING = "^[a-zA-Zа-яА-ЯёЁ]{3,20}$";
    public static final String REGEX_FIO_STRING = "^[a-zA-Zа-яА-ЯёЁ]{2,25}$";
    public static final String REGEX_PATRONYMIC = "^[a-zA-Zа-яА-ЯёЁ]{0,25}$";
    public static final String REGEX_PASSWORD = "^[a-zA-Z0-9_()*]{6,25}$";
    public static final String REGEX_LOGIN = "^[a-zA-Z0-9_]{3,25}$";
    public static final String REGEX_EMAIL = "^.+@.+\\..+$";

    public static final String KEY_ID = "id";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NAME = "name";
    public static final String KEY_SURNAME = "surname";
    public static final String KEY_PATRONYMIC = "patronymic";
    public static final String KEY_LOGIN = "login";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_COLOR = "color";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_SUBJECT = "subject";
    public static final String KEY_BARRIER = "barrier";
    public static final String KEY_SUBJECT_ID = "subjectId";
    public static final String KEY_DATE = "date";
    public static final String KEY_BARRIER_ID = "barrierId";
    public static final String KEY_HISTORY_ID = "historyId";
    public static final String KEY_MOVE_TYPE = "moveType";
    public static final String KEY_BARRIER_FLOOR = "barrierFloor";
    public static final String KEY_IS_OPEN = "isOpen";

    public static final String NOT_VALID_NAME = "Имя должно состоять из только букв латинского, длиною от 2 до 25 символов.";
    public static final String NOT_VALID_SURNAME = "Фамилия должна состоять из только букв латинского, длиною от 2 до 25 символов.";
    public static final String NOT_VALID_PATRONYMIC = "Отчество должно состоять из только букв латинского алфавита, длиною от 0 до 25 символов.";
    public static final String NOT_VALID_LOGIN = "Логин должен состоять только букв латинского алфавита, цифр и знака нижнего подчеркивания, длиною от 3 до 25 символов.";
    public static final String NOT_VALID_PASSWORD = "Пароль должен состоять только букв латинского алфавита, цифр и знаков _()*, длиною от 6 до 25 символов.";
    public static final String NOT_VALID_EMAIL = "Невалидный email";
    public static final String NOT_VALID_NUMBER = "Номер должен состоять только из букв латинского алфавита, цифр, пробелов и знака тире, длиною от 3 до 20";
    public static final String NOT_VALID_COLOR = "Цвет должен состоять из только букв латинского, длиною от 3 до 20 символов.";
    public static final String NOT_VALID_NICKNAME = "Кличка должена состоять из только букв латинского, длиною от 3 до 20 символов.";

    public static final String NOT_FOUND_BARRIER = "Такого входа нет в базе данных.";
    public static final String NOT_FOUND_SUBJECT = "Такого пользователя нет в базе данных.";

    public static final String SQL_TABLE_NAME_SUBJECT = "subject";
    public static final String SQL_TABLE_NAME_HISTORY = "history";
    public static final String SQL_TABLE_NAME_ACCESS_BARRIER = "accessBarrier";
    public static final String SQL_TABLE_NAME_MOTION = "motion";
    public static final String SQL_TABLE_NAME_BARRIER = "barrier";

    public static final String SQL_TABLE_CREATE_SUBJECT = "CREATE TABLE IF NOT EXISTS ".concat(SQL_TABLE_NAME_SUBJECT).concat("(").concat(KEY_ID).concat(" INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,").concat(KEY_TYPE).concat(" VARCHAR(20),").concat(KEY_NAME).concat(" VARCHAR(25),").concat(KEY_PASSWORD).concat(" VARCHAR(25),").concat(KEY_LOGIN).concat(" VARCHAR(25),").concat(KEY_SURNAME).concat(" VARCHAR(25),").concat(KEY_PATRONYMIC).concat(" VARCHAR(25),").concat(KEY_EMAIL).concat(" VARCHAR(35),".concat(KEY_COLOR).concat(" VARCHAR(20),").concat(KEY_NUMBER).concat(" VARCHAR(20))"));
    public static final String SQL_TABLE_CREATE_HISTORY = "CREATE TABLE IF NOT EXISTS ".concat(SQL_TABLE_NAME_HISTORY).concat("(").concat(KEY_ID).concat(" INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,").concat(KEY_SUBJECT_ID).concat(" BIGINT NOT NULL,").concat(KEY_DATE).concat(" BIGINT NOT NULL)");
    public static final String SQL_TABLE_CREATE_ACCESS_BARRIER = "CREATE TABLE IF NOT EXISTS ".concat(SQL_TABLE_NAME_ACCESS_BARRIER).concat("(").concat(KEY_ID).concat(" INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,").concat(KEY_SUBJECT_ID).concat(" INTEGER NOT NULL,").concat(KEY_BARRIER_ID).concat(" INTEGER NOT NULL,".concat(KEY_DATE).concat(" BIGINT NOT NULL)"));
    public static final String SQL_TABLE_CREATE_MOTION = "CREATE TABLE IF NOT EXISTS ".concat(SQL_TABLE_NAME_MOTION).concat("(").concat(KEY_ID).concat(" INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,").concat(KEY_BARRIER_ID).concat(" INTEGER NOT NULL,").concat(KEY_HISTORY_ID).concat(" INTEGER NOT NULL,").concat(KEY_MOVE_TYPE).concat(" VARCHAR(20))");
    public static final String SQL_TABLE_CREATE_BARRIER = "CREATE TABLE IF NOT EXISTS ".concat(SQL_TABLE_NAME_BARRIER).concat("(").concat(KEY_ID).concat(" INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,").concat(KEY_BARRIER_FLOOR).concat(" INTEGER NOT NULL,").concat(KEY_IS_OPEN).concat(" BOOLEAN NOT NULL)");

    public static final String SELECT_BARRIER_BY_ID = String.format("SELECT * FROM %s WHERE %s ",SQL_TABLE_NAME_BARRIER,KEY_ID).concat(" = %d");
    public static final String SELECT_ACCESS_BARRIER_IF_HAS_PERMISSION = String.format("SELECT * FROM %s WHERE %s ",SQL_TABLE_NAME_ACCESS_BARRIER,KEY_SUBJECT_ID).concat(" = %d AND ").concat(KEY_BARRIER_ID).concat(" = %d AND ").concat(KEY_DATE).concat(" > %d");
    public static final String SELECT_SUBJECT_BY_ID = String.format("SELECT * FROM %s WHERE %s ", SQL_TABLE_NAME_SUBJECT, KEY_ID).concat(" = %d");
    public static final String SELECT_HISTORY_BY_DATE_AND_SUBJECT_ID = "SELECT * FROM ".concat(SQL_TABLE_NAME_HISTORY).concat(" WHERE ").concat(KEY_DATE).concat(" = %d AND ").concat(KEY_SUBJECT_ID).concat(" = %d");

    public static final String INSERT_ACCESS_BARRIER = String.format("INSERT INTO %s (%s,%s,%s)",SQL_TABLE_NAME_ACCESS_BARRIER,KEY_SUBJECT_ID,KEY_BARRIER_ID,KEY_DATE).concat("VALUES('%d','%d','%d')");
    public static final String INSERT_MOTION = String.format("INSERT INTO %s (%s,%s,%s)",SQL_TABLE_NAME_MOTION,KEY_BARRIER_ID,KEY_HISTORY_ID,KEY_MOVE_TYPE).concat("VALUES('%d','%d','%s')");
    public static final String INSERT_HISTORY = String.format("INSERT INTO %s (%s,%s) ", SQL_TABLE_NAME_HISTORY, KEY_SUBJECT_ID, KEY_DATE).concat("VALUES('%d','%d')");
    public static final String INSERT_BARRIER = String.format("INSERT INTO %s (%s,%s) ", SQL_TABLE_NAME_BARRIER, KEY_BARRIER_FLOOR, KEY_IS_OPEN).concat("VALUES('%d','%b')");
    public static final String INSERT_SUBJECT = String.format("INSERT INTO %s (%s,%s,%s,%s,%s,%s,%s,%s,%s) ", SQL_TABLE_NAME_SUBJECT, KEY_TYPE, KEY_NAME, KEY_PASSWORD, KEY_LOGIN, KEY_SURNAME, KEY_PATRONYMIC, KEY_EMAIL, KEY_COLOR, KEY_NUMBER).concat("VALUES('%s','%s','%s','%s','%s','%s','%s','%s','%s')");

    public static final String UPDATE_BARRIER_IS_OPEN = "UPDATE ".concat(SQL_TABLE_NAME_BARRIER).concat(" set ").concat(KEY_IS_OPEN).concat(" = '%b' WHERE ").concat(KEY_ID).concat(" = %d");
    public static final String UPDATE_SUBJECT = "UPDATE ".concat(SQL_TABLE_NAME_SUBJECT).concat(" set ").concat(KEY_TYPE).concat(" = '%s',").concat(KEY_NAME).concat(" = '%s',").concat(KEY_PASSWORD).concat(" = '%s',").concat(KEY_LOGIN).concat(" = '%s',").concat(KEY_SURNAME).concat(" = '%s',").concat(KEY_PATRONYMIC).concat(" = '%s',").concat(KEY_EMAIL).concat(" = '%s',").concat(KEY_COLOR).concat(" = '%s',").concat(KEY_NUMBER).concat(" = '%s' WHERE ").concat(KEY_ID).concat(" = %d");
}
