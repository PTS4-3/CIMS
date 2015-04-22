/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import ServerApp.ServerMain;
import static ServerApp.ServerMain.sortedDatabaseManager;
import Shared.Data.ISortedData;
import Shared.Data.SortedData;
import Shared.Tag;
import Shared.Tasks.IPlan;
import Shared.Tasks.IStep;
import Shared.Tasks.ITask;
import Shared.Tasks.Task;
import Shared.Tasks.TaskStatus;
import Shared.Users.HQChief;
import Shared.Users.HQUser;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import Shared.Users.ServiceUser;
import Shared.Users.UserRole;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
public class TasksDatabaseManager extends DatabaseManager {

    private final String taskTable = "TASK",
            userTaskTable = "USERTASK",
            planTable = "PLAN",
            keywordTable = "KEYWORD",
            stepTable = "STEP",
            userTable = "USER";

    public TasksDatabaseManager(String fileName) {
        super(fileName);
    }

    /**
     * Extract users from a resultset, and cast it to correct type. Does not
     * open its own connection.
     *
     * @param rs
     * @return
     */
    private List<IUser> extractUsers(ResultSet rs) throws SQLException {
        List<IUser> output = new ArrayList<>();
        while (rs.next()) {
            String outputUserName = rs.getString("USERNAME");
            String outputName = rs.getString("NAME");
            String outputTagString = rs.getString("TAG");
            UserRole outputRole = UserRole.valueOf(rs.getString("ROLE"));

            switch (outputRole) {
                case SERVICE:
                    output.add(new ServiceUser(outputUserName, outputName,
                            Tag.valueOf(outputTagString)));
                    break;
                case HQ:
                    output.add(new HQUser(outputUserName, outputName));
                    break;
                case CHIEF:
                    output.add(new HQChief(outputUserName, outputName));
                    break;
                default:
                    throw new SQLException("User did not have a role defined");
            }
        }
        return output;
    }

    /**
     * Extracts tasks from a resultSet. Does not set Executor or Data. Does not
     * open its own connection.
     *
     * @param rs
     * @param data (optional) if associated SortedData for task(s) is known.
     * Will query for it otherwise.
     * @return
     * @throws SQLException
     */
    private List<ITask> extractTasks(ResultSet rs, ISortedData data) throws SQLException {
        List<ITask> output = new ArrayList<>();
        while (rs.next()) {
            int outputID = rs.getInt("ID");
            String outputTitle = rs.getString("TITLE");
            String outputDescription = rs.getString("DESCRIPTION");
            TaskStatus outputStatus = TaskStatus.valueOf(rs.getString("STATUS"));
            String outputDeclineReason = rs.getString("REASON");
            Tag outputExecutorTag = Tag.valueOf(rs.getString("TAG"));
            int outputDataID = rs.getInt("DATAID");
            // gets task executor
            IServiceUser executor = getTaskExecutor(outputID);
            // gets data (if needs be)
            ISortedData outputData = null;
            if(data != null){
                outputData = data;
            } else if (outputDataID != -1) {
                outputData = ServerMain.sortedDatabaseManager
                        .getFromSortedData(outputDataID);
            }

            output.add(new Task(outputID, outputTitle, outputDescription,
                    outputStatus, outputData, outputExecutorTag, executor));
        }
        return output;
    }

    /**
     * Does not open its own connection. Can return null.
     *
     * @param taskID
     * @return IServiceUser currently slated to execute given task.
     */
    private IServiceUser getTaskExecutor(int taskID) throws SQLException {
        IServiceUser output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        // retrieve the executor name
        String execUserName = null;
        query = "SELECT * FROM " + userTaskTable
                + " WHERE TASKID = " + taskID;
        prepStat = conn.prepareStatement(query);
        rs = prepStat.executeQuery();
        while (rs.next()) {
            execUserName = rs.getString("USERNAME");
        }

        // starts query for actual user
        // uses extractUsers to read it
        query = "SELECT * FROM " + userTable
                + " WHERE USERNAME = ?";
        prepStat = conn.prepareStatement(query);
        prepStat.setString(1, execUserName);
        rs = prepStat.executeQuery();

        // Delegates extracting resultset
        IUser executor = this.extractUsers(rs).get(0);
        if (executor instanceof IServiceUser) {
            output = (IServiceUser) executor;
        } else {
            throw new SQLException("getTaskExecutor() error: executor was not a ServiceUser");
        }
        return output;
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
            query = "INSERT INTO " + taskTable
                    + " (ID, TITLE, DESCRIPTION, TAG, DATAID, STATUS, REASON)"
                    + " VALUES (ID,?,?,?,?,?,?)";
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, newTask.getTitle());
            prepStat.setString(2, newTask.getDescription());
            prepStat.setString(3, newTask.getTargetExecutor().toString());
            if (newTask.getSortedData() == null) {
                prepStat.setInt(4, -1);
            } else {
                prepStat.setInt(4, newTask.getSortedData().getId());
            }
            prepStat.setString(5, newTask.getStatus().toString());
            if (newTask.getDeclineReason() == null) {
                prepStat.setString(6, "");
            } else {
                prepStat.setString(6, newTask.getDeclineReason());
            }
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
            query = "UPDATE " + taskTable + " SET STATUS = ? WHERE ID = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, input.getStatus().toString());
            prepStat.setInt(2, input.getId());
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

