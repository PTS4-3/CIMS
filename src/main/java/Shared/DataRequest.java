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
public class DataRequest implements IDataRequest {
    private int id;
    private String title;
    private String description;
    private String location;
    private String source;
    private int requestId;
    private HashSet<Tag> tags;    
    
     /**
     * 
     * @param id
     * @param title cannot be null or empty
     * @param description
     * @param location
     * @param source cannot be null or empty
     * @param requestId id of the data this is a request of, otherwise -1
     * @param tags has to be a size of at least 1
     */
    public DataRequest(int id, String title, String description, String location,
            String source, int requestId, HashSet<Tag> tags) {
        if(title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title DataRequest cannot be "
                    + "null or empty");
        }
        if(source == null || source.isEmpty()) {
            throw new IllegalArgumentException("Source DataRequest cannot be "
                    + "null or empty");
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
        this.requestId = requestId;
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
    public int getRequestId() {
        return this.requestId;
    }

    @Override
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(this.tags);
    }
}
