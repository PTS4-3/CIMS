/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.Connection;

import HeadquartersApp.UI.HeadquartersController;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Kargathia
 */
public class ConnectionManager {

    public static final int DEFAULT_PORT = 8189;
    
    
    private static ExecutorService pool = Executors.newCachedThreadPool();
    private HeadquartersController guiController = null;
    private String defaultIP = "127.0.0.1";
    private int defaultPort;

    
    public ConnectionManager(HeadquartersController guiController,
            String defaultIP) {

        this.defaultIP = defaultIP;
        this.defaultPort = DEFAULT_PORT;
    }
    
    public void setDefaultPort(int port){
        this.defaultPort = port;
    }

    /**
     * Sends sorted data to server @ default IP / port
     *
     * @param data
     */
    public void sendSortedData(ISortedData data) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                new Connection(defaultIP, defaultPort).sendSortedData(data);
            }
        });
        
    }

    /**
     * Queries server for batch of unsorted data from default IP / port.
     * Automatically calls HeadquartersController.displayData(data) on
     * completion
     */
    public void getData() {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                List<IData> output;
                output = new Connection(defaultIP, defaultPort).getData();
                if(output != null){
                    guiController.displayData(output);
                }              
            }
        });       
    }

    /**
     * Signals server that HQ will not process this list of data.
     *
     * @param data
     */
    public void stopWorkingOnData(ArrayList<IData> data) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                new Connection(defaultIP, defaultPort).stopWorkingOnData(data);
            }
        });
        
    }

    /**
     * Signals server that this headquarters client is no longer working on
     * given data.
     *
     * @param data
     */
    public void discardUnsortedData(IData data) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                new Connection(defaultIP, defaultPort).discardUnsortedData(data);
            }
        });
        
    }

    /**
     * Files a request for an update of given piece of data with the server.
     * @param data
     */
    public void requestUpdate(IDataRequest data) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                new Connection(defaultIP, defaultPort).requestUpdate(data);
            }
        });
        
    }
}
