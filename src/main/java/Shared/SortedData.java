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
    
    /**
     * 
     * @param id
     * @param title cannot be null or empty
     * @param description
     * @param location
     * @param source cannot be null or empty
     * @param relevance has to be between 1 and 5
     * @param reliability has to be between 1 and 5
     * @param quality has to be between 1 and 5
     * @param tags has to be a size of at least 1
     */
    public SortedData(int id, String title, String description, String location,
            String source, int relevance, int reliability, int quality, 
            HashSet<Tag> tags) {
        if(title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title SortedData cannot be "
                    + "null or empty");
        }
        if(source == null || source.isEmpty()) {
            throw new IllegalArgumentException("Source SortedData cannot be "
                    + "null or empty");
        }
        if(relevance < 1 || relevance > 5) {
            throw new IllegalArgumentException("Relevance SortedData has to be "
                    + "between 1 and 5");
        }
        if(reliability < 1 || reliability > 5) {
            throw new IllegalArgumentException("Reliability SortedData has to be "
                    + "between 1 and 5");
        }
        if(quality < 1 || quality > 5) {
            throw new IllegalArgumentException("Quality SortedData has to be "
                    + "between 1 and 5");
        }
        if(tags == null || tags.size() < 1) {
            throw new IllegalArgumentException("Tags SortedData has to be a "
                    + "size of at least 1");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.source = source;
        this.relevance = relevance;
        this.reliability = reliability;
        this.quality = quality;
        this.tags = tags;
    }

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
