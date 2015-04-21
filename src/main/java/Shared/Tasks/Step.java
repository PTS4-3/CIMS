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
public class Step extends Task implements IStep {
    private int stepnr;
    private String condition;
    
    /**
     * 
     * @param id
     * @param title cannot be null or empty
     * @param description 
     * @param status 
     * @param sortedData null if this task is not linked yet
     * @param targetExecutor the target executor for this task
     * @param executor the current executor of this task
     * @param stepnr has to be greater than 0
     * @param condition
     */
    public Step(int id, String title, String description, TaskStatus status, 
            ISortedData sortedData, Tag targetExecutor, IServiceUser executor,
            int stepnr, String condition) {
        super(id, title, description, status, sortedData, targetExecutor, executor);
        if(stepnr <= 0) {
            throw new IllegalArgumentException("Stapnummer moet groter zijn dan 0");
        }
        this.stepnr = stepnr;
        this.condition = condition;
    }
    
    @Override
    public int getStepnr() {
        return this.stepnr;
    }
    
    @Override
    public void setStepnr(int nr) {
        this.stepnr = nr;
    }

    @Override
    public String getCondition() {
        return this.condition;
    }

    @Override
    public int compareTo(IStep o) {
        return Integer.compare(this.stepnr, o.getStepnr());
    }

    @Override
    public void setId(int Id) {
        super.setId(Id);
    }

    @Override
    public void setSortedData(SortedData data) {
        super.setSortedData(data);
    }
}
