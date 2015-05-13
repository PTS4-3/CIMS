/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import static ServerApp.ConnectionManager.SORTEDLOCK;
import static ServerApp.ConnectionManager.TASKSLOCK;
import static ServerApp.ConnectionManager.UNSORTEDLOCK;
import static ServerApp.ConnectionManager.getBuffer;
import static ServerApp.ConnectionManager.getPlanExecutorHandler;
import Shared.Connection.ConnState;
import Shared.Connection.ConnCommand;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.INewsItem;
import Shared.Data.ISortedData;
import Shared.Data.Situation;
import Shared.Tag;
import Shared.Tasks.IPlan;
import Shared.Tasks.IStep;
import Shared.Tasks.ITask;
import Shared.Tasks.TaskStatus;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia + Alexander
 */
public class Connection implements Runnable {

    private static final String eol = System.getProperty("line.separator");

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
     *
     * @param output
     * @throws IOException
     */
    private void writeOutput(Object output) throws IOException {
        if (output != null) {
            out.writeObject(output);
        } else {
            out.writeObject(ConnState.COMMAND_FAIL);
        }
        out.flush();
    }

    /**
     * Output of a boolean result.
     *
     * @param result
     * @throws java.io.IOException
     */
    private void writeResult(boolean result) throws IOException {
        if (result) {
            out.writeObject(ConnState.COMMAND_SUCCESS);
        } else {
            out.writeObject(ConnState.COMMAND_FAIL);
        }
        out.flush();
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
//                            System.out.println("Connection is working as intended");
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
                                this.saveDataRequest();
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
                            case CLIENT_ID_GET:
                                this.assignID();
                                break;
                            case SORTED_GET_NEW:
                                this.sendNewSortedData();
                                break;
                            case SORTED_SUBSCRIBE:
                                this.subscribeSorted();
                                break;
                            case SORTED_UNSUBSCRIBE:
                                this.unsubscribeSorted();
                                break;
                            case UPDATE_REQUEST_GET_NEW:
                                this.sendNewRequests();
                                break;
                            case UPDATE_REQUEST_SUBSCRIBE:
                                this.subscribeRequest();
                                break;
                            case UPDATE_REQUEST_UNSUBSCRIBE:
                                this.unsubscribeRequest();
                                break;
                            case SENT_GET_NEW:
                                this.sendNewSent();
                                break;
                            case SENT_SUBSCRIBE:
                                this.subscribeSent();
                                break;
                            case SENT_UNSUBSCRIBE:
                                this.unsubscribeSent();
                                break;
                            case TASK_SEND:
                                this.sendTask();
                                break;
                            case PLAN_SEND_NEW:
                                this.saveNewPlan();
                                break;
                            case PLAN_APPLY:
                                this.applyPlan();
                                break;
                            case TASKS_GET_NEW:
                                this.sendNewTasks();
                                break;
                            case TASKS_SUBSCRIBE:
                                this.subscribeTasks();
                                break;
                            case TASKS_UNSUBSCRIBE:
                                this.unsubscribeTasks();
                                break;
                            case SIGN_IN:
                                this.getSigninUser();
                                break;
                            case TASK_UPDATE:
                                this.updateTask();
                                break;
                            case TASKS_GET:
                                this.getTasks();
                                break;
                            case PLAN_SEARCH:
                                this.searchPlans();
                                break;
                            case SORTED_GET_ALL:
                                this.getSortedData();
                                break;
                            case SERVICEUSERS_GET:
                                this.getServiceUsers();
                                break;
                            case NEWSITEM_SEND:
                                this.saveNewsItem();
                                break;
                            case SITUATIONS_GET:
                                this.getSituations();
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
//                System.out.println("Request finished - closing down");
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
        synchronized (UNSORTEDLOCK) {
            writeOutput(ServerMain.unsortedDatabaseManager.getFromUnsortedData());
        }
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
            synchronized (SORTEDLOCK) {
                writeOutput(ServerMain.sortedDatabaseManager.getFromSortedData(tags));
            }
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
        boolean output = false;
        synchronized (SORTEDLOCK) {
            output = ServerMain.sortedDatabaseManager.insertToSortedData(data);
        }
        if (output) {
            getBuffer().addSorted(data);
        }
        writeResult(output);
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
        boolean output = false;
        synchronized (UNSORTEDLOCK) {
            output = ServerMain.unsortedDatabaseManager.insertToUnsortedData(data);
        }
        if (output) {
            getBuffer().addSentData(data);
        }
        writeResult(output);
    }

