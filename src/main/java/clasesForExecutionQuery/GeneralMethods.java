package clasesForExecutionQuery;

import com.google.gson.JsonObject;

import java.sql.SQLException;

/**
 * Created by narek on 01.11.14.
 */
public interface GeneralMethods {

    public String delegationCall(String method, JsonObject data) throws SQLException;
}
