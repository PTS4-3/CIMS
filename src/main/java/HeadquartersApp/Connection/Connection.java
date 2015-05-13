/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.Connection;

import Shared.Connection.ConnCommand;
import Shared.Connection.ConnClientBase;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.INewsItem;
import Shared.Data.ISortedData;
import Shared.Data.Situation;
import Shared.NetworkException;
import Shared.Tasks.IPlan;
import Shared.Tasks.ITask;
import Shared.Tasks.TaskStatus;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
     */
    protected void sendSortedData(ISortedData data) {
        super.booleanCommand(ConnCommand.SORTED_SEND, new Object[]{data});
    }

    /**
     * Queries server for a batch of unsorted data. Automatically calls
     * controller after data is received.
     *
     * @param IP manually provided
     * @param port manually provided
     */
    protected List<IData> getData() {
        List<IData> output = null;
        Object inObject = super.objectCommand(ConnCommand.UNSORTED_GET, new Object[]{});
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
                    + super.getCommandDescription(ConnCommand.UNSORTED_GET));
        }
        return output;
    }

    /**
     * Signals server that HQ will not process this checked out data.
     *
     * @param data
     */
    protected void stopWorkingOnData(ArrayList<IData> data) {
        super.booleanCommand(ConnCommand.UNSORTED_STATUS_RESET, new Object[]{data});
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
        super.booleanCommand(ConnCommand.UNSORTED_DISCARD, new Object[]{data});
    }

    /**
     * Files a request for an update of given piece of data with the server.
     *
     * @param data
     * @param IP
     * @param port
     */
    protected void requestUpdate(IDataRequest data) {
        super.booleanCommand(ConnCommand.UPDATE_REQUEST_SEND, new Object[]{data});
    }

    /**
     * Sends the given task to the server
     *
     * @param task
     */
    protected void sendTask(ITask task) {
        super.booleanCommand(ConnCommand.TASK_SEND, new Object[]{task});
    }

    /**
     * Sends the new given plan to the server
     *
     * @param plan
     */
    protected void sendNewPlan(IPlan plan) {
        super.booleanCommand(ConnCommand.PLAN_SEND_NEW, new Object[]{plan});
    }

    /**
     * Applies a plan and send its steps to the executors
     *
     * @param plan cannot be null
     */
    protected void applyPlan(IPlan plan) {
        super.booleanCommand(ConnCommand.PLAN_APPLY, new Object[]{plan});
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
     * Gets clientId from the server
     *
     * @return the retrieved clientId
     */
    protected int getClientId() {
        return super.getClientID();
    }

    /**
     * Subscribes to get updates for sortedData for HQChief
     *
     * @param username
     * @param clientId
     * @return
     */
    protected boolean subscribeSortedData(String username, int clientId) {
        return super.booleanCommand(
                ConnCommand.SORTED_SUBSCRIBE, new Object[]{username, clientId});
    }

    /**
     * Unsubscribes to get updates for sortedData for HQChief
     *
     * @param username
     * @param clientId
     * @return
     */
    protected boolean unsubscribeSortedData(String username, int clientId) {
        return super.booleanCommand(
                ConnCommand.SORTED_UNSUBSCRIBE, new Object[]{username, clientId});
    }

    /**
     * Get updates for sorted data
     *
     * @param clientId
     * @return
     */
    protected List<ISortedData> getNewSortedData(int clientId) throws NetworkException {
        Object output = super.objectCommand(
                ConnCommand.SORTED_GET_NEW, new Object[]{clientId});

        if (!(output instanceof List)) {
            throw new NetworkException("Unexpected result in "
                    + super.getCommandDescription(ConnCommand.SORTED_GET_NEW));
        }

        return (List<ISortedData>) output;
    }

    /**
     * Subscribes to get updates for the status of tasks for HQChief
     *
     * @param username
     * @param clientId
     * @return
     */
    protected boolean subscribeTasks(String username, int clientId) {
        return super.booleanCommand(ConnCommand.TASKS_SUBSCRIBE,
                new Object[]{username, clientId});
    }

    /**
     * Unsubscribes to get updates for the status of tasks for HQChief
     *
     * @param username
     * @param clientId
     * @return
     */
    protected boolean unsubscribeTasks(String username, int clientId) {
        return super.booleanCommand(
                ConnCommand.TASKS_UNSUBSCRIBE, new Object[]{username, clientId});
    }

    /**
     * Get updates for the status of tasks
     *
     * @param clientId
     * @return
     */
    protected List<ITask> getNewTasks(int clientId) throws NetworkException {
        Object output = super.objectCommand(
                ConnCommand.TASKS_GET_NEW, new Object[]{clientId});

        if (!(output instanceof List)) {
            throw new NetworkException("Unexpected result in "
                    + super.getCommandDescription(ConnCommand.TASKS_GET_NEW));
        }

        return (List<ITask>) output;
    }

    /**
     * Updates the status of the given task Used to update the status to
     * TaskStatus.READ
     *
     * @param task
     * @return
     */
    protected boolean updateTask(ITask task) {
        return super.booleanCommand(ConnCommand.TASK_UPDATE, new Object[]{task});
    }

    /**
     * Gets plans with given keywords
     *
     * @param keywords
     * @return
     */
    protected List<IPlan> searchPlans(HashSet<String> keywords) throws NetworkException {
        Object output = super.objectCommand(
                ConnCommand.PLAN_SEARCH, new Object[]{keywords});

        if (!(output instanceof List)) {
            throw new NetworkException("Unexpected result in "
                    + super.getCommandDescription(ConnCommand.PLAN_SEARCH));
        }

        return (List<IPlan>) output;
    }

    /**
     * Get all sorted data
     *
     * @return
     */
    protected List<ISortedData> getSortedData() throws NetworkException {
        Object output = super.objectCommand(
                ConnCommand.SORTED_GET_ALL, new Object[]{});

        if (!(output instanceof List)) {
            throw new NetworkException("Unexpected result in "
                    + super.getCommandDescription(ConnCommand.SORTED_GET_ALL));
        }

        return (List<ISortedData>) output;
    }

    /**
     * Get all serviceusers
     *
     * @return
     */
    protected List<IServiceUser> getServiceUsers() throws NetworkException {
        Object output = super.objectCommand(
                ConnCommand.SERVICEUSERS_GET, new Object[]{});

        if (!(output instanceof List)) {
            throw new NetworkException("Unexpected result in "
                    + super.getCommandDescription(ConnCommand.SERVICEUSERS_GET));
        }

        return (List<IServiceUser>) output;
    }

    /**
     * Get all tasks
     *
     * @return
     */
    protected List<ITask> getTasks() throws NetworkException {
        HashSet<TaskStatus> statuses = new HashSet<TaskStatus>();
        for (TaskStatus ts : TaskStatus.values()) {
            if (ts != TaskStatus.READ && ts != TaskStatus.SENT && ts != TaskStatus.UNASSIGNED) {
                statuses.add(ts);
            }
        }

        Object output = super.objectCommand(
                ConnCommand.TASKS_GET, new Object[]{"", statuses});

        if (!(output instanceof List)) {
            throw new NetworkException("Unexpected result in "
                    + super.getCommandDescription(ConnCommand.TASKS_GET));
        }

        return (List<ITask>) output;
    }

    /**
     * Sends the NewsItem to the server
     *
     * @param item
     */
    protected void sendNewsItem(INewsItem item) {
        super.booleanCommand(ConnCommand.NEWSITEM_SEND, new Object[]{item});
    }
    
    protected List<Situation> getSituations() throws NetworkException{
        Object output = super.objectCommand(
                ConnCommand.SERVICEUSERS_GET, new Object[]{});

        if (!(output instanceof List)) {
            throw new NetworkException("Unexpected result in "
                    + super.getCommandDescription(ConnCommand.SITUATIONS_GET));
        }

        return (List<Situation>) output;
    }
}
