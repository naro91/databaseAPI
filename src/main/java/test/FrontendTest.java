package test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * Created by narek on 06.11.14.
 */
public class FrontendTest extends HttpServlet {
    Gson gson = new Gson();

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {

        String json;

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        //json = getRequestParser(URLDecoder.decode(request.getQueryString(), "UTF-8"));
        //System.out.println(json);
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=utf-8");
        JsonObject status = new JsonObject();
        Gson gson = new Gson();
        String entity[] = request.getRequestURI().split("/");
        if (entity.length < 3){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }else response.setStatus(HttpServletResponse.SC_OK);
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = request.getReader().readLine()) != null) {
            sb.append(s);
        }

        System.out.println(gson.fromJson(sb.toString(), JsonObject.class));
        if (entity.length == 4) {
            if (entity[3].equals("clear")) {
                status.addProperty("code", 0);
                status.addProperty("response", "ok");
                response.getOutputStream().print(gson.toJson(status));
                response.getOutputStream().flush();
                System.out.println(gson.toJson(status));
            }
        }


    }

    public String getRequestParser ( String data ) {
        JsonObject jsonObject = new JsonObject();
        String strings[] = data.split("&");
        for(int i = 0; i < strings.length; i++) {
            String values[] = strings[i].split("=");
            jsonObject.addProperty(values[0],values[1]);
        }
        if(jsonObject.has("forum")) {
            return jsonObject.get("forum").toString();
        }else return gson.toJson(jsonObject);
    }
}
