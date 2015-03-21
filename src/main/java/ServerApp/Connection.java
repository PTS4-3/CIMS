/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.Connection.ConnState;
import Shared.Connection.ConnCommand;
import Shared.IData;
import Shared.IDataRequest;
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

    /**
     * Output of data after checking if it's not null.
     * @param output
     * @throws IOException
     */
    protected void writeOutput(Object output) throws IOException{
        if(output != null){
            out.writeObject(output);
        } else {
            out.writeObject(ConnState.COMMAND_FAIL);
        }
    }

    /**
     * Output of a boolean result.
     * @param result
     * @throws java.io.IOException
     */
    protected void writeOutput(boolean result) throws IOException{
        if(result){
            out.writeObject(ConnState.COMMAND_SUCCESS);
        } else {
            out.writeObject(ConnState.COMMAND_FAIL);
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
                out.writeObject(ConnState.CONNECTION_START);
                out.flush();

                boolean isDone = false;
                while (!isDone) {

                    Object inObject = in.readObject();
                    if (inObject instanceof ConnState) {
                        ConnState state = (ConnState) inObject;
                        if (state == ConnState.CONNECTION_END) {
                            isDone = true;
                        } else if (state == ConnState.CONNECTION_START) {
                            System.out.println("Connection is working as intended");
                        }
                    }
                    if (inObject instanceof ConnCommand) {
                        ConnCommand command = (ConnCommand) inObject;
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
                            case UNSORTED_UPDATE_SEND:
                                this.updateUnsortedData();
                                break;
                            case UNSORTED_DISCARD:
                                this.discardUnsortedData();
                                break;
                            case UPDATE_REQUEST_SEND:
                                this.requestDataUpdate();
                                break;
                            case UPDATE_REQUEST_GET:
                                this.sendDataRequests();
                                break;
                            case UNSORTED_GET_ID:
                                this.sendDataItem();
                                break;
                            case UNSORTED_GET_SOURCE:
                                this.sendSentData();
                                break;
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println("IOException connecting / listening commands: "
                        + ex.getMessage());
                Logger.getLogger(Connection.class.getName())
                        .log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                System.out.println("ClassNotFoundException connecting / listening commands: "
                        + ex.getMessage());
                Logger.getLogger(Connection.class.getName())
                        .log(Level.SEVERE, null, ex);
            } finally {
                System.out.println("Request finished - closing down");
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
        writeOutput(ServerMain.dummyManager.getFromUnsortedData());
        out.flush();
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
            writeOutput(ServerMain.dummyManager.getFromSortedData(tags));
        } else {
            out.writeObject(ConnState.COMMAND_ERROR);
        }
        out.flush();
    }

    /**
     * Saves given ISortedData to database.
     *
     * @param data
     */
    private void saveSortedData() throws IOException,
            ClassNotFoundException {
        Object inObject = in.readObject();
        if (!(inObject instanceof ISortedData) || inObject == null) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        ISortedData data = (ISortedData) inObject;
        writeOutput(ServerMain.dummyManager.insertToSortedData(data));
        out.flush();
    }

    /**
     * Saves given IData to database.
     *
     */
    private void saveUnsortedData() throws IOException,
            ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject == null || !(inObject instanceof IData)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        IData data = (IData) inObject;
        writeOutput(ServerMain.dummyManager.insertToUnsortedData(data));
        out.flush();
    }

    /**
     * Notifies Database that a list of data is no longer being worked on.
     */
    private void resetUnsortedData() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if(inObject == null){
            System.out.println("resetUnsortedData inObject was null");
            out.writeObject(ConnState.COMMAND_ERROR);
            out.flush();
            return;
        }

        if (inObject instanceof List) {
            List list = (List) inObject;
            if (!list.isEmpty() && (list.get(0) instanceof IData)) {
                writeOutput(ServerMain.dummyManager.resetUnsortedData((List<IData>) list));
            }
        } else {
            out.writeObject(ConnState.COMMAND_ERROR);
        }
        out.flush();
    }

    /**
     * Updates piece of unsorted data with given id.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void updateUnsortedData() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject == null || !(inObject instanceof IData)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        writeOutput(ServerMain.dummyManager.updateUnsortedData((IData) inObject));
        out.flush();
    }

    /**
     * Tells database to mark given piece of IData as discarded.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void discardUnsortedData() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject == null || !(inObject instanceof IData)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        writeOutput(ServerMain.dummyManager.discardUnsortedData((IData) inObject));
        out.flush();
    }

    /**
     * Files a request for an update to given piece of info.
     */
    private void requestDataUpdate() throws IOException,
            ClassNotFoundException {
        Object inObject = in.readObject();
        if (!(inObject instanceof IDataRequest) || inObject == null) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        IDataRequest data = (IDataRequest) inObject;
        writeOutput(ServerMain.dummyManager.insertDataRequest(data));
        out.flush();
    }

    /**
     * Provides all datarequests conforming to all given tags.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void sendDataRequests() throws IOException,
            ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject instanceof HashSet) {
            HashSet tags = (HashSet) inObject;
            writeOutput(ServerMain.dummyManager.getUpdateRequests(tags));
        } else {
            out.writeObject(ConnState.COMMAND_ERROR);
        }
        out.flush();
    }

    /**
     * returns IData with given ID
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void sendDataItem() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject instanceof Integer) {
            writeOutput(ServerMain.dummyManager.getDataItem((int) inObject));
        } else {
            out.writeObject(ConnState.COMMAND_ERROR);
        }
        out.flush();
    }

    /**
     * Returns a list of IData with given source
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void sendSentData() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject instanceof String) {
            writeOutput(ServerMain.dummyManager.getSentData(inObject.toString()));
        } else {
            out.writeObject(ConnState.COMMAND_ERROR);
        }
        out.flush();
    }

}
