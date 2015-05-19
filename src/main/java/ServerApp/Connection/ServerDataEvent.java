/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Connection;

import java.nio.channels.SocketChannel;

class ServerDataEvent {

    public ConnectionHandler server;
    public SocketChannel socket;
    public byte[] data;

    public ServerDataEvent(ConnectionHandler server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
    }
}
