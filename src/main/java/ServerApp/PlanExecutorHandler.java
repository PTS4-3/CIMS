/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.Tasks.IPlan;
import Shared.Tasks.IStep;
import java.util.HashMap;

/**
 *
 * @author Alexander
 */
public class PlanExecutorHandler {
    // key: IPlan.id, Value: PlanExecutor
    private HashMap<Integer, PlanExecutor> executors;
    
    public PlanExecutorHandler() {
        this.executors = new HashMap<>();
    }
    
    /**
     * Add plan executor with the given plan
     * @param plan 
     */
    public synchronized void addPlanExecutor(IPlan plan) {
        this.executors.put(plan.getId(), new PlanExecutor(plan));
        executors.get(plan.getId()).executeNextStep();
    }
    
    /**
     * Remove plan executor of the given plan
     * @param plan 
     */    
    public synchronized void removePlanExecutor(IPlan plan) {
        this.executors.remove(plan.getId());
    }
    
    /**
     * Send the step after this step to the executor
     * @param step 
     */
    public void executeNextStepOf(IStep step) {
        this.executors.get(step.getPlanId()).executeNextStep();
    }
}
