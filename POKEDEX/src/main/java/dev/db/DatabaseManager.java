package dev.db;

import dev.models.SqlCommand;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

    private Connection connection;

    private static DatabaseManager instance;


    public void connect() throws SQLException, IOException {

        Properties appProps = new Properties();
        appProps.load(new FileInputStream(getClass().getClassLoader().getResource("application.properties").getPath()));

        String username = appProps.getProperty("db.username");
        String password = appProps.getProperty("db.password");
        String name = appProps.getProperty("db.name");
        String url = "jdbc:h2:mem:"+name+";DB_CLOSE_DELAY=-1";

        connection = DriverManager.getConnection(url, username, password);

        System.out.println("Ejecutando SQL");

        Reader reader = new BufferedReader(new FileReader(getClass().getClassLoader().getResource(appProps.getProperty("db.initScript")).getPath()));
        ScriptRunner sr = new ScriptRunner(connection);
        sr.runScript(reader);

    }

    private DatabaseManager() throws IOException, SQLException {

        connect();

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
            preparedStatement.setObject(i+1, sqlCommand.getParams().get(i));
        }

        return preparedStatement;
    }

    public ResultSet executeQuery(SqlCommand sqlCommand) throws SQLException {

        PreparedStatement preparedStatement = prepareStatement(sqlCommand);

        return preparedStatement.executeQuery();

    }

    public int executeUpdate(SqlCommand sqlCommand) throws SQLException {

        PreparedStatement preparedStatement = prepareStatement(sqlCommand);

        return preparedStatement.executeUpdate();

    }


}