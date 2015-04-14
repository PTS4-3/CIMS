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
import Shared.Data.ISortedData;
import Shared.Tasks.IPlan;
import Shared.Tasks.ITask;
import java.util.ArrayList;
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
     * @param task
     */
    protected void sendTask(ITask task) {
        super.booleanCommand(ConnCommand.TASK_SEND, new Object[]{task});
    }
    
    /**
     * Sends the new given plan to the server
     * @param plan 
     */
    protected void sendNewPlan(IPlan plan) {
        super.booleanCommand(ConnCommand.PLAN_SEND_NEW, new Object[]{plan});
    }
    
    /**
     * Applies a plan and send its steps to the executors
     * @param plan cannot be null
     */
    protected void applyPlan(IPlan plan) {
        super.booleanCommand(ConnCommand.PLAN_APPLY, new Object[]{plan});
    }
}
