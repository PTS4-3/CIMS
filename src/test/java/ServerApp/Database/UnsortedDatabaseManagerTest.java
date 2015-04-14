/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import ServerApp.ServerMain;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Kargathia
 */
public class UnsortedDatabaseManagerTest {

    private static UnsortedDatabaseManager myDB;

    public UnsortedDatabaseManagerTest() {
    }

    @BeforeClass
    public static void setUpClass(){
        ServerMain.startDatabases();
        myDB = ServerMain.unsortedDatabaseManager;
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
    public void testInsertToUnsortedData() {
    }

    @Test
    public void testGetFromUnsortedData() {
    }

    @Test
    public void testResetUnsortedData() {
    }

    @Test
    public void testUpdateStatusUnsortedData() {
    }

    @Test
    public void testUpdateUnsortedData() {
    }

    @Test
    public void testDiscardUnsortedData() {
    }

    @Test
    public void testGetDataItem() {
    }

    @Test
    public void testGetSentData() {
    }

}
