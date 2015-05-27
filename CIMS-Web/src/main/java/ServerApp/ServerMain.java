/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import ServerApp.Connection.ConnectionWorker;
import ServerApp.Connection.ConnectionHandler;
import ServerApp.Database.DummyDatabaseManager;
import ServerApp.Database.SortedDatabaseManager;
import ServerApp.Database.TasksDatabaseManager;
import ServerApp.Database.UnsortedDatabaseManager;
import ServerApp.Database.UnsortedDatabaseManager;
import java.io.IOException;
import java.net.InetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author Kargathia
 */
public class ServerMain extends HttpServlet{
    
    public static String SERVER_ADDRESS = "127.0.0.1";

    public static SortedDatabaseManager sortedDatabaseManager = null;
    public static UnsortedDatabaseManager unsortedDatabaseManager = null;
    public static TasksDatabaseManager tasksDatabaseManager = null;
    public static PlanExecutorHandler planExecutorHandler = null;
    public static ConnectionHandler connectionHandler = null;
    public static PushHandler pushHandler = null;

    public static DummyDatabaseManager dummyDatabaseManager = null;

    @Override
    public void init() throws ServletException {

        System.out.println("ServerMain servlet init");
        startDatabases(getServletContext().getRealPath("/WEB-INF/"));
        startConnection();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        startDatabases(null);
        startConnection();
    }

    public static void startDatabases(String baseDir) {
        if(baseDir == null){
            baseDir = "";
        } else if (!baseDir.endsWith("\\")){
            baseDir+= "\\";
        }
        System.out.println("baseDir: " + baseDir);
        sortedDatabaseManager = new SortedDatabaseManager(baseDir + "sorteddatabase.properties");
        unsortedDatabaseManager = new UnsortedDatabaseManager(baseDir + "unsorteddatabase.properties");
        tasksDatabaseManager = new TasksDatabaseManager(baseDir + "taskdatabase.properties");

        planExecutorHandler = new PlanExecutorHandler();

        dummyDatabaseManager = new DummyDatabaseManager();
    }

    public static void startConnection() {
        try {
            connectionHandler = new ConnectionHandler(
                    InetAddress.getByName(SERVER_ADDRESS), 9090);
            new Thread(connectionHandler).start();         
            pushHandler = new PushHandler();
            System.out.println("connection started");
        } catch (IOException e) {
            System.out.println("failed to start connection");
            e.printStackTrace();
        }
    }
}
