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
import Shared.Tasks.ITask;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Kargathia + Alexander
 */
public class PushBuffer {

    private static final String HQChief = "HQChief";

    private final Object LOCK_SORTED = "",
            LOCK_REQUESTS = "",
            LOCK_UNSORTED = "",
            LOCK_TASKS = "";

    // key: ClientID, Value: sortedData
    private HashMap<Integer, List<ISortedData>> sortedDataBuffer;
    // key: ClientID, Value: requests
    private HashMap<Integer, List<IDataRequest>> requestBuffer;
    // key: ClientID, Value: sentData
    private HashMap<Integer, List<IData>> sentDataBuffer;
    // key: ClientID, Value: steps
    private HashMap<Integer, List<ITask>> tasksBuffer;

    // key: username, Value: ClientIDs
    private HashMap<String, HashSet<Integer>> clientIDs;

    public PushBuffer() {
        sortedDataBuffer = new HashMap<>();
        requestBuffer = new HashMap<>();
        sentDataBuffer = new HashMap<>();
        this.tasksBuffer = new HashMap<>();
        this.clientIDs = new HashMap<>();
    }

    /**
     * Adds the given clientID to the given username in clientIDs
     *
     * @param username cannot be null or empty
     * @param clientID has to be zero or greater
     */
    private synchronized void addClientID(String username, int clientID) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("username cannot be null or empty");
        }
        if (clientID < 0) {
            throw new IllegalArgumentException("clientID has to be zero or greater");
        }
        if (this.clientIDs.get(username) == null) {
            this.clientIDs.put(username, new HashSet<>());
        }
        this.clientIDs.get(username).add(clientID);
    }

    /**
     * Removes the given clientID from the given username in clientIDs
     *
     * @param username cannot be null or empty
     * @param clientID has to be zero or greater
     */
    private synchronized void removeClientID(String username, int clientID) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("username cannot be null or empty");
        }
        if (clientID < 0) {
            throw new IllegalArgumentException("clientID has to be zero or greater");
        }
        if (this.sortedDataBuffer.get(clientID) == null
                && this.sentDataBuffer.get(clientID) == null
                && this.requestBuffer.get(clientID) == null) {
            this.clientIDs.get(username).remove(clientID);

            if (this.clientIDs.get(username).isEmpty()) {
                this.clientIDs.remove(username);
            }
        }
    }

    /**
     * Subscribe to get updates for sorted data
     *
     * @param username
     * @param clientID
     */
    public void subscribeSorted(String username, int clientID) {
        this.addClientID(username, clientID);
        synchronized (LOCK_SORTED) {
            sortedDataBuffer.put(clientID, new ArrayList<>());
        }
    }

    /**
     * Subscribe to get updates for DataRequests
     *
     * @param username
     * @param clientID
     */
    public void subscribeRequests(String username, int clientID) {
        this.addClientID(username, clientID);
        synchronized (LOCK_REQUESTS) {
            requestBuffer.put(clientID, new ArrayList<>());
        }
    }

    /**
     * Subscribe to get updates for SentData
     *
     * @param username
     * @param clientID
     */
    public void subscribeSent(String username, int clientID) {
        this.addClientID(username, clientID);
        synchronized (LOCK_UNSORTED) {
            sentDataBuffer.put(clientID, new ArrayList<>());
        }
    }

    /**
     * Subscribe to get updates for Tasks
     *
     * @param username
     * @param clientID
     */
    public void subscribeTasks(String username, int clientID) {
        this.addClientID(username, clientID);
        synchronized (LOCK_TASKS) {
            tasksBuffer.put(clientID, new ArrayList<>());
        }
    }

    /**
     * Unsubscribe to get updates for SortedData
     *
     * @param username
     * @param clientID
     */
    public void unsubscribeSorted(String username, int clientID) {
        synchronized (LOCK_SORTED) {
            sortedDataBuffer.remove(clientID);
        }
        this.removeClientID(username, clientID);
    }

    /**
     * Unsubscribe to get updates for DataRequests
     *
     * @param username
     * @param clientID
     */
    public void unsubscribeRequests(String username, int clientID) {
        synchronized (LOCK_REQUESTS) {
            requestBuffer.remove(clientID);
        }
        this.removeClientID(username, clientID);
    }

    /**
     * Unsubscribe to get updates for SentData
     *
     * @param username
     * @param clientID
     */
    public void unsubscribeSent(String username, int clientID) {
        synchronized (LOCK_UNSORTED) {
            sentDataBuffer.remove(clientID);
        }
        this.removeClientID(username, clientID);
    }

    /**
     * Unsubscribe to get updates for Tasks
     *
     * @param username
     * @param clientID
     */
    public void unsubscribeTasks(String username, int clientID) {
        synchronized (LOCK_TASKS) {
            tasksBuffer.remove(clientID);
        }
        this.removeClientID(username, clientID);
    }

    /**
     * Add the given sortedData to the buffer for all subscribed clients
     *
     * @param data
     */
    public void addSorted(ISortedData data) {
        synchronized (LOCK_SORTED) {
            for (String username : clientIDs.keySet()) {
                boolean isTargetUser = false;

                if (username.equals(HQChief)) {
                    isTargetUser = true;
                } else {
                    // Determine if user has same tag
                    IUser user = ServerMain.tasksDatabaseManager.getUser(username);

                    if (user instanceof IServiceUser) {
                        IServiceUser serviceUser = (IServiceUser) user;
                        // ??
                        if (data.getTags().contains(serviceUser.getType())) {
                            isTargetUser = true;
                        }
                    }
                }

                if (isTargetUser) {
                    HashSet<Integer> clients = this.clientIDs.get(username);
                    if (clients != null) {
                        for (int clientId : clients) {
                            sortedDataBuffer.get(clientId).add(data);
                        }
                    }
                }
            }
        }
    }

    /**
     * Add the given request to the buffer for all subscribed clients
     *
     * @param request
     */
    public void addRequest(IDataRequest request) {
        synchronized (LOCK_REQUESTS) {
            for (String username : this.clientIDs.keySet()) {
                // Determine if user has same tag
                IUser user = ServerMain.tasksDatabaseManager.getUser(username);

                if (user instanceof IServiceUser) {
                    IServiceUser serviceUser = (IServiceUser) user;
                    // ??
                    if (request.getTags().contains(serviceUser.getType())) {
                        HashSet<Integer> clients = this.clientIDs.get(username);
                        if (clients != null) {
                            for (int clientId : clients) {
                                requestBuffer.get(clientId).add(request);
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * Adds the given sent data to the buffer for all subscribed clients
     *
     * @param data
     */
    public void addSentData(IData data) {
        synchronized (LOCK_UNSORTED) {
            for (String username : clientIDs.keySet()) {
                if (username.equals(data.getSource())) {
                    HashSet<Integer> clients = this.clientIDs.get(username);
                        if (clients != null) {
                            for (int clientId : clients) {
                                sentDataBuffer.get(clientId).add(data);
                            }
                        }
                }
            }
        }
    }

    /**
     * Adds the given task to the buffer of the executor of the given task
     *
     * @param task
     */
    public void addTask(ITask task) {
        synchronized (LOCK_TASKS) {
            HashSet<Integer> clients = clientIDs.get(task.getExecutor().getUsername());
            if (clients != null) {
                for (int client : clients) {
                    tasksBuffer.get(client).add(task);
                }
            }
        }
    }

    /**
     * Adds the given task to the buffer of the HQChief
     *
     * @param task
     */
    public void addTaskForChief(ITask task) {
        synchronized (LOCK_TASKS) {
            HashSet<Integer> chiefClientIDs = clientIDs.get(HQChief);
            if (chiefClientIDs != null) {
                for (int client : chiefClientIDs) {
                    tasksBuffer.get(client).add(task);
                }
            }
        }
    }

    /**
     * Collect the sortedData in the buffer of the given clientID
     *
     * @param clientID
     * @return
     */
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

    /**
     * Collect the dataRequests in the buffer of the given clientID
     *
     * @param clientID
     * @return
     */
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

    /**
     * Collect the unsortedData in the buffer of the given clientID
     *
     * @param clientID
     * @return
     */
    public List<IData> collectUnsorted(int clientID) {
        synchronized (LOCK_UNSORTED) {
            if (sentDataBuffer.get(clientID) == null) {
                return null;
            }
            List<IData> output = new ArrayList<>();
            List<IData> buffer = sentDataBuffer.get(clientID);
            output.addAll(buffer);
            buffer.clear();
            return output;
        }
    }

    /**
     * Collect the tasks in the buffer of the given clientID
     *
     * @param clientID
     * @return
     */
    public List<ITask> collectTasks(int clientID) {
        synchronized (LOCK_TASKS) {
            List<ITask> output = new ArrayList<>();
            List<ITask> buffer = tasksBuffer.get(clientID);
            output.addAll(buffer);
            buffer.clear();
            return output;
        }
    }
}
