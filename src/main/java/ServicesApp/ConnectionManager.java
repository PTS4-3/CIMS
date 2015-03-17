/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp;

import ServicesApp.UI.ServicesController;
import Shared.ConnState;
import Shared.ConnCommand;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import Shared.Tag;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
public class ConnectionManager {

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

    public ConnectionManager(ServicesController guiController, 
            String defaultIP, int defaultPort) {
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
                    Logger.getLogger(ServicesApp.ConnectionManager.class.getName())
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
            Logger.getLogger(ServicesApp.ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sends sorted data to server.
     *
     * @param IP manually provided.
     * @param port manually provided.
     * @param data
     * @return success on attempting to send piece of unsorted data.
     */
    public boolean sendUnSortedData(String IP, int port, IData data) {
        if (!this.greetServer(IP, port)) {
            return false;
        }
        boolean output = false;
        try {
            out.writeObject(ConnCommand.UNSORTED_SEND);
            out.writeObject(data);
            out.flush();
            output = true;
        } catch (IOException ex) {
            Logger.getLogger(ServicesApp.ConnectionManager.class.getName())
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
    public boolean sendUnsortedData(IData data) {
        return this.sendUnSortedData(defaultIP, defaultPort, data);
    }

    /**
     * Queries server for sorted data that has all given tags.
     *
     * @param IP manually provided
     * @param port manually provided
     * @param tags
     * @return batch of data. Null on general error.
     */
    public List<ISortedData> getSortedData(String IP, int port, HashSet<Tag> tags) {
        if (!this.greetServer(IP, port)) {
            return null;
        }
        List<ISortedData> output = null;
        try {
            out.writeObject(ConnCommand.SORTED_GET);
            out.writeObject(tags);
            out.flush();
            Object inObject = in.readObject();
            if (inObject instanceof List) {
                List list = (List) inObject;
                if (list.isEmpty()) {
                    output = new ArrayList<>();
                } else {
                    if (list.get(0) instanceof ISortedData) {
                        output = (List<ISortedData>) list;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ServicesApp.ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Queries server for batch of sorted data from default IP / port.
     *
     * @param tags
     * @return batch.
     */
    public List<ISortedData> getSortedData(HashSet<Tag> tags) {
        return this.getSortedData(defaultIP, defaultPort, tags);
    }
    
    /**
     * Gets all data requests conforming to all given tags. Custom IP/port
     * @param tags
     * @param IP
     * @param port
     * @return 
     */
    public List<IDataRequest> getDataRequests(HashSet<Tag> tags, String IP, int port){
         if (!this.greetServer(IP, port)) {
            return null;
        }
        List<IDataRequest> output = null;
        try {
            out.writeObject(ConnCommand.UNSORTED_UPDATE_REQUEST_GET);
            out.writeObject(tags);
            out.flush();
            Object inObject = in.readObject();
            if (inObject instanceof List) {
                List list = (List) inObject;
                if (list.isEmpty()) {
                    output = new ArrayList<>();
                } else {
                    if (list.get(0) instanceof IDataRequest) {
                        output = (List<IDataRequest>) list;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ServicesApp.ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Gets all data requests conforming to all given tags. Convenience method.
     * @param tags
     * @return
     */
    public List<IDataRequest> getRequests(HashSet<Tag> tags) {
        return this.getDataRequests(tags, defaultIP, defaultPort);
    }

    /**
     * Updates data with given id with given IData.
     * @param data
     * @param id
     * @param IP
     * @param port
     * @return
     */
    public boolean updateUnsortedData(IData data, int id, String IP, int port){
        if (!this.greetServer(IP, port)) {
            return false;
        }
        boolean output = false;
        try {
            out.writeObject(ConnCommand.UNSORTED_UPDATE_SEND);
            out.writeObject(id);
            out.writeObject(data);
            out.flush();
            output = true;
        } catch (IOException ex) {
            Logger.getLogger(ServicesApp.ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Updates data with given id with given IData.
     * @param data
     * @param id
     * @return
     */
    public boolean updateUnsortedData(IData data, int id){
        return this.updateUnsortedData(data, id, defaultIP, defaultPort);
    }

    public void getSendData(HashSet<Tag> tags) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void getData(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
