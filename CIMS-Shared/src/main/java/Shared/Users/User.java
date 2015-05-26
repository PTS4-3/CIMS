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
public class User implements IUser {
    private String username;
    private String name;
    
    /**
     * 
     * @param username cannot be null or empty
     * @param name cannot be null or empty
     */
    public User(String username, String name) {
        if(username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Voer een gebruikersnaam in");
        }
        if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Voer een naam in");
        }
        this.username = username;
        this.name = name;
    }
    
    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getName() {
        return this.name;
    }
    
}
