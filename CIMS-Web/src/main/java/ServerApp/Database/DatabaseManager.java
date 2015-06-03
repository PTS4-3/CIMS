/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Linda
 */
class DatabaseManager {

//    protected Connection conn;
    private Properties props;
    ComboPooledDataSource connPool = new ComboPooledDataSource();

    /**
     *
     * @param fileName
     */
    public DatabaseManager(String fileName) {
        this.configure(fileName);
    }

    /**
     * configureproperties
     *
     * @param fileName
     */
    private void configure(String fileName) {
        props = new Properties();
        Connection conn = null;

        try (FileInputStream in = new FileInputStream(fileName)) {
            props.load(in);
            Class.forName("com.mysql.jdbc.Driver");
        } catch (FileNotFoundException ex) {
            System.out.println("file not found in database configure: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IOException in database configure: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException in database configure: " + ex.getMessage());
        }

        try {
            connPool = new ComboPooledDataSource();
            connPool.setDriverClass("com.mysql.jdbc.Driver"); //loads the jdbc driver
            connPool.setJdbcUrl(props.getProperty("url"));
            connPool.setUser(props.getProperty("username"));
            connPool.setPassword(props.getProperty("password"));

            connPool.setContextClassLoaderSource("library");
            connPool.setPrivilegeSpawnedThreads(true);

            conn = connPool.getConnection();
            if (!conn.isValid(2)) {
                throw new SQLException("Connection was null or closed");
            }
        } catch (SQLException ex) {
            System.out.println("failed to init connection: " + ex.getMessage());
        } catch (PropertyVetoException ex) {
            System.out.println("failed to init connection pool: " + ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    /**
     * Resets database to dummy data state
     *
     * @return
     */
    protected boolean resetDatabase() {
//        Connection conn = null;
        try (Connection conn = connPool.getConnection()) {
//            conn = connPool.getConnection();
            CallableStatement cs = conn.prepareCall("{call ResetDatabase()}");
            cs.executeQuery();
            return true;
        } catch (SQLException ex) {
            System.out.println("Failed to reset database: " + ex.getMessage());
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * open connection
     *
     * @return
     */
    protected synchronized Connection openConnection() {
        try {
            return connPool.getConnection();
        } catch (Exception ex) {
            System.out.println("Connection open failed: " + ex);
            return null;
        }
    }

    public void shutDownManager() {
        if (connPool == null) {
            return;
        }
        try {
            connPool.close();
        } catch (Exception ex) {
            System.out.println("Connection close failed: " + ex);
        } finally {
            connPool = null;
        }
    }

    /**
     * gets max ID from given table. Does not open its own connection.
     *
     * @param tableName
     * @return
     * @throws SQLException
     */
    protected synchronized int getMaxID(Connection conn, String tableName) throws SQLException {
        int output = -1;
        // Gets assigned ID. Throws Exception if not found
        String query = "SELECT MAX(ID) FROM " + tableName;
        PreparedStatement prepStat = conn.prepareStatement(query);
        ResultSet rs = prepStat.executeQuery();
        while (rs.next()) {
            output = rs.getInt(1);
        }
        return output;
    }

}
