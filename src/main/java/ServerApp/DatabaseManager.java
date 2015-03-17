/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

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
    private void configure(){
        this.props = new Properties();
        try(FileInputStream in = new FileInputStream("database.properties")){
            props.load(in);
            
        } catch (FileNotFoundException ex){
            System.out.println("file not found in database configure: " + ex.getMessage());
        } catch (IOException ex){
            System.out.println("IOException in database configure: " + ex.getMessage());
        }
        
        try{
            this.initConnection();
        } catch (SQLException ex){
            System.out.println("failed to init connection: " + ex.getMessage());
        }
    }
    
    /**
     * test properties
     */
    private void initConnection() throws SQLException{
        String url = (String) props.get("url");
        System.out.println(url);
        String username = (String) props.get("username");
        System.out.println(username);
        String password = (String) props.get("password");
        System.out.println(password);
        
        this.conn = DriverManager.getConnection(url, username, password);
    }

    /**
     * @param data object unsorteddata
     * @return success on attempting to insert unsorted data.
     */
    public synchronized boolean insertToUnsortedData(IData data) {
        boolean succeed = false;
        try {
            String query = "INSERT INTO dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` VALUES (?,?,?,?,?,?)";
            PreparedStatement unsortedData = conn.prepareStatement(query);
            unsortedData.setString(2, data.getTitle());
            unsortedData.setString(3, data.getDescription());
            unsortedData.setString(4, data.getLocation());
            unsortedData.setString(5, data.getSource());
            unsortedData.setString(6, Status.NONE.toString());
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
        Status status;

        try {

            String query = "SELECT * FROM dbi294542.`UNSORTEDDATABASE` WHERE STATUS = 'NONE' ORDER BY ID";
            String update = "";
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();
            //getting unsorteddata
            while (result.next() && unsorted.size() < 50) {
                id = result.getInt("ID");
                title = result.getString("TITLE");
                description = result.getString("DESCRIPTION");
                location = result.getString("LOCATION");
                source = result.getString("SOURCE");
                status = Status.valueOf(result.getString("STATUS"));

                unsorted.add(new UnsortedData(id, title, description, location, source, Status.INPROCESS));
                System.out.println("Getting object succeed");
                
            }
            
            //update data
            for(UnsortedData x : unsorted)
            {
            update = "UPDATE dbi294542.`UNSORTEDDATABASE` SET STATUS = 'INPROCESS' WHERE ID = " + x.getId();
            PreparedStatement updateData = conn.prepareStatement(update);
            updateData.execute();
            System.out.println("Updating status succeed");
            }
            
            System.out.println("Data unsorted read succeeded");
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
        boolean succeed = false;
        try {
            //openConnectionSorted();
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
            //closeConnection();
            
           // openConnectionUnsorted();
            //delete from unsorteddata
            query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = 'COMPLEET' WHERE id = " + sorted.getId();
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

        int id;
        String title;
        String description;
        String location;
        String source;
        int relevance;
        int reliability;
        int quality;

        //list of id's with correct tags
        try {
            //openConnectionSorted();

            String query = "SELECT IDSORTEDDATA FROM dbi294542.`SORTEDDATABASE.SORTEDDATATAGS` WHERE TAGNAME = ";
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
                    query += "AND IDSORTEDDATA IN (SELECT IDSORTEDDATA FROM"
                            + "dbi294542.`SORTEDDATABASE.SORTEDDATATAGS` WHERE  '" + element.toString() + "' ";
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
                update = "SELECT * FROM dbi294542.`SORTEDDATABASE.SORTEDDATA` WHERE ID = " + x;
                PreparedStatement updateData = conn.prepareStatement(update);
                ResultSet resultTag = updateData.executeQuery();
                while (resultTag.next() && sorted.size() < 50) {
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
            System.out.println("Getting object succeed");
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
        boolean succeed = false;

        try {
            //openConnectionUnsorted();

            for (IData x : data) {
                String query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = 'NONE' WHERE id = " + x.getId();
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
     * @return succeed reset status unsorted data
     */
    public synchronized boolean updateStatusUnsortedData(IData data)
    {
        boolean succeed = false;
        //dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA`
        return succeed;
    }
    
    /**
     * Updates piece of unsorted data with given id. 
     * @param id
     * @param iData 
     * @return false if id not found
     */
    public synchronized boolean updateUnsortedData(int id, IData iData) {
        boolean succeed = false;
        
        try {
            //openConnectionUnsorted();

                String query = "UPDATE dbi294542.`UNSORTEDDATABASE.UNSORTEDDATA` SET STATUS = 'NONE' WHERE id = " + id;
                PreparedStatement update = conn.prepareStatement(query);

                update.execute();

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
     * closing connection
     */
    public void closeConnection() {
        try {
            conn.close();
            System.out.println("Connection close succeeded");
        } catch (SQLException ex) {
            System.out.println("Connection close failed: " + ex);
        }

    }


    void discardUnsortedData(IData iData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void insertDataRequest(IDataRequest data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
