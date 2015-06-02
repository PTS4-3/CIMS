/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import ServerApp.ServerMain;
import Shared.Data.Advice;
import Shared.Data.INewsItem;
import Shared.Data.NewsItem;
import Shared.Data.Situation;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.*;

/**
 *
 * @author Linda
 */
public class webController extends HttpServlet {

    private HashSet<Situation> situations = new HashSet<Situation>();
    private HashSet<Advice> advices = new HashSet<Advice>();
    private List<INewsItem> news = new ArrayList<INewsItem>();
    private Date date = new Date();

    public webController() {
        advices.add(new Advice(100, "Sluit ramen en deuren"));
        situations.add(new Situation(10, advices, "Gevaarlijke stoffen"));
        news.add(new NewsItem(1, "Title1", "Description1", "Rachelsmolen, Eindhoven",
                "Source1", situations, 0, date));
        news.add(new NewsItem(2, "Title2", "Description2", "Rachelsmolen, Eindhoven",
                "Source2", situations, 1, date));
        news.add(new NewsItem(3, "Title3", "Description3", "Rachelsmolen, Eindhoven",
                "Source3", situations, 2, date));

    }

    public INewsItem getNewsWithID(String ID) {
        return ServerMain.sortedDatabaseManager.getNewsItemByID(Integer.parseInt(ID));
    }

    public String getIconURL(Situation situation) {
        if (situation.getDescription().equals("Giftige stoffen")) {
            return "images/icon_stoffen.png";
        } else if (situation.getDescription().equals("Inbraak")) {
            return "images/icon_inbraak.png";
        } else if (situation.getDescription().equals("Levensgevaar")) {
            return "images/icon_leven.png";
        } else if (situation.getDescription().equals("Terroristische aanslag")) {
            return "images/icon_aanslag.png";
        } else if (situation.getDescription().equals("Brand")) {
            return "images/icon_brand.png";
        } else if (situation.getDescription().equals("Instortingsgevaar")) {
            return "images/icon_instorting.png";
        } else if (situation.getDescription().equals("Ordeverstoring")) {
            return "images/icon_orde.png";
        } else if (situation.getDescription().equals("Verkeersongeval")) {
            return "images/icon_verkeer.png";
        } else if (situation.getDescription().equals("Extreem weer")) {
            return "images/icon_weer.png";
        } else {
            return null;
        }
    }

    public HashSet<Advice> getAdvices(INewsItem item) {
        HashSet<Advice> set = new HashSet<Advice>();
        for (Situation sit : item.getSituations()) {
            for (Advice ad : sit.getAdvices()) {
                set.add(ad);
            }
        }
        return set;
    }
    
    public String getDate(){
        Date newsDate = new Date();
        DateFormat date = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat time = new SimpleDateFormat("HH:mm");
            
        return date.format(newsDate) + " om " + time.format(newsDate);
    }
}
