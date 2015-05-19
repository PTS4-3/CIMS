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
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import Shared.Connection.SerializeUtils;
import Shared.Connection.Transaction.ConnCommand;
import Shared.Data.IData;
import Shared.Data.ISortedData;
import Shared.Data.Situation;
import Shared.NetworkException;
import Shared.Tasks.IPlan;
import Shared.Tasks.ITask;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import java.util.List;
import java.util.Set;

class ResponseHandler implements IResponseHandler {

    private HeadquartersController hqController = null;
    private HeadquartersLogInController loginController = null;
    private ConnectionHandler connectionHandler = null;

    protected ResponseHandler(ConnectionHandler connHandler) {
        this.connectionHandler = connHandler;
    }

    protected void setLoginController(HeadquartersLogInController loginController) {
        this.loginController = loginController;
    }

    protected void setHQController(HeadquartersController hqController) {
        this.hqController = hqController;
    }

    private ConcurrentLinkedQueue<byte[]> responses = new ConcurrentLinkedQueue<>();

    @Override
    public synchronized boolean handleResponse(byte[] rsp) {
        this.responses.add(rsp);
        this.notify();
        return true;
    }

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
                        case SITUATIONS_GET:
                            this.handleSituationsResult(transaction);
                            break;
                        case USERS_UNSORTED_SUBSCRIBE:
                        case USERS_UNSORTED_UNSUBSCRIBE:
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

    private void handleGenericResult(ClientBoundTransaction transaction) {
        System.err.println("Command result received for "
                + transaction.command.toString()
                + ": "
                + transaction.result.toString());
    }

    private void handleLoginResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
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

}
