/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp;

import Shared.ConnState;
import Shared.DataRequest;
import Shared.IData;
import Shared.ISortedData;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
public class ConnectionManager {

    /*
     Connection start ->
     Server: ConnState.CONNECTED
     ------
     Option 1 - Client: ConnState.DONE
     -> Closes down connection
     Option 2 - Client: DataRequest.SORTED_GET
     -> Client: Set<Tag>
     -> Server: List<ISortedData>
     Option 3 - Client: DataRequest.SORTED_SEND
     -> Client: ISortedData
     Option 4 - Client: DataRequest.UNSORTED_GET
     -> Server: List<IData>
     Option 5 - Client: DataRequest.UNSORTED_SEND
     -> Client: IData
     Option 6 - Client: DataRequest.UNSORTED_RESET
     -> Client: List<IData>
     -----
     Return to start, except on closed conn
     */
    private static ExecutorService pool = Executors.newCachedThreadPool();
    public int defaultPort = 8189;
    private String defaultIP;

    private InputStream inStream = null;
    private OutputStream outStream = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    Socket socket = null;

    public ConnectionManager(String defaultIP) {
        this.defaultIP = defaultIP;
        this.defaultPort = 8189;
    }
    
    public ConnectionManager(String defaultIP, int defaultPort) {
        this.defaultIP = defaultIP;
        this.defaultPort = defaultPort;
    }

    /**
     * Connects to Server
     *
     * @param IP
     * @param port
     * @return connection success
     */
    private boolean greetServer(String IP, int port) {
        try {
            socket = new Socket(IP, port);

            this.outStream = socket.getOutputStream();
            this.inStream = socket.getInputStream();

            this.out = new ObjectOutputStream(outStream);
            this.in = new ObjectInputStream(inStream);

            System.out.println("Saying hello to server");
            out.writeObject(ConnState.CONNECTED);
            out.flush();

            // checks whether connection with server is up and running
            boolean doneSayingHello = false;
            while (!doneSayingHello) {
                Object inObject;
                try {
                    inObject = in.readObject();

                    if ((inObject instanceof ConnState)
                            && (ConnState) inObject == ConnState.CONNECTED) {
                        System.out.println("Connected to server");
                        doneSayingHello = true;
                    } else {
                        System.out.println("no valid object as connection "
                                + "confirmation");
                        return false;
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ConnectionManager.class.getName())
                            .log(Level.SEVERE, null, ex);
                    return false;
                }
            }

        } catch (IOException e) {
            System.out.println("IOException ConnectionManager.greetServer(): "
                    + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Closes down the connection - also notifies the server.
     */
    private void closeSocket() {
        try {
            out.writeObject(ConnState.DONE);
            out.flush();
            socket.close();
        } catch (IOException ex) {
            System.out.println("IOException closing down connection: "
                    + ex.getMessage());
            Logger.getLogger(ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sends sorted data to server.
     *
     * @param IP manually provided.
     * @param port manually provided.
     * @param data
     * @return success on attempting to send sorted data.
     */
    public boolean sendSortedData(String IP, int port, ISortedData data) {
        if (!this.greetServer(IP, port)) {
            return false;
        }
        boolean output = false;
        try {
            out.writeObject(DataRequest.SORTED_SEND);
            out.writeObject(data);
            out.flush();
            output = true;
        } catch (IOException ex) {
            Logger.getLogger(ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Sends sorted data to server @ default IP / port
     *
     * @param data
     * @return success
     */
    public boolean sendSortedData(ISortedData data) {
        return this.sendSortedData(defaultIP, defaultPort, data);
    }

    /**
     * Queries server for a batch of unsorted data.
     *
     * @param IP manually provided
     * @param port manually provided
     * @return batch of data. Null on general error.
     */
    public List<IData> getData(String IP, int port) {
        if (!this.greetServer(IP, port)) {
            return null;
        }
        List<IData> output = null;
        try {
            out.writeObject(DataRequest.UNSORTED_GET);
            out.flush();
            Object inObject = in.readObject();
            if (inObject instanceof List) {
                List list = (List) inObject;
                if (list.isEmpty()) {
                    output = new ArrayList<>();
                } else {
                    if (list.get(0) instanceof IData) {
                        output = (List<IData>) list;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Queries server for batch of unsorted data from default IP / port.
     *
     * @return batch.
     */
    public List<IData> getData() {
        return getData(defaultIP, defaultPort);
    }

    /**
     * Signals server that HQ will not process this checked out data.
     *
     * @param data
     * @param IP
     * @param port
     * @return
     */
    public boolean stopWorkingOnData(List<IData> data, String IP, int port) {
        if (!this.greetServer(IP, port)) {
            return false;
        }
        boolean output = false;
        try {
            out.writeObject(DataRequest.UNSORTED_STATUS_RESET);
            out.writeObject(data);
            out.flush();
            output = true;
        } catch (IOException ex) {
            Logger.getLogger(ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
            output = false;
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Signals server that HQ will nto process this list of data.
     *
     * @param data
     * @return
     */
    public boolean stopWorkingOnData(List<IData> data) {
        return this.stopWorkingOnData(data, defaultIP, defaultPort);
    }

    /**
     *
     * @param data
     * @param IP
     * @param port
     * @return
     */
    public boolean discardUnsortedData(IData data, String IP, int port){
        return false;
    }

    /**
     * 
     * @param data
     * @return
     */
    public boolean discardUnsortedData(IData data){
        return this.discardUnsortedData(data, defaultIP, defaultPort);
    }
}
