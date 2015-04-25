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
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.Tag;
import Shared.Tasks.ITask;
import Shared.Users.IUser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia + Alexander
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
    protected int getClientID() {
        return super.getClientID();
    }

    /**
     * Subscribes to buffer on server.
     *
     * @param clientID
     */
    protected boolean subscribeSorted(String username, int clientID) {
        return super.booleanCommand(
                ConnCommand.SORTED_SUBSCRIBE, new Object[]{username, clientID});
    }

    /**
     * Subscribes to buffer on server.
     *
     * @param clientID
     */
    protected boolean subscribeRequests(String username, int clientID) {
        return super.booleanCommand(
                ConnCommand.UPDATE_REQUEST_SUBSCRIBE, new Object[]{username, clientID});
    }
    
    /**
     * Subscribes to buffer on server.
     * @param username
     * @param clientID
     * @return 
     */
    boolean subscribeTasks(String username, int clientID) {
        return super.booleanCommand(
                ConnCommand.TASKS_SUBSCRIBE, new Object[]{username, clientID});
    }

    /**
     *
     * @param clientID
     * @return newly submitted sorted data since last call.
     */
    protected List<ISortedData> getNewSorted(int clientID) {
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
    protected List<IDataRequest> getNewRequests(int clientID) {
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
     * 
     * @param clientID
     * @return the tasks from the buffer in the server
     */
    List<ITask> getNewTasks(int clientID) {
        List<ITask> newTasks = new ArrayList<>();
        Object inObject = super.objectCommand(
                ConnCommand.TASKS_GET_NEW, new Object[]{clientID});
        
        if(inObject instanceof List) {
            newTasks = (List<ITask>) inObject;
        } else {
            System.err.println("Unexpected output from "
                    + super.getCommandDescription(ConnCommand.TASKS_GET_NEW));
        }
        
        return newTasks;
    }

    /**
     * Unsubscribes from server. Does nothing if not subscribed.
     *
     * @param clientID
     */
    protected boolean unsubscribeSorted(String username, int clientID) {
        return super.booleanCommand(
                ConnCommand.SORTED_UNSUBSCRIBE, new Object[]{username, clientID});
    }

    /**
     * Unsubscribes from server buffer. Does nothing if not subscribed.
     *
     * @param clientID
     */
    protected boolean unsubscribeRequests(String username, int clientID) {
        return super.booleanCommand(
                ConnCommand.UPDATE_REQUEST_UNSUBSCRIBE, new Object[]{username, clientID});
    }
    
    /**
     * Unsubscribes from server buffer. Does nothing if not subscribed.
     * @param username
     * @param clientID
     * @return 
     */
    boolean unsubscribeTasks(String username, int clientID) {
        return super.booleanCommand(
                ConnCommand.TASKS_UNSUBSCRIBE, new Object[]{username, clientID});
    }

    /**
     * subscribes to updates of sentdata
     *
     * @param clientID
     * @return
     */
    protected boolean subscribeSent(String username, int clientID) {
        return super.booleanCommand(ConnCommand.SENT_SUBSCRIBE, new Object[]{username, clientID});
    }

    /**
     * Unsubscribes to updates sentdata
     *
     * @param clientID
     * @return
     */
    protected boolean unsubscribeSent(String username, int clientID) {
        return super.booleanCommand(ConnCommand.SENT_UNSUBSCRIBE, new Object[]{username, clientID});
    }

    /**
     * Gets sentdata held at server since last call
     *
     * @param clientID
     * @return
     */
    protected List<IData> getNewSent(int clientID) {
        List<IData> output = null;
            Object inObject = super.objectCommand(ConnCommand.SENT_GET_NEW, new Object[]{clientID});
            if (inObject instanceof List) {
                output = (List<IData>) inObject;
            } else {
                System.err.println("Unexpected output from "
                    + super.getCommandDescription(ConnCommand.SENT_GET_NEW));
            }
        return output;
    }
    
        
    /**
     * Gets IUser from server
     * 
     * @param username
     * @param password
     * @return IUser
     */
    protected IUser getSigninUser(String username, String password) {
        return super.getSigninUser(username, password);
    }
    
    /**
     * Updates the status of the given task
     * @param task
     * @return 
     */
    protected boolean updateTask(ITask task) {
        return super.booleanCommand(ConnCommand.TASK_UPDATE, new Object[]{task});
    }
    
    /**
     * Gets the tasks from the serviceUser with the given username
     * @param username
     * @return
     */
    protected List<ITask> getTasks(String username) {
        List<ITask> output = null;
            Object inObject = super.objectCommand(
                    ConnCommand.TASKS_GET, new Object[]{username});
            if (inObject instanceof List) {
                output = (List<ITask>) inObject;
            } else {
                System.err.println("Unexpected output from "
                    + super.getCommandDescription(ConnCommand.TASKS_GET));
            }
        return output;
    }
}
