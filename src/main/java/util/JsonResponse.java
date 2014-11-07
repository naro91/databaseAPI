package util;

import com.google.gson.JsonObject;

/**
 * Created by narek on 06.11.14.
 */
public class JsonResponse {
    public static String createResponse(JsonObject data) {
        JsonObject requestResult = new JsonObject();

        requestResult.addProperty("code", 0);
        requestResult.add("response", data);
        return requestResult.toString();
    }
}
