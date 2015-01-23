package database;

import java.sql.*;

/**
 * Created by Abovyan Narek on 03.11.14.
 */
public class SimpleExecutor {
    public void execUpdate(Connection connection, String update) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute(update);
        stmt.close();
    }

    public int execUpdateAndReturnId(Connection connection, PreparedStatement stm) throws SQLException  {
        int id ;
        connection.setAutoCommit(false);
        stm.executeUpdate();
        ResultSet resultSet = stm.getGeneratedKeys();
        if (resultSet != null && resultSet.next()) {
            id = resultSet.getInt(1);
        } else {
            id = -1;
        }
        connection.commit();
        connection.setAutoCommit(true);
        stm.close();
        return id;
    }
}
