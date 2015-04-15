/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Tasks;

import java.io.Serializable;
import java.util.HashSet;
import java.util.SortedSet;

/**
 *
 * @author Alexander
 */
public interface IPlan extends Serializable {
    int getId();
    void setId(int Id);
    String getTitle();
    String getDescription();
    HashSet<String> getKeywords();
    SortedSet<IStep> getSteps();
    boolean isTemplate();
}
