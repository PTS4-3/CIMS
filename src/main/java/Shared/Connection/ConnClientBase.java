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
 * @author Kargathia
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
     * @param IP
     * @param port
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
     * @param description - a short description of what calling method was
     * trying to accomplish
     * @return true if command was a success. Optional.
     */
    protected boolean getCommandSuccess(String description){
        try {
            Object inObject = in.readObject();
            if (inObject instanceof ConnState) {
                ConnState result = (ConnState) inObject;
                if (result == ConnState.COMMAND_SUCCESS) {
                    System.err.println(description + ": success");
                    return true;
                } else if (result == ConnState.COMMAND_FAIL){
                    System.err.println(description + ": failure");
                } else {
                    System.err.println("Unexpected input ("+ description + "): "
                            + result.toString());
                }
            } else {
                System.err.println(description + ": ERROR");
            }
        } catch (ClassNotFoundException | IOException ex) {
            System.err.println("Exception getting command result: " + ex.getMessage());
            Logger.getLogger(ConnClientBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
