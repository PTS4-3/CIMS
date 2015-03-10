/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp;

import Shared.IData;
import Shared.ISortedData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Kargathia
 */
public class ConnectionManager {

    private static ExecutorService pool = Executors.newCachedThreadPool();
    public static int defaultPort = 8189;
    private String defaultIP;

    public ConnectionManager(String defaultIP){
        this.defaultIP = defaultIP;
    }

    /**
     * Sends sorted data to server.
     * @param IP manually provided.
     * @param port manually provided.
     * @param data
     * @return success on attempting to send sorted data.
     */
    public boolean sendSortedData(String IP, int port, ISortedData data){
        return false;
    }

    /**
     * Sends sorted data to server @ default IP / port
     * @param data
     * @return success
     */
    public boolean sendSortedData(ISortedData data){
        return this.sendSortedData(defaultIP, defaultPort, data);
    }

    /**
     * Queries server for a batch of unsorted data.
     * @param IP manually provided
     * @param port manually provided
     * @return batch of data.
     * Null on general error.
     */
    public List<IData> getData(String IP, int port){
        return null;
    }

    /**
     * Queries server for batch of unsorted data from default IP / port.
     * @return batch.
     */
    public List<IData> getData(){
        return getData(defaultIP, defaultPort);
    }
}
