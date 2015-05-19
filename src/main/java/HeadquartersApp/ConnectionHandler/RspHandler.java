/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.ConnectionHandler;

import HeadquartersApp.UI.HeadquartersController;
import HeadquartersApp.UI.HeadquartersLogInController;
import Shared.Connection.ClientBoundTransaction;
import Shared.Connection.ConnState;
import Shared.Connection.IRspHandler;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import Shared.Connection.SerializeUtils;
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

class RspHandler implements IRspHandler {

    private HeadquartersController hqController = null;
    private HeadquartersLogInController loginController = null;

    protected void setLoginController(HeadquartersLogInController loginController) {
        this.loginController = loginController;
    }

    protected void setHQController(HeadquartersController hqController) {
        this.hqController = hqController;
    }

    private ConcurrentLinkedQueue<byte[]> responses = new ConcurrentLinkedQueue<>();
//    private byte[] rsp = null;

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

                    if (transaction.result == ConnState.COMMAND_ERROR) {
                        throw new NetworkException(transaction.command.toString());
                    }

                    switch (transaction.command) {
                        default:
                            throw new NetworkException("(Unknown Command) - "
                                    + transaction.command.toString());
                        case USERS_REGISTER:
                            handleLoginResult(transaction);
                            break;
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
                        case USERS_GET_SERVICE:
                            this.handleServiceUsersResult(transaction);
                            break;
                        case USERS_SIGN_IN:
                            this.handleLoginResult(transaction);
                            break;
                        case SITUATIONS_GET:
                            this.handleSituationsResult(transaction);
                            break;
                        case UNSORTED_GET_ID:
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
                        case SORTED_GET_ALL:
                        case NEWSITEM_SEND:
                        case NEWSITEM_UPDATE:
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
            IUser user = (IUser) transaction.data;
            this.loginController.logIn(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleUnsortedResult(ClientBoundTransaction transaction) {
        try {
            List<IData> list = (List) transaction.data;
            this.hqController.displayData(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSearchPlansResult(ClientBoundTransaction transaction) {
        try {
            List<IPlan> plans = (List) transaction.data;
            this.hqController.displayPlans(plans);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSortedResponse(ClientBoundTransaction transaction) {
        try {
            List<ISortedData> data = (List) transaction.data;
            this.hqController.displaySortedData(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleServiceUsersResult(ClientBoundTransaction transaction) {
        try {
            List<IServiceUser> users = (List) transaction.data;
            this.hqController.displayServiceUsers(users);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleTasksResult(ClientBoundTransaction transaction) {
        try {
            List<ITask> tasks = (List) transaction.data;
            this.hqController.displayTasks(tasks);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSituationsResult(ClientBoundTransaction transaction) {
        try {
          Set<Situation> situations = (Set) transaction.data;
          this.hqController.displaySituations(situations);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
