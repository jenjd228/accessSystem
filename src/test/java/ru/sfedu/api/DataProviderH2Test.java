package ru.sfedu.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import ru.sfedu.Constants;
import ru.sfedu.model.Animal;
import ru.sfedu.model.MoveType;
import ru.sfedu.model.Result;
import ru.sfedu.model.Subject;
import ru.sfedu.utils.FileUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static ru.sfedu.utils.ConfigurationUtil.getConfigurationEntry;

class DataProviderH2Test extends BaseTest {

    private static final Logger log = LogManager.getLogger(DataProviderH2Test.class.getName());

    private static DataProviderH2 actualDataProviderH2;
    private static String h2PathFolder = "./";

    @BeforeAll
    static void beforeAll() {
        String testPathFolder = Constants.TEST_MAIN_FOLDER_PATH;
        h2PathFolder = h2PathFolder.concat(testPathFolder).concat(Constants.H2_PATH_FOLDER);
        ;
        actualDataProviderH2 = new DataProviderH2(testPathFolder, Constants.MONGO_DB_NAME_FOR_TEST);
    }

    @AfterAll
    static void tearDown() {
        FileUtil.deleteFileOrFolderIfExists(Constants.TEST_MAIN_FOLDER_PATH.concat(Constants.H2_PATH_FOLDER).concat(Constants.H2_DB_NAME).concat(".mv.db"));
        FileUtil.deleteFileOrFolderIfExists(Constants.TEST_MAIN_FOLDER_PATH.concat(Constants.H2_PATH_FOLDER).concat(Constants.H2_DB_NAME).concat(".trace.db"));
    }

    @BeforeEach
    void setUp() {
        try {
            Connection connection = connection();
            Statement statement = connection.createStatement();
            clearTable(statement, Constants.SQL_TABLE_NAME_SUBJECT);
            resetId(statement, Constants.SQL_TABLE_NAME_SUBJECT);
            clearTable(statement, Constants.SQL_TABLE_NAME_ACCESS_BARRIER);
            resetId(statement, Constants.SQL_TABLE_NAME_ACCESS_BARRIER);
            clearTable(statement, Constants.SQL_TABLE_NAME_BARRIER);
            resetId(statement, Constants.SQL_TABLE_NAME_BARRIER);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            log.error("setUp [1]: {}", e.getMessage());
        }
    }

    private static Connection connection() throws SQLException {
        return DriverManager.getConnection(
                getConfigurationEntry(Constants.H2_CONNECTOR).concat(h2PathFolder).concat(Constants.H2_DB_NAME),
                getConfigurationEntry(Constants.H2_LOGIN),
                getConfigurationEntry(Constants.H2_PASSWORD));
    }

