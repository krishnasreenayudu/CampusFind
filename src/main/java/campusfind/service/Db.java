package campusfind.service;

import campusfind.util.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Opens one connection per operation and closes it automatically.
 * read()  : plain queries.
 * write() : a transaction - commit if the work finishes, rollback if it throws.
 */
final class Db {
    private Db() { }

    interface Work<T> { T run(Connection c) throws SQLException; }

    interface VoidWork { void run(Connection c) throws SQLException; }

    static <T> T read(Work<T> work) {
        try (Connection c = DBConnection.getConnection()) {
            return work.run(c);
        } catch (SQLException e) {
            throw new ServiceException("Database error: " + e.getMessage(), e);
        }
    }

    static <T> T write(Work<T> work) {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (ServiceException e) {
            throw e;
        } catch (SQLException e) {
            throw new ServiceException("Database error: " + e.getMessage(), e);
        }
    }

    static void exec(VoidWork work) {
        write(c -> {
            work.run(c);
            return null;
        });
    }
}
