/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared;

/**
 *
 * @author Alexander
 */
public class NetworkException extends Exception {
    
    /**
     * 
     * @param message 
     */
    public NetworkException(String message) {
        super(message);
    }
}
