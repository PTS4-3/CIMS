/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Tasks;

import Shared.Data.ISortedData;
import Shared.Tag;
import Shared.Users.IServiceUser;
import java.io.Serializable;

/**
 *
 * @author Alexander
 */
public interface ITask extends Serializable {
    int getId();
    String getTitle();
    String getDescription();
    TaskStatus getStatus();
    ISortedData getSortedData();
    Tag getTargetExecutor();
    IServiceUser getExecutor();
}
