package clasesForExecutionQuery;


import com.google.gson.Gson;
import database.Database;

/**
 * Created by narek on 01.11.14.
 */
public class Thread implements GeneralMethods{

    Database database;
    Gson gson;

    public Thread(Database database, Gson gson){
        this.database = database;
        this.gson = gson;
    }

    private String create(String query) {
        return "ok";
    }

    private String close(String query) {
        return "ok";
    }


    private String details(String query) {
        return "ok";
    }

    private String list(String query) {
        return "ok";
    }

    private String listPosts(String query) {
        return "ok";
    }

    private String open(String query) {
        return "ok";
    }

    private String remove(String query) {
        return "ok";
    }

    private String restore(String query) {
        return "ok";
    }

    private String subscribe(String query) {
        return "ok";
    }

    private String unsubscribe(String query) {
        return "ok";
    }

    private String update(String query) {
        return "ok";
    }

    private String vote(String query) {
        return "ok";
    }

    @Override
    public String delegationCall (String method, String data) {

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
