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
   
   /**
     * 
     */
   public DatabaseManager()
   {
       
   }
   /**
     * @param data object unsorteddata
     */
   public boolean insertToUnsortedData(UnsortedData data)
   {
       boolean succeed = false;
       try{
            openConnection();
            String query = "INSERT INTO UNSORTEDDATA VALUES (?,?,?,?,?,?)";
            PreparedStatement unsortedData = conn.prepareStatement(query);
            unsortedData.setString(2, data.getTitle());
            unsortedData.setString(3, data.getDescription());
            unsortedData.setString(4, data.getLocation());
            unsortedData.setString(5, data.getSource());
            //unsortedData.setString(6, data.getStatus());
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
   
   /**
     * 
     */
   public UnsortedData getFromUnsortedData()
   {
       return null;
   }
   
   /**
     * @param sorted object sorteddata
     * @param unsorted object unsorteddata
     */
   public boolean insertToSortedData(SortedData sorted, UnsortedData unsorted)
   {
       boolean succeed = false;
      try{
            openConnection();
            //insert to sorteddata
            String query = "INSERT INTO SORTEDDATA VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement sortedData = conn.prepareStatement(query);
            sortedData.setInt(1, sorted.getId());
            sortedData.setString(2, sorted.getTitle());
            sortedData.setString(3, sorted.getDescription());
            sortedData.setString(4, sorted.getLocation());
            sortedData.setString(5, sorted.getSource());
            sortedData.setInt(6, sorted.getRelevance());
            sortedData.setInt(7, sorted.getReliability());
            sortedData.setInt(8, sorted.getQuality());
            //unsortedData.setString(9, sorted.getStatus());
            sortedData.execute();
            
            System.out.println("Insert sortedData succeeded");
            
            //delete from unsorteddata
            query = "DELETE FROM UNSORTEDDATA WHERE ID = " + unsorted.getId();
            PreparedStatement unsortedData = conn.prepareStatement(query);
            unsortedData.execute();
            
            System.out.println("Delete from unsortedData succeeded");
            
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
   
   /**
     * 
     */
   public SortedData getFromSortedData()
   {
       return null;
   }
   
   /**
     * opening connection
     */
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
   /**
     * closing connection
     */
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
