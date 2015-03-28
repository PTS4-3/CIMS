/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ServerApp.DatabaseManager;
import Shared.DataRequest;
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
import static junit.framework.Assert.assertNotNull;
import org.junit.Before;
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
    private List<UnsortedData> unsorteddata;
    private String source;
    private int ID = 440;
    
    public TestDatabase() {
    }
    
    @Before
    public void setUp() {
        database = new DatabaseManager();
        unsortedData = new ArrayList();
        unsorteddata = new ArrayList();
        source = "FIREDEPARTMENT";
        tag.add(Tag.FIREDEPARTMENT);
        tag.add(Tag.AMBULANCE);
        Status status = Status.NONE;
        sorted = new SortedData(ID, "Brand bejaarde huis","Brand bejaarde huis centrum eindhoven, kamer 73", "Eindhoven centrum",
                source, 2, 2, 2, tag);
        unsorted = new UnsortedData(ID, "Brand bejaarde huis","Brand bejaarde huis centrum eindhoven, kamer 73", "Eindhoven centrum"
                , source, status);        
        unsortedData.add(unsorted);
        request = new DataRequest(ID, "Brand bejaarde huis","Brand bejaarde huis centrum eindhoven, kamer 73", "Eindhoven centrum",
                source, ID, tag);
    }
    
    ///unsorted data testen    
    @Test
    public void insertUnsorted(){
        System.out.println("\r\nTest1 start\r\n");
        boolean test = database.insertToUnsortedData(unsorted);
        assertEquals("Insert unsorted failed", test, true);
        
        System.out.println("\r\nTest2 start\r\n");
        unsorteddata = new ArrayList();
        unsorteddata = database.getFromUnsortedData();
        assertEquals("Have to get a list", unsorteddata.isEmpty(), false);
        
        System.out.println("\r\nTest3 start\r\n");
        boolean succeed = database.resetUnsortedData(unsortedData);
        assertEquals("Fail reset", succeed, true);
        
        System.out.println("\r\nTest4 start\r\n");
        succeed = false;
        succeed = database.updateStatusUnsortedData(unsorted);
        assertEquals("Fail update status", succeed, true);
        
        System.out.println("\r\nTest5 start\r\n");
        succeed = false;
        succeed = database.updateUnsortedData(unsorted);
        assertEquals("Fail update data", succeed, true);
        
        System.out.println("\r\nTest6 start\r\n");
        succeed = false;
        succeed = database.discardUnsortedData(unsorted);
        assertEquals("Fail discard data", succeed, true);
    }

    
    @Test
    public void insertSorted()
    {
        System.out.println("\r\nTest7 start\r\n");
        boolean test = database.insertToSortedData(sorted);
        assertEquals("Insert sorted failed", test, true);
        
        System.out.println("\r\nTest8 start\r\n");
        List<SortedData> sorteddata = new ArrayList();
        sorteddata = database.getFromSortedData(tag);
        assertEquals("Have to get a list", sorteddata.isEmpty(), false);
    }
   
    @Test
    public void insertDataRequest()
    {
        System.out.println("\r\nTest9 start\r\n");
        boolean succeed = database.insertDataRequest(request);
        assertEquals("Insert datarequest failed", succeed, true);
        
        System.out.println("\r\nTest10 start\r\n");
        List<IDataRequest> result = new ArrayList();
        result = database.getUpdateRequests(tag);
        assertEquals("Have to get a list", result.isEmpty(), false);
    }
    
    @Test
    public void getDataItem()
    {
        System.out.println("\r\nTest11 start\r\n");
        IData data1 = database.getDataItem(14);
        assertNotNull("Have to get a object", data1);
        
        System.out.println("\r\nTest12 start\r\n");
        List<IData> data = new ArrayList();
        data = database.getSentData(source);
        assertEquals("Have to get a list", data.isEmpty(), false);
    }

}
