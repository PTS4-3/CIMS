/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared;

/**
 *
 * @author Alexander
 */
public class UnsortedData implements IData {
    private int id;
    private String title;
    private String description;
    private String location;
    private String source;
    
    public UnsortedData(int id, String title, String description, 
            String location, String source) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.source = source;
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
    
}
