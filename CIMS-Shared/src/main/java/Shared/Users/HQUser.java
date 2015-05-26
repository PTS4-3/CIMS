/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Users;

/**
 *
 * @author Alexander
 */
public class HQUser extends User implements IHQUser {
    
    /**
     * 
     * @param username cannot be null or empty
     * @param name cannot be null or empty
     */
    public HQUser(String username, String name) {
        super(username, name);
    }
        
}
