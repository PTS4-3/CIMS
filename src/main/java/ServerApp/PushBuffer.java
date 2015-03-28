/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Kargathia
 */
public class PushBuffer {

    private final Object
            LOCK_SORTED = "",
            LOCK_REQUESTS = "",
            LOCK_UNSORTED = "";

    // key: ClientID, Value: sortedData
    private HashMap<Integer, List<ISortedData>> sortedDataBuffer;
    // key: ClientID, Value: requests
    private HashMap<Integer, List<IDataRequest>> requestBuffer;
    // key: ClientID, Value: sentData
    private HashMap<Integer, List<IData>> unsortedDataBuffer;

    public PushBuffer() {
        sortedDataBuffer = new HashMap<>();
        requestBuffer = new HashMap<>();
        unsortedDataBuffer = new HashMap<>();
    }

    public void subscribeSorted(int clientID) {
        synchronized (LOCK_SORTED) {
            sortedDataBuffer.put(clientID, new ArrayList<>());
        }
    }

    public void subscribeRequests(int clientID) {
        synchronized (LOCK_REQUESTS) {
            requestBuffer.put(clientID, new ArrayList<>());
        }
    }

    public void subscribeUnsorted(int clientID){
        synchronized(LOCK_UNSORTED){
            unsortedDataBuffer.put(clientID, new ArrayList<>());
        }
    }

    public void unsubscribeSorted(int clientID) {
        synchronized (LOCK_SORTED) {
            sortedDataBuffer.remove(clientID);
        }
    }

    public void unsubscribeRequests(int clientID) {
        synchronized (LOCK_REQUESTS) {
            requestBuffer.remove(clientID);
        }
    }

    public void unsubscribeUnsorted(int clientID){
        synchronized(LOCK_UNSORTED){
            unsortedDataBuffer.remove(clientID);
        }
    }

    public void addSorted(ISortedData data) {
        synchronized (LOCK_SORTED) {
            for (int client : sortedDataBuffer.keySet()) {
                sortedDataBuffer.get(client).add(data);
            }
        }
    }

    public void addRequest(IDataRequest request) {
        synchronized (LOCK_REQUESTS) {
            for (int client : requestBuffer.keySet()) {
                requestBuffer.get(client).add(request);
            }
        }
    }

    public void addUnsorted(IData data){
        synchronized(LOCK_UNSORTED){
            for(int client : unsortedDataBuffer.keySet()){
                unsortedDataBuffer.get(client).add(data);
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

}
