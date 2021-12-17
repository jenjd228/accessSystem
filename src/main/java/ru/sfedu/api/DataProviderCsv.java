package ru.sfedu.api;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.Constants;
import ru.sfedu.model.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.TreeMap;

import static ru.sfedu.utils.CsvUtil.getNewObjectId;
import static ru.sfedu.utils.CsvUtil.write;
import static ru.sfedu.utils.FileUtil.createFileIfNotExists;
import static ru.sfedu.utils.FileUtil.createFolderIfNotExists;
import static ru.sfedu.utils.SubjectUtil.*;
import static ru.sfedu.utils.TImeUtil.getCurrentUtcTimeInMillis;
import static ru.sfedu.utils.TImeUtil.getUtcTimeInMillis;

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
    public Result<Object> saveOrUpdateSubject(Subject subject) {
        log.info("saveSubject [1]: {}", subject);

        Result<TreeMap<String, String>> validationResult = objectValidation(subject);
        Result<Subject> saveResult = new Result<>(null, Constants.CODE_ERROR, subject);

        if (validationResult.getCode() != Constants.CODE_ACCESS && !validationResult.getResult().isEmpty()) {
            return new Result<>(validationResult.getMessage(), validationResult.getCode(), new AbstractMap.SimpleEntry<>(saveResult.getResult(), validationResult.getResult()));
        }

        Result<Object> result;
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

    @Override
    public boolean barrierRegistration(Integer barrierFloor) {
        log.info("barrierRegistration [1]: barrierFloor = {}", barrierFloor);
        Barrier barrier;
        try {
            barrier = createBarrier(getNewObjectId(barriersFilePath), barrierFloor, false);
            write(barrier, barriersFilePath, getAllObjectFields(barrier));
        } catch (IOException | CsvValidationException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            log.error("barrierRegistration [2]: error = {}", e.getMessage());
            return false;
        }
        log.info("barrierRegistration [3]: barrier created successfully = {}", barrier);
        return true;
    }

    @Override
    public Result<Object> grantAccess(Integer subjectId, Integer barrierId, Integer year, Integer month, Integer day, Integer hours) {
        log.info("grantAccess [1]: subjectId = {}, barrierId = {}", subjectId, barrierId);
        AccessBarrier accessBarrier;
        Result<Object> result = new Result<>();
        try {
            accessBarrier = createAccessBarrier(getNewObjectId(accessBarriersFilePath), subjectId, barrierId, getUtcTimeInMillis(year, month, day, hours));
            Result<TreeMap<String,String>> checkResult = checkForExistenceSubjectAndBarrier(subjectId,barrierId);
            if (checkResult.getCode() == Constants.CODE_ACCESS) {
                write(accessBarrier, accessBarriersFilePath, getAllObjectFields(accessBarrier));
                result.setCode(Constants.CODE_ACCESS);
            } else {
                result.setCode(Constants.CODE_INVALID_DATA);
                result.setResult(checkResult.getResult());
            }
        } catch (CsvValidationException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            log.error("grantAccess [2]: error = {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
            return result;
        }
        log.info("grantAccess [3]: access granted successfully = {}", accessBarrier);
        return result;
    }

    private Result<TreeMap<String,String>> checkForExistenceSubjectAndBarrier(Integer subjectId, Integer barrierId) {
        Result<Subject> subjectResult = getSubjectById(subjectId);
        Result<Barrier> barrierResult = getBarrierById(barrierId);
        Result<TreeMap<String,String>> result = new Result<>();
        result.setCode(Constants.CODE_ACCESS);
        TreeMap<String,String> errors = new TreeMap<>();

        if (subjectResult.getCode() != Constants.CODE_ACCESS){
            errors.put(Constants.KEY_SUBJECT,Constants.NOT_FOUND_SUBJECT);
            result.setCode(Constants.CODE_NOT_FOUND);
        }

        if (barrierResult.getCode() != Constants.CODE_ACCESS){
            errors.put(Constants.KEY_BARRIER,Constants.NOT_FOUND_BARRIER);
            result.setCode(Constants.CODE_NOT_FOUND);
        }
        result.setResult(errors);
        return result;
    }

    @Override
    public boolean gateAction(Integer subjectId, Integer barrierId, MoveType moveType) {
        motionRegistration(subjectId, barrierId, moveType);
        boolean isSubjectHasAccess = checkPermission(subjectId, barrierId);
        if (isSubjectHasAccess) {
            openOrCloseBarrier(barrierId, true);
            openOrCloseBarrier(barrierId, false);
        }
        return isSubjectHasAccess;
    }

    private Result<Subject> getSubjectById(Integer id) {
        log.info("getSubjectById [1]: id = {}", id);
        Result<Subject> result = new Result<>();
        result.setCode(Constants.CODE_NOT_FOUND);

        try {
            FileReader fileReader = new FileReader(subjectsFilePath);
            CSVReader reader = new CSVReader(fileReader);

            for (String[] nextLine : reader) {
                String[] records = nextLine[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                if (records[0].equals(String.valueOf(id))) {
                    result.setResult(createSubject(records));
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

    private Result<Barrier> getBarrierById(Integer id) {
        log.info("getBarrierById [1]: id = {}", id);
        Result<Barrier> result = new Result<>();
        result.setCode(Constants.CODE_NOT_FOUND);

        try {
            FileReader fileReader = new FileReader(barriersFilePath);
            CSVReader reader = new CSVReader(fileReader);

            for (String[] nextLine : reader) {
                String[] records = nextLine[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                if (records[0].equals(String.valueOf(id))) {
                    result.setResult(createBarrier(Integer.parseInt(records[0]), Integer.parseInt(records[1]), Boolean.parseBoolean(records[2])));
                    result.setCode(Constants.CODE_ACCESS);
                    break;
                }
            }

            fileReader.close();
            reader.close();

        } catch (Exception e) {
            log.error("getBarrierById [2]: {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
            result.setMessage(e.getMessage());
        }

        log.info("getBarrierById [3]: result {}", result);
        return result;
    }

    private Result<Object> writeNewSubject(Subject subject) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, CsvValidationException {
        log.info("writeNewSubject [1]: New subject is writing {}", subject);

        subject.setId(getNewObjectId(subjectsFilePath));

        write(subject, subjectsFilePath, getAllSubjectFields(subject));

        log.info("writeNewSubject [2]: new subject record is successful {}", subject);
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    private Result<Object> saveModifySubject(Subject subject) throws CsvValidationException, IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        log.info("saveModifySubject [1] : {}", subject);
        String newFilePath = subjectsFilePath.substring(0, subjectsFilePath.lastIndexOf(".")).concat("new").concat(Constants.CSV_FILE_TYPE);
        File oldFile = new File(subjectsFilePath);
        File newFile = new File(newFilePath);

        FileReader fileReader = new FileReader(subjectsFilePath);
        CSVReader reader = new CSVReader(fileReader);

        String[] records;
        String subId = subject.getId().toString();
        while ((records = reader.readNext()) != null) {
            String[] subStrings = records[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
            if (subStrings[0].contains(subId)) {
                log.info("saveModifySubject [2]: subject has found");
                write(subject, newFilePath, getAllSubjectFields(subject));
            } else {
                write(createSubject(subStrings), newFilePath, getAllSubjectFields(subject));
            }
        }
        fileReader.close();
        reader.close();

        log.info("saveModifySubject [3] : New file {} written", newFilePath);

        boolean isDeleted = oldFile.delete();
        log.info("saveModifySubject [4] : Old file {} has deleted: {}", oldFile.getName(), isDeleted);

        boolean isRenamed = newFile.renameTo(oldFile);
        log.info("saveModifySubject [5] : New file {}, isRenamed: {}", newFilePath, isRenamed);

        log.info("saveModifySubject [6] : subject modification is successful");
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    private boolean checkPermission(Integer subjectId, Integer barrierId) {
        log.info("checkPermission [1]: subjectId = {}, barrierId = {}", subjectId, barrierId);
        boolean isHasAccess = false;
        try {
            FileReader fileReader = new FileReader(accessBarriersFilePath);
            CSVReader reader = new CSVReader(fileReader);

            for (String[] nextLine : reader) {
                String[] records = nextLine[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                Long currentTime = getCurrentUtcTimeInMillis();
                log.info("checkPermission [2]: currentTime = {}, date = {}", currentTime, records[3]);
                if (records[1].equals(String.valueOf(subjectId)) && records[2].equals(String.valueOf(barrierId))
                        && Long.parseLong(records[3]) > currentTime) {
                    log.info("checkPermission [3]: subject has an access");
                    isHasAccess = true;
                    break;
                }
            }

            fileReader.close();
            reader.close();
        } catch (Exception e) {
            log.error("checkPermission [4]: {}", e.getMessage());
        }
        if (!isHasAccess) {
            log.info("checkPermission [5] subject has no an access or there is no such a barrier");
        }
        return isHasAccess;
    }

    private void motionRegistration(Integer subjectId, Integer barrierId, MoveType moveType) {
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
        } catch (Exception e) {
            log.error("saveMotion [3]: {}", e.getMessage());
        }
    }

    private Result<History> createAndSaveHistory(Integer subjectId) {
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

    private boolean openOrCloseBarrier(Integer barrierId, boolean flag) {
        log.info("openOrCloseBarrier [1]: barrierId = {}, isOpen = {}", barrierId, flag);
        try {
            FileReader fileReader = new FileReader(barriersFilePath);
            CSVReader reader = new CSVReader(fileReader);

            for (String[] nextLine : reader) {
                String[] records = nextLine[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                if (records[0].equals(String.valueOf(barrierId))) {
                    log.info("openOrCloseBarrier [2]: barrier has found");
                    updateBarrierStatus(barrierId, flag);
                    return true;
                }
            }
        } catch (CsvRequiredFieldEmptyException | CsvValidationException | IOException | CsvDataTypeMismatchException e) {
            log.error("openOrCloseBarrier [3]: error = {}", e.getMessage());
        }
        return false;
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
        String records = barrierId.toString();
        while ((strings = reader.readNext()) != null) {
            String[] barStrings = strings[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
            barrier = createBarrier(Integer.valueOf(barStrings[0]), Integer.valueOf(barStrings[1]), false);
            if (barStrings[0].contains(records)) {
                log.info("updateBarrierStatus [2]: barrier has found");
                barrier.setOpen(flag);
            } else {
                barrier.setOpen(Boolean.parseBoolean(barStrings[2]));
            }
            write(barrier, newFilePath, getAllObjectFields(barrier));
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
        }
        return result;
    }

    private Subject createSubject(String[] records) {
        Integer id = Integer.parseInt(records[0]);
        SubjectType subjectType = SubjectType.valueOf(records[1]);
        Subject result = null;

        switch (subjectType) {
            case ADMIN, USER -> result = createHuman(id, subjectType, records[2], records[3], records[4], records[5], records[6], records[7]);
            case ANIMAL -> result = createAnimal(id, records[2], records[3]);
            case TRANSPORT -> result = createTransport(id, records[2], records[3]);
            case UNDEFINED -> log.info("objectValidation [1]: UNDEFINED subject");
            default -> log.error("objectValidation [2]: error there is no such SubjectType");
        }
        return result;
    }
}
