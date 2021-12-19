package ru.sfedu.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.Constants;
import ru.sfedu.model.*;

import java.sql.*;
import java.util.AbstractMap;
import java.util.TreeMap;

import static ru.sfedu.utils.ConfigurationUtil.getConfigurationEntry;
import static ru.sfedu.utils.SubjectUtil.*;

public class DataProviderH2 implements IDataProvider {

    private final Logger log = LogManager.getLogger(DataProviderXml.class.getName());

    private String h2PathFolder = "./";

    public DataProviderH2() {
        h2PathFolder = h2PathFolder.concat(Constants.H2_PATH_FOLDER);
        createTables();
    }

    public DataProviderH2(String path) {
        h2PathFolder = h2PathFolder.concat(path).concat(Constants.H2_PATH_FOLDER);
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
            log.error("DataProviderH2 - initialization error");
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
        return false;
    }

    @Override
    public Result<Object> grantAccess(Integer subjectId, Integer barrierId, Integer year, Integer month, Integer day, Integer hours) {
        return null;
    }

    @Override
    public boolean gateAction(Integer subjectId, Integer barrierId, MoveType moveType) {
        return false;
    }

    private Result<Object> writeNewSubject(Subject subject) throws SQLException {
        log.info("writeNewSubject [1]: New subject is writing {}", subject);

        Connection connection = connection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(createSubjectInsertString(subject));
        closeStatementAndConnection(connection, statement);

        log.info("writeNewSubject [2]: new subject record is successful {}", subject);
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    private Result<Object> saveModifySubject(Subject subject) throws SQLException {
        log.info("saveModifySubject [1] : {}", subject);

        Connection connection = connection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(createSubjectUpdateString(subject));
        closeStatementAndConnection(connection, statement);

        log.info("saveModifySubject [2] : subject modification is successful");
        return new Result<>(null, Constants.CODE_ACCESS, subject);
    }

    private String createSubjectUpdateString(Subject subject) {
        SubjectType subjectType = subject.getType();
        String result = "";

        switch (subjectType) {
            case ADMIN, USER -> result = createHumanUpdate(subject);
            case ANIMAL -> result = createAnimalUpdate(subject);
            case TRANSPORT -> result = createTransportUpdate(subject);
            case UNDEFINED -> log.info("objectValidation [1]: UNDEFINED subject");
            default -> log.error("objectValidation [2]: error there is no such SubjectType");
        }
        log.info("createSubjectInsertString [3]: insertString = {}", result);
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
            case UNDEFINED -> log.info("objectValidation [1]: UNDEFINED subject");
            default -> log.error("objectValidation [2]: error there is no such SubjectType");
        }
        log.info("createSubjectInsertString [3]: insertString = {}", result);
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
        log.info("getSubjectById [1] : id = {}", id);
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

        log.info("getSubjectById [2] : result {}", result);
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
            case UNDEFINED -> log.info("objectValidation [1]: UNDEFINED subject");
            default -> log.error("objectValidation [2]: error there is no such SubjectType");
        }
        return result;
    }
}
