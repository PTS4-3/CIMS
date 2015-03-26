/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.Connection;

import HeadquartersApp.UI.HeadquartersController;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Kargathia
 */
public class ConnectionManager {

    public static final int DEFAULT_PORT = 8189;
    
    
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private HeadquartersController guiController = null;
    private String defaultIP = "127.0.0.1";
    private int defaultPort;

    
    public ConnectionManager(HeadquartersController guiController,
            String defaultIP) {

        this.defaultIP = defaultIP;
        this.defaultPort = DEFAULT_PORT;
        this.guiController = guiController;
        this.testMethods();
    }

    /**
     * Testing only. Takes place in lieu of unit tests.
     */
    private void testMethods(){
        this.getData();
        this.discardUnsortedData(new UnsortedData("discardTitle", "discardDesc", "discardLoc", "discardSource"));
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        this.requestUpdate(new DataRequest(1, "requestTitle", "reqDesc", "recLoc", "recSource", 2, tags));
        tags.add(Tag.AMBULANCE);
        this.sendSortedData(new SortedData(2, "sortTitle", "sortDesc", "sortLoc", "sortSource", 3, 2, 1, tags));
        ArrayList<IData> data = new ArrayList<>();
        data.add(new UnsortedData(3, "resetTitle", "resetDesc", "resetLoc", "resetSource", Status.NONE));
        this.stopWorkingOnData(data);
    }
    
    public void setDefaultPort(int port){
        this.defaultPort = port;
    }

    /**
     * Terminates the active pool, in preparation for program shutdown.
     */
    public void close(){
        pool.shutdown();
    }

    /**
     * Sends sorted data to server @ default IP / port
     *
     * @param data
     */
    public void sendSortedData(ISortedData data) {
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).sendSortedData(data);
        });
        
    }

    /**
     * Queries server for batch of unsorted data from default IP / port.
     * Automatically calls HeadquartersController.displayData(data) on
     * completion
     */
    public void getData() {
        pool.execute(() -> {
            List<IData> output;
            output = new Connection(defaultIP, defaultPort).getData();
            if(output != null){
                guiController.displayData(output);
            } else {
                System.err.println("Unable to retrieve Unsorted Data from server.");              
            }
        });       
    }

    /**
     * Signals server that HQ will not process this list of data.
     *
     * @param data
     */
    public void stopWorkingOnData(ArrayList<IData> data) {
        if(data == null){
            System.err.println("Null parameter in stopWorkingOnData");
            return;
        }
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).stopWorkingOnData(data);
        });
    }

    /**
     * Signals server that this headquarters client is no longer working on
     * given data.
     *
     * @param data
     */
    public void discardUnsortedData(IData data) {
        if(data == null){
            System.err.println("Null parameter in discardUnsortedData");
            return;
        }
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).discardUnsortedData(data);
        });
        
    }

    /**
     * Files a request for an update of given piece of data with the server.
     * @param data
     */
    public void requestUpdate(IDataRequest data) {
        if(data == null){
            System.err.println("Null parameter in requestUpdate");
            return;
        }
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).requestUpdate(data);
        });
        
    }
}
