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
import Shared.Tasks.Plan;
import Shared.Tasks.Step;
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
import java.util.HashMap;
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

    /**
     * A data-only container for all variables needed for initiating a plan.
     * <br>
     * Only used by database to temporarily store variables until it has enough
     * data to satisfy the plan constructor.
     */
    private class PlanStruct {

        // plan table
        int id;
        String title, description;
        boolean isTemplate;
        //keyword table
        HashSet<String> keywords = new HashSet<>();
        //step table
        HashSet<Integer> stepTaskIDs = new HashSet<>();
        HashMap<Integer, Integer> stepNumbers = new HashMap<>();
        HashMap<Integer, String> stepConditions = new HashMap<>();
        //task + step table
        List<IStep> steps = new ArrayList<>();
    }

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
            if (data != null) {
                outputData = data;
            } else if (outputDataID != -1) {
                outputData = ServerMain.sortedDatabaseManager
                        .getFromSortedData(outputDataID);
            }
            ITask outputItem = new Task(outputID, outputTitle, outputDescription,
                    outputStatus, outputData, outputExecutorTag, executor);
//            if (!outputDeclineReason.isEmpty()) {
            outputItem.setDeclineReason(outputDeclineReason);
//            }
            output.add(outputItem);
        }
        return output;
    }

    /**
     * Does not open its own connection. Can return null without something being
     * wrong.
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
        // return null if task was not matched with an executor yet.
        if (execUserName == null) {
            return null;
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
            } else if (newTask.getSortedData().getId() == -1) {
                throw new SQLException("Sorted data with -1 ID");
            } else {
                prepStat.setInt(4, newTask.getSortedData().getId());
            }
            prepStat.setString(5, newTask.getStatus().toString());
            prepStat.setString(6, newTask.getDeclineReason());
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
    public boolean updateTask(ITask input) {
        if (!openConnection() || (input == null)) {
            return false;
        }
        boolean output = false;
        String query;
        PreparedStatement prepStat;

        try {
            // updates task itself
            query = "UPDATE " + taskTable
                    + " SET TITLE = ?, DESCRIPTION = ?, STATUS = ?, REASON = ?"
                    + " WHERE ID = ?";
            
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, input.getTitle());
            prepStat.setString(2, input.getDescription());
            prepStat.setString(3, input.getStatus().toString());
            prepStat.setString(4, input.getDeclineReason());
            prepStat.setInt(5, input.getId());
            prepStat.execute();

            if (input.getExecutor() != null) {
                query = "REPLACE INTO " + userTaskTable
                        + "(TASKID, USERNAME) VALUES (?, ?)";
                prepStat = conn.prepareStatement(query);
                prepStat.setInt(1, input.getId());
                prepStat.setString(2, input.getExecutor().getUsername());
                prepStat.execute();
            }

            // returns updated task
            output = true;
        } catch (SQLException ex) {
            System.out.println("failed to update task: " + ex.getMessage());
            Logger.getLogger(TasksDatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
            output = false;
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
            query = "SELECT * FROM " + userTable + " WHERE BINARY USERNAME = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, userName);
            rs = prepStat.executeQuery();

            // Delegates extracting users
            List<IUser> extractedUsers = this.extractUsers(rs);
            if (extractedUsers.size() == 1) {
                output = extractedUsers.get(0);
            }
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
     * @return all ServiceUsers
     */
    public List<IServiceUser> getServiceUsers() {
        if (!openConnection()) {
            return null;
        }
        List<IServiceUser> output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            query = "SELECT * FROM " + userTable + " WHERE ROLE = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, UserRole.SERVICE.toString());
            rs = prepStat.executeQuery();

            output = new ArrayList<>();
            // Delegates extracting users, but checks them anyway
            for (IUser user : this.extractUsers(rs)) {
                if (user instanceof IServiceUser) {
                    output.add((IServiceUser) user);
                } else {
                    throw new SQLException("Extracted user was no ServiceUser "
                            + "(getServiceUsers)");
                }
            }
        } catch (SQLException ex) {
            System.out.println("failed to retrieve serviceUsers: "
                    + ex.getMessage());
            Logger.getLogger(TasksDatabaseManager.class.getName())
                    .log(Level.SEVERE, null, ex);
            output = null;
        } finally {
            closeConnection();
        }
        return output;
    }

    /**
     *
     * @param execUserName if null or empty, return all tasks
     * @param filter
     * @return
     */
    public List<ITask> getTasks(String execUserName, HashSet<TaskStatus> filter) {
        if (!openConnection() || filter == null) {
            return null;
        }
        List<ITask> output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            query = "SELECT * FROM " + taskTable;
            int count = 1;
            if (execUserName != null && !execUserName.isEmpty()) {
                query += " WHERE ID IN "
                        + "(SELECT TASKID FROM " + userTaskTable
                        + " WHERE USERNAME = ?)";
                count++;
            }
            boolean firstItem = true;
            
            for(TaskStatus status : filter){
                if(firstItem && execUserName != null && !execUserName.isEmpty()){
                    query += " AND STATUS = ?";
                    firstItem = false;
                } else if(firstItem){
                    query += " WHERE STATUS = ?";
                    firstItem = false;
                } else {
                    query += " OR STATUS = ?";
                }
            }
            prepStat = conn.prepareStatement(query);
            if(execUserName != null && !execUserName.isEmpty()) {
                prepStat.setString(1, execUserName);
            }

            for (TaskStatus status : filter) {
                prepStat.setString(count, status.toString());
                count++;
            }
            rs = prepStat.executeQuery();

            // Delegates extracting tasks
            output = this.extractTasks(rs, null);
        } catch (SQLException ex) {
            if (execUserName != null && !execUserName.isEmpty()) {
                System.out.print("(given IServiceUser: "
                        + execUserName + ") ");
            }
            System.out.println("failed to retrieve tasks: " + ex.getMessage());
            Logger.getLogger(TasksDatabaseManager.class.getName())
                    .log(Level.SEVERE, null, ex);
            output = null;
        } finally {
            closeConnection();
        }
        return output;
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
            query = "SELECT * FROM " + userTable
                    + " WHERE BINARY USERNAME = ? AND BINARY PASSWORD = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, userName);
            prepStat.setString(2, password);
            rs = prepStat.executeQuery();

            // delegates extracting users
            List<IUser> extractedUsers = this.extractUsers(rs);
            if (extractedUsers.size() == 1) {
                output = extractedUsers.get(0);
            }
        } catch (SQLException ex) {
            System.out.println("failed login attempt for " + userName
                    + ": " + ex.getMessage());
            Logger.getLogger(TasksDatabaseManager.class.getName())
                    .log(Level.SEVERE, null, ex);
            output = null;
        } finally {
            closeConnection();
        }
        return output;
    }

    /**
     *
     * @param keywords
     * @return IPlans with <i>all</i> given keywords. <br>
     * Will return all Plans in database if empty <br>
     * only returns templates
     */
    public List<IPlan> getTemplatePlans(HashSet<String> keywords) {
        if (!openConnection() || (keywords == null)) {
            return null;
        }
        List<IPlan> output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        HashMap<Integer, PlanStruct> outputPlanStructs = new HashMap<>();

        try {
            // builds query, depending on how many keywords are provided
            query = "SELECT * FROM " + planTable
                    + " WHERE TEMPLATE = 1";
            if (keywords.size() > 0) {
                query += " and ID IN"
                        + " (SELECT PLANID FROM " + keywordTable
                        + " WHERE WORD LIKE ?)";
                for (int pos = 1; pos < keywords.size(); pos++) {
                    query += " AND ID IN (SELECT PLANID FROM " + keywordTable
                            + " WHERE WORD LIKE ?)";
                }
            }
            prepStat = conn.prepareStatement(query);
            int rep = 1;
            for (String kw : keywords) {
                prepStat.setString(rep, "%" + kw + "%");
                rep++;
            }
            rs = prepStat.executeQuery();

            // saves info from plan table in temporary struct
            while (rs.next()) {
                PlanStruct struct = new PlanStruct();
                struct.id = rs.getInt("ID");
                struct.description = rs.getString("DESCRIPTION");
                struct.title = rs.getString("TITLE");
                int isTemplateInt = rs.getInt("TEMPLATE");
                if (isTemplateInt == 0) {
                    struct.isTemplate = false;
                } else if (isTemplateInt == 1) {
                    struct.isTemplate = true;
                } else {
                    throw new SQLException("template int was not a recognised value");
                }
                outputPlanStructs.put(struct.id, struct);
            }

            // returns empty list now if no plans were found
            if (outputPlanStructs.isEmpty()) {
                return new ArrayList<>();
            }

            // gets keywords
            query = "SELECT * FROM " + keywordTable + " WHERE PLANID = ?";
            for (int pos = 1; pos < outputPlanStructs.size(); pos++) {
                query += " OR PLANID = ?";
            }
            prepStat = conn.prepareStatement(query);
            rep = 1;
            for (PlanStruct struct : outputPlanStructs.values()) {
                prepStat.setInt(rep, struct.id);
                rep++;
            }
            rs = prepStat.executeQuery();

            // updates plan structs with keywords
            while (rs.next()) {
                int planID = rs.getInt("PLANID");
                outputPlanStructs.get(planID).keywords.add(rs.getString("WORD"));
            }

            // gets task IDs per plan. taskID + planID can later be used as primary key
            query = "SELECT * FROM " + stepTable + " WHERE PLANID = ?";
            for (int pos = 1; pos < outputPlanStructs.size(); pos++) {
                query += " OR PLANID = ?";
            }
            prepStat = conn.prepareStatement(query);
            rep = 1;
            for (PlanStruct struct : outputPlanStructs.values()) {
                prepStat.setInt(rep, struct.id);
                rep++;
            }
            rs = prepStat.executeQuery();

            // updates plan structs with task IDs from steps
            while (rs.next()) {
                int planID = rs.getInt("PLANID");
                int taskID = rs.getInt("TASKID");
                PlanStruct struct = outputPlanStructs.get(planID);
                struct.stepTaskIDs.add(taskID);
                struct.stepNumbers.put(taskID, rs.getInt("NUMBER"));
                struct.stepConditions.put(taskID, rs.getString("CONDITION"));
            }

            // gets tasks per plan
            for (PlanStruct struct : outputPlanStructs.values()) {
                query = "SELECT * FROM " + taskTable + " WHERE ID = ?";
                for (int pos = 1; pos < struct.stepTaskIDs.size(); pos++) {
                    query += " OR ID = ?";
                }
                prepStat = conn.prepareStatement(query);
                rep = 1;
                for (int taskID : struct.stepTaskIDs) {
                    prepStat.setInt(rep, taskID);
                    rep++;
                }
                rs = prepStat.executeQuery();
                // Delegates extracting tasks, adds them to struct
                for (ITask task : this.extractTasks(rs, null)) {
                    int stepNr = struct.stepNumbers.get(task.getId());
                    String stepCondition = struct.stepConditions.get(task.getId());
                    struct.steps.add(new Step(task, stepNr, stepCondition));
                }
            }

            // creates plans
            // adds them to output
            output = new ArrayList<>();
            for (PlanStruct struct : outputPlanStructs.values()) {
                Plan outputItem = new Plan(struct.id, struct.title,
                        struct.description, struct.keywords, struct.steps,
                        struct.isTemplate);
                output.add(outputItem);
            }

        } catch (SQLException ex) {
            System.out.println("Error trying to retrieve plans filtered by keyword: "
                    + ex.getMessage());
            Logger.getLogger(TasksDatabaseManager.class.getName())
                    .log(Level.SEVERE, null, ex);
            output = null;
        } finally {
            closeConnection();
        }
        return output;
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
