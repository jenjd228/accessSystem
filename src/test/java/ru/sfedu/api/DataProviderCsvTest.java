package ru.sfedu.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import ru.sfedu.Constants;
import ru.sfedu.model.Animal;
import ru.sfedu.model.MoveType;
import ru.sfedu.model.Result;
import ru.sfedu.model.Subject;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.TreeMap;

import static ru.sfedu.utils.FileUtil.createFileIfNotExists;
import static ru.sfedu.utils.FileUtil.deleteFileOrFolderIfExists;

class DataProviderCsvTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(DataProviderCsvTest.class.getName());

    private static String subjectsFilePath;
    private static String accessBarriersFilePath;
    private static String motionsFilePath;
    private static String historyFilePath;
    private static String barriersFilePath;
    private static DataProviderCsv actualDataProviderCsv;

    @BeforeAll
    static void beforeAll() {
        String testPathFolder = Constants.TEST_MAIN_FOLDER_PATH;
        subjectsFilePath = testPathFolder.concat(Constants.CSV_PATH_FOLDER).concat(Constants.SUBJECT_FILENAME).concat(Constants.CSV_FILE_TYPE);
        accessBarriersFilePath = testPathFolder.concat(Constants.CSV_PATH_FOLDER).concat(Constants.ACCESSIBLE_BARRIERS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        motionsFilePath = testPathFolder.concat(Constants.CSV_PATH_FOLDER).concat(Constants.MOTIONS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        historyFilePath = testPathFolder.concat(Constants.CSV_PATH_FOLDER).concat(Constants.HISTORY_FILENAME).concat(Constants.CSV_FILE_TYPE);
        barriersFilePath = testPathFolder.concat(Constants.CSV_PATH_FOLDER).concat(Constants.BARRIERS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        actualDataProviderCsv = new DataProviderCsv(testPathFolder);
    }

    @AfterAll
    static void tearDown() {
    }

    @BeforeEach
    void setUp() {
        deleteFileOrFolderIfExists(subjectsFilePath);
        deleteFileOrFolderIfExists(accessBarriersFilePath);
        deleteFileOrFolderIfExists(barriersFilePath);
        deleteFileOrFolderIfExists(motionsFilePath);
        deleteFileOrFolderIfExists(historyFilePath);
    }

    @Test
    void saveOrUpdateSubjectIfNotExists() {
        log.info("saveOrUpdateSubjectIfNotExists [1]: - test started");
        Animal actualAnimal = createAnimal(null, "Red", "animal");
        Animal expectedAnimal = createAnimal(1, "Red", "animal");
        Result<Object> actual = actualDataProviderCsv.saveOrUpdateSubject(actualAnimal);
        Result<Subject> expected = new Result<>(null, Constants.CODE_ACCESS, expectedAnimal);
        log.info("saveOrUpdateSubjectIfNotExists [2]: actual data = {}", actual);
        log.info("saveOrUpdateSubjectIfNotExists [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("saveOrUpdateSubjectIfNotExists [4]: - test succeeded");
    }

    @Test
    void saveOrUpdateSubjectIfNotValid() {
        log.info("saveOrUpdateSubjectIfNotExists [1]: - test started");
        Animal actualAnimal = createAnimal(null, "Red2", "animal123");
        Animal expectedAnimal = createAnimal(null, "Red2", "animal123");
        TreeMap<String, String> errors = new TreeMap<>();
        errors.put(Constants.KEY_COLOR, Constants.NOT_VALID_COLOR);
        errors.put(Constants.KEY_NICKNAME, Constants.NOT_VALID_NICKNAME);

        Result<Object> actual = actualDataProviderCsv.saveOrUpdateSubject(actualAnimal);
        Result<TreeMap<String, String>> expected = new Result(null, Constants.CODE_INVALID_DATA, new AbstractMap.SimpleEntry<>(expectedAnimal, errors));
        log.info("saveOrUpdateSubjectIfNotExists [2]: actual data = {}", actual);
        log.info("saveOrUpdateSubjectIfNotExists [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("saveOrUpdateSubjectIfNotExists [4]: - test succeeded");
    }

    @Test
    void saveOrUpdateSubjectIfExists() {
        log.info("saveOrUpdateSubjectIfExists [1]: - test started");
        Animal animal = createAnimal(null, "Red", "animal");
        Animal newAnimal = createAnimal(1, "Black", "animal");

        actualDataProviderCsv.saveOrUpdateSubject(animal);

        Result<Object> actual = actualDataProviderCsv.saveOrUpdateSubject(newAnimal);
        Result<Subject> expected = new Result<>(null, Constants.CODE_ACCESS, newAnimal);
        log.info("saveOrUpdateSubjectIfExists [2]: actual data = {}", actual);
        log.info("saveOrUpdateSubjectIfExists [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("saveOrUpdateSubjectIfExists [4]: - test succeeded");
    }

    @Test
    void barrierRegistration() {
        log.info("barrierRegistration [1]: - test started");

        boolean actual = actualDataProviderCsv.barrierRegistration(1);
        log.info("barrierRegistration [2]: actual data = {}", actual);
        log.info("barrierRegistration [3]: expected data = {}", true);

        Assertions.assertTrue(actual);
        log.info("barrierRegistration [4]: - test succeeded");
    }

    @Test
    void gateActionIfHasAccess() {
        log.info("barrierRegistration [1]: - test started");

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("gateActionIfHasAccess [2]: error = {}", e.getMessage());
        }

        actualDataProviderCsv.barrierRegistration(1);
        actualDataProviderCsv.saveOrUpdateSubject(createAnimal(1,"Red","animal"));
        actualDataProviderCsv.grantAccess(1,1,2025,1,1,1);
        boolean actual = actualDataProviderCsv.gateAction(1, 1, MoveType.IN);
        log.info("barrierRegistration [2]: actual data = {}", actual);
        log.info("barrierRegistration [3]: expected data = {}", true);

        Assertions.assertTrue(actual);
        log.info("barrierRegistration [4]: - test succeeded");
    }

    @Test
    void gateActionIfNoAccess() {
        log.info("barrierRegistration [1]: - test started");

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("gateActionIfHasAccess [2]: error = {}", e.getMessage());
        }

        actualDataProviderCsv.barrierRegistration(1);
        actualDataProviderCsv.grantAccess(1,1,2020,1,1,1);
        boolean actual = actualDataProviderCsv.gateAction(1, 1, MoveType.IN);
        log.info("barrierRegistration [2]: actual data = {}", actual);
        log.info("barrierRegistration [3]: expected data = {}", false);

        Assertions.assertFalse(actual);
        log.info("barrierRegistration [4]: - test succeeded");
    }

    @Test
    void gateActionIfNoBarrier() {
        log.info("barrierRegistration [1]: - test started");

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("gateActionIfHasAccess [2]: error = {}", e.getMessage());
        }

        boolean actual = actualDataProviderCsv.gateAction(1, 1, MoveType.IN);
        log.info("barrierRegistration [2]: actual data = {}", actual);
        log.info("barrierRegistration [3]: expected data = {}", false);

        Assertions.assertFalse(actual);
        log.info("barrierRegistration [4]: - test succeeded");
    }

    @Test
    void grantAccessIfBarrierNotFound() {
        log.info("barrierRegistration [1]: - test started");

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("gateActionIfHasAccess [2]: error = {}", e.getMessage());
        }

        actualDataProviderCsv.saveOrUpdateSubject(createAnimal(null,"Red","animal"));
        Result<Object> actual = actualDataProviderCsv.grantAccess(1, 1,2025,1,1,1);
        TreeMap<String,String> errors = new TreeMap<>();
        errors.put(Constants.KEY_BARRIER,Constants.NOT_FOUND_BARRIER);
        Result<Object> expected = new Result<>(null,Constants.CODE_INVALID_DATA,errors);
        log.info("barrierRegistration [2]: actual data = {}", actual);
        log.info("barrierRegistration [3]: expected data = {}", expected);

        Assertions.assertEquals(expected,actual);
        log.info("barrierRegistration [4]: - test succeeded");
    }

    @Test
    void grantAccessIfSubjectNotFound() {
        log.info("barrierRegistration [1]: - test started");

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("gateActionIfHasAccess [2]: error = {}", e.getMessage());
        }

        actualDataProviderCsv.barrierRegistration(2);
        Result<Object> actual = actualDataProviderCsv.grantAccess(1, 1,2025,1,1,1);
        TreeMap<String,String> errors = new TreeMap<>();
        errors.put(Constants.KEY_SUBJECT,Constants.NOT_FOUND_SUBJECT);
        Result<Object> expected = new Result<>(null,Constants.CODE_INVALID_DATA,errors);
        log.info("barrierRegistration [2]: actual data = {}", actual);
        log.info("barrierRegistration [3]: expected data = {}", expected);

        Assertions.assertEquals(expected,actual);
        log.info("barrierRegistration [4]: - test succeeded");
    }

    @Test
    void grantAccessIfSubjectAndBarrierNotFound() {
        log.info("barrierRegistration [1]: - test started");

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("gateActionIfHasAccess [2]: error = {}", e.getMessage());
        }

        Result<Object> actual = actualDataProviderCsv.grantAccess(1, 1,2025,1,1,1);
        TreeMap<String,String> errors = new TreeMap<>();
        errors.put(Constants.KEY_BARRIER,Constants.NOT_FOUND_BARRIER);
        errors.put(Constants.KEY_SUBJECT,Constants.NOT_FOUND_SUBJECT);
        Result<Object> expected = new Result<>(null,Constants.CODE_INVALID_DATA,errors);
        log.info("barrierRegistration [2]: actual data = {}", actual);
        log.info("barrierRegistration [3]: expected data = {}", expected);

        Assertions.assertEquals(expected,actual);
        log.info("barrierRegistration [4]: - test succeeded");
    }

    @Test
    void grantAccessIfAllFound() {
        log.info("barrierRegistration [1]: - test started");

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("gateActionIfHasAccess [2]: error = {}", e.getMessage());
        }

        actualDataProviderCsv.barrierRegistration(1);
        actualDataProviderCsv.saveOrUpdateSubject(createAnimal(null,"Red","animal"));
        Result<Object> actual = actualDataProviderCsv.grantAccess(1, 1,2025,1,1,1);
        Result<Object> expected = new Result<>(null,Constants.CODE_ACCESS,null);
        log.info("barrierRegistration [2]: actual data = {}", actual);
        log.info("barrierRegistration [3]: expected data = {}", expected);

        Assertions.assertEquals(expected,actual);
        log.info("barrierRegistration [4]: - test succeeded");
    }

}