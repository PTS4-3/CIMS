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
import java.net.InetSocketAddress;
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

    private int defaultPort = 8189;

    /**
     * Needs to be started only once. Automatically responds to incoming
     * queries.
     *
     */
    public ConnectionManager() {
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
                System.setProperty("sun.net.useExclusiveBind", "false"); 
                ServerSocket mySocket = null;
                try {
                    mySocket = new ServerSocket();
                    mySocket.setReuseAddress(true);
                    mySocket.bind(new InetSocketAddress(defaultPort));
                } catch (IOException ex) {
                    System.out.println("Unable to start serversocket: "
                            + ex.getMessage());
                    Logger.getLogger(ConnectionManager.class.getName())
                            .log(Level.SEVERE, null, ex);
                    return;
                }
                System.out.println("Server started");

                while (true) {

                    try {

                        // threadblocking until a connection is made,
                        // then starts a new runnable for it.
                        // repeats until program quit
                        System.out.println("Waiting for client");
                        Socket incoming = mySocket.accept();
                        System.out.println("Handling client requests");
                        pool.execute(new Connection(incoming));

                    } catch (IOException ex) {
                        Logger.getLogger(ConnectionManager.class.getName())
                                .log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

}
