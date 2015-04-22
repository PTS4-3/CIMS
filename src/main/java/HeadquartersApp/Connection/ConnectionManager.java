/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.Connection;

import HeadquartersApp.UI.HeadquartersController;
import HeadquartersApp.UI.HeadquartersLogInController;
import Shared.Data.*;
import Shared.NetworkException;
import Shared.Tag;
import Shared.Tasks.*;
import Shared.Users.IUser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
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
    private static final String HQChief = "HQChief";
    
    private final ScheduledExecutorService pool = Executors.newScheduledThreadPool(4);
    private ScheduledFuture collectFuture;
    
    private int clientId;
    private AtomicBoolean isRegisteredSorted;
    private AtomicBoolean isRegisteredTasks;
    
    private HeadquartersController hqController = null;
    private HeadquartersLogInController loginController = null;
    
    private String defaultIP = "127.0.0.1";
    private int defaultPort;

    public ConnectionManager(String defaultIP) {
        this.defaultIP = defaultIP;
        this.defaultPort = DEFAULT_PORT;
        this.isRegisteredSorted.set(false);
        this.isRegisteredTasks.set(false);
        //this.testMethods();
    }
    
    /**
     * Sets the loginController
     * @param loginController 
     */
    public void setLogInController(HeadquartersLogInController loginController) {
        this.loginController = loginController;
    }
    
    /**
     * Sets the HQController and starts pulling if not pulling yet
     * @param hqController
     * @throws NetworkException if the retrieved clientId is -1
     */
    public void setHQController(HeadquartersController hqController) throws NetworkException {
        this.hqController = hqController;
        if(this.collectFuture != null) {
            this.getID();
            this.startPulling();
        }
    }
    
    /**
     * Gets the clientId from the server
     * @throws NetworkException if the retrieved clientId is -1
     */
    private void getID() throws NetworkException {
        this.clientId = new Connection(defaultIP, defaultPort).getClientId();
        if(this.clientId == -1) {
            throw new NetworkException("ClientId is -1");
        }
    }
    
    /**
     * Starts pulling for new information
     */
    private void startPulling() {
        this.collectFuture = this.pool.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                getNewSortedData();
                getNewTasks();
            }
            
        }, collectionIntervalInMillis, collectionIntervalInMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Testing only. Takes place in lieu of unit tests.
     */
    private void testMethods(){
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
    
    public void setDefaultPort(int port){
        this.defaultPort = port;
    }

    /**
     * Terminates the active pool, in preparation for program shutdown.
     */
    public void close(){
        if(this.collectFuture != null) {
            this.collectFuture.cancel(false);
        }
        pool.shutdown();
    }

    /**
     * Sends sorted data to server @ default IP / port
     *
     * @param data
     */
    public void sendSortedData(ISortedData data) {
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).sendSortedData(data);
        });
        
    }

    /**
     * Queries server for batch of unsorted data from default IP / port.
     * Automatically calls HeadquartersController.displayData(data) on
     * completion
     */
    public void getData() {
        if(this.hqController != null) {
            pool.execute(() -> {
                List<IData> output;
                output = new Connection(defaultIP, defaultPort).getData();
                if(output != null){
                    hqController.displayData(output);
                } else {
                    System.err.println("Unable to retrieve Unsorted Data from server.");              
                }
            });   
        }
    }

    /**
     * Signals server that HQ will not process this list of data.
     *
     * @param data
     */
    public void stopWorkingOnData(ArrayList<IData> data) {
        if(data == null){
            System.err.println("Null parameter in stopWorkingOnData");
            return;
        }
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).stopWorkingOnData(data);
        });
    }

    /**
     * Signals server that this headquarters client is no longer working on
     * given data.
     *
     * @param data
     */
    public void discardUnsortedData(IData data) {
        if(data == null){
            System.err.println("Null parameter in discardUnsortedData");
            return;
        }
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).discardUnsortedData(data);
        });
        
    }

    /**
     * Files a request for an update of given piece of data with the server.
     * @param data
     */
    public void requestUpdate(IDataRequest data) {
        if(data == null){
            System.err.println("Null parameter in requestUpdate");
            return;
        }
        pool.execute(() -> {
            new Connection(defaultIP, defaultPort).requestUpdate(data);
        });
        
    }
    
    /**
     * Sends the given task to the server
     * @param task cannot be null
     */
    public void sendTask(ITask task) {
        if(task == null) {
            throw new IllegalArgumentException("Voer een taak in");
        }
        
        pool.execute(new Runnable() {

            @Override
            public void run() {
                new Connection(defaultIP, defaultPort).sendTask(task);
            }
            
        });
    }
    
    /**
     * Sends the new given plan to the server
     * @param plan cannot be null
     */
    public void sendNewPlan(IPlan plan) {
        if(plan == null) {
            throw new IllegalArgumentException("Voer een nieuw plan in"); 
        }
        
        pool.execute(new Runnable() {

            @Override
            public void run() {
                new Connection(defaultIP, defaultPort).sendNewPlan(plan);
            }
            
        });
    }
    
    /**
     * Applies a plan and send its steps to the executors
     * @param plan cannot be null
     */
    public void applyPlan(IPlan plan) {
        if(plan == null) {
            throw new IllegalArgumentException("Voer een plan in");
        }
        
        pool.execute(new Runnable() {

            @Override
            public void run() {
                new Connection(defaultIP, defaultPort).applyPlan(plan);
            }
            
        });
    }
    
    /**
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
                if (output != null) {
                    this.loginController.logIn(output);
                } else {
                    System.err.println("Unable to retrieve IUser from "
                            + "buffer in server.");
                }
            });  
        }
    }
    
    /**
     * Search for plans with the given keywords
     * Sends returnvalue to hqController.displayPlans()
     * @param keywords if hashSet is empty, get all plans
     */
    public void searchPlans(HashSet<String> keywords) {
        
    }
    
    /**
     * Gets all sorted data 
     * Sends returnvalue to hqController.displaySortedData()
     */
    public void getSortedData() {
        
    }
    
    /**
     * Gets all serviceUsers
     * Sends returnvalue to hqController.displayServiceUsers()
     */
    public void getServiceUsers() {
        
    }
        
    /**
     * Subscribes to get updates for sortedData for HQChief
     */
    public void subscribeSortedData() {
        
    }
    
    /**
     * Unsubscribes to get updates for sortedData for HQChief
     */
    public void unsubscribeSortedData() {
        
    }
    
    /**
     * Get updates for sorted data
     */
    private void getNewSortedData() {
        
    }
    
    /**
     * Subscribes to get updates for the status of tasks for HQChief
     */
    public void subscribeTasks() {
        
    }
    
    /**
     * Unsubscribes to get updates for the status of tasks for HQChief
     */
    public void unsubscribeTasks() {
        
    }
    
    /**
     * Get updates for the status of tasks
     */
    private void getNewTasks() {
        
    }
    
    /**
     * Updates the status of the task
     * Used to update the status to TaskStatus.READ
     * No returnvalue
     * @param task 
     */
    public void updateTask(ITask task) {
        
    }
}
