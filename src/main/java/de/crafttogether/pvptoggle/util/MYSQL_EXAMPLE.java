package de.crafttogether.pvptoggle.util;

import de.crafttogether.pvptoggle.util.MySQLAdapter;
import de.crafttogether.pvptoggle.util.MySQLAdapter.MySQLConnection;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MYSQL_EXAMPLE {

    /*
       Anwendungsbeispiele
     */

    public MYSQL_EXAMPLE() {



        // Instantiate
        MySQLAdapter MySQL = new MySQLAdapter("srv02.craft-together.de", 3306, "minecraft", "ctogether", "");

        // Static access
        MySQLAdapter.getAdapter().getConnection();




        // Sync Query
        MySQLConnection conn1 = MySQL.getConnection();
        try {
            ResultSet resultSet = conn1.query("SELECT * FROM `?`.`?` WHERE `username` LIKE ?","test_database", "test_table", "Kaai");

            try {
                if (resultSet.next()) {
                    String username = resultSet.getString("uuid");
                    String uuid = resultSet.getString("uuid");
                    /* ... */
                }
                else {
                    // Nothing foundy
                }

            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            conn1.close();
        }

        // Async Query
        MySQL.getConnection().queryAsync("SELECT * FROM `?`.`?` WHERE `username` LIKE ?", (err, resultSet) -> {
            if (err != null)
                err.printStackTrace();

            try {
                if (resultSet.next()) {
                    String username = resultSet.getString("uuid");
                    String uuid = resultSet.getString("uuid");
                    /* ... */
                }
                else {
                    // Nothing found
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }, "test_database", "test_table", "Kaai").close();







        // Sync Update
        MySQLConnection conn2 = MySQL.getConnection();
        try {
            int affectedRows = conn2.update("UPDATE `?`.`?` SET `username` = '?' WHERE `username` LIKE ?",
                    "test_database", "test_table", "Joscha", "Kaai"
            );

            System.out.println(affectedRows + " row(s) affected.");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        finally {
            conn2.close();
        }

        // Async Update
        MySQL.getConnection().updateAsync("UPDATE `?`.`?` SET `username` = '?' WHERE `username` LIKE ?", (err, affectedRows) -> {
            if (err != null)
                err.printStackTrace();

            System.out.println(affectedRows + " row(s) affected.");
        }, "test_database", "test_table", "Joscha", "Kaai").close();






        // Sync Execute
        MySQLConnection conn3 = MySQL.getConnection();
        try {
            boolean success = conn3.execute(
            "CREATE TABLE IF NOT EXISTS `?`.`?` (" +
                        "`id` INT(11) NOT NULL AUTO_INCREMENT, " +
                        "`uuid` VARCHAR(36) NOT NULL, " +
                        "`playername` VARCHAR(16) NOT NULL, " +
                        "`pvp` BOOLEAN NULL DEFAULT NULL, " +
                    "PRIMARY KEY (`id`), INDEX (`uuid`)) ENGINE = InnoDB;",

            "database", "tablename"
            );

            System.out.println(success ? "Execute was successful" : "Execute failed");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        finally {
            conn3.close();
        }

        // Async Execute
        MySQL.getConnection().executeAsync(
"CREATE TABLE IF NOT EXISTS `?`.`?` (" +
            "`id` INT(11) NOT NULL AUTO_INCREMENT, " +
            "`uuid` VARCHAR(36) NOT NULL, " +
            "`playername` VARCHAR(16) NOT NULL, " +
            "`pvp` BOOLEAN NULL DEFAULT NULL, " +
            "PRIMARY KEY (`id`), INDEX (`uuid`)) ENGINE = InnoDB;",
        (err, success) -> {
            if (err != null)
                err.printStackTrace();

            System.out.println(success ? "Execute was successful" : "Execute failed");
        }, "database", "tablename").close();
    }
}
