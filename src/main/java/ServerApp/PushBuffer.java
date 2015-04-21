/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.Tasks.IStep;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Kargathia
 */
public class PushBuffer {

    private final Object
            LOCK_SORTED = "",
            LOCK_REQUESTS = "",
            LOCK_UNSORTED = "",
            LOCK_STEPS = "";

    // key: ClientID, Value: sortedData
    private HashMap<Integer, List<ISortedData>> sortedDataBuffer;
    // key: ClientID, Value: requests
    private HashMap<Integer, List<IDataRequest>> requestBuffer;
    // key: ClientID, Value: sentData
    private HashMap<Integer, List<IData>> unsortedDataBuffer;
    // key: ClientID, Value: steps
    private HashMap<Integer, List<IStep>> stepsBuffer;
    
    // key: username, Value: ClientIDs
    private HashMap<String, HashSet<Integer>> clientIDs;

    public PushBuffer() {
        sortedDataBuffer = new HashMap<>();
        requestBuffer = new HashMap<>();
        unsortedDataBuffer = new HashMap<>();
        this.stepsBuffer = new HashMap<>();
        this.clientIDs = new HashMap<>();
    }
    
    private synchronized void addClientID(String username, int clientID) {
        if(this.clientIDs.get(username) == null) {
            this.clientIDs.put(username, new HashSet<>());
        }
        this.clientIDs.get(username).add(clientID);
    }
    
    private synchronized void removeClientID(String username, int clientID) {
        if(this.sortedDataBuffer.get(clientID) == null &&
                this.unsortedDataBuffer.get(clientID) == null &&
                this.requestBuffer.get(clientID) == null) {
            this.clientIDs.get(username).remove(clientID);
            
            if(this.clientIDs.get(username).isEmpty()) {
                this.clientIDs.remove(username);
            }
        }
    }

    public void subscribeSorted(String username, int clientID) {
        this.addClientID(username, clientID);
        synchronized (LOCK_SORTED) {
            sortedDataBuffer.put(clientID, new ArrayList<>());
        }
    }

    public void subscribeRequests(String username, int clientID) {
        this.addClientID(username, clientID);
        synchronized (LOCK_REQUESTS) {
            requestBuffer.put(clientID, new ArrayList<>());
        }
    }

    public void subscribeUnsorted(String username, int clientID){
        this.addClientID(username, clientID);
        synchronized(LOCK_UNSORTED){
            unsortedDataBuffer.put(clientID, new ArrayList<>());
        }
    }
    
    public void subscribeSteps(String username, int clientID){
        this.addClientID(username, clientID);
        synchronized(LOCK_STEPS){
            stepsBuffer.put(clientID, new ArrayList<>());
        }
    }

    public void unsubscribeSorted(String username, int clientID) {
        synchronized (LOCK_SORTED) {
            sortedDataBuffer.remove(clientID);
        }
        this.removeClientID(username, clientID);
    }

    public void unsubscribeRequests(String username, int clientID) {
        synchronized (LOCK_REQUESTS) {
            requestBuffer.remove(clientID);
        }
        this.removeClientID(username, clientID);
    }

    public void unsubscribeUnsorted(String username, int clientID){
        synchronized(LOCK_UNSORTED){
            unsortedDataBuffer.remove(clientID);
        }
        this.removeClientID(username, clientID);
    }
    
    public void unsubscribeSteps(String username, int clientID){
        synchronized(LOCK_STEPS){
            stepsBuffer.remove(clientID);
        }
        this.removeClientID(username, clientID);
    }

    public void addSorted(ISortedData data) {
        //TODO
        synchronized (LOCK_SORTED) {
            for (int client : sortedDataBuffer.keySet()) {
                sortedDataBuffer.get(client).add(data);
            }
        }
    }

    public void addRequest(IDataRequest request) {
        //TODO
        synchronized (LOCK_REQUESTS) {
            for (int client : requestBuffer.keySet()) {
                requestBuffer.get(client).add(request);
            }
        }
    }

    public void addUnsorted(IData data){
        //TODO
        synchronized(LOCK_UNSORTED){
            for(int client : unsortedDataBuffer.keySet()){
                unsortedDataBuffer.get(client).add(data);
            }
        }
    }
    
    public void addStep(IStep step) {
        synchronized(LOCK_STEPS) {
            for(int client : clientIDs.get(step.getExecutor().getUsername())) {
                stepsBuffer.get(client).add(step);
            }
        }
    }

    public List<ISortedData> collectSorted(int clientID) {
        synchronized (LOCK_SORTED) {
            if (sortedDataBuffer.get(clientID) == null) {
                return null;
            }
            List<ISortedData> output = new ArrayList<>();
            List<ISortedData> buffer = sortedDataBuffer.get(clientID);
            output.addAll(buffer);
            buffer.clear();
            return output;
        }
    }

    public List<IDataRequest> collectRequests(int clientID) {
        synchronized (LOCK_REQUESTS) {
            if (requestBuffer.get(clientID) == null) {
                return null;
            }
            List<IDataRequest> output = new ArrayList<>();
            List<IDataRequest> buffer = requestBuffer.get(clientID);
            output.addAll(buffer);
            buffer.clear();
            return output;
        }
    }

    public List<IData> collectUnsorted(int clientID) {
        synchronized (LOCK_UNSORTED) {
            if (unsortedDataBuffer.get(clientID) == null) {
                return null;
            }
            List<IData> output = new ArrayList<>();
            List<IData> buffer = unsortedDataBuffer.get(clientID);
            output.addAll(buffer);
            buffer.clear();
            return output;
        }
    }
    
    public List<IStep> collectSteps(int clientID) {
        synchronized(LOCK_STEPS) {
            List<IStep> output = new ArrayList<>();
            List<IStep> buffer = stepsBuffer.get(clientID);
            output.addAll(buffer);
            buffer.clear();
            return output;
        }
    }
}
