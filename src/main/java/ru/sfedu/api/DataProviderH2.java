package ru.sfedu.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.Constants;
import ru.sfedu.model.*;

import java.sql.*;
import java.util.*;

import static ru.sfedu.utils.ConfigurationUtil.getConfigurationEntry;
import static ru.sfedu.utils.SubjectUtil.*;
import static ru.sfedu.utils.TImeUtil.getCurrentUtcTimeInMillis;
import static ru.sfedu.utils.TImeUtil.getUtcTimeInMillis;

public class DataProviderH2 implements IDataProvider {

    private final Logger log = LogManager.getLogger(DataProviderH2.class.getName());

    private String h2PathFolder = "./";
    private final String mongoDbName;

    public DataProviderH2() {
        h2PathFolder = h2PathFolder.concat(Constants.H2_PATH_FOLDER);
        mongoDbName = getConfigurationEntry(Constants.MONGO_DB_NAME);
        createTables();
    }

    public DataProviderH2(String path, String mongoDbName) {
        h2PathFolder = h2PathFolder.concat(path).concat(Constants.H2_PATH_FOLDER);
        this.mongoDbName = mongoDbName;
        createTables();
    }

    private void createTables() {
        try {
            Connection connection = connection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(Constants.SQL_TABLE_CREATE_SUBJECT);
            statement.executeUpdate(Constants.SQL_TABLE_CREATE_MOTION);
            statement.executeUpdate(Constants.SQL_TABLE_CREATE_ACCESS_BARRIER);
            statement.executeUpdate(Constants.SQL_TABLE_CREATE_BARRIER);
            statement.executeUpdate(Constants.SQL_TABLE_CREATE_HISTORY);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            log.error("DataProviderH2 - initialization error {}", e.getMessage());
        }
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(
                getConfigurationEntry(Constants.H2_CONNECTOR).concat(h2PathFolder).concat(Constants.H2_DB_NAME),
                getConfigurationEntry(Constants.H2_LOGIN),
                getConfigurationEntry(Constants.H2_PASSWORD));
    }

