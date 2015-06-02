/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

/**
 *
 * @author Kargathia
 */
public class NewsItem implements INewsItem {

    private HashSet<Situation> situations;
    private int victims, ID;
    private String title, description, location, source;
    private Date date;

    @Override
    public void setID(int ID){
        this.ID = ID;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public HashSet<Situation> getSituations() {
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

    @Override
    public Date getDate() {
        return date;
    }
    
    @Override
    public String getDateString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.date);
        String dateString = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) +"-"+
                       String.valueOf(calendar.get(Calendar.MONTH)+1)+"-"+
                       String.valueOf(calendar.get(Calendar.YEAR)) +" om "+
                       String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) +":"+
                       String.valueOf(calendar.get(Calendar.MINUTE));
        return dateString;
    }

    /**
     * id specified
     * @param ID
     * @param title cannot be null or empty
     * @param description
     * @param location
     * @param source the username of the source, cannot be null or empty
     * @param situations
     * @param victims has to be zero or greater
     * @param date
     */
    public NewsItem(int ID,String title, String description, String location,
            String source, HashSet<Situation> situations, int victims, Date date) {
        if(title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Voer een titel in");
        }
        if(source == null || source.isEmpty()) {
            throw new IllegalArgumentException("Voer een bron in");
        }
        if(victims < 0) {
            throw new IllegalArgumentException("Slachtoffers moet 0 of meer zijn");
        }
        this.situations = situations;
        this.victims = victims;
        this.ID = ID;
        this.title = title;
        this.description = description;
        this.location = location;
        this.source = source;
        this.date = date;

        if(this.situations == null){
            this.situations = new HashSet<>();
        }
    }

    /**
     * no id specified
     * @param title cannot be null or empty
     * @param description
     * @param location
     * @param source the username of the source, cannot be null or empty
     * @param situations
     * @param victims has to be zero or greater
     */
    public NewsItem(String title, String description, String location,
            String source, HashSet<Situation> situations, int victims) {
        this(-1, title, description, location, source, situations, victims, null);
    }

    public void addSituation(Situation sit){
        this.situations.add(sit);
    }

    @Override
    public String toString() {
        return title;
    }
}
