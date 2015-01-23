package database;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.fabric.jdbc.FabricMySQLDriver;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import java.sql.*;
import java.util.ArrayList;


/**
 * Created by Abovyan Narek on 06.11.14.
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
        try (PreparedStatement stm = connection.prepareStatement("INSERT INTO Forum (`user`, `name`, `short_name`) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stm.setString(1, forumDate.get("user").getAsString());
            stm.setString(2, forumDate.get("name").getAsString());
            stm.setString(3, forumDate.get("short_name").getAsString());

            return exec.execUpdateAndReturnId(stm);
        }catch (SQLException e) {
           throw new SQLException();
        }
    }

    public JsonObject forumDetails (JsonObject query) throws SQLException {
        JsonObject responseJson = new JsonObject();
        String short_name;

        if ( query.get("related") != null) {
            ArrayList<String> temp = new ArrayList<String>();
            short_name = query.get("forum").getAsString();

            PreparedStatement stmForumJoinUser = connection.prepareStatement("SELECT Forum.id,Forum.name,Forum.short_name," +
                    "User.id,User.username,User.email,User.about,User.isAnonymous, " +
                    "User.name FROM Forum JOIN User ON Forum.user = User.email WHERE short_name = ? ");
            stmForumJoinUser.setString(1, short_name);
            ResultSet forumJoinUser = stmForumJoinUser.executeQuery();


            if(forumJoinUser.next()){

                responseJson.addProperty("id", forumJoinUser.getLong(1));
                responseJson.addProperty("name", forumJoinUser.getString(2));
                responseJson.addProperty("short_name", short_name);

                PreparedStatement stmFollowers = connection.prepareStatement("SELECT * From User_followers WHERE user = ?");
                stmFollowers.setString(1, short_name);
                ResultSet followers = stmFollowers.executeQuery();


                while (followers.next()) {
                    temp.add(followers.getString(2));
                }

                PreparedStatement stmFollowing = connection.prepareStatement("SELECT * From User_followers WHERE followers = ?");
                JsonObject jUser = new JsonObject();
                jUser.addProperty("about", forumJoinUser.getString(7));
                jUser.addProperty("email", forumJoinUser.getString(6));
                jUser.add("followers", gson.toJsonTree(temp));

                stmFollowing.setString(1, short_name);
                ResultSet following = stmFollowing.executeQuery();
                temp.clear();

                while (following.next()) {
                    temp.add(following.getString(1));
                }
                jUser.add("following", gson.toJsonTree(temp));
                jUser.addProperty("id", forumJoinUser.getLong(4));
                jUser.addProperty("isAnonymous", forumJoinUser.getBoolean(8));
                jUser.addProperty("name", forumJoinUser.getString(9));

                PreparedStatement stmSubscriptions = connection.prepareStatement("SELECT * From Thread_followers WHERE follower_email = ?");
                stmSubscriptions.setString(1, short_name);
                ResultSet subscriptions = stmSubscriptions.executeQuery();
                ArrayList<Integer> temp2 = new ArrayList<Integer>();

                while (subscriptions.next()) {
                    temp2.add(subscriptions.getInt(1));
                }
                jUser.add("subscriptions", gson.toJsonTree(temp2));
                jUser.addProperty("username", forumJoinUser.getString(5));

                responseJson.add("user", jUser);

                followers.close();
                following.close();
                subscriptions.close();
                stmFollowers.close();
                stmFollowing.close();
                stmSubscriptions.close();
            }
            forumJoinUser.close();
            stmForumJoinUser.close();
        } else {
            short_name = safelyGetStringFromJson(query, "forum"); //query.get("forum").getAsString();
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

        //System.out.println(responseJson);
        return responseJson;
    }

    public JsonArray forumListUsers(JsonObject query) throws SQLException {
        String querySqlmass[] = { "SELECT u.about, u.email, u.id, u.isAnonymous, u.name, u.username FROM User u WHERE u.email in (SELECT DISTINCT user FROM Post p WHERE p.forum = ?) " +
        "AND u.id >= ? ORDER BY u.name DESC LIMIT ?",
                "SELECT u.about, u.email, u.id, u.isAnonymous, u.name, u.username FROM User u WHERE u.email in (SELECT DISTINCT user FROM Post p WHERE p.forum = ?) " +
        "AND u.id >= ? ORDER BY u.name ASC LIMIT ?"};
        String querySql;
        JsonObject detalisUserQuery = new JsonObject();
        JsonArray usersArray = new JsonArray();
        if (query.get("order") != null) {
            querySql = query.get("order").getAsString().equals("asc") ? querySqlmass[1] : querySqlmass[0];
        } else  querySql = querySqlmass[0];
        PreparedStatement stm = connection.prepareStatement(querySql);
        stm.setString(1, query.get("forum").getAsString());
        stm.setInt(2, query.get("since_id") != null ? query.get("since_id").getAsInt() : 0);
        stm.setInt(3, query.get("limit") != null ? query.get("limit").getAsInt() : 10);
        ResultSet usersResultSet = stm.executeQuery();

        /*while ( usersResultSet.next() ){
            detalisUserQuery.addProperty("user", usersResultSet.getString("u.email"));
            usersArray.add(userDetails(detalisUserQuery));
        }*/
        usersArray = userDetailsFromResultSet(usersResultSet);
        usersResultSet.close();
        stm.close();

        return usersArray;
    }

    public JsonArray forumListPost(JsonObject query) throws SQLException {
        String querySqlmass[] = { "SELECT p.id FROM Post p WHERE p.forum = ? AND p.date > ? ORDER BY p.date DESC LIMIT ?",
                "SELECT p.id FROM Post p WHERE p.forum = ? AND p.date > ? ORDER BY p.date ASC LIMIT ?"};
        String querySql;
        JsonObject detalisPostQuery = new JsonObject();
        JsonArray postArray = new JsonArray();
        if (query.get("order") != null) {
            querySql = query.get("order").getAsString().equals("asc") ? querySqlmass[1] : querySqlmass[0];
        } else  querySql = querySqlmass[0];
        PreparedStatement stm = connection.prepareStatement(querySql);
        stm.setString(1, safelyGetStringFromJson(query, "forum"));
        stm.setString(2, query.get("since") != null ? query.get("since").getAsString() : "1970-01-01 00:00:00");
        stm.setInt(3, query.get("limit") != null ? query.get("limit").getAsInt() : 10);
        ResultSet postsResultSet = stm.executeQuery();

        while ( postsResultSet.next() ){
            detalisPostQuery.addProperty("post", postsResultSet.getString("p.id"));
            if (query.get("related") != null) detalisPostQuery.addProperty("related", query.get("related").getAsString());
            postArray.add(postDetails(detalisPostQuery));
        }
        postsResultSet.close();
        stm.close();

        return postArray;
    }

    public JsonArray forumListThread(JsonObject query) throws SQLException {
        String querySqlmass[] = { "SELECT t.id FROM Thread t WHERE t.forum = ? AND t.date > ? ORDER BY t.date DESC LIMIT ?",
                "SELECT t.id FROM Thread t WHERE t.forum = ? AND t.date > ? ORDER BY t.date ASC LIMIT ?"};
        String querySql;
        JsonObject detalisThreadQuery = new JsonObject();
        JsonArray threadArray = new JsonArray();
        if (query.get("order") != null) {
            querySql = query.get("order").getAsString().equals("asc") ? querySqlmass[1] : querySqlmass[0];
        } else  querySql = querySqlmass[0];
        PreparedStatement stm = connection.prepareStatement(querySql);
        stm.setString(1, query.get("forum").getAsString());
        stm.setString(2, query.get("since") != null ? query.get("since").getAsString() : "1970-01-01 00:00:00");
        stm.setInt(3, query.get("limit") != null ? query.get("limit").getAsInt() : 10);
        ResultSet threadResultSet = stm.executeQuery();

        while ( threadResultSet.next() ){
            detalisThreadQuery.addProperty("thread", threadResultSet.getString("t.id"));
            if (query.get("related") != null) detalisThreadQuery.addProperty("related", query.get("related").getAsString());
            threadArray.add(threadDetails(detalisThreadQuery));
        }
        threadResultSet.close();
        stm.close();

        return threadArray;
    }





    public int createPost( JsonObject postData ) throws SQLException {
        int idPost;
        try (PreparedStatement stmThread = connection.prepareStatement("UPDATE Thread SET posts = posts + 1 WHERE id = ?")) {
            stmThread.setInt(1, postData.get("thread").getAsInt());
            if (stmThread.executeUpdate() == 1) {
                stmThread.close();
                SimpleExecutor exec = new SimpleExecutor();
                try (PreparedStatement stmPost = connection.prepareStatement("INSERT INTO Post (`parent`, `isApproved`, `isHighlighted`," +
                                "`isEdited`, `isSpam`, `isDeleted`, `date`, `thread`, `message`, `user`, `forum` ) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    stmPost.setString(1, safelyGetStringFromJson(postData, "parent"));
                    stmPost.setBoolean(2, postData.get("isApproved").getAsBoolean());
                    stmPost.setBoolean(3, postData.get("isHighlighted").getAsBoolean());
                    stmPost.setBoolean(4, postData.get("isEdited").getAsBoolean());
                    stmPost.setBoolean(5, postData.get("isSpam").getAsBoolean());
                    stmPost.setBoolean(6, postData.get("isDeleted").getAsBoolean());
                    stmPost.setString(7, postData.get("date").getAsString());
                    stmPost.setString(8, postData.get("thread").getAsString());
                    stmPost.setString(9, postData.get("message").getAsString());
                    stmPost.setString(10, postData.get("user").getAsString());
                    stmPost.setString(11, postData.get("forum").getAsString());
                    idPost = exec.execUpdateAndReturnId(stmPost);
                }catch (SQLException e) {
                    throw new SQLException();
                }
            }else {
                idPost = -1;
                stmThread.close();
            }
            return idPost;

        }catch (SQLException e) {
            throw new SQLException();
        }
    }

    public JsonObject postDetails(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("SELECT p.date, p.dislikes, p.id, p.isApproved, p.isDeleted, p.isEdited, " +
                "p.isHighlighted, p.isSpam, p.likes, p.message, p.parent, p.points, " +
                "p.forum, p.thread, p.user FROM Post p WHERE p.id = ?");
        int id = query.get("post").getAsInt() ;
        stm.setInt(1, id);
        ResultSet resultSet = stm.executeQuery();
        JsonObject response = new JsonObject();
        String related;

        if (id > 0) {
            if (resultSet.next()) {
                response.addProperty("date", resultSet.getString("p.date"));
                response.addProperty("dislikes", resultSet.getInt("p.dislikes"));
                response.addProperty("id", resultSet.getInt("p.id"));
                response.addProperty("isApproved", resultSet.getBoolean("p.isApproved"));
                response.addProperty("isDeleted", resultSet.getBoolean("p.isDeleted"));
                response.addProperty("isEdited", resultSet.getBoolean("p.isEdited"));
                response.addProperty("isHighlighted", resultSet.getBoolean("p.isHighlighted"));
                response.addProperty("isSpam", resultSet.getBoolean("p.isSpam"));
                response.addProperty("likes", resultSet.getInt("p.likes"));
                response.addProperty("message", resultSet.getString("p.message"));
                response.addProperty("parent", resultSet.getString("p.parent") == null ? null : resultSet.getInt("p.parent"));
                response.addProperty("points", resultSet.getInt("p.points"));

                if (query.get("related") != null) {
                    related = query.get("related").getAsString();

                    if (related.contains("forum")) {
                        JsonObject forumQuery = new JsonObject();
                        forumQuery.addProperty("forum", resultSet.getString("p.forum"));
                        response.add("forum", forumDetails(forumQuery));
                    } else response.addProperty("forum", resultSet.getString("p.forum"));

                    if (related.contains("thread")) {
                        JsonObject threadQuery = new JsonObject();
                        threadQuery.addProperty("thread", resultSet.getInt("p.thread"));
                        response.add("thread", threadDetails(threadQuery));
                    } else response.addProperty("thread", resultSet.getInt("p.thread"));

                    if (related.contains("user")) {
                        JsonObject userQuery = new JsonObject();
                        userQuery.addProperty("user", resultSet.getString("p.user"));
                        response.add("user", userDetails(userQuery));
                    } else response.addProperty("user", resultSet.getString("p.user"));

                } else {
                    response.addProperty("forum", resultSet.getString("p.forum"));
                    response.addProperty("thread", resultSet.getInt("p.thread"));
                    response.addProperty("user", resultSet.getString("p.user"));
                }

            }
        } else response.addProperty("exception", "not found");
        resultSet.close();
        stm.close();

        return response;
    }

    public JsonObject postRemove(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE Post p INNER JOIN Thread t ON p.thread = t.id SET p.isDeleted = true, " +
                "t.posts = t.posts - 1 WHERE p.id = ? AND p.isDeleted = false");
        int id = query.get("post").getAsInt();
        if (id > 0) {
            stm.setInt(1, id);
            stm.executeUpdate();
        } else query.addProperty("exception", "not found");
        stm.close();

        return query;
    }

    public JsonObject postRestore(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE Post p INNER JOIN Thread t ON p.thread = t.id " +
                "SET p.isDeleted = false, t.posts = t.posts + 1 WHERE p.id = ? AND p.isDeleted = true");
        int id = query.get("post").getAsInt();
        if (id > 0) {
            stm.setInt(1, id);
            stm.executeUpdate();
        }else query.addProperty("exception", "not found");
        stm.close();

        return query;
    }

    public JsonObject postUpdate(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE Post SET message = ? WHERE id = ?");
        int id = Integer.valueOf( query.get("post").getAsString() );
        stm.setString(1, query.get("message").getAsString() );
        if (id >= 0) {
            stm.setInt(2, id);
            stm.executeUpdate();
        } else query.addProperty("exception", "not found");
        stm.close();

        return query;
    }

    public JsonObject postVote(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE Post SET likes = likes + ?, dislikes = dislikes - ?, " +
                "points = likes - dislikes WHERE id = ? AND isDeleted = false");
        int id = Integer.valueOf( query.get("post").getAsString() );
        if ( Integer.valueOf( query.get("vote").getAsString() ) > 0 ) {
            stm.setString(1, query.get("vote").getAsString() );
            stm.setString(2, "0");
        } else {
            stm.setString(1, "0" );
            stm.setString(2, query.get("vote").getAsString());
        }

        stm.setInt(3, id);
        stm.executeUpdate();
        stm.close();

        return postDetails(query);
    }

    public Object postList(JsonObject query) throws SQLException {
        String querySqlmass[] = { "SELECT p.id FROM Post p WHERE p.forum = ? AND p.date > ? ORDER BY p.date DESC LIMIT ?",
                "SELECT p.id FROM Post p WHERE p.forum = ? AND p.date > ? ORDER BY p.date ASC LIMIT ?",
                "SELECT p.id FROM Post p WHERE p.thread = ? AND p.date > ? ORDER BY p.date DESC LIMIT ?",
                "SELECT p.id FROM Post p WHERE p.thread = ? AND p.date > ? ORDER BY p.date ASC LIMIT ?"};
        String querySql;
        JsonObject detalisPostQuery = new JsonObject();
        JsonArray postArray = new JsonArray();
        Boolean isForum = query.get("forum") != null;
        if (query.get("order") != null) {
            if (isForum) {
                querySql = query.get("order").getAsString().equals("asc") ? querySqlmass[1] : querySqlmass[0];
            }else querySql = query.get("order").getAsString().equals("asc") ? querySqlmass[2] : querySqlmass[2];
        } else if (isForum) {
            querySql = querySqlmass[0];
        }else querySql = querySqlmass[2];
        PreparedStatement stm = connection.prepareStatement(querySql);
        stm.setString(1, isForum ? query.get("forum").getAsString() : query.get("thread").getAsString());
        stm.setString(2, query.get("since") != null ? query.get("since").getAsString() : "1970-01-01 00:00:00");
        stm.setInt(3, query.get("limit") != null ? query.get("limit").getAsInt() : 10);
        ResultSet postsResultSet = stm.executeQuery();

        while ( postsResultSet.next() ){
            detalisPostQuery.addProperty("post", postsResultSet.getString("p.id"));
            postArray.add(postDetails(detalisPostQuery));
        }
        postsResultSet.close();
        stm.close();

        return postArray;
    }



    public int createUser(JsonObject userData) throws SQLException {
        SimpleExecutor exec = new SimpleExecutor();
        int id;
        try (PreparedStatement stm = connection.prepareStatement("INSERT INTO User (`isAnonymous`, `username`, `about`," +
                "`name`, `email`) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS) ) {
            stm.setBoolean(1, userData.get("isAnonymous").getAsBoolean());
            stm.setString(2, safelyGetStringFromJson(userData, "username"));
            stm.setString(3, safelyGetStringFromJson(userData, "about"));
            stm.setString(4, safelyGetStringFromJson(userData, "name"));
            stm.setString(5, safelyGetStringFromJson(userData, "email")); //userData.get("email").getAsString());
            try {
                id = exec.execUpdateAndReturnId(stm);
            } catch (MySQLIntegrityConstraintViolationException e) {
                id = -1;
                stm.close();
            }

            return id;
        }catch (SQLException e) {
            throw new SQLException();
        }
    }

    public JsonObject userDetails(JsonObject query) throws SQLException {
        JsonObject jUser = new JsonObject();
        ArrayList<String> temp = new ArrayList<String>();
        String userEmail = query.get("user").getAsString();
        //System.out.println(userEmail);

        PreparedStatement stmUser = connection.prepareStatement("SELECT * FROM User WHERE email = ? ");
        stmUser.setString(1, userEmail);
        ResultSet User = stmUser.executeQuery();

        if(User.next()) {
            PreparedStatement stmFollowers = connection.prepareStatement("SELECT * From User_followers WHERE user = ?");
            stmFollowers.setString(1, userEmail);
            ResultSet followers = stmFollowers.executeQuery();

            while (followers.next()) {
                temp.add(followers.getString("followers"));
            }
            jUser.addProperty("about", User.getString("about"));
            jUser.addProperty("email", User.getString("email"));
            jUser.add("followers", gson.toJsonTree(temp));

            PreparedStatement stmFollowing = connection.prepareStatement("SELECT * From User_followers WHERE followers = ?");
            stmFollowing.setString(1, userEmail);
            ResultSet following = stmFollowing.executeQuery();
            temp.clear();

            while (following.next()) {
                temp.add(following.getString("user"));
            }
            jUser.add("following", gson.toJsonTree(temp));
            jUser.addProperty("id", User.getLong("id"));
            jUser.addProperty("isAnonymous", User.getBoolean("isAnonymous"));
            jUser.addProperty("name", User.getString("name"));

            PreparedStatement stmSubscriptions = connection.prepareStatement("SELECT * From Thread_followers WHERE follower_email = ?");
            stmSubscriptions.setString(1, userEmail);
            ResultSet subscriptions = stmSubscriptions.executeQuery();
            ArrayList<Integer> temp2 = new ArrayList<Integer>();

            while (subscriptions.next()) {
                temp2.add(subscriptions.getInt("thread_id"));
            }
            jUser.add("subscriptions", gson.toJsonTree(temp2));
            jUser.addProperty("username", User.getString("username"));

            followers.close();
            stmFollowers.close();
            following.close();
            stmFollowing.close();
            subscriptions.close();
            stmSubscriptions.close();
        }
        User.close();
        stmUser.close();
        return jUser;
    }

    public JsonObject userFollow(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("INSERT INTO User_followers (`user`, `followers`) VALUES (?, ?)");
        String followee = query.get("followee").getAsString();
        stm.setString(1, followee);
        stm.setString(2, query.get("follower").getAsString());
        stm.executeUpdate();
        stm.close();
        JsonObject detalis = new JsonObject();
        detalis.addProperty("user" ,followee);
        return userDetails(detalis);
    }

    public JsonObject userUnfollow(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("DELETE FROM User_followers WHERE user = ? AND followers = ?");
        String followee = query.get("followee").getAsString();
        stm.setString(1, followee);
        stm.setString(2, query.get("follower").getAsString());
        stm.executeUpdate();
        stm.close();
        JsonObject detalis = new JsonObject();
        detalis.addProperty("user", followee);
        return userDetails(detalis);
    }

    public JsonObject userUpdate(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE User SET about = ?, name = ? WHERE email = ?");
        stm.setString(1, query.get("about").getAsString());
        stm.setString(2, query.get("name").getAsString());
        stm.setString(3, query.get("user").getAsString());
        stm.executeUpdate();
        stm.close();

        return userDetails(query);
    }

    public Object userListFollowers(JsonObject query) throws SQLException {
        String querySqlmass[] = { "SELECT uf.followers FROM User_followers uf LEFT JOIN User u ON uf.followers = u.email WHERE uf.user = ? " +
                "AND u.id >= ? ORDER BY u.name DESC LIMIT ?",
                "SELECT uf.followers FROM User_followers uf LEFT JOIN User u ON uf.followers = u.email WHERE uf.user = ? " +
                        "AND u.id >= ? ORDER BY u.name ASC LIMIT ?"};
        String querySql;
        JsonObject detalisUserQuery = new JsonObject();
        JsonArray usersArray = new JsonArray();
        if (query.get("order") != null) {
            querySql = query.get("order").getAsString().equals("asc") ? querySqlmass[1] : querySqlmass[0];
        } else  querySql = querySqlmass[0];
        PreparedStatement stm = connection.prepareStatement(querySql);
        stm.setString(1, query.get("user").getAsString());
        stm.setInt(2, query.get("since_id") != null ? query.get("since_id").getAsInt() : 0);
        stm.setInt(3, query.get("limit") != null ? query.get("limit").getAsInt() : 10);
        ResultSet usersResultSet = stm.executeQuery();

        while ( usersResultSet.next() ){
            detalisUserQuery.addProperty("user", usersResultSet.getString("uf.followers"));
            usersArray.add(userDetails(detalisUserQuery));
        }
        usersResultSet.close();
        stm.close();

        return usersArray;
    }

    public Object userListFollowing(JsonObject query) throws SQLException {
        String querySqlmass[] = { "SELECT uf.user FROM User_followers uf LEFT JOIN User u ON uf.followers = u.email WHERE uf.followers = ? " +
                "AND u.id >= ? ORDER BY u.name DESC LIMIT ?",
                "SELECT uf.user FROM User_followers uf LEFT JOIN User u ON uf.followers = u.email WHERE uf.followers = ? " +
                        "AND u.id >= ? ORDER BY u.name ASC LIMIT ?"};
        String querySql;
        JsonObject detalisUserQuery = new JsonObject();
        JsonArray usersArray = new JsonArray();
        if (query.get("order") != null) {
            querySql = query.get("order").getAsString().equals("asc") ? querySqlmass[1] : querySqlmass[0];
        } else  querySql = querySqlmass[0];
        PreparedStatement stm = connection.prepareStatement(querySql);
        stm.setString(1, query.get("user").getAsString());
        stm.setInt(2, query.get("since_id") != null ? query.get("since_id").getAsInt() : 0);
        stm.setInt(3, query.get("limit") != null ? query.get("limit").getAsInt() : 10);
        ResultSet usersResultSet = stm.executeQuery();

        while ( usersResultSet.next() ){
            detalisUserQuery.addProperty("user", usersResultSet.getString("uf.user"));
            usersArray.add(userDetails(detalisUserQuery));
        }
        usersResultSet.close();
        stm.close();

        return usersArray;
    }

    public Object userListPosts(JsonObject query) throws SQLException {
        String querySqlmass[] = { "SELECT p.id FROM Post p WHERE p.user = ? AND p.date > ? ORDER BY p.date DESC LIMIT ?",
                "SELECT p.id FROM Post p WHERE p.user = ? AND p.date > ? ORDER BY p.date ASC LIMIT ?"};
        String querySql;
        JsonObject detalisPostQuery = new JsonObject();
        JsonArray postArray = new JsonArray();
        if (query.get("order") != null) {
            querySql = query.get("order").getAsString().equals("asc") ? querySqlmass[1] : querySqlmass[0];
        } else  querySql = querySqlmass[0];
        PreparedStatement stm = connection.prepareStatement(querySql);
        stm.setString(1, query.get("user").getAsString());
        stm.setString(2, query.get("since") != null ? query.get("since").getAsString() : "1970-01-01 00:00:00");
        stm.setInt(3, query.get("limit") != null ? query.get("limit").getAsInt() : 10);
        ResultSet postsResultSet = stm.executeQuery();

        while ( postsResultSet.next() ){
            detalisPostQuery.addProperty("post", postsResultSet.getString("p.id"));
            if (query.get("related") != null) detalisPostQuery.addProperty("related", query.get("related").getAsString());
            postArray.add(postDetails(detalisPostQuery));
        }
        postsResultSet.close();
        stm.close();

        return postArray;
    }



    public int createThread(JsonObject threadData) throws SQLException {
        SimpleExecutor exec = new SimpleExecutor();
        try (PreparedStatement stm = connection.prepareStatement("INSERT INTO Thread (`forum`, `title`, `isClosed`," +
                "`user`, `date`, `message`, `slug`, `isDeleted`) VALUES (?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS) ) {
            stm.setString(1, threadData.get("forum").getAsString());
            stm.setString(2, threadData.get("title").getAsString());
            stm.setBoolean(3, threadData.get("isClosed").getAsBoolean());
            stm.setString(4, threadData.get("user").getAsString());
            stm.setString(5, threadData.get("date").getAsString());
            stm.setString(6, threadData.get("message").getAsString());
            stm.setString(7, threadData.get("slug").getAsString());
            stm.setBoolean(8, threadData.get("isDeleted").getAsBoolean());

            return exec.execUpdateAndReturnId(stm);
        }catch (SQLException e) {
            throw new SQLException();
        }
    }

    public JsonObject threadDetails(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("SELECT `id`,`forum`,`title`,`user`,`date`,`isClosed`,`isDeleted`,`message`,`slug`,`likes`, `points`, `posts`, `dislikes` FROM `Thread` WHERE id=?");
        stm.setInt(1, Integer.valueOf(query.get("thread").getAsString()) );
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
            response.addProperty("dislikes", threadDefault.getInt("dislikes"));

            if (query.get("related") != null) {
                related = query.get("related").getAsString();

                if (related.contains("thread")) {
                    response.addProperty("exception", "invalid query");
                }else {

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
                }

            } else {
                response.addProperty("user", threadDefault.getString("user"));
                response.addProperty("forum", threadDefault.getString("forum"));
            }

        }

        return response;
    }

    public JsonObject threadUpdate(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE Thread SET message = ?, slug = ? WHERE id = ? AND isDeleted = false");
        int id = Integer.valueOf( query.get("thread").getAsString() );
        stm.setString(1, query.get("message").getAsString() );
        stm.setString(2, query.get("slug").getAsString() );
        if (id >= 0) {
            stm.setInt(3, id);
            stm.executeUpdate();
        } else query.addProperty("exception", "not found");
        stm.close();

        return threadDetails(query);
    }

    public JsonObject threadClose(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE Thread SET isClosed = true WHERE id = ?");
        int id = Integer.valueOf( query.get("thread").getAsString() );
        if (id >= 0) {
            stm.setInt(1, id);
            stm.executeUpdate();
        } else query.addProperty("exception", "not found");
        stm.close();

        return query;
    }

    public JsonObject threadOpen(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE Thread SET isClosed = false WHERE id = ?");
        int id = query.get("thread").getAsInt();
        if (id >= 0) {
            stm.setInt(1, id);
            stm.executeUpdate();
        } else query.addProperty("exception", "not found");
        stm.close();

        return query;
    }

    public JsonObject threadRemove(JsonObject query) throws SQLException {
        int id = query.get("thread").getAsInt();
        if (id >= 0) {
            PreparedStatement stm = connection.prepareStatement("UPDATE Thread SET isDeleted = true, posts = 0 WHERE id = ?");
            stm.setInt(1, id);
            stm.executeUpdate();
            stm.close();
            stm = connection.prepareStatement("UPDATE Post SET isDeleted = true WHERE thread = ?");
            stm.setInt(1, id);
            stm.executeUpdate();
            stm.close();
        } else query.addProperty("exception", "not found");

        return query;
    }

    public JsonObject threadRestore(JsonObject query) throws SQLException {
        int id = query.get("thread").getAsInt();
        int posts;
        if (id >= 0) {
            PreparedStatement stm = connection.prepareStatement("UPDATE Post SET isDeleted = false WHERE thread = ?");
            stm.setInt(1, id);
            posts = stm.executeUpdate();
            stm.close();
            stm = connection.prepareStatement("UPDATE Thread SET isDeleted = false, posts = ? WHERE id = ?");
            stm.setInt(1, posts);
            stm.setInt(2, id);
            stm.executeUpdate();
            stm.close();
        } else query.addProperty("exception", "not found");

        return query;
    }

    public JsonObject threadVote(JsonObject query) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("UPDATE Thread SET likes = likes + ?, dislikes = dislikes - ?, " +
                "points = likes - dislikes WHERE id = ?");
        int id = query.get("thread").getAsInt();
        if ( !query.get("vote").toString().equals("null") ) {
            if ( query.get("vote").getAsInt()  > 0 ) {
                stm.setString(1, query.get("vote").getAsString() );
                stm.setString(2, "0");
            } else {
                stm.setString(1, "0" );
                stm.setString(2, query.get("vote").getAsString());
            }

            stm.setInt(3, id);
            stm.executeUpdate();
        }
        stm.close();

        return threadDetails(query);
    }

    public JsonObject threadSubscribe(JsonObject query) throws SQLException {
        int id = query.get("thread").getAsInt();
        if ( id > 0) {
            PreparedStatement stm = connection.prepareStatement("INSERT INTO Thread_followers(thread_id, follower_email) " +
                    "VALUES (?,?)");
            stm.setInt(1, id);
            stm.setString(2, safelyGetStringFromJson(query, "user"));
            stm.executeUpdate();
            return query;
        } else {
            query.addProperty("exception", "not found");
            return query;
        }
    }

    public JsonObject threadUnsubscribe(JsonObject query) throws SQLException {
        int id = query.get("thread").getAsInt();
        if ( id > 0) {
            PreparedStatement stm = connection.prepareStatement("DELETE FROM Thread_followers WHERE thread_id = ? AND follower_email = ? ");
            stm.setInt(1, id);
            stm.setString(2, safelyGetStringFromJson(query, "user"));
            stm.executeUpdate();
            return query;
        } else {
            query.addProperty("exception", "not found");
            return query;
        }
    }

    public Object threadList(JsonObject query) throws SQLException {
        String querySqlmass[] = { "SELECT t.id FROM Thread t WHERE t.forum = ? AND t.date > ? ORDER BY t.date DESC LIMIT ?",
                "SELECT t.id FROM Thread t WHERE t.forum = ? AND t.date > ? ORDER BY t.date ASC LIMIT ?",
                "SELECT t.id FROM Thread t WHERE t.user = ? AND t.date > ? ORDER BY t.date DESC LIMIT ?",
                "SELECT t.id FROM Thread t WHERE t.user = ? AND t.date > ? ORDER BY t.date ASC LIMIT ?"};
        String querySql;
        JsonObject detalisThreadQuery = new JsonObject();
        JsonArray threadArray = new JsonArray();
        Boolean isForum = (query.get("forum") != null);
        if (query.get("order") != null) {
            if (isForum) {
                querySql = query.get("order").getAsString().equals("asc") ? querySqlmass[1] : querySqlmass[0];
            } else querySql = query.get("order").getAsString().equals("asc") ? querySqlmass[3] : querySqlmass[2];
        } else {
            if (isForum) {
                querySql = querySqlmass[0];
            }else querySql = querySqlmass[3];
        }
        PreparedStatement stm = connection.prepareStatement(querySql);
        stm.setString(1, isForum ? query.get("forum").getAsString() : query.get("user").getAsString());
        stm.setString(2, query.get("since") != null ? query.get("since").getAsString() : "1970-01-01 00:00:00");
        stm.setInt(3, query.get("limit") != null ? query.get("limit").getAsInt() : 10);
        ResultSet threadResultSet = stm.executeQuery();

        while ( threadResultSet.next() ){
            detalisThreadQuery.addProperty("thread", threadResultSet.getString("t.id"));
            if (query.get("related") != null) detalisThreadQuery.addProperty("related", query.get("related").getAsString());
            threadArray.add(threadDetails(detalisThreadQuery));
        }
        threadResultSet.close();
        stm.close();

        return threadArray;
    }

    public Object threadListPosts(JsonObject query) throws SQLException {
        String querySqlmass[] = { "SELECT p.id FROM Post p WHERE p.thread = ? AND p.date > ? ORDER BY p.id DESC LIMIT ?",
                "SELECT p.id FROM Post p WHERE p.thread = ? AND p.date > ? ORDER BY p.id ASC LIMIT ?"};
        String querySql;
        JsonObject detalisPostQuery = new JsonObject();
        JsonArray postArray = new JsonArray();
        if (query.get("order") != null) {
            querySql = query.get("order").getAsString().equals("asc") ? querySqlmass[1] : querySqlmass[0];
        } else  querySql = querySqlmass[0];
        PreparedStatement stm = connection.prepareStatement(querySql);
        stm.setString(1, query.get("thread").getAsString());
        stm.setString(2, query.get("since") != null ? query.get("since").getAsString() : "1970-01-01 00:00:00");
        stm.setInt(3, query.get("limit") != null ? query.get("limit").getAsInt() : 10);
        ResultSet postsResultSet = stm.executeQuery();

        while ( postsResultSet.next() ){
            detalisPostQuery.addProperty("post", postsResultSet.getString("p.id"));
            postArray.add(postDetails(detalisPostQuery));
        }
        postsResultSet.close();
        stm.close();

        return postArray;
    }

    public String clear() throws SQLException {
        JsonObject response = new JsonObject();
        Statement stm = connection.createStatement();
        stm.execute("TRUNCATE Forum");
        stm.execute("TRUNCATE User");
        stm.execute("TRUNCATE Thread");
        stm.execute("TRUNCATE Post");
        stm.execute("TRUNCATE User_followers");
        stm.execute("TRUNCATE Thread_followers");
        stm.close();

        response.addProperty("code", 0);
        response.addProperty("response", "ok");

        return response.toString();
    }

    // вспомогательные функции

    public String safelyGetStringFromJson(JsonObject json, String name) {
        if (json.get(name) != null) {
            if (json.get(name).toString().equals("null")) {
                return null;
            } else return json.get(name).getAsString();
        } else return null;
    }

    public JsonArray userDetailsFromResultSet (ResultSet usersResultSet) throws SQLException {
        long time = System.currentTimeMillis();
        PreparedStatement stmFollowers, stmFollowing, stmSubscription;
        stmFollowers = connection.prepareStatement("SELECT * From User_followers WHERE user = ?");
        stmFollowing = connection.prepareStatement("SELECT * From User_followers WHERE followers = ?");
        stmSubscription = connection.prepareStatement("SELECT * From Thread_followers WHERE follower_email = ?");
        ResultSet resultSetFollowers, resultSetFollowing, resultSetSubscription;
        ArrayList<String> temp = new ArrayList<String>();
        ArrayList<Integer> temp2 = new ArrayList<Integer>();
        JsonArray jsonArrayResponse = new JsonArray();

        while ( usersResultSet.next() ){
            JsonObject jUser = new JsonObject();

            stmFollowers.setString(1, usersResultSet.getString("u.email"));
            resultSetFollowers = stmFollowers.executeQuery();
            while (resultSetFollowers.next()) {
                temp.add(resultSetFollowers.getString("followers"));
            }
            resultSetFollowers.close();
            stmFollowers.clearParameters();

            jUser.addProperty("about", usersResultSet.getString("u.about"));
            jUser.addProperty("email", usersResultSet.getString("u.email"));
            jUser.add("followers", gson.toJsonTree(temp));


            stmFollowing.setString(1, usersResultSet.getString("u.email"));
            resultSetFollowing = stmFollowing.executeQuery();
            temp.clear();
            while (resultSetFollowing.next()) {
                temp.add(resultSetFollowing.getString("user"));
            }
            resultSetFollowing.close();
            stmFollowing.clearParameters();


            jUser.add("following", gson.toJsonTree(temp));
            jUser.addProperty("id", usersResultSet.getLong("u.id"));
            jUser.addProperty("isAnonymous", usersResultSet.getBoolean("u.isAnonymous"));
            jUser.addProperty("name", usersResultSet.getString("u.name"));


            stmSubscription.setString(1, usersResultSet.getString("u.email"));
            resultSetSubscription = stmSubscription.executeQuery();
            temp2.clear();
            while (resultSetSubscription.next()) {
                temp2.add(resultSetSubscription.getInt("thread_id"));
            }
            resultSetSubscription.close();
            stmSubscription.clearParameters();


            jUser.add("subscriptions", gson.toJsonTree(temp2));
            jUser.addProperty("username", usersResultSet.getString("u.username"));
            jsonArrayResponse.add(jUser);
        }

        stmFollowers.close();
        stmFollowing.close();
        stmSubscription.close();
        System.out.println("Время сборки listUser  " + (System.currentTimeMillis() - time) );
        return jsonArrayResponse;
    }

}
