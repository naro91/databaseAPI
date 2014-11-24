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
            System.out.println("Это Post create");
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

    private String details(JsonObject query) throws SQLException {
        System.out.println("Это Post details");
        System.out.println(query);
        return JsonResponse.createResponse(database.postDetails(query));
    }

    private String list(JsonObject query) {
        return "ok";
    }

    private String remove(JsonObject query) throws SQLException {
        System.out.println("Это Post remove");
        return JsonResponse.createResponse(database.postRemove(query));
    }

    private String restore(JsonObject query) throws SQLException {
        System.out.println("Это Post restore");
        return JsonResponse.createResponse(database.postRestore(query));
    }

    private String update(JsonObject query) throws SQLException {
        System.out.println("Это Post update");
        return JsonResponse.createResponse(database.postUpdate(query));
    }

    private String vote(JsonObject query) {
        return "ok";
    }

    @Override
    public String delegationCall (String method, JsonObject data) throws SQLException {
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
