/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.Connection;

import Shared.Connection.ClientConnection;
import ServicesApp.UI.ServicesController;
import ServicesApp.UI.ServicesLogInController;
import Shared.Connection.Transaction.ConnCommand;
import Shared.Connection.SerializeUtils;
import Shared.Connection.Transaction.ServerBoundTransaction;
import Shared.Data.IData;
import Shared.Data.Status;
import Shared.NetworkException;
import Shared.Tag;
import Shared.Tasks.ITask;
import Shared.Users.IServiceUser;
import Shared.Users.UserRole;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Kargathia
 */
public class ConnectionHandler {

    public static final int DEFAULT_PORT = 9090;
    private ResponseHandler responder;
    private ClientConnection client;
    private int commandID;

    private final HashMap<Integer, ConnCommand> inProgressCommands = new HashMap<>();

    /**
     * Sets the loginController
     *
     * @param controller
     */
    public void setLogInController(ServicesLogInController controller) {
        this.responder.setLoginController(controller);
    }

    /**
     * Sets the HQController and starts pulling if not pulling yet
     *
     * @param controller
     * @throws NetworkException if the retrieved clientId is -1
     */
    public void setServicesController(ServicesController controller) {
        this.responder.setServicesController(controller);
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

        this.responder = new ResponseHandler(this);
        Thread handlerThread = new Thread(responder);
        handlerThread.setDaemon(true);
        handlerThread.start();
    }

    private synchronized int getCommandID() {
        return this.commandID++;
    }

    /**
     * Keeps track of transactions sent to server.
     *
     * @param transaction
     */
    private void registerCommandSent(ServerBoundTransaction transaction) {
        synchronized (inProgressCommands) {
            inProgressCommands.put(transaction.ID, transaction.command);
        }
    }

    /**
     * Keeps track of transactions answered by server - called by any response
     * from server, even if it was an error or a fail.
     *
     * @param ID
     */
    protected void notifyCommandResponse(int ID) {
        synchronized (inProgressCommands) {
            inProgressCommands.remove(ID);
            System.out.println("Non-answered commands remaining: "
                    + inProgressCommands.size());
        }
    }

    /**
     * // TODO: find some use for this
     */
    public void close() {
    }

    /**
     * Registers user after logging in for receiving updates appropriate for his
     * role.
     *
     * @param user
     * @param role
     */
    public void registerForUpdates(IServiceUser user) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.USERS_REGISTER,
                        UserRole.SERVICE, user.getType(), user.getUsername());
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
            this.registerCommandSent(transaction);
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
            this.registerCommandSent(transaction);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void getRequests(HashSet<Tag> tags) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.UPDATE_REQUEST_GET, tags);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
            this.registerCommandSent(transaction);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void getSortedData(HashSet<Tag> tags) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.SORTED_GET, tags);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
            this.registerCommandSent(transaction);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void getSentData(String username) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.UNSORTED_GET_SOURCE, username);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
            this.registerCommandSent(transaction);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void getTasks(String username) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.TASKS_GET, username, new HashSet<Status>());
        // empty set because there is no filter
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
            this.registerCommandSent(transaction);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendUnsortedData(IData data) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.UNSORTED_SEND, data);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
            this.registerCommandSent(transaction);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void updateUnsortedData(IData update) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.UNSORTED_UPDATE_SEND, update);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
            this.registerCommandSent(transaction);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void getDataItem(int requestId) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.UNSORTED_GET_ID, requestId);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
            this.registerCommandSent(transaction);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void updateTask(ITask selectedTask) {
        ServerBoundTransaction transaction
                = new ServerBoundTransaction(this.getCommandID(),
                        ConnCommand.TASK_UPDATE, selectedTask);
        try {
            this.client.send(SerializeUtils.serialize(transaction), responder);
            this.registerCommandSent(transaction);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
