package clasesForExecutionQuery;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.Database;
import util.JsonResponse;

import java.sql.SQLException;

/**
 * Created by Abovyan Narek on 01.11.14.
 */
public class User implements GeneralMethods{

    Database database;
    Gson gson;

    public User(Database database, Gson gson){
        this.database = database;
        this.gson = gson;
    }

    private String create(JsonObject userData) {
        int id;
        try {
            System.out.println("Это User  " + userData.toString());
            id = database.createUser(userData);
            if (id != -1) {
                userData.addProperty("id", id);
            } else userData.addProperty("exception", "user already exists");
            return JsonResponse.createResponse(userData);
        } catch (SQLException e) {
            e.printStackTrace();
            userData.addProperty("exception", "An unknown error");
            return JsonResponse.createResponse(userData);
        }
    }

    private String details(JsonObject query) throws SQLException {
        System.out.println("Это User детали  " + query.toString());
        try {
            return JsonResponse.createResponse(database.userDetails(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String follow(JsonObject query) throws SQLException {
        System.out.println("Это User Follow   " + query.toString());
        try {
            return JsonResponse.createResponse(database.userFollow(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String listFollowers(JsonObject query) throws SQLException {
        System.out.println("Это User ListFollowers   " + query.toString() );
        try {
            return JsonResponse.createResponse(database.userListFollowers(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String listFollowing(JsonObject query) throws SQLException {
        System.out.println("Это User ListFolloweing   " + query.toString());
        try {
            return JsonResponse.createResponse(database.userListFollowing(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String listPosts(JsonObject query) throws SQLException {
        System.out.println("Это User ListPost   " + query.toString() );
        try {
            return JsonResponse.createResponse(database.userListPosts(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String unfollow(JsonObject query) throws SQLException {
        System.out.println("Это User unfollow   " + query.toString());
        try {
            return JsonResponse.createResponse(database.userUnfollow(query));
        } catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String updateProfile(JsonObject query) throws SQLException {
        System.out.println("Это update user   " + query.toString());
        try {
            return JsonResponse.createResponse(database.userUpdate(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    @Override
    public String delegationCall (String method, JsonObject data) throws SQLException {

        switch (method){
            case "create":
                return create(data);
            case "details":
                return details(data);
            case "follow":
                return follow(data);
            case "listFollowers":
                return listFollowers(data);
            case "listFollowing":
                return listFollowing(data);
            case "listPosts":
                return listPosts(data);
            case "unfollow":
                return unfollow(data);
            case "updateProfile":
                return updateProfile(data);
            default: return null;
        }
    }
}
