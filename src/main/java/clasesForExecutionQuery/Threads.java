package clasesForExecutionQuery;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.Database;
import util.JsonResponse;

import java.sql.SQLException;

/**
 * Created by narek on 01.11.14.
 */
public class Threads implements GeneralMethods{

    Database database;
    Gson gson;

    public Threads(Database database, Gson gson){
        this.database = database;
        this.gson = gson;
    }

    private String create(JsonObject threadData) {
        try {
            System.out.println("это Thread");
            System.out.println(threadData);
            int id = database.createThread(threadData);
            threadData.addProperty("id", id);
            return JsonResponse.createResponse(threadData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "bad";
    }

    private String close(JsonObject query) {
        return "ok";
    }


    private String details(JsonObject query) {
        return "ok";
    }

    private String list(JsonObject query) {
        return "ok";
    }

    private String listPosts(JsonObject query) {
        return "ok";
    }

    private String open(JsonObject query) {
        return "ok";
    }

    private String remove(JsonObject query) {
        return "ok";
    }

    private String restore(JsonObject query) {
        return "ok";
    }

    private String subscribe(JsonObject query) {
        return "ok";
    }

    private String unsubscribe(JsonObject query) {
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
            case "close":
                return close(data);
            case "create":
                return create(data);
            case "details":
                return details(data);
            case "list":
                return list(data);
            case "listPosts":
                return listPosts(data);
            case "open":
                return open(data);
            case "remove":
                return remove(data);
            case "restore":
                return restore(data);
            case "subscribe":
                return subscribe(data);
            case "unsubscribe":
                return unsubscribe(data);
            case "update":
                return update(data);
            case "vote":
                return vote(data);
            default: return null;
        }
    }
}
