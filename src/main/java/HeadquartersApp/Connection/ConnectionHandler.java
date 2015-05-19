/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.Connection;

import Shared.Connection.ClientConnection;
import HeadquartersApp.UI.HeadquartersController;
import HeadquartersApp.UI.HeadquartersLogInController;
import Shared.Connection.Transaction.ConnCommand;
import Shared.Connection.SerializeUtils;
import Shared.Connection.Transaction.ServerBoundTransaction;
import Shared.Data.DataRequest;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.INewsItem;
import Shared.Data.ISortedData;
import Shared.Data.SortedData;
import Shared.Data.Status;
import Shared.Data.UnsortedData;
import Shared.NetworkException;
import Shared.Tag;
import Shared.Tasks.IPlan;
import Shared.Tasks.IStep;
import Shared.Tasks.ITask;
import Shared.Tasks.Plan;
import Shared.Tasks.Step;
import Shared.Tasks.Task;
import Shared.Tasks.TaskStatus;
import Shared.Users.UserRole;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Kargathia
 */
public class ConnectionHandler {

    public static final int DEFAULT_PORT = 9090;
    private ResponseHandler responder;
    private ClientConnection client;
    private int commandID;

    /**
     * Sets the loginController
     *
     * @param loginController
     */
    public void setLogInController(HeadquartersLogInController loginController) {
//        this.loginController = loginController;
        this.responder.setLoginController(loginController);
    }

    /**
     * Sets the HQController and starts pulling if not pulling yet
     *
     * @param hqController
     * @throws NetworkException if the retrieved clientId is -1
     */
    public void setHQController(HeadquartersController hqController) throws NetworkException {
        this.responder.setHQController(hqController);
//        this.hqController = hqController;
//        if (this.collectFuture == null) {
//            this.getID();
//            this.startPulling();
//        }
    }

    /**
     *
     * @param address
     * @throws IOException
     */
    public ConnectionHandler(String address) throws IOException {
        this.client = new ClientConnection(InetAddress.getByName(address), DEFAULT_PORT);
        this.commandID = 0;

        Thread clientThread = new Thread(client);
        clientThread.setDaemon(true);
        clientThread.start();

        this.responder = new ResponseHandler();
        Thread handlerThread = new Thread(responder);
        handlerThread.setDaemon(true);
        handlerThread.start();
    }

    private synchronized int getCommandID() {
        return this.commandID++;
    }

    /**
     * Testing only. Takes place in lieu of unit tests.
     */
    private void testMethods() {
        this.getData();
        this.discardUnsortedData(new UnsortedData("discardTitle", "discardDesc", "discardLoc", "discardSource"));
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        this.requestUpdate(new DataRequest(1, "requestTitle", "reqDesc", "recLoc", "recSource", 2, tags));
        tags.add(Tag.AMBULANCE);
        ISortedData sortedData = new SortedData(2, "sortTitle", "sortDesc", "sortLoc", "sortSource", 3, 2, 1, tags);
        this.sendSortedData(sortedData);
        ArrayList<IData> data = new ArrayList<>();
        data.add(new UnsortedData(3, "resetTitle", "resetDesc", "resetLoc", "resetSource", Status.NONE));
        this.stopWorkingOnData(data);

        List<IStep> steps = new ArrayList<>();
        ITask task = new Task(-1, "newTaskTitle", "newTaskDesc", TaskStatus.INPROCESS, null, Tag.POLICE, null);
        steps.add(new Step(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getSortedData(), task.getTargetExecutor(), task.getExecutor(), 1, ""));

        this.sendTask(task);
        HashSet<String> keywords = new HashSet<>();
        keywords.add("Brand");
        this.sendNewPlan(new Plan(-1, "newPlanTitle", "newPlanDesc", keywords, steps, true));
    }

    /**
     * Terminates the active pool, in preparation for program shutdown.
     */
    public void close() {
//        if (this.collectFuture != null) {
//            this.collectFuture.cancel(false);
//        }
//        pool.shutdown();
    }

    /**
     * Registers user after logging in for receiving updates appropriate for his
     * role.
     *
     * @param role
     */
    public void registerForUpdates(UserRole role) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.USERS_REGISTER, role);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sends sorted data to server @ default IP / port
     *
     * @param data
     */
    public void sendSortedData(ISortedData data) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.SORTED_SEND, data);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Queries server for batch of unsorted data from default IP / port.
     * Automatically calls HeadquartersController.displayData(data) on
     * completion
     */
    public void getData() {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.UNSORTED_GET);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Signals server that HQ will not process this list of data.
     *
     * @param data
     */
    public void stopWorkingOnData(ArrayList<IData> data) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.UNSORTED_STATUS_RESET, data);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Signals server that this headquarters client is no longer working on
     * given data.
     *
     * @param data
     */
    public void discardUnsortedData(IData data) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.UNSORTED_DISCARD, data);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Files a request for an update of given piece of data with the server.
     *
     * @param data
     */
    public void requestUpdate(IDataRequest data) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.UPDATE_REQUEST_SEND, data);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sends the given task to the server
     *
     * @param task cannot be null
     */
    public void sendTask(ITask task) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.TASK_SEND, task);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sends the new given plan to the server
     *
     * @param plan cannot be null
     */
    public void sendNewPlan(IPlan plan) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.PLAN_SEND_NEW, plan);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Applies a plan and send its steps to the executors
     *
     * @param plan cannot be null
     */
    public void applyPlan(IPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Voer een plan in");
        }

        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.PLAN_APPLY, plan);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get IUser from server with given username and password. Gives IUser to
     * servicesController.
     *
     * @param username
     * @param password
     */
    public void getSigninUser(String username, String password) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.USERS_SIGN_IN, username, password);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Search for plans with the given keywords Sends returnvalue to
     * hqController.displayPlans()
     *
     * @param keywords if hashSet is empty, get all plans
     */
    public void searchPlans(HashSet<String> keywords) {
        if (keywords == null) {
            return;
        }

        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.PLAN_SEARCH, keywords);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets all sorted data Sends returnvalue to
     * hqController.displaySortedData()
     */
    public void getSortedData() {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.SORTED_GET_ALL, new HashSet<Tag>());
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets all serviceUsers Sends returnvalue to
     * hqController.displayServiceUsers()
     */
    public void getServiceUsers() {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.USERS_GET_SERVICE);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets all tasks Sends returnvalue to hqController.displayTasks()
     */
    public void getTasks() {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.TASKS_GET, null, new HashSet<TaskStatus>());
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Updates the status of the task Used to update the status to
     * TaskStatus.READ No returnvalue
     *
     * @param task
     */
    public void updateTask(ITask task) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.TASK_UPDATE, task);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sends the NewsItem to the server
     *
     * @param item
     */
    public void sendNewsItem(INewsItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Voer een niewsbericht in");
        }

        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.NEWSITEM_SEND, item);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sends the NewsItem to the server, where it will update the existing
     * NewsItem.
     *
     * @param item
     */
    public void updateNewsItem(INewsItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Selecteer een nieuwsbericht om te updateten");
        }

        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.NEWSITEM_UPDATE, item);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets all situations from server and sends the returnvalue to
     * hqController.displaySituations()
     */
    public void getSituations() {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.SITUATIONS_GET);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
