/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.SortedData;
import Shared.Status;
import Shared.Tag;
import Shared.UnsortedData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Linda
 */
public class DatabaseManager {

    private Connection conn;

    /**
     *
     */
    public DatabaseManager() {

    }

    /**
     * @param data object unsorteddata
     * @return success on attempting to insert unsorted data.
     */
    public synchronized boolean insertToUnsortedData(UnsortedData data) {
        boolean succeed = false;
        try {
            openConnection();
            String query = "INSERT INTO UNSORTEDDATA VALUES (?,?,?,?,?,?)";
            PreparedStatement unsortedData = conn.prepareStatement(query);
            unsortedData.setString(2, data.getTitle());
            unsortedData.setString(3, data.getDescription());
            unsortedData.setString(4, data.getLocation());
            unsortedData.setString(5, data.getSource());
            //unsortedData.setString(6, data.getStatus());
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
     * @return List unsorteddata
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
            openConnection();

            String query = "SELECT * FROM UNSORTEDDATA ORDER BY ID";
            String update = null;
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();
            while (result.next() && unsorted.size() < 50) {
                id = result.getInt("ID");
                title = result.getString("TITLE");
                description = result.getString("DESCRIPTION");
                location = result.getString("LOCATION");
                source = result.getString("SOURCE");
                status = Status.valueOf(result.getString("STATUS"));

                if (!status.equals(Status.INPROCESS)) {

                    unsorted.add(new UnsortedData(id, title, description, location, source, Status.INPROCESS));

                    update = "UPDATE UNSORTEDDATA SET STATUS = 'INPROCESS' WHERE id = " + id;
                    PreparedStatement updateData = conn.prepareStatement(update);
                    updateData.execute();
                }
            }
            System.out.println("Data unsorted read succeeded");

        } catch (SQLException ex) {
            System.out.println("Data unsorted read succeeded");
        } finally {
            closeConnection();
        }
        return unsorted;
    }

    /**
     * @param sorted object sorteddata
     * @param unsorted object unsorteddata
     * @return success on attempting to insert sorted data.
     */
    public synchronized boolean insertToSortedData(SortedData sorted, UnsortedData unsorted) {
        boolean succeed = false;
        try {
            openConnection();
            //insert to sorteddata
            String query = "INSERT INTO SORTEDDATA VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement sortedData = conn.prepareStatement(query);
            sortedData.setInt(1, sorted.getId());
            sortedData.setString(2, sorted.getTitle());
            sortedData.setString(3, sorted.getDescription());
            sortedData.setString(4, sorted.getLocation());
            sortedData.setString(5, sorted.getSource());
            sortedData.setInt(6, sorted.getRelevance());
            sortedData.setInt(7, sorted.getReliability());
            sortedData.setInt(8, sorted.getQuality());
            //unsortedData.setString(9, sorted.getStatus());
            sortedData.execute();

            System.out.println("Insert sortedData succeeded");

            //delete from unsorteddata
            query = "DELETE FROM UNSORTEDDATA WHERE ID = " + unsorted.getId();
            PreparedStatement unsortedData = conn.prepareStatement(query);
            unsortedData.execute();

            System.out.println("Delete from unsortedData succeeded");

            succeed = true;
        } catch (SQLException ex) {
            System.out.println("Insert sortedData failed: " + ex);
        } finally {
            closeConnection();
        }
        return succeed;
    }

    /**
     * @return List sorteddata
     */
    public synchronized List<SortedData> getFromSortedData() {
        List<SortedData> sorted = new ArrayList();

        int id;
        String title;
        String description;
        String location;
        String source;
        int relevance;
        int reliability;
        int quality;
        HashSet<Tag> tags = new HashSet();

        try {
            openConnection();

            String query = "SELECT * FROM SORTEDDATA ORDER BY ID";
            String update = null;
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();
            
            while (result.next() && sorted.size() < 50) {
                id = result.getInt("ID");
                title = result.getString("TITLE");
                description = result.getString("DESCRIPTION");
                location = result.getString("LOCATION");
                source = result.getString("SOURCE");
                relevance = result.getInt("RELEVANCE");
                reliability = result.getInt("RELIABILITY");
                quality = result.getInt("QUALITY");

                update = "SELECT * FROM SORTEDDATATAGS WHERE SORTEDDATAID = " + id;
                PreparedStatement updateData = conn.prepareStatement(update);
                ResultSet resultTag = updateData.executeQuery();
                while (resultTag.next()) {
                    Tag tag = Tag.valueOf(resultTag.getString("TAGNAME"));
                    tags.add(tag);
                }
                
                sorted.add(new SortedData(id, title, description, location, source, relevance, reliability
                        ,quality, tags));
                tags.removeAll(tags);
            }
            System.out.println("Data sorted read succeeded");

        } catch (SQLException ex) {
            System.out.println("Data sorted read succeeded");
        } finally {
            closeConnection();
        }
        return sorted;
    }

    /**
     * opening connection
     */
    private void openConnection() {
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@fhictora01.fhict.local:1521:fhictora", "dbi294542", "vl4ldKvhy8");
            System.out.println("Connection open succeeded");
        } catch (SQLException ex) {
            System.out.println("Connection open failed: " + ex);
        }

    }

    /**
     * closing connection
     */
    private void closeConnection() {
        try {
            conn.close();
            System.out.println("Connection close succeeded");
        } catch (SQLException ex) {
            System.out.println("Connection close failed: " + ex);
        }

    }
}
