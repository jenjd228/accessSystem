package ru.sfedu.api;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static ru.sfedu.utils.ConfigurationUtil.getConfigurationEntry;
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
    private final String mongoDbName;

    public DataProviderCsv() {
        subjectsFilePath = Constants.CSV_PATH_FOLDER.concat(Constants.SUBJECT_FILENAME).concat(Constants.CSV_FILE_TYPE);
        accessBarriersFilePath = Constants.CSV_PATH_FOLDER.concat(Constants.ACCESSIBLE_BARRIERS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        motionsFilePath = Constants.CSV_PATH_FOLDER.concat(Constants.MOTIONS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        historyFilePath = Constants.CSV_PATH_FOLDER.concat(Constants.HISTORY_FILENAME).concat(Constants.CSV_FILE_TYPE);
        barriersFilePath = Constants.CSV_PATH_FOLDER.concat(Constants.BARRIERS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        mongoDbName = getConfigurationEntry(Constants.MONGO_DB_NAME);

        try {
            createFolderIfNotExists(Constants.CSV_PATH_FOLDER);
            createCommonFiles();
        } catch (IOException e) {
            log.error("Data providerCsv - initialization error");
        }
    }

    public DataProviderCsv(String path, String mongoDbName) {
        String mainFolder = path.concat(Constants.CSV_PATH_FOLDER);
        subjectsFilePath = mainFolder.concat(Constants.SUBJECT_FILENAME).concat(Constants.CSV_FILE_TYPE);
        accessBarriersFilePath = mainFolder.concat(Constants.ACCESSIBLE_BARRIERS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        motionsFilePath = mainFolder.concat(Constants.MOTIONS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        historyFilePath = mainFolder.concat(Constants.HISTORY_FILENAME).concat(Constants.CSV_FILE_TYPE);
        barriersFilePath = mainFolder.concat(Constants.BARRIERS_FILENAME).concat(Constants.CSV_FILE_TYPE);
        this.mongoDbName = mongoDbName;

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
    public Result<Object> subjectRegistration(Subject subject) {
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
                MongoProvider.save(CommandType.UPDATED, RepositoryType.CSV, mongoDbName, oldSubject.getResult());
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
            Result<TreeMap<String, String>> checkResult = checkForExistenceSubjectAndBarrier(subjectId, barrierId);
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

    @Override
    public List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(subjectsFilePath);
            CSVReader reader = new CSVReader(fileReader);

            subjects = reader.readAll()
                    .stream()
                    .map(it -> {
                        String[] records = it[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                        return createSubject(records);
                    }).toList();

            fileReader.close();
            reader.close();

        } catch (Exception e) {
            log.error("getAllUsers [2]: {}", e.getMessage());
        }
        return subjects;
    }

    @Override
    public List<AccessBarrier> getAccessBarriersBySubjectId(Integer subjectId) {
        log.info("getAccessBarriersBySubjectId [1]: subjectId = {}", subjectId);
        List<AccessBarrier> accessBarriers = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(accessBarriersFilePath);
            CSVReader reader = new CSVReader(fileReader);

            accessBarriers = reader.readAll()
                    .stream()
                    .map(it -> {
                        String[] records = it[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                        return createAccessBarrier(Integer.parseInt(records[0]), Integer.parseInt(records[1]), Integer.parseInt(records[2]), Long.parseLong(records[3]));
                    })
                    .filter(it -> it.getSubjectId().equals(subjectId))
                    .toList();

            fileReader.close();
            reader.close();

        } catch (Exception e) {
            log.error("getAllUsers [2]: {}", e.getMessage());
        }
        return accessBarriers;
    }

    @Override
    public List<Barrier> getAllBarriers() {
        List<Barrier> barriers = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(barriersFilePath);
            CSVReader reader = new CSVReader(fileReader);

            barriers = reader.readAll()
                    .stream()
                    .map(it -> {
                        String[] records = it[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                        return createBarrier(Integer.parseInt(records[0]), Integer.parseInt(records[1]), Boolean.parseBoolean(records[2]));
                    })
                    .toList();

            fileReader.close();
            reader.close();

        } catch (Exception e) {
            log.error("getAllUsers [2]: {}", e.getMessage());
        }
        return barriers;
    }

    @Override
    public Result<Subject> deleteSubjectById(Integer subjectId) {
        log.info("deleteSubjectById [1]: subjectId = {}", subjectId);
        Result<Subject> result = new Result<>(null, Constants.CODE_NOT_FOUND, null);
        String newFilePath = subjectsFilePath.substring(0, subjectsFilePath.lastIndexOf(".")).concat("new").concat(Constants.CSV_FILE_TYPE);
        File oldFile = new File(subjectsFilePath);
        File newFile = new File(newFilePath);
        try {
            FileReader fileReader = new FileReader(subjectsFilePath);
            CSVReader reader = new CSVReader(fileReader);

            reader.readAll().stream()
                    .map(it -> {
                        String[] records = it[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                        return createSubject(records);
                    })
                    .filter(it -> {
                        if (!it.getId().equals(subjectId)) {
                            return true;
                        } else {
                            result.setResult(it);
                            result.setCode(Constants.CODE_ACCESS);
                            deleteAccessBarriersBySubjectId(subjectId);
                            MongoProvider.save(CommandType.DELETED, RepositoryType.CSV, mongoDbName, it);
                            return false;
                        }
                    })
                    .forEach(it -> {
                        try {
                            write(it, newFilePath, getAllSubjectFields(it));
                        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
                            log.error("deleteSubjectById [2]: error {} ", e.getMessage());
                        }
                    });

            fileReader.close();
            reader.close();

        } catch (Exception e) {
            log.error("getSubjectById [3]: error {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
            result.setMessage(e.getMessage());
        }

        log.info("saveModifySubject [4] : New file {} written", newFilePath);

        boolean isDeleted = oldFile.delete();
        log.info("saveModifySubject [5] : Old file {} has deleted: {}", oldFile.getName(), isDeleted);

        boolean isRenamed = newFile.renameTo(oldFile);
        log.info("saveModifySubject [6] : New file {}, isRenamed: {}", newFilePath, isRenamed);

        log.info("saveModifySubject [7] : subject modification is successful");
        return result;
    }

    @Override
    public Result<AccessBarrier> deleteAccessBarrierBySubjectAndBarrierId(Integer subjectId, Integer barrierId) {
        log.info("deleteAccessBarrierBySubjectAndBarrierId [1]: subjectId = {},barrierId = {}", subjectId, barrierId);
        String newFilePath = accessBarriersFilePath.substring(0, accessBarriersFilePath.lastIndexOf(".")).concat("new").concat(Constants.CSV_FILE_TYPE);
        Result<AccessBarrier> accessBarrierResult = new Result<>(null, Constants.CODE_NOT_FOUND, null);
        File oldFile = new File(accessBarriersFilePath);
        File newFile = new File(newFilePath);
        try {
            FileReader fileReader = new FileReader(accessBarriersFilePath);
            CSVReader reader = new CSVReader(fileReader);

            reader.readAll().stream()
                    .map(it -> {
                        String[] records = it[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                        return createAccessBarrier(Integer.parseInt(records[0]), Integer.parseInt(records[1]), Integer.parseInt(records[2]), Long.parseLong(records[3]));
                    })
                    .filter(it -> {
                        if (it.getSubjectId().equals(subjectId) && it.getBarrierId().equals(barrierId)) {
                            log.info("deleteAccessBarrierBySubjectAndBarrierId [2]: deleted barrier = {}", it);
                            accessBarrierResult.setCode(Constants.CODE_ACCESS);
                            accessBarrierResult.setResult(it);
                            MongoProvider.save(CommandType.DELETED, RepositoryType.CSV, mongoDbName, it);
                            return false;
                        } else {
                            return true;
                        }
                    })
                    .forEach(it -> {
                        try {
                            write(it, newFilePath, getAllObjectFields(it));
                        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
                            log.error("deleteAccessBarrierBySubjectAndBarrierId [3]: error {} ", e.getMessage());
                        }
                    });

            fileReader.close();
            reader.close();

        } catch (Exception e) {
            log.error("deleteAccessBarrierBySubjectAndBarrierId [4]: error {}", e.getMessage());
        }

        log.info("deleteAccessBarrierBySubjectAndBarrierId [5] : New file {} written", newFilePath);

        boolean isDeleted = oldFile.delete();
        log.info("deleteAccessBarrierBySubjectAndBarrierId [6] : Old file {} has deleted: {}", oldFile.getName(), isDeleted);

        boolean isRenamed = newFile.renameTo(oldFile);
        log.info("deleteAccessBarrierBySubjectAndBarrierId [7] : New file {}, isRenamed: {}", newFilePath, isRenamed);

        log.info("deleteAccessBarrierBySubjectAndBarrierId [8] : subject modification is successful");
        return accessBarrierResult;
    }

    @Override
    public Result<TreeMap<History, List<Motion>>> getSubjectHistoryBySubjectId(Integer subjectId) {
        Result<TreeMap<History, List<Motion>>> result = new Result<>();
        TreeMap<History, List<Motion>> listTreeMap = new TreeMap<>();
        List<History> histories = getAllSubjectHistories(subjectId);
        histories.forEach(history -> {
            List<Motion> motions = getMotionByHistoryId(history.getId());
            listTreeMap.put(history, motions);
        });
        if (listTreeMap.isEmpty()) {
            result.setCode(Constants.CODE_NOT_FOUND);
        } else {
            result.setCode(Constants.CODE_ACCESS);
            result.setResult(listTreeMap);
        }
        return result;
    }

    private List<Motion> getMotionByHistoryId(Integer historyId) {
        log.info("getMotionBySubjectId [1]: historyId = {}", historyId);
        try {
            FileReader fileReader = new FileReader(motionsFilePath);
            CSVReader reader = new CSVReader(fileReader);
            List<Motion> motions = reader.readAll().stream()
                    .map(it -> {
                        String[] records = it[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                        return createMotion(Integer.parseInt(records[0]), Integer.parseInt(records[2]), Integer.parseInt(records[1]), MoveType.valueOf(records[3]));
                    })
                    .filter(it -> it.getHistoryId().equals(historyId)).toList();
            return motions;
        } catch (IOException | CsvException e) {
            log.error("getMotionBySubjectId [2]: error = {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private List<History> getAllSubjectHistories(Integer subjectId) {
        log.info("getAllSubjectHistories [1]: subjectId = {}", subjectId);
        try {
            FileReader fileReader = new FileReader(historyFilePath);
            CSVReader reader = new CSVReader(fileReader);
            List<History> histories = reader.readAll().stream()
                    .map(it -> {
                        String[] records = it[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                        return createHistory(Integer.parseInt(records[0]), Integer.parseInt(records[1]), Long.parseLong(records[2]));
                    })
                    .filter(it -> it.getSubjectId().equals(subjectId)).toList();
            return histories;
        } catch (IOException | CsvException e) {
            log.error("getAllSubjectHistories [2]: error = {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private void deleteAccessBarriersBySubjectId(Integer subjectId) {
        log.info("deleteAccessBarrierBySubjectId [1]: subjectId = {}", subjectId);
        String newFilePath = accessBarriersFilePath.substring(0, accessBarriersFilePath.lastIndexOf(".")).concat("new").concat(Constants.CSV_FILE_TYPE);
        File oldFile = new File(accessBarriersFilePath);
        File newFile = new File(newFilePath);
        try {
            FileReader fileReader = new FileReader(accessBarriersFilePath);
            CSVReader reader = new CSVReader(fileReader);

            reader.readAll().stream()
                    .map(it -> {
                        String[] records = it[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                        return createAccessBarrier(Integer.parseInt(records[0]), Integer.parseInt(records[1]), Integer.parseInt(records[2]), Long.parseLong(records[3]));
                    })
                    .filter(it -> {
                        if (!it.getSubjectId().equals(subjectId)) {
                            return true;
                        } else {
                            log.info("deleteAccessBarrierBySubjectId [2]: deleted barrier = {}", it);
                            MongoProvider.save(CommandType.DELETED, RepositoryType.CSV, mongoDbName, it);
                            return false;
                        }
                    })
                    .forEach(it -> {
                        try {
                            write(it, newFilePath, getAllObjectFields(it));
                        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
                            log.error("deleteAccessBarrierBySubjectId [3]: error {} ", e.getMessage());
                        }
                    });

            fileReader.close();
            reader.close();

        } catch (Exception e) {
            log.error("deleteAccessBarrierBySubjectId [4]: error {}", e.getMessage());
        }

        log.info("deleteAccessBarrierBySubjectId [5] : New file {} written", newFilePath);

        boolean isDeleted = oldFile.delete();
        log.info("deleteAccessBarrierBySubjectId [6] : Old file {} has deleted: {}", oldFile.getName(), isDeleted);

        boolean isRenamed = newFile.renameTo(oldFile);
        log.info("deleteAccessBarrierBySubjectId [7] : New file {}, isRenamed: {}", newFilePath, isRenamed);

        log.info("deleteAccessBarrierBySubjectId [8] : subject modification is successful");
    }

    private Result<TreeMap<String, String>> checkForExistenceSubjectAndBarrier(Integer subjectId, Integer barrierId) {
        Result<Subject> subjectResult = getSubjectById(subjectId);
        Result<Barrier> barrierResult = getBarrierById(barrierId);
        Result<TreeMap<String, String>> result = new Result<>();
        result.setCode(Constants.CODE_ACCESS);
        TreeMap<String, String> errors = new TreeMap<>();

        if (subjectResult.getCode() != Constants.CODE_ACCESS) {
            errors.put(Constants.KEY_SUBJECT, Constants.NOT_FOUND_SUBJECT);
            result.setCode(Constants.CODE_NOT_FOUND);
        }

        if (barrierResult.getCode() != Constants.CODE_ACCESS) {
            errors.put(Constants.KEY_BARRIER, Constants.NOT_FOUND_BARRIER);
            result.setCode(Constants.CODE_NOT_FOUND);
        }
        result.setResult(errors);
        return result;
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
            log.error("getBarrierById [2]: error {}", e.getMessage());
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
            log.error("checkPermission [4]: error {}", e.getMessage());
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
            log.error("saveMotion [3]: error {}", e.getMessage());
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

    private void openOrCloseBarrier(Integer barrierId, boolean flag) {
        log.info("openOrCloseBarrier [1]: barrierId = {}, isOpen = {}", barrierId, flag);
        try {
            FileReader fileReader = new FileReader(barriersFilePath);
            CSVReader reader = new CSVReader(fileReader);

            for (String[] nextLine : reader) {
                String[] records = nextLine[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
                if (records[0].equals(String.valueOf(barrierId))) {
                    log.info("openOrCloseBarrier [2]: barrier has found");
                    updateBarrierStatus(barrierId, flag);
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
        String records = barrierId.toString();
        while ((strings = reader.readNext()) != null) {
            String[] barStrings = strings[0].split(String.valueOf(Constants.CSV_DEFAULT_SEPARATOR));
            barrier = createBarrier(Integer.valueOf(barStrings[0]), Integer.valueOf(barStrings[1]), false);
            if (barStrings[0].contains(records)) {
                log.info("updateBarrierStatus [2]: barrier has found");
                MongoProvider.save(CommandType.UPDATED, RepositoryType.CSV, mongoDbName, barrier);
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
            log.error("getHistoryIdForMotion [5]: error {}", e.getMessage());
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
