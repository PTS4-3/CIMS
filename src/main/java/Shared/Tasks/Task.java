/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Tasks;

import Shared.Data.ISortedData;
import Shared.Data.SortedData;
import Shared.Tag;
import Shared.Users.IServiceUser;

/**
 *
 * @author Alexander
 */
public class Task implements ITask {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;
    private ISortedData sortedData;
    private Tag targetExecutor;
    private IServiceUser executor;
    
    /**
     * 
     * @param id
     * @param title cannot be null or empty
     * @param description 
     * @param status 
     * @param sortedData null if this task is not linked yet
     * @param targetExecutor the target executor for this task
     * @param executor the current executor of this task
     */
    public Task(int id, String title, String description, TaskStatus status, 
            ISortedData sortedData, Tag targetExecutor, IServiceUser executor) {
        if(title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Voer een titel in");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.sortedData = sortedData;
        this.targetExecutor = targetExecutor;
        this.executor = executor;
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
    public TaskStatus getStatus() {
        return this.status;
    }

    @Override
    public ISortedData getSortedData() {
        return this.sortedData;
    }

    @Override
    public Tag getTargetExecutor() {
        return this.targetExecutor;
    }

    @Override
    public IServiceUser getExecutor() {
        return this.executor;
    }

    @Override
    public void setExecutor(IServiceUser serviceUser) {
        this.executor = serviceUser;
    }
}
