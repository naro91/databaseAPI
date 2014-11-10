package database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mysql.fabric.jdbc.FabricMySQLDriver;
import com.sun.rowset.CachedRowSetImpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by narek on 06.11.14.
 */
public class Database {

    private static final String URL = "jdbc:mysql://localhost:3306/api_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "kocmoc";
    private Connection connection;
    Gson gson = new Gson();

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
        PreparedStatement stm = connection.prepareStatement("INSERT INTO Forum (`user`, `name`, `short_name`) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        stm.setString(1, ForumDate.get("user").getAsString());
        stm.setString(2, ForumDate.get("name").getAsString());
        stm.setString(3, ForumDate.get("short_name").getAsString());

        return exec.execUpdateAndReturnId(stm);
    }

    public JsonObject forumDetails (JsonObject query) throws SQLException {
        CachedRowSetImpl res1 = new CachedRowSetImpl();
        CachedRowSetImpl res2 = new CachedRowSetImpl();
        CachedRowSetImpl res3 = new CachedRowSetImpl();
        CachedRowSetImpl res4 = new CachedRowSetImpl();
        JsonObject responseJson = new JsonObject();
        String short_name;

        if ( query.get("related") != null) {
            ArrayList<String> temp = new ArrayList<String>();
            short_name = query.get("forum").getAsString();

            PreparedStatement stm = connection.prepareStatement("SELECT Forum.id,Forum.name,Forum.short_name," +
                    "User.id,User.username,User.email,User.about,User.isAnonymous, " +
                    "User.name FROM Forum JOIN User ON Forum.user = User.email WHERE short_name = ? ");
            stm.setString(1, short_name);
            ResultSet resultSet = stm.executeQuery();
            res1.populate(resultSet);
            resultSet.close();
            stm.close();

            if(res1.next()){

                responseJson.addProperty("id", res1.getLong(1));
                responseJson.addProperty("name", res1.getString(2));
                responseJson.addProperty("short_name", short_name);

                stm = connection.prepareStatement("SELECT * From User_followers WHERE user = ?");
                stm.setString(1, short_name);
                resultSet = stm.executeQuery();
                res2.populate(resultSet);
                resultSet.close();
                stm.close();

                if (res2 != null) {
                    while (res2.next()) {
                        temp.add(res2.getString(2));
                    }
                }

                stm = connection.prepareStatement("SELECT * From User_followers WHERE followers = ?");
                JsonObject jUser = new JsonObject();
                jUser.addProperty("about", res1.getString(7));
                jUser.addProperty("email", res1.getString(6));
                jUser.add("followers", gson.toJsonTree(temp));

                stm.setString(1, short_name);
                resultSet = stm.executeQuery();
                res3.populate(resultSet);
                resultSet.close();
                stm.close();
                temp.clear();

                if (res3 != null) {
                    while (res3.next()) {
                        temp.add(res3.getString(1));
                    }
                }
                jUser.add("following", gson.toJsonTree(temp));
                jUser.addProperty("id", res1.getLong(4));
                jUser.addProperty("isAnonymous", res1.getBoolean(8));
                jUser.addProperty("name", res1.getString(9));

                stm = connection.prepareStatement("SELECT * From Thread_followers WHERE follower_email = ?");
                stm.setString(1, short_name);
                resultSet = stm.executeQuery();
                res4.populate(resultSet);
                resultSet.close();
                stm.close();
                ArrayList<Integer> temp2 = new ArrayList<Integer>();

                if (res4 != null) {
                    while (res4.next()) {
                        temp2.add(res4.getInt(1));
                    }
                }
                jUser.add("subscriptions", gson.toJsonTree(temp2));
                jUser.addProperty("username", res1.getString(5));

                responseJson.add("user", jUser);
            }

        } else {
            short_name = query.get("forum").getAsString();
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM Forum WHERE short_name=?");
            stm.setObject(1,short_name);
            ResultSet resultSet = stm.executeQuery();
            if(resultSet.next()){
                responseJson.addProperty("id", resultSet.getLong("id"));
                responseJson.addProperty("name", resultSet.getString("name"));
                responseJson.addProperty("short_name", short_name);
                responseJson.addProperty("user", resultSet.getString("user"));
            }
            resultSet.close();
            stm.close();
        }

        System.out.println(responseJson);
        return responseJson;
    }

