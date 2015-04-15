/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Tasks;

import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Alexander
 */
public class Plan implements IPlan {
    private int id;
    private String title;
    private String description;
    private HashSet<String> keywords;
    private TreeSet<IStep> steps;
    private boolean template;
    
    /**
     * 
     * @param id
     * @param title cannot be null or empty
     * @param description
     * @param keywords has to be a size of at least 1
     * @param steps has to be a size of at least 1
     * @param template
     */
    public Plan(int id, String title, String description, 
            HashSet<String> keywords, TreeSet<IStep> steps, boolean template) {
        if(title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Voer een titel in");
        }
        if(keywords == null || keywords.size() < 1) {
            throw new IllegalArgumentException("Voer tenminste 1 keyword in");
        }
        if(steps == null || steps.size() < 1) {
            throw new IllegalArgumentException("Voer tenminste 1 stap in");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.steps = steps;
        this.template = template;
    }
    
    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int input){
        this.id = input;
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
    public HashSet<String> getKeywords() {
        return this.keywords;
    }

    @Override
    public TreeSet<IStep> getSteps() {
        return this.steps;
    }
    
    @Override
    public boolean isTemplate() {
        return this.template;
    }
}
