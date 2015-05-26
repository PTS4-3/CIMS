/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.Connection.SerializeUtils;
import Shared.Connection.Transaction.ClientBoundTransaction;
import Shared.Connection.Transaction.ConnCommand;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.Data.SortedData;
import Shared.Tag;
import Shared.Tasks.ITask;
import Shared.Users.UserRole;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Kargathia
 */
public class PushHandler {

    // used for tasks
    private final HashSet<Socket> chiefConnections;
    // all HQ users, including the chief - can be toggled mid-session
    private final HashSet<Socket> unsortedSubscribers;
    // key: username (ServiceUser)
    private final HashMap<String, Set<Socket>> taskSubscribers;
    // service users subscribing by tag - used for sorted data + requests
    private final HashMap<Tag, Set<Socket>> serviceSubscribers;

    private final Set<Socket> faultySockets;

    /**
     * Initiates collections
     */
    public PushHandler() {
        this.faultySockets = Collections.newSetFromMap(new ConcurrentHashMap<>());
        chiefConnections = new HashSet<>();
        unsortedSubscribers = new HashSet<>();
        taskSubscribers = new HashMap<>();
        serviceSubscribers = new HashMap<>();
        for (Tag tag : Tag.values()) {
            serviceSubscribers.put(tag, new HashSet<>());
        }
    }

    /**
     * Checks if there are unsorted subscribers to whom can be pushed.
     *
     * @param exclude is disregarded to avoid pushing data back to a client
     * mid-shutdown.
     * @return
     */
    public boolean canPushUnsorted(SocketChannel exclude) {
        synchronized (unsortedSubscribers) {
            if (exclude == null) {
                return !unsortedSubscribers.isEmpty();
            } else {
                return (unsortedSubscribers.size() > 1
                        || unsortedSubscribers.iterator().next() != exclude.socket());
            }
        }
    }

    /**
     * Checks whether socket is open before bothering it.
     *
     * @param socket
     * @return
     */
    private boolean trySend(Socket socket, byte[] data) {
        if (socket.isClosed()
                || !socket.isConnected()
                || socket.isInputShutdown()
                || socket.isOutputShutdown()) {
            return false;
        }
        ServerMain.connectionHandler.send(socket.getChannel(), data);
        return true;
    }

    /**
     *
     * @param role can't be null
     * @param tag can be null if role != SERVICE
     * @param username can be null if role != SERVICE
     * @param channel
     * @return
     */
    public boolean subscribe(UserRole role, Tag tag, String username, SocketChannel channel) {
        if (role == null) {
            System.out.println("invalid registration - null UserRole");
            return false;
        }
        if (role == UserRole.SERVICE
                && (tag == null || username == null || username.isEmpty())) {
            System.out.println("invalid registration - null tag or username");
            return false;
        }

        if (role == UserRole.CHIEF) {
            synchronized (chiefConnections) {
                return chiefConnections.add(channel.socket());
            }
        } else if (role == UserRole.HQ) {
            // HQ users are not subscribed to anything (unsorted is a separate subscription)
            return true;
        } else if (role == UserRole.SERVICE) {
            boolean tasksResult, serviceSubsResult;
            synchronized (taskSubscribers) {
                // tasks
                taskSubscribers.putIfAbsent(username, new HashSet<>());
                tasksResult = taskSubscribers.get(username).add(channel.socket());
            }
            synchronized (serviceSubscribers) {
                // requests, sorted data
                serviceSubsResult = serviceSubscribers.get(tag).add(channel.socket());
            }
            return (tasksResult && serviceSubsResult);
        }
        // shouldn't be hit
        return false;
    }

    private void cleanFaultySockets() {
        synchronized (this.faultySockets) {
            for (Socket socket : this.faultySockets) {
                this.unsubscribe(socket);
            }
            this.faultySockets.clear();
        }
    }

    /**
     * Removes given sockets from all lists of subscribers. <br>
     * Not specified because it's also used on lapsed connections where client
     * didn't provide his role/tag/username
     *
     * @param socket
     */
    public void unsubscribe(Socket socket) {
        System.out.println("unsubscribing socket"); // debugging
        synchronized (chiefConnections) {
            chiefConnections.remove(socket);
        }
        synchronized (unsortedSubscribers) {
            unsortedSubscribers.remove(socket);
        }
        synchronized (taskSubscribers) {
            for (String username : taskSubscribers.keySet()) {
                taskSubscribers.get(username).remove(socket);
            }
        }
        synchronized (serviceSubscribers) {
            for (Tag tag : serviceSubscribers.keySet()) {
                serviceSubscribers.get(tag).remove(socket);
            }
        }
    }

    /**
     *
     * @param channel
     * @return
     */
    public boolean subscribeUnsorted(SocketChannel channel) {
        synchronized (unsortedSubscribers) {
            return unsortedSubscribers.add(channel.socket());
        }
    }

    /**
     *
     * @param channel
     * @return
     */
    public boolean unsubscribeUnsorted(SocketChannel channel) {
        synchronized (unsortedSubscribers) {
            return unsortedSubscribers.remove(channel.socket());
        }
    }

    /**
     *
     * @param task
     */
    public void push(ITask task) {
        if(task == null){
            return;
        }
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.TASKS_PUSH, task);
        byte[] output = SerializeUtils.serialize(transaction);
        // to chief
        synchronized (chiefConnections) {
            for (Socket socket : chiefConnections) {
                System.out.println("pushing task to chief"); // debugging
                if (!this.trySend(socket, output)) {
                    this.faultySockets.add(socket);
                }
            }
        }
        // to relevant serviceuser
        if (task.getExecutor() != null) {
            String userName = task.getExecutor().getUsername();
            synchronized (taskSubscribers) {
                if (taskSubscribers.containsKey(userName)) {
                    for (Socket socket : taskSubscribers.get(userName)) {
                        System.out.println("pushing task"); // debugging
                        if (!this.trySend(socket, output)) {
                            this.faultySockets.add(socket);
                        }
                    }
                }
            }
        }
        this.cleanFaultySockets();
    }

    /**
     *
     * @param data
     */
    public boolean push(List<IData> data, Socket disregard) {
        if(data == null || data.isEmpty()){
            return false;
        }
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.UNSORTED_GET, data);
        byte[] output = SerializeUtils.serialize(transaction);
        // Pushes it towards the first valid subscriber it finds in an iterator
        // HashSet do not guarantee order, so this should happen semi-randomly
        synchronized (unsortedSubscribers) {
            if (!unsortedSubscribers.isEmpty()) {
                for (Socket socket : unsortedSubscribers) {
                    if (socket != disregard) {
                        if (!this.trySend(socket, output)) {
                            this.faultySockets.add(socket);
                        } else {
                            System.out.println("pushing list of unsorted data"); // debugging
                            return true;
                        }
                    }
                }
                this.cleanFaultySockets();
            }
        }
        return false;
    }

    /**
     *
     * @param data
     */
    public void push(ISortedData data) {
        if(data == null){
            return;
        }
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.SORTED_GET,
                        Arrays.asList(new ISortedData[]{data}) );
        byte[] output = SerializeUtils.serialize(transaction);
        // sends to chief
        synchronized (chiefConnections) {
            for (Socket socket : chiefConnections) {
                System.out.println("pushing sorted data to chief"); // debugging
                if (!this.trySend(socket, output)) {
                    this.faultySockets.add(socket);
                }
            }
        }
        // sends to relevant serviceusers
        synchronized (serviceSubscribers) {
            for (Tag target : data.getTags()) {
                for (Socket socket : serviceSubscribers.get(target)) {
                    System.out.println("pushing sorted data"); // debugging
                    if (!this.trySend(socket, output)) {
                        this.faultySockets.add(socket);
                    }
                }
            }
        }
        this.cleanFaultySockets();
    }

    /**
     *
     * @param request
     */
    public void push(IDataRequest request) {
        if(request == null){
            return;
        }
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.UPDATE_REQUEST_GET, request);
        byte[] output = SerializeUtils.serialize(transaction);
        // sends to relevant serviceusers
        synchronized (serviceSubscribers) {
            for (Tag target : request.getTags()) {
                for (Socket socket : serviceSubscribers.get(target)) {
                    System.out.println("pushing datarequest");
                    if (!this.trySend(socket, output)) {
                        this.faultySockets.add(socket);
                    }
                }
            }
        }
        this.cleanFaultySockets();
    }

}
