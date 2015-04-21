/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import ServerApp.ServerMain;
import Shared.Data.DataRequest;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.Data.SortedData;
import Shared.Data.Status;
import Shared.Tag;
import java.sql.Connection;
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
 * @author Kargathia
 */
public class SortedDatabaseManager extends DatabaseManager {

    private final String
            sortedDataTable = "SORTEDDATA",
            sortedDataTagsTable = "SORTEDDATATAGS",
            requestsTable = "REQUEST",
            requestTagsTable = "REQUESTTAGS";


    
    public SortedDatabaseManager(String propsFileName){
        super(propsFileName);
    }

    /**
     * @param sorted object sorteddata
     * @return succeed on attempting to insert sorted data.
     */
    public boolean insertToSortedData(ISortedData sorted) {
        if (!openConnection()) {
            return false;
        }
        Set<Tag> tags = sorted.getTags();
        boolean succeed = false;
        try {
            //insert to sorteddata
            String query = "INSERT INTO " + sortedDataTable + " VALUES (?,?,?,?,?,?,?,?)";
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

//            System.out.println("Insert sortedData succeeded");

            Iterator it = tags.iterator();
            while (it.hasNext()) {
                // Get element
                Object element = it.next();

                //insert into sorteddatatags database
                query = "INSERT INTO " + sortedDataTagsTable + " VALUES (?,?) ";
                sortedData = conn.prepareStatement(query);
                sortedData.setInt(1, sorted.getId());
                sortedData.setString(2, element.toString());
                sortedData.execute();
            }

            ServerMain.unsortedDatabaseManager.updateStatusUnsortedData(sorted);

//            System.out.println("insertToSortedData succeeded");

            succeed = true;
        } catch (SQLException ex) {
            System.out.println("insertToSortedData failed: " + ex);
            succeed = false;
        } finally {
            closeConnection();
        }
        return succeed;
    }

    /**
     * @param info list of tags
     * @return List sorteddata
     */
    public List<SortedData> getFromSortedData(HashSet<Tag> info) {
        if (!openConnection()) {
            return null;
        }

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

        try {
            String query = "SELECT DATAID FROM " + sortedDataTagsTable + " ";
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
                    query += "AND DATAID IN (SELECT DATAID FROM"
                            + " " + sortedDataTagsTable + " WHERE  TAGNAME = '"
                            + element.toString() + "' ";
                }
            }
            for (int x = 1; x < sizeList; x++) {
                query += ")";
            }

            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            while (result.next()) {
                numbers.add(result.getInt("DATAID"));
            }

            //make list of object with correct id's
            String update = "";
            Iterator it2 = numbers.iterator();
            while (it2.hasNext()) {
                // Get element
                Object element = it2.next();
                if (sorted.size() < 50) {
                    update = "SELECT * FROM " + sortedDataTable + " WHERE ID = " + element.toString();
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

                        String getTags = "Select TAGNAME From " + sortedDataTagsTable
                                + " WHERE "
                                + "DATAID = " + id;
                        PreparedStatement getTagsData = conn.prepareStatement(getTags);
                        ResultSet tagsData = getTagsData.executeQuery();
                        while (tagsData.next()) {
                            Tag tag = Tag.valueOf(tagsData.getString("TAGNAME"));
                            newTags.add(tag);
                        }

                        sorted.add(new SortedData(id, title, description, 
                                location, source, relevance,
                                reliability, quality, newTags));
//                        System.out.println("Getting sorted object  succeed");

                    }
                }

            }