    /**
     * Notifies Database that a list of data is no longer being worked on.
     */
    private void resetUnsortedData() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject == null) {
            System.out.println("resetUnsortedData inObject was null");
            out.writeObject(ConnState.COMMAND_ERROR);
            out.flush();
            return;
        }

        if (inObject instanceof List) {
            List list = (List) inObject;
            if (list.isEmpty()) {
                // database should do nothing, but client is expecting result
                this.writeResult(true);
            } else if (list.get(0) instanceof IData) {
                synchronized (UNSORTEDLOCK) {
                    writeResult(ServerMain.unsortedDatabaseManager.
                            resetUnsortedData((List<IData>) list));
                }
            } else {
                out.writeObject(ConnState.COMMAND_ERROR);
                out.flush();
            }
        } else {
            out.writeObject(ConnState.COMMAND_ERROR);
            out.flush();
        }
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
        synchronized (UNSORTEDLOCK) {
            writeResult(ServerMain.unsortedDatabaseManager.updateUnsortedData((IData) inObject));
        }
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
        synchronized (UNSORTEDLOCK) {
            writeResult(ServerMain.unsortedDatabaseManager.discardUnsortedData((IData) inObject));
        }
    }

    /**
     * Files a request for an update to given piece of info.
     */
    private void saveDataRequest() throws IOException,
            ClassNotFoundException {
        Object inObject = in.readObject();
        if (!(inObject instanceof IDataRequest) || inObject == null) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        IDataRequest data = (IDataRequest) inObject;
        boolean output = false;
        synchronized (SORTEDLOCK) {
            output = ServerMain.sortedDatabaseManager.insertDataRequest(data);
        }
        if (output) {
            getBuffer().addRequest(data);
        }
        writeResult(output);
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
            synchronized (SORTEDLOCK) {
                writeOutput(ServerMain.sortedDatabaseManager.getUpdateRequests(tags));
            }
        } else {
            out.writeObject(ConnState.COMMAND_ERROR);
            out.flush();
        }
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
            synchronized (UNSORTEDLOCK) {
                writeOutput(ServerMain.unsortedDatabaseManager.getDataItem((int) inObject));
            }
        } else {
            out.writeObject(ConnState.COMMAND_ERROR);
            out.flush();
        }
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
            synchronized (UNSORTEDLOCK) {
                writeOutput(ServerMain.unsortedDatabaseManager.getSentData(inObject.toString()));
            }
        } else {
            out.writeObject(ConnState.COMMAND_ERROR);
            out.flush();
        }
    }

    /**
     * Assigns an ID to new clients.
     *
     * @throws IOException
     */
    private void assignID() throws IOException {
        writeOutput(ConnectionManager.getNextID());
    }

    /**
     * Sends the newly submitted sorted data since last sendNewSortedData call
     * by this client.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void sendNewSortedData() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject instanceof Integer) {
            writeOutput(getBuffer().collectSorted((int) inObject));
        } else {
            writeResult(false);
        }
    }

    /**
     * Subscribes client with given clientID for his own buffer of new
     * sortedData
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void subscribeSorted() throws IOException, ClassNotFoundException {
        Object par1 = in.readObject();
        Object par2 = in.readObject();

        if (par1 == null || !(par1 instanceof String)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        if (par2 == null || !(par2 instanceof Integer)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        String username = (String) par1;
        int clientID = (Integer) par2;

        getBuffer().subscribeSorted(username, clientID);
        this.writeResult(true);
    }

    /**
     * Sends newly submitted data requests since last method call.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void sendNewRequests() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject instanceof Integer) {
            writeOutput(getBuffer().collectRequests((int) inObject));
        } else {
            writeResult(false);
        }
    }

    /**
     * Subscribes client with given id for his own buffer of datarequests.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void subscribeRequest() throws IOException, ClassNotFoundException {
        Object par1 = in.readObject();
        Object par2 = in.readObject();

        if (par1 == null || !(par1 instanceof String)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        if (par2 == null || !(par2 instanceof Integer)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        String username = (String) par1;
        int clientID = (Integer) par2;

        getBuffer().subscribeRequests(username, clientID);
        this.writeResult(true);
    }

    /**
     * Unsubscribes client with given ID from his personal buffer.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void unsubscribeRequest() throws IOException, ClassNotFoundException {
        Object par1 = in.readObject();
        Object par2 = in.readObject();

        if (par1 == null || !(par1 instanceof String)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        if (par2 == null || !(par2 instanceof Integer)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        String username = (String) par1;
        int clientID = (Integer) par2;

        getBuffer().unsubscribeRequests(username, clientID);
        this.writeResult(true);
    }

    /**
     * Unsubscribes client with given ID from his personal buffer.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void unsubscribeSorted() throws IOException, ClassNotFoundException {
        Object par1 = in.readObject();
        Object par2 = in.readObject();

        if (par1 == null || !(par1 instanceof String)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        if (par2 == null || !(par2 instanceof Integer)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        String username = (String) par1;
        int clientID = (Integer) par2;

        getBuffer().unsubscribeSorted(username, clientID);
        this.writeResult(true);
    }

    /**
     * Sends newly submitted data requests since last method call.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void sendNewSent() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject instanceof Integer) {
            writeOutput(getBuffer().collectUnsorted((int) inObject));
        } else {
            writeResult(false);
        }
    }

    /**
     * Subscribes client with given id for his own buffer of datarequests.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void subscribeSent() throws IOException, ClassNotFoundException {
        Object par1 = in.readObject();
        Object par2 = in.readObject();

        if (par1 == null || !(par1 instanceof String)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        if (par2 == null || !(par2 instanceof Integer)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        String username = (String) par1;
        int clientID = (Integer) par2;

        getBuffer().subscribeSent(username, clientID);
        this.writeResult(true);
    }

    /**
     * Unsubscribes client with given ID from his personal buffer.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void unsubscribeSent() throws IOException, ClassNotFoundException {
        Object par1 = in.readObject();
        Object par2 = in.readObject();

        if (par1 == null || !(par1 instanceof String)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        if (par2 == null || !(par2 instanceof Integer)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        String username = (String) par1;
        int clientID = (Integer) par2;

        getBuffer().unsubscribeSent(username, clientID);
        this.writeResult(true);
    }

    /**
     * Sends newly submitted data tasks since last method call.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void sendNewTasks() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject instanceof Integer) {
            writeOutput(getBuffer().collectTasks((int) inObject));
        } else {
            writeResult(false);
        }
    }

    /**
     * Subscribes client with given id for his own buffer of Tasks.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void subscribeTasks() throws IOException, ClassNotFoundException {
        Object par1 = in.readObject();
        Object par2 = in.readObject();

        if (par1 == null || !(par1 instanceof String)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        if (par2 == null || !(par2 instanceof Integer)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        String username = (String) par1;
        int clientID = (Integer) par2;

        getBuffer().subscribeTasks(username, clientID);
        this.writeResult(true);
    }

    /**
     * Unsubscribes client with given ID from his personal buffer.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void unsubscribeTasks() throws IOException, ClassNotFoundException {
        Object par1 = in.readObject();
        Object par2 = in.readObject();

        if (par1 == null || !(par1 instanceof String)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        if (par2 == null || !(par2 instanceof Integer)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        String username = (String) par1;
        int clientID = (Integer) par2;

        getBuffer().unsubscribeTasks(username, clientID);
        this.writeResult(true);
    }

    /**
     * Saves a task and sends it to the executor
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void sendTask() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject == null || !(inObject instanceof ITask)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        ITask task = (ITask) inObject;
        boolean success = false;

        synchronized (TASKSLOCK) {
            task = ServerMain.tasksDatabaseManager.insertNewTask(task);
        }
        if (task != null) {
            getBuffer().addTask(task);
            success = true;
        }
        this.writeResult(success);
    }

    /**
     * Saves a new plan
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void saveNewPlan() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();
        if (inObject == null || !(inObject instanceof IPlan)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        IPlan plan = (IPlan) inObject;
        boolean success = false;

        synchronized (TASKSLOCK) {
            plan = ServerMain.tasksDatabaseManager.insertNewPlan(plan);
        }

        if (plan != null) {
            success = true;
        }

        this.writeResult(success);
    }

    /**
     * Applies a plan, save to database and send its steps to the executors
     */
    private void applyPlan() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();

        if (inObject == null || !(inObject instanceof IPlan)) {
            out.writeObject(ConnState.COMMAND_ERROR);
        }

        IPlan plan = (IPlan) inObject;
        boolean success = false;

        synchronized (TASKSLOCK) {
            plan = ServerMain.tasksDatabaseManager.insertNewPlan(plan);
        }

        if (plan != null) {
            getPlanExecutorHandler().addPlanExecutor(plan);
            success = true;
        }
        this.writeResult(success);
    }

    /**
     * Files a request for an update to given piece of info.
     */
    private void getSigninUser() throws IOException, ClassNotFoundException {
        Object par1 = in.readObject();
        Object par2 = in.readObject();

        if (par1 == null || !(par1 instanceof String)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        if (par2 == null || !(par2 instanceof String)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        String username = (String) par1;
        String password = (String) par2;

        IUser output = null;
        synchronized (TASKSLOCK) {
            output = ServerMain.tasksDatabaseManager.loginUser(username, password);
        }
        writeOutput(output);
    }

    /**
     * Get tasks from database
     */
    private void getTasks() throws IOException, ClassNotFoundException {
        Object par1 = in.readObject();
        Object par2 = in.readObject();

        if (par1 == null || !(par1 instanceof String)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }
        if (par2 == null || !(par2 instanceof HashSet)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        String username = (String) par1;
        HashSet<TaskStatus> statusses = (HashSet<TaskStatus>) par2;

        List<ITask> output = null;
        synchronized (TASKSLOCK) {
            output = ServerMain.tasksDatabaseManager.getTasks(username, statusses);
        }
        writeOutput(output);
    }

    /**
     * Updates the task in the database Adds the updated task to the buffer for
     * the HQChief Handles the task if it is a step
     */
    private void updateTask() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();

        if (inObject == null || !(inObject instanceof ITask)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        ITask task = (ITask) inObject;
        boolean success = false;

        synchronized (TASKSLOCK) {
            success = ServerMain.tasksDatabaseManager.updateTask(task);
        }

        if (task.getStatus() != TaskStatus.READ) {
            // Add to buffer of HQChief
            getBuffer().addTaskForChief(task);
        }

        if ((task.getStatus() == TaskStatus.SUCCEEDED || task.getStatus() == TaskStatus.FAILED) && task instanceof IStep) {
            // Execute next step of plan
            getPlanExecutorHandler().executeNextStepOf((IStep) task);
        }
        this.writeResult(success);
    }

    /**
     * Get plans with keywords from database
     */
    private void searchPlans() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();

        if (inObject == null || !(inObject instanceof HashSet)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        HashSet<String> keywords = (HashSet) inObject;

        List<IPlan> output = null;
        synchronized (TASKSLOCK) {
            output = ServerMain.tasksDatabaseManager.getTemplatePlans(keywords);
        }
        writeOutput(output);
    }

    /**
     * Get all sorted data from database
     */
    private void getSortedData() throws IOException, ClassNotFoundException {
        List<ISortedData> output = null;
        synchronized (SORTEDLOCK) {
            output = ServerMain.sortedDatabaseManager.getFromSortedData(new HashSet<Tag>());
        }
        writeOutput(output);
    }

    /**
     * Get all serviceusers from database
     */
    private void getServiceUsers() throws IOException, ClassNotFoundException {
        List<IServiceUser> output = null;
        synchronized (TASKSLOCK) {
            output = ServerMain.tasksDatabaseManager.getServiceUsers();
        }
        writeOutput(output);
    }

    /**
     * Send the NewsItem to the database
     */
    private void saveNewsItem() throws IOException, ClassNotFoundException {
        Object inObject = in.readObject();

        if (inObject == null || !(inObject instanceof INewsItem)) {
            out.writeObject(ConnState.COMMAND_ERROR);
            return;
        }

        INewsItem item = (INewsItem) inObject;
        boolean success = false;

        synchronized (SORTEDLOCK) {
            item = ServerMain.sortedDatabaseManager.insertNewsItem(item);
        }

        if (item != null) {
            success = true;
        }

        this.writeResult(success);
    }
    
    private void getSituations(){
        List<Situation> output = null;
        synchronized (SORTEDLOCK){
            output = ServerMain.sortedDatabaseManager.getSituations();
        }
        this.writeOutput(output);
    }
}
