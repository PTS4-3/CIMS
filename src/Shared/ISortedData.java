/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared;

import java.util.Set;

/**
 *
 * @author Alexander
 */
public interface ISortedData extends IData {
    int getRelevance();
    int getReliability();
    int getQuality();
    Set<Tag> getTags();
}
