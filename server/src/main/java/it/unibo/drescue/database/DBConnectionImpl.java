package it.unibo.drescue.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionImpl implements DBConnection {

    protected static final String LOCAL_ADDRESS = "jdbc:mysql://localhost:3306/drescueDB";
    protected static final String REMOTE_ADDRESS =
            "jdbc:mysql://rds-mysql-drescue.cnwnbp8hx7vq.us-east-2.rds.amazonaws.com:3306/drescueDB";
    private static final String DRIVER_NAME =
            "com.mysql.jdbc.Driver";
    private static Connection connection;
    private static String dbAddress;
    private static String dbUsername;
    private static String dbPassword;

    /**
     * Private constructor, cannot be instantiated
     */
    private DBConnectionImpl() {
    }

    /**
     * Instantiate a new connection to db in local environment
     *
     * @return a db connection in local
     */
    public static DBConnectionImpl getLocalConnection() {
        setEnvironment(Environment.LOCAL);
        return new DBConnectionImpl();
    }

    /**
     * Instantiate a new connection to db in remote environment
     *
     * @return a db connection in remote
     */
    public static DBConnectionImpl getRemoteConnection() {
        setEnvironment(Environment.REMOTE);
        return new DBConnectionImpl();
    }

    private static void setEnvironment(final Environment env) {
        switch (env) {
            case LOCAL:
                dbAddress = LOCAL_ADDRESS;
                dbUsername = "admin";
                dbPassword = "4dm1n";
                break;
            case REMOTE:
                dbAddress = REMOTE_ADDRESS;
                dbUsername = "masterDrescue";
                dbPassword = "rdsTeamPass";
                break;
        }
    }

    @Override
    public void openConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DRIVER_NAME);
                connection = DriverManager.getConnection(dbAddress, dbUsername, dbPassword);
                System.out.println("[DB]: Connection established with db address: " + dbAddress);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB]: Connection closed");
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValid() {
        try {
            return connection.isValid(5000);
        } catch (final SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected String getDbAddress() {
        return dbAddress;
    }

    private enum Environment {
        LOCAL,
        REMOTE
    }
}
