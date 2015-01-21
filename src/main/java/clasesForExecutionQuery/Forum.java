package clasesForExecutionQuery;


import com.google.gson.Gson;

import com.google.gson.JsonObject;
import database.Database;
import util.JsonResponse;

import java.sql.SQLException;


/**
 * Created by Abovyan Narek on 01.11.14.
 */
public class Forum implements GeneralMethods {

    Database database;
    Gson gson;

    public Forum(Database database, Gson gson) {
        this.database = database;
        this.gson = gson;
    }

    private String create(JsonObject forumData) {
        try {
            //System.out.println("Это форум  ");
            int id = database.createForum(forumData);
            forumData.addProperty("id", id);
            return JsonResponse.createResponse(forumData);

        } catch (SQLException e) {
            e.printStackTrace();
            forumData.addProperty("exception", "An unknown error");
            return JsonResponse.createResponse(forumData);
        }
    }

    private String details(JsonObject query) throws SQLException {
        //System.out.println("Это детали форум  " + query);
        try {
            return JsonResponse.createResponse(database.forumDetails(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String listPosts(JsonObject query) throws SQLException {
        //System.out.println("Это Forum ListPost  " + query);
        try {
            return JsonResponse.createResponse(database.forumListPost(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String listThreads(JsonObject query) throws SQLException {
        //System.out.println("Это Forum ListThread  " + query);
        try {
            return JsonResponse.createResponse(database.forumListThread(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String listUsers(JsonObject query) throws SQLException {
        //System.out.println("Это Forum listUser");
        //System.out.println("Это запрос   " + query.toString());
        try {
            return JsonResponse.createResponse(database.forumListUsers(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
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
