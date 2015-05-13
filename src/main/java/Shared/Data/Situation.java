/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import java.util.HashSet;

/**
 *
 * @author Kargathia
 */
public class Situation {
    private HashSet<Advice> advices;
    private String description;

    public HashSet<Advice> getAdvices() {
        return advices;
    }

    public String getDescription() {
        return description;
    }

    public Situation(HashSet<Advice> advices, String description) {
        this.advices = advices;
        this.description = description;
    }

}
