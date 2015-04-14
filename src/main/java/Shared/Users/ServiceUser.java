/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Users;

import Shared.Tag;

/**
 *
 * @author Alexander
 */
public class ServiceUser extends User implements IServiceUser {
    private Tag type;
    
    /**
     * 
     * @param username cannot be null or empty
     * @param name cannot be null or empty
     * @param type the kind of ServiceUser, cannot be null
     */
    public ServiceUser(String username, String name, Tag type) {
        super(username, name);
        if(type == null) {
            throw new IllegalArgumentException("Voer een type in");
        }
        this.type = type;
    }
    
    @Override
    public Tag getType() {
        return this.type;
    }

}