    @Override
    public Result<Object> subjectRegistration(Subject subject) {
        log.debug("saveOrUpdateSubject [1]: {}", subject);
        Result<Object> result;

        Result<TreeMap<String, String>> validationResult = objectValidation(subject);
        Result<Subject> saveResult = new Result<>(null, Constants.CODE_ERROR, subject);

        if (validationResult.getCode() != Constants.CODE_ACCESS && !validationResult.getResult().isEmpty()) {
            return new Result<>(validationResult.getMessage(), validationResult.getCode(), new AbstractMap.SimpleEntry<>(saveResult.getResult(), validationResult.getResult()));
        }

        try {
            Result<Subject> oldSubject = getSubjectById(subject.getId());
            if (oldSubject.getCode() == Constants.CODE_ACCESS) {
                log.debug("saveOrUpdateSubject [2]: There is the same subject {}", oldSubject);
                MongoProvider.save(CommandType.UPDATED, RepositoryType.H2, mongoDbName, oldSubject.getResult());
                result = saveModifySubject(subject);
            } else {
                log.debug("saveOrUpdateSubject [3]: There is no the same subject");
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
        log.debug("barrierRegistration [1]: barrierFloor = {}", barrierFloor);
        Barrier barrier;
        try {
            barrier = createBarrier(null, barrierFloor, false);
            Connection connection = connection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(String.format(Constants.INSERT_BARRIER, barrier.getBarrierFloor(), barrier.isOpen()));
            closeStatementAndConnection(connection, statement);
        } catch (SQLException e) {
            log.error("barrierRegistration [2]: error = {}", e.getMessage());
            return false;
        }
        log.debug("barrierRegistration [3]: barrier created successfully = {}", barrier);
        return true;
    }

    @Override
    public Result<Object> grantAccess(Integer subjectId, Integer barrierId, Integer year, Integer month, Integer day, Integer hours) {
        log.debug("grantAccess [1]: subjectId = {}, barrierId = {}", subjectId, barrierId);
        AccessBarrier accessBarrier = null;
        Result<Object> result = new Result<>();
        Connection connection = null;
        Statement statement = null;
        Result<TreeMap<String, String>> checkResult = checkForExistenceSubjectAndBarrier(subjectId, barrierId);
        try {
            if (checkResult.getCode() == Constants.CODE_ACCESS) {
                List<AccessBarrier> accessBarriers = getAccessBarriersBySubjectId(subjectId);
                Optional<AccessBarrier> optionalAccessBarrier = accessBarriers.stream().filter(it -> it.getBarrierId().equals(barrierId) && it.getSubjectId().equals(subjectId)).findFirst();
                if (optionalAccessBarrier.isPresent()) {
                    updateSubjectAccess(subjectId, barrierId, getUtcTimeInMillis(year, month, day, hours));
                } else {
                    accessBarrier = createAccessBarrier(null, subjectId, barrierId, getUtcTimeInMillis(year, month, day, hours));
                    connection = connection();
                    statement = connection.createStatement();
                    statement.executeUpdate(String.format(Constants.INSERT_ACCESS_BARRIER, accessBarrier.getSubjectId(), accessBarrier.getBarrierId(), accessBarrier.getDate()));
                }
                result.setCode(Constants.CODE_ACCESS);
            } else {
                result.setCode(Constants.CODE_INVALID_DATA);
                result.setResult(checkResult.getResult());
            }
        } catch (Exception e) {
            log.error("grantAccess [2]: error = {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
            return result;
        } finally {
            closeStatementAndConnection(connection, statement);
        }
        log.debug("grantAccess [3]: access granted successfully");
        return result;
    }

    private void updateSubjectAccess(Integer subjectId, Integer barrierId, Long date) {
        log.debug("updateSubjectAccess [1]: subjectId = {}", subjectId);
        Connection connection = null;
        Statement statement = null;
        try {
            connection = connection();
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_ACCESS_BARRIER_BY_SUBJECT_AND_BARRIER_ID, subjectId, barrierId));
            if (resultSet.next()) {
                AccessBarrier accessBarrier = createAccessBarrier(resultSet.getInt(Constants.KEY_ID), resultSet.getInt(Constants.KEY_SUBJECT_ID), resultSet.getInt(Constants.KEY_BARRIER_ID), resultSet.getLong(Constants.KEY_DATE));
                statement.executeUpdate(String.format(Constants.UPDATE_ACCESS_SUBJECT_DATE_BY_SUBJECT_AND_BARRIER_ID, date, subjectId, barrierId));
                MongoProvider.save(CommandType.UPDATED, RepositoryType.H2, mongoDbName, accessBarrier);
            }
        } catch (Exception e) {
            log.error("updateSubjectAccess [2]: error = {}", e.getMessage());
        } finally {
            closeStatementAndConnection(connection, statement);
        }
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
            Connection connection = connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(Constants.SELECT_ALL_FROM_SUBJECT);
            while (resultSet.next()) {
                subjects.add(createSubject(resultSet));
            }
            closeStatementAndConnection(connection, statement);
        } catch (SQLException e) {
            log.error("getAllUsers [1]: error = {}", e.getMessage());
        }
        return subjects;
    }

