/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import java.io.Serializable;
import java.util.HashSet;

/**
 *
 * @author Kargathia
 */
public class Situation implements Serializable {
    private HashSet<Advice> advices;
    private String description;
    private int ID;

    public int getID() {
        return ID;
    }

    public HashSet<Advice> getAdvices() {
        return advices;
    }

    public String getDescription() {
        return description;
    }

    public void addAdvice(Advice advice){
        advices.add(advice);
    }

    public Situation(int ID, HashSet<Advice> advices, String description) {
        this.advices = advices;
        this.description = description;
        this.ID = ID;

        if(this.advices == null){
            this.advices = new HashSet<>();
        }
    }

    @Override
    public String toString() {
        return description;
    }
}