    @Test
    void subjectRegistrationIfNotExists() {
        log.info("subjectRegistrationIfNotExists [1]: - test started");
        Animal actualAnimal = createAnimal(null, "Red", "animal");
        Animal expectedAnimal = createAnimal(null, "Red", "animal");
        Result<Object> actual = actualDataProviderH2.subjectRegistration(actualAnimal);
        Result<Subject> expected = new Result<>(null, Constants.CODE_ACCESS, expectedAnimal);
        log.info("subjectRegistrationIfNotExists [2]: actual data = {}", actual);
        log.info("subjectRegistrationIfNotExists [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("subjectRegistrationIfNotExists [4]: - test succeeded");
    }

    @Test
    void subjectRegistrationIfNotValid() {
        log.info("subjectRegistrationIfNotExists [1]: - test started");
        Animal actualAnimal = createAnimal(null, "Red2", "animal123");
        Animal expectedAnimal = createAnimal(null, "Red2", "animal123");
        TreeMap<String, String> errors = new TreeMap<>();
        errors.put(Constants.KEY_COLOR, Constants.NOT_VALID_COLOR);
        errors.put(Constants.KEY_NAME, Constants.NOT_VALID_NICKNAME);

        Result<Object> actual = actualDataProviderH2.subjectRegistration(actualAnimal);
        Result<TreeMap<String, String>> expected = new Result(null, Constants.CODE_INVALID_DATA, new AbstractMap.SimpleEntry<>(expectedAnimal, errors));
        log.info("subjectRegistrationIfNotExists [2]: actual data = {}", actual);
        log.info("subjectRegistrationIfNotExists [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("subjectRegistrationIfNotExists [4]: - test succeeded");
    }

    @Test
    void subjectRegistrationIfExists() {
        log.info("subjectRegistrationIfExists [1]: - test started");
        Animal animal = createAnimal(null, "Red", "animal");
        Animal newAnimal = createAnimal(1, "Black", "animal");

        actualDataProviderH2.subjectRegistration(animal);

        Result<Object> actual = actualDataProviderH2.subjectRegistration(newAnimal);
        Result<Subject> expected = new Result<>(null, Constants.CODE_ACCESS, newAnimal);
        log.info("subjectRegistrationIfExists [2]: actual data = {}", actual);
        log.info("subjectRegistrationIfExists [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("subjectRegistrationIfExists [4]: - test succeeded");
    }

    @Test
    void barrierRegistration() {
        log.info("barrierRegistration [1]: - test started");

        boolean actual = actualDataProviderH2.barrierRegistration(1);
        log.info("barrierRegistration [2]: actual data = {}", actual);
        log.info("barrierRegistration [3]: expected data = {}", true);

        Assertions.assertTrue(actual);
        log.info("barrierRegistration [4]: - test succeeded");
    }

    @Test
    void gateActionIfHasAccess() {
        log.info("barrierRegistration [1]: - test started");

        actualDataProviderH2.barrierRegistration(1);
        actualDataProviderH2.subjectRegistration(createAnimal(null, "Red", "animal"));
        actualDataProviderH2.grantAccess(1, 1, 2025, 1, 1, 1);
        boolean actual = actualDataProviderH2.gateAction(1, 1, MoveType.IN);
        log.info("gateActionIfHasAccess [2]: actual data = {}", actual);
        log.info("gateActionIfHasAccess [3]: expected data = {}", true);

        Assertions.assertTrue(actual);
        log.info("gateActionIfHasAccess [4]: - test succeeded");
    }

    @Test
    void gateActionIfNoAccess() {
        log.info("gateActionIfNoAccess [1]: - test started");

        actualDataProviderH2.barrierRegistration(1);
        actualDataProviderH2.grantAccess(1, 1, 2020, 1, 1, 1);
        boolean actual = actualDataProviderH2.gateAction(1, 1, MoveType.IN);
        log.info("gateActionIfNoAccess [2]: actual data = {}", actual);
        log.info("gateActionIfNoAccess [3]: expected data = {}", false);

        Assertions.assertFalse(actual);
        log.info("gateActionIfNoAccess [4]: - test succeeded");
    }

    @Test
    void gateActionIfNoBarrier() {
        log.info("gateActionIfNoBarrier [1]: - test started");

        boolean actual = actualDataProviderH2.gateAction(1, 1, MoveType.IN);
        log.info("gateActionIfNoBarrier [2]: actual data = {}", actual);
        log.info("gateActionIfNoBarrier [3]: expected data = {}", false);

        Assertions.assertFalse(actual);
        log.info("gateActionIfNoBarrier [4]: - test succeeded");
    }

    @Test
    void grantAccessIfBarrierNotFound() {
        log.info("grantAccessIfBarrierNotFound [1]: - test started");

        actualDataProviderH2.subjectRegistration(createAnimal(null, "Red", "animal"));
        Result<Object> actual = actualDataProviderH2.grantAccess(1, 1, 2025, 1, 1, 1);
        TreeMap<String, String> errors = new TreeMap<>();
        errors.put(Constants.KEY_BARRIER, Constants.NOT_FOUND_BARRIER);
        Result<Object> expected = new Result<>(null, Constants.CODE_INVALID_DATA, errors);
        log.info("grantAccessIfBarrierNotFound [2]: actual data = {}", actual);
        log.info("grantAccessIfBarrierNotFound [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("grantAccessIfBarrierNotFound [4]: - test succeeded");
    }

    @Test
    void grantAccessIfSubjectNotFound() {
        log.info("grantAccessIfSubjectNotFound [1]: - test started");

        actualDataProviderH2.barrierRegistration(2);
        Result<Object> actual = actualDataProviderH2.grantAccess(1, 1, 2025, 1, 1, 1);
        TreeMap<String, String> errors = new TreeMap<>();
        errors.put(Constants.KEY_SUBJECT, Constants.NOT_FOUND_SUBJECT);
        Result<Object> expected = new Result<>(null, Constants.CODE_INVALID_DATA, errors);
        log.info("grantAccessIfSubjectNotFound [2]: actual data = {}", actual);
        log.info("grantAccessIfSubjectNotFound [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("grantAccessIfSubjectNotFound [4]: - test succeeded");
    }

    @Test
    void grantAccessIfSubjectAndBarrierNotFound() {
        log.info("grantAccessIfSubjectAndBarrierNotFound [1]: - test started");

        Result<Object> actual = actualDataProviderH2.grantAccess(1, 1, 2025, 1, 1, 1);
        TreeMap<String, String> errors = new TreeMap<>();
        errors.put(Constants.KEY_BARRIER, Constants.NOT_FOUND_BARRIER);
        errors.put(Constants.KEY_SUBJECT, Constants.NOT_FOUND_SUBJECT);
        Result<Object> expected = new Result<>(null, Constants.CODE_INVALID_DATA, errors);
        log.info("grantAccessIfSubjectAndBarrierNotFound [2]: actual data = {}", actual);
        log.info("grantAccessIfSubjectAndBarrierNotFound [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("grantAccessIfSubjectAndBarrierNotFound [4]: - test succeeded");
    }

    @Test
    void grantAccessIfAllFound() {
        log.info("grantAccessIfAllFound [1]: - test started");

        actualDataProviderH2.barrierRegistration(1);
        actualDataProviderH2.subjectRegistration(createAnimal(null, "Red", "animal"));
        Result<Object> actual = actualDataProviderH2.grantAccess(1, 1, 2025, 1, 1, 1);
        Result<Object> expected = new Result<>(null, Constants.CODE_ACCESS, null);
        log.info("grantAccessIfAllFound [2]: actual data = {}", actual);
        log.info("grantAccessIfAllFound [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("grantAccessIfAllFound [4]: - test succeeded");
    }

    @Test
    void getAllSubjectsIfNoSubjects() {
        log.info("getAllSubjectsIfNoSubjects [1]: - test started");

        List<Subject> actual = actualDataProviderH2.getAllUsers();
        List<Subject> expected = new ArrayList<>();
        log.info("getAllSubjectsIfNoSubjects [3]: actual data = {}", actual);
        log.info("getAllSubjectsIfNoSubjects [4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getAllSubjectsIfNoSubjects [5]: - test succeeded");
    }

    @Test
    void getAllSubjectsIfSubjectsExits() {
        log.info("getAllSubjectsIfSubjectsExits [1]: - test started");

        Animal animal = createAnimal(null, "Red", "animal");
        actualDataProviderH2.subjectRegistration(animal);
        List<Subject> actual = actualDataProviderH2.getAllUsers();
        List<Subject> expected = new ArrayList<>();
        animal.setId(1);
        expected.add(animal);
        log.info("getAllSubjectsIfSubjectsExits [3]: actual data = {}", actual);
        log.info("getAllSubjectsIfSubjectsExits [4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getAllSubjectsIfSubjectsExits [5]: - test succeeded");
    }

    private void resetId(Statement statement, String dbName) {
        try {
            statement.executeUpdate("ALTER TABLE " + dbName + " DROP id");
            statement.executeUpdate("ALTER TABLE " + dbName + " ADD  id BIGINT NOT NULL AUTO_INCREMENT FIRST");
            statement.executeUpdate("ALTER TABLE " + dbName + " ADD  CONSTRAINT users_pk PRIMARY KEY(id)");
        } catch (SQLException e) {
            log.error("getUserByIdIfUserNoExists [1]: {}", e.getMessage());
        }
    }

    private void clearTable(Statement statement, String dbName) {
        try {
            statement.executeUpdate("delete from " + dbName);
        } catch (SQLException e) {
            log.error("getUserByIdIfUserNoExists [1]: {}", e.getMessage());
        }
    }
}