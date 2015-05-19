/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.ConnectionHandler;

import ServerApp.ServerMain;
import Shared.Connection.ClientBoundTransaction;
import Shared.Connection.ConnState;
import Shared.Connection.ServerBoundTransaction;
import Shared.Connection.SerializeUtils;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.INewsItem;
import Shared.Data.ISortedData;
import Shared.Tag;
import Shared.Tasks.IPlan;
import Shared.Tasks.IStep;
import Shared.Tasks.ITask;
import Shared.Tasks.TaskStatus;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class EchoWorker implements Runnable {

    private static List queue = new LinkedList();
    private static final String
            SORTEDLOCK = "",
            UNSORTEDLOCK = "",
            TASKSLOCK = "";

    /**
     * data arrived over given socket - handled here.
     *
     * @param server
     * @param socket
     * @param data
     * @param count
     */
    public static void processData(NioServer server, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized (queue) {
            queue.add(new ServerDataEvent(server, socket, dataCopy));
            queue.notify();
        }
    }

    @Override
    public void run() {
        ServerDataEvent dataEvent;
        ServerBoundTransaction incoming;
        ClientBoundTransaction outgoing;

        while (true) {
            // Wait for data to become available
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                    }
                }
                dataEvent = (ServerDataEvent) queue.remove(0);
                incoming = (ServerBoundTransaction) SerializeUtils.deserialize(dataEvent.data);
                outgoing = null;
            }

            switch (incoming.command) {
                case SORTED_GET:
                    outgoing = this.sendSortedData(incoming);
                    break;
                case SORTED_SEND:
                    outgoing = this.saveSortedData(incoming);
                    break;
                case UNSORTED_GET:
                    outgoing = this.sendUnsortedData(incoming);
                    break;
                case UNSORTED_SEND:
                    outgoing = this.saveUnsortedData(incoming);
                    break;
                case UNSORTED_STATUS_RESET:
                    outgoing = this.resetUnsortedData(incoming);
                    break;
                case UNSORTED_UPDATE_SEND:
                    outgoing = this.updateUnsortedData(incoming);
                    break;
                case UNSORTED_DISCARD:
                    outgoing = this.discardUnsortedData(incoming);
                    break;
                case UPDATE_REQUEST_SEND:
                    outgoing = this.saveDataRequest(incoming);
                    break;
                case UPDATE_REQUEST_GET:
                    outgoing = this.sendDataRequests(incoming);
                    break;
                case UNSORTED_GET_ID:
                    outgoing = this.sendDataItem(incoming);
                    break;
                case UNSORTED_GET_SOURCE:
                    outgoing = this.sendSentData(incoming);
                    break;
                case TASK_SEND:
                    outgoing = this.sendTask(incoming);
                    break;
                case PLAN_SEND_NEW:
                    outgoing = this.saveNewPlan(incoming);
                    break;
                case PLAN_APPLY:
                    outgoing = this.applyPlan(incoming);
                    break;
                case USERS_SIGN_IN:
                    outgoing = this.getSigninUser(incoming);
                    break;
                case TASK_UPDATE:
                    outgoing = this.updateTask(incoming);
                    break;
                case TASKS_GET:
                    outgoing = this.getTasks(incoming);
                    break;
                case PLAN_SEARCH:
                    outgoing = this.searchPlans(incoming);
                    break;
                case SORTED_GET_ALL:
                    outgoing = this.getSortedData(incoming);
                    break;
                case USERS_GET_SERVICE:
                    outgoing = this.getServiceUsers(incoming);
                    break;
                case NEWSITEM_SEND:
                    outgoing = this.saveNewsItem(incoming);
                    break;
                case NEWSITEM_UPDATE:
                    outgoing = this.updateNewsItem(incoming);
                    break;
                case SITUATIONS_GET:
                    outgoing = this.getSituations(incoming);
                    break;
                default:
                    outgoing = new ClientBoundTransaction(incoming);
                    outgoing.result = ConnState.COMMAND_UNKNOWN;
                    break;
            }

            // Return output to sender
            dataEvent.data = SerializeUtils.serialize(outgoing);
            dataEvent.server.send(dataEvent.socket, dataEvent.data);
        }
    }

    /**
     * Sends a batch of 50 items of unsorted data
     */
    private ClientBoundTransaction sendUnsortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        synchronized (UNSORTEDLOCK) {
            return output.setResult(
                    ServerMain.unsortedDatabaseManager.getFromUnsortedData());
        }
    }

    /**
     * Sends a List of sorted data to given IP.
     *
     * @param tags only data with -all- these tag is provided
     */
    private ClientBoundTransaction sendSortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            HashSet tags = (HashSet) input.objects[0];
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.getFromSortedData(tags));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Saves given ISortedData to database.
     *
     * @param data
     */
    private ClientBoundTransaction saveSortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            ISortedData data = (ISortedData) input.objects[0];
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.insertToSortedData(data));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
//        if (output) {
//            getBuffer().addSorted(data);
//        }
    }

    /**
     * Saves given IData to database.
     *
     */
    private ClientBoundTransaction saveUnsortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IData data = (IData) input.objects[0];
            synchronized (UNSORTEDLOCK) {
                return output.setResult(
                        ServerMain.unsortedDatabaseManager.insertToUnsortedData(data));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
//        if (output) {
//            getBuffer().addSentData(data);
//        }
    }

    /**
     * Notifies Database that a list of data is no longer being worked on.
     */
    private ClientBoundTransaction resetUnsortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            List<IData> list = (List) input.objects[0];
            synchronized (UNSORTEDLOCK) {
                return output.setResult(ServerMain.unsortedDatabaseManager.
                        resetUnsortedData(list));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Updates piece of unsorted data with given id.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction updateUnsortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IData inObject = (IData) input.objects[0];
            synchronized (UNSORTEDLOCK) {
                return output.setResult(
                        ServerMain.unsortedDatabaseManager.updateUnsortedData(inObject));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Tells database to mark given piece of IData as discarded.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction discardUnsortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IData inObject = (IData) input.objects[0];
            synchronized (UNSORTEDLOCK) {
                return output.setResult(
                        ServerMain.unsortedDatabaseManager.discardUnsortedData(inObject));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Files a request for an update to given piece of info.
     */
    private ClientBoundTransaction saveDataRequest(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IDataRequest data = (IDataRequest) input.objects[0];
            synchronized (SORTEDLOCK) {
                boolean result = ServerMain.sortedDatabaseManager.insertDataRequest(data);
                return output.setResult(result);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
//        if (output) {
//            getBuffer().addRequest(data);
//        }
    }

    /**
     * Provides all datarequests conforming to all given tags.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction sendDataRequests(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            HashSet<Tag> tags = (HashSet) input.objects[0];
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.getUpdateRequests(tags));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * returns IData with given ID
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction sendDataItem(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            int inObject = (int) input.objects[0];
            synchronized (UNSORTEDLOCK) {
                return output.setResult(
                        ServerMain.unsortedDatabaseManager.getDataItem((int) inObject));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Returns a list of IData with given source
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction sendSentData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            String source = (String) input.objects[0];
            synchronized (UNSORTEDLOCK) {
                return output.setResult(
                        ServerMain.unsortedDatabaseManager.getSentData(source));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Assigns an ID to new clients.
     *
     * @throws IOException
     */
    @Deprecated
    private ClientBoundTransaction assignID(ServerBoundTransaction input) {
//        writeOutput(ConnectionManager.getNextID());
        return null;
    }

    /**
     * Sends the newly submitted sorted data since last sendNewSortedData call
     * by this client.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Deprecated
    private ClientBoundTransaction sendNewSortedData(ServerBoundTransaction input) {
//        Object inObject = in.readObject();
//        if (inObject instanceof Integer) {
//            writeOutput(getBuffer().collectSorted((int) inObject));
//        } else {
//            writeResult(false);
//        }
        return null;
    }

    /**
     * Subscribes client with given clientID for his own buffer of new
     * sortedData
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction subscribeSorted(ServerBoundTransaction input) {
//        Object par1 = in.readObject();
//        Object par2 = in.readObject();
//
//        if (par1 == null || !(par1 instanceof String)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//        if (par2 == null || !(par2 instanceof Integer)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//
//        String username = (String) par1;
//        int clientID = (Integer) par2;
//
//        getBuffer().subscribeSorted(username, clientID);
//        this.writeResult(true);
        return null;
    }

    /**
     * Sends newly submitted data requests since last method call.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Deprecated
    private ClientBoundTransaction sendNewRequests(ServerBoundTransaction input) {
//        Object inObject = in.readObject();
//        if (inObject instanceof Integer) {
//            writeOutput(getBuffer().collectRequests((int) inObject));
//        } else {
//            writeResult(false);
//        }
        return null;
    }

    /**
     * Subscribes client with given id for his own buffer of datarequests.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction subscribeRequest(ServerBoundTransaction input) {
//        Object par1 = in.readObject();
//        Object par2 = in.readObject();
//
//        if (par1 == null || !(par1 instanceof String)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//        if (par2 == null || !(par2 instanceof Integer)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//
//        String username = (String) par1;
//        int clientID = (Integer) par2;
//
//        getBuffer().subscribeRequests(username, clientID);
//        this.writeResult(true);
        return null;
    }

    /**
     * Unsubscribes client with given ID from his personal buffer.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction unsubscribeRequest(ServerBoundTransaction input) {
//        Object par1 = in.readObject();
//        Object par2 = in.readObject();
//
//        if (par1 == null || !(par1 instanceof String)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//        if (par2 == null || !(par2 instanceof Integer)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//
//        String username = (String) par1;
//        int clientID = (Integer) par2;
//
//        getBuffer().unsubscribeRequests(username, clientID);
//        this.writeResult(true);
        return null;
    }

    /**
     * Unsubscribes client with given ID from his personal buffer.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction unsubscribeSorted(ServerBoundTransaction input) {
//        Object par1 = in.readObject();
//        Object par2 = in.readObject();
//
//        if (par1 == null || !(par1 instanceof String)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//        if (par2 == null || !(par2 instanceof Integer)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//
//        String username = (String) par1;
//        int clientID = (Integer) par2;
//
//        getBuffer().unsubscribeSorted(username, clientID);
//        this.writeResult(true);
        return null;
    }

    /**
     * Sends newly submitted data requests since last method call.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Deprecated
    private ClientBoundTransaction sendNewSent(ServerBoundTransaction input) {
//        Object inObject = in.readObject();
//        if (inObject instanceof Integer) {
//            writeOutput(getBuffer().collectUnsorted((int) inObject));
//        } else {
//            writeResult(false);
//        }
        return null;
    }

    /**
     * Subscribes client with given id for his own buffer of datarequests.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction subscribeSent(ServerBoundTransaction input) {
//        Object par1 = in.readObject();
//        Object par2 = in.readObject();
//
//        if (par1 == null || !(par1 instanceof String)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//        if (par2 == null || !(par2 instanceof Integer)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//
//        String username = (String) par1;
//        int clientID = (Integer) par2;
//
//        getBuffer().subscribeSent(username, clientID);
//        this.writeResult(true);
        return null;
    }

    /**
     * Unsubscribes client with given ID from his personal buffer.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction unsubscribeSent(ServerBoundTransaction input) {
//        Object par1 = in.readObject();
//        Object par2 = in.readObject();
//
//        if (par1 == null || !(par1 instanceof String)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//        if (par2 == null || !(par2 instanceof Integer)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//
//        String username = (String) par1;
//        int clientID = (Integer) par2;
//
//        getBuffer().unsubscribeSent(username, clientID);
//        this.writeResult(true);
        return null;
    }

    /**
     * Sends newly submitted data tasks since last method call.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Deprecated
    private ClientBoundTransaction sendNewTasks(ServerBoundTransaction input) {
//        Object inObject = in.readObject();
//        if (inObject instanceof Integer) {
//            writeOutput(getBuffer().collectTasks((int) inObject));
//        } else {
//            writeResult(false);
//        }
        return null;
    }

    /**
     * Subscribes client with given id for his own buffer of Tasks.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction subscribeTasks(ServerBoundTransaction input) {
//        Object par1 = in.readObject();
//        Object par2 = in.readObject();
//
//        if (par1 == null || !(par1 instanceof String)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//        if (par2 == null || !(par2 instanceof Integer)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//
//        String username = (String) par1;
//        int clientID = (Integer) par2;
//
//        getBuffer().subscribeTasks(username, clientID);
//        this.writeResult(true);
        return null;
    }

    /**
     * Unsubscribes client with given ID from his personal buffer.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction unsubscribeTasks(ServerBoundTransaction input) {
//        Object par1 = in.readObject();
//        Object par2 = in.readObject();
//
//        if (par1 == null || !(par1 instanceof String)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//        if (par2 == null || !(par2 instanceof Integer)) {
//            out.writeObject(ConnState.COMMAND_ERROR);
//            return;
//        }
//
//        String username = (String) par1;
//        int clientID = (Integer) par2;
//
//        getBuffer().unsubscribeTasks(username, clientID);
//        this.writeResult(true);
        return null;
    }

    /**
     * Saves a task and sends it to the executor
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction sendTask(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            ITask task = (ITask) input.objects[0];
            synchronized (TASKSLOCK) {
                task = ServerMain.tasksDatabaseManager.insertNewTask(task);
                return output.setResult(task);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
        // TODO
//        if (task != null) {
//            getBuffer().addTask(task);
//            success = true;
//        }
    }

    /**
     * Saves a new plan
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction saveNewPlan(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IPlan plan = (IPlan) input.objects[0];
            synchronized (TASKSLOCK) {
                return output.setResult(
                        ServerMain.tasksDatabaseManager.insertNewPlan(plan));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Applies a plan, save to database and send its steps to the executors
     */
    private ClientBoundTransaction applyPlan(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IPlan plan = (IPlan) input.objects[0];
            if (plan != null) {
            ServerMain.planExecutorHandler.addPlanExecutor(plan);
            }
            return output.setResult(plan != null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Files a request for an update to given piece of info.
     */
    private ClientBoundTransaction getSigninUser(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            String username = (String) input.objects[0];
            String password = (String) input.objects[1];
            synchronized (TASKSLOCK) {
                return output.setResult(
                        ServerMain.tasksDatabaseManager.loginUser(username, password));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Get tasks from database
     */
    private ClientBoundTransaction getTasks(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            String username = (String) input.objects[0];
            HashSet<TaskStatus> statusses = (HashSet<TaskStatus>) input.objects[1];
            synchronized (TASKSLOCK) {
                return output.setResult(
                        ServerMain.tasksDatabaseManager.getTasks(username, statusses));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Updates the task in the database Adds the updated task to the buffer for
     * the HQChief Handles the task if it is a step
     */
    private ClientBoundTransaction updateTask(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            ITask task = (ITask) input.objects[0];
            boolean success = false;

            synchronized (TASKSLOCK) {
                success = ServerMain.tasksDatabaseManager.updateTask(task);
            }
            // TODO
            //        (task.getStatus() != TaskStatus.READ) {
//            // Add to buffer of HQChief
//            getBuffer().addTaskForChief(task);
//        }

        if ((task.getStatus() == TaskStatus.SUCCEEDED 
                || task.getStatus() == TaskStatus.FAILED)
                && task instanceof IStep) {
            // Execute next step of plan
            ServerMain.planExecutorHandler.executeNextStepOf((IStep) task);
        }
            return output.setResult(success);
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Get plans with keywords from database
     */
    private ClientBoundTransaction searchPlans(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            HashSet<String> keywords = (HashSet) input.objects[0];
            synchronized (TASKSLOCK) {
                return output.setResult(
                        ServerMain.tasksDatabaseManager.getTemplatePlans(keywords));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Get all sorted data from database
     */
    private ClientBoundTransaction getSortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            HashSet<Tag> tags = (HashSet) input.objects[0];
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.getFromSortedData(tags));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Get all serviceusers from database
     */
    private ClientBoundTransaction getServiceUsers(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            synchronized (TASKSLOCK) {
                return output.setResult(
                        ServerMain.tasksDatabaseManager.getServiceUsers());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Send the NewsItem to the database
     */
    private ClientBoundTransaction saveNewsItem(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            INewsItem item = (INewsItem) input.objects[0];
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.insertNewsItem(item));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Updates the NewsItem in the database.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ClientBoundTransaction updateNewsItem(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            INewsItem item = (INewsItem) input.objects[0];
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.updateNewsItem(item));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Get all situations from database
     *
     * @throws IOException
     */
    private ClientBoundTransaction getSituations(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.getSituations());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }
}
