package frontend;

import clasesForExecutionQuery.Forum;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.Database;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;



/**
 * Created by narek on 06.11.14.
 */
public class Frontend extends HttpServlet {
    private Gson gson = new Gson();
    private String json, responseResult;
    Database database = new Database();
    private Forum forum = new Forum(database, gson);

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=utf-8");
        json = getRequestParser(URLDecoder.decode(request.getQueryString(), "UTF-8")); // получение json из get запроса
        responseResult = delegationCall(request.getRequestURI().split("/"), json);  // делегирование запроса соответствующему классу и методу
        responseSetstatus(responseResult, response);  // установление статуса ответа в зависимости от результата выполнения запроса
        response.getOutputStream().print(responseResult); // отправка ответа
        response.getOutputStream().flush();

    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=utf-8");
        json = requestGetJson(request);  // получение json из запроса
        responseResult = delegationCall(request.getRequestURI().split("/"), json);  // делегирование запроса соответствующему классу и методу
        responseSetstatus(responseResult, response);  // установление статуса ответа в зависимости от результата выполнения запроса
        //response.getOutputStream().print(responseResult); // отправка ответа
        //response.getOutputStream().flush();
        System.out.println(responseResult);
    }


    private String delegationCall (String requestMass[], String json) {  // функция для делегирования запроса

        if (requestMass.length == 4) {
            if (requestMass[3].equals("clear")) {
                return "ok"/*clear()*/;
            } else {
                return "bad"/*clear()*/;
            }
        } else {
            if (requestMass.length > 4) {
                switch (requestMass[3]) {
                    case "forum":
                        return forum.delegationCall(requestMass[4], json);
                    case "post":
                        return "ok"/*call post method*/;
                    case "user":
                        return "ok"/*call user method*/;
                    case "thread":
                        return "ok"/*call thread method*/;
                    default: return "bad";
                }
            } else return "bad";
        }

    }

    private String requestGetJson (HttpServletRequest request) throws IOException { // функция для считывания json в запросе
        StringBuilder json = new StringBuilder();
        String temp;
        while ((temp = request.getReader().readLine()) != null) {
            json.append(temp);
        }
        return json.toString();
    }

    private void responseSetstatus (String status, HttpServletResponse response) { // функция для установления статуса ответа
        switch (status) {
            case "ok":
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            case "bad":
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                break;
        }
    }


    public String getRequestParser ( String data ) {
        JsonObject jsonObject = new JsonObject();
        String strings[] = data.split("&");
        for(int i = 0; i < strings.length; i++) {
            String values[] = strings[i].split("=");
            jsonObject.addProperty(values[0], values[1]);
        }
        return gson.toJson(jsonObject);
    }
}
