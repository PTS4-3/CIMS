/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.Connection;

import HeadquartersApp.UI.HeadquartersController;
import Shared.Data.*;
import Shared.Tag;
import Shared.Tasks.*;
import Shared.Users.IServiceUser;
import Shared.Users.ServiceUser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Kargathia + Alexander
 */
public class ConnectionManager {

    public static final int DEFAULT_PORT = 8189;
    
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private HeadquartersController hqController = null;
    private HeadquartersLogInContoller loginController = null;
    private String defaultIP = "127.0.0.1";
    private int defaultPort;

    public ConnectionManager(HeadquartersLogInController loginController,
            String defaultIP) {

        this.defaultIP = defaultIP;
        this.defaultPort = DEFAULT_PORT;
        this.loginController = loginController;
        this.testMethods();
    }
    
    public void setHQController(HeadquartersController hqController) {
        this.hqController = hqController;
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
        
        TreeSet<IStep> steps = new TreeSet<>();
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
    protected void sendNewPlan(IPlan plan) {
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
    protected void applyPlan(IPlan plan) {
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
}
