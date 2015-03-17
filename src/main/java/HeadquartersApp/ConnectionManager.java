/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp;

import HeadquartersApp.UI.HeadquartersController;
import Shared.ConnState;
import Shared.ConnCommand;
import Shared.IData;
import Shared.IDataRequest;
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
     Option 2 - Client: ConnCommand.SORTED_GET
     -> Client: Set<Tag>
     -> Server: List<ISortedData>
     Option 3 - Client: ConnCommand.SORTED_SEND
     -> Client: ISortedData
     Option 4 - Client: ConnCommand.UNSORTED_GET
     -> Server: List<IData>
     Option 5 - Client: ConnCommand.UNSORTED_SEND
     -> Client: IData
     Option 6 - Client: ConnCommand.UNSORTED_RESET
     -> Client: List<IData>
     -----
     Return to start, except on closed conn
     */
    private static ExecutorService pool = Executors.newCachedThreadPool();
    private HeadquartersController guiController = null;
    public int defaultPort = 8189;
    private String defaultIP;

    private InputStream inStream = null;
    private OutputStream outStream = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    Socket socket = null;

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
     */
    public void sendSortedData(String IP, int port, ISortedData data) {
        final String myIP = IP;
        final int myPort = port;
        final ISortedData myData = data;

        pool.execute(new Runnable() {

            @Override
            public void run() {
                if (!greetServer(myIP, myPort)) {
                    return;
                }
                try {
                    out.writeObject(ConnCommand.SORTED_SEND);
                    out.writeObject(myData);
                    out.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ConnectionManager.class.getName())
                            .log(Level.SEVERE, null, ex);
                } finally {
                    closeSocket();
                }
            }
        });

    }

    /**
     * Sends sorted data to server @ default IP / port
     *
     * @param data
     */
    public void sendSortedData(ISortedData data) {
        this.sendSortedData(defaultIP, defaultPort, data);
    }

    /**
     * Queries server for a batch of unsorted data. Automatically calls
     * controller after data is received.
     *
     * @param IP manually provided
     * @param port manually provided
     */
    public void getData(String IP, int port) {
        final String myIP = IP;
        final int myPort = port;

        pool.execute(new Runnable() {

            @Override
            public void run() {
                if (!greetServer(myIP, myPort)) {
                    return;
                }
                List<IData> output = null;
                try {
                    out.writeObject(ConnCommand.UNSORTED_GET);
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
                        guiController.displayData(output);
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(ConnectionManager.class.getName())
                            .log(Level.SEVERE, null, ex);
                } finally {
                    closeSocket();
                }
            }
        });
    }

    /**
     * Queries server for batch of unsorted data from default IP / port.
     * Automatically calls HeadquartersController.displayData(data) on
     * completion
     */
    public void getData() {
        getData(defaultIP, defaultPort);
    }

    /**
     * Signals server that HQ will not process this checked out data.
     *
     * @param data
     * @param IP
     * @param port
     */
    public void stopWorkingOnData(List<IData> data, String IP, int port) {
        final List<IData> myData = data;
        final String myIP = IP;
        final int myPort = port;

        pool.execute(new Runnable() {

            @Override
            public void run() {
                if (!greetServer(myIP, myPort)) {
                    return;
                }
                try {
                    out.writeObject(ConnCommand.UNSORTED_STATUS_RESET);
                    out.writeObject(myData);
                    out.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ConnectionManager.class.getName())
                            .log(Level.SEVERE, null, ex);
                } finally {
                    closeSocket();
                }
            }
        });
    }

    /**
     * Signals server that HQ will not process this list of data.
     *
     * @param data
     */
    public void stopWorkingOnData(List<IData> data) {
        this.stopWorkingOnData(data, defaultIP, defaultPort);
    }

    /**
     * Signals server that this headquarters client is no longer working on
     * given data.
     *
     * @param data
     * @param IP
     * @param port
     */
    public void discardUnsortedData(IData data, String IP, int port) {
        final IData myData = data;
        final String myIP = IP;
        final int myPort = port;

        pool.execute(new Runnable() {

            @Override
            public void run() {
                if (!greetServer(myIP, myPort)) {
                    return;
                }
                try {
                    out.writeObject(ConnCommand.UNSORTED_DISCARD);
                    out.writeObject(myData);
                    out.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ConnectionManager.class.getName())
                            .log(Level.SEVERE, null, ex);
                } finally {
                    closeSocket();
                }
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
        this.discardUnsortedData(data, defaultIP, defaultPort);
    }

    /**
     * Files a request for an update of given piece of data with the server.
     * @param data
     * @param IP
     * @param port
     */
    public void requestUpdate(IDataRequest data, String IP, int port) {
        final IDataRequest myData = data;
        final String myIP = IP;
        final int myPort = port;

        pool.execute(new Runnable() {

            @Override
            public void run() {
                if (!greetServer(myIP, myPort)) {
                    return;
                }
                try {
                    out.writeObject(ConnCommand.UNSORTED_UPDATE_REQUEST);
                    out.writeObject(myData);
                    out.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ConnectionManager.class.getName())
                            .log(Level.SEVERE, null, ex);
                } finally {
                    closeSocket();
                }
            }
        });
    }

    /**
     * Files a request for an update of given piece of data with the server.
     * @param data
     */
    public void requestUpdate(IDataRequest data) {
        this.requestUpdate(data, defaultIP, defaultPort);
    }
}
