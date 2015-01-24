package frontend;

import clasesForExecutionQuery.Forum;
import clasesForExecutionQuery.Post;
import clasesForExecutionQuery.Threads;
import clasesForExecutionQuery.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import clasesForExecutionQuery.General;
import com.google.gson.JsonSyntaxException;
import database.Database;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by Abovyan Narek on 06.11.14.
 */
public class Frontend extends HttpServlet {
    private Gson gson = new Gson();
    private String responseResult;
    JsonObject json;
    Database database = new Database();
    private Forum forum = new Forum(database, gson);
    private Post post = new Post(database, gson);
    private User user = new User(database, gson);
    private Threads thread = new Threads(database, gson);
    private General general = new General(database, gson);
    volatile private long timeG;
    volatile private long timeP;
    private  int i = 0;

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=utf-8");
        //timeG = System.currentTimeMillis();
        try {
            json = getRequestParser(URLDecoder.decode(request.getQueryString(), "UTF-8")); // получение json из get запроса
        }catch (NullPointerException e) {
            json = new JsonObject();
            json.addProperty("exception", "exception");
            json.addProperty("code", 2);
            json.addProperty("response", "invalid request");
        }
        if (json.get("exception") == null) {
            try {
                responseResult = delegationCall(request.getRequestURI().split("/"), json);  // делегирование запроса соответствующему классу и методу
                if (responseResult == null) responseResult = "{code : 3, response : \"invalid query\"}";
            } catch (SQLException e) {
                e.printStackTrace();
                //responseResult = "{code : 4, response : \"An unknown error\"}";
            }
            responseSetstatus(responseResult, response);  // установление статуса ответа в зависимости от результата выполнения запроса
            response.getOutputStream().print(responseResult); // отправка ответа
        } else {
            json.remove("exception");
            responseSetstatus(json.toString(), response);  // установление статуса ответа в зависимости от результата выполнения запроса
            response.getOutputStream().print(json.toString()); // отправка ответа
        }
        response.getOutputStream().flush();
        //timeG = System.currentTimeMillis() - timeG;
       // if (timeG > 300) {
        //    i++;
         //   System.out.println("      TIME      " + timeG);
         //   System.out.println(responseResult);
        //}
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=utf-8");
        //timeP = System.currentTimeMillis();
        json = requestGetJson(request);  // получение json из запроса
        if (json != null && json.get("exception") == null) {
            try {
                responseResult = delegationCall(request.getRequestURI().split("/"), json);  // делегирование запроса соответствующему классу и методу
                if (responseResult == null) responseResult = "{code : 3, response : \"invalid query\"}";
            } catch (SQLException e) {
                System.out.println(e);
                //responseResult = "{code : 4, response : \"An unknown error\"}";
            }
            responseSetstatus(responseResult, response);  // установление статуса ответа в зависимости от результата выполнения запроса
            response.getOutputStream().print(responseResult); // отправка ответа
        } else {
            if (json == null) {
                json = new JsonObject();
            } else json.remove("exception");
            responseSetstatus(json.toString(), response);  // установление статуса ответа в зависимости от результата выполнения запроса
            response.getOutputStream().print(json.toString()); // отправка ответа
        }
        response.getOutputStream().flush();
        //timeP = System.currentTimeMillis() - timeP;
        //if (timeP > 300) {
         //   i++;
          //  System.out.println("      TIME      " + timeP);
           // System.out.println(responseResult);
        //}
    }


    private String delegationCall (String requestMass[], JsonObject json) throws SQLException {  // функция для делегирования запроса
        if (requestMass.length == 4) {
            if (requestMass[3].equals("clear")) {
                return general.clear();
            } else {
                return "{code : 3, response : \"invalid query\"}";
            }
        } else {
            if (requestMass.length > 4) {
                switch (requestMass[3]) {
                    case "forum":
                        return forum.delegationCall(requestMass[4], json);
                    case "post":
                        return post.delegationCall(requestMass[4], json);
                    case "user":
                        return user.delegationCall(requestMass[4], json);
                    case "thread":
                        return thread.delegationCall(requestMass[4], json);
                    default: return "{code : 3, response : \"invalid query\"}";
                }
            } else return "{code : 3, response : \"invalid query\"}";
        }

    }

    private JsonObject requestGetJson (HttpServletRequest request) throws IOException { // функция для считывания json в запросе
        StringBuilder json = new StringBuilder();
        JsonObject jsonObject;
        String temp;
        while ((temp = request.getReader().readLine()) != null) {
            json.append(temp);
        }
        try {
            jsonObject = gson.fromJson(json.toString(), JsonObject.class);
        } catch (JsonSyntaxException e) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("exception", "exception");
            jsonObject.addProperty("code", 2);
            jsonObject.addProperty("response", "invalid request");
        }
        return jsonObject;
    }

    private void responseSetstatus (String status, HttpServletResponse response) { // функция для установления статуса ответа
       response.setStatus(HttpServletResponse.SC_OK);
    }


    public JsonObject getRequestParser ( String data ) {
        try {
            JsonObject jsonObject = new JsonObject();
            String strings[] = data.split("&");
            ArrayList<String> array = new ArrayList<>();

            for (int i = 0; i < strings.length; i++) {
                String values[] = strings[i].split("=");
                if (values.length == 2) {
                    if (jsonObject.get(values[0]) != null) {
                        if (array.size() == 0) array.add(jsonObject.get(values[0]).getAsString());
                        array.add(values[1]);
                        jsonObject.addProperty(values[0], array.toString());
                    } else {
                        array.clear();
                        jsonObject.addProperty(values[0], values[1]);
                    }
                } else {
                    jsonObject = new JsonObject();
                    jsonObject.addProperty("exception", "exception");
                    jsonObject.addProperty("code", 2);
                    jsonObject.addProperty("response", "invalid request");
                    return jsonObject;
                }
            }

            return jsonObject;
        }catch (Exception e) {
            return new JsonObject();
        }
    }
}
