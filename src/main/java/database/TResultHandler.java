package database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by narek on 01.11.14.
 */
public interface TResultHandler<T> {
    T handle(ResultSet resultSet) throws SQLException;
}
