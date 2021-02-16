package com.revature.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class ConnectionFactory {

    private static ConnectionFactory connFactory = new ConnectionFactory();
    private static Connection conn = null;
    private static Properties props = new Properties();


    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private ConnectionFactory() {
    }

    public static ConnectionFactory getInstance() {
        return connFactory;
    }

    public static void addCredentials(Properties props) {
        ConnectionFactory.props = props;
    }

    public Connection getConnection() {
        if (conn == null) {
            try {
                conn = DriverManager.getConnection(
                        props.getProperty("url"),
                        props.getProperty("username"),
                        props.getProperty("password")
                );
                conn.setSchema(props.getProperty("currentSchema"));

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        return conn;
    }
}