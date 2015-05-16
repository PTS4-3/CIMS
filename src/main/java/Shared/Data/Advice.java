/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import java.io.Serializable;

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

}
