/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

/**
 *
 * @author Kargathia
 */
public class ServerMain{

    public static DatabaseManager databaseManager = null;
    public static ConnectionManager connectionManager = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       databaseManager = new DatabaseManager();
       connectionManager = new ConnectionManager();
    }
}
