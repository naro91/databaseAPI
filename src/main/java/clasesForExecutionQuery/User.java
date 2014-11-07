package clasesForExecutionQuery;


import com.google.gson.Gson;
import database.Database;
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

    private String create(String query) {
        return "Ok";
    }

    private String details(String query) {
        return "Ok";
    }

    private String follow(String query) {
        return "Ok";
    }

    private String listFollowers(String query) {
        return "Ok";
    }

    private String listFollowing(String query) {
        return "Ok";
    }

    private String listPosts(String query) {
        return "Ok";
    }

    private String unfollow(String query) {
        return "Ok";
    }

    private String updateProfile(String query) {
        return "Ok";
    }

    @Override
    public String delegationCall (String method, String data) {

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
