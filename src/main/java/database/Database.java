package database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mysql.fabric.jdbc.FabricMySQLDriver;
import com.sun.rowset.CachedRowSetImpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
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

    public int createForum(JsonObject forumDate) throws SQLException {
        SimpleExecutor exec = new SimpleExecutor();
        PreparedStatement stm = connection.prepareStatement("INSERT INTO Forum (`user`, `name`, `short_name`) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        stm.setString(1, forumDate.get("user").getAsString());
        stm.setString(2, forumDate.get("name").getAsString());
        stm.setString(3, forumDate.get("short_name").getAsString());

        return exec.execUpdateAndReturnId(stm);
    }

    public JsonObject forumDetails (JsonObject query) throws SQLException {
        CachedRowSetImpl forumJoinUser = new CachedRowSetImpl();
        CachedRowSetImpl followers = new CachedRowSetImpl();
        CachedRowSetImpl following = new CachedRowSetImpl();
        CachedRowSetImpl subscriptions = new CachedRowSetImpl();
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
            forumJoinUser.populate(resultSet);
            resultSet.close();
            stm.close();

            if(forumJoinUser.next()){

                responseJson.addProperty("id", forumJoinUser.getLong(1));
                responseJson.addProperty("name", forumJoinUser.getString(2));
                responseJson.addProperty("short_name", short_name);

                stm = connection.prepareStatement("SELECT * From User_followers WHERE user = ?");
                stm.setString(1, short_name);
                resultSet = stm.executeQuery();
                followers.populate(resultSet);
                resultSet.close();
                stm.close();

                while (followers.next()) {
                    temp.add(followers.getString(2));
                }

                stm = connection.prepareStatement("SELECT * From User_followers WHERE followers = ?");
                JsonObject jUser = new JsonObject();
                jUser.addProperty("about", forumJoinUser.getString(7));
                jUser.addProperty("email", forumJoinUser.getString(6));
                jUser.add("followers", gson.toJsonTree(temp));

                stm.setString(1, short_name);
                resultSet = stm.executeQuery();
                following.populate(resultSet);
                resultSet.close();
                stm.close();
                temp.clear();

                while (following.next()) {
                    temp.add(following.getString(1));
                }
                jUser.add("following", gson.toJsonTree(temp));
                jUser.addProperty("id", forumJoinUser.getLong(4));
                jUser.addProperty("isAnonymous", forumJoinUser.getBoolean(8));
                jUser.addProperty("name", forumJoinUser.getString(9));

                stm = connection.prepareStatement("SELECT * From Thread_followers WHERE follower_email = ?");
                stm.setString(1, short_name);
                resultSet = stm.executeQuery();
                subscriptions.populate(resultSet);
                resultSet.close();
                stm.close();
                ArrayList<Integer> temp2 = new ArrayList<Integer>();

                while (subscriptions.next()) {
                    temp2.add(subscriptions.getInt(1));
                }
                jUser.add("subscriptions", gson.toJsonTree(temp2));
                jUser.addProperty("username", forumJoinUser.getString(5));

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
        stm.setString(7, postData.get("date").getAsString() );
        stm.setString(8, postData.get("thread").getAsString());
        stm.setString(9, postData.get("message").getAsString());
        stm.setString(10, postData.get("user").getAsString() );
        stm.setString(11, postData.get("forum").getAsString());

        return exec.execUpdateAndReturnId(stm);
    }

    public JsonObject postDetails(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("SELECT * FROM Post WHERE id = ?");
        stm.setString(1, query.get("post").getAsString());
        ResultSet resultSet = stm.executeQuery();
        JsonObject response = new JsonObject();
        HashMap<String, String> tempQuery = new HashMap<>();
        String related;

        if (resultSet.next()) {
            response.addProperty("date", resultSet.getString("date"));
            response.addProperty("dislikes", resultSet.getInt("dislikes"));
            response.addProperty("id", resultSet.getInt("id"));
            response.addProperty("isApproved", resultSet.getBoolean("isApproved"));
            response.addProperty("isDeleted", resultSet.getBoolean("isDeleted"));
            response.addProperty("isEdited", resultSet.getBoolean("isEdited"));
            response.addProperty("isHighlighted", resultSet.getBoolean("isHighlighted"));
            response.addProperty("isSpam", resultSet.getBoolean("isSpam"));
            response.addProperty("likes", resultSet.getInt("likes"));
            response.addProperty("message", resultSet.getString("message"));
            response.addProperty("parent", resultSet.getString("parent") == null ? null : resultSet.getInt("parent"));
            response.addProperty("points", resultSet.getInt("points"));



            if (query.get("related") != null) {
                related = query.get("related").getAsString();

                if (related.contains("forum")) {
                    response.addProperty("forum", resultSet.getString("forum"));
                }else response.addProperty("forum", resultSet.getString("forum"));

                if (related.contains("thread")) {
                    response.addProperty("thread", resultSet.getInt("thread"));
                }else response.addProperty("thread", resultSet.getInt("thread"));

                if (related.contains("user")) {
                    response.addProperty("user", resultSet.getString("user"));
                }else response.addProperty("user", resultSet.getString("user"));

            } else {
                response.addProperty("forum", resultSet.getString("forum"));
                response.addProperty("thread", resultSet.getInt("thread"));
                response.addProperty("user", resultSet.getString("user"));
            }

        }
        resultSet.close();
        stm.close();

        return response;
    }

    public JsonObject postRemove(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE Post SET isDeleted = true WHERE id = ?");
        int id = Integer.valueOf( query.get("post").getAsString() );
        stm.setInt(1, id);
        stm.executeUpdate();
        JsonObject details = new JsonObject();
        details.addProperty("post", id);
        return postDetails(details);
    }

    public JsonObject postRestore(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE Post SET isDeleted = false WHERE id = ?");
        int id = Integer.valueOf( query.get("post").getAsString() );
        stm.setInt(1, id);
        stm.executeUpdate();

        return query;
    }

    public JsonObject postUpdate(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE Post SET message = ? WHERE id = ?");
        int id = Integer.valueOf( query.get("post").getAsString() );
        stm.setString(1, query.get("message").getAsString() );
        stm.setInt(2, id);
        stm.executeUpdate();

        return query;
    }



    public int createUser(JsonObject userData) throws SQLException {
        SimpleExecutor exec = new SimpleExecutor();
        PreparedStatement stm = connection.prepareStatement("INSERT INTO User (`isAnonymous`, `username`, `about`," +
                "`name`, `email`) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        stm.setBoolean(1, Boolean.parseBoolean(userData.get("isAnonymous").getAsString()));
        stm.setString(2, !userData.get("username").toString().equals("null") ? userData.get("username").getAsString() : null );
        stm.setString(3, !userData.get("about").toString().equals("null") ? userData.get("about").getAsString() : null);
        stm.setString(4, !userData.get("name").toString().equals("null") ? userData.get("name").getAsString() : null);
        stm.setString(5, userData.get("email").getAsString());

        return exec.execUpdateAndReturnId(stm);
    }

    public JsonObject userDetails(JsonObject query) throws SQLException {
        CachedRowSetImpl User = new CachedRowSetImpl();
        CachedRowSetImpl followers = new CachedRowSetImpl();
        CachedRowSetImpl following = new CachedRowSetImpl();
        CachedRowSetImpl subscriptions = new CachedRowSetImpl();
        JsonObject jUser = new JsonObject();
        ArrayList<String> temp = new ArrayList<String>();
        String userEmail;
        userEmail = query.get("user").getAsString();
        //System.out.println(userEmail);

        PreparedStatement stm = connection.prepareStatement("SELECT * FROM User WHERE email = ? ");
        stm.setString(1, userEmail);
        ResultSet resultSet = stm.executeQuery();
        User.populate(resultSet);
        resultSet.close();
        stm.close();

        if(User.next()) {
            stm = connection.prepareStatement("SELECT * From User_followers WHERE user = ?");
            stm.setString(1, userEmail);
            resultSet = stm.executeQuery();
            followers.populate(resultSet);
            resultSet.close();
            stm.close();

            while (followers.next()) {
                temp.add(followers.getString("followers"));
            }

            stm = connection.prepareStatement("SELECT * From User_followers WHERE followers = ?");
            jUser.addProperty("about", User.getString("about"));
            jUser.addProperty("email", User.getString("email"));
            jUser.add("followers", gson.toJsonTree(temp));

            stm.setString(1, userEmail);
            resultSet = stm.executeQuery();
            following.populate(resultSet);
            resultSet.close();
            stm.close();
            temp.clear();

            while (following.next()) {
                temp.add(following.getString("user"));
            }
            jUser.add("following", gson.toJsonTree(temp));
            jUser.addProperty("id", User.getLong("id"));
            jUser.addProperty("isAnonymous", User.getBoolean("isAnonymous"));
            jUser.addProperty("name", User.getString("name"));

            stm = connection.prepareStatement("SELECT * From Thread_followers WHERE follower_email = ?");
            stm.setString(1, userEmail);
            resultSet = stm.executeQuery();
            subscriptions.populate(resultSet);
            resultSet.close();
            stm.close();
            ArrayList<Integer> temp2 = new ArrayList<Integer>();

            while (subscriptions.next()) {
                temp2.add(subscriptions.getInt(1));
            }
            jUser.add("subscriptions", gson.toJsonTree(temp2));
            jUser.addProperty("username", User.getString("username"));

        }
    
        return jUser;
    }

    public JsonObject userFollow(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("INSERT INTO User_followers (`user`, `followers`) VALUES (?, ?)");
        String follower = query.get("follower").getAsString();
        stm.setString(1, follower);
        stm.setString(2, query.get("followee").getAsString());
        stm.executeUpdate();
        stm.close();
        JsonObject detalis = new JsonObject();
        detalis.addProperty("user" ,follower);
        return userDetails(detalis);
    }

    public JsonObject userUnfollow(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("DELETE FROM User_followers WHERE user = ?");
        String follower = query.get("follower").getAsString();
        stm.setString(1, follower);
        stm.executeUpdate();
        stm.close();
        JsonObject detalis = new JsonObject();
        detalis.addProperty("user", follower);
        return userDetails(detalis);
    }

    public JsonObject userUpdate(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE User SET about = ?, name = ? WHERE email = ?");
        String user = query.get("user").getAsString();
        stm.setString(1, query.get("about").getAsString());
        stm.setString(2, query.get("name").getAsString());
        stm.setString(3, user);
        stm.executeUpdate();
        stm.close();
        JsonObject detalis = new JsonObject();
        detalis.addProperty("user", user);

        return userDetails(detalis);
    }







    public int createThread(JsonObject threadData) throws SQLException {
        SimpleExecutor exec = new SimpleExecutor();
        PreparedStatement stm = connection.prepareStatement("INSERT INTO Thread (`forum`, `title`, `isClosed`," +
                "`user`, `date`, `message`, `slug`, `isDeleted`) VALUES (?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        stm.setString(1, threadData.get("forum").getAsString());
        stm.setString(2, threadData.get("title").getAsString());
        stm.setBoolean(3, Boolean.parseBoolean(threadData.get("isClosed").toString()));
        stm.setString(4, threadData.get("user").getAsString());
        stm.setString(5, threadData.get("date").getAsString());
        stm.setString(6, threadData.get("message").getAsString());
        stm.setString(7, threadData.get("slug").getAsString());
        stm.setBoolean(8, Boolean.parseBoolean(threadData.get("isDeleted").toString()));

        return exec.execUpdateAndReturnId(stm);
    }

    public JsonObject ThreadDetails(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("SELECT `id`,`forum`,`title`,`user`,`date`,`isClosed`,`isDeleted`,`message`,`slug`,`likes`, `points`, `posts`FROM `Thread` WHERE id=?");
        stm.setInt(1, Integer.valueOf(query.get("thread").getAsString()));
        JsonObject response = new JsonObject();
        ResultSet threadDefault = stm.executeQuery();
        String related;

        if (threadDefault.next()) {

            response.addProperty("date", threadDefault.getString("date"));
            response.addProperty("id", threadDefault.getInt("id"));
            response.addProperty("isDeleted", threadDefault.getBoolean("isDeleted"));
            response.addProperty("isClosed", threadDefault.getBoolean("isClosed"));
            response.addProperty("message", threadDefault.getString("message"));
            response.addProperty("points", threadDefault.getInt("points"));
            response.addProperty("posts", threadDefault.getInt("posts"));
            response.addProperty("slug", threadDefault.getString("slug"));
            response.addProperty("title", threadDefault.getString("title"));
            response.addProperty("likes", threadDefault.getInt("likes"));

            if (query.get("related") != null) {
                related = query.get("related").getAsString();

                if (related.contains("user")) {
                    JsonObject userQuery = new JsonObject();
                    userQuery.addProperty("user", threadDefault.getString("user"));
                    response.add("user", userDetails(userQuery));
                } else response.addProperty("user", threadDefault.getString("user"));

                if (related.contains("forum")) {
                    JsonObject forumQuery = new JsonObject();
                    forumQuery.addProperty("forum", threadDefault.getString("forum"));
                    response.add("forum", forumDetails(forumQuery));
                } else response.addProperty("forum", threadDefault.getString("forum"));

            } else {
                response.addProperty("user", threadDefault.getString("user"));
                response.addProperty("forum", threadDefault.getString("forum"));
            }

        }

        return response;
    }



    public String clear() throws SQLException {
        JsonObject response = new JsonObject();
        Statement stm = connection.createStatement();
        stm.execute("TRUNCATE Forum");
        stm.execute("TRUNCATE User");
        stm.execute("TRUNCATE Thread");
        stm.execute("TRUNCATE Post");
        stm.execute("TRUNCATE User_followers");
        stm.close();

        response.addProperty("code", 0);
        response.addProperty("response", "ok");

        return response.toString();
    }

    public ArrayList followers () {

        return null;
    }

}
