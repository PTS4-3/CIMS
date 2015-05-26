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
public class HQChief extends HQUser implements IHQChief {
    
    /**
     * 
     * @param username cannot be null or empty
     * @param name cannot be null or empty
     */
    public HQChief(String username, String name) {
        super(username, name);
    }
}
