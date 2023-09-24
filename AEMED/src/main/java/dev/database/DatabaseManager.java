package dev.database;

import dev.database.models.SqlCommand;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

    private Connection connection;

    private static DatabaseManager instance;

    private final String username;
    private final String password;
    private final String connectionString;

    private void connect() throws SQLException, IOException {
        connection = DriverManager.getConnection(connectionString, username, password);
    }

    private void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión");
        }
    }

    private DatabaseManager() throws IOException, SQLException {
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(getClass().getClassLoader().getResource("application.properties").getPath()));
        username = appProps.getProperty("db.username");
        password = appProps.getProperty("db.password");
        String filePath = Paths.get(appProps.getProperty("db.filepath")).toAbsolutePath().toString();
        connectionString = ("jdbc:h2:" + filePath).trim();
        Reader reader = new BufferedReader(new FileReader(getClass().getClassLoader().getResource(appProps.getProperty("db.initScript")).getPath()));
        connect();
        ScriptRunner sr = new ScriptRunner(connection);
        sr.runScript(reader);
        close();
    }

    public static DatabaseManager getInstance() throws SQLException, IOException {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private PreparedStatement prepareStatement(SqlCommand sqlCommand) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(sqlCommand.getCommand());

        for (int i = 0; i < sqlCommand.getParams().size(); i++) {
            preparedStatement.setObject(i + 1, sqlCommand.getParams().get(i));
        }

        return preparedStatement;
    }

    public ResultSet executeQuery(SqlCommand sqlCommand) throws SQLException, IOException {
        connect();
        PreparedStatement preparedStatement = prepareStatement(sqlCommand);
        ResultSet resultSet = preparedStatement.executeQuery();
        close();
        return resultSet;

    }

    public int executeUpdate(SqlCommand sqlCommand) throws SQLException, IOException {
        connect();
        PreparedStatement preparedStatement = prepareStatement(sqlCommand);
        int affectedRows = preparedStatement.executeUpdate();
        close();
        return affectedRows;

    }


}