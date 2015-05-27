/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Connection;

import ServerApp.ServerMain;
import Shared.Connection.Transaction.ClientBoundTransaction;
import Shared.Connection.Transaction.ConnState;
import Shared.Connection.Transaction.ServerBoundTransaction;
import Shared.Connection.SerializeUtils;
import Shared.Connection.Transaction.ConnCommand;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.INewsItem;
import Shared.Data.ISortedData;
import Shared.Tag;
import Shared.Tasks.IPlan;
import Shared.Tasks.IStep;
import Shared.Tasks.ITask;
import Shared.Tasks.TaskStatus;
import Shared.Users.UserRole;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ConnectionWorker implements Runnable {

    private static final List queue = new LinkedList();
    private static final String SORTEDLOCK = "",
            UNSORTEDLOCK = "",
            TASKSLOCK = "";

    /**
     * data arrived over given socket, and is put in the queue to be handled by
     * a worker thread. The queue is static, and might be emptied by any of the
     * fixed number of worker threads.
     *
     * @param server
     * @param socket
     * @param data
     * @param count
     */
    public static void processData(ConnectionHandler server, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized (queue) {
            queue.add(new ServerDataEvent(server, socket, dataCopy));
            queue.notify();
        }
    }

    /**
     * Reads byte arrays from the queue, turns them into
     * ServerBoundTransactions, and handles the transaction. <br>
     * Every incoming transaction is answered with a ClientBoundTransaction
     * containing the results of the command. <br> <br>
     * Where relevant, methods call the PushHandler.
     */
    @Override
    public void run() {
        ServerDataEvent dataEvent;
        ServerBoundTransaction incoming;
        ClientBoundTransaction outgoing;

        while (true) {
            // Wait for data to become available
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                    }
                }
                dataEvent = (ServerDataEvent) queue.remove(0);
                incoming = (ServerBoundTransaction) SerializeUtils.deserialize(dataEvent.data);
                outgoing = null;
            }

            System.out.println(incoming.command.toString()); // debugging

            switch (incoming.command) {
                case SORTED_GET:
                    outgoing = this.getSortedData(incoming);
                    break;
                case SORTED_SEND:
                    outgoing = this.saveSortedData(incoming);
                    break;
                case UNSORTED_GET:
                    outgoing = this.sendUnsortedData(incoming);
                    break;
                case UNSORTED_SEND:
                    outgoing = this.saveUnsortedData(incoming);
                    break;
                case UNSORTED_STATUS_RESET:
                    outgoing = this.resetUnsortedData(incoming, dataEvent);
                    break;
                case UNSORTED_UPDATE_SEND:
                    outgoing = this.updateUnsortedData(incoming);
                    break;
                case UNSORTED_DISCARD:
                    outgoing = this.discardUnsortedData(incoming);
                    break;
                case UPDATE_REQUEST_SEND:
                    outgoing = this.saveDataRequest(incoming);
                    break;
                case UPDATE_REQUEST_GET:
                    outgoing = this.sendDataRequests(incoming);
                    break;
                case UNSORTED_GET_ID:
                    outgoing = this.sendDataItem(incoming);
                    break;
                case UNSORTED_GET_SOURCE:
                    outgoing = this.sendSentData(incoming);
                    break;
                case TASK_SEND:
                    outgoing = this.sendTask(incoming);
                    break;
                case PLAN_SEND_NEW:
                    outgoing = this.saveNewPlan(incoming);
                    break;
                case PLAN_APPLY:
                    outgoing = this.applyPlan(incoming);
                    break;
                case USERS_SIGN_IN:
                    outgoing = this.getSigninUser(incoming);
                    break;
                case TASK_UPDATE:
                    outgoing = this.updateTask(incoming);
                    break;
                case TASKS_GET:
                    outgoing = this.getTasks(incoming);
                    break;
                case PLAN_SEARCH:
                    outgoing = this.searchPlans(incoming);
                    break;
                case USERS_GET_SERVICEUSERS:
                    outgoing = this.getServiceUsers(incoming);
                    break;
                case NEWSITEM_SEND:
                    outgoing = this.saveNewsItem(incoming);
                    break;
                case NEWSITEM_UPDATE:
                    outgoing = this.updateNewsItem(incoming);
                    break;
                case NEWSITEMS_GET:
                    outgoing = this.getNewsItems(incoming);
                    break;
                case SITUATIONS_GET:
                    outgoing = this.getSituations(incoming);
                    break;
                case USERS_REGISTER:
                    outgoing = this.registerUser(incoming, dataEvent);
                    break;
                case USERS_UNSORTED_SUBSCRIBE:
                    outgoing = this.subscribeUnsorted(incoming, dataEvent);
                    break;
                case USERS_UNSORTED_UNSUBSCRIBE:
                    outgoing = this.unsubscribeUnsorted(incoming, dataEvent);
                    break;
                default:
                    outgoing = new ClientBoundTransaction(incoming);
                    outgoing.result = ConnState.COMMAND_UNKNOWN;
                    break;
            }

            // Return output to sender
            dataEvent.data = SerializeUtils.serialize(outgoing);
            dataEvent.server.send(dataEvent.socket, dataEvent.data);
        }
    }

    /**
     * Sends a batch of 50 items of unsorted data
     */
    private ClientBoundTransaction sendUnsortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        synchronized (UNSORTEDLOCK) {
            return output.setResult(
                    ServerMain.unsortedDatabaseManager.getFromUnsortedData());
        }
    }

    /**
     * Saves given ISortedData to database.
     *
     * @param data
     */
    private ClientBoundTransaction saveSortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            ISortedData data = (ISortedData) input.objects[0];
            boolean result;
            synchronized (SORTEDLOCK) {
                result = ServerMain.sortedDatabaseManager.insertToSortedData(data);
            }
            if (result) {
                ServerMain.pushHandler.push(data);
            }
            return output.setResult(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Saves given IData to database.
     *
     */
    private ClientBoundTransaction saveUnsortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IData data = (IData) input.objects[0];
            synchronized (UNSORTEDLOCK) {
                data = ServerMain.unsortedDatabaseManager.insertToUnsortedData(data);
                // pushes getFromUnsorted to set status as it should
                // resets if it couldn't push
                List<IData> newData = ServerMain.unsortedDatabaseManager.getFromUnsortedData();
                if (!ServerMain.pushHandler.push(newData, null)) {
                    ServerMain.unsortedDatabaseManager.resetUnsortedData(newData);
                }
            }
            return output.setResult(data != null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Notifies Database that a list of data is no longer being worked on.
     */
    private ClientBoundTransaction resetUnsortedData(
            ServerBoundTransaction input, ServerDataEvent dataEvent) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            List<IData> inputList = (List) input.objects[0];
            // first checks if can push to other HQ
            if (ServerMain.pushHandler.push(inputList, dataEvent.socket.socket())) {
                return output.setResult(true);
            }
            // if not: resets
            synchronized (UNSORTEDLOCK) {
                return output.setResult(
                        ServerMain.unsortedDatabaseManager.resetUnsortedData(inputList));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Updates piece of unsorted data with given id.
     */
    private ClientBoundTransaction updateUnsortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IData inObject = (IData) input.objects[0];
            synchronized (UNSORTEDLOCK) {
                return output.setResult(
                        ServerMain.unsortedDatabaseManager.updateUnsortedData(inObject));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Tells database to mark given piece of IData as discarded.
     */
    private ClientBoundTransaction discardUnsortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IData inObject = (IData) input.objects[0];
            synchronized (UNSORTEDLOCK) {
                return output.setResult(
                        ServerMain.unsortedDatabaseManager.discardUnsortedData(inObject));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Files a request for an update to given piece of info.
     */
    private ClientBoundTransaction saveDataRequest(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IDataRequest data = (IDataRequest) input.objects[0];
            boolean result;
            synchronized (SORTEDLOCK) {
                result = ServerMain.sortedDatabaseManager.insertDataRequest(data);
            }
            if (result) {
                ServerMain.pushHandler.push(data);
            }
            return output.setResult(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Provides all datarequests conforming to all given tags.
     */
    private ClientBoundTransaction sendDataRequests(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            HashSet<Tag> tags = (HashSet) input.objects[0];
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.getUpdateRequests(tags));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * returns IData with given ID
     */
    private ClientBoundTransaction sendDataItem(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            int inObject = (int) input.objects[0];
            synchronized (UNSORTEDLOCK) {
                return output.setResult(
                        ServerMain.unsortedDatabaseManager.getDataItem((int) inObject));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Returns a list of IData with given source
     */
    private ClientBoundTransaction sendSentData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            String source = (String) input.objects[0];
            synchronized (UNSORTEDLOCK) {
                return output.setResult(
                        ServerMain.unsortedDatabaseManager.getSentData(source));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Saves a task and sends it to the executor
     */
    private ClientBoundTransaction sendTask(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            ITask task = (ITask) input.objects[0];
            ITask insertedTask;
            synchronized (TASKSLOCK) {
                insertedTask = ServerMain.tasksDatabaseManager.insertNewTask(task);
            }
            if (insertedTask != null) {
                ServerMain.pushHandler.push(insertedTask);
            }
            return output.setResult(insertedTask);
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Saves a new plan
     */
    private ClientBoundTransaction saveNewPlan(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IPlan plan = (IPlan) input.objects[0];
            synchronized (TASKSLOCK) {
                return output.setResult(
                        ServerMain.tasksDatabaseManager.insertNewPlan(plan));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Applies a plan, save to database and send its steps to the executors
     */
    private ClientBoundTransaction applyPlan(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            IPlan plan = (IPlan) input.objects[0];
            if (plan != null) {
                ServerMain.planExecutorHandler.addPlanExecutor(plan);
            }
            return output.setResult(plan != null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Files a request for an update to given piece of info.
     */
    private ClientBoundTransaction getSigninUser(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            String username = (String) input.objects[0];
            String password = (String) input.objects[1];
            synchronized (TASKSLOCK) {
                return output.setResult(
                        ServerMain.tasksDatabaseManager.loginUser(username, password));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Get tasks from database
     */
    private ClientBoundTransaction getTasks(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            String username = (String) input.objects[0];
            HashSet<TaskStatus> statuses = (HashSet<TaskStatus>) input.objects[1];
            System.out.println("getTasks filter contains: " + statuses.size());
            synchronized (TASKSLOCK) {
                return output.setResult(
                        ServerMain.tasksDatabaseManager.getTasks(username, statuses));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Updates the task in the database Adds the updated task to the buffer for
     * the HQChief Handles the task if it is a step
     */
    private ClientBoundTransaction updateTask(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            ITask task = (ITask) input.objects[0];
            boolean success = false;

            synchronized (TASKSLOCK) {
                success = ServerMain.tasksDatabaseManager.updateTask(task);
                task = ServerMain.tasksDatabaseManager.getTask(task.getId());
            }
            if (success) {
                ServerMain.pushHandler.push(task);
                ServerMain.pushHandler.push(task.getSortedData());
            }

            if ((task.getStatus() == TaskStatus.SUCCEEDED
                    || task.getStatus() == TaskStatus.FAILED)
                    && task instanceof IStep) {
                // Execute next step of plan
                ServerMain.planExecutorHandler.executeNextStepOf((IStep) task);
            }
            return output.setResult(success);
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Get plans with keywords from database
     */
    private ClientBoundTransaction searchPlans(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            HashSet<String> keywords = (HashSet) input.objects[0];
            synchronized (TASKSLOCK) {
                return output.setResult(
                        ServerMain.tasksDatabaseManager.getTemplatePlans(keywords));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Get all sorted data from database
     */
    private ClientBoundTransaction getSortedData(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            HashSet<Tag> tags = (HashSet) input.objects[0];
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.getFromSortedData(tags));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Get all serviceusers from database
     */
    private ClientBoundTransaction getServiceUsers(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            synchronized (TASKSLOCK) {
                return output.setResult(
                        ServerMain.tasksDatabaseManager.getServiceUsers());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Send the NewsItem to the database
     */
    private ClientBoundTransaction saveNewsItem(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            INewsItem item = (INewsItem) input.objects[0];
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.insertNewsItem(item));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Updates the NewsItem in the database.
     */
    private ClientBoundTransaction updateNewsItem(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            INewsItem item = (INewsItem) input.objects[0];
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.updateNewsItem(item));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }
    
    private ClientBoundTransaction getNewsItems(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.getNewsItems());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Get all situations from database
     */
    private ClientBoundTransaction getSituations(ServerBoundTransaction input) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            synchronized (SORTEDLOCK) {
                return output.setResult(
                        ServerMain.sortedDatabaseManager.getSituations());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Subscribes Channel for updates for given user role.
     *
     * @param input
     * @param event
     * @return
     */
    private ClientBoundTransaction registerUser(
            ServerBoundTransaction input, ServerDataEvent event) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            UserRole role = (UserRole) input.objects[0];
            Tag tag = (Tag) input.objects[1];
            String username = (String) input.objects[2];

            boolean result = ServerMain.pushHandler.subscribe(
                    role, tag, username, event.socket);
            return output.setResult(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Subscribes Channel to updates on unsorted data.
     *
     * @param input
     * @param dataEvent
     * @return
     */
    private ClientBoundTransaction subscribeUnsorted(
            ServerBoundTransaction input, ServerDataEvent dataEvent) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            return output.setResult(
                    ServerMain.pushHandler.subscribeUnsorted(dataEvent.socket));
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }

    /**
     * Unsubscribes given channel to updates on unsorted data.
     *
     * @param input
     * @param dataEvent
     * @return
     */
    private ClientBoundTransaction unsubscribeUnsorted(
            ServerBoundTransaction input, ServerDataEvent dataEvent) {
        ClientBoundTransaction output = new ClientBoundTransaction(input);
        try {
            return output.setResult(
                    ServerMain.pushHandler.unsubscribeUnsorted(dataEvent.socket));
        } catch (Exception ex) {
            ex.printStackTrace();
            return output.setError();
        }
    }
}
