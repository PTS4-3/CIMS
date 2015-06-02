/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Kargathia
 */
public class Advice implements Serializable {

    private String description;
    private int ID;

    public int getID() {
        return ID;
    }

    public String getDescription() {
        return description;
    }

    public Advice(int ID, String description) {
        this.ID = ID;
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.description);
        hash = 97 * hash + this.ID;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Advice other = (Advice) obj;
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (this.ID != other.ID) {
            return false;
        }
        return true;
    }

    
}
