/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.DataRequest;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import Shared.SortedData;
import Shared.Status;
import Shared.Tag;
import Shared.UnsortedData;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Linda
 */
public class DatabaseManager {

    private Connection conn;
    private Properties props;

    /**
     *
     */
    public DatabaseManager() {
        this.configure();
    }

    /**
     * configureproperties
     */
    private void configure() {
        this.props = new Properties();
        try (FileInputStream in = new FileInputStream("database.properties")) {
            props.load(in);

        } catch (FileNotFoundException ex) {
            System.out.println("file not found in database configure: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IOException in database configure: " + ex.getMessage());
        }

        try {
            this.initConnection();
        } catch (SQLException ex) {
            System.out.println("failed to init connection: " + ex.getMessage());
        }
    }

    /**
     * test properties
     */
    private void initConnection() throws SQLException {
        String url = (String) props.get("url");
        String username = (String) props.get("username");
        String password = (String) props.get("password");

        this.conn = DriverManager.getConnection(url, username, password);
        if (!conn.isClosed()) {
            System.out.println("test connection succeed");
            this.conn.close();
        }
    }

    /**
     * @param data object unsorteddata
     * @return success on attempting to insert unsorted data.
     */
    public synchronized boolean insertToUnsortedData(IData data) {
        boolean succeed = false;

        if(!openConnection()){
            return false;
        }

        try {
            String query = "INSERT INTO dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` VALUES (ID,?,?,?,?,?)";
            PreparedStatement unsortedData = conn.prepareStatement(query);
            unsortedData.setString(1, data.getTitle());
            unsortedData.setString(2, data.getDescription());
            unsortedData.setString(3, data.getLocation());
            unsortedData.setString(4, data.getSource());
            unsortedData.setString(5, Status.NONE.toString());
            unsortedData.execute();

            System.out.println("Insert unsortedData succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("Insert unsortedData failed: " + ex);
        } finally {
            closeConnection();
        }
        return succeed;
    }

    /**
     * @return List unsorteddata first get information from database second
     * change status to INPROCESS
     */
    public synchronized List<UnsortedData> getFromUnsortedData() {
        List<UnsortedData> unsorted = new ArrayList();

        int id;
        String title;
        String description;
        String location;
        String source;

        if(!openConnection()){
            return null;
        }

        try {
            String query = "SELECT * FROM dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` WHERE STATUS "
                    + " = 'NONE' ORDER BY ID";
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            //getting unsorteddata
            while (result.next() && unsorted.size() < 50) {
                id = result.getInt("ID");
                title = result.getString("TITLE");
                description = result.getString("DESCRIPTION");
                location = result.getString("LOCATION");
                source = result.getString("SOURCE");

                unsorted.add(new UnsortedData(id, title, description, location, source, Status.INPROCESS));
                System.out.println("Getting object unsorted succeed");
            }

            //update data
            for (UnsortedData x : unsorted) {
                String update = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = '"
                        + Status.INPROCESS.toString() + "' WHERE ID = " + x.getId();
                PreparedStatement updateData = conn.prepareStatement(update);
                updateData.execute();
                System.out.println("Updating unsorted status succeed");
            }

            System.out.println("Data unsorted read & update succeeded");
        } catch (SQLException ex) {
            System.out.println("Data unsorted read failed: " + ex);
        } finally {
            closeConnection();
        }
        return unsorted;
    }

    /**
     * @param sorted object sorteddata
     * @return succeed on attempting to insert sorted data.
     */
    public synchronized boolean insertToSortedData(ISortedData sorted) {
        if(!openConnection()){
            return false;
        }
        Set<Tag> tags = sorted.getTags();
        boolean succeed = false;
        try {
            //insert to sorteddata
            String query = "INSERT INTO dbi294542.`SORTEDDATABASE.SORTEDDATA` VALUES (?,?,?,?,?,?,?,?)";
            PreparedStatement sortedData = conn.prepareStatement(query);
            sortedData.setInt(1, sorted.getId());
            sortedData.setString(2, sorted.getTitle());
            sortedData.setString(3, sorted.getDescription());
            sortedData.setString(4, sorted.getLocation());
            sortedData.setString(5, sorted.getSource());
            sortedData.setInt(6, sorted.getRelevance());
            sortedData.setInt(7, sorted.getReliability());
            sortedData.setInt(8, sorted.getQuality());
            sortedData.execute();

            System.out.println("Insert sortedData succeeded");
            
            Iterator it = tags.iterator();
            while (it.hasNext()) {
                // Get element
                Object element = it.next();

                //insert into sorteddatatags database
                query = "INSERT INTO dbi294542.`SORTEDDATABASE.SORTEDDATATAGS` VALUES (?,?) ";
                sortedData = conn.prepareStatement(query);
                sortedData.setInt(1, sorted.getId());
                sortedData.setString(2, element.toString());
                sortedData.execute();
            }

            //delete from unsorteddata
            query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = '"
                    + Status.COMPLETED.toString() + "' WHERE id = " + sorted.getId();
            PreparedStatement unsortedData = conn.prepareStatement(query);
            unsortedData.execute();

            System.out.println("Update unsortedData succeeded");

            succeed = true;
        } catch (SQLException ex) {
            System.out.println("Insert sortedData failed: " + ex);
        } finally {
            closeConnection();
        }
        return succeed;
    }

    /**
     * @param info list of tags
     * @return List sorteddata
     */
    public synchronized List<SortedData> getFromSortedData(HashSet<Tag> info) {
        List<SortedData> sorted = new ArrayList();
        List<Integer> numbers = new ArrayList();

        //list of id's with correct tags
        int id;
        String title;
        String description;
        String location;
        String source;
        int relevance;
        int reliability;
        int quality;

        if(!openConnection()){
            return null;
        }

        try {
            String query = "SELECT IDSORTEDDATA FROM dbi294542.`SORTEDDATABASE.SORTEDDATATAGS` WHERE"
                    + " TAGNAME = ";
            int sizeList = info.size();
            Iterator it = info.iterator();
            int aantal = 0;
            while (it.hasNext()) {
                // Get element
                Object element = it.next();
                if (aantal == 1) {
                    query += "'" + element.toString() + "' ";
                    aantal++;
                } else {
                    query += "AND IDSORTEDDATA IN (SELECT ID FROM"
                            + " dbi294542.`SORTEDDATABASE.SORTEDDATATAGS` WHERE  '" + element.toString() + "' ";
                }
            }
            for (int x = 1; x < sizeList; x++) {
                query += ")";
            }

            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            while (result.next()) {
                numbers.add(result.getInt("SORTEDDATAID"));
            }

            //make list of object with correct id's
            String update = "";
            for (int x : numbers) {
                if (sorted.size() < 50) {
                    update = "SELECT * FROM dbi294542.`SORTEDDATABASE.SORTEDDATA` WHERE ID = " + x;
                    PreparedStatement updateData = conn.prepareStatement(update);
                    ResultSet resultTag = updateData.executeQuery();

                    id = result.getInt("IDSORTEDDATA");
                    title = result.getString("TITLE");
                    description = result.getString("DESCRIPTION");
                    location = result.getString("LOCATION");
                    source = result.getString("SOURCE");
                    relevance = result.getInt("RELEVANCE");
                    reliability = result.getInt("RELIABILITY");
                    quality = result.getInt("QUALITY");

                    sorted.add(new SortedData(id, title, description, location, source, relevance, reliability, quality, info));
                }
            }
            System.out.println("Getting sorted object succeed");
        } catch (SQLException ex) {
            System.out.println("Data sorted read failed: " + ex);
        } finally {
            closeConnection();
        }
        return sorted;
    }

    /**
     * @param data list of unsorteddata
     * @return succeed reset status unsorted data
     */
    public synchronized boolean resetUnsortedData(List<IData> data) {
        if(!openConnection()){
            return false;
        }

        boolean succeed = false;

        try {
            for (IData x : data) {
                String query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = '" + 
                        Status.NONE.toString() + "' WHERE id = " + x.getId();
                PreparedStatement reset = conn.prepareStatement(query);

                reset.execute();
                System.out.println("Resetting object succeed");
            }

            System.out.println("Data unsorted reset succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("Data unsorted reset failed: " + ex);
        } finally {
            closeConnection();
        }
        return succeed;
    }

    /**
     * @param data object of unsorteddata
     * @return succeed reset status unsorted data to Completed
     */
    public synchronized boolean updateStatusUnsortedData(IData data) {
        if(!openConnection()){
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = '" + 
                        Status.COMPLETED.toString() + "' WHERE id = " + data.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();

            System.out.println("Data unsorted update Completed succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("Data unsorted update Completed failed: " + ex);
        } finally {
            closeConnection();
        }

        return succeed;
    }

    /**
     * Updates piece of unsorted data with given id.
     *
     * @param id
     * @param iData
     * @return false if id not found
     */
    public synchronized boolean updateUnsortedData(IData iData) {
        if(!openConnection()){
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET TITLE = '"+ iData.getTitle()+
                    "', DESCRIPTION = '"+ iData.getDescription()+"', LOCATION = '"+"', SOURCE = '"+iData.getSource() +
                    " WHERE ID="+iData.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();

            System.out.println("Data unsorted update succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("Data unsorted update failed: " + ex);
        } finally {
            closeConnection();
        }

        return succeed;
    }

    /**
     * @param iData object of unsorteddata
     * @return succeed reset status unsorted data to Discard
     */
    public synchronized boolean discardUnsortedData(IData iData) {
        if(!openConnection()){
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = '" + 
                        Status.DISCARDED.toString() + "' WHERE id = " + iData.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();

            System.out.println("Data unsorted update Discard succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("Data unsorted update Discard failed: " + ex);
        } finally {
            closeConnection();
        }

        return succeed;
    }

    /**
     * @param data object of datarequest
     * @return succeed if succeeded
     */
    public synchronized boolean insertDataRequest(IDataRequest data) {
        if(!openConnection()){
            return false;
        }

        boolean succeed = false;
        try {
            Set<Tag> tags = data.getTags();
            openConnection();
            //insert to sorteddata
            String query = "INSERT INTO dbi294542.`REQUESTDATABASE.REQUESTDATA` VALUES (?,?,?,?,?)";
            PreparedStatement requestData = conn.prepareStatement(query);
            requestData.setInt(1, data.getId());
            requestData.setString(2, data.getTitle());
            requestData.setString(3, data.getDescription());
            requestData.setString(4, data.getLocation());
            requestData.setString(5, data.getSource());
            requestData.execute();

            Iterator it = tags.iterator();
            while (it.hasNext()) {
                // Get tagid
                Object element = it.next();

                //insert into requesttag database
                query = "INSERT INTO dbi294542.`REQUESTDATABASE.REQUESTDATATAGS` VALUES (?,?) ";
                requestData = conn.prepareStatement(query);
                requestData.setInt(1, data.getId());
                requestData.setString(2, element.toString());
                requestData.execute(); 
            }
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("Data unsorted reset failed: " + ex);
        } finally {
            closeConnection();
        }

        return succeed;
    }

    /**
     * @param tags list of tags
     * @return succeed if succeeded
     */
    public synchronized List<IDataRequest> getUpdateRequests(HashSet tags) {
        if(!openConnection()){
            return null;
        }

       boolean succeed = false;
        List<IDataRequest> request = new ArrayList();
        List<Integer> numbers = new ArrayList();

        int id;
        String title;
        String description;
        String location;
        String source;
        int requestId;

        //dbi294542.`UNSORTEDDATABASE`
        try {
            openConnection();

            //build a string with all tha tags
            String query = "SELECT REQUESTID FROM dbi294542.`REQUESTDATABASE.REQUESTDATA` WHERE"
                    + " TAGNAME = ";
            int sizeList = tags.size();
            Iterator it = tags.iterator();
            int aantal = 0;
            while (it.hasNext()) {
                // Get element
                Object element = it.next();
                if (aantal == 1) {
                    query += "'" + element.toString() + "' ";
                    aantal++;
                } else {
                    query += "AND IDREQUESTDATA IN (SELECT ID FROM dbi294542.`REQUESTDATABASE."
                            + "REQUESTTAGS` WHERE  '" + element.toString() + "' ";
                }
            }
            for (int x = 1; x < sizeList; x++) {
                query += ")";
            }
            //making a list with al de id's with correct tags
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            while (result.next()) {
                numbers.add(result.getInt("REQUESTID"));
            }

            //Get request data from database with correct id's
            String update = "";
            for (int x : numbers) {
                if (request.size() < 50) {
                    update = "SELECT * FROM dbi294542.`REQUESTDATABASE.REQUESTDATA` WHERE REQUESTID = " + x;
                    PreparedStatement updateData = conn.prepareStatement(update);
                    ResultSet resultTag = updateData.executeQuery();

                    id = resultTag.getInt("REQUESTID");
                    title = resultTag.getString("TITLE");
                    description = resultTag.getString("DESCRIPTION");
                    location = resultTag.getString("LOCATION");
                    source = resultTag.getString("SOURCE");
                    requestId = resultTag.getInt("REQUESTID");

                    request.add(new DataRequest(id, title, description, location, source, requestId, tags));

                    System.out.println("Getting object request succeed");
                }
            }
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("Data unsorted reset failed: " + ex);
        } finally {
            closeConnection();
        }

        return null;
    }

    /**
     * open connection
     */
    private boolean openConnection() {
        try {
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
    private void closeConnection() {
        if(conn == null){
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

    IData getDataItem(int id) {
        if(!openConnection()){
            return null;
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    List<IData> getSentData(String source) {
        if(!openConnection()){
            return null;
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
