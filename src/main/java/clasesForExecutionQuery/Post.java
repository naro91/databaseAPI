package clasesForExecutionQuery;


import com.google.gson.Gson;
import database.Database;

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

    private String create(String data) {
        return "ok";
    }

    private String details(String query) {
        return "ok";
    }

    private String list(String query) {
        return "ok";
    }

    private String remove(String query) {
        return "ok";
    }

    private String restore(String query) {
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
