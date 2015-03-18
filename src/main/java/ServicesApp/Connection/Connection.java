/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.Connection;

import ServicesApp.UI.ServicesController;
import Shared.Connection.ConnClientBase;
import Shared.Connection.ConnCommand;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import Shared.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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
     * @return success on attempting to send piece of unsorted data.
     */
    protected void sendUnSortedData(IData data) {
        if (!greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.UNSORTED_SEND);
            out.writeObject(data);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
    }

    /**
     * Queries server for sorted data that has all given tags.
     *
     * @param IP manually provided
     * @param port manually provided
     * @param tags
     * @return batch of data. Null on general error.
     */
    protected List<ISortedData> getSortedData(HashSet<Tag> tags) {
        if (!this.greetServer()) {
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
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Gets all data requests conforming to all given tags. Custom IP/port
     *
     * @param tags
     * @param IP
     * @param port
     * @return
     */
    protected List<IDataRequest> getDataRequests(HashSet<Tag> tags) {
        if (!this.greetServer()) {
            return null;
        }
        List<IDataRequest> output = null;
        try {
            out.writeObject(ConnCommand.UPDATE_REQUEST_GET);
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
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Updates data with given id with given IData.
     *
     * @param data
     * @param id
     * @param IP
     * @param port
     * @return
     */
    protected void updateUnsortedData(IData data, int id) {
        if (!this.greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.UNSORTED_UPDATE_SEND);
            out.writeObject(id);
            out.writeObject(data);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
    }

    /**
     *
     * @param tags
     * @return unsorted data conforming to parameter (eg. all sent items from
     * this source)
     */
    protected List<IData> getSentData(HashSet<Tag> tags) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @param id
     * @return specific IData based on given ID.
     */
    protected IData getDataItem(int id) {
        if (!this.greetServer()) {
            return null;
        }
        IData output = null;
        try {
            out.writeObject(ConnCommand.UNSORTED_GET_ID);
            out.writeObject(id);
            out.flush();

            Object inObject = in.readObject();
            if (inObject instanceof IData) {
                output = (IData) inObject;
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

}
