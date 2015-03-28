/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.Connection;

import ServicesApp.UI.ServicesController;
import Shared.Connection.ConnClientBase;
import Shared.Connection.ConnCommand;
import Shared.Connection.ConnState;
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
        super.booleanCommand(ConnCommand.UNSORTED_SEND, new Object[]{data});
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

        Object inObject = super.objectCommand(ConnCommand.SORTED_GET, new Object[]{tags});
        List<ISortedData> output = null;
        if (inObject instanceof List) {
            List list = (List) inObject;
            if (list.isEmpty()) {
                output = new ArrayList<>();
            } else {
                if (list.get(0) instanceof ISortedData) {
                    output = (List<ISortedData>) list;
                }
            }
        } else {
            System.err.println("Unexpected output from "
                    + super.getCommandDescription(ConnCommand.SORTED_GET));
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
        List<IDataRequest> output = null;
        Object inObject = super.objectCommand(
                ConnCommand.UPDATE_REQUEST_GET, new Object[]{tags});
        if (inObject instanceof List) {
            List list = (List) inObject;
            if (list.isEmpty()) {
                output = new ArrayList<>();
            } else {
                if (list.get(0) instanceof IDataRequest) {
                    output = (List<IDataRequest>) list;
                }
            }
        } else {
            System.err.println("Unexpected output from "
                    + super.getCommandDescription(ConnCommand.UPDATE_REQUEST_GET));
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
    protected void updateUnsortedData(IData data) {
        super.booleanCommand(ConnCommand.UNSORTED_UPDATE_SEND, new Object[]{data});
    }

    /**
     *
     * @param tags
     * @return unsorted data conforming to parameter (eg. all sent items from
     * this source).
     *
     */
    protected List<IData> getSentData(String source) {
        List<IData> output = null;
        Object inObject = super.objectCommand(
                ConnCommand.UNSORTED_GET_SOURCE, new Object[]{source});
        if (inObject instanceof List) {
            List list = (List) inObject;
            if (list.isEmpty()) {
                output = new ArrayList<>();
            } else {
                if (list.get(0) instanceof IData) {
                    output = (List<IData>) list;
                }
            }
        } else {
            System.err.println("Unexpected output from "
                    + super.getCommandDescription(ConnCommand.UNSORTED_GET_SOURCE));
        }

        return output;
    }

    /**
     * @param id
     * @return specific IData based on given ID.
     */
    protected IData getDataItem(int id) {
        IData output = null;
        Object inObject = super.objectCommand(
                ConnCommand.UNSORTED_GET_ID, new Object[]{id});
        if (inObject instanceof IData) {
            output = (IData) inObject;
        } else {
            System.err.println("Unexpected output from "
                    + super.getCommandDescription(ConnCommand.UNSORTED_GET_ID));
        }

        return output;
    }

    /**
     * Queries server for clientID.
     *
     * @return ClientID. -1 if anything went wrong.
     */
    int getClientID() {
        int output = -1;
        Object inObject = super.objectCommand(
                ConnCommand.CLIENT_ID_GET, new Object[]{});
        if (inObject instanceof Integer) {
            output = (Integer) inObject;
        } else {
            System.err.println("Unexpected output from "
                    + super.getCommandDescription(ConnCommand.CLIENT_ID_GET));
        }

        return output;
    }

    /**
     * Subscribes to buffer on server.
     *
     * @param clientID
     */
    boolean subscribeSorted(int clientID) {
        return super.booleanCommand(
                ConnCommand.SORTED_SUBSCRIBE, new Object[]{clientID});
    }

    /**
     * Subscribes to buffer on server.
     *
     * @param clientID
     */
    boolean subscribeRequests(int clientID) {
        return super.booleanCommand(
                ConnCommand.UPDATE_REQUEST_SUBSCRIBE, new Object[]{clientID});
    }

    /**
     *
     * @param clientID
     * @return newly submitted sorted data since last call.
     */
    List<ISortedData> getNewSorted(int clientID) {
        List<ISortedData> output = null;
        Object inObject = super.objectCommand(
                ConnCommand.SORTED_GET_NEW, new Object[]{clientID});
        if (inObject instanceof List) {
            output = (List<ISortedData>) inObject;
        } else {
            System.err.println("Unexpected output from "
                    + super.getCommandDescription(ConnCommand.SORTED_GET_NEW));
        }

        return output;
    }

    /**
     *
     * @param clientID
     * @return newly submitted requests since last call.
     */
    List<IDataRequest> getNewRequests(int clientID) {
        List<IDataRequest> output = null;
        Object inObject = super.objectCommand(
                ConnCommand.UPDATE_REQUEST_GET_NEW, new Object[]{clientID});
        if (inObject instanceof List) {
            output = (List<IDataRequest>) inObject;
        } else {
            System.err.println("Unexpected output from "
                    + super.getCommandDescription(ConnCommand.UPDATE_REQUEST_GET_NEW));
        }

        return output;
    }

    /**
     * Unsubscribes from server. Does nothing if not subscribed.
     *
     * @param clientID
     */
    boolean unsubscribeSorted(int clientID) {
        return super.booleanCommand(
                ConnCommand.SORTED_UNSUBSCRIBE, new Object[]{clientID});
    }

    /**
     * Unsubscribes from server buffer. Does nothing if not subscribed.
     *
     * @param clientID
     */
    boolean unsubscribeRequests(int clientID) {
        return super.booleanCommand(
                ConnCommand.UPDATE_REQUEST_UNSUBSCRIBE, new Object[]{clientID});
    }

    /**
     * subscribes to updates of unsorted data
     *
     * @param clientID
     * @return
     */
    boolean subscribeUnsorted(int clientID) {
        return super.booleanCommand(
                ConnCommand.UNSORTED_SUBSCRIBE, new Object[]{clientID});
    }

    /**
     * Unsubscribes to updates unsorted data
     *
     * @param clientID
     * @return
     */
    boolean unsubscribeUnsorted(int clientID) {
        return super.booleanCommand(
                ConnCommand.UNSORTED_UNSUBSCRIBE, new Object[]{clientID});
    }

    /**
     * Gets unsorted data held at server since last call
     *
     * @param clientID
     * @return
     */
    List<IData> getNewUnsorted(int clientID) {
        List<IData> output = null;
            Object inObject = super.objectCommand(
                    ConnCommand.UNSORTED_GET_NEW, new Object[]{clientID});
            if (inObject instanceof List) {
                output = (List<IData>) inObject;
            } else {
                System.err.println("Unexpected output from "
                    + super.getCommandDescription(ConnCommand.UNSORTED_GET_NEW));
            }
        return output;
    }

}
