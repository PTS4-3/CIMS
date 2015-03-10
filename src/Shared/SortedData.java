/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Alexander
 */
public class SortedData implements ISortedData {
    private int id;
    private String title;
    private String description;
    private String location;
    private String source;
    private int relevance;
    private int reliability;
    private int quality;
    private HashSet<Tag> tags;

    @Override
    public int getId() {
        return this.id;
    }
    
    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getLocation() {
        return this.location;
    }

    @Override
    public String getSource() {
        return this.source;
    }
            
    @Override
    public int getRelevance() {
        return this.relevance;
    }

    @Override
    public int getReliability() {
        return this.reliability;
    }

    @Override
    public int getQuality() {
        return this.quality;
    }

    @Override
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }
    
}
