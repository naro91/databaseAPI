
import frontend.Frontend;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * Created by Abovyan Narek on 06.11.14.
 */
public class Main {

    public static void main(String args[]) throws Exception{
        Frontend frontend = new Frontend();
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(10);
        threadPool.setMaxThreads(100);
        Server server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(5000);
        server.addConnector(connector);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setHandler(context);
        context.addServlet(new ServletHolder(frontend), "/*");

        server.start();
        server.join();
    }
}
