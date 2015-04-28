/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.Connection;

import ServicesApp.UI.ServicesController;
import ServicesApp.UI.ServicesLogInController;
import Shared.Connection.ConnCommand;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.Data.Status;
import Shared.Tag;
import Shared.Data.UnsortedData;
import Shared.Tasks.ITask;
import Shared.Users.IUser;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Kargathia + Alexander
 */
public class ConnectionManager {

    public static final int DEFAULT_PORT = 8189;
    private static int collectionIntervalInMillis = 10000;

    private final ScheduledExecutorService pool = Executors.newScheduledThreadPool(4);
    private ScheduledFuture collectFuture = null;
    private int defaultPort = DEFAULT_PORT;
    private String defaultIP;
    private int clientID;
    private ServicesController servicesController = null;
    private ServicesLogInController loginController = null;
    private AtomicBoolean
            isRegisteredRequests,
            isRegisteredSorted,
            isRegisteredUnsorted,
            isRegisteredTasks;

    /**
     * Starts a new ConnectionManager, responsible for
     * @param defaultIP
     */
    public ConnectionManager(String defaultIP) {
        this.defaultIP = defaultIP;
        this.isRegisteredSorted = new AtomicBoolean(false);
        this.isRegisteredRequests = new AtomicBoolean(false);
        this.isRegisteredUnsorted = new AtomicBoolean(false);
        this.isRegisteredTasks = new AtomicBoolean(false);
        //this.testMethods();
    }
    
    /**
     * Sets loginController
     * @param loginController 
     */
    public void setLogInController(ServicesLogInController loginController) {
        this.loginController = loginController;
    }
    
    /**
     * Sets ServicesController and starts pulling if not pulling yet
     * @param servicesController 
     */
    public void setServicesController(ServicesController servicesController) {
        this.servicesController = servicesController;
        if(this.collectFuture == null) {
            this.getID();
            this.startCollectTask();
        }
    }

