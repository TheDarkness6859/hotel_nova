package com.hotelNova.db;

import com.hotelNova.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;

    //Instance of AppConfig to get db credentials:
    private static final AppConfig config = AppConfig.getInstance();

    //Create threads to easier db manage:
    private static final ThreadLocal<Connection> thread = new ThreadLocal<>();

    private DatabaseConnection () {

        try {

            Class.forName(config.getDriver());

        }catch (ClassNotFoundException err){

            throw new RuntimeException("don't found JDBC driver:", err);

        }

    }

    public static DatabaseConnection getInstance() {

        if (instance == null){

            synchronized (DatabaseConnection.class){

                if (instance == null) instance = new DatabaseConnection();

            }

        }

        return instance;

    }

    public Connection getConnection() throws SQLException {

        if (thread.get() != null && !thread.get().isClosed()){

            return thread.get();

        }

        Connection conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPass());
        thread.set(conn);
        return conn;

    }

    public static void startTransaction () throws SQLException {

        if (thread.get() == null || thread.get().isClosed()){

            Connection conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPass());
            conn.setAutoCommit(false);
            thread.set(conn);

        }

    }

    public static void commit () throws SQLException {

        Connection conn = thread.get();

        if (conn != null){

            conn.commit();
            closeConnection();

        }

    }

    public static void rollback() {

        try {

            Connection conn = thread.get();

            if (conn != null) {

                conn.rollback();
                closeConnection();

            }

        }catch (SQLException err){

            System.err.println("Error in Rollback: " + err.getMessage());

        }

    }

    public static void closeConnection () throws SQLException {

        Connection conn = thread.get();

        if (conn != null){

            conn.close();
            thread.remove();

        }

    }

}
