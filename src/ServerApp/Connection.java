/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.ConnState;
import Shared.DataRequest;
import Shared.IData;
import Shared.ISortedData;
import Shared.Tag;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
public class Connection implements Runnable {

    
    private static String eol = System.getProperty("line.separator");
    private static String fs = File.separator;

    private Socket conn = null;
    private InputStream inStream = null;
    private OutputStream outStream = null;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Connection(Socket conn) {
        try {
            this.conn = conn;
            this.inStream = conn.getInputStream();
            this.outStream = conn.getOutputStream();

            this.in = new ObjectInputStream(inStream);
            this.out = new ObjectOutputStream(outStream);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        // lets console know if something went wrong
        if (conn == null || inStream == null || outStream == null || in == null
                || out == null) {
            System.out.println("an object in Runnable was null: " + eol
                    + "conn: " + (conn == null) + eol
                    + "inStream: " + (inStream == null) + eol
                    + "outStream: " + (outStream == null) + eol
                    + "in: " + (in == null) + eol
                    + "out: " + (out == null) + eol);
        }

        try {
            try {
                boolean isDone = false;
                while (!isDone) {
                    // writes this before every cycle
                    out.writeObject(ConnState.CONNECTED);

                    Object inObject = in.readObject();
                    if (inObject instanceof ConnState) {
                        ConnState state = (ConnState) inObject;
                        if (state == ConnState.DONE) {
                            isDone = true;
                        } else if (state == ConnState.CONNECTED) {
                            System.out.println("Connection is working as intended");
                        }
                    }
                    if (inObject instanceof DataRequest) {
                        DataRequest command = (DataRequest) inObject;
                        System.out.println("-- Command: " + command.toString());

                        switch (command) {
                            case SORTED_GET:
                                this.sendSortedData();
                                break;
                            case SORTED_SEND:
                                this.saveSortedData();
                                break;
                            case UNSORTED_GET:
                                this.sendUnsortedData();
                                break;
                            case UNSORTED_SEND:
                                this.saveUnsortedData();
                                break;
                            case UNSORTED_STATUS_RESET:
                                this.resetUnsortedData();
                                break;
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println("IOException in while loop Runnable: "
                        + ex.getMessage());
                Logger.getLogger(Connection.class.getName())
                        .log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                System.out.println("ClassNotFoundException in while loop Runnable: "
                        + ex.getMessage());
                Logger.getLogger(Connection.class.getName())
                        .log(Level.SEVERE, null, ex);
            } finally {
                conn.close();
            }
        } catch (IOException ex) {
            // catches IOException thrown in finally
            System.out.println("IOException closing socket after use: "
                    + ex.getMessage());
        }
    }

    /**
     * Sends a batch of 50 items of unsorted data
     */
    private void sendUnsortedData() throws IOException {
        out.writeObject(ServerMain.databaseManager.getFromUnsortedData());
    }

    /**
     * Sends a List of sorted data to given IP.
     *
     * @param tags only data with -all- these tag is provided
     */
    private void sendSortedData() throws IOException,
            ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject instanceof HashSet) {
            HashSet tags = (HashSet) inObject;
            out.writeObject(ServerMain.databaseManager.getFromSortedData(tags));
        } else {
//            out.writeObject(ConnState.ERROR);
        }
    }

    /**
     * Saves given ISortedData to database. Writes a connstate.error if data is
     * null.
     *
     * @param data
     */
    private void saveSortedData() throws IOException,
            ClassNotFoundException {
        Object inObject = in.readObject();
        if (!(inObject instanceof ISortedData) || inObject == null) {
//            out.writeObject(ConnState.ERROR);
            return;
        }
        ISortedData data = (ISortedData) inObject;
        ServerMain.databaseManager.insertToSortedData(data);
    }

    /**
     * Saves given IData to database. Writes a connstate.error if data is null.
     *
     */
    private void saveUnsortedData() throws IOException,
            ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject == null || !(inObject instanceof IData)) {
//            out.writeObject(ConnState.ERROR);
            return;
        }
        IData data = (IData) inObject;
        ServerMain.databaseManager.insertToUnsortedData(data);
    }

    /**
     * Notifies Database that a list of data is no longer being worked on.
     */
    private void resetUnsortedData() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if(inObject instanceof List){
            List list = (List) inObject;
            if(!list.isEmpty() && (list.get(0) instanceof IData)){
                ServerMain.databaseManager.resetUnsortedData((List<IData>) list);
            }
        } else {
//            out.writeObject(ConnState.ERROR);
        }
    }

}
