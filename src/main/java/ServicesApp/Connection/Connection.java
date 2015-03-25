/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.Connection;

import ServicesApp.UI.ServicesController;
import Shared.Connection.ConnClientBase;
import Shared.Connection.ConnCommand;
import Shared.Connection.ConnState;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import Shared.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
class Connection extends ConnClientBase {

    Connection(String IP, int port) {
        super(IP, port);
    }

    /**
     * Sends sorted data to server.
     *
     * @param IP manually provided.
     * @param port manually provided.
     * @param data
     * @return success on attempting to send piece of unsorted data.
     */
    protected void sendUnSortedData(IData data) {
        if (!greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.UNSORTED_SEND);
            out.writeObject(data);
            out.flush();
            getCommandSuccess("Send unsorted data (" + data.getTitle() + ")");
        } catch (IOException ex) {
            System.err.println("Exception trying to send unsorted data to server: "
                    + ex.getMessage());
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
    }

    /**
     * Queries server for sorted data that has all given tags.
     *
     * @param IP manually provided
     * @param port manually provided
     * @param tags
     * @return batch of data. Null on general error.
     */
    protected List<ISortedData> getSortedData(HashSet<Tag> tags) {
        if (!this.greetServer()) {
            return null;
        }
        List<ISortedData> output = null;
        try {
            out.writeObject(ConnCommand.SORTED_GET);
            out.writeObject(tags);
            out.flush();
            Object inObject = in.readObject();
            if (inObject instanceof List) {
                List list = (List) inObject;
                if (list.isEmpty()) {
                    output = new ArrayList<>();
                } else {
                    if (list.get(0) instanceof ISortedData) {
                        output = (List<ISortedData>) list;
                    }
                }
            } else if (inObject instanceof ConnState) {
                if ((ConnState) inObject == ConnState.COMMAND_FAIL) {
                    System.err.println("Server failed to execute command "
                            + "(getSortedData)");
                } else {
                    System.err.println("Unexpected ConnState as output: "
                            + inObject.toString());
                }
            } else {
                throw new IOException("Unexpected object");
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Exception getting sorted data: "
                    + ex.getMessage());
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Gets all data requests conforming to all given tags. Custom IP/port
     *
     * @param tags
     * @param IP
     * @param port
     * @return
     */
    protected List<IDataRequest> getDataRequests(HashSet<Tag> tags) {
        if (!this.greetServer()) {
            return null;
        }
        List<IDataRequest> output = null;
        try {
            out.writeObject(ConnCommand.UPDATE_REQUEST_GET);
            out.writeObject(tags);
            out.flush();
            Object inObject = in.readObject();
            if (inObject instanceof List) {
                List list = (List) inObject;
                if (list.isEmpty()) {
                    output = new ArrayList<>();
                } else {
                    if (list.get(0) instanceof IDataRequest) {
                        output = (List<IDataRequest>) list;
                    }
                }
            } else if (inObject instanceof ConnState) {
                if ((ConnState) inObject == ConnState.COMMAND_FAIL) {
                    System.err.println("Server failed to execute command "
                            + "(getDataRequests)");
                } else {
                    System.err.println("Unexpected ConnState as output: "
                            + inObject.toString());
                }
            } else {
                throw new IOException("Unexpected object");
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Exception getting data requests: "
                    + ex.getMessage());
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Updates data with given id with given IData.
     *
     * @param data
     * @param id
     * @param IP
     * @param port
     * @return
     */
    protected void updateUnsortedData(IData data) {
        if (!this.greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.UNSORTED_UPDATE_SEND);
            out.writeObject(data);
            out.flush();
            getCommandSuccess("Update unsorted data (" + data.getTitle() + ")");
        } catch (IOException ex) {
            System.err.println("Exception updating unsorted data: "
                    + ex.getMessage());
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
    }

    /**
     *
     * @param tags
     * @return unsorted data conforming to parameter (eg. all sent items from
     * this source).
     *
     */
    protected List<IData> getSentData(String source) {
        if (!this.greetServer()) {
            return null;
        }
        List<IData> output = null;
        try {
            out.writeObject(ConnCommand.UNSORTED_GET_SOURCE);
            out.writeObject(source);
            out.flush();
            Object inObject = in.readObject();
            if (inObject instanceof List) {
                List list = (List) inObject;
                if (list.isEmpty()) {
                    output = new ArrayList<>();
                } else {
                    if (list.get(0) instanceof IData) {
                        output = (List<IData>) list;
                    }
                }
            } else if (inObject instanceof ConnState) {
                if ((ConnState) inObject == ConnState.COMMAND_FAIL) {
                    System.err.println("Server failed to execute command "
                            + "(getSentData)");
                } else {
                    System.err.println("Unexpected ConnState as output: "
                            + inObject.toString());
                }
            } else {
                throw new IOException("Unexpected object");
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Exception getting sent data: " + ex.getMessage());
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * @param id
     * @return specific IData based on given ID.
     */
    protected IData getDataItem(int id) {
        if (!this.greetServer()) {
            return null;
        }
        IData output = null;
        try {
            out.writeObject(ConnCommand.UNSORTED_GET_ID);
            out.writeObject(id);
            out.flush();

            Object inObject = in.readObject();
            if (inObject instanceof IData) {
                output = (IData) inObject;
            } else if (inObject instanceof ConnState) {
                if ((ConnState) inObject == ConnState.COMMAND_FAIL) {
                    System.err.println("Server failed to execute command "
                            + "(getDataItem)");
                } else {
                    System.err.println("Unexpected ConnState as output: "
                            + inObject.toString());
                }
            } else {
                throw new IOException("Unexpected object");
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Exception getting specific data item: "
                    + ex.getMessage());
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Queries server for clientID.
     * @return ClientID. -1 if anything went wrong.
     */
    int getClientID() {
        if (!this.greetServer()) {
            return -1;
        }
        int output = -1;
        try {
            out.writeObject(ConnCommand.CLIENT_ID_GET);
            out.flush();

            Object inObject = in.readObject();
            if (inObject instanceof Integer) {
                output = (Integer) inObject;
            } else if (inObject instanceof ConnState) {
                if ((ConnState) inObject == ConnState.COMMAND_FAIL) {
                    System.err.println("Server failed to execute command "
                            + "(getClientID)");
                } else {
                    System.err.println("Unexpected ConnState as output: "
                            + inObject.toString());
                }
            } else {
                throw new IOException("Unexpected object");
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Subscribes to buffer on server.
     * @param clientID
     */
    void subscribeSorted(int clientID) {
        if (!this.greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.SORTED_SUBSCRIBE);
            out.writeObject(clientID);
            out.flush();
            getCommandSuccess("Subscribe sorted data (" + clientID + ")");
        } catch (IOException ex) {
            System.err.println("Exception updating unsorted data: "
                    + ex.getMessage());
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
    }

    /**
     * Subscribes to buffer on server.
     * @param clientID
     */
    void subscribeRequests(int clientID) {
        if (!this.greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.UPDATE_REQUEST_SUBSCRIBE);
            out.writeObject(clientID);
            out.flush();
            getCommandSuccess("Subscribe requests (" + clientID + ")");
        } catch (IOException ex) {
            System.err.println("Exception updating unsorted data: "
                    + ex.getMessage());
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
    }

    /**
     *
     * @param clientID
     * @return newly submitted sorted data since last call.
     */
    List<ISortedData> getNewSorted(int clientID) {
       if (!this.greetServer()) {
            return null;
        }
        List<ISortedData> output = null;
        try {
            out.writeObject(ConnCommand.SORTED_GET_NEW);
            out.writeObject(clientID);
            out.flush();

            Object inObject = in.readObject();
            if (inObject instanceof List) {
                output = (List<ISortedData>) inObject;
            } else if (inObject instanceof ConnState) {
                if ((ConnState) inObject == ConnState.COMMAND_FAIL) {
                    System.err.println("Server failed to execute command "
                            + "(getNewSorted)");
                } else {
                    System.err.println("Unexpected ConnState as output: "
                            + inObject.toString());
                }
            } else {
                throw new IOException("Unexpected object");
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     *
     * @param clientID
     * @return newly submitted requests since last call.
     */
    List<IDataRequest> getNewRequests(int clientID) {
        if (!this.greetServer()) {
            return null;
        }
        List<IDataRequest> output = null;
        try {
            out.writeObject(ConnCommand.UPDATE_REQUEST_GET_NEW);
            out.writeObject(clientID);
            out.flush();

            Object inObject = in.readObject();
            if (inObject instanceof List) {
                output = (List<IDataRequest>) inObject;
            } else if (inObject instanceof ConnState) {
                if ((ConnState) inObject == ConnState.COMMAND_FAIL) {
                    System.err.println("Server failed to execute command "
                            + "(getNewRequests)");
                } else {
                    System.err.println("Unexpected ConnState as output: "
                            + inObject.toString());
                }
            } else {
                throw new IOException("Unexpected object");
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Unsubscribes from server. Does nothing if not subscribed.
     * @param clientID
     */
    void unsubscribeSorted(int clientID) {
        if (!this.greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.SORTED_UNSUBSCRIBE);
            out.writeObject(clientID);
            out.flush();
            getCommandSuccess("Unsubscribe sorted data (" + clientID + ")");
        } catch (IOException ex) {
            System.err.println("Exception updating unsorted data: "
                    + ex.getMessage());
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
    }

    /**
     * Unsubscribes from server buffer. Does nothing if not subscribed.
     * @param clientID
     */
    void unsubscribeRequests(int clientID) {
        if (!this.greetServer()) {
            return;
        }
        try {
            out.writeObject(ConnCommand.UPDATE_REQUEST_UNSUBSCRIBE);
            out.writeObject(clientID);
            out.flush();
            getCommandSuccess("Unsubscribe requests (" + clientID + ")");
        } catch (IOException ex) {
            System.err.println("Exception updating unsorted data: "
                    + ex.getMessage());
            Logger.getLogger(ServicesApp.Connection.Connection.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
    }

}
