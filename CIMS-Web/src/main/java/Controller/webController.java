/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Shared.Data.Advice;
import Shared.Data.NewsItem;
import Shared.Data.Situation;
import java.io.IOException;
import java.util.ArrayList;
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
public class webController extends HttpServlet{
    
    private HashSet<Situation> situations = new HashSet<Situation>();
    private HashSet<Advice> advices = new HashSet<Advice>();
    private List<NewsItem> news = new ArrayList<NewsItem>();
    private Date date = new Date();
    
    public webController(){
        advices.add(new Advice(100, "Sluit ramen en deuren"));
        situations.add(new Situation(10, advices, "Gevaarlijke stoffen"));
        news.add(new NewsItem(1,"Title1", "Description1", "Location1",
            "Source1", situations, 0, date));
        news.add(new NewsItem(2,"Title2", "Description2", "Location2",
            "Source2", situations, 1, date));
        news.add(new NewsItem(3,"Title3", "Description3", "Location3",
            "Source3", situations, 2, date));

    }
    
    public String goToNews(){
        String redirectURL = "news.jsp";
        return redirectURL;
    }
    
    public List<NewsItem> getNews(){
        return news;
    }
    
    public NewsItem getNewsWithID(String ID){
        NewsItem item = null;
        int id = (Integer.parseInt(ID));
        for(NewsItem x : news){
            if(x.getId()== id)
            {
                return x;
            }
        }
        return item;   
    }
    
    public String getIconURL(Situation situation){
        if(situation.getDescription().equals("Gevaarlijke stoffen"))
        {
            return "images/icon_stoffen.png";
        }
        else if(situation.getDescription().equals("Inbraak"))
        {
            return "images/icon_inbraak.png";
        }
        else if(situation.getDescription().equals("Levensgevaar"))
        {
            return "images/icon_leven.png";
        }
        else if(situation.getDescription().equals("Terroristische aanslag"))
        {
            return "images/icon_aanslag.png";
        }
        else if(situation.getDescription().equals("Brand"))
        {
            return "images/icon_brand.png";
        }
        else if(situation.getDescription().equals("Instortingsgevaar"))
        {
            return "images/icon_instorting.png";
        }
        else if(situation.getDescription().equals("Ordeverstoring"))
        {
            return "images/icon_orde.png";
        }
        else if(situation.getDescription().equals("Verkeersongeval"))
        {
            return "images/icon_verkeer.png";
        }
        else if(situation.getDescription().equals("Extreem weer"))
        {
            return "images/icon_weer.png";
        }
        else{
        return null;
        }
    }
}
