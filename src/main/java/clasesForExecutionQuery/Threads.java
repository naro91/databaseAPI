package clasesForExecutionQuery;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.Database;
import util.JsonResponse;

import java.sql.SQLException;

/**
 * Created by Abovyan Narek on 01.11.14.
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
            //System.out.println("это Thread   " + threadData.toString() );
            //System.out.println(threadData);
            int id = database.createThread(threadData);
            threadData.addProperty("id", id);
            return JsonResponse.createResponse(threadData);
        } catch (SQLException e) {
            e.printStackTrace();
            threadData.addProperty("exception", "An unknown error");
            return JsonResponse.createResponse(threadData);
        }catch (NullPointerException e) {
            return JsonResponse.createResponse(threadData);
        }
    }

    private String close(JsonObject query) throws SQLException {
        //System.out.println("Это Thread Close   " + query.toString());
        try {
            return JsonResponse.createResponse(database.threadClose(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }


    private String details(JsonObject query) throws SQLException {
        //System.out.println("Это Thread details   " + query.toString());
        try {
            return JsonResponse.createResponse(database.threadDetails(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String list(JsonObject query) throws SQLException {
        //System.out.println("Это Thread List    " + query.toString());
        try {
            return JsonResponse.createResponse(database.threadList(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String listPosts(JsonObject query) throws SQLException {
        //System.out.println("Это Thread List   " + query.toString());
        try {
            return JsonResponse.createResponse(database.threadListPosts(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String open(JsonObject query) throws SQLException {
        //System.out.println("Это Thread open   " + query.toString());
        try {
            return JsonResponse.createResponse(database.threadOpen(query));
        } catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String remove(JsonObject query) throws SQLException {
        //System.out.println("Это Thread Remove    " + query.toString());
        try {
            return JsonResponse.createResponse(database.threadRemove(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String restore(JsonObject query) throws SQLException {
        //System.out.println("Это Thread Restore    " + query.toString());
        try {
            return JsonResponse.createResponse(database.threadRestore(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String subscribe(JsonObject query) throws SQLException {
        //System.out.println("Это Thread Subscribe    " + query.toString());
        try {
            return JsonResponse.createResponse(database.threadSubscribe(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String unsubscribe(JsonObject query) throws SQLException {
        //System.out.println("Это Thread Unsubscribe    " + query.toString());
        try {
            return JsonResponse.createResponse(database.threadUnsubscribe(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String update(JsonObject query) throws SQLException {
        //System.out.println("Это Thread Update    " + query.toString());
        try {
            return JsonResponse.createResponse(database.threadUpdate(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    private String vote(JsonObject query) throws SQLException {
        //System.out.println("Это Thread Vote   " + query.toString());
        try {
            return JsonResponse.createResponse(database.threadVote(query));
        }catch (NullPointerException e) {
            query.addProperty("exception", "invalid query");
            return JsonResponse.createResponse(query);
        }
    }

    @Override
    public String delegationCall (String method, JsonObject data) throws SQLException {

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
