package de.kaai.pvptoggle.util;

import com.zaxxer.hikari.HikariDataSource;
import de.kaai.pvptoggle.PvPTogglePlugin;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLAdapter {
    private static MySQLAdapter instance;

    private HikariDataSource dataSource;

    public interface Callback<E extends Throwable, V extends Object> {
        public void call(E exception, V result);
    }

    public MySQLAdapter(String host, int port, String database, String user, String password) {
        instance = this;
        this.dataSource = new HikariDataSource();
        this.dataSource.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        this.dataSource.addDataSourceProperty("serverName", host);
        this.dataSource.addDataSourceProperty("port", port);
        this.dataSource.addDataSourceProperty("databaseName", database);
        this.dataSource.addDataSourceProperty("user", user);
        this.dataSource.addDataSourceProperty("password", password);
    }

    public MySQLConnection getConnection() {
        return new MySQLConnection();
    }

    public void disconnect() {
        // TODO: Should we call .close() on all instantiated MySQLConnection-Objects here?
        dataSource.close();
    }

    public class MySQLConnection {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        private void executeAsync(Runnable task) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(PvPTogglePlugin.getInstance(), task);
        }

        public ResultSet query(String statement, final Object... args) throws SQLException {
            if (args.length > 0) statement = String.format(statement, args);
            String finalStatement = statement;

            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(finalStatement);
            resultSet = preparedStatement.executeQuery();

            return resultSet;
        }

        public int update(String statement, final Object... args) throws SQLException {
            if (args.length > 0) statement = String.format(statement, args);
            String finalStatement = statement;

            int rows = 0;
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(finalStatement);
            rows = preparedStatement.executeUpdate();

            return rows;
        }

        public Boolean execute(String statement, final Object... args) throws SQLException {
            if (args.length > 0) statement = String.format(statement, args);
            String finalStatement = statement;

            Boolean result = false;
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(finalStatement);
            result = preparedStatement.execute();

            return result;
        }

        public MySQLConnection queryAsync(String statement, final @Nullable Callback<SQLException, ResultSet> callback, final Object... args) {
            if (args.length > 0) statement = String.format(statement, args);
            final String finalStatement = statement;

            executeAsync(() -> {
                try {
                    ResultSet resultSet = query(finalStatement);
                    callback.call(null, resultSet);
                } catch (SQLException e) {
                    callback.call(e, null);
                }
            });

            return this;
        }

        public MySQLConnection updateAsync(String statement, final @Nullable Callback<SQLException, Integer> callback, final Object... args) {
            if (args.length > 0) statement = String.format(statement, args);
            final String finalStatement = statement;

            executeAsync(() -> {
                try {
                    int rows = update(finalStatement);
                    callback.call(null, rows);
                } catch (SQLException e) {
                    callback.call(e, null);
                }
            });

            return this;
        }

        public MySQLConnection executeAsync(String statement, final @Nullable Callback<SQLException, Boolean> callback, final Object... args) {
            if (args.length > 0) statement = String.format(statement, args);
            final String finalStatement = statement;

            executeAsync(() -> {
                try {
                    Boolean result = execute(finalStatement);
                    callback.call(null, result);
                } catch (SQLException e) {
                    callback.call(e, null);
                }
            });

            return this;
        }

        public MySQLConnection close() {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            return this;
        };
    }

    public static MySQLAdapter getAdapter() {
        return instance;
    }
}

