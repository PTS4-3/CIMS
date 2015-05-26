/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import Shared.Tag;
import java.util.Set;

/**
 *
 * @author Alexander
 */
public interface IDataRequest extends IData {
    int getRequestId();
    public Set<Tag> getTags();
}
