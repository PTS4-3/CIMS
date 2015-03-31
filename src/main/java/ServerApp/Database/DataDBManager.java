/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import Shared.Data.DataRequest;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.Data.SortedData;
import Shared.Data.Status;
import Shared.Data.UnsortedData;
import Shared.Tag;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Kargathia
 */
public class DataDBManager extends BaseDBManager {

    /**
     * @param data object unsorteddata
     * @return success on attempting to insert unsorted data.
     */
    public boolean insertToUnsorted(IData data) {
        boolean succeed = false;

        if (!openConnection()) {
            return false;
        }

        try {
            String query = "INSERT INTO " + UNSORTEDDATA_TABLE + " VALUES (ID,?,?,?,?,?)";
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
    public List<UnsortedData> getFromUnsorted() {
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
            String query = "SELECT * FROM " + UNSORTEDDATA_TABLE + " WHERE STATUS "
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
                String update = "UPDATE " + UNSORTEDDATA_TABLE + " SET STATUS = '"
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
    public boolean insertToSorted(ISortedData sorted) {
        if (!openConnection()) {
            return false;
        }
        Set<Tag> tags = sorted.getTags();
        boolean succeed = false;
        try {
            //insert to sorteddata
            String query = "INSERT INTO " + SORTEDDATA_TABLE + " VALUES (?,?,?,?,?,?,?,?)";
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
                query = "INSERT INTO " + SORTEDDATATAGS_TABLE + " VALUES (?,?) ";
                sortedData = conn.prepareStatement(query);
                sortedData.setInt(1, sorted.getId());
                sortedData.setString(2, element.toString());
                sortedData.execute();
            }

            //delete from unsorteddata
            query = "UPDATE " + UNSORTEDDATA_TABLE + " SET STATUS = '"
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
    public List<SortedData> getFromSorted(HashSet<Tag> info) {
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
            String query = "SELECT ID FROM " + SORTEDDATATAGS_TABLE;
            int sizeList = info.size();
            Iterator it = info.iterator();
            int amount = 1;
            while (it.hasNext()) {
                // Get element
                Object element = it.next();
                if (amount == 1) {
                    query += "WHERE TAGNAME = '" + element.toString() + "' ";
                    amount++;
                } else {
                    query += "AND ID IN (SELECT ID FROM "
                            + SORTEDDATATAGS_TABLE + " WHERE  TAGNAME = '"
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
                    update = "SELECT * FROM " + SORTEDDATA_TABLE + " WHERE ID = "
                            + element.toString();
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

                        String getTags = "Select TAGNAME From " + SORTEDDATATAGS_TABLE + " WHERE "
                                + "ID = " + id;
                        PreparedStatement getTagsData = conn.prepareStatement(getTags);
                        ResultSet tagsData = getTagsData.executeQuery();
                        while (tagsData.next()) {
                            Tag tag = Tag.valueOf(tagsData.getString("TAGNAME"));
                            newTags.add(tag);
                        }

                        sorted.add(new SortedData(
                                id,
                                title,
                                description,
                                location,
                                source,
                                relevance,
                                reliability,
                                quality,
                                newTags));
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
    public boolean resetUnsorted(List<IData> data) {
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;

        try {
            for (IData x : data) {
                String query = "UPDATE " + UNSORTEDDATA_TABLE + " SET STATUS = '"
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
    public boolean updateStatusUnsorted(IData data) {
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE " + UNSORTEDDATA_TABLE + " SET STATUS = '"
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
    public boolean updateUnsorted(IData iData) {
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE " + UNSORTEDDATA_TABLE + " SET TITLE = '"
                    + iData.getTitle() + "', DESCRIPTION = '"
                    + iData.getDescription() + "', LOCATION = '"
                    + iData.getLocation() + "', SOURCE = '"
                    + iData.getSource() + "', STATUS = '"
                    + Status.NONE + "' WHERE ID=" + iData.getId();

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
    public boolean discardUnsorted(IData iData) {
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            String query = "UPDATE " + UNSORTEDDATA_TABLE + " SET STATUS = '"
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
    public boolean insertRequest(IDataRequest data) {
        if (!openConnection()) {
            return false;
        }

        boolean succeed = false;
        try {
            Set<Tag> tags = data.getTags();
            //insert to sorteddata
            String query = "INSERT INTO " + REQUESTS_TABLE + " VALUES (ID,?,?,?,?,?)";
            PreparedStatement requestData = conn.prepareStatement(query);
            requestData.setString(1, data.getTitle());
            requestData.setString(2, data.getDescription());
            requestData.setString(3, data.getLocation());
            requestData.setString(4, data.getSource());
            requestData.setInt(5, data.getRequestId());
            requestData.execute();

            //Find id from this object
            int id = 0;
            query = "SELECT MAX(ID) FROM " + REQUESTS_TABLE;
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
                query = "INSERT INTO " + REQUESTTAGS_TABLE + " VALUES (?,?) ";
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
    public List<IDataRequest> getRequests(HashSet tags) {
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
        int requestId;
        HashSet<Tag> newTags = new HashSet<Tag>();

        try {
            //build a string with all tha tags
            String query = "SELECT ID FROM " + REQUESTTAGS_TABLE;
            int sizeList = tags.size();
            Iterator it = tags.iterator();
            int aantal = 1;
            while (it.hasNext()) {
                // Get element
                Object element = it.next();
                if (aantal == 1) {
                    query += " WHERE TAGNAME = '" + element.toString() + "' ";
                    aantal++;
                } else {
                    query += "AND ID IN (SELECT ID FROM " + REQUESTTAGS_TABLE
                            + " WHERE  TAGNAME ='" + element.toString() + "' ";
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
            Iterator it2 = numbers.iterator();
            while (it2.hasNext()) {
                // Get element
                Object element = it2.next();
                if (request.size() < 50) {
                    update = "SELECT * FROM " + REQUESTS_TABLE + " WHERE ID = "
                            + element.toString();
                    PreparedStatement updateData = conn.prepareStatement(update);
                    ResultSet resultTag = updateData.executeQuery();
                    while (resultTag.next()) {
                        id = resultTag.getInt("ID");
                        title = resultTag.getString("TITLE");
                        description = resultTag.getString("DESCRIPTION");
                        location = resultTag.getString("LOCATION");
                        source = resultTag.getString("SOURCE");
                        requestId = resultTag.getInt("REQUESTID");

                        String getTags = "Select TAGNAME From "
                                + SORTEDDATATAGS_TABLE + " WHERE ID = " + id;
                        PreparedStatement getTagsData = conn.prepareStatement(getTags);
                        ResultSet tagsData = getTagsData.executeQuery();
                        while (tagsData.next()) {
                            Tag tag = Tag.valueOf(tagsData.getString("TAGNAME"));
                            newTags.add(tag);
                        }

                        request.add(new DataRequest(
                                        id,
                                        title,
                                        description,
                                        location,
                                        source,
                                        requestId,
                                        tags));
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
    public IData getUnsortedItem(int id) {
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
            String query = "SELECT * FROM " + UNSORTEDDATA_TABLE + " WHERE ID = " + id;
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
    public List<IData> getSent(String source) {
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
            String query = "SELECT * FROM " + UNSORTEDDATA_TABLE;
            if (!source.isEmpty()) {
                query += " WHERE SOURCE = '" + source + "'";
            }
            query += " ORDER BY ID";
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

}