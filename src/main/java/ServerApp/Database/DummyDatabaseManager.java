/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import Shared.Tasks.IPlan;
import Shared.Tasks.ITask;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import java.util.HashSet;
import java.util.List;

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

    @Deprecated
    public boolean updateTask(ITask input){
        return true;
    }

    /**
     *
     * @param execFilter if null, return all tasks
     * @return
     */
    public List<ITask> getTasks(IServiceUser execFilter){
        return null;
    }

    /**
     * Returns specific type of user
     * @param userName
     * @param password
     * @return null if not found
     */
    @Deprecated
    public IUser loginUser(String userName, String password){
        return null;
    }

    /**
     *
     * @param keywords
     * @return
     */
    public List<IPlan> getPlans(HashSet<String> keywords){
        return null;
    }

    /**
     *
     * @param ID
     * @return null if not found
     */
    @Deprecated
    public ITask getTask(int ID){
        return null;
    }

    @Deprecated
    public IUser getUser(String userName){
        return null;
    }

}
