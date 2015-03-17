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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Kargathia
 */
public class ConnectionManager {

    static ExecutorService pool = Executors.newCachedThreadPool();
    private HeadquartersController guiController = null;
    private int defaultPort = 8189;
    private String defaultIP = "127.0.0.1";

    

    public ConnectionManager(HeadquartersController guiController,
            String defaultIP) {

        this.defaultIP = defaultIP;
        this.defaultPort = 8189;
    }

    public ConnectionManager(HeadquartersController guiController,
            String defaultIP, int defaultPort) {
        this.guiController = guiController;
        this.defaultIP = defaultIP;
        this.defaultPort = defaultPort;
    }

    /**
     * Sends sorted data to server @ default IP / port
     *
     * @param data
     */
    public void sendSortedData(ISortedData data) {
        new Connection().sendSortedData(defaultIP, defaultPort, data);
    }

    /**
     * Queries server for batch of unsorted data from default IP / port.
     * Automatically calls HeadquartersController.displayData(data) on
     * completion
     */
    public void getData() {
        new Connection().getData(defaultIP, defaultPort, guiController);
    }

    /**
     * Signals server that HQ will not process this list of data.
     *
     * @param data
     */
    public void stopWorkingOnData(ArrayList<IData> data) {
        new Connection().stopWorkingOnData(data, defaultIP, defaultPort);
    }

    /**
     * Signals server that this headquarters client is no longer working on
     * given data.
     *
     * @param data
     */
    public void discardUnsortedData(IData data) {
        new Connection().discardUnsortedData(data, defaultIP, defaultPort);
    }

    /**
     * Files a request for an update of given piece of data with the server.
     * @param data
     */
    public void requestUpdate(IDataRequest data) {
        new Connection().requestUpdate(data, defaultIP, defaultPort);
    }
}
