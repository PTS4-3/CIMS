/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Kargathia
 */
public class ConnectionManager {

    private static ExecutorService pool = Executors.newCachedThreadPool();

    private static Map<String, Connection> myConnections = new HashMap<>();

    /**
     * Starts a connection, and assigns it its own thread.
     * @param ip
     * @param port
     * @return Created connection if successfull.
     * null if ip was null
     */
    public static Connection startConnection(String ip, int port){
        if(ip == null){
            return null;
        }
        Connection newConn = new Connection();

    }

    public static Connection startConnection(String ip){
        return startConnection(ip, 8189);
    }
}
