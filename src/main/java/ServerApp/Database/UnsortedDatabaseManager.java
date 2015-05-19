/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import Shared.Data.IData;
import Shared.Data.Status;
import Shared.Data.UnsortedData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
public class UnsortedDatabaseManager extends DatabaseManager {

    private final String unsortedDataTable = "UNSORTEDDATA";

    public UnsortedDatabaseManager(String propsFileName) {
        super(propsFileName);
    }

    /**
     * @param data object unsorteddata
     * @return success on attempting to insert unsorted data.
     */
    public IData insertToUnsortedData(IData data) {
        IData output = null;
        int id = -1;
        if (!openConnection() || data == null) {
            return null;
        }

        try {
            String query = "INSERT INTO " + unsortedDataTable + " VALUES (ID,?,?,?,?,?)";
            PreparedStatement unsortedData = conn.prepareStatement(query);
            unsortedData.setString(1, data.getTitle());
            unsortedData.setString(2, data.getDescription());
            unsortedData.setString(3, data.getLocation());
            unsortedData.setString(4, data.getSource());
            unsortedData.setString(5, Status.NONE.toString());
            unsortedData.execute();            

//            System.out.println("insertToUnsortedData succeeded");
            id = super.getMaxID(unsortedDataTable);
        } catch (SQLException ex) {
            System.out.println("insertToUnsortedData failed: " + ex);
        } finally {
            closeConnection();
        }
        return this.getDataItem(id);
    }

    /**
     * @return List unsorteddata first get information from database second
     * change status to INPROCESS
     */
    public List<IData> getFromUnsortedData() {
        List<IData> unsorted = new ArrayList();

        int id;
        String title;
        String description;
        String location;
        String source;

        if (!openConnection()) {
            return null;
        }

        try {
            String query = "SELECT * FROM " + unsortedDataTable + " WHERE STATUS "
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
//                    System.out.println("Getting object" + unsorted.size() + " unsorted succeed");
                }
            }

            //update data
            for (IData x : unsorted) {
                String update = "UPDATE " + unsortedDataTable + " SET STATUS = '"
                        + Status.INPROCESS.toString() + "' WHERE ID = " + x.getId();
                PreparedStatement updateData = conn.prepareStatement(update);
                updateData.execute();
//                System.out.println("Updating unsorted status succeed");
            }

//            System.out.println("getFromUnsortedData succeed");
        } catch (SQLException ex) {
            System.out.println("getFromUnsortedData failed: " + ex);
        } finally {
            closeConnection();
        }
        return unsorted;
    }

    /**
     * @param data list of unsorteddata
     * @return succeed reset status unsorted data
     */
    public boolean resetUnsortedData(List<IData> data) {
        if(data.isEmpty()){
            return true;
        }
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;

        try {
            for (IData x : data) {
                String query = "UPDATE " + unsortedDataTable + " SET STATUS = '"
                        + Status.NONE.toString() + "' WHERE id = " + x.getId();
                PreparedStatement reset = conn.prepareStatement(query);

                reset.execute();
//                System.out.println("Resetting object succeed");
            }

//            System.out.println("resetUnsortedData succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("resetUnsortedData failed: " + ex);
            succeed = false;
        } finally {
            closeConnection();
        }
        return succeed;
    }

    /**
     * @param data object of unsorteddata
     * @return succeed reset status unsorted data to Completed
     */
    public boolean updateStatusUnsortedData(IData data) {
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE " + unsortedDataTable + " SET STATUS = '"
                    + Status.COMPLETED.toString() + "' WHERE id = " + data.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();

//            System.out.println("updateStatusUnsortedData succeeded");
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
    public boolean updateUnsortedData(IData iData) {
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE " + unsortedDataTable + " SET TITLE = '"
                    + iData.getTitle() + "', DESCRIPTION = '"
                    + iData.getDescription() + "', LOCATION = '"
                    + iData.getLocation() + "', SOURCE = '"
                    + iData.getSource() + "', STATUS = '"
                    + Status.NONE + "' WHERE ID=" + iData.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();

//            System.out.println("updateUnsortedData succeeded");
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
    public boolean discardUnsortedData(IData iData) {
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE " + unsortedDataTable + " SET STATUS = '"
                    + Status.DISCARDED.toString() + "' WHERE id = " + iData.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();

//            System.out.println("discardUnsortedData succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("discardUnsortedData failed: " + ex);
        } finally {
            closeConnection();
        }

        return succeed;
    }

    /**
     * @param id of object
     * @return unsorted-object with correct id
     */
    public IData getDataItem(int id) {
        if (!openConnection()) {
            return null;
        }
        IData unsorted = null;

        String title;
        String description;
        String location;
        String source;

        try {
            String query = "SELECT * FROM " + unsortedDataTable + " WHERE ID = " + id;
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
//                System.out.println("Getting object getDataItem succeed");
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
    public List<IData> getSentData(String source) {
        if (!openConnection()) {
            return null;
        }
        List<IData> unsorted = new ArrayList();

        int id;
        String title;
        String description;
        String location;
        String realSource;

        try {
            String query = "SELECT * FROM " + unsortedDataTable + " ";
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
//                System.out.println("Getting object getSentData succeed");
            }

        } catch (SQLException ex) {
            System.out.println("getSentData failed: " + ex);
        } finally {
            closeConnection();
        }
        return unsorted;
    }
}
