package clasesForExecutionQuery;


import com.google.gson.Gson;

import com.google.gson.JsonObject;
import database.Database;
import util.JsonResponse;

import java.sql.SQLException;


/**
 * Created by narek on 01.11.14.
 */
public class Forum implements GeneralMethods {

    Database database;
    Gson gson;

    public Forum(Database database, Gson gson){
        this.database = database;
        this.gson = gson;
    }

    private String create(JsonObject forumData) {
        try {
            System.out.println("Это форум  ");
            int id = database.createForum(forumData);
            forumData.addProperty("id", id);

            return JsonResponse.createResponse(forumData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "bad";
    }

    private String details(JsonObject query) throws SQLException {
        System.out.println(query);
        return JsonResponse.createResponse(database.forumDetails(query));
    }

    private String listPosts(JsonObject query) {
        return "ok";
    }

    private String listThreads(JsonObject query) {
        return "ok";
    }

    private String listUsers(JsonObject query) {
        return "ok";
    }


    @Override
    public String delegationCall (String method, JsonObject data) throws SQLException {
        switch (method) {
            case "create":
                return create(data);
            case "details":
                return details(data);
            case "listPosts":
                return listPosts(data);
            case "listThreads":
                return listThreads(data);
            case "listUsers":
                return listUsers(data);
            default: return null;
        }
    }
}
