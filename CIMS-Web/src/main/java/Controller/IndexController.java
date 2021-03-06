/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import ServerApp.Database.SortedDatabaseManager;
import ServerApp.ServerMain;
import Shared.Data.Advice;
import Shared.Data.INewsItem;
import Shared.Data.NewsItem;
import Shared.Data.Situation;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

/**
 *
 * @author Alexander
 */
public class IndexController {
    private HashSet<Situation> situations = new HashSet<>();
    private HashSet<Advice> advices = new HashSet<>();
    private List<INewsItem> news = new ArrayList<>();
    private Date date = new Date();
    
//    private SortedDatabaseManager sortedDatabaseManager;
    
    public IndexController(ServletContext servletContext) {
        advices.add(new Advice(100, "Sluit ramen en deuren"));
        situations.add(new Situation(10, advices, "Gevaarlijke stoffen"));
        for(int i = 1; i <= 53; i++) {
            news.add(new NewsItem(i,"Title" + i, "Description" + i, "Location" + i,
            "Source" + 1, situations, 0, date));
        }

//        ServerMain.startDatabases(servletContext.getRealPath("/WEB-INF/"));
//        sortedDatabaseManager = new SortedDatabaseManager(servletContext.getRealPath("/WEB-INF/sorteddatabase.properties"));
    }
    
    public List<INewsItem> getNewsItems(int offset, int limit) {
//        if(news.size() > offset + limit) {
//            return news.subList(offset, offset + limit);
//        } else {
//            return news.subList(offset, news.size());
//        }
        
        return ServerMain.sortedDatabaseManager.getNewsItems(offset, limit);
    }
    
    public int getNewsItemCount() {
//        return news.size();
        return ServerMain.sortedDatabaseManager.getNewsItemCount();
    }
}