    @Override
    public List<AccessBarrier> getAccessBarriersBySubjectId(Integer subjectId) {
        List<AccessBarrier> accessBarriers = new ArrayList<>();
        try {
            Connection connection = connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_ACCESS_BARRIER_BY_SUBJECT_ID, subjectId));
            while (resultSet.next()) {
                accessBarriers.add(createAccessBarrier(resultSet.getInt(Constants.KEY_ID), resultSet.getInt(Constants.KEY_SUBJECT_ID), resultSet.getInt(Constants.KEY_BARRIER_ID), resultSet.getLong(Constants.KEY_DATE)));
            }
            closeStatementAndConnection(connection, statement);
        } catch (SQLException e) {
            log.error("getAccessBarriersBySubjectId [1]: error = {}", e.getMessage());
        }
        return accessBarriers;
    }

    @Override
    public List<Barrier> getAllBarriers() {
        List<Barrier> accessBarriers = new ArrayList<>();
        try {
            Connection connection = connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(Constants.SELECT_ALL_FROM_BARRIER);
            while (resultSet.next()) {
                accessBarriers.add(createBarrier(resultSet.getInt(Constants.KEY_ID), resultSet.getInt(Constants.KEY_BARRIER_FLOOR), resultSet.getBoolean(Constants.KEY_IS_OPEN)));
            }
            closeStatementAndConnection(connection, statement);
        } catch (SQLException e) {
            log.error("getAllBarriers [1]: error = {}", e.getMessage());
        }
        return accessBarriers;
    }

    @Override
    public Result<Subject> deleteSubjectById(Integer subjectId) {
        log.debug("deleteSubjectById[1]: subjectId = {}", subjectId);
        Result<Subject> result = new Result<>(null, Constants.CODE_NOT_FOUND, null);
        try {
            Result<Subject> subjectResult = getSubjectById(subjectId);
            if (subjectResult.getCode() == Constants.CODE_ACCESS) {
                log.debug("deleteSubjectById [2]: subject has found = {}", subjectResult.getResult());
                result = subjectResult;
                Connection connection = connection();
                Statement statement = connection.createStatement();
                statement.executeUpdate(String.format(Constants.DELETE_SUBJECT_BY_ID, subjectId));
                deleteAccessBarriersBySubjectId(subjectId);
                MongoProvider.save(CommandType.DELETED, RepositoryType.H2, mongoDbName, result.getResult());
                closeStatementAndConnection(connection, statement);
                log.debug("deleteSubjectById [3]: subject deleted");
            }
        } catch (Exception e) {
            log.error("deleteSubjectById[4]: error = {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
        }
        return result;
    }

    @Override
    public Result<AccessBarrier> deleteAccessBarrierBySubjectAndBarrierId(Integer subjectId, Integer barrierId) {
        log.debug("deleteAccessBarrierBySubjectAndBarrierId[1]: subjectId = {}", subjectId);
        Result<AccessBarrier> result = new Result<>(null, Constants.CODE_NOT_FOUND, null);
        try {
            Connection connection = connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_ACCESS_BARRIER_BY_SUBJECT_AND_BARRIER_ID, subjectId, barrierId));
            if (resultSet.next()) {
                AccessBarrier accessBarrier = createAccessBarrier(resultSet.getInt(Constants.KEY_ID), resultSet.getInt(Constants.KEY_SUBJECT_ID), resultSet.getInt(Constants.KEY_BARRIER_ID), resultSet.getLong(Constants.KEY_DATE));
                log.debug("deleteAccessBarrierBySubjectAndBarrierId[2]: accessBarriers has found");
                result.setCode(Constants.CODE_ACCESS);
                result.setResult(accessBarrier);
                statement.executeUpdate(String.format(Constants.DELETE_ACCESS_BARRIERS_BY_ID, accessBarrier.getId()));
                MongoProvider.save(CommandType.DELETED, RepositoryType.H2, mongoDbName, accessBarrier);
                log.debug("deleteAccessBarrierBySubjectAndBarrierId[3]: access barrier deleted");
            }
            closeStatementAndConnection(connection, statement);
        } catch (Exception e) {
            log.error("deleteAccessBarrierBySubjectAndBarrierId[4]: error = {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
        }
        return result;
    }

    @Override
    public Result<Barrier> deleteBarrierById(Integer barrierId) {
        log.debug("deleteBarrierById[1]: barrierId = {}", barrierId);
        Result<Barrier> result = new Result<>(null, Constants.CODE_NOT_FOUND, null);
        try {
            Connection connection = connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_BARRIER_BY_ID, barrierId));
            if (resultSet.next()) {
                result.setCode(Constants.CODE_ACCESS);
                Barrier barrier = createBarrier(resultSet.getInt(Constants.KEY_ID), resultSet.getInt(Constants.KEY_BARRIER_FLOOR), resultSet.getBoolean(Constants.KEY_IS_OPEN));
                result.setResult(barrier);
                statement.executeUpdate(String.format(Constants.DELETE_BARRIER_BY_BARRIER_ID, barrierId));
                MongoProvider.save(CommandType.DELETED, RepositoryType.H2, mongoDbName, barrier);
            }
            closeStatementAndConnection(connection, statement);
        } catch (Exception e) {
            log.error("deleteBarrierById[5]: error = {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
        }
        return result;
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
        log.debug("getMotionBySubjectId [1]: historyId = {}", historyId);
        try {
            List<Motion> motions = new ArrayList<>();
            Connection connection = connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_MOTION_BY_HISTORY_ID, historyId));
            while (resultSet.next()) {
                if (resultSet.getInt(Constants.KEY_HISTORY_ID) == historyId) {
                    motions.add(createMotion(resultSet.getInt(Constants.KEY_ID), resultSet.getInt(Constants.KEY_HISTORY_ID), resultSet.getInt(Constants.KEY_BARRIER_ID), MoveType.valueOf(resultSet.getString(Constants.KEY_MOVE_TYPE))));
                }
            }
            closeStatementAndConnection(connection, statement);
            return motions;
        } catch (SQLException e) {
            log.error("getMotionBySubjectId [2]: error = {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private List<History> getAllSubjectHistories(Integer subjectId) {
        log.debug("getAllSubjectHistories [1]: subjectId = {}", subjectId);
        try {
            List<History> histories = new ArrayList<>();
            Connection connection = connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_HISTORY_BY_SUBJECT_ID, subjectId));
            while (resultSet.next()) {
                if (resultSet.getInt(Constants.KEY_SUBJECT_ID) == subjectId) {
                    histories.add(createHistory(resultSet.getInt(Constants.KEY_ID), resultSet.getInt(Constants.KEY_SUBJECT_ID), resultSet.getLong(Constants.KEY_DATE)));
                }
            }
            closeStatementAndConnection(connection, statement);
            return histories;
        } catch (Exception e) {
            log.error("getAllSubjectHistories [2]: error = {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private void deleteAccessBarriersBySubjectId(Integer subjectId) {
        log.debug("deleteAccessBarrierBySubjectId [1]: subjectId = {}", subjectId);
        try {
            Connection connection = connection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_ACCESS_BARRIER_BY_SUBJECT_ID, subjectId));
            while (resultSet.next()) {
                statement.executeUpdate(String.format(Constants.DELETE_ACCESS_BARRIERS_BY_ID, resultSet.getInt(Constants.KEY_ID)));
                MongoProvider.save(CommandType.DELETED, RepositoryType.H2, mongoDbName, createAccessBarrier(resultSet.getInt(Constants.KEY_ID), subjectId, resultSet.getInt(Constants.KEY_BARRIER_ID), resultSet.getLong(Constants.KEY_DATE)));
            }
            closeStatementAndConnection(connection, statement);
        } catch (Exception e) {
            log.error("deleteAccessBarrierBySubjectId [4]: error {}", e.getMessage());
        }
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

    private void openOrCloseBarrier(Integer barrierId, boolean flag) {
        log.debug("openOrCloseBarrier [1]: barrierId = {}, isOpen = {}", barrierId, flag);
        try {
            Connection connection = connection();
            Statement statement = connection.createStatement();
            saveOldBarrierInMongo(barrierId);
            int rowsUpdates = statement.executeUpdate(String.format(Constants.UPDATE_BARRIER_IS_OPEN_BY_ID, flag, barrierId));
            if (rowsUpdates != 0) {
                log.debug("openOrCloseBarrier [2]: barrier has updated");
            } else {
                log.debug("openOrCloseBarrier [2]: barrier not found");
            }
            closeStatementAndConnection(connection, statement);
        } catch (SQLException e) {
            log.error("openOrCloseBarrier [3]: error = {}", e.getMessage());
        }
    }

    private void saveOldBarrierInMongo(Integer barrierId) {
        Result<Barrier> result = getBarrierById(barrierId);
        if (result.getCode() == Constants.CODE_ACCESS) {
            MongoProvider.save(CommandType.UPDATED, RepositoryType.H2, mongoDbName, result.getResult());
            log.debug("saveOldBarrierInMongo [1]: barrier has saved");
        } else {
            log.debug("saveOldBarrierInMongo [2]: barrier has no saved");
        }
    }

    private boolean checkPermission(Integer subjectId, Integer barrierId) {
        log.debug("checkPermission [1]: subjectId = {}, barrierId = {}", subjectId, barrierId);
        boolean isHasAccess = false;
        try {
            Connection connection = connection();
            Statement statement = connection.createStatement();

            Long currentTime = getCurrentUtcTimeInMillis();
            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_ACCESS_BARRIER_IF_HAS_PERMISSION, subjectId, barrierId, currentTime));

            if (resultSet.next()) {
                log.debug("checkPermission [3]: subject has an access");
                isHasAccess = true;
            } else {
                log.debug("checkPermission [5] subject has no an access or there is no such a barrier");
            }
            closeStatementAndConnection(connection, statement);
        } catch (Exception e) {
            log.error("checkPermission [4]: {}", e.getMessage());
        }
        return isHasAccess;
    }

    private void motionRegistration(Integer subjectId, Integer barrierId, MoveType moveType) {
        log.debug("saveMotion [1]: subjectId = {}, barrierId = {}, moveType = {}", subjectId, barrierId, moveType);
        try {
            Motion motion = createMotion(barrierId, moveType);
            Result<String> result = getHistoryIdForMotion(subjectId);
            if (result.getCode() == Constants.CODE_ACCESS) {
                motion.setHistoryId(Integer.parseInt(result.getResult()));
                Connection connection = connection();
                Statement statement = connection.createStatement();
                statement.executeUpdate(String.format(Constants.INSERT_MOTION, motion.getBarrierId(), motion.getHistoryId(), motion.getMoveType()));
                closeStatementAndConnection(connection, statement);
            } else {
                log.debug("saveMotion [2]: history cannot be create");
            }
        } catch (Exception e) {
            log.error("saveMotion [3]: {}", e.getMessage());
        }
    }

    private Result<String> getHistoryIdForMotion(Integer subjectId) {
        log.debug("getHistoryIdForMotion [1]: subjectId = {}", subjectId);
        Result<String> result = new Result<>(null, Constants.CODE_ERROR, null);
        Connection connection = null;
        Statement statement = null;
        try {
            connection = connection();
            statement = connection.createStatement();

            Long currentUtcTime = getCurrentUtcTimeInMillis();
            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_HISTORY_BY_DATE_AND_SUBJECT_ID, currentUtcTime, subjectId));

            if (resultSet.next()) {
                String id = resultSet.getString(Constants.KEY_ID);
                log.debug("getHistoryIdForMotion [2] history has found historyId = {}", id);
                result.setCode(Constants.CODE_ACCESS);
                result.setResult(id);
                closeStatementAndConnection(connection, statement);
                return result;
            }

            log.debug("getHistoryIdForMotion [3]: history not found");

            Result<History> resultHistory = createAndSaveHistory(subjectId);
            if (resultHistory.getCode() == Constants.CODE_ACCESS) {
                result.setCode(Constants.CODE_ACCESS);
                result.setResult(resultHistory.getResult().getId().toString());
            }
            log.debug("getHistoryIdForMotion [4]: result = {}", result);
        } catch (Exception e) {
            log.error("getHistoryIdForMotion [5]: {}", e.getMessage());
        } finally {
            closeStatementAndConnection(connection, statement);
        }
        return result;
    }

    private Result<Barrier> getBarrierById(Integer id) {
        log.debug("getBarrierById [1]: id = {}", id);
        Result<Barrier> result = new Result<>();
        result.setCode(Constants.CODE_NOT_FOUND);

        Connection connection = null;
        Statement statement = null;
        try {
            connection = connection();
            statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_BARRIER_BY_ID, id));

            if (resultSet.next()) {
                result.setResult(createBarrier(id, resultSet.getInt(Constants.KEY_BARRIER_FLOOR), resultSet.getBoolean(Constants.KEY_IS_OPEN)));
                result.setCode(Constants.CODE_ACCESS);
            }

        } catch (Exception e) {
            log.error("getBarrierById [2]: {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
            result.setMessage(e.getMessage());
        } finally {
            closeStatementAndConnection(connection, statement);
        }

        log.debug("getBarrierById [3]: result {}", result);
        return result;
    }

    private Result<History> createAndSaveHistory(Integer subjectId) {
        log.debug("createAndSaveHistory [1]: subjectId = {}", subjectId);
        Result<History> result = new Result<>(null, Constants.CODE_ERROR, null);
        try {
            History history = createHistory(subjectId, null);
            Connection connection = connection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(String.format(Constants.INSERT_HISTORY, history.getSubjectId(), history.getDate()));
            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_HISTORY_BY_DATE_AND_SUBJECT_ID, history.getDate(), history.getSubjectId()));
            if (resultSet.next()) {
                history.setId(Integer.parseInt(resultSet.getString(Constants.KEY_ID)));
                result.setCode(Constants.CODE_ACCESS);
                result.setResult(history);
                log.debug("createAndSaveHistory [2]: history = {}", history);
            } else {
                log.debug("createAndSaveHistory [3]: history not found = {}", history);
            }
        } catch (Exception e) {
            log.error("createAndSaveHistory [4]: {}", e.getMessage());
        }
        return result;
    }

    private Result<Object> writeNewSubject(Subject subject) throws SQLException {
        log.debug("writeNewSubject [1]: New subject is writing {}", subject);

        Connection connection = connection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(createSubjectInsertString(subject));
        closeStatementAndConnection(connection, statement);

        log.debug("writeNewSubject [2]: new subject record is successful {}", subject);
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    private Result<Object> saveModifySubject(Subject subject) throws SQLException {
        log.debug("saveModifySubject [1] : {}", subject);
        Connection connection = connection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(createSubjectUpdateString(subject));
        closeStatementAndConnection(connection, statement);

        log.debug("saveModifySubject [2] : subject modification is successful");
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    private String createSubjectUpdateString(Subject subject) {
        SubjectType subjectType = subject.getType();
        String result = "";

        switch (subjectType) {
            case ADMIN, USER -> result = createHumanUpdate(subject);
            case ANIMAL -> result = createAnimalUpdate(subject);
            case TRANSPORT -> result = createTransportUpdate(subject);
            case UNDEFINED -> log.debug("objectValidation [1]: UNDEFINED subject");
            default -> log.error("objectValidation [2]: error there is no such SubjectType");
        }
        log.debug("createSubjectInsertString [3]: insertString = {}", result);
        return result;
    }

    private String createTransportUpdate(Subject subject) {
        Transport transport = (Transport) subject;
        return String.format(Constants.UPDATE_SUBJECT, transport.getType(), null, null, null, null, null, null, transport.getColor(), transport.getNumber(), transport.getId());
    }

    private String createAnimalUpdate(Subject subject) {
        Animal animal = (Animal) subject;
        return String.format(Constants.UPDATE_SUBJECT, animal.getType(), animal.getName(), null, null, null, null, null, animal.getColor(), null, animal.getId());
    }

    private String createHumanUpdate(Subject subject) {
        Human human = (Human) subject;
        return String.format(Constants.UPDATE_SUBJECT, human.getType(), human.getName(), human.getPassword(), human.getLogin(), human.getSurname(), human.getLogin(), human.getEmail(), null, null, human.getId());
    }

    private String createSubjectInsertString(Subject subject) {
        SubjectType subjectType = subject.getType();
        String result = "";

        switch (subjectType) {
            case ADMIN, USER -> result = createHumanInsert(subject);
            case ANIMAL -> result = createAnimalInsert(subject);
            case TRANSPORT -> result = createTransportInsert(subject);
            case UNDEFINED -> log.debug("objectValidation [1]: UNDEFINED subject");
            default -> log.error("objectValidation [2]: error there is no such SubjectType");
        }
        log.debug("createSubjectInsertString [3]: insertString = {}", result);
        return result;
    }

    private String createTransportInsert(Subject subject) {
        Transport transport = (Transport) subject;
        return String.format(Constants.INSERT_SUBJECT, transport.getType(), null, null, null, null, null, null, transport.getColor(), transport.getNumber());
    }

    private String createAnimalInsert(Subject subject) {
        Animal animal = (Animal) subject;
        return String.format(Constants.INSERT_SUBJECT, animal.getType(), animal.getName(), null, null, null, null, null, animal.getColor(), null);
    }

    private String createHumanInsert(Subject subject) {
        Human human = (Human) subject;
        return String.format(Constants.INSERT_SUBJECT, human.getType(), human.getName(), human.getPassword(), human.getLogin(), human.getSurname(), human.getPatronymic(), human.getEmail(), null, null);
    }

    private Result<Subject> getSubjectById(Integer id) {
        log.debug("getSubjectById [1] : id = {}", id);
        Result<Subject> result = new Result<>();
        result.setCode(Constants.CODE_NOT_FOUND);

        Connection connection = null;
        Statement statement = null;
        try {
            connection = connection();
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(Constants.SELECT_SUBJECT_BY_ID, id));
            if (resultSet.next()) {
                Subject subject = createSubject(resultSet);
                result.setResult(subject);
                result.setCode(Constants.CODE_ACCESS);
            }
        } catch (Exception e) {
            log.error("getSubjectById: {}", e.getMessage());
            result.setCode(Constants.CODE_ERROR);
            result.setMessage(e.getMessage());
        } finally {
            closeStatementAndConnection(connection, statement);
        }

        log.debug("getSubjectById [2] : result {}", result);
        return result;
    }

    private void closeStatementAndConnection(Connection connection, Statement statement) {
        try {
            if (connection != null && statement != null) {
                statement.close();
                connection.close();
            }
        } catch (SQLException e) {
            log.error("closeStatementAndConnection [1]: error = {}", e.getMessage());
        }
    }

    private Subject createSubject(ResultSet resultSet) throws SQLException {
        Integer id = Integer.parseInt(resultSet.getString(Constants.KEY_ID));
        SubjectType subjectType = SubjectType.valueOf(resultSet.getString(Constants.KEY_TYPE));
        Subject result = null;

        switch (subjectType) {
            case ADMIN, USER -> result = createHuman(id, subjectType, resultSet.getString(Constants.KEY_PASSWORD), resultSet.getString(Constants.KEY_LOGIN), resultSet.getString(Constants.KEY_NAME), resultSet.getString(Constants.KEY_SURNAME), resultSet.getString(Constants.KEY_PATRONYMIC), resultSet.getString(Constants.KEY_EMAIL));
            case ANIMAL -> result = createAnimal(id, resultSet.getString(Constants.KEY_NAME), resultSet.getString(Constants.KEY_COLOR));
            case TRANSPORT -> result = createTransport(id, resultSet.getString(Constants.KEY_NUMBER), resultSet.getString(Constants.KEY_COLOR));
            case UNDEFINED -> log.debug("objectValidation [1]: UNDEFINED subject");
            default -> log.error("objectValidation [2]: error there is no such SubjectType");
        }
        return result;
    }
}
