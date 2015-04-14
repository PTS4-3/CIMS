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
public class TasksDatabaseManager extends DatabaseManager{

    private final String tasksTable = "";

    public TasksDatabaseManager(String fileName) {
        super(fileName);
    }

    /**
     * Inserts new ITask in the database
     * @param newTask
     * @return
     */
    @Deprecated
    public boolean insertNewTask(ITask newTask) {
        //TODO
        return true;
    }

    /**
     * Inserts new IPlan and its tasks in the database
     * @param plan
     * @return
     */
    @Deprecated
    public boolean insertNewPlan(IPlan plan) {
        //TODO
        return true;
    }

}
