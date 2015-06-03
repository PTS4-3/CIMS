/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.Connection;

import HeadquartersApp.UI.HeadquartersController;
import HeadquartersApp.UI.HeadquartersLogInController;
import Shared.Connection.Transaction.ClientBoundTransaction;
import Shared.Connection.Transaction.ConnState;
import Shared.Connection.IResponseHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import Shared.Connection.SerializeUtils;
import Shared.Data.IData;
import Shared.Data.INewsItem;
import Shared.Data.ISortedData;
import Shared.Data.Situation;
import Shared.NetworkException;
import Shared.Tasks.IPlan;
import Shared.Tasks.ITask;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

class ResponseHandler implements IResponseHandler {

    private HeadquartersController hqController = null;
    private HeadquartersLogInController loginController = null;
    private final ConnectionHandler connectionHandler;
    private final ConcurrentLinkedQueue<byte[]> responses;

    protected ResponseHandler(ConnectionHandler connHandler) {
        this.connectionHandler = connHandler;
        this.responses = new ConcurrentLinkedQueue<>();
    }

    protected void setLoginController(HeadquartersLogInController loginController) {
        this.loginController = loginController;
    }

    protected void setHQController(HeadquartersController hqController) {
        this.hqController = hqController;
    }

    /**
     * Loads given byte array into a queue, waiting to be processed by run().
     * This method is called by the thread in charge of run() in
     * ClientConnection.
     *
     * @param rsp
     * @return
     */
    @Override
    public synchronized boolean handleResponse(byte[] rsp) {
        this.responses.add(rsp);
        this.notify();
        return true;
    }

    /**
     * Handles responses in queue on appropriate thread.
     */
    @Override
    public void run() {
        while (true) {
            while (this.responses.isEmpty()) {
                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                }
            }
            while (!this.responses.isEmpty()) {
                byte[] rsp = this.responses.poll();
                try {
                    ClientBoundTransaction transaction
                            = (ClientBoundTransaction) SerializeUtils.deserialize(rsp);
                    // notifies handler of completed query.
                    // pushed transactions have a commandID of -1 and are not relevant
                    if (transaction.ID > -1) {
                        this.connectionHandler.notifyCommandResponse(transaction.ID);
                    }

                    System.out.println(transaction.command.toString() // debugging println
                            + ": "
                            + transaction.result.toString());

                    if (transaction.result == ConnState.COMMAND_ERROR) {
                        throw new NetworkException(transaction.command.toString());
                    }

                    switch (transaction.command) {
                        default:
                            throw new NetworkException("(Unknown Command) - "
                                    + transaction.command.toString());
                        case SORTED_SEND:
                            this.handleGenericResult(transaction);
                            break;
                        case SORTED_GET:
                            this.handleSortedResponse(transaction);
                            break;
                        case UNSORTED_GET:
                            this.handleUnsortedResult(transaction);
                            break;
                        case TASKS_GET:
                            this.handleTasksResult(transaction);
                            break;
                        case PLAN_SEARCH:
                            this.handleSearchPlansResult(transaction);
                            break;
                        case USERS_GET_SERVICEUSERS:
                            this.handleServiceUsersResult(transaction);
                            break;
                        case USERS_SIGN_IN:
                            this.handleLoginResult(transaction);
                            break;
                        case NEWSITEMS_GET:
                            this.handleNewsItemsResult(transaction);
                            break;
                        case SITUATIONS_GET:
                            this.handleSituationsResult(transaction);
                            break;
                        case TASKS_PUSH:
                            this.handleTaskPush(transaction);
                            break;
                        case USERS_UNSORTED_SUBSCRIBE:
                            this.handleSubscribeUnsorted(transaction);
                            break;
                        case USERS_UNSORTED_UNSUBSCRIBE:
                            this.handleUnsubscribeUnsorted(transaction);
                            break;
                        case UNSORTED_GET_ID:
                        case UNSORTED_SEND:
                        case UNSORTED_GET_SOURCE:
                        case UNSORTED_STATUS_RESET:
                        case UNSORTED_UPDATE_SEND:
                        case UNSORTED_DISCARD:
                        case UPDATE_REQUEST_SEND:
                        case UPDATE_REQUEST_GET:
                        case TASK_SEND:
                        case PLAN_SEND_NEW:
                        case PLAN_APPLY:
                        case TASK_UPDATE:
                        case NEWSITEM_SEND:
                        case NEWSITEM_UPDATE:
                        case USERS_REGISTER:
                            this.handleGenericResult(transaction);
                            break;
                    }

                } catch (NetworkException nEx) {
                    System.err.println("Server failure handling command: " + nEx.getMessage());
                } catch (Exception ex) {
                    System.out.println("Error handling file from Server");
                    ex.printStackTrace();
                }
            }

        }
    }

    /**
     * Handles command results that do not need any specific handling (boolean
     * results)
     * // TODO: notify user of these things
     * @param transaction
     */
    private void handleGenericResult(ClientBoundTransaction transaction) {
//        System.err.println("Command result received for "
//                + transaction.command.toString()
//                + ": "
//                + transaction.result.toString());
    }

    private void handleLoginResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result != ConnState.COMMAND_ERROR) {
                IUser user = (IUser) transaction.data;
                this.loginController.logIn(user);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleUnsortedResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                List<IData> list = (List) transaction.data;
                this.hqController.displayData(list);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSearchPlansResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                List<IPlan> plans = (List) transaction.data;
                this.hqController.displayPlans(plans);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSortedResponse(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                List<ISortedData> data = (List) transaction.data;
                this.hqController.displaySortedData(data);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleServiceUsersResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                List<IServiceUser> users = (List) transaction.data;
                this.hqController.displayServiceUsers(users);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleTasksResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                List<ITask> tasks = (List) transaction.data;
                this.hqController.displayTasks(tasks);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void handleNewsItemsResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                List<INewsItem> news = (List) transaction.data;
                this.hqController.displayNewsItems(news);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSituationsResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                Set<Situation> situations = (Set) transaction.data;
                this.hqController.displaySituations(situations);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleTaskPush(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                ITask task = (ITask) transaction.data;
                this.hqController.displayTasks(Arrays.asList(new ITask[]{task}));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSubscribeUnsorted(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_SUCCESS) {
            this.connectionHandler.setSubscribedUnsorted(true);
        }
    }
    
    private void handleUnsubscribeUnsorted(ClientBoundTransaction transaction) {
        if(transaction.result == ConnState.COMMAND_SUCCESS) {
            this.connectionHandler.setSubscribedUnsorted(false);
        }
    }
}
