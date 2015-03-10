/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.IData;
import Shared.ISortedData;
import Shared.Tag;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
public class ConnectionManager {

    private ExecutorService pool;
    private DatabaseManager myDBManager;

    private int defaultPort = 8189;

    /**
     * Needs to be started only once. Automatically responds to incoming
     * queries.
     *
     * @param manager
     */
    public ConnectionManager(DatabaseManager manager) {
        this.myDBManager = manager;
        this.pool = Executors.newCachedThreadPool();
        this.startListener();
    }

    /**
     * Starts listening to connections at the default port. Automatically
     * forwards them.
     */
    private void startListener() {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                while (true) {

                    try {
                        ServerSocket mySocket = new ServerSocket(defaultPort);
                        System.out.println("Server started");

                        // threadblocking until a connection is made,
                        // then starts a new runnable for it.
                        // repeats until program quit
                        System.out.println("Waiting for client");
                        Socket incoming = mySocket.accept();
                        System.out.println("Handling client requests");
                        pool.execute(new Connection(incoming));

                    } catch (IOException ex) {
                        Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    /**
     * Sends a batch of 50 items of unsorted data to given IP
     */
    private void sendUnsortedData(String destinationIP) {

    }

    /**
     * Sends a List of sorted data to given IP.
     * @param destinationIP
     * @param tags only data with -all- these tag is provided
     */
    private void sendSortedData(String destinationIP, List<Tag> tags) {

    }

    /**
     *
     * @param data
     */
    private void saveSortedData(ISortedData data) {

    }

    private void saveUnsortedData(IData data) {

    }
}