    /**
     * Testing only. Takes place in lieu of unit tests.
     */
    private void testMethods() {
        this.getDataItem(5);
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.CITIZENS);
        this.getRequests(tags);
        this.getSentData("sourceInput");
        tags.add(Tag.FIREDEPARTMENT);
        this.getSortedData(tags);
        this.subscribeRequests("firefighter01");
        this.subscribeSorted("firefighter01");
        this.subscribeUnsorted("firefighter01");
        this.sendUnsortedData(new UnsortedData(-1, "servicesTitle", "desc", "loc", "source", Status.NONE));
        this.updateUnsortedData(new UnsortedData(5, "updateTitle", "updateDesc", "updateLoc", "updateSource", Status.NONE));
        this.getNewRequests();
        this.getNewSorted();
        this.getNewUnsorted();
    }

    /**
     * Schedules regular collection of new subscribed items.
     */
    private void startCollectTask(){
        Runnable collectionTask = () -> {
            this.getNewRequests();
            this.getNewSorted();
            this.getNewUnsorted();
            this.getNewTasks();
        };

        this.collectFuture = pool.scheduleWithFixedDelay(
                collectionTask,
                collectionIntervalInMillis,
                collectionIntervalInMillis,
                TimeUnit.MILLISECONDS);

    }

    /**
     * Queries server to be assigned his unique ID. Is executed on main thread,
     * as later operations rely on this being completed.
     */
    private void getID() {
        this.clientID = new Connection(defaultIP, defaultPort).getClientID();
        if (this.clientID == -1) {
            System.err.println("Error retrieving clientID from server");
        } else {
            System.out.println("clientID: " + this.clientID);
        }
    }

    /**
     * Sets custom value for used port.
     *
     * @param port
     */
    public void setPort(int port) {
        this.defaultPort = port;
    }

    /**
     * Orderly shuts down thread pool after all requests are handled.
     */
    public void closeConnection() {
        if(collectFuture != null){
            collectFuture.cancel(false);
        } 
        pool.shutdown();
    }

    /**
     * Called to notify that a command is done. Functions as a spam blocker.
     *
     * @param action
     */
    private void notifyCommandDone(ConnCommand action) {
        switch (action) {
            case SORTED_SUBSCRIBE:
                this.isRegisteredSorted.set(true);
                break;
            case SORTED_UNSUBSCRIBE:
                this.isRegisteredSorted.set(false);
                break;
            case UPDATE_REQUEST_SUBSCRIBE:
                this.isRegisteredRequests.set(true);
                break;
            case UPDATE_REQUEST_UNSUBSCRIBE:
                this.isRegisteredRequests.set(false);
                break;
            case SENT_SUBSCRIBE:
                this.isRegisteredUnsorted.set(true);
                break;
            case SENT_UNSUBSCRIBE:
                this.isRegisteredUnsorted.set(false);
                break;
            case TASKS_SUBSCRIBE:
                this.isRegisteredTasks.set(true);
                break;
            case TASKS_UNSUBSCRIBE:
                this.isRegisteredTasks.set(false);
                break;
        };

    }

    /**
     * Sends sorted data to server
     *
     * @param data
     */
    public void sendUnsortedData(IData data) {
        if (data == null) {
            System.err.println("Null parameter in sendUnsortedData");
            return;
        }
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).sendUnSortedData(data);
        });
    }

    /**
     * Queries server for sorted data satisfying all given tags. Automatically
     * calls ServicesController.displaySortedData() on completion
     *
     * @param tags
     */
    public void getSortedData(HashSet<Tag> tags) {
        if(this.servicesController != null) {
            if (tags == null) {
                System.err.println("Null parameter in getSortedData");
                return;
            }
            pool.execute(() -> {
                List<ISortedData> output = null;
                output = new Connection(defaultIP, defaultPort).getSortedData(tags);
                if (output != null) {
                    servicesController.displaySortedData(output);
                } else {
                    System.err.println("Unable to retrieve sorted data from server");
                }
            });
        }
    }

    /**
     * Gets all data requests conforming to all given tags. Convenience method.
     * Automatically calls ServicesController.displayRequests() on completion.
     *
     * @param tags
     */
    public void getRequests(HashSet<Tag> tags) {
        if(this.servicesController != null) {
            if (tags == null) {
                System.err.println("Null parameter in getRequests");
                return;
            }
            pool.execute(() -> {
                List<IDataRequest> output = null;
                output = new Connection(defaultIP, defaultPort).getDataRequests(tags);
                if (output != null) {
                    servicesController.displayRequests(output);
                } else {
                    System.err.println("Unable to retrieve requests from server.");
                }
            });
        }
    }

    /**
     * Updates data with given id with given IData.
     *
     * @param data
     */
    public void updateUnsortedData(IData data) {
        if (data == null) {
            System.err.println("Null parameter in updateUnsortedData");
            return;
        }
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).updateUnsortedData(data);
        });
    }

    /**
     *
     * @param source
     */
    public void getSentData(String source) {
        if(this.servicesController != null) {
            if (source == null) {
                System.err.println("Null parameter in getSentData");
                return;
            }
            pool.execute(() -> {
                List<IData> output = null;
                output = new Connection(defaultIP, defaultPort).getSentData(source);
                if (output != null) {
                    servicesController.displaySentData(output);
                } else {
                    System.err.println("Unable to retrieve sent data from server.");
                }
            });
        }
    }

    /**
     * Queries server for IData with given ID. Calls
     * ServicesController.displayDataItem() on arrival.
     *
     * @param id
     */
    public void getDataItem(int id) {
        if(this.servicesController != null) {
            pool.execute(() -> {
                IData output = null;
                output = new Connection(defaultIP, defaultPort).getDataItem(id);
                if (output != null) {
                    servicesController.displayDataItem(output);
                } else {
                    System.err.println("Unable to retrieve specific data item from server.");
                }
            });
        }
    }

    /**
     * Subscribes this client to updates of all new sorted data items. call
     * getNewSorted to collect.
     *
     * @return Whether it was able to execute this command right now. A true
     * return type does not guarantee the command is executed, merely that it is
     * transmitted to server.
     */
    public boolean subscribeSorted(String username) {
        if (this.isRegisteredSorted.get()) {
            return false;
        }
        pool.execute(() -> {
            if(new Connection(defaultIP, defaultPort).subscribeSorted(username, this.clientID)){
                this.notifyCommandDone(ConnCommand.SORTED_SUBSCRIBE);
            }
        });
        return true;
    }

    /**
     * Unsubscribes this client from updates. Does nothing if client wasn't
     * subscribed.
     *
     * @return Whether it was able to execute this command right now. A true
     * return type does not guarantee the command is executed, merely that it is
     * transmitted to server.
     */
    public boolean unsubscribeSorted(String username) {
        if (!this.isRegisteredSorted.get()) {
            return false;
        }
        pool.execute(() -> {
            if(new Connection(defaultIP, defaultPort).unsubscribeSorted(username, this.clientID)){
                this.notifyCommandDone(ConnCommand.SORTED_UNSUBSCRIBE);
            }
        });
        return true;
    }

    /**
     * Subscribes this client to updates of all new data requests submitted to
     * server. Call getNewRequests to collect.
     *
     * @return Whether it was able to execute this command right now. A true
     * return type does not guarantee the command is executed, merely that it is
     * transmitted to server.
     */
    public boolean subscribeRequests(String username) {
        if (this.isRegisteredRequests.get()) {
            return false;
        }
        pool.execute(() -> {
            if(new Connection(defaultIP, defaultPort).subscribeRequests(username, this.clientID)){
                this.notifyCommandDone(ConnCommand.UPDATE_REQUEST_SUBSCRIBE);
            }
        });
        return true;
    }
    
    /**
     * Subscribes this client to updates of all new tasks submitted to
     * server. Call getNewTasks to collect.
     *
     * @return Whether it was able to execute this command right now. A true
     * return type does not guarantee the command is executed, merely that it is
     * transmitted to server.
     */
    public boolean subscribeTasks(String username) {
        if (this.isRegisteredTasks.get()) {
            return false;
        }
        pool.execute(new Runnable() {
            @Override
            public void run() {
                if(new Connection(defaultIP, defaultPort).subscribeTasks(username, clientID)){
                    notifyCommandDone(ConnCommand.TASKS_SUBSCRIBE);
                }
            }
        });
        return true;
    }

    /**
     * Unsubscribes this client from updates. Does nothing if client wasn't
     * subscribed.
     *
     * @return Whether it was able to execute this command right now. A true
     * return type does not guarantee the command is executed, merely that it is
     * transmitted to server.
     */
    public boolean unsubscribeRequests(String username) {
        if (!this.isRegisteredRequests.get()) {
            return false;
        }
        pool.execute(() -> {
            if(new Connection(defaultIP, defaultPort).unsubscribeRequests(username, this.clientID)){
                this.notifyCommandDone(ConnCommand.UPDATE_REQUEST_UNSUBSCRIBE);
            }
        });
        return true;
    }

    /**
     * Subscribes this client to updates of all new sorted data items. call
     * getNewSorted to collect.
     *
     * @return Whether it was able to execute this command right now. A true
     * return type does not guarantee the command is executed, merely that it is
     * transmitted to server.
     */
    public boolean subscribeUnsorted(String username) {
        if (this.isRegisteredUnsorted.get()) {
            return false;
        }
        pool.execute(() -> {
            if(new Connection(defaultIP, defaultPort).subscribeSent(username, this.clientID)){
                this.notifyCommandDone(ConnCommand.SENT_SUBSCRIBE);
            }
        });
        return true;
    }

    /**
     * Unsubscribes this client from updates. Does nothing if client wasn't
     * subscribed.
     *
     * @return Whether it was able to execute this command right now. A true
     * return type does not guarantee the command is executed, merely that it is
     * transmitted to server.
     */
    public boolean unsubscribeUnsorted(String username) {
        if (!this.isRegisteredUnsorted.get()) {
            return false;
        }
        pool.execute(() -> {
            if(new Connection(defaultIP, defaultPort).unsubscribeSent(username, this.clientID)){
                this.notifyCommandDone(ConnCommand.SENT_UNSUBSCRIBE);
            }
        });
        return true;
    }
    
    /**
     * Unsubscribes this client from updates. Does nothing if client wasn't
     * subscribed.
     *
     * @return Whether it was able to execute this command right now. A true
     * return type does not guarantee the command is executed, merely that it is
     * transmitted to server.
     */
    public boolean unsubscribeTasks(String username) {
        if (!this.isRegisteredTasks.get()) {
            return false;
        }
        pool.execute(new Runnable() {

            @Override
            public void run() {
                if(new Connection(defaultIP, defaultPort).unsubscribeTasks(username, clientID)) {
                    notifyCommandDone(ConnCommand.TASKS_UNSUBSCRIBE);
                }
            }
            
        });
        return true;
    }

    /**
     * Collects all new sorted data collected on server since last call. Client
     * needs to have called subscribeSorted() for this to do anything.
     *
     * @return Whether it was able to execute this command right now. A true
     * return type does not guarantee the command is executed, merely that it is
     * transmitted to server.
     */
    public boolean getNewSorted() {
        if(this.servicesController != null) {
            return false;
        }
        if (!this.isRegisteredSorted.get()) {
            return false;
        }
        pool.execute(() -> {
            List<ISortedData> output
                    = new Connection(defaultIP, defaultPort).getNewSorted(this.clientID);
            if (output != null) {
                this.servicesController.displaySortedData(output);
            } else {
                System.err.println("Unable to retrieve new Sorted Data from "
                        + "buffer in server.");
            }
        });
        return true;
    }

    /**
     * Collects all new requests collected on server since last call. Client
     * needs to have called subscribeRequests() for this to do anything.
     *
     * @return Whether it was able to execute this command right now. A true
     * return type does not guarantee the command is executed, merely that it is
     * transmitted to server.
     */
    public boolean getNewRequests() {
        if(this.servicesController == null) {
            return false; 
        }
        if (!this.isRegisteredRequests.get()) {
            return false;
        }
        pool.execute(() -> {
            List<IDataRequest> output
                    = new Connection(defaultIP, defaultPort).getNewRequests(this.clientID);
            if (output != null) {
                this.servicesController.displayRequests(output);
            } else {
                System.err.println("Unable to retrieve new Requests from buffer in server.");
            }
        });
        return true;
    }

    /**
     * Collects all new unsorted data collected on server since last call. Client
 needs to have called subscribeSent() for this to do anything.
     *
     * @return Whether it was able to execute this command right now. A true
     * return type does not guarantee the command is executed, merely that it is
     * transmitted to server.
     */
    public boolean getNewUnsorted() {
        if(this.servicesController == null) {
            return false; 
        }
        if (!this.isRegisteredUnsorted.get()) {
            return false;
        }
        pool.execute(() -> {
            List<IData> output
                    = new Connection(defaultIP, defaultPort).getNewSent(this.clientID);
            if (output != null) {
                this.servicesController.displaySentData(output);
            } else {
                System.err.println("Unable to retrieve new Unsorted Data from "
                        + "buffer in server.");
            }
        });
        return true;        
    }
    
    /**
     * Get updates of tasks from the server
     * Returnvalue is sent to servicesController.displayTasks()
     * @return 
     */
    public boolean getNewTasks() {
        if(this.servicesController == null || !this.isRegisteredTasks.get()) {
            return false; 
        }
        
        pool.execute(new Runnable() {

            @Override
            public void run() {
                List<ITask> newTasks = 
                        new Connection(defaultIP, defaultPort).getNewTasks(clientID);
                if(newTasks != null) {
                    servicesController.displayTasks(newTasks);
                } else {
                    System.err.println("Unable to retrieve new Tasks from "
                        + "buffer in server.");
                }
            }
            
        });
        
        return true;    
    }
    
    /*
     * Get IUser from server with given username and password. Gives IUser to
     * servicesController.
     * 
     * @param username
     * @param password 
     */
    public void getSigninUser(String username, String password) {
        if(this.loginController != null) {
            pool.execute(() -> {
                IUser output
                        = new Connection(defaultIP, defaultPort).getSigninUser(username, password);
                this.loginController.logIn(output);
            });  
        }
    }
    
    /**
     * Gets the tasks from the serviceUser with the given username
     * Sends returnvalue to servicesController.displayTasks()
     * @param username cannot be null
     */
    public void getTasks(String username) {
        if(username == null) {
            throw new IllegalArgumentException("Username is null");
        }
        
        pool.execute(new Runnable() {

            @Override
            public void run() {
                List<ITask> tasks = 
                        new Connection(defaultIP, defaultPort).getTasks(username);
                if(tasks != null) {
                    servicesController.displayTasks(tasks);
                } else {
                    System.err.println("Unable to retrieve Tasks from server.");
                }
            }
            
        });
    }
    
    /**
     * Update the status of a task
     * No returnvalue
     * @param task cannot be null
     */
    public void updateTask(ITask task) {
        if(task == null) {
            throw new IllegalArgumentException("Voer een taak in");
        }
        
        pool.execute(new Runnable() {

            @Override
            public void run() {
                new Connection(defaultIP, defaultPort).updateTask(task);
            }
            
        });
    }
}
