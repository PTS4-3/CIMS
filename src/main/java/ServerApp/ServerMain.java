/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import ServerApp.ConnectionHandler.EchoWorker;
import ServerApp.ConnectionHandler.NioServer;
import ServerApp.Database.DummyDatabaseManager;
import ServerApp.Database.SortedDatabaseManager;
import ServerApp.Database.TasksDatabaseManager;
import ServerApp.Database.UnsortedDatabaseManager;
import ServerApp.Database.UnsortedDatabaseManager;
import java.io.IOException;

/**
 *
 * @author Kargathia
 */
public class ServerMain {

    public static SortedDatabaseManager sortedDatabaseManager = null;
    public static UnsortedDatabaseManager unsortedDatabaseManager = null;
    public static TasksDatabaseManager tasksDatabaseManager = null;
    public static PlanExecutorHandler planExecutorHandler = null;
    public static NioServer nioServerManager = null;

    public static DummyDatabaseManager dummyDatabaseManager = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        planExecutorHandler = new PlanExecutorHandler();
        startDatabases();
        startConnection();
    }

    public static void startDatabases() {
        sortedDatabaseManager = new SortedDatabaseManager("sorteddatabase.properties");
        unsortedDatabaseManager = new UnsortedDatabaseManager("unsorteddatabase.properties");
        tasksDatabaseManager = new TasksDatabaseManager("taskdatabase.properties");
//       connectionManager = new ConnectionManager();

        dummyDatabaseManager = new DummyDatabaseManager();
    }

    public static void startConnection() {
        try {
//            EchoWorker worker = new EchoWorker();
//            new Thread(worker).start();

            nioServerManager = new NioServer(null, 9090);
            new Thread(nioServerManager).start();
            System.out.println("connection started");
        } catch (IOException e) {
            System.out.println("failed to start server");
            e.printStackTrace();
        }
    }
}
