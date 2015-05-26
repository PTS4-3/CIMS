/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import ServerApp.ServerMain;
import Shared.Data.INewsItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexander
 */
public class IndexController {
    
    public IndexController() {
        
    }
    
    public List<INewsItem> getNewsItems(int offset, int limit) {
        return new ArrayList<>();
        //return ServerMain.sortedDatabaseManager.getNewsItems(offset, limit);
    }
}
