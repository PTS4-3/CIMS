/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Kargathia
 */
public class BaseDBManager {
    
    protected static boolean isConfigured = false;
    protected static Properties props;

    protected static final String
            UNSORTEDDATA_TABLE = "dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA`",
            SORTEDDATA_TABLE = "dbi294542.`SORTEDDATABASE.SORTEDDATA`",
            SORTEDDATATAGS_TABLE = "dbi294542.`SORTEDDATABASE.SORTEDDATATAGS`",
            REQUESTS_TABLE = "dbi294542.`REQUESTDATABASE.SORTEDDATA`",
            REQUESTTAGS_TABLE = "dbi294542.`REQUESTDATABASE.SORTEDDATATAGS`";

    protected Connection conn;
//    private Properties props;

    /**
     * configureproperties
     */
    private void configure() {
        props = new Properties();
        try (FileInputStream in = new FileInputStream("database.properties")) {
            props.load(in);
            isConfigured = true;
        } catch (FileNotFoundException ex) {
            System.out.println("file not found in database configure: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IOException in database configure: " + ex.getMessage());
        }
        boolean testResult = this.openConnection();
        this.closeConnection();
        System.out.println("Able to open connection: " + testResult);
    }

    /**
     * open connection
     */
    protected boolean openConnection() {
        if(!isConfigured){
            configure();
        }
        try {
            System.setProperty("jdbc.drivers", props.getProperty("driver"));
            this.conn = DriverManager.getConnection(
                    (String) props.get("url"),
                    (String) props.get("username"),
                    (String) props.get("password"));
            System.out.println("Connection open succeeded");
            return true;
        } catch (Exception ex) {
            System.out.println("Connection open failed: " + ex);
            closeConnection();
            return false;
        }
    }

    /**
     * closing connection
     */
    protected void closeConnection() {
        if (conn == null) {
            return;
        }

        try {
            conn.close();
            System.out.println("Connection close succeeded");
        } catch (SQLException ex) {
            System.out.println("Connection close failed: " + ex);
        } finally {
            conn = null;
        }

    }

}
