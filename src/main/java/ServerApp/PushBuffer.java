/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.DataRequest;
import Shared.SortedData;
import Shared.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Kargathia
 */
public class PushBuffer {

    // key: ClientID, Value: sortedData
    private HashMap<Integer, List<SortedData>> sortedDataBuffer;
    // key: ClientID, Value: requests
    private HashMap<Integer, List<DataRequest>> requestBuffer;

    public PushBuffer(){
        sortedDataBuffer = new HashMap<>();
        requestBuffer = new HashMap<>();
    }
    
    public synchronized void subscribeSorted(int clientID){
        sortedDataBuffer.put(clientID, new ArrayList<>());
    }

    public synchronized void subscribeRequests(int clientID){
        requestBuffer.put(clientID, new ArrayList<>());
    }
    
    public synchronized void unsubscribeSorted(int clientID){
        sortedDataBuffer.remove(clientID);
    }

    public synchronized void unsubscribeRequests(int clientID){
        requestBuffer.remove(clientID);
    }
    
    public synchronized void addSorted(SortedData data){
        for(int client : sortedDataBuffer.keySet()){
            sortedDataBuffer.get(client).add(data);
        }
    }

    public synchronized void addRequest(DataRequest request){
        for(int client : requestBuffer.keySet()){
            requestBuffer.get(client).add(request);
        }
    }
    
    public synchronized List<SortedData> collectSorted(int clientID){
        if(sortedDataBuffer.get(clientID) == null){
            return null;
        }
        List<SortedData> output = new ArrayList<>();
        List<SortedData> buffer = sortedDataBuffer.get(clientID);
        output.addAll(buffer);
        buffer.clear();
        return output;
    }
    
    public synchronized List<DataRequest> collectRequests(int clientID){
        if(requestBuffer.get(clientID) == null){
            return null;
        }
        List<DataRequest> output = new ArrayList<>();
        List<DataRequest> buffer = requestBuffer.get(clientID);
        output.addAll(buffer);
        buffer.clear();
        return output;
    }



}
