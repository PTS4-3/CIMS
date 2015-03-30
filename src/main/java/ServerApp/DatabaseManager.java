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
import java.util.Arrays;
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

    /*
     * Notes:
     - merge initConnection and openConnection
     - code folding at 80 characters
     - use a private variable for database / schema name instead of calling it in every
     command
     -- less text 
     -- easier to update for new database
     - if method throws an error, set output type to null / false to be sure
     - check input for illegal values before opening connection
     */
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

        if (!openConnection()) {
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

            System.out.println("insertToUnsortedData succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("insertToUnsortedData failed: " + ex);
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

        if (!openConnection()) {
            return null;
        }

        try {
            String query = "SELECT * FROM dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` WHERE STATUS "
                    + " = 'NONE' ORDER BY ID";
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            //getting unsorteddata
            while (result.next()) {
                if (unsorted.size() < 50) {
                    id = result.getInt("ID");
                    title = result.getString("TITLE");
                    description = result.getString("DESCRIPTION");
                    location = result.getString("LOCATION");
                    source = result.getString("SOURCE");
                    Status status = Status.valueOf(result.getString("STATUS"));

                    unsorted.add(new UnsortedData(id, title, description, location, source, status));
                    System.out.println("Getting object" + unsorted.size() + " unsorted succeed");
                }
            }

            //update data
            for (UnsortedData x : unsorted) {
                String update = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = '"
                        + Status.INPROCESS.toString() + "' WHERE ID = " + x.getId();
                PreparedStatement updateData = conn.prepareStatement(update);
                updateData.execute();
                System.out.println("Updating unsorted status succeed");
            }

            System.out.println("getFromUnsortedData succeed");
        } catch (SQLException ex) {
            System.out.println("getFromUnsortedData failed: " + ex);
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
        if (!openConnection()) {
            return false;
        }
        Set<Tag> tags = sorted.getTags();
        if (tags.isEmpty()) {
            tags.addAll(Arrays.asList(Tag.values()));
        }
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

            System.out.println("insertToSortedData succeeded");

            succeed = true;
        } catch (SQLException ex) {
            System.out.println("insertToSortedData failed: " + ex);
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
        HashSet<Integer> numbers = new HashSet<Integer>();

        //list of id's with correct tags
        int id;
        String title;
        String description;
        String location;
        String source;
        int relevance;
        int reliability;
        int quality;
        HashSet<Tag> newTags = new HashSet<Tag>();

        if (!openConnection()) {
            return null;
        }

        try {
            String query = "SELECT ID FROM dbi294542.`SORTEDDATABASE.SORTEDDATATAGS` ";
            int sizeList = info.size();
            Iterator it = info.iterator();
            int aantal = 1;
            while (it.hasNext()) {
                // Get element
                Object element = it.next();
                if (aantal == 1) {
                    query += "WHERE TAGNAME = '" + element.toString() + "' ";
                    aantal++;
                } else {
                    query += "AND ID IN (SELECT ID FROM"
                            + " dbi294542.`SORTEDDATABASE.SORTEDDATATAGS` WHERE  TAGNAME = '"
                            + element.toString() + "' ";
                }
            }
            for (int x = 1; x < sizeList; x++) {
                query += ")";
            }

            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            while (result.next()) {
                numbers.add(result.getInt("ID"));
            }

            //make list of object with correct id's
            String update = "";
            Iterator it2 = numbers.iterator();
            while (it2.hasNext()) {
                // Get element
                Object element = it2.next();
                if (sorted.size() < 50) {
                    update = "SELECT * FROM dbi294542.`SORTEDDATABASE.SORTEDDATA` WHERE ID = " + element.toString();
                    PreparedStatement updateData = conn.prepareStatement(update);
                    ResultSet resultTag = updateData.executeQuery();
                    while (resultTag.next()) {
                        id = resultTag.getInt("ID");
                        title = resultTag.getString("TITLE");
                        description = resultTag.getString("DESCRIPTION");
                        location = resultTag.getString("LOCATION");
                        source = resultTag.getString("SOURCE");
                        relevance = resultTag.getInt("RELEVANCE");
                        reliability = resultTag.getInt("RELIABILITY");
                        quality = resultTag.getInt("QUALITY");

                        String getTags = "Select TAGNAME From dbi294542.`SORTEDDATABASE.SORTEDDATATAGS` WHERE "
                                + "ID = " + id;
                        PreparedStatement getTagsData = conn.prepareStatement(getTags);
                        ResultSet tagsData = getTagsData.executeQuery();
                        while (tagsData.next()) {
                            Tag tag = Tag.valueOf(tagsData.getString("TAGNAME"));
                            newTags.add(tag);
                        }

                        sorted.add(new SortedData(id, title, description, location, source, relevance, reliability, quality, newTags));
                        System.out.println("Getting sorted object  succeed");

                    }
                }

            }
            System.out.println("Getting sorted object getFromSortedData succeed");
        } catch (SQLException ex) {
            System.out.println("getFromSortedData failed: " + ex);
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
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;

        try {
            for (IData x : data) {
                String query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = '"
                        + Status.NONE.toString() + "' WHERE id = " + x.getId();
                PreparedStatement reset = conn.prepareStatement(query);

                reset.execute();
                System.out.println("Resetting object succeed");
            }

            System.out.println("resetUnsortedData succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("resetUnsortedData failed: " + ex);
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
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = '"
                    + Status.COMPLETED.toString() + "' WHERE id = " + data.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();

            System.out.println("updateStatusUnsortedData succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("updateStatusUnsortedData failed: " + ex);
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
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET TITLE = '" + iData.getTitle()
                    + "', DESCRIPTION = '" + iData.getDescription() + "', LOCATION = '" + "', SOURCE = '" + iData.getSource()
                    + "' WHERE ID=" + iData.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();

            System.out.println("updateUnsortedData succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("updateUnsortedData failed: " + ex);
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
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = '"
                    + Status.DISCARDED.toString() + "' WHERE id = " + iData.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();

            System.out.println("discardUnsortedData succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("discardUnsortedData failed: " + ex);
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
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            Set<Tag> tags = data.getTags();
            if (tags.isEmpty()) {
            tags.addAll(Arrays.asList(Tag.values()));
        }
            //insert to sorteddata
            String query = "INSERT INTO dbi294542.`REQUESTDATABASE.SORTEDDATA` VALUES (ID,?,?,?,?,?)";
            PreparedStatement requestData = conn.prepareStatement(query);
            requestData.setString(1, data.getTitle());
            requestData.setString(2, data.getDescription());
            requestData.setString(3, data.getLocation());
            requestData.setString(4, data.getSource());
            requestData.setInt(5, data.getRequestId());
            requestData.execute();
            
            //Find id from this object
            int id = 0;
            query = "SELECT MAX(ID) FROM dbi294542.`REQUESTDATABASE.SORTEDDATA`";
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            //getting unsorteddata
            while (result.next()) {
                id = result.getInt(1);
            }

            System.out.println("insertDataRequest object insert");
            Iterator it = tags.iterator();
            while (it.hasNext()) {
                // Get tagid
                Object element = it.next();

                //insert into requesttag database
                query = "INSERT INTO dbi294542.`REQUESTDATABASE.SORTEDDATATAGS` VALUES (?,?) ";
                requestData = conn.prepareStatement(query);
                requestData.setInt(1, id);
                requestData.setString(2, element.toString());
                requestData.execute();
            }
            System.out.println("insertDataRequest succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("insertDataRequest failed: " + ex);
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
        if (!openConnection()) {
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
        HashSet<Tag> newTags = new HashSet<Tag>();

        try {
            //build a string with all tha tags
            String query = "SELECT ID FROM dbi294542.`REQUESTDATABASE.SORTEDDATATAGS` ";
            int sizeList = tags.size();
            Iterator it = tags.iterator();
            int aantal = 1;
            while (it.hasNext()) {
                // Get element
                Object element = it.next();
                if (aantal == 1) {
                    query += "WHERE TAGNAME = '" + element.toString() + "' ";
                    aantal++;
                } else {
                    query += "AND ID IN (SELECT ID FROM dbi294542.`REQUESTDATABASE."
                            + "SORTEDDATATAGS` WHERE  TAGNAME ='" + element.toString() + "' ";
                }
            }
            for (int x = 1; x < sizeList; x++) {
                query += ")";
            }
            //making a list with al de id's with correct tags
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            while (result.next()) {
                numbers.add(result.getInt("ID"));
            }

            //Get request data from database with correct id's
            String update = "";
            for (int x : numbers) {
                if (request.size() < 50) {
                    update = "SELECT * FROM dbi294542.`REQUESTDATABASE.SORTEDDATA` WHERE ID = " + x;
                    PreparedStatement updateData = conn.prepareStatement(update);
                    ResultSet resultTag = updateData.executeQuery();
                    while (resultTag.next()) {
                        id = resultTag.getInt("ID");
                        title = resultTag.getString("TITLE");
                        description = resultTag.getString("DESCRIPTION");
                        location = resultTag.getString("LOCATION");
                        source = resultTag.getString("SOURCE");
                        requestId = resultTag.getInt("REQUESTID");

                        String getTags = "Select TAGNAME From dbi294542.`REQUESTDATABASE.SORTEDDATATAGS` WHERE "
                                + "ID = " + id;
                        PreparedStatement getTagsData = conn.prepareStatement(getTags);
                        ResultSet tagsData = getTagsData.executeQuery();
                        while (tagsData.next()) {
                            Tag tag = Tag.valueOf(tagsData.getString("TAGNAME"));
                            newTags.add(tag);
                        }

                        request.add(new DataRequest(id, title, description, location, source, requestId, tags));
                    }
                    System.out.println("getUpdateRequests object succeed");
                }
            }
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("getUpdateRequests failed: " + ex);
        } finally {
            closeConnection();
        }

        return request;
    }

    /**
     * @param id of object
     * @return unsorted-object with correct id
     */
    public synchronized IData getDataItem(int id) {
        if (!openConnection()) {
            return null;
        }
        IData unsorted = null;

        String title;
        String description;
        String location;
        String source;

        if (!openConnection()) {
            return null;
        }

        try {
            String query = "SELECT * FROM dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` WHERE ID = " + id;
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            //getting unsorteddata
            while (result.next() && unsorted == null) {
                title = result.getString("TITLE");
                description = result.getString("DESCRIPTION");
                location = result.getString("LOCATION");
                source = result.getString("SOURCE");
                Status status = Status.valueOf(result.getString("STATUS"));

                unsorted = new UnsortedData(id, title, description, location, source, status);
                System.out.println("Getting object getDataItem succeed");
            }
        } catch (SQLException ex) {
            System.out.println("getDataItem failed: " + ex);
        } finally {
            closeConnection();
        }
        return unsorted;
    }

    /**
     * @param source of object
     * @return list unsorted-objects with correct source
     */
    public synchronized List<IData> getSentData(String source) {
        if (!openConnection()) {
            return null;
        }
        List<IData> unsorted = new ArrayList();

        int id;
        String title;
        String description;
        String location;
        String realSource;

        if (!openConnection()) {
            return null;
        }

        try {
            String query = "SELECT * FROM dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` ";
            if (!source.isEmpty()) {
                query += "WHERE SOURCE = '" + source + "' ";
            }
            query += "ORDER BY ID";
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            //getting unsorteddata
            while (result.next() && unsorted.size() < 50) {
                id = result.getInt("ID");
                title = result.getString("TITLE");
                description = result.getString("DESCRIPTION");
                location = result.getString("LOCATION");
                realSource = result.getString("SOURCE");
                Status status = Status.valueOf(result.getString("STATUS"));

                // TODO Status uitlezen uit database
                unsorted.add(new UnsortedData(id, title, description, location, realSource, status));
                System.out.println("Getting object getSentData succeed");
            }

        } catch (SQLException ex) {
            System.out.println("getSentData failed: " + ex);
        } finally {
            closeConnection();
        }
        return unsorted;
    }

    /**
     * open connection
     */
    private boolean openConnection() {
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
    private void closeConnection() {
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
