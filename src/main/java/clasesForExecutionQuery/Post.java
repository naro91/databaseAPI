package clasesForExecutionQuery;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.Database;
import util.JsonResponse;

import java.sql.SQLException;

/**
 * Created by narek on 01.11.14.
 */
public class Post implements GeneralMethods{

    Database database;
    Gson gson;

    public Post(Database database, Gson gson){
        this.database = database;
        this.gson = gson;
    }

    private String create(JsonObject postData) {
        try {
            System.out.println("Это пост");
            System.out.println(postData);
            int id = database.createPost(postData);
            postData.addProperty("id", id);
            postData.addProperty("parent", "null");
            return JsonResponse.createResponse(postData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "bad";
    }

    private String details(JsonObject query) {
        return "ok";
    }

    private String list(JsonObject query) {
        return "ok";
    }

    private String remove(JsonObject query) {
        return "ok";
    }

    private String restore(JsonObject query) {
        return "ok";
    }

    private String update(JsonObject query) {
        return "ok";
    }

    private String vote(JsonObject query) {
        return "ok";
    }

    @Override
    public String delegationCall (String method, JsonObject data) {
        switch (method) {
            case "create":
                return create(data);
            case "details":
                return details(data);
            case "list":
                return list(data);
            case "remove":
                return remove(data);
            case "restore":
                return restore(data);
            case "update":
                return update(data);
            case "vote":
                return vote(data);
            default: return null;
        }

    }
}
