package util;

import com.google.gson.JsonObject;

/**
 * Created by narek on 06.11.14.
 */
public class JsonResponse {
    public static String createResponse(JsonObject data) {
        JsonObject requestResult = new JsonObject();
        if (data.get("exception") == null) {
            requestResult.addProperty("code", 0);
            requestResult.add("response", data);
        } else {
            switch ( data.get("exception").getAsString() ) {
                case "not found" :
                    requestFailBuilder(requestResult, 1, "the requested object was not found");
                    break;
                case "invalid query" :
                    requestFailBuilder(requestResult, 3, "invalid query");
                    break;
                case "user already exists" :
                    requestFailBuilder(requestResult, 5, "user already exists");
                    break;
                default: requestFailBuilder(requestResult, 4, "An unknown error");
            }
        }
        return requestResult.toString();
    }

    private static void requestFailBuilder (JsonObject requestResult, int code, String response) {
        requestResult.addProperty("code", code);
        requestResult.addProperty("response", response);
    }
}
