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

/**
 *
 * @author Kargathia
 */
public class SortedDatabaseManagerTest {
    
    private SortedDatabaseManager myDB;


    public SortedDatabaseManagerTest() {
    }

    @BeforeClass
    public void setUpTests(){
        ServerMain.main(null);
        myDB = ServerMain.sortedDatabaseManager;
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
    }

    @Test
    public void testGetFromSortedData() {
    }

    @Test
    public void testInsertDataRequest() {
    }

    @Test
    public void testGetUpdateRequests() {
    }

}
