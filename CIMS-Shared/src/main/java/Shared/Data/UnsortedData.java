/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

/**
 *
 * @author Alexander
 */
public class UnsortedData implements IUnsortedData {
    private int id;
    private String title;
    private String description;
    private String location;
    private String source;
    private Status status;
    
    /**
     * 
     * @param id
     * @param title cannot be null or empty
     * @param description
     * @param location
     * @param source the username of the source, cannot be null or empty
     * @param status 
     */
    public UnsortedData(int id, String title, String description, 
            String location, String source, Status status) {
        if(title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Voer een titel in");
        }
        if(source == null || source.isEmpty()) {
            throw new IllegalArgumentException("Voer een bron in");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.source = source;
        this.status = status;
    }

    /**
     * Convenience method for client-side init - no id and status
     * @param title cannot be null or empty
     * @param description
     * @param location
     * @param source cannot be null or empty
     */
    public UnsortedData(String title, String description, String location, String source){
        this(-1, title, description, location, source, Status.NONE);
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
    public Status getStatus() {
        return this.status;
    }
    
    @Override
    public String toString() {
        return this.title;
    }
}
