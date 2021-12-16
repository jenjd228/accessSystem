package ru.sfedu.api;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.model.*;
import ru.sfedu.utils.Constants;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static ru.sfedu.utils.CsvUtil.getNewObjectId;
import static ru.sfedu.utils.CsvUtil.write;
import static ru.sfedu.utils.FileUtil.createFileIfNotExists;
import static ru.sfedu.utils.FileUtil.createFolderIfNotExists;
import static ru.sfedu.utils.SubjectUtil.getAllObjectFields;
import static ru.sfedu.utils.SubjectUtil.getAllSubjectFields;
import static ru.sfedu.utils.TImeUtil.getCurrentUtcTimeInMillis;

public class DataProviderCsv implements IDataProvider {

    private final Logger log = LogManager.getLogger(DataProviderCsv.class.getName());

    private final String subjectsFilePath;
    private final String accessBarriersFilePath;
    private final String motionsFilePath;
    private final String historyFilePath;
    private final String barriersFilePath;

    public DataProviderCsv() {
        subjectsFilePath = Constants.CSV_PATH_FOLDER.concat(Constants.SUBJECT_FILENAME).concat(Constants.CSV_FILE_TYPE);
        accessBarriersFilePath = Constants.CSV_PATH_FOLDER.concat(Constants.ACCESSIBLE_BARRIERS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        motionsFilePath = Constants.CSV_PATH_FOLDER.concat(Constants.MOTIONS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        historyFilePath = Constants.CSV_PATH_FOLDER.concat(Constants.HISTORY_FILENAME).concat(Constants.CSV_FILE_TYPE);
        barriersFilePath = Constants.CSV_PATH_FOLDER.concat(Constants.BARRIERS_FILENAME).concat(Constants.CSV_FILE_TYPE);

        try {
            createFolderIfNotExists(Constants.CSV_PATH_FOLDER);
            createCommonFiles();
        } catch (IOException e) {
            log.error("Data providerCsv - initialization error");
        }
    }

    public DataProviderCsv(String path) {
        String mainFolder = path.concat(Constants.CSV_PATH_FOLDER);
        subjectsFilePath = mainFolder.concat(Constants.SUBJECT_FILENAME).concat(Constants.CSV_FILE_TYPE);
        accessBarriersFilePath = mainFolder.concat(Constants.ACCESSIBLE_BARRIERS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        motionsFilePath = mainFolder.concat(Constants.MOTIONS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        historyFilePath = mainFolder.concat(Constants.HISTORY_FILENAME).concat(Constants.CSV_FILE_TYPE);
        barriersFilePath = mainFolder.concat(Constants.BARRIERS_FILENAME).concat(Constants.CSV_FILE_TYPE);

        try {
            createFolderIfNotExists(mainFolder);
            createCommonFiles();
        } catch (IOException e) {
            log.error("Data providerCsv - initialization error");
        }
    }

    private void createCommonFiles() throws IOException {
        createFileIfNotExists(subjectsFilePath);
        createFileIfNotExists(accessBarriersFilePath);
        createFileIfNotExists(motionsFilePath);
        createFileIfNotExists(historyFilePath);
        createFileIfNotExists(barriersFilePath);
    }

    @Override
    public Result<Subject> saveOrUpdateSubject(Subject subject) {
        log.info("saveSubject [1]: {}", subject);
        Result<Subject> result;
        try {
            Result<Subject> oldSubject = getSubjectById(subject.getId());
            if (oldSubject.getCode() == Constants.CODE_ACCESS) {
                log.info("saveSubject [2]: There is the same subject {}", oldSubject);
                result = saveModifySubject(subject);
            } else {
                log.info("saveSubject [3]: There is no the same subject");
                result = writeNewSubject(subject);
            }
        } catch (Exception e) {
            log.error("saveSubject [4]: {}", e.getMessage());
            return new Result<>(e.getMessage(), Constants.CODE_ERROR, null);
        }
        return result;
    }

    private Result<Subject> writeNewSubject(Subject subject) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, CsvValidationException {
        log.info("writeNewSubject [1]: New subject is writing {}", subject);

        subject.setId(getNewObjectId(subjectsFilePath));

        write(subject, subjectsFilePath, getAllSubjectFields(subject));

        log.info("writeNewSubject [2]: new subject record is successful {}", subject);
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    private Result<Subject> saveModifySubject(Subject subject) throws CsvValidationException, IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        log.info("saveModifySubject [1] : {}", subject);
        String newFilePath = subjectsFilePath.substring(0, subjectsFilePath.lastIndexOf(".")).concat("new").concat(Constants.CSV_FILE_TYPE);
        File oldFile = new File(subjectsFilePath);
        File newFile = new File(newFilePath);

        FileReader fileReader = new FileReader(subjectsFilePath);
        CSVReader reader = new CSVReader(fileReader);

        String[] strings;
        String subId = subject.getId().toString();
        while ((strings = reader.readNext()) != null) {
            String[] subStrings = strings[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
            if (subStrings[0].contains(subId)) {
                log.info("saveModifySubject [2]: subject has found");
                write(subject, newFilePath, getAllSubjectFields(subject));
            } else {
                write(createSubject(subStrings), newFilePath, getAllSubjectFields(subject));
            }
        }

        log.info("saveModifySubject [3] : New file {} written", newFilePath);

        boolean isDeleted = oldFile.delete();
        log.info("saveModifySubject [4] : Old file {} has deleted: {}", oldFile.getName(), isDeleted);

        boolean isRenamed = newFile.renameTo(oldFile);
        log.info("saveModifySubject [5] : New file {}, isRenamed: {}", newFilePath, isRenamed);

        reader.close();

        log.info("saveModifySubject [6] : subject modification is successful");
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    @Override
    public Result<Subject> getSubjectById(Integer id) {
        log.info("getSubjectById [1]: id = {}", id);
        Result<Subject> result = new Result<>();
        result.setCode(Constants.CODE_NOT_FOUND);

        try {
            FileReader fileReader = new FileReader(subjectsFilePath);
            CSVReader reader = new CSVReader(fileReader);

            for (String[] nextLine : reader) {
                String[] strings = nextLine[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                if (strings[0].equals(String.valueOf(id))) {
                    result.setResult(createSubject(strings));
                    result.setCode(Constants.CODE_ACCESS);
                    break;
                }
            }

            fileReader.close();
            reader.close();

        } catch (Exception e) {
            log.error("getSubjectById [2]: {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
            result.setMessage(e.getMessage());
        }

        log.info("getSubjectById [3]: result {}", result);
        return result;
    }

    @Override
    public boolean isSubjectHasAccess(Integer subjectId, Integer barrierId) {
        log.info("isSubjectHasAccess [1]: subjectId = {}, barrierId = {}", subjectId, barrierId);
        boolean isHasAccess = false;
        try {
            FileReader fileReader = new FileReader(accessBarriersFilePath);
            CSVReader reader = new CSVReader(fileReader);

            for (String[] nextLine : reader) {
                String[] strings = nextLine[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                Long currentTime = getCurrentUtcTimeInMillis();
                log.info("isSubjectHasAccess [2]: currentTime = {}, date = {}", currentTime, strings[3]);
                if (strings[1].equals(String.valueOf(subjectId)) && strings[2].equals(String.valueOf(barrierId))
                        && Long.parseLong(strings[3]) > currentTime) {
                    log.info("isSubjectHasAccess [3]: subject has an access");
                    isHasAccess = true;
                    break;
                }
            }

            fileReader.close();
            reader.close();
        } catch (Exception e) {
            log.error("isSubjectHasAccess [4]: {}", e.getMessage());
        }
        if (!isHasAccess) {
            log.info("isSubjectHasAccess [5] subject has no an access or there is no such a barrier");
        }
        return isHasAccess;
    }

    @Override
    public void saveMotion(Integer subjectId, Integer barrierId, MoveType moveType) {
        log.info("saveMotion [1]: subjectId = {}, barrierId = {}, moveType = {}", subjectId, barrierId, moveType);
        try {
            Motion motion = createMotion(barrierId, moveType);
            motion.setId(getNewObjectId(motionsFilePath));
            Result<String> result = getHistoryIdForMotion(subjectId);
            if (result.getCode() == Constants.CODE_ACCESS) {
                motion.setHistoryId(Integer.parseInt(result.getResult()));
                write(motion, motionsFilePath, getAllObjectFields(motion));
            } else {
                log.info("saveMotion [2]: history cannot be create");
            }
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | CsvValidationException e) {
            log.error("saveMotion [3]: {}", e.getMessage());
        }
    }

    @Override
    public Result<History> createAndSaveHistory(Integer subjectId) {
        log.info("createAndSaveHistory [1]: subjectId = {}", subjectId);
        Result<History> result = new Result<>(null, Constants.CODE_ERROR, null);
        try {
            Integer newHistoryId = getNewObjectId(historyFilePath);
            History history = createHistory(subjectId, newHistoryId);
            write(history, historyFilePath, getAllObjectFields(history));
            result.setCode(Constants.CODE_ACCESS);
            result.setResult(history);
            log.info("createAndSaveHistory [2]: history = {}", history);
        } catch (Exception e) {
            log.error("createAndSaveHistory [3]: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public boolean barrierRegistration(Integer barrierFloor) {
        log.info("barrierRegistration [1]: barrierFloor = {}", barrierFloor);
        Barrier barrier;
        try {
            barrier = createBarrier(barrierFloor);
            barrier.setId(getNewObjectId(barriersFilePath));
            write(barrier, barriersFilePath, getAllObjectFields(barrier));
        } catch (IOException | CsvValidationException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            log.error("barrierRegistration [2]: error = {}", e.getMessage());
            return false;
        }
        log.info("barrierRegistration [3]: barrier created successfully = {}", barrier);
        return true;
    }

    @Override
    public boolean grantAccess(Integer subjectId, Integer barrierId, Long date) {
        log.info("grantAccess [1]: subjectId = {}, barrierId = {}", subjectId, barrierId);
        AccessBarrier accessBarrier;
        try {
            accessBarrier = createAccessBarrier(subjectId, barrierId, date);
            accessBarrier.setId(getNewObjectId(accessBarriersFilePath));
            write(accessBarrier, accessBarriersFilePath, getAllObjectFields(accessBarrier));
        } catch (CsvValidationException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            log.error("grantAccess [2]: error = {}", e.getMessage());
            return false;
        }
        log.info("grantAccess [3]: access granted successfully = {}", accessBarrier);
        return true;
    }

    @Override
    public void openOrCloseBarrier(Integer barrierId, boolean flag) {
        log.info("openOrCloseBarrier [1]: barrierId = {}, isOpen = {}", barrierId, flag);
        try {
            FileReader fileReader = new FileReader(barriersFilePath);
            CSVReader reader = new CSVReader(fileReader);

            for (String[] nextLine : reader) {
                String[] strings = nextLine[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                if (strings[0].equals(String.valueOf(barrierId))) {
                    log.info("openOrCloseBarrier [2]: barrier has found");
                    updateBarrierStatus(barrierId, flag);
                    break;
                }
            }
        } catch (CsvRequiredFieldEmptyException | CsvValidationException | IOException | CsvDataTypeMismatchException e) {
            log.error("openOrCloseBarrier [3]: error = {}", e.getMessage());
        }
    }

    private void updateBarrierStatus(Integer barrierId, boolean flag) throws IOException, CsvValidationException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        log.info("updateBarrierStatus [1] : {}, isOpen = {}", barrierId, flag);
        String newFilePath = barriersFilePath.substring(0, barriersFilePath.lastIndexOf(".")).concat("new").concat(Constants.CSV_FILE_TYPE);
        File oldFile = new File(barriersFilePath);
        File newFile = new File(newFilePath);

        FileReader fileReader = new FileReader(barriersFilePath);
        CSVReader reader = new CSVReader(fileReader);

        String[] strings;
        Barrier barrier;
        String strBarrierId = barrierId.toString();
        while ((strings = reader.readNext()) != null) {
            String[] barStrings = strings[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
            barrier = createBarrier(Integer.valueOf(barStrings[1]));
            barrier.setId(Integer.valueOf(barStrings[0]));
            if (barStrings[0].contains(strBarrierId)) {
                log.info("updateBarrierStatus [2]: barrier has found");
                barrier.setOpen(flag);
                write(barrier, newFilePath, getAllObjectFields(barrier));
            } else {
                barrier.setOpen(Boolean.parseBoolean(barStrings[2]));
                write(barrier, newFilePath, getAllObjectFields(barrier));
            }
        }

        log.info("updateBarrierStatus [3] : New file {} written", newFilePath);

        boolean isDeleted = oldFile.delete();
        log.info("updateBarrierStatus [4] : Old file {} has deleted: {}", oldFile.getName(), isDeleted);

        boolean isRenamed = newFile.renameTo(oldFile);
        log.info("updateBarrierStatus [5] : New file {}, isRenamed: {}", newFilePath, isRenamed);

        reader.close();

        log.info("updateBarrierStatus [6] : barrier modification is successful");
    }

    private Result<String> getHistoryIdForMotion(Integer subjectId) {
        log.info("getHistoryIdForMotion [1]: subjectId = {}", subjectId);
        Result<String> result = new Result<>(null, Constants.CODE_ERROR, null);
        try {
            FileReader fileReader = new FileReader(historyFilePath);
            CSVReader reader = new CSVReader(fileReader);

            String currentUtcTime = getCurrentUtcTimeInMillis().toString();

            for (String[] nextLine : reader) {
                String[] strings = nextLine[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                if (strings[1].equals(String.valueOf(subjectId)) && strings[2].equals(currentUtcTime)) {
                    log.info("getHistoryIdForMotion [2] history has found historyId = {}", strings[0]);
                    result.setCode(Constants.CODE_ACCESS);
                    result.setResult(strings[0]);
                    return result;
                }
            }

            log.info("getHistoryIdForMotion [3]: history not found");

            Result<History> resultHistory = createAndSaveHistory(subjectId);
            if (resultHistory.getCode() == Constants.CODE_ACCESS) {
                result.setCode(Constants.CODE_ACCESS);
                result.setResult(resultHistory.getResult().getId().toString());
            }
            log.info("getHistoryIdForMotion [4]: result = {}", result);
        } catch (Exception e) {
            log.error("getHistoryIdForMotion [5]: {}", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private Subject createSubject(String[] strings) {
        SubjectType subjectType = SubjectType.valueOf(strings[1]);
        Subject result = null;

        switch (subjectType) {
            case ADMIN, USER -> result = createHuman(strings);
            case ANIMAL -> result = createAnimal(strings);
            case TRANSPORT -> result = createTransport(strings);
            case UNDEFINED -> log.info("objectValidation [1]: UNDEFINED subject");
            default -> log.error("objectValidation [2]: error there is no such SubjectType");
        }
        return result;
    }

    private History createHistory(Integer subjectId, Integer historyId) {
        History history = new History();
        history.setDate(getCurrentUtcTimeInMillis());
        history.setSubjectId(subjectId);
        history.setId(historyId);
        return history;
    }

    private Motion createMotion(Integer barrierId, MoveType moveType) {
        Motion motion = new Motion();
        motion.setBarrierId(barrierId);
        motion.seteMoveType(moveType);
        return motion;
    }

    private Subject createTransport(String[] strings) {
        Transport transport = new Transport();
        setCommonFields(transport, strings);
        transport.setNumber(strings[2]);
        transport.setColor(strings[3]);
        return transport;
    }

    private Subject createAnimal(String[] strings) {
        Animal animal = new Animal();
        setCommonFields(animal, strings);
        animal.setNickName(strings[2]);
        animal.setColor(strings[3]);
        return animal;
    }

    private void setCommonFields(Subject subject, String[] strings) {
        subject.setId(Integer.parseInt(strings[0]));
        subject.setType(SubjectType.valueOf(strings[1]));
    }

    private Subject createHuman(String[] strings) {
        Human human = new Human();
        setCommonFields(human, strings);
        human.setPassword(strings[2]);
        human.setLogin(strings[3]);
        human.setName(strings[4]);
        human.setSurname(strings[5]);
        human.setPatronymic(strings[6]);
        human.setEmail(strings[7]);
        return human;
    }

    private Barrier createBarrier(Integer barrierFloor) {
        Barrier barrier = new Barrier();
        barrier.setBarrierFloor(barrierFloor);
        barrier.setOpen(false);
        return barrier;
    }

    private AccessBarrier createAccessBarrier(Integer subjectId, Integer barrierId, Long date) {
        AccessBarrier accessBarrier = new AccessBarrier();
        accessBarrier.setSubjectId(subjectId);
        accessBarrier.setBarrierId(barrierId);
        accessBarrier.setDate(date);
        return accessBarrier;
    }
}
