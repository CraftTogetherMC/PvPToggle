package de.kaai.pvptoggle.util;

import java.sql.*;

import de.kaai.pvptoggle.PvPTogglePlugin;

public class MySQLHandler {

    private static String HOST = "";
    private static String PORT = "";
    private static String DATABASE = "";
    private static String USER = "";
    private static String PASSWORD = "";
    private static Connection con;

    public MySQLHandler(String HOST, String PORT, String DATABASE, String USER, String PASSWORD) {
        this.HOST = HOST;
        this.PORT = PORT;
        this.DATABASE = DATABASE;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
        connect();
    }

    public MySQLHandler(String HOST, String PORT, String DATABASE, String USER) {
        this.HOST = HOST;
        this.PORT = PORT;
        this.DATABASE = DATABASE;
        this.USER = USER;
        connect();
    }

    public static void connect() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?autoReconnect=true",
                    USER,
                    PASSWORD);
            PvPTogglePlugin.getInstance().getLogger().info("MySQL connection is successful");

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void close() {
        try {
            if (con != null) {
                con.close();
                PvPTogglePlugin.getInstance().getLogger().info("MySQL disconnection is successful");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static boolean isConnected() {
        return (con == null ? false : true);
    }

    public void update(String qry) {
        try {
            Statement st = con.createStatement();
            st.executeUpdate(qry);
            st.close();
        } catch (SQLException exception) {
            connect();
            exception.printStackTrace();
        }
    }

    public ResultSet query(String qry) {
        ResultSet rs = null;
        try {
            Statement st = con.createStatement();
            rs = st.executeQuery(qry);
        } catch (SQLException exception) {
            connect();
            exception.printStackTrace();
        }

        return rs;
    }
}