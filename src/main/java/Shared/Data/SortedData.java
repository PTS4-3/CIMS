/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import Shared.Tag;
import Shared.Tasks.ITask;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    private List<ITask> tasks;
    
    /**
     * 
     * @param id
     * @param title cannot be null or empty
     * @param description
     * @param location
     * @param source the username of the source, cannot be null or empty
     * @param relevance has to be between 1 and 5
     * @param reliability has to be between 1 and 5
     * @param quality has to be between 1 and 5
     * @param tags cannot be null, empty list means all tags
     */
    public SortedData(int id, String title, String description, String location,
            String source, int relevance, int reliability, int quality, 
            HashSet<Tag> tags) {
        if(title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Voer een titel in");
        }
        if(source == null || source.isEmpty()) {
            throw new IllegalArgumentException("Voer een bron in");
        }
        if(relevance < 1 || relevance > 5) {
            throw new IllegalArgumentException("Voer een relevantie tussen "
                    + "1 en 5 in");
        }
        if(reliability < 1 || reliability > 5) {
            throw new IllegalArgumentException("Voer een betrouwbaarheid tussen "
                    + "1 en 5 in");
        }
        if(quality < 1 || quality > 5) {
            throw new IllegalArgumentException("Voer een kwaliteit tussen "
                    + "1 en 5 in");
        }
        if(tags == null) {
            throw new IllegalArgumentException("Voer een bestemming in");
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

        if(this.tags.isEmpty()){
            this.tags.addAll(Arrays.asList(Tag.values()));
        }
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
    
    @Override
    public List<ITask> getTasks() {
        return this.tasks;
    }
    
    @Override
    public void setTasks(List<ITask> tasks) {
        this.tasks = tasks;
    }
    
    @Override
    public String toString() {
        return this.title + ", " + String.valueOf(this.relevance) + ", " +
                String.valueOf(this.reliability) + ", " + String.valueOf(this.quality);
    }
    
}
