/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.Connection;

import ServicesApp.UI.ServicesController;
import ServicesApp.UI.ServicesLogInController;
import Shared.Connection.Transaction.ClientBoundTransaction;
import Shared.Connection.Transaction.ConnState;
import Shared.Connection.IResponseHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import Shared.Connection.SerializeUtils;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.NetworkException;
import Shared.Tasks.ITask;
import Shared.Users.IUser;
import java.util.List;

class ResponseHandler implements IResponseHandler {

    private ServicesController servicesController = null;
    private ServicesLogInController loginController = null;

    protected void setLoginController(ServicesLogInController loginController) {
        this.loginController = loginController;
    }

    protected void setServicesController(ServicesController hqController) {
        this.servicesController = hqController;
    }

    private final ConcurrentLinkedQueue<byte[]> responses = new ConcurrentLinkedQueue<>();

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

                    System.out.println(transaction.command.toString() // debugging println
                            + ": "
                            + transaction.result.toString());

                    switch (transaction.command) {
                        default:
                            throw new NetworkException("(Unknown Command) - "
                                    + transaction.command.toString());
                        case USERS_REGISTER:
                            handleRegisterResult(transaction);
                            break;
                        case SORTED_SEND:
                            this.handleGenericResult(transaction);
                            break;
                        case SORTED_GET:
                            this.handleSortedResponse(transaction);
                            break;
                        case UNSORTED_GET_ID:
                            this.handleDataItemResult(transaction);
                            break;
                        case UNSORTED_GET_SOURCE:
                            this.handleSentDataResult(transaction);
                            break;
                        case TASKS_GET:
                            this.handleTasksResult(transaction);
                            break;
                        case USERS_SIGN_IN:
                            this.handleLoginResult(transaction);
                            break;
                        case UPDATE_REQUEST_GET:
                            this.handleRequestsResult(transaction);
                            break;
                        case UNSORTED_SEND:
                        case UNSORTED_GET:
                        case USERS_UNSORTED_SUBSCRIBE:
                        case USERS_UNSORTED_UNSUBSCRIBE:
                        case PLAN_SEARCH:
                        case USERS_GET_SERVICEUSERS:
                        case SITUATIONS_GET:
                        case UNSORTED_STATUS_RESET:
                        case UNSORTED_UPDATE_SEND:
                        case UNSORTED_DISCARD:
                        case UPDATE_REQUEST_SEND:
                        case TASK_SEND:
                        case PLAN_SEND_NEW:
                        case PLAN_APPLY:
                        case TASK_UPDATE:
                        case NEWSITEM_SEND:
                        case NEWSITEM_UPDATE:
                            this.handleGenericResult(transaction);
                            break;
                    }

                } catch (NetworkException nEx) {
                    System.err.println(nEx.getMessage());
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

    private void handleSortedResponse(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                List<ISortedData> data = (List) transaction.data;
                this.servicesController.displaySortedData(data);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleTasksResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                List<ITask> tasks = (List) transaction.data;
                this.servicesController.displayTasks(tasks);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleDataItemResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                IData data = (IData) transaction.data;
                this.servicesController.displayDataItem(data);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleRequestsResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                List<IDataRequest> requests = (List) transaction.data;
                this.servicesController.displayRequests(requests);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSentDataResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {
                List<IData> data = (List) transaction.data;
                this.servicesController.displaySentData(data);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleRegisterResult(ClientBoundTransaction transaction) {
        try {
            if (transaction.result == ConnState.COMMAND_SUCCESS) {

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
