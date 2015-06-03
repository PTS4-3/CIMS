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
//    private static final ReentrantLock lock = new ReentrantLock(true);
    private String managerName;
    ComboPooledDataSource connPool = new ComboPooledDataSource();

    /**
     *
     * @param fileName
     */
    public DatabaseManager(String fileName) {
        this.managerName = fileName;
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
            closeConnection(conn);
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
//            if (conn != null && !conn.isClosed()) {
////                System.out.println("connection already open");
//                return true;
//            }
//            System.out.println("-trying to acquire lock for "
//                        + Thread.currentThread().getName()
//                        + " on " + managerName);
//            if (!lock.isHeldByCurrentThread()
//                    && !lock.tryLock(10000, TimeUnit.MILLISECONDS)) {
//                System.out.println("------ERROR: Database lock timeout for "
//                        + Thread.currentThread().getName()
//                        + " on " + managerName);
//                return null;
//            }
//            System.out.println("--lock acquired for "
//                    + Thread.currentThread().getName()
//                    + " on " + managerName);
//            System.setProperty("jdbc.drivers", props.getProperty("driver"));
//            this.conn = DriverManager.getConnection(
//                    (String) props.get("url"),
//                    (String) props.get("username"),
//                    (String) props.get("password"));
            
//            return true;
            return connPool.getConnection();
        } catch (Exception ex) {
            System.out.println("Connection open failed: " + ex);
            return null;
//            return false;
        }
    }

    /**
     * closing connection
     */
    @Deprecated
    protected synchronized void closeConnection(Connection conn) {
//        if (!lock.isHeldByCurrentThread()) {
//            return;
//        }
//        lock.unlock();
//        System.out.println("---lock released for "
//                + Thread.currentThread().getName()
//                + " on " + managerName);
        if (conn == null) {
            return;
        }

        try {
            conn.close();
        } catch (SQLException ex) {
            System.out.println("Connection close failed: " + ex);
        } finally {
            conn = null;
        }
    }

    public void shutDownConnection() {
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
    protected int getMaxID(Connection conn, String tableName) throws SQLException {
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