    /**
     *
     * @param ID
     * @return Task with given ID. Null if ID == -1 or no Task found.
     */
    public ITask getTask(int ID) {
        if (!openConnection() || (ID == -1)) {
            return null;
        }
        ITask output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            query = "SELECT * FROM " + taskTable
                    + " WHERE ID = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setInt(1, ID);
            rs = prepStat.executeQuery();
            // delegates actually extracting said tasks
            output = this.extractTasks(rs, null).get(0);

        } catch (SQLException ex) {
            System.out.println("failed to get task with id " + ID + ": " + ex.getMessage());
            Logger.getLogger(TasksDatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
            output = null;
        } finally {
            closeConnection();
        }

        return output;
    }

    /**
     * Updates given task with all new data
     *
     * @param input
     * @return
     */
    public ITask updateTask(ITask input) {
        if (!openConnection() || (input == null)) {
            return null;
        }
        ITask output = null;
        String query;
        PreparedStatement prepStat;

        try {
            // updates task itself
            query = "UPDATE " + taskTable
                    + " SET TITLE = " + input.getTitle()
                    + ", DESCRIPTION = " + input.getDescription()
                    + ", STATUS = " + input.getStatus().toString();
            if (input.getDeclineReason() != null) {
                query += ", REASON = " + input.getDeclineReason();
            }
            query += " WHERE ID = " + input.getId();
            prepStat = conn.prepareStatement(query);
            prepStat.execute();

            if (input.getExecutor() != null) {
                query = "REPLACE INTO " + userTaskTable
                        + "SET TASKID = " + input.getId()
                        + ", SET USERNAME = " + input.getExecutor().getUsername();
                prepStat = conn.prepareStatement(query);
                prepStat.execute();
            }

            // returns updated task
            output = getTask(input.getId());
        } catch (SQLException ex) {
            System.out.println("failed to update task: " + ex.getMessage());
            Logger.getLogger(TasksDatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
            output = null;
        } finally {
            closeConnection();
        }
        return output;
    }

    /**
     * Should not be used for login purposes - does not check for password
     *
     * @param userName
     * @return ServiceUser/HQChief/HQUser with given name
     */
    public IUser getUser(String userName) {
        if (!openConnection() || (userName == null)) {
            return null;
        }
        IUser output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            query = "SELECT * FROM " + userTable + " WHERE USERNAME = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, userName);
            rs = prepStat.executeQuery();

            output = this.extractUsers(rs).get(0);

        } catch (SQLException ex) {
            System.out.println("failed to retrieve user " + userName + ": "
                    + ex.getMessage());
            Logger.getLogger(TasksDatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
            output = null;
        } finally {
            closeConnection();
        }

        return output;
    }

    /**
     *
     * @param execFilter if null, return all tasks
     * @return
     */
    @Deprecated
    public List<ITask> getTasks(IServiceUser execFilter) {
        return null;
    }

    /**
     * Returns specific type of user
     *
     * @param userName
     * @param password
     * @return null if not found
     */
    public IUser loginUser(String userName, String password) {
        if (!openConnection() || (userName == null) || (password == null)) {
            return null;
        }
        IUser output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            query = "SELECT * FROM " + userTable + " WHERE USERNAME = ? AND PASSWORD = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, userName);
            prepStat.setString(2, password);
            rs = prepStat.executeQuery();

            output = this.extractUsers(rs).get(0);
        } catch (SQLException ex) {
            System.out.println("failed login attempt for " + userName
                    + ": " + ex.getMessage());
            Logger.getLogger(TasksDatabaseManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            closeConnection();
        }
        return output;
    }

    /**
     *
     * @param keywords
     * @return
     */
    @Deprecated
    public List<IPlan> getPlans(HashSet<String> keywords) {
        return null;
    }

    /**
     *
     * @param input
     * @return tasks associated with given ISortedData
     */
    public List<ITask> getSortedDataTasks(ISortedData input) {
        if (!openConnection() || (input == null)) {
            return null;
        }
        List<ITask> output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            query = "SELECT * FROM " + taskTable + " WHERE DATAID = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setInt(1, input.getId());
            rs = prepStat.executeQuery();

            // delegates actually extracting tasks
            output = this.extractTasks(rs, input);

            for (ITask task : output) {
                task.setSortedData((SortedData) input);
            }
        } catch (SQLException ex) {
            System.out.println("Failed to retrieve sorted data task IDs: " + ex.getMessage());
            Logger.getLogger(TasksDatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
            output = null;
        } finally {
            closeConnection();
        }

        return output;
    }

}
