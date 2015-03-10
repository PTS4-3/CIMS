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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
       try{
            openConnection();
            String query = "INSERT INTO UNSORTEDDATA VALUES (?,?,?,?,?)";
            PreparedStatement unsortedData = conn.prepareStatement(query);
            unsortedData.setString(2, data.getTitle());
            unsortedData.setString(3, data.getDescription());
            unsortedData.setString(4, data.getLocation());
            unsortedData.setString(5, data.getSource());
            unsortedData.execute();
            
            System.out.println("Insert unsortedData succeeded");
            succeed = true;
       }
       catch(SQLException ex)
       {
           System.out.println("Insert unsortedData failed: " + ex);
       }
       finally
       {
           closeConnection();
       }
       return succeed;
   }
   
   public UnsortedData getFromUnsortedData()
   {
       return null;
   }
   
   public boolean insertToSortedData(SortedData data)
   {
       boolean succeed = false;
      try{
            openConnection();
            String query = "INSERT INTO SORTEDDATA VALUES (?,?,?,?,?,?,?,?)";
            PreparedStatement unsortedData = conn.prepareStatement(query);
            unsortedData.setString(2, data.getTitle());
            unsortedData.setString(3, data.getDescription());
            unsortedData.setString(4, data.getLocation());
            unsortedData.setString(5, data.getSource());
            unsortedData.setInt(6, data.getRelevance());
            unsortedData.setInt(7, data.getReliability());
            unsortedData.setInt(8, data.getQuality());
            unsortedData.execute();
            
            System.out.println("Insert sortedData succeeded");
            succeed = true;
       }
       catch(SQLException ex)
       {
           System.out.println("Insert sortedData failed: " + ex);
       }
      finally
       {
           closeConnection();
       }
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
