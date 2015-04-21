/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia + Alexander
 */
public class ConnClientBase {

    private InputStream inStream = null;
    private OutputStream outStream = null;
    protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;
    private Socket socket = null;
    protected String IP;
    protected int port;

    protected ConnClientBase(String IP, int port) {
        this.IP = IP;
        this.port = port;
    }

    /**
     * Connects to Server
     *
     * @return connection success
     */
    protected boolean greetServer() {
        try {
            socket = new Socket(IP, port);

            this.outStream = socket.getOutputStream();
            this.inStream = socket.getInputStream();

            this.out = new ObjectOutputStream(outStream);
            this.in = new ObjectInputStream(inStream);

//            System.out.println("Saying hello to server");
            out.writeObject(ConnState.CONNECTION_START);
            out.flush();

            // checks whether connection with server is up and running
            boolean doneSayingHello = false;
            while (!doneSayingHello) {
                Object inObject;
                try {
                    inObject = in.readObject();

                    if ((inObject instanceof ConnState)
                            && (ConnState) inObject == ConnState.CONNECTION_START) {
//                        System.out.println("Connected to server");
                        doneSayingHello = true;
                    } else {
                        System.out.println("no valid object as connection "
                                + "confirmation");
                        System.err.println("Unable to connect to server "
                                + "- unexpected handshake");
                        return false;
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ConnClientBase.class.getName())
                            .log(Level.SEVERE, null, ex);
                    System.err.println("Unable to connect to server");
                    return false;
                }
            }

        } catch (IOException e) {
            System.out.println("IOException ConnectionManager.greetServer(): "
                    + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Closes down the connection - also notifies the server.
     */
    protected void closeSocket() {
        if (socket == null) {
            return;
        }
        try {
            out.writeObject(ConnState.CONNECTION_END);
            out.flush();
        } catch (IOException ex) {
            System.out.println("IOException notifying server of closedown: "
                    + ex.getMessage());
            Logger.getLogger(ConnClientBase.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ConnClientBase.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            socket = null;
        }

    }

    /**
     * Listens to server output whether command succeeded. Does its own output
     * to s.err
     *
     * @param command
     * @return true if command was a success. Optional.
     */
    protected boolean getCommandSuccess(ConnCommand command) {
        String description = this.getCommandDescription(command);
        try {
            Object inObject = in.readObject();
            if (inObject instanceof ConnState) {
                ConnState result = (ConnState) inObject;
                if (result == ConnState.COMMAND_SUCCESS) {
                    System.err.println(description + ": success");
                    return true;
                } else if (result == ConnState.COMMAND_FAIL) {
                    System.err.println(description + ": failure (Database failure)");
                } else if (result == ConnState.COMMAND_ERROR) {
                    System.err.println(description + ": error (Connection failure)");
                } else {
                    System.err.println("Unexpected input (" + description + "): "
                            + result.toString());
                }
            } else {
                System.err.println(description + ": Unrecognised object = "
                        + inObject.toString());
            }
        } catch (ClassNotFoundException | IOException ex) {
            System.err.println("Exception getting command result: " + ex.getMessage());
            Logger.getLogger(ConnClientBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Executes a command expecting only a success indicator.
     *
     * @param command
     * @param arguments
     * @return
     */
    protected boolean booleanCommand(ConnCommand command, Object[] arguments) {
        if (!this.greetServer()) {
            return false;
        }
        try {
            out.writeObject(command);
            for (Object arg : arguments) {
                out.writeObject(arg);
            }
            out.flush();
            return getCommandSuccess(command);
        } catch (IOException ex) {
            System.out.println("Exception sending boolean command to server: "
                    + ex.getMessage());
            Logger.getLogger(ConnClientBase.class.getName())
                    .log(Level.SEVERE, null, ex);
            return false;
        } finally {
            this.closeSocket();
        }
    }

    /**
     * Executes a command expecting a return type.
     *
     * @param command
     * @param arguments
     * @return null if something went wrong.
     */
    protected Object objectCommand(ConnCommand command, Object[] arguments) {
        if (!this.greetServer()) {
            return null;
        }
        Object output = null;
        try {
            out.writeObject(command);
            for (Object arg : arguments) {
                if(arg != null) {
                    out.writeObject(arg);
                } else {
                    return null;
                }
            }
            out.flush();

            Object inObject = in.readObject();
            if (inObject instanceof ConnState) {
                if ((ConnState) inObject == ConnState.COMMAND_FAIL) {
                    System.out.println("Server failed to execute object command "
                            + command.toString());
                } else {
                    System.err.println("Unexpected ConnState as output: "
                            + inObject.toString());
                }
                output = null;
            } else {
                output = inObject;
            }
        } catch (Exception ex) {
            Logger.getLogger(ConnClientBase.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.closeSocket();
        }
        return output;
    }

    /**
     * Translates ConnCommands into human-readable text.
     * @param command
     * @return
     */
    protected String getCommandDescription(ConnCommand command) {
        String output = "Unknown command";
        switch (command) {
            case CLIENT_ID_GET:
                output = "getting client ID";
                break;
            case SORTED_SEND:
                output = "sending sorted data";
                break;
            case SORTED_GET:
                output = "getting sorted data";
                break;
            case SORTED_GET_NEW:
                output = "retrieving new sorted data";
                break;
            case SORTED_SUBSCRIBE:
                output = "subscribing to sorted data updates";
                break;
            case SORTED_UNSUBSCRIBE:
                output = "unsubscribing from sorted data updates";
                break;
            case UNSORTED_SEND:
                output = "submitting new unsorted data";
                break;
            case UNSORTED_GET:
                output = "getting a batch of unsorted data";
                break;
            case UNSORTED_GET_ID:
                output = "get unsorted item by ID";
                break;
            case UNSORTED_GET_SOURCE:
                output = "get unsorted, filtered by source";
                break;
            case UNSORTED_STATUS_RESET:
                output = "notify server that data is nog longer being worked on";
                break;
            case UNSORTED_UPDATE_SEND:
                output = "update unsorted data";
                break;
            case UNSORTED_DISCARD:
                output = "discard given piece of unsorted data";
                break;
            case UNSORTED_GET_NEW:
                output = "retrieving new unsorted data";
                break;
            case UNSORTED_SUBSCRIBE:
                output = "subscriving to unsorted data updates";
                break;
            case UNSORTED_UNSUBSCRIBE:
                output = "unsubscribing from unsorted data updates";
                break;
            case UPDATE_REQUEST_SEND:
                output = "submitting a request for updated data";
                break;
            case UPDATE_REQUEST_GET:
                output = "retrieving all requests for updated data";
                break;
            case UPDATE_REQUEST_GET_NEW:
                output = "retrieving new update requests";
                break;
            case UPDATE_REQUEST_SUBSCRIBE:
                output = "subscribing to new update requests";
                break;
            case UPDATE_REQUEST_UNSUBSCRIBE:
                output = "unsubscribing from new update requests";
                break;
            case TASK_SEND:
                output = "sending task";
                break;
            case PLAN_SEND_NEW:
                output = "sending new plan";
                break;
            case PLAN_APPLY:
                output = "apply a plan";
                break;
            case TASKS_GET_NEW:
                output = "get new tasks";
                break;
            case TASKS_SUBSCRIBE:
                output = "subscribe to get updates for tasks";
                break;
            case TASKS_UNSUBSCRIBE:
                output = "unsubscribe to get updates for tasks";
                break;
        }
        return output;
    }

}
