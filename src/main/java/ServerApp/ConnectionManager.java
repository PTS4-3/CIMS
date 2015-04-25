/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
public class ConnectionManager {

    public static final Object 
            SORTEDLOCK = "",
            UNSORTEDLOCK = "",
            TASKSLOCK = "";

    private final ExecutorService pool;
    private static PushBuffer buffer = new PushBuffer();
    private static PlanExecutorHandler planExecutorHandler = new PlanExecutorHandler();
    private static int nextID = 0;

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
     * Returns a unique int to serve as client ID.
     * @return
     */
    protected static synchronized int getNextID(){
        return nextID++;
    }

    protected static PushBuffer getBuffer(){
        return buffer;
    }
    
    protected static PlanExecutorHandler getPlanExecutorHandler() {
        return planExecutorHandler;
    }

    /**
     * Starts listening to connections at the default port. Automatically
     * forwards them.
     */
    private void startListener() {
        pool.execute(() -> {
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
//                    System.out.println("Waiting for client");
                    Socket incoming = mySocket.accept();
//                    System.out.println("Handling client requests");
                    pool.execute(new Connection(incoming));

                } catch (IOException ex) {
                    Logger.getLogger(ConnectionManager.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
    }

}
