package de.crafttogether.pvptoggle.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.crafttogether.pvptoggle.PvPTogglePlugin;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLAdapter {
    private static MySQLAdapter instance;

    private static MySQLConfig config;
    private HikariDataSource dataSource;

    public interface Callback<E extends Throwable, V extends Object> {
        void call(E exception, V result);
    }

    public MySQLAdapter(MySQLConfig _config) {
        instance = this;
        config = _config;
        setupHikari();
    }

    public MySQLAdapter(String host, int port, String database, String username, String password) {
        instance = this;
        config = new MySQLConfig(host, port, database, username, password);
        setupHikari();
    }

    private void setupHikari() {

        try {
            HikariConfig hikariCfg = new HikariConfig();
            hikariCfg.setJdbcUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort() + ((config.getDatabase() != null) ? ("/" + config.getDatabase()) : ""));
            hikariCfg.setUsername(config.getUsername());
            hikariCfg.setPassword(config.getPassword());
            hikariCfg.addDataSourceProperty("cachePrepStmts", "true");
            hikariCfg.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariCfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            this.dataSource = new HikariDataSource(hikariCfg);
        }
        catch (Throwable ex) {
            System.out.println("MYSQL: " + ex.getMessage());
        }
    }

    public static MySQLAdapter getAdapter() {
        return instance;
    }

    public MySQLConfig getConfig() { return config; }
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

        public ResultSet query(String statement, final Object... args) throws Throwable {
            if (args.length > 0) statement = String.format(statement, args);
            String finalStatement = statement;

            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(finalStatement);
            resultSet = preparedStatement.executeQuery();

            return resultSet;
        }

        public int update(String statement, final Object... args) throws Throwable {
            if (args.length > 0) statement = String.format(statement, args);
            String finalStatement = statement;

            int rows = 0;
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(finalStatement);
            rows = preparedStatement.executeUpdate();

            return rows;
        }

        public Boolean execute(String statement, final Object... args) throws Throwable {
            if (args.length > 0) statement = String.format(statement, args);
            String finalStatement = statement;

            Boolean result = false;
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(finalStatement);
            result = preparedStatement.execute();

            return result;
        }

        public MySQLConnection queryAsync(String statement, final @Nullable Callback<Throwable, ResultSet> callback, final Object... args) {
            if (args.length > 0) statement = String.format(statement, args);
            final String finalStatement = statement;

            executeAsync(() -> {
                try {
                    ResultSet resultSet = query(finalStatement);
                    callback.call(null, resultSet);
                } catch (Throwable e) {
                    callback.call(e, null);
                }
            });

            return this;
        }

        public MySQLConnection updateAsync(String statement, final @Nullable Callback<Throwable, Integer> callback, final Object... args) {
            if (args.length > 0) statement = String.format(statement, args);
            final String finalStatement = statement;

            executeAsync(() -> {
                try {
                    int rows = update(finalStatement);
                    callback.call(null, rows);
                } catch (Throwable e) {
                    callback.call(e, null);
                }
            });

            return this;
        }

        public MySQLConnection executeAsync(String statement, final @Nullable Callback<Throwable, Boolean> callback, final Object... args) {
            if (args.length > 0) statement = String.format(statement, args);
            final String finalStatement = statement;

            executeAsync(() -> {
                try {
                    Boolean result = execute(finalStatement);
                    callback.call(null, result);
                } catch (Throwable e) {
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
        }
    }

    public static class MySQLConfig {
        String host;
        Integer port;
        String username;
        String password;
        String database;

        public MySQLConfig() { }

        public MySQLConfig(String host, int port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        public MySQLConfig(String host, int port, String username, String password, String database) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.database = database;
        }

        public boolean checkInputs() {
            return (this.host != null && port != null && username != null && password != null);
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getDatabase() {
            return database;
        }
    }
}

