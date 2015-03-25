/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.Connection;

import ServicesApp.UI.ServicesController;
import Shared.Connection.ConnCommand;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import Shared.Status;
import Shared.Tag;
import Shared.UnsortedData;
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
    private int clientID;
    private ServicesController guiController;

    public ConnectionManager(ServicesController guiController, String defaultIP) {
        this.guiController = guiController;
        this.defaultIP = defaultIP;
        this.getID();
//        this.testMethods();
    }

    /**
     * Testing only. Takes place in lieu of unit tests.
     */
    private void testMethods() {
        this.getDataItem(5);
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.CITIZENS);
        this.getRequests(tags);
        this.getSentData("sourceInput");
        tags.add(Tag.FIREDEPARTMENT);
        this.getSortedData(tags);
        this.subscribeRequests();
        this.subscribeSorted();
        this.sendUnsortedData(new UnsortedData(-1, "servicesTitle", "desc", "loc", "source", Status.NONE));
        this.updateUnsortedData(new UnsortedData(5, "updateTitle", "updateDesc", "updateLoc", "updateSource", Status.NONE));
        this.getNewRequests();
        this.getNewSorted();
    }

    /**
     * Queries server to be assigned his unique ID. Is executed on main thread,
     * as later operations rely on this being completed.
     */
    private void getID() {
        this.clientID = new Connection(defaultIP, defaultPort).getClientID();
        if (this.clientID == -1) {
            System.err.println("Error retrieving clientID from server");
        } else {
            System.out.println("clientID: " + this.clientID);
        }
    }

    /**
     * Sets custom value for used port.
     *
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
        if (data == null) {
            System.err.println("Null parameter in sendUnsortedData");
            return;
        }
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).sendUnSortedData(data);
        });
    }

    /**
     * Queries server for sorted data satisfying all given tags. Automatically
     * calls ServicesController.displaySortedData() on completion
     *
     * @param tags
     */
    public void getSortedData(HashSet<Tag> tags) {
        if (tags == null) {
            System.err.println("Null parameter in getSortedData");
            return;
        }
        pool.execute(() -> {
            List<ISortedData> output = null;
            output = new Connection(defaultIP, defaultPort).getSortedData(tags);
            if (output != null) {
                guiController.displaySortedData(output);
            } else {
                System.err.println("Unable to retrieve sorted data from server");
            }
        });
    }

    /**
     * Gets all data requests conforming to all given tags. Convenience method.
     * Automatically calls ServicesController.displayRequests() on completion.
     *
     * @param tags
     */
    public void getRequests(HashSet<Tag> tags) {
        if (tags == null) {
            System.err.println("Null parameter in getRequests");
            return;
        }
        pool.execute(() -> {
            List<IDataRequest> output = null;
            output = new Connection(defaultIP, defaultPort).getDataRequests(tags);
            if (output != null) {
                guiController.displayRequests(output);
            } else {
                System.err.println("Unable to retrieve requests from server.");
            }
        });
    }

    /**
     * Updates data with given id with given IData.
     *
     * @param data
     */
    public void updateUnsortedData(IData data) {
        if (data == null) {
            System.err.println("Null parameter in updateUnsortedData");
            return;
        }
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).updateUnsortedData(data);
        });
    }

    /**
     *
     * @param source
     */
    public void getSentData(String source) {
        if (source == null) {
            System.err.println("Null parameter in getSentData");
            return;
        }
        pool.execute(() -> {
            List<IData> output = null;
            output = new Connection(defaultIP, defaultPort).getSentData(source);
            if (output != null) {
                guiController.displaySentData(output);
            } else {
                System.err.println("Unable to retrieve sent data from server.");
            }
        });
    }

    /**
     * Queries server for IData with given ID. Calls
     * ServicesController.displayDataItem() on arrival.
     *
     * @param id
     */
    public void getDataItem(int id) {
        pool.execute(() -> {
            IData output = null;
            output = new Connection(defaultIP, defaultPort).getDataItem(id);
            if (output != null) {
                guiController.displayDataItem(output);
            } else {
                System.err.println("Unable to retrieve specific data item from server.");
            }
        });
    }

    /**
     * Subscribes this client to updates of all new sorted data items. call
     * getNewSorted to collect.
     */
    public void subscribeSorted() {
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).subscribeSorted(this.clientID);
            guiController.notifyConnDone(ConnCommand.SORTED_SUBSCRIBE);
        });
    }

    /**
     * Unsubscribes this client from updates. Does nothing if client wasn't
     * subscribed.
     */
    public void unsubscribeSorted() {
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).unsubscribeSorted(this.clientID);
            guiController.notifyConnDone(ConnCommand.SORTED_UNSUBSCRIBE);
        });
    }

    /**
     * Subscribes this client to updates of all new data requests submitted to
     * server. Call getNewRequests to collect.
     */
    public void subscribeRequests() {
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).subscribeRequests(this.clientID);
            guiController.notifyConnDone(ConnCommand.UPDATE_REQUEST_SUBSCRIBE);
        });
    }

    /**
     * Unsubscribes this client from updates. Does nothing if client wasn't
     * subscribed.
     */
    public void unsubscribeRequests() {
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).unsubscribeRequests(this.clientID);
            guiController.notifyConnDone(ConnCommand.UPDATE_REQUEST_UNSUBSCRIBE);
        });
    }

    /**
     * Collects all new sorted data collected on server since last call. Client
     * needs to have called subscribeSorted() for this to do anything.
     */
    public void getNewSorted() {
        pool.execute(() -> {
            List<ISortedData> output
                    = new Connection(defaultIP, defaultPort).getNewSorted(this.clientID);
            if (output != null) {
                this.guiController.displayNewData(output);
            } else {
                System.err.println("Unable to retrieve new Sorted Data from buffer in server.");
            }
        });
    }

    /**
     * Collects all new requests collected on server since last call. Client
     * needs to have called subscribeRequests() for this to do anything.
     */
    public void getNewRequests() {
        pool.execute(() -> {
            List<IDataRequest> output
                    = new Connection(defaultIP, defaultPort).getNewRequests(this.clientID);
            if (output != null) {
                this.guiController.displayNewRequests(output);
            } else {
                System.err.println("Unable to retrieve new Requests from buffer in server.");
            }
        });
    }

}
