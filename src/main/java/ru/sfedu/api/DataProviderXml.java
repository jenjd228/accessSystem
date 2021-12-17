package ru.sfedu.api;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.Constants;
import ru.sfedu.model.*;
import ru.sfedu.utils.XmlUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.sfedu.utils.FileUtil.createFileIfNotExists;
import static ru.sfedu.utils.FileUtil.createFolderIfNotExists;
import static ru.sfedu.utils.SubjectUtil.*;
import static ru.sfedu.utils.TImeUtil.getCurrentUtcTimeInMillis;
import static ru.sfedu.utils.TImeUtil.getUtcTimeInMillis;
import static ru.sfedu.utils.XmlUtil.*;

public class DataProviderXml implements IDataProvider {

    private final Logger log = LogManager.getLogger(DataProviderXml.class.getName());

    private final String subjectsFilePath;
    private final String accessBarriersFilePath;
    private final String motionsFilePath;
    private final String historyFilePath;
    private final String barriersFilePath;

    public DataProviderXml() {
        subjectsFilePath = Constants.XML_PATH_FOLDER.concat(Constants.SUBJECT_FILENAME).concat(Constants.XML_FILE_TYPE);
        accessBarriersFilePath = Constants.XML_PATH_FOLDER.concat(Constants.ACCESSIBLE_BARRIERS_FILENAME).concat(Constants.XML_FILE_TYPE);
        motionsFilePath = Constants.XML_PATH_FOLDER.concat(Constants.MOTIONS_FILENAME).concat(Constants.XML_FILE_TYPE);
        historyFilePath = Constants.XML_PATH_FOLDER.concat(Constants.HISTORY_FILENAME).concat(Constants.XML_FILE_TYPE);
        barriersFilePath = Constants.XML_PATH_FOLDER.concat(Constants.BARRIERS_FILENAME).concat(Constants.XML_FILE_TYPE);

        try {
            createFolderIfNotExists(Constants.XML_PATH_FOLDER);
            createCommonFiles();
        } catch (IOException e) {
            log.error("DataProviderXml - initialization error");
        }
    }