//            System.out.println("Getting sorted object getFromSortedData succeed");
        } catch (SQLException ex) {
            System.out.println("getFromSortedData failed: " + ex);
            return null;
        } finally {
            closeConnection();
        }
        return sorted;
    }

    /**
     * @param data object of datarequest
     * @return succeed if succeeded
     */
    public boolean insertDataRequest(IDataRequest data) {
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            Set<Tag> tags = data.getTags();
            //insert to sorteddata
            String query = "INSERT INTO " + requestsTable + " VALUES (ID,?,?,?,?,?)";
            PreparedStatement requestData = conn.prepareStatement(query);
            requestData.setString(1, data.getTitle());
            requestData.setString(2, data.getDescription());
            requestData.setString(3, data.getLocation());
            requestData.setString(4, data.getSource());
            requestData.setInt(5, data.getRequestId());
            requestData.execute();

            //Find id from this object
            int id = 0;
            query = "SELECT MAX(ID) FROM " + requestsTable;
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            //getting unsorteddata
            while (result.next()) {
                id = result.getInt(1);
            }

//            System.out.println("insertDataRequest object insert");
            Iterator it = tags.iterator();
            while (it.hasNext()) {
                // Get tagid
                Object element = it.next();

                //insert into requesttag database
                query = "INSERT INTO " + requestTagsTable + " VALUES (?,?) ";
                requestData = conn.prepareStatement(query);
                requestData.setInt(1, id);
                requestData.setString(2, element.toString());
                requestData.execute();
            }
//            System.out.println("insertDataRequest succeeded");
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("insertDataRequest failed: " + ex);
            succeed = false;
        } finally {
            closeConnection();
        }

        return succeed;
    }

    /**
     * @param tags list of tags
     * @return succeed if succeeded
     */
    public List<IDataRequest> getUpdateRequests(HashSet tags) {
        if (!openConnection()) {
            return null;
        }

        boolean succeed = false;
        List<IDataRequest> request = new ArrayList();
        HashSet<Integer> numbers = new HashSet<Integer>();

        int id;
        String title;
        String description;
        String location;
        String source;
        int dataID;
        HashSet<Tag> newTags = new HashSet<Tag>();

        try {
            //build a string with all tha tags
            String query = "SELECT REQUESTID FROM " + requestTagsTable + " ";
            int sizeList = tags.size();
            Iterator it = tags.iterator();
            int amount = 1;
            while (it.hasNext()) {
                // Get element
                Object element = it.next();
                if (amount == 1) {
                    query += "WHERE TAGNAME = '" + element.toString() + "' ";
                    amount++;
                } else {
                    query += "AND REQUESTID IN (SELECT REQUESTID FROM " + requestTagsTable +
                            " WHERE  TAGNAME ='" + element.toString() + "' ";
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
            Iterator it2 = numbers.iterator();
            while (it2.hasNext()) {
                // Get element
                Object element = it2.next();
                if (request.size() < 50) {
                    update = "SELECT * FROM " + requestsTable + " WHERE ID = "
                            + element.toString();
                    PreparedStatement updateData = conn.prepareStatement(update);
                    ResultSet resultTag = updateData.executeQuery();
                    while (resultTag.next()) {
                        id = resultTag.getInt("ID");
                        title = resultTag.getString("TITLE");
                        description = resultTag.getString("DESCRIPTION");
                        location = resultTag.getString("LOCATION");
                        source = resultTag.getString("SOURCE");
                        dataID = resultTag.getInt("DATAID");

                        String getTags = "Select TAGNAME From "
                                + requestTagsTable + " WHERE "
                                + "REQUESTID = " + id;
                        PreparedStatement getTagsData = conn.prepareStatement(getTags);
                        ResultSet tagsData = getTagsData.executeQuery();
                        while (tagsData.next()) {
                            Tag tag = Tag.valueOf(tagsData.getString("TAGNAME"));
                            newTags.add(tag);
                        }

                        request.add(new DataRequest(id, title, description, location, source, dataID, tags));
                    }
//                    System.out.println("getUpdateRequests object succeed");
                }
            }
            succeed = true;
        } catch (SQLException ex) {
            System.out.println("getUpdateRequests failed: " + ex);
            return null;
        } finally {
            closeConnection();
        }

        return request;
    }

    @Deprecated
    SortedData getFromSortedData(int outputDataID) {
        return null;
    }

}
