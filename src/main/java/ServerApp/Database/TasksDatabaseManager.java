/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import Shared.Tasks.IPlan;
import Shared.Tasks.IStep;
import Shared.Tasks.ITask;
import Shared.Tasks.TaskStatus;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
public class TasksDatabaseManager extends DatabaseManager {

    private final String taskTable = "TASK",
            userTaskTable = "USERTASK",
            taskStatusTable = "TASKSTATUS",
            planTable = "PLAN",
            keywordTable = "KEYWORD",
            stepTable = "STEP";

    public TasksDatabaseManager(String fileName) {
        super(fileName);
    }

    /**
     * Inserts new ITask in the database
     *
     * @param newTask
     * @return
     */
    public ITask insertNewTask(ITask newTask) {
        if (!openConnection() || (newTask == null)) {
            return null;
        }
        ITask output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            // Inserts Task object
            query = "INSERT INTO " + taskTable + " VALUES (ID,?,?)";
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, newTask.getTitle());
            prepStat.setString(2, newTask.getDescription());
            prepStat.execute();

            // Gets assigned ID. Throws Exception if not found
            newTask.setId(getMaxID(taskTable));
            if (newTask.getId() == -1) {
                throw new SQLException("assigned ID not found");
            }

            // Sets executor (if applicable)
            if (newTask.getExecutor() != null) {
                query = "INSERT INTO " + userTaskTable + " VALUES (?, ?)";
                prepStat = conn.prepareStatement(query);
                prepStat.setInt(1, newTask.getId());
                prepStat.setString(2, newTask.getExecutor().getUsername());
                prepStat.execute();
            }
            output = newTask;

            // Sets task status if it was not Unassigned
            if (newTask.getStatus() != TaskStatus.UNASSIGNED
                    && newTask.getStatus() != null) {
                if (!setTaskStatus(newTask)) {
                    output = null;
                }
            }

        } catch (SQLException ex) {
            System.out.println("failed to execute insertNewTask: " + ex.getMessage());
            output = null;
            Logger.getLogger(TasksDatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeConnection();
        }
        return output;
    }

    /**
     * Inserts new IPlan and its tasks in the database
     *
     * @param input
     * @return
     */
    public IPlan insertNewPlan(IPlan input) {

        // inserts tasks
        // done beforehand, as method called opens/closes connection
        for (Iterator<IStep> it = input.getSteps().iterator(); it.hasNext();) {
            IStep step = it.next();
            ITask newTask = this.insertNewTask(step);
            if (newTask != null) {
                step.setId(newTask.getId());
            } else {
                System.out.println("newtask was null");
            }
        }

        if (!openConnection() || (input == null)) {
            return null;
        }

        IPlan output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            // inserts plan itself
            query = "INSERT INTO " + planTable + " VALUES (ID, ?, ?, ?)";
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, input.getTitle());
            prepStat.setString(2, input.getDescription());
            prepStat.setBoolean(3, input.isTemplate());
            prepStat.execute();

            // Gets assigned ID. Throws Exception if not found
            input.setId(getMaxID(planTable));
            if (input.getId() == -1) {
                throw new SQLException("assigned ID not found");
            }

            // inserts keywords
            for (String keyword : input.getKeywords()) {
                query = "INSERT INTO " + keywordTable + " VALUES (?, ?)";
                prepStat = conn.prepareStatement(query);
                prepStat.setInt(1, input.getId());
                prepStat.setString(2, keyword);
                prepStat.execute();
            }

            // inserts steps
            // corresponding tasks are at method start
            for (IStep step : input.getSteps()) {
                query = "INSERT INTO " + stepTable + " VALUES (?, ?, ?, ?)";
                prepStat = conn.prepareStatement(query);
                prepStat.setInt(1, input.getId());
                prepStat.setInt(2, step.getId());
                prepStat.setInt(3, step.getStepnr());
                prepStat.setString(4, step.getCondition());
                prepStat.execute();
            }
            output = input;

        } catch (SQLException ex) {
            System.out.println("failed to insert new plan: " + ex.getMessage());
            output = null;
            Logger.getLogger(TasksDatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeConnection();
        }

        return output;
    }

    /**
     * Updates status in database to status of given task. Nothing else is done.
     *
     * @param input
     * @return
     */
    public boolean setTaskStatus(ITask input) {
        if (!openConnection() || (input == null)) {
            return false;
        }
        boolean result = false;
        String query;
        PreparedStatement prepStat;

        try {
            // overwrites if existing, inserts if not
            query = "REPLACE INTO " + taskStatusTable
                    + " SET TASKID = " + input.getId()
                    + ", STATUS = " + input.getStatus().toString();
            prepStat = conn.prepareStatement(query);
            prepStat.execute();
            result = true;
        } catch (SQLException ex) {
            System.out.println("failed to set task status: " + ex.getMessage());
            Logger.getLogger(TasksDatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        } finally {
            closeConnection();
        }

        return result;
    }

}
