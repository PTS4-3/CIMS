/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.Tasks.IPlan;

/**
 *
 * @author Alexander
 */
public class PlanExecutor {
    private IPlan plan;
    private int nextStep;

    /**
     * 
     * @param plan cannot be null or empty
     */
    public PlanExecutor(IPlan plan) {
        if(plan == null) {
            throw new IllegalArgumentException("Plan cannot be null or empty");
        }
        this.plan = plan;
        this.nextStep = 1;
    }
    
    public void executeNextStep() {
        if(this.nextStep > 0 && this.nextStep <= this.plan.getSteps().size()) {
            ServerMain.pushHandler.push(this.plan.getSteps().get(nextStep - 1));
            this.nextStep++;
        } else {
            ServerMain.planExecutorHandler.removePlanExecutor(this.plan);
        }
    }
}
