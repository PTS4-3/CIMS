/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import Shared.Tag;
import Shared.Tasks.ITask;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Alexander
 */
public interface ISortedData extends IData {
    public int getRelevance();
    public int getReliability();
    public int getQuality();
    public Set<Tag> getTags();
    public List<ITask> getTasks();
    public void setTasks(List<ITask> tasks);
}
