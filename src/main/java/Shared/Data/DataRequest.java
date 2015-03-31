/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import Shared.Tag;
import java.util.Arrays;
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
     * @param tags cannot be null, empty list means all tags
     */
    public DataRequest(int id, String title, String description, String location,
            String source, int requestId, HashSet<Tag> tags) {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Voer een titel in");
        }
        if (source == null || source.isEmpty()) {
            throw new IllegalArgumentException("Voer een bron in");
        }
        if (tags == null) {
            throw new IllegalArgumentException("Voer een bestemming in");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.source = source;
        this.requestId = requestId;
        this.tags = tags;

        if(this.tags.isEmpty()){
            this.tags.addAll(Arrays.asList(Tag.values()));
        }
    }

    /**
     * Convenience method for generating new requests clientSide, when ID is not
     * assigned yet.
     *
     * @param title
     * @param description
     * @param location
     * @param source
     * @param requestId
     * @param tags
     */
    public DataRequest(String title, String description, String location,
            String source, int requestId, HashSet<Tag> tags) {
        this(-1, title, description, location, source, requestId, tags);
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
    
    @Override
    public String toString() {
        return "MELDING: " + this.title;
    }
}
