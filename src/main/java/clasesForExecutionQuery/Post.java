package clasesForExecutionQuery;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.Database;
import util.JsonResponse;

import java.sql.SQLException;

/**
 * Created by Abovyan Narek on 01.11.14.
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
            //System.out.println("Это Post create   " + postData.toString());
            //System.out.println(postData);
            int id = database.createPost(postData);
            if (id != -1) {
                postData.addProperty("id", id);
            } else postData.addProperty("exception", "invalid query");

            return JsonResponse.createResponse(postData);

        } catch (SQLException e) {
            //e.printStackTrace();
            postData.addProperty("exception", "An unknown error");
            return JsonResponse.createResponse(postData);
        }catch (NullPointerException e) {
            postData.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(postData);
        }
    }

    private String details(JsonObject query) throws SQLException {
        //System.out.println("Это Post details   " + query.toString());
        //System.out.println(query);
        try {
            return JsonResponse.createResponse(database.postDetails(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String list(JsonObject query) throws SQLException {
        //System.out.println("Это Post List   " + query.toString());
        try {
            return JsonResponse.createResponse(database.postList(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String remove(JsonObject query) throws SQLException {
        //System.out.println("Это Post remove   " + query.toString());
        try {
            return JsonResponse.createResponse(database.postRemove(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String restore(JsonObject query) throws SQLException {
        //System.out.println("Это Post restore    " + query.toString());
        try {
            return JsonResponse.createResponse(database.postRestore(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String update(JsonObject query) throws SQLException {
        //System.out.println("Это Post update    " + query.toString());
        try {
            return JsonResponse.createResponse(database.postUpdate(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String vote(JsonObject query) throws SQLException {
        //System.out.println("Это Post vote  " + query.toString());
        try {
            return JsonResponse.createResponse(database.postVote(query));
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
