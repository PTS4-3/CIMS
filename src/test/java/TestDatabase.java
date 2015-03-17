/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ServerApp.DatabaseManager;
import Shared.IData;
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
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
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
    private HashSet<Tag> tag = new HashSet();
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
        
        tag.add(Tag.FIREDEPARTMENT);
        Status status = Status.NONE;
        sorted = new SortedData(1, "Brand bejaarde huis","Brand bejaarde huis centrum echt, kamer 73", "Eindhoven centrum",
                "brandweer", 35, 35, 35, tag);
        unsorted = new UnsortedData(1, "Brand bejaarde huis","Brand bejaarde huis centrum echt, kamer 73", "Eindhoven centrum"
                , "brandweer", status);
        
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    @Test
    public void TestConnectie()
    {
        database.openConnection();
        database.closeConnection();
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
    }
}
