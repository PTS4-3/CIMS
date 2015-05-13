/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import java.util.Set;

/**
 *
 * @author Kargathia
 */
public interface INewsItem extends IData{
    public Set<Situation> getSituations();
    public int getVictims();
}
