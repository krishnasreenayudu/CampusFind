package campusfind;

import campusfind.util.DBConnection;
import java.sql.Connection;

/** Run this first to test the database settings: prints "Connected" or the error. */
public class ConnectionCheck {
    public static void main(String[] args) throws Exception {
        try (Connection c = DBConnection.getServerConnection()) {
            System.out.println("Connected to " + c.getMetaData().getDatabaseProductName() + " "
                    + c.getMetaData().getDatabaseProductVersion());
        }
    }
}
