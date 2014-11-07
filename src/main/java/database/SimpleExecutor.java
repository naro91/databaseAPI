package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by narek on 03.11.14.
 */
public class SimpleExecutor {
    public void execUpdate(Connection connection, String update) throws SQLException
    {
        Statement stmt = connection.createStatement();
        stmt.execute(update);
        stmt.close();
    }

    public int execUpdateAndReturnId(Connection connection, String update) throws SQLException
    {
        Statement stmt = connection.createStatement();
        //stmt.execute(update, Statement.RETURN_GENERATED_KEYS);

        //ResultSet created = stmt.getGeneratedKeys();

        //created.next();
        int id = stmt.executeUpdate(update); /*created.getInt(1);*/

        stmt.close();
        return id;
    }
}
