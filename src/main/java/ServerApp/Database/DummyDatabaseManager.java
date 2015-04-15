/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import Shared.Tasks.IPlan;
import Shared.Tasks.ITask;

/**
 *
 * @author Kargathia
 */
public class DummyDatabaseManager {

    /**
     * Database works
     * @param newTask
     * @return
     * @deprecated
     */
    @Deprecated
    public boolean insertTask(ITask newTask) {
        //TODO
        return true;
    }

    @Deprecated
    public boolean insertNewPlan(IPlan plan) {
        //TODO
        return true;
    }

}
