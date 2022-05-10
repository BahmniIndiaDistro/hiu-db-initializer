package org.projecteka.hiu;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Application {
    public static void main(String[] args) {
        System.out.println("Connecting...");
        try {
            Connection connection = openConnection();
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            System.out.println("Creating schema for health information user");
            Liquibase liquibase = new liquibase.Liquibase("liquibase.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            e.printStackTrace();
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
        Statement stmt = connection.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery(String.format("SELECT FROM pg_database WHERE datname = '%s'", jdbcDatabase));
        rs.last();
        if(rs.getRow() == 0){
            System.out.println("Creating database for health information user");
            stmt.executeUpdate(String.format("CREATE DATABASE %s", jdbcDatabase));
        }
        return DriverManager.getConnection(jdbcUrl.concat(jdbcDatabase), jdbcUsername, jdbcUserPwd);
    }

    private static boolean isEmptyString(String value) {
        return (value == null) || "".equals(value.trim());
    }
}
