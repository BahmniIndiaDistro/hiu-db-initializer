package org.projecteka.hiu;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Application {
    public static void main(String[] args) {
        System.out.println("Connecting...");
        try {
            Connection connection = openConnection();
            Database database = getDatabaseConnection(connection);
            System.out.println("Creating schema for health information user");
            runLiquibaseScript(database);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Database getDatabaseConnection(Connection connection) throws DatabaseException {
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

    private static void runLiquibaseScript(Database database) throws LiquibaseException {
        Liquibase liquibase = new liquibase.Liquibase("liquibase.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());
    }

    private static boolean checkIfDBExits(Connection connection, String databaseName) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT FROM pg_database WHERE datname = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.setString(1,databaseName);
        ResultSet rs = stmt.executeQuery();
        rs.last();
        return rs.getRow() == 0;
    }

    private static void createDB(Connection connection, String databaseName) throws SQLException {
        if(checkIfDBExits(connection,databaseName)){
            PreparedStatement stmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
            System.out.println("Creating database for health information user");
            stmt.executeUpdate();
        }
    }

    private static Connection openConnection() throws Exception {
        String jdbcUrl = System.getProperty("jdbc.url");
        String jdbcUsername = System.getProperty("jdbc.username");
        String jdbcUserPwd = System.getProperty("jdbc.password");
        String jdbcDatabase = System.getProperty("jdbc.database");
        if (isEmptyString(jdbcUrl) || isEmptyString(jdbcUsername) || isEmptyString(jdbcUserPwd) || isEmptyString(jdbcDatabase)) {
            throw new Exception("you must set jdbc.url, jdbc.username, jdbc.password as properties, either using -D option or setting as env");
        }
        Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcUserPwd);
        createDB(connection,jdbcDatabase);
        return DriverManager.getConnection(jdbcUrl.concat(jdbcDatabase), jdbcUsername, jdbcUserPwd);
    }

    private static boolean isEmptyString(String value) {
        return (value == null) || "".equals(value.trim());
    }
}
