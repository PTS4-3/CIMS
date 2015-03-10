/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.ConnectionState;
import java.io.File;
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
public class Connection implements Runnable {
    
    private static String eol = System.getProperty("line.separator");
    private static String fs = File.separator;

    private Socket conn = null;
    private InputStream inStream = null;
    private OutputStream outStream = null;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Connection(Socket conn) {
        try {
            this.conn = conn;
            this.inStream = conn.getInputStream();
            this.outStream = conn.getOutputStream();

            this.in = new ObjectInputStream(inStream);
            this.out = new ObjectOutputStream(outStream);

            out.writeObject(ConnectionState.CONNECTED);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        // lets console know if something went wrong
        if (conn == null || inStream == null || outStream == null || in == null || out == null) {
            System.out.println("an object in Runnable was null: " + eol
                    + "conn: " + (conn == null) + eol
                    + "inStream: " + (inStream == null) + eol
                    + "outStream: " + (outStream == null) + eol
                    + "in: " + (in == null) + eol
                    + "out: " + (out == null) + eol);
        }
    }

}
