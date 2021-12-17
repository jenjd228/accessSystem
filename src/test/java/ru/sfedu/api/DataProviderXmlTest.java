package ru.sfedu.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import ru.sfedu.Constants;
import ru.sfedu.model.Animal;
import ru.sfedu.model.Result;
import ru.sfedu.model.Subject;

import java.util.AbstractMap;
import java.util.TreeMap;

import static ru.sfedu.utils.FileUtil.deleteFileOrFolderIfExists;

class DataProviderXmlTest extends BaseTest{

    private static final Logger log = LogManager.getLogger(DataProviderCsvTest.class.getName());

    private static String subjectsFilePath;
    private static String accessBarriersFilePath;
    private static String motionsFilePath;
    private static String historyFilePath;
    private static String barriersFilePath;
    private static DataProviderXml actualDataProviderXml;

    @BeforeAll
    static void beforeAll() {
        String testPathFolder = Constants.TEST_MAIN_FOLDER_PATH;
        subjectsFilePath = testPathFolder.concat(Constants.XML_PATH_FOLDER).concat(Constants.SUBJECT_FILENAME).concat(Constants.XML_FILE_TYPE);
        accessBarriersFilePath = testPathFolder.concat(Constants.XML_PATH_FOLDER).concat(Constants.ACCESSIBLE_BARRIERS_FILENAME).concat(Constants.XML_FILE_TYPE);
        motionsFilePath = testPathFolder.concat(Constants.XML_PATH_FOLDER).concat(Constants.MOTIONS_FILENAME).concat(Constants.XML_FILE_TYPE);
        historyFilePath = testPathFolder.concat(Constants.XML_PATH_FOLDER).concat(Constants.HISTORY_FILENAME).concat(Constants.XML_FILE_TYPE);
        barriersFilePath = testPathFolder.concat(Constants.XML_PATH_FOLDER).concat(Constants.BARRIERS_FILENAME).concat(Constants.XML_FILE_TYPE);
        actualDataProviderXml = new DataProviderXml(testPathFolder);
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
        Result<Object> actual = actualDataProviderXml.saveOrUpdateSubject(actualAnimal);
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

        Result<Object> actual = actualDataProviderXml.saveOrUpdateSubject(actualAnimal);
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

        actualDataProviderXml.saveOrUpdateSubject(animal);

        Result<Object> actual = actualDataProviderXml.saveOrUpdateSubject(newAnimal);
        Result<Subject> expected = new Result<>(null, Constants.CODE_ACCESS, newAnimal);
        log.info("saveOrUpdateSubjectIfExists [2]: actual data = {}", actual);
        log.info("saveOrUpdateSubjectIfExists [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("saveOrUpdateSubjectIfExists [4]: - test succeeded");
    }

    @Test
    void barrierRegistration() {
        log.info("barrierRegistration [1]: - test started");

        boolean actual = actualDataProviderXml.barrierRegistration(1);
        log.info("barrierRegistration [2]: actual data = {}", actual);
        log.info("barrierRegistration [3]: expected data = {}", true);

        Assertions.assertTrue(actual);
        log.info("barrierRegistration [4]: - test succeeded");
    }

    @Test
    void grantAccess() {
    }

    @Test
    void gateAction() {
    }
}