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
import Shared.Data.Advice;
import Shared.Data.DataRequest;
import Shared.Data.IDataRequest;
import Shared.Data.INewsItem;
import Shared.Data.ISortedData;
import Shared.Data.NewsItem;
import Shared.Data.Situation;
import Shared.Data.SortedData;
import Shared.Tag;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
    public static void setUpClass() {
        if (ServerMain.sortedDatabaseManager == null) {
            ServerMain.startDatabases();
        }
        myDB = ServerMain.sortedDatabaseManager;
        myDB.resetDatabase();
    }

    @AfterClass
    public static void tearDownClass() {
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

        for (ISortedData data : sortedDataList) {
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
            while (it.hasNext()) {
                if (it.next().equals(Tag.POLICE)) {
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
        for (IDataRequest req : dataRequests) {
            assertNotNull("title was null (getUpdateRequests)", req.getTitle());
            assertNotNull("description was null (getUpdateRequests)", req.getDescription());
            assertNotNull("location was null (getUpdateRequests)", req.getLocation());
            assertNotNull("source was null (getUpdateRequests)", req.getSource());

            assertFalse("id was -1 (getUpdateRequests)", req.getId() == -1);
        }
    }

    @Test
    public void testNewsItems() {
        assertNull(myDB.getNewsItems(0));
        assertNull(myDB.getNewsItems(-1));

        List<INewsItem> items = myDB.getNewsItems(20);
        assertTrue("wrong number of items", items.size() == 6);

        items = myDB.getNewsItems(2);
        assertTrue("wrong number of limited items", items.size() == 2);

        Set<Situation> situations = myDB.getSituations();
        assertTrue("wrong number of situations", situations.size() == 10);

        HashMap<Integer, Situation> sitMap = myDB.getSituationsMap();
        Situation sit = sitMap.get(2);
        assertTrue("situation has wrong number of advices",
                sit.getAdvices().size() == 3);
        for (Advice ad : sit.getAdvices()) {
            String expectedDesc = "???????";
            switch (ad.getID()) {
                case 1:
                    expectedDesc = "Sluit ramen en deuren.";
                    break;
                case 3:
                    expectedDesc = "Kom niet in de buurt van de situatie.";
                    break;
                case 4:
                    expectedDesc = "Gebouw proberen te verlaten.";
                    break;
                default:
                    fail("unrecognised advice ID");
            }
            assertEquals("wrong description", expectedDesc, ad.getDescription());
        }

        // test insert
        INewsItem expectedItem, insertedItem;
        situations = new HashSet<>();
        situations.add(sitMap.get(4));
        situations.add(sitMap.get(6));
        String expectedTitle = "title",
                expectedDesc = "desc",
                expectedLoc = "loc",
                expectedSource = "source";
        int expectedVictims = 9001;
        int expectedID = 7;
        long expectedTime = System.currentTimeMillis();

        expectedItem = new NewsItem(expectedID, expectedTitle, expectedDesc,
                expectedLoc, expectedSource, (HashSet) situations,
                expectedVictims, new Date(expectedTime));

        // tests for return type insertion
        insertedItem = myDB.insertNewsItem(
                new NewsItem(expectedTitle, expectedDesc, expectedLoc,
                        expectedSource, (HashSet) situations, expectedVictims));
        
        this.testNewsItem("insertion return", expectedItem, insertedItem);
        
        // runs same tests on newsItem gotten from getNewsItems
        insertedItem = null;
        for(INewsItem item : myDB.getNewsItems(10)){
            if(item.getId() == expectedID){
                insertedItem = item;
                break;
            }
        }
        this.testNewsItem("retrieved item after insertion",
                expectedItem, insertedItem);

        // test update
        ((NewsItem)insertedItem).addSituation(sitMap.get(2));
        ((NewsItem)expectedItem).addSituation(sitMap.get(2));
        assertTrue(myDB.updateNewsItem(insertedItem));

        // and reruns tests
        insertedItem = null;
        for(INewsItem item : myDB.getNewsItems(10)){
            if(item.getId() == expectedID){
                insertedItem = item;
                break;
            }
        }
        this.testNewsItem("retrieved item after update",
                expectedItem, insertedItem);
    }

    /**
     * fully tests NewsItems
     * @param expected
     * @param tested
     */
    private void testNewsItem(String testDesc, INewsItem expected, INewsItem tested){
        String expectedTitle = expected.getTitle(),
                expectedDesc = expected.getDescription(),
                expectedLoc = expected.getLocation(),
                expectedSource = expected.getSource();
        int expectedVictims = expected.getVictims();
        int expectedID = expected.getId();
        long expectedTime = expected.getDate().getTime();
        Set<Situation> expectedSituations = expected.getSituations();

        assertEquals("wrong ID (" + testDesc + ")",
                expectedID, tested.getId());
        assertEquals("wrong title (" + testDesc + ")",
                expectedTitle, tested.getTitle());
        assertEquals("wrong desc (" + testDesc + ")",
                expectedDesc, tested.getDescription());
        assertEquals("wrong loc (" + testDesc + ")",
                expectedLoc, tested.getLocation());
        assertEquals("wrong victims (" + testDesc + ")",
                expectedVictims, tested.getVictims());
        assertEquals("wrong number of situations (" +
                testDesc + ")", expectedSituations.size(),
                tested.getSituations().size());
        long dateMillis = tested.getDate().getTime();
        assertTrue("date was more than an hour off (" + testDesc + ")",
                (expectedTime + 3600000 > dateMillis)
                || (expectedTime - 3600000 < dateMillis));
    }

}
