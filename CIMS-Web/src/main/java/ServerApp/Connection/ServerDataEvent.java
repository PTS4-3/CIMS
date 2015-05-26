/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

class ServerDataEvent {

    public ConnectionHandler server;
    public SocketChannel socket;
    public byte[] data;

    public ServerDataEvent(ConnectionHandler server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
        
        // for debugging only
        try {
            System.out.println("remote: " + socket.getRemoteAddress().toString()
                    + " - local: " + socket.getLocalAddress().toString());
        } catch (IOException ex) {
            Logger.getLogger(ServerDataEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
