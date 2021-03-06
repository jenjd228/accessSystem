package ru.sfedu.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import ru.sfedu.Constants;
import ru.sfedu.model.*;
import ru.sfedu.utils.SubjectUtil;
import ru.sfedu.utils.TImeUtil;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static ru.sfedu.utils.FileUtil.createFileIfNotExists;
import static ru.sfedu.utils.FileUtil.deleteFileOrFolderIfExists;

class DataProviderXmlTest extends BaseTest {

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
        actualDataProviderXml = new DataProviderXml(testPathFolder, Constants.MONGO_DB_NAME_FOR_TEST);
    }

    @AfterAll
    static void tearDown() {
        deleteAllFiles();
    }

    @BeforeEach
    void setUp() {
        deleteAllFiles();
    }

    private static void deleteAllFiles() {
        deleteFileOrFolderIfExists(subjectsFilePath);
        deleteFileOrFolderIfExists(accessBarriersFilePath);
        deleteFileOrFolderIfExists(barriersFilePath);
        deleteFileOrFolderIfExists(motionsFilePath);
        deleteFileOrFolderIfExists(historyFilePath);
    }

    @Test
    void subjectRegistrationIfNotExists() {
        log.info("subjectRegistrationIfNotExists [1]: - test started");
        Animal actualAnimal = createAnimal(null, "Red", "animal");
        Animal expectedAnimal = createAnimal(1, "Red", "animal");
        Result<Object> actual = actualDataProviderXml.subjectRegistration(actualAnimal);
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

        Result<Object> actual = actualDataProviderXml.subjectRegistration(actualAnimal);
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

        actualDataProviderXml.subjectRegistration(animal);

        Result<Object> actual = actualDataProviderXml.subjectRegistration(newAnimal);
        Result<Subject> expected = new Result<>(null, Constants.CODE_ACCESS, newAnimal);
        log.info("subjectRegistrationIfExists [2]: actual data = {}", actual);
        log.info("subjectRegistrationIfExists [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("subjectRegistrationIfExists [4]: - test succeeded");
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
    void gateActionIfHasAccess() {
        log.info("barrierRegistration [1]: - test started");

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("gateActionIfHasAccess [2]: error = {}", e.getMessage());
        }

        actualDataProviderXml.barrierRegistration(1);
        actualDataProviderXml.subjectRegistration(createAnimal(1, "Red", "animal"));
        actualDataProviderXml.grantAccess(1, 1, 2025, 1, 1, 1);
        boolean actual = actualDataProviderXml.gateAction(1, 1, MoveType.IN);
        log.info("gateActionIfHasAccess [2]: actual data = {}", actual);
        log.info("gateActionIfHasAccess [3]: expected data = {}", true);

        Assertions.assertTrue(actual);
        log.info("gateActionIfHasAccess [4]: - test succeeded");
    }

    @Test
    void gateActionIfNoAccess() {
        log.info("gateActionIfNoAccess [1]: - test started");

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("gateActionIfNoAccess [2]: error = {}", e.getMessage());
        }

        actualDataProviderXml.barrierRegistration(1);
        actualDataProviderXml.grantAccess(1, 1, 2020, 1, 1, 1);
        boolean actual = actualDataProviderXml.gateAction(1, 1, MoveType.IN);
        log.info("gateActionIfNoAccess [2]: actual data = {}", actual);
        log.info("gateActionIfNoAccess [3]: expected data = {}", false);

        Assertions.assertFalse(actual);
        log.info("gateActionIfNoAccess [4]: - test succeeded");
    }

    @Test
    void gateActionIfNoBarrier() {
        log.info("gateActionIfNoBarrier [1]: - test started");

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("gateActionIfNoBarrier [2]: error = {}", e.getMessage());
        }

        boolean actual = actualDataProviderXml.gateAction(1, 1, MoveType.IN);
        log.info("gateActionIfNoBarrier [2]: actual data = {}", actual);
        log.info("gateActionIfNoBarrier [3]: expected data = {}", false);

        Assertions.assertFalse(actual);
        log.info("gateActionIfNoBarrier [4]: - test succeeded");
    }

    @Test
    void grantAccessIfBarrierNotFound() {
        log.info("grantAccessIfBarrierNotFound [1]: - test started");

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("grantAccessIfBarrierNotFound [2]: error = {}", e.getMessage());
        }

        actualDataProviderXml.subjectRegistration(createAnimal(null, "Red", "animal"));
        Result<Object> actual = actualDataProviderXml.grantAccess(1, 1, 2025, 1, 1, 1);
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

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("grantAccessIfSubjectNotFound [2]: error = {}", e.getMessage());
        }

        actualDataProviderXml.barrierRegistration(2);
        Result<Object> actual = actualDataProviderXml.grantAccess(1, 1, 2025, 1, 1, 1);
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

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("grantAccessIfSubjectAndBarrierNotFound [2]: error = {}", e.getMessage());
        }

        Result<Object> actual = actualDataProviderXml.grantAccess(1, 1, 2025, 1, 1, 1);
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

        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("grantAccessIfAllFound [2]: error = {}", e.getMessage());
        }

        actualDataProviderXml.barrierRegistration(1);
        actualDataProviderXml.subjectRegistration(createAnimal(null, "Red", "animal"));
        Result<Object> actual = actualDataProviderXml.grantAccess(1, 1, 2025, 1, 1, 1);
        Result<Object> expected = new Result<>(null, Constants.CODE_ACCESS, null);
        log.info("grantAccessIfAllFound [2]: actual data = {}", actual);
        log.info("grantAccessIfAllFound [3]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("grantAccessIfAllFound [4]: - test succeeded");
    }

    @Test
    void getAllSubjectsIfNoSubjects() {
        log.info("getAllSubjectsIfNoSubjects [1]: - test started");

        try {
            createFileIfNotExists(subjectsFilePath);
        } catch (IOException e) {
            log.error("getAllSubjectsIfNoSubjects [2]: error = {}", e.getMessage());
        }
        List<Subject> actual = actualDataProviderXml.getAllSubjects();
        List<Subject> expected = new ArrayList<>();
        log.info("getAllSubjectsIfNoSubjects [3]: actual data = {}", actual);
        log.info("getAllSubjectsIfNoSubjects [4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getAllSubjectsIfNoSubjects [5]: - test succeeded");
    }

    @Test
    void getAllSubjectsIfSubjectsExits() {
        log.info("getAllSubjectsIfSubjectsExits [1]: - test started");

        try {
            createFileIfNotExists(subjectsFilePath);
        } catch (IOException e) {
            log.error("getAllSubjectsIfSubjectsExits [2]: error = {}", e.getMessage());
        }
        Animal animal = createAnimal(null, "Red", "animal");
        actualDataProviderXml.subjectRegistration(animal);
        List<Subject> actual = actualDataProviderXml.getAllSubjects();
        List<Subject> expected = new ArrayList<>();
        expected.add(animal);
        log.info("getAllSubjectsIfSubjectsExits [3]: actual data = {}", actual);
        log.info("getAllSubjectsIfSubjectsExits [4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getAllSubjectsIfSubjectsExits [5]: - test succeeded");
    }

    @Test
    void deleteSubjectIfExists() {
        log.info("deleteSubjectIfExists [1]: - test started");

        try {
            createFileIfNotExists(subjectsFilePath);
        } catch (IOException e) {
            log.error("getAllSubjectsIfSubjectsExits [2]: error = {}", e.getMessage());
        }
        Animal animal = createAnimal(null, "Red", "animal");
        actualDataProviderXml.subjectRegistration(animal);
        Result<Subject> actual = actualDataProviderXml.deleteSubjectById(1);
        Result<Subject> expected = new Result<>(null, Constants.CODE_ACCESS, animal);
        log.info("deleteSubjectIfExists [3]: actual data = {}", actual);
        log.info("deleteSubjectIfExists [4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("deleteSubjectIfExists [5]: - test succeeded");
    }

    @Test
    void deleteSubjectIfNotExists() {
        log.info("deleteSubjectIfExists [1]: - test started");

        try {
            createFileIfNotExists(subjectsFilePath);
        } catch (IOException e) {
            log.error("getAllSubjectsIfSubjectsExits [2]: error = {}", e.getMessage());
        }
        Result<Subject> actual = actualDataProviderXml.deleteSubjectById(1);
        Result<Subject> expected = new Result<>(null, Constants.CODE_NOT_FOUND, null);
        log.info("deleteSubjectIfExists [3]: actual data = {}", actual);
        log.info("deleteSubjectIfExists [4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("deleteSubjectIfExists [5]: - test succeeded");
    }

    @Test
    void getAllBarriersIfNoBarriers() {
        log.info("getAllBarriersIfNoBarriers [1]: - test started");

        try {
            createFileIfNotExists(subjectsFilePath);
        } catch (IOException e) {
            log.error("getAllBarriersIfNoBarriers [2]: error = {}", e.getMessage());
        }
        List<Barrier> actual = actualDataProviderXml.getAllBarriers();
        List<Barrier> expected = new ArrayList<>();
        log.info("getAllBarriersIfNoBarriers [3]: actual data = {}", actual);
        log.info("getAllBarriersIfNoBarriers [4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getAllBarriersIfNoBarriers [5]: - test succeeded");
    }

    @Test
    void getAllBarriersIfBarriersExists() {
        log.info("getAllBarriersIfBarriersExists [1]: - test started");

        try {
            createFileIfNotExists(subjectsFilePath);
        } catch (IOException e) {
            log.error("getAllBarriersIfBarriersExists[2]: error = {}", e.getMessage());
        }
        Barrier barrier = SubjectUtil.createBarrier(1, 2, false);
        actualDataProviderXml.barrierRegistration(2);
        List<Barrier> actual = actualDataProviderXml.getAllBarriers();
        List<Barrier> expected = new ArrayList<>();
        expected.add(barrier);
        log.info("getAllBarriersIfBarriersExists [3]: actual data = {}", actual);
        log.info("getAllBarriersIfBarriersExists [4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getAllBarriersIfBarriersExists [5]: - test succeeded");
    }

    @Test
    void getAccessBarriersBySubjectIdIfAccessBarriersExists() {
        log.info("getAccessBarriersBySubjectIdIfAccessBarriersExists[1]: - test started");

        try {
            createFileIfNotExists(accessBarriersFilePath);
        } catch (IOException e) {
            log.error("getAccessBarriersBySubjectIdIfAccessBarriersExists[2]: error = {}", e.getMessage());
        }
        Animal animal = createAnimal(null, "Red", "animal");
        AccessBarrier accessBarrier = SubjectUtil.createAccessBarrier(1, 1, 1, TImeUtil.getUtcTimeInMillis(2020, 1, 1, 1));
        actualDataProviderXml.barrierRegistration(2);
        actualDataProviderXml.subjectRegistration(animal);
        actualDataProviderXml.grantAccess(1, 1, 2020, 1, 1, 1);
        List<AccessBarrier> actual = actualDataProviderXml.getAccessBarriersBySubjectId(1);
        List<AccessBarrier> expected = new ArrayList<>();
        expected.add(accessBarrier);
        log.info("getAccessBarriersBySubjectIdIfAccessBarriersExists[3]: actual data = {}", actual);
        log.info("getAccessBarriersBySubjectIdIfAccessBarriersExists[4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getAccessBarriersBySubjectIdIfAccessBarriersExists[5]: - test succeeded");
    }

    @Test
    void getAccessBarriersBySubjectIdIfNoAccessBarriers() {
        log.info("getAccessBarriersBySubjectIdIfNoAccessBarriers[1]: - test started");

        try {
            createFileIfNotExists(accessBarriersFilePath);
        } catch (IOException e) {
            log.error("getAccessBarriersBySubjectIdIfNoAccessBarriers[2]: error = {}", e.getMessage());
        }
        List<AccessBarrier> actual = actualDataProviderXml.getAccessBarriersBySubjectId(1);
        List<AccessBarrier> expected = new ArrayList<>();
        log.info("getAccessBarriersBySubjectIdIfNoAccessBarriers[3]: actual data = {}", actual);
        log.info("getAccessBarriersBySubjectIdIfNoAccessBarriers[4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getAccessBarriersBySubjectIdIfNoAccessBarriers[5]: - test succeeded");
    }

    @Test
    void deleteAccessBarrierBySubjectAndBarrierIdIfNoExists() {
        log.info("deleteAccessBarrierBySubjectAndBarrierIdIfNoExists[1]: - test started");

        try {
            createFileIfNotExists(accessBarriersFilePath);
        } catch (IOException e) {
            log.error("deleteAccessBarrierBySubjectAndBarrierIdIfNoExists[2]: error = {}", e.getMessage());
        }
        Result<AccessBarrier> actual = actualDataProviderXml.deleteAccessBarrierBySubjectAndBarrierId(1, 1);
        Result<AccessBarrier> expected = new Result<>(null, Constants.CODE_NOT_FOUND, null);
        log.info("deleteAccessBarrierBySubjectAndBarrierIdIfNoExists[3]: actual data = {}", actual);
        log.info("deleteAccessBarrierBySubjectAndBarrierIdIfNoExists[4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("deleteAccessBarrierBySubjectAndBarrierIdIfNoExists[5]: - test succeeded");
    }

    @Test
    void deleteAccessBarrierBySubjectAndBarrierIdIfExists() {
        log.info("deleteAccessBarrierBySubjectAndBarrierIdIfExists[1]: - test started");

        try {
            createFileIfNotExists(accessBarriersFilePath);
        } catch (IOException e) {
            log.error("deleteAccessBarrierBySubjectAndBarrierIdIfExists[2]: error = {}", e.getMessage());
        }
        Animal animal = createAnimal(null, "Red", "animal");
        AccessBarrier accessBarrier = SubjectUtil.createAccessBarrier(1, 1, 1, TImeUtil.getUtcTimeInMillis(2020, 1, 1, 1));
        actualDataProviderXml.barrierRegistration(2);
        actualDataProviderXml.subjectRegistration(animal);
        actualDataProviderXml.grantAccess(1, 1, 2020, 1, 1, 1);
        Result<AccessBarrier> actual = actualDataProviderXml.deleteAccessBarrierBySubjectAndBarrierId(1, 1);
        Result<AccessBarrier> expected = new Result<>(null, Constants.CODE_ACCESS, accessBarrier);
        log.info("deleteAccessBarrierBySubjectAndBarrierIdIfExists[3]: actual data = {}", actual);
        log.info("deleteAccessBarrierBySubjectAndBarrierIdIfExists[4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("deleteAccessBarrierBySubjectAndBarrierIdIfExists[5]: - test succeeded");
    }

    @Test
    void getSubjectHistoryBySubjectIdIfHistoryExists() {
        log.info("getSubjectHistoryBySubjectId[1]: - test started");
        try {
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
        } catch (IOException e) {
            log.error("getSubjectHistoryBySubjectId[2]: error = {}", e.getMessage());
        }

        Animal animal = createAnimal(null, "Red", "animal");
        Result<TreeMap<History, List<Motion>>> expected = new Result<>();
        TreeMap<History, List<Motion>> treeMap = new TreeMap<>();
        History history = SubjectUtil.createHistory(1, 1, TImeUtil.getCurrentUtcTimeInMillis());
        Motion motion = SubjectUtil.createMotion(1, 1, 1, MoveType.IN);
        List<Motion> list = new ArrayList<>();
        list.add(motion);
        treeMap.put(history, list);
        expected.setCode(Constants.CODE_ACCESS);
        expected.setResult(treeMap);
        actualDataProviderXml.barrierRegistration(2);
        actualDataProviderXml.subjectRegistration(animal);
        actualDataProviderXml.grantAccess(1, 1, 2025, 1, 1, 1);
        actualDataProviderXml.gateAction(1, 1, MoveType.IN);
        Result<TreeMap<History, List<Motion>>> actual = actualDataProviderXml.getSubjectHistoryBySubjectId(1);

        log.info("getSubjectHistoryBySubjectIdIfExists[3]: actual data = {}", actual);
        log.info("getSubjectHistoryBySubjectIdIfExists[4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getSubjectHistoryBySubjectId[5]: - test succeeded");
    }

    @Test
    void getSubjectHistoryBySubjectIdIfHistoryNotExists() {
        log.info("getSubjectHistoryBySubjectIdIfHistoryNotExists[1]: - test started");

        Result<TreeMap<History, List<Motion>>> expected = new Result<>();
        expected.setCode(Constants.CODE_NOT_FOUND);
        expected.setResult(null);
        Result<TreeMap<History, List<Motion>>> actual = actualDataProviderXml.getSubjectHistoryBySubjectId(1);

        log.info("getSubjectHistoryBySubjectIdIfHistoryNotExists[3]: actual data = {}", actual);
        log.info("getSubjectHistoryBySubjectIdIfHistoryNotExists[4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("getSubjectHistoryBySubjectIdIfHistoryNotExists[5]: - test succeeded");
    }

    @Test
    void deleteBarrierIfNotExists() {
        log.info("deleteBarrierIfNotExist[1]: - test started");

        try {
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("deleteBarrierIfNotExist[2]: error = {}",e.getMessage());
        }

        Result<Barrier> expected = new Result<>(null,Constants.CODE_NOT_FOUND,null);
        Result<Barrier> actual = actualDataProviderXml.deleteBarrierById(1);

        log.info("deleteBarrierIfNotExist[3]: actual data = {}", actual);
        log.info("deleteBarrierIfNotExist[4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("deleteBarrierIfNotExist[5]: - test succeeded");
    }

    @Test
    void deleteBarrierIfFileNotFound() {
        log.info("deleteBarrierIfFileNotFound[1]: - test started");


        Result<Barrier> expected = new Result<>(null,Constants.CODE_ERROR,null);
        Result<Barrier> actual = actualDataProviderXml.deleteBarrierById(1);

        log.info("deleteBarrierIfFileNotFound[3]: actual data = {}", actual);
        log.info("deleteBarrierIfFileNotFound[4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("deleteBarrierIfFileNotFound[5]: - test succeeded");
    }

    @Test
    void deleteBarrierIfExists() {
        log.info("deleteBarrierIfExist[1]: - test started");

        Barrier barrier = SubjectUtil.createBarrier(1,1,false);
        Result<Barrier> expected = new Result<>(null,Constants.CODE_ACCESS,barrier);
        actualDataProviderXml.barrierRegistration(1);
        Result<Barrier> actual = actualDataProviderXml.deleteBarrierById(1);

        log.info("deleteBarrierIfExist[3]: actual data = {}", actual);
        log.info("deleteBarrierIfExist[4]: expected data = {}", expected);

        Assertions.assertEquals(expected, actual);
        log.info("deleteBarrierIfExist[5]: - test succeeded");
    }

}