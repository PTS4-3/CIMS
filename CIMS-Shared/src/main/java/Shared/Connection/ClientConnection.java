/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import Shared.Connection.ChangeRequest;

public class ClientConnection implements Runnable {

    // The host:port combination to connect to
    private final InetAddress hostAddress;
    private final int port;

    // The selector we'll be monitoring
    private final Selector selector;

    // The buffer into which we'll read data when it's available
    private final int bufferCapacity = 10485760;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(bufferCapacity);
    private final ByteBuffer overflowBuffer = ByteBuffer.allocate(bufferCapacity);

    // A list of PendingChange instances
    private final List pendingChanges = new LinkedList();

    // Maps a SocketChannel to a list of ByteBuffer instances
    private final List pendingData = new ArrayList<>();

    // Maps a SocketChannel to a RspHandler
    private final Map rspHandlers = Collections.synchronizedMap(new HashMap());

    // Currently active socket
    private SocketChannel socket;

    public ClientConnection(InetAddress hostAddress, int port) throws IOException {
        this.hostAddress = hostAddress;
        this.port = port;
        this.selector = this.initSelector();
    }

    /**
     * Queues up given piece of data. It will be sent to server when the loop in
     * run() gets around to it. <br>
     * This is done so that it can be called from multiple threads.
     *
     * @param data
     * @param handler
     * @throws IOException
     */
    public void send(byte[] data, IResponseHandler handler) throws IOException {
        if (socket == null || !socket.isConnected()) {
            // Start a new connection
            this.socket = this.initiateConnection();
            // Register the response handler
            this.rspHandlers.put(socket, handler);
        } else {
            synchronized (this.pendingChanges) {
                this.pendingChanges.add(new ChangeRequest(
                        socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
            }
        }

        // And queue the data we want written
        synchronized (this.pendingData) {
            // inserts a preceding int with array size
            byte[] length = ByteBuffer.allocate(4).putInt(data.length).array();
            byte[] dataWithLength = SerializeUtils.concat(length, data);
            this.pendingData.add(ByteBuffer.wrap(dataWithLength));
        }

        // Finally, wake up our selecting thread so it can make the required changes
        this.selector.wakeup();
    }

    /**
     * A constant loop, executing change requests per channel (register, read,
     * write). Will wait halfway until it is woken up by changerequests
     * (outgoing or incoming).
     */
    @Override
    public void run() {
        while (true) {
            try {
                // Process any pending changes
                synchronized (this.pendingChanges) {
                    Iterator changes = this.pendingChanges.iterator();
                    while (changes.hasNext()) {
                        ChangeRequest change = (ChangeRequest) changes.next();
                        switch (change.type) {
                            case ChangeRequest.CHANGEOPS:
                                SelectionKey key = change.socket.keyFor(this.selector);
                                key.interestOps(change.ops);
                                break;
                            case ChangeRequest.REGISTER:
                                change.socket.register(this.selector, change.ops);
                                break;
                        }
                    }
                    this.pendingChanges.clear();
                }

                // Wait for an event one of the registered channels
                this.selector.select();

                // Iterate over the set of keys for which events are available
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isConnectable()) {
                        this.finishConnection(key);
                    } else if (key.isReadable()) {
                        this.read(key);
                    } else if (key.isWritable()) {
                        this.write(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads the channel belonging to given key. The channel may contain
     * multiple transactions. <br>
     * The stream will always first contain an int with the size of the next
     * Transaction object in the stream. <br>
     * This method is always called from the run() method, as channel keys are
     * not thread-safe.
     *
     * @param key
     * @throws IOException
     */
    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Clear out our read buffer so it's ready for new data
        this.overflowBuffer.flip();
        this.readBuffer.clear();
        this.readBuffer.put(overflowBuffer);
        this.overflowBuffer.clear();
        if(readBuffer.position() > 0){
            System.out.println("overflowBuffer found: " + readBuffer.position());
        }
        

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            key.channel().close();
            key.cancel();
            return;
        }

        // while is repeated for every Transaction currently queued.
        // A portion of the byte array the size of a transaction is read into a new array
        // That array is handed off to the channel's responsehandler
        readBuffer.flip();
        while (readBuffer.limit() - readBuffer.position() > 4) {
            int size = readBuffer.getInt();
//            System.out.println("transaction size: " + size + " - numRead: " + numRead);
            if (readBuffer.limit() - readBuffer.position() >= size) {
                byte[] data = new byte[size];
                readBuffer.get(data);

                // Hand the data off to our worker threads
                // Look up the handler for this channel
                IResponseHandler handler = (IResponseHandler) this.rspHandlers.get(socketChannel);
                // And pass the response to it
                handler.handleResponse(data);
            } else {
                readBuffer.position(readBuffer.position() - 4);
                overflowBuffer.put(readBuffer.slice());
                readBuffer.position(readBuffer.limit());
            }
        }
    }

    /**
     * Writes all queued data into the channel belonging to given key. <br>
     * This method is only called from run(), as keys are not thread-safe.
     *
     * @param key
     * @throws IOException
     */
    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (this.pendingData) {
            // Write until there's not more data ...
            while (!pendingData.isEmpty()) {
                ByteBuffer buf = (ByteBuffer) pendingData.get(0);
                socketChannel.write(buf);
                if (buf.remaining() > 0) {
                    // ... or the socket's buffer fills up
                    break;
                }
                pendingData.remove(0);
            }

            if (pendingData.isEmpty()) {
                // We wrote away all data, so we're no longer interested
                // in writing on this socket. Switch back to waiting for
                // data.
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    /**
     * Doesn't close - finishes the process of connecting. <br>
     * Called from the run() method to make sure it only happens on the right thread.
     * @param key
     * @throws IOException
     */
    private void finishConnection(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Finish the connection. If the connection operation failed
        // this will raise an IOException.
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            // Cancel the channel's registration with our selector
            System.out.println(e);
            key.cancel();
            return;
        }

        // Register an interest in writing on this channel
        key.interestOps(SelectionKey.OP_WRITE);
    }

    /**
     * Attempts to connect to the server. This merely queues an attempt, the
     * connection will not be completed when this method finishes.
     *
     * @return
     * @throws IOException
     */
    private SocketChannel initiateConnection() throws IOException {
        // Create a non-blocking socket channel
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        // Kick off connection establishment
        socketChannel.connect(new InetSocketAddress(this.hostAddress, this.port));

        // Queue a channel registration since the caller is not the
        // selecting thread. As part of the registration we'll register
        // an interest in connection events. These are raised when a channel
        // is ready to complete connection establishment.
        synchronized (this.pendingChanges) {
            this.pendingChanges.add(new ChangeRequest(socketChannel,
                    ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
        }

        return socketChannel;
    }

    private Selector initSelector() throws IOException {
        // Create a new selector
        return SelectorProvider.provider().openSelector();
    }
}
