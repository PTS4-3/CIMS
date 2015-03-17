/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.UI;

import ServicesApp.ConnectionManager;
import Shared.IData;
import Shared.Status;
import Shared.Tag;
import Shared.UnsortedData;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 *
 * @author Alexander
 */
public class ServicesController implements Initializable {
    @FXML TabPane tabPane;
    
    // SendInfo
    @FXML Tab tabSendInfo;
    @FXML TextField tfnTitle;
    @FXML TextArea tanDescription;
    @FXML TextField tfnSource;
    @FXML TextField tfnLocation;
    
    // UpdateInfo
    @FXML Tab tabUpdateInfo;
    @FXML ListView lvuSendData;
    @FXML TextField tfuTitle;
    @FXML TextArea tauDescription;
    @FXML TextField tfuSource;
    @FXML TextField tfuLocation;
    
    // ReadSortedData
    @FXML Tab tabReadSortedData;
    // tableView or something like that????
    @FXML CheckBox chbsData;
    @FXML CheckBox chbsRequests;
    @FXML TextField tfsTitle;
    @FXML TextArea tasDescription;
    @FXML TextField tfsSource;
    @FXML TextField tfsLocation;
    
    private ConnectionManager connectionManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
    /**
     * Configures connectionManager and fills GUI with initial values
     * @param ipAdressServer
     * @param portnumber 
     */
    public void configure(String ipAdressServer, int portnumber) {
        this.connectionManager = new ConnectionManager(this, ipAdressServer, portnumber);

        // Fill GUI with initial values
        //TODO
        //Load Send data
        //this.connectionManager.getSendData(new HashSet<Tag>());
        //Load sortedData + requests
    }
    
    /**
     * Maybe not needed??
     */
    public void close() {
        // TODO??
    }
    
    /**
     * Sends the unsortedData to the server
     */
    public void sendUnsortedData() {
        try {
            // Load values from GUI
            String title = tfnTitle.getText();
            String description = tanDescription.getText();
            String source = tfnSource.getText();
            String location = tfnLocation.getText();
            
            // Make and send new data
            IData data = new UnsortedData(-1, title, description, 
                    location, source, Status.NONE);
            this.connectionManager.sendUnsortedData(data);
            
            // Clear tab
            this.clearSendInfo();
        } catch(IllegalArgumentException iaEx) {
            System.out.println(iaEx.getMessage());
        }
    }
    
    /**
     * Clear the tab sendInfo
     */
    private void clearSendInfo() {
        tfnTitle.clear();
        tanDescription.clear();
        tfnSource.clear();
        tfnLocation.clear();
    }
    
    /**
     * Sends an update to the server
     */
    public void sendUpdate() {
        
    }
    
    /**
     * Resets the filter of sendData, all sendData becomes visible
     */
    public void resetSendData() {
        
    }
    
    /**
     * Changes the display of the sorted data
     * @param evt
     */
    public void changeDisplay(Event evt) {
        //evt.getSource() en dan kijken of die gecheckt is enzo
    }
    
    /**
     * Go to the tab sendUpdate
     */
    public void goToSendUpdate() {
        //this.connectionManager.getData(-1);
    }
}
