import frontend.Frontend;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import test.FrontendTest;

/**
 * Created by narek on 06.11.14.
 */
public class Main {

    public static void main(String args[]) throws Exception{
        //FrontendTest frontend = new FrontendTest();
        Frontend frontend = new Frontend();

        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setHandler(context);
        context.addServlet(new ServletHolder(frontend), "/*");

        server.start();
        server.join();
    }
}
