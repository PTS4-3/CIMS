/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import ServerApp.ServerMain;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Kargathia
 */
public class ResetDB {

    public ResetDB() {
    }

    @BeforeClass
    public static void setUp() {
        if(ServerMain.connectionManager == null){
            ServerMain.startDatabases();
        }
        ServerMain.sortedDatabaseManager.resetDatabase();
        ServerMain.tasksDatabaseManager.resetDatabase();
        ServerMain.unsortedDatabaseManager.resetDatabase();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void printReset(){
        System.out.println("resetting database");
    }

}
