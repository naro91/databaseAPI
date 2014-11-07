package database;

import com.google.gson.JsonObject;
import com.mysql.fabric.jdbc.FabricMySQLDriver;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by narek on 06.11.14.
 */
public class Database {

    private static final String URL = "jdbc:mysql://localhost:3306/api_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "kocmoc";
    private Connection connection;

    public Database () {
        try {
            Driver driver = new FabricMySQLDriver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Ошибка");
        }
    }

    public void setConnection() {
        try {
            Driver driver = new FabricMySQLDriver();
            DriverManager.registerDriver(driver);
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            //Statement statement = connection.createStatement();
            //statement.execute("INSERT INTO Movie(mID, title, year, director) VALUES (3, \"Magic programm\", 2005, \"Abovyan\")");
            //int res = statement.executeUpdate("UPDATE Movie SET director = 'Narek' WHERE mID = 3");
            //ResultSet res = statement.executeQuery("SELECT * FROM Movie");
            // блок запросов добавляется с помощью метода
            //statement.addBatch("INSERT INTO Movie(mID, title, year, director) VALUES (4, \"Bond\", 2004, \"Abovyan\")");
            //statement.addBatch("INSERT INTO Movie(mID, title, year, director) VALUES (5, \"Bauman\", 2014, \"Meloyan\")");
            // и запросы выполняются с помощью
            //statement.executeBatch();
            // пул запросов стирается
            //statement.clearBatch();
            //while (res.next()) {
            //    System.out.println(res.getInt("mID")+ "\t\t" + res.getString("title") + "\t\t"
            //            + res.getInt("year") + "\t\t" + res.getString("director"));
            //}

        } catch (SQLException e) {
            System.err.println("Ошибка");
        }
    }

    public Connection getConnection()
    {
        return connection;
    }

    public int createForum(JsonObject ForumDate) throws SQLException {
        SimpleExecutor exec = new SimpleExecutor();
        String query = new String().concat("INSERT INTO Forum (`user`, `name`, `short_name`) VALUES (")
        .concat(ForumDate.get("user").toString()).concat(",").concat(ForumDate.get("name").toString()).concat(",")
                .concat(ForumDate.get("short_name").toString()).concat(");");
        return exec.execUpdateAndReturnId(connection, query);
    }

}
