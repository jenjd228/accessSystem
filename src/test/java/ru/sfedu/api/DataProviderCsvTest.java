package ru.sfedu.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import ru.sfedu.model.Animal;
import ru.sfedu.model.Result;
import ru.sfedu.model.Subject;
import ru.sfedu.utils.Constants;

import java.io.IOException;

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
    }

    @Test
    void saveOrUpdateSubjectIfNotExists() {
        log.info("saveOrUpdateSubjectIfNotExists [1]: - test started");
        Animal actualAnimal = createAnimal(null, "Red", "animal");
        Animal expectedAnimal = createAnimal(1, "Red", "animal");
        Result<Subject> actual = actualDataProviderCsv.saveOrUpdateSubject(actualAnimal);
        Result<Subject> expected = new Result<>(null, Constants.CODE_ACCESS, expectedAnimal);
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

        Result<Subject> actual = actualDataProviderCsv.saveOrUpdateSubject(newAnimal);
        Result<Subject> expected = new Result<>(null, Constants.CODE_ACCESS, newAnimal);
        log.info("saveOrUpdateSubjectIfExists [2]: actual data = {}", actual);
        log.info("saveOrUpdateSubjectIfExists [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("saveOrUpdateSubjectIfExists [4]: - test succeeded");
    }

    @Test
    void getSubjectByIdIfExists() {
        log.info("getSubjectByIdIfExists [1]: - test started");
        Animal animal = createAnimal(null, "Red", "animal");

        actualDataProviderCsv.saveOrUpdateSubject(animal);

        Result<Subject> actual = actualDataProviderCsv.getSubjectById(1);
        animal.setId(1);
        Result<Subject> expected = new Result<>(null, Constants.CODE_ACCESS, animal);
        log.info("getSubjectByIdIfExists [2]: actual data = {}", actual);
        log.info("getSubjectByIdIfExists [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getSubjectByIdIfExists [4]: - test succeeded");
    }

    @Test
    void getSubjectByIdIfNotExists() {
        log.info("getSubjectByIdIfNotExists [1]: - test started");
        try {
            createFileIfNotExists(subjectsFilePath);
        } catch (IOException e) {
            log.error("getSubjectByIdIfNotExists [2]: - error = {}", e.getMessage());
        }

        Result<Subject> actual = actualDataProviderCsv.getSubjectById(1);
        Result<Subject> expected = new Result<>(null, Constants.CODE_NOT_FOUND, null);
        log.info("getSubjectByIdIfNotExists [3]: actual data = {}", actual);
        log.info("getSubjectByIdIfNotExists [4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getSubjectByIdIfNotExists [5]: - test succeeded");
    }

    @Test
    void getSubjectByIdError() {
        log.info("getSubjectByIdIfNotExists [1]: - test started");

        Result<Subject> actual = actualDataProviderCsv.getSubjectById(1);
        Result<Subject> expected = new Result<>(subjectsFilePath.concat(" (No such file or directory)"), Constants.CODE_ERROR, null);
        log.info("getSubjectByIdIfNotExists [2]: actual data = {}", actual);
        log.info("getSubjectByIdIfNotExists [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getSubjectByIdIfNotExists [4]: - test succeeded");
    }

    @Test
    void isSubjectHasAccessCorrectDate() {
        log.info("isSubjectHasAccess [1]: - test started");

        actualDataProviderCsv.grantAccess(1, 1, getUtcTimeInMillis(2024, 12, 16, 1));
        boolean actual = actualDataProviderCsv.isSubjectHasAccess(1, 1);
        boolean expected = true;
        log.info("isSubjectHasAccess [2]: actual data = {}", actual);
        log.info("isSubjectHasAccess [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("isSubjectHasAccess [4]: - test succeeded");
    }

    @Test
    void isSubjectHasAccessNoAccessNoData() {
        log.info("isSubjectHasAccess [1]: - test started");

        try {
            createFileIfNotExists(accessBarriersFilePath);
        } catch (IOException e) {
            log.error("");
        }
        boolean actual = actualDataProviderCsv.isSubjectHasAccess(1, 1);
        boolean expected = false;
        log.info("isSubjectHasAccess [2]: actual data = {}", actual);
        log.info("isSubjectHasAccess [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("isSubjectHasAccess [4]: - test succeeded");
    }

    @Test
    void isSubjectHasAccessNoAccessNoFile() {
        log.info("isSubjectHasAccess [1]: - test started");

        boolean actual = actualDataProviderCsv.isSubjectHasAccess(1, 1);
        boolean expected = false;
        log.info("isSubjectHasAccess [2]: actual data = {}", actual);
        log.info("isSubjectHasAccess [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("isSubjectHasAccess [4]: - test succeeded");
    }

    @Test
    void isSubjectHasAccessNoAccessWrongDate() {
        log.info("isSubjectHasAccess [1]: - test started");

        actualDataProviderCsv.grantAccess(1, 1, getUtcTimeInMillis(2021, 12, 15, 1));
        boolean actual = actualDataProviderCsv.isSubjectHasAccess(1, 1);
        boolean expected = false;
        log.info("isSubjectHasAccess [2]: actual data = {}", actual);
        log.info("isSubjectHasAccess [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("isSubjectHasAccess [4]: - test succeeded");
    }

    @Test
    void saveMotion() {
    }

    @Test
    void createAndSaveHistory() {
    }

    @Test
    void barrierRegistration() {
    }

    @Test
    void grantAccess() {
    }

    @Test
    void openOrCloseBarrier() {
    }
}