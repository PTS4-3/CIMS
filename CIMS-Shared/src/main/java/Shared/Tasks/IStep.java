/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Tasks;

/**
 *
 * @author Alexander
 */
public interface IStep extends ITask, Comparable<IStep> {
    int getStepnr();
    void setStepnr(int nr);
    int getPlanId();
    void setPlanId(int planId);
    String getCondition();    
}