    public DataProviderXml(String path) {
        String mainFolder = path.concat(Constants.XML_PATH_FOLDER);
        subjectsFilePath = mainFolder.concat(Constants.SUBJECT_FILENAME).concat(Constants.XML_FILE_TYPE);
        accessBarriersFilePath = mainFolder.concat(Constants.ACCESSIBLE_BARRIERS_FILENAME).concat(Constants.XML_FILE_TYPE);
        motionsFilePath = mainFolder.concat(Constants.MOTIONS_FILENAME).concat(Constants.XML_FILE_TYPE);
        historyFilePath = mainFolder.concat(Constants.HISTORY_FILENAME).concat(Constants.XML_FILE_TYPE);
        barriersFilePath = mainFolder.concat(Constants.BARRIERS_FILENAME).concat(Constants.XML_FILE_TYPE);

        try {
            createFolderIfNotExists(mainFolder);
            createCommonFiles();
        } catch (IOException e) {
            log.error("DataProviderXml - initialization error");
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
        log.info("saveOrUpdateSubject [1]: {}", subject);
        Result<Object> result;

        Result<TreeMap<String, String>> validationResult = objectValidation(subject);
        Result<Subject> saveResult = new Result<>(null, Constants.CODE_ERROR, subject);

        if (validationResult.getCode() != Constants.CODE_ACCESS && !validationResult.getResult().isEmpty()) {
            return new Result<>(validationResult.getMessage(), validationResult.getCode(), new AbstractMap.SimpleEntry<>(saveResult.getResult(), validationResult.getResult()));
        }

        try {
            Result<Subject> oldSubject = getSubjectById(subject.getId());
            if (oldSubject.getCode() == Constants.CODE_ACCESS) {
                log.info("saveOrUpdateSubject [2]: There is the same subject {}", oldSubject);
                result = saveModifySubject(subject);
            } else {
                log.info("saveOrUpdateSubject [3]: There is no the same subject");
                result = writeNewSubject(subject);
            }
        } catch (Exception e) {
            log.error("saveOrUpdateSubject [4]: {}", e.getMessage());
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
            write(barriersFilePath, barrier);
        } catch (Exception e) {
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
                XmlUtil.write(accessBarriersFilePath,accessBarrier);
                result.setCode(Constants.CODE_ACCESS);
            } else {
                result.setCode(Constants.CODE_INVALID_DATA);
                result.setResult(checkResult.getResult());
            }
        } catch (Exception e) {
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

    private boolean openOrCloseBarrier(Integer barrierId, boolean flag) {
        log.info("openOrCloseBarrier [1]: barrierId = {}, isOpen = {}", barrierId, flag);
        AtomicBoolean isAccess = new AtomicBoolean(false);
        try {
            Wrapper<Barrier> wrapper = readFile(barriersFilePath);
            List<Barrier> list = wrapper.getList();

            list.stream().filter(it -> it.getId().equals(barrierId))
                    .findFirst()
                    .ifPresent(it -> {
                        log.info("openOrCloseBarrier [2]: barrier has found");
                        try {
                            updateBarrierStatus(barrierId, flag);
                        } catch (Exception e) {
                            log.error("openOrCloseBarrier [3]: error = {}",e.getMessage());
                        }
                        isAccess.set(true);
                    });
        } catch (Exception e) {
            log.error("openOrCloseBarrier [3]: error = {}", e.getMessage());
        }
        return isAccess.get();
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

    private Result<Barrier> getBarrierById(Integer barrierId) {
        log.info("getBarrierById [1]: id = {}", barrierId);
        Result<Barrier> result = new Result<>();
        result.setCode(Constants.CODE_NOT_FOUND);

        try {
            Wrapper<Barrier> wrapper = readFile(barriersFilePath);
            wrapper.getList().stream().filter(it -> it.getId().equals(barrierId))
                    .findFirst()
                    .ifPresent(it -> {
                        result.setResult(it);
                        result.setCode(Constants.CODE_ACCESS);
                    });
        } catch (Exception e) {
            log.error("getBarrierById [2]: {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
            result.setMessage(e.getMessage());
        }

        log.info("getBarrierById [3]: result {}", result);
        return result;
    }

    private void updateBarrierStatus(Integer barrierId, boolean flag) throws Exception {
        log.info("updateBarrierStatus [1] : {}, isOpen = {}", barrierId, flag);
        Wrapper<Barrier> wrapper = readFile(barriersFilePath);
        List<Barrier> list = wrapper.getList();
        list.stream().filter(it -> it.getId().equals(barrierId))
                .findFirst()
                .ifPresent(it -> {
                    log.info("updateBarrierStatus [2]: barrier has found");
                    it.setOpen(flag);
                    try {
                        XmlUtil.write(barriersFilePath,it);
                    } catch (Exception e) {
                        log.error("updateBarrierStatus [3]: error = {}",e.getMessage());
                    }
                });
        log.info("updateBarrierStatus [6] : barrier modification is successful");
    }

    private Result<Subject> getSubjectById(Integer id) {
        log.info("getSubjectById [1] : id = {}", id);
        Result<Subject> result = new Result<>();
        result.setCode(Constants.CODE_NOT_FOUND);

        try {
            Wrapper subjectWrapper = readFile(subjectsFilePath);
            for (Object object : subjectWrapper.getList()) {
                Subject subject = (Subject) object;
                if (subject.getId().equals(id)) {
                    result.setResult(subject);
                    result.setCode(Constants.CODE_ACCESS);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("getSubjectById: {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
            result.setMessage(e.getMessage());
        }

        log.info("getSubjectById [2] : result {}", result);
        return result;
    }

    private Result<Object> writeNewSubject(Subject subject) {
        log.info("writeNewSubject [1] : New subject is creating");

        try {
            subject.setId(getNewObjectId(subjectsFilePath));
            write(subjectsFilePath, subject);
        } catch (Exception ex) {
            new Result<>(ex.getMessage(), Constants.CODE_ERROR, null);
        }

        log.info("writeNewSubject [3] : New subject has written {}", subject);
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    private Result<Object> saveModifySubject(Subject subject) {
        log.info("saveModifyUser [1] : {}", subject);
        try {
            write(subjectsFilePath, subject);
        } catch (Exception e) {
            log.error("saveModifyUser [3]: {}", e.getMessage());
            return new Result<>(e.getMessage(), Constants.CODE_ERROR, null);
        }

        log.info("saveModifyUser [4]: subject modification is successful");
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    private boolean checkPermission(Integer subjectId, Integer barrierId) {
        log.info("checkPermission [1]: subjectId = {}, barrierId = {}", subjectId, barrierId);
        AtomicBoolean isHasAccess = new AtomicBoolean(false);
        try {

            Wrapper<AccessBarrier> wrapper = readFile(accessBarriersFilePath);
            List<AccessBarrier> list = wrapper.getList();
            Long currentTime = getCurrentUtcTimeInMillis();

            list.stream().filter(it -> it.getSubjectId().equals(subjectId) && it.getBarrierId().equals(barrierId) && it.getDate() > currentTime)
                    .findFirst()
                    .ifPresent(it ->{
                        log.info("checkPermission [3]: subject has an access");
                        isHasAccess.set(true);
                    });
        } catch (Exception e) {
            log.error("checkPermission [4]: {}", e.getMessage());
        }
        if (!isHasAccess.get()) {
            log.info("checkPermission [5] subject has no an access or there is no such a barrier");
        }
        return isHasAccess.get();
    }

    private void motionRegistration(Integer subjectId, Integer barrierId, MoveType moveType) {
        log.info("motionRegistration [1]: subjectId = {}, barrierId = {}, moveType = {}", subjectId, barrierId, moveType);
        try {
            Motion motion = createMotion(barrierId, moveType);
            motion.setId(getNewObjectId(motionsFilePath));
            Result<Integer> result = getHistoryIdForMotion(subjectId);
            if (result.getCode() == Constants.CODE_ACCESS) {
                motion.setHistoryId(result.getResult());
                write(motionsFilePath, motion);
            } else {
                log.info("motionRegistration [2]: history cannot be create");
            }
        } catch (Exception e) {
            log.error("motionRegistration [3]: {}", e.getMessage());
        }
    }

    private Result<Integer> getHistoryIdForMotion(Integer subjectId) {
        log.info("getHistoryIdForMotion [1]: subjectId = {}", subjectId);
        Result<Integer> result = new Result<>(null, Constants.CODE_ERROR, null);
        try {
            try {
                Wrapper wrapper = readFile(historyFilePath);
                List<History> list = wrapper.getList();

                Long currentUtcTime = getCurrentUtcTimeInMillis();

                list.stream().filter(it -> it.getSubjectId().equals(subjectId) && it.getDate().equals(currentUtcTime))
                        .findFirst()
                        .ifPresent(it -> {
                            log.info("getHistoryIdForMotion [2] history has found historyId = {}", it.getId());
                            result.setCode(Constants.CODE_ACCESS);
                            result.setResult(it.getId());
                        });
                if (result.getCode() == Constants.CODE_ACCESS){
                    return result;
                }
            }catch (Exception e){
                log.error("getHistoryIdForMotion [3]: error = {}",e.getMessage());
            }

            Result<History> resultHistory = createAndSaveHistory(subjectId);
            if (resultHistory.getCode() == Constants.CODE_ACCESS) {
                result.setCode(Constants.CODE_ACCESS);
                result.setResult(resultHistory.getResult().getId());
            }
            log.info("getHistoryIdForMotion [4]: result = {}", result);
        } catch (Exception e) {
            log.error("getHistoryIdForMotion [5]: error {}", e.getMessage());
        }
        return result;
    }

    private Result<History> createAndSaveHistory(Integer subjectId) {
        log.info("createAndSaveHistory [1]: subjectId = {}", subjectId);
        Result<History> result = new Result<>(null, Constants.CODE_ERROR, null);
        try {
            Integer newHistoryId = getNewObjectId(historyFilePath);
            History history = createHistory(subjectId, newHistoryId);
            write(historyFilePath,history);
            result.setCode(Constants.CODE_ACCESS);
            result.setResult(history);
            log.info("createAndSaveHistory [2]: history = {}", history);
        } catch (Exception e) {
            log.error("createAndSaveHistory [3]: {}", e.getMessage());
        }
        return result;
    }
}
