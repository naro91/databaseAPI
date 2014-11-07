package clasesForExecutionQuery;

import com.google.gson.JsonObject;

/**
 * Created by narek on 01.11.14.
 */
public class General implements GeneralMethods{

    private String clear() {
        return "ok";
    }

    @Override
    public String delegationCall (String method, JsonObject data) {
        return "ok";
    }

}
