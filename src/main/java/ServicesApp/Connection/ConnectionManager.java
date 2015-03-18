/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.Connection;

import ServicesApp.UI.ServicesController;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import Shared.Tag;
import java.util.HashSet;
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
    private int defaultPort = DEFAULT_PORT;
    private String defaultIP;
    private ServicesController guiController;

    public ConnectionManager(ServicesController guiController, String defaultIP) {
        this.guiController = guiController;
        this.defaultIP = defaultIP;
    }

    /**
     * Sets custom value for used port.
     * @param port 
     */
    public void setPort(int port) {
        this.defaultPort = port;
    }

    /**
     * Sends sorted data to server
     *
     * @param data
     */
    public void sendUnsortedData(IData data) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                new Connection(defaultIP, defaultPort).sendUnSortedData(data);
            }
        });
    }

    /**
     * Queries server for sorted data satisfying all given tags.
     * Automatically calls ServicesController.displaySortedData() on completion
     * @param tags
     */
    public void getSortedData(HashSet<Tag> tags) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                List<ISortedData> output = null;
                output = new Connection(defaultIP, defaultPort).getSortedData(tags);
                if(output != null){
                    guiController.displaySortedData(output);
                } else {
                    System.err.println("Unable to retrieve sorted data from server");
                }
                
            }
        });
    }

    /**
     * Gets all data requests conforming to all given tags. Convenience method.
     * Automatically calls ServicesController.displayRequests() on completion.
     * @param tags
     */
    public void getRequests(HashSet<Tag> tags) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                List<IDataRequest> output = null;
                output = new Connection(defaultIP, defaultPort).getDataRequests(tags);
                if(output != null){
                    guiController.displayRequests(output);
                } else {
                    System.err.println("Unable to retrieve requests from server.");
                }                
            }
        });
    }

    /**
     * Updates data with given id with given IData.
     *
     * @param data
     * @param id
     */
    public void updateUnsortedData(IData data) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                new Connection(defaultIP, defaultPort).updateUnsortedData(data);
            }
        });
    }

    /**
     * 
     * @param source
     */
    public void getSentData(String source) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                List<IData> output = null;
                output = new Connection(defaultIP, defaultPort).getSentData(source);
                if(output != null){
                    guiController.displaySentData(output);
                } else {
                    System.err.println("Unable to retrieve sent data from server.");
                }
            }
        });
    }

    /**
     * Queries server for IData with given ID. 
     * Calls ServicesController.displayDataItem() on arrival.
     *
     * @param id
     */
    public void getDataItem(int id) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                IData output = null;
                output = new Connection(defaultIP, defaultPort).getDataItem(id);
                if(output != null){
                    guiController.displayDataItem(output);
                } else {
                    System.err.println("Unable to retrieve specific data item from server.");
                }
            }
        });
    }

}
