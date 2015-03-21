/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.Connection;

import HeadquartersApp.UI.HeadquartersController;
import Shared.Connection.ConnCommand;
import Shared.Connection.ConnClientBase;
import Shared.Connection.ConnState;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
class Connection extends ConnClientBase {

    Connection(String IP, int port) {
        super(IP, port);
    }

    /**
     * Sends sorted data to server.
     *
     * @param IP manually provided.
     * @param port manually provided.
     * @param data
     */
    protected void sendSortedData(ISortedData data) {
        final ISortedData myData = data;

        if (!greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.SORTED_SEND);
            out.writeObject(myData);
            out.flush();
            getCommandSuccess("send sorted data (" + data.getTitle() + ")");
        } catch (IOException ex) {
            System.err.println("Exception trying to send sorted data.");
            Logger.getLogger(ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            closeSocket();
        }

    }

    /**
     * Queries server for a batch of unsorted data. Automatically calls
     * controller after data is received.
     *
     * @param IP manually provided
     * @param port manually provided
     */
    protected List<IData> getData() {
        System.out.println("trying to get data");
        if (!greetServer()) {
            return null;
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
            } else if (inObject instanceof ConnState) {
                if ((ConnState) inObject == ConnState.COMMAND_FAIL) {
                    System.err.println("Server failed to execute command (getData)");
                } else {
                    System.err.println("Unexpected ConnState as output: "
                            + inObject.toString());
                }
            } else {
                System.err.println("Error trying to get unsorted data: unexpected object");
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Exception trying to retrieve unsorted data: " + ex.getMessage());
            Logger.getLogger(ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            closeSocket();
        }
        return output;
    }

    /**
     * Signals server that HQ will not process this checked out data.
     *
     * @param data
     */
    protected void stopWorkingOnData(ArrayList<IData> data) {
        final ArrayList<IData> myData = data;

        if (!greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.UNSORTED_STATUS_RESET);
            out.writeObject(myData);
            out.flush();
            getCommandSuccess("stop working on data");
        } catch (IOException ex) {
            System.err.println("Unable to notify server data is no longer being worked on");
            Logger.getLogger(ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            closeSocket();
        }
    }

    /**
     * Signals server that this headquarters client is no longer working on
     * given data.
     *
     * @param data
     * @param IP
     * @param port
     */
    protected void discardUnsortedData(IData data) {
        final IData myData = data;

        if (!greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.UNSORTED_DISCARD);
            out.writeObject(myData);
            out.flush();
            getCommandSuccess("discard unsorted data (" + data.getTitle() + ")");
        } catch (IOException ex) {
            System.err.println("Error notifying server to discard unsorted data.");
            Logger.getLogger(ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            closeSocket();
        }
    }

    /**
     * Files a request for an update of given piece of data with the server.
     *
     * @param data
     * @param IP
     * @param port
     */
    protected void requestUpdate(IDataRequest data) {
        final IDataRequest myData = data;

        if (!greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.UPDATE_REQUEST_SEND);
            out.writeObject(myData);
            out.flush();
            getCommandSuccess("request update (" + data.getTitle() + ")");
        } catch (IOException ex) {
            System.err.println("Error submitting update request: " + ex.getMessage());
            Logger.getLogger(ConnectionManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            closeSocket();
        }
    }

}
