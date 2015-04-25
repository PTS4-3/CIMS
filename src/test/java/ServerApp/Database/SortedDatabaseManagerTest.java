/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import ServerApp.ServerMain;
import Shared.Data.DataRequest;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.Data.SortedData;
import Shared.Tag;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.junit.AfterClass;

/**
 *
 * @author Kargathia
 */
public class SortedDatabaseManagerTest {
    
    private static SortedDatabaseManager myDB;

    private SortedData sortedData;
    private DataRequest request;


    public SortedDatabaseManagerTest() {
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        sortedData = new SortedData(100, "title", "description",
                "location", "source", 1, 2, 3, tags);
        request = new DataRequest("title", "description", "location", "source", 100, tags);
    }

    @BeforeClass
    public static void setUpClass(){
        if(ServerMain.connectionManager == null){
            ServerMain.startDatabases();
        }
        myDB = ServerMain.sortedDatabaseManager;
        myDB.resetDatabase();
    }

    @AfterClass
    public static void tearDownClass(){
        myDB.resetDatabase();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testInsertToSortedData() {
        assertTrue("database failed to insert new sorted data",
                myDB.insertToSortedData(sortedData));
        System.out.println("Next line should be an error (Duplicate entry)");
        assertFalse("database inserted same sorted data twice",
                myDB.insertToSortedData(sortedData));
    }

    @Test
    public void testGetFromSortedData() {
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        List<ISortedData> sortedDataList = myDB.getFromSortedData(tags);
        assertNotNull("No data found in getFromSortedData", sortedDataList);

        for(ISortedData data : sortedDataList){
            // checks not null
            assertNotNull("title was null (getFromSortedData)", data.getTitle());
            assertNotNull("description was null (getFromSortedData)", data.getDescription());
            assertNotNull("location was null (getFromSortedData)", data.getLocation());
            assertNotNull("source was null (getFromSortedData)", data.getSource());
            assertNotNull("tasks was null (getFromSortedData)", data.getTasks());

            assertFalse("id was -1 (getFromSortedData)", data.getId() == -1);

            // checks tags
            boolean hasTag = false;
            Iterator it = data.getTags().iterator();
            while(it.hasNext()){
                if(it.next().equals(Tag.POLICE)){
                    hasTag = true;
                    break;
                }
            }
            assertTrue("Item retrieved in getFromSortedData did not have the requested tag", hasTag);
        }
    }

    @Test
    public void testInsertDataRequest() {
        assertTrue("failed to insert datarequest", myDB.insertDataRequest(request));
        assertTrue("failed to insert second datarequest", myDB.insertDataRequest(request));
    }

    @Test
    public void testGetUpdateRequests() {
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        List<IDataRequest> dataRequests = myDB.getUpdateRequests(tags);
        assertNotNull("output getUpdateRequests was null", dataRequests);
        for(IDataRequest req: dataRequests){
            assertNotNull("title was null (getUpdateRequests)", req.getTitle());
            assertNotNull("description was null (getUpdateRequests)", req.getDescription());
            assertNotNull("location was null (getUpdateRequests)", req.getLocation());
            assertNotNull("source was null (getUpdateRequests)", req.getSource());

            assertFalse("id was -1 (getUpdateRequests)", req.getId() == -1);
        }
    }

}
