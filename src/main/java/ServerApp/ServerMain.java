/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import ServerApp.Database.DummyDatabaseManager;
import ServerApp.Database.SortedDatabaseManager;
import ServerApp.Database.TasksDatabaseManager;
import ServerApp.Database.UnsortedDatabaseManager;
import ServerApp.Database.UnsortedDatabaseManager;

/**
 *
 * @author Kargathia
 */
public class ServerMain{

    public static SortedDatabaseManager sortedDatabaseManager = null;
    public static UnsortedDatabaseManager unsortedDatabaseManager = null;
    public static TasksDatabaseManager tasksDatabaseManager = null;
    public static ConnectionManager connectionManager = null;

    public static DummyDatabaseManager dummyDatabaseManager = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       startDatabases();
    }

    public static void startDatabases(){
       sortedDatabaseManager = new SortedDatabaseManager("sorteddatabase.properties");
       unsortedDatabaseManager = new UnsortedDatabaseManager("unsorteddatabase.properties");
       tasksDatabaseManager = new TasksDatabaseManager("taskdatabase.properties");
       connectionManager = new ConnectionManager();

       dummyDatabaseManager = new DummyDatabaseManager();
    }
}
