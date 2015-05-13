/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Kargathia
 */
public class NewsItem implements INewsItem {

    private HashSet<Situation> situations;
    private int victims, ID;
    private String title, description, location, source;

    @Override
    public Set<Situation> getSituations() {
        return situations;
    }

    @Override
    public int getVictims() {
        return victims;
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getSource() {
        return source;
    }

    /**
     * id specified
     * @param ID
     * @param title
     * @param description
     * @param location
     * @param source
     * @param situations
     * @param victims
     */
    public NewsItem(int ID,String title, String description, String location,
            String source, HashSet<Situation> situations, int victims) {
        this.situations = situations;
        this.victims = victims;
        this.ID = ID;
        this.title = title;
        this.description = description;
        this.location = location;
        this.source = source;
    }

    /**
     * no id specified
     * @param title
     * @param description
     * @param location
     * @param source
     * @param situations
     * @param victims
     */
    public NewsItem(String title, String description, String location,
            String source, HashSet<Situation> situations, int victims) {
        this.ID = -1;
        this.situations = situations;
        this.victims = victims;
        this.title = title;
        this.description = description;
        this.location = location;
        this.source = source;
    }



}
