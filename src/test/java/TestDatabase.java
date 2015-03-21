/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ServerApp.DatabaseManager;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import Shared.SortedData;
import Shared.Status;
import Shared.Tag;
import Shared.UnsortedData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Linda
 */
public class TestDatabase {
    
    private DatabaseManager database;
    private IData unsorted;
    private ISortedData sorted;
    private IDataRequest request;
    private HashSet<Tag> tag = new HashSet();
    private List<IData> unsortedData;
    public TestDatabase() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        database = new DatabaseManager();
        unsortedData = new ArrayList();
        
        tag.add(Tag.FIREDEPARTMENT);
        Status status = Status.NONE;
        sorted = new SortedData(100, "Brand bejaarde huis","Brand bejaarde huis centrum eindhoven, kamer 73", "Eindhoven centrum",
                "FIREDEPARTMENT", 2, 2, 2, tag);
        unsorted = new UnsortedData(100, "Brand bejaarde huis","Brand bejaarde huis centrum eindhoven, kamer 73", "Eindhoven centrum"
                , "FIREDEPARTMENT", status);        
        unsortedData.add(unsorted);
    }
    
    @After
    public void tearDown() {
    }

    
    
    @Test
    public void insertUnsorted(){
        boolean test = database.insertToUnsortedData(unsorted);
        assertEquals("Insert unsorted failed", test, true);
    }
    
    @Test
    public void getUnsorted()
    {
        List<UnsortedData> unsorteddata = new ArrayList();
        unsorteddata = database.getFromUnsortedData();
    }
    
    @Test
    public void insertSorted()
    {
        boolean test = database.insertToSortedData(sorted);
        assertEquals("Insert sorted failed", test, true);
    }
    
    @Test
    public void getSorted()
    {
        List<SortedData> sorteddata = new ArrayList();
        sorteddata = database.getFromSortedData(tag);
        assertNotNull("Have to get a list", sorteddata);
    }
    @Test
    public void resetUnsorted()
    {
        boolean succeed = database.resetUnsortedData(unsortedData);
        assertEquals("Fail reset", succeed, true);
    }
    
    @Test
    public void updateStatusUnsortedData()
    {
        boolean succeed = database.updateStatusUnsortedData(unsorted);
        assertEquals("Fail update status", succeed, true);
    }
    
    @Test
    public void updateUnsortedData()
    {
        boolean succeed = database.updateUnsortedData(unsorted);
        assertEquals("Fail update data", succeed, true);
    }
    
    @Test
    public void discardUnsortedData()
    {
        boolean succeed = database.discardUnsortedData(unsorted);
        assertEquals("Fail discard data", succeed, true);
    }
    
    @Test
    public void insertDataRequest()
    {
        
    }
    
    @Test
    public void getUpdateRequests()
    {
        
    }
}