    public int createPost( JsonObject postData ) throws SQLException {
        SimpleExecutor exec = new SimpleExecutor();
        PreparedStatement stm = connection.prepareStatement("INSERT INTO Post (`parent`, `isApproved`, `isHighlighted`," +
                "`isEdited`, `isSpam`, `isDeleted`, `date`, `thread`, `message`, `user`, `forum` ) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        stm.setString(1, null);
        stm.setBoolean(2, Boolean.parseBoolean(postData.get("isApproved").toString()));
        stm.setBoolean(3, Boolean.parseBoolean(postData.get("isHighlighted").toString()));
        stm.setBoolean(4, Boolean.parseBoolean(postData.get("isEdited").toString()));
        stm.setBoolean(5, Boolean.parseBoolean(postData.get("isSpam").toString()));
        stm.setBoolean(6, Boolean.parseBoolean(postData.get("isDeleted").toString()));
        stm.setString(7, postData.get("date").toString() );
        stm.setString(8, postData.get("thread").toString());
        stm.setString(9, postData.get("message").toString());
        stm.setString(10, postData.get("user").toString() );
        stm.setString(11, postData.get("forum").toString());

        return exec.execUpdateAndReturnId(stm);
    }

    public int createUser(JsonObject userData) throws SQLException {
        SimpleExecutor exec = new SimpleExecutor();
        PreparedStatement stm = connection.prepareStatement("INSERT INTO User (`isAnonymous`, `username`, `about`," +
                "`name`, `email`) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        stm.setBoolean(1, Boolean.parseBoolean(userData.get("isAnonymous").getAsString()));
        System.out.println(userData.get("username").toString().equals("null"));
        stm.setString(2, !userData.get("username").toString().equals("null") ? userData.get("username").getAsString() : null );
        stm.setString(3, !userData.get("about").toString().equals("null") ? userData.get("about").getAsString() : null);
        stm.setString(4, !userData.get("name").toString().equals("null") ? userData.get("name").getAsString() : null);
        stm.setString(5, userData.get("email").getAsString());

        return exec.execUpdateAndReturnId(stm);
    }

    public int createThread(JsonObject threadData) throws SQLException {
        SimpleExecutor exec = new SimpleExecutor();
        PreparedStatement stm = connection.prepareStatement("INSERT INTO Thread (`forum`, `title`, `isClosed`," +
                "`user`, `date`, `message`, `slug`, `isDeleted`) VALUES (?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        stm.setString(1, threadData.get("forum").toString());
        stm.setString(2, threadData.get("title").toString());
        stm.setBoolean(3, Boolean.parseBoolean(threadData.get("isClosed").toString()));
        stm.setString(4, threadData.get("user").toString());
        stm.setString(5, threadData.get("date").toString());
        stm.setString(6, threadData.get("message").toString());
        stm.setString(7, threadData.get("slug").toString());
        stm.setBoolean(8, Boolean.parseBoolean(threadData.get("isDeleted").toString()));

        return exec.execUpdateAndReturnId(stm);
    }

    public String clear() throws SQLException {
        JsonObject response = new JsonObject();
        Statement stm = connection.createStatement();
        stm.execute("TRUNCATE Forum");
        stm.execute("TRUNCATE User");
        stm.execute("TRUNCATE Thread");
        stm.execute("TRUNCATE Post");
        stm.close();

        response.addProperty("code", 0);
        response.addProperty("response", "ok");

        return response.toString();
    }
}
