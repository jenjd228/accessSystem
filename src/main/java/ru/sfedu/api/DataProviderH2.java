package ru.sfedu.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sfedu.Constants;
import ru.sfedu.model.MoveType;
import ru.sfedu.model.Result;
import ru.sfedu.model.Subject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static ru.sfedu.utils.ConfigurationUtil.getConfigurationEntry;

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

        return null;
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
}
