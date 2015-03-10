/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ServerApp;

import Shared.SortedData;
import Shared.UnsortedData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Linda
 */
public class DatabaseManager {
    
   private Connection conn;
   
   public DatabaseManager()
   {
       
   }
   
   public boolean insertToUnsortedData(UnsortedData data)
   {
       boolean succeed = false;
       
       return succeed;
   }
   
   public UnsortedData getFromUnsortedData()
   {
       return null;
   }
   
   public boolean insertToSortedData(SortedData data)
   {
       boolean succeed = false;
       
       return succeed;
   }
   
   public SortedData getFromSortedData()
   {
       return null;
   }
   
   
   private void openConnection()
   {
       try{
           conn = DriverManager.getConnection("jdbc:oracle:thin:@fhictora01.fhict.local:1521:fhictora", "dbi294542", "vl4ldKvhy8");
           System.out.println("Connection open succeeded");
       }
       catch(SQLException ex)
       {
           System.out.println("Connection open failed: " + ex);
       }
       
   }
   private void closeConnection()
   {
       try{
           conn.close();
           System.out.println("Connection close succeeded");
       }
       catch(SQLException ex)
       {
           System.out.println("Connection close failed: " + ex);
       }
       
   }
}
