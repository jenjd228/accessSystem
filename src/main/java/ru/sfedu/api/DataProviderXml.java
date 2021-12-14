package ru.sfedu.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.sfedu.model.*;
import ru.sfedu.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.sfedu.utils.FileUtil.createFileIfNotExists;
import static ru.sfedu.utils.FileUtil.createFolderIfNotExists;

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
            createFileIfNotExists(subjectsFilePath);
            createFileIfNotExists(accessBarriersFilePath);
            createFileIfNotExists(motionsFilePath);
            createFileIfNotExists(historyFilePath);
            createFileIfNotExists(barriersFilePath);
        } catch (IOException e) {
            log.error("DataProviderXml - initialization error");
        }
    }

    @Override
    public Result<Subject> saveOrUpdateSubject(Subject subject) {
        log.info("saveOrUpdateSubject [1]: {}", subject);
        Result<Subject> result;
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
    public Result<Subject> getSubjectById(Integer id) {
        log.info("getSubjectById [1] : id = {}", id);
        Result<Subject> result = new Result<>();
        result.setCode(Constants.CODE_NOT_FOUND);

        try {
            Serializer serializer = new Persister();
            File file = new File(subjectsFilePath);
            Wrapper subjectWrapper = serializer.read(Wrapper.class, file);
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

    private Result<Subject> writeNewSubject(Subject subject) {
        log.info("writeNewSubject [1] : New subject is creating");

        Serializer serializer = new Persister();
        File file = new File(subjectsFilePath);

        Wrapper wrapper;
        try {
            wrapper = serializer.read(Wrapper.class, file);
            subject.setId(getNewSubjectId(wrapper.getList()));
            wrapper.addToList(subject);
        } catch (Exception e) {
            wrapper = new Wrapper();
            List list = new ArrayList();
            subject.setId(getNewSubjectId(list));
            list.add(subject);
            wrapper.setList(list);
        }

        try {
            serializer.write(wrapper, file);
        } catch (Exception ex) {
            new Result<>(ex.getMessage(), Constants.CODE_ERROR, null);
        }

        log.info("writeNewSubject [3] : New subject has written {}", subject);
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    private Result<Subject> saveModifySubject(Subject subject) {
        log.info("saveModifyUser [1] : {}", subject);
        File file = new File(subjectsFilePath);

        Serializer serializer = new Persister();

        try {
            Wrapper wrapper = serializer.read(Wrapper.class, file);
            List list = wrapper.getList();

            for (int i = 0; i < list.size(); i++) {
                Subject myObject = (Subject) list.get(i);
                if (myObject.getId().equals(subject.getId())) {
                    log.info("saveModifyUser [2]: subject has found");
                    list.set(i, subject);
                    break;
                }
            }

            wrapper.setList(list);

            serializer.write(wrapper, file);

        } catch (Exception e) {
            log.error("saveModifyUser [3]: {}", e.getMessage());
            return new Result<>(e.getMessage(), Constants.CODE_ERROR, null);
        }

        log.info("saveModifyUser [4]: subject modification is successful");
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    private Integer getNewSubjectId(List list) {
        if (list.isEmpty()) {
            return 1;
        }
        return ((Subject) list.get(list.size() - 1)).getId() + 1;
    }

    @Override
    public boolean isSubjectHasAccess(Integer subjectId, Integer barrierId) {
        return false;
    }

    @Override
    public void saveMotion(Integer subjectId, Integer barrierId, MoveType moveType) {

    }

    @Override
    public Result<History> createAndSaveHistory(Integer subjectId) {
        return null;
    }

    @Override
    public boolean barrierRegistration(Integer barrierFloor) {
        return false;
    }

    @Override
    public boolean grantAccess(Integer subjectId, Integer barrierId, Long date) {
        return false;
    }

    @Override
    public void openOrCloseBarrier(Integer barrierId, boolean flag) {

    }
}
