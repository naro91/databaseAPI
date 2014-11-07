package clasesForExecutionQuery;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.Database;
import util.JsonResponse;

import java.sql.SQLException;

/**
 * Created by narek on 01.11.14.
 */
public class User implements GeneralMethods{

    Database database;
    Gson gson;

    public User(Database database, Gson gson){
        this.database = database;
        this.gson = gson;
    }

    private String create(JsonObject userData) {
        try {
            System.out.println(userData);
            int id = database.createUser(userData);
            userData.addProperty("id", id);
            return JsonResponse.createResponse(userData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "bad";
    }

    private String details(JsonObject query) {
        return "Ok";
    }

    private String follow(JsonObject query) {
        return "Ok";
    }

    private String listFollowers(JsonObject query) {
        return "Ok";
    }

    private String listFollowing(JsonObject query) {
        return "Ok";
    }

    private String listPosts(JsonObject query) {
        return "Ok";
    }

    private String unfollow(JsonObject query) {
        return "Ok";
    }

    private String updateProfile(JsonObject query) {
        return "Ok";
    }

    @Override
    public String delegationCall (String method, JsonObject data) {

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
