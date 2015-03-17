/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.UI;

import HeadquartersApp.Connection.ConnectionManager;
import HeadquartersApp.*;
import Shared.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 *
 * @author Alexander
 */
public class HeadquartersController implements Initializable {
    @FXML TabPane tabPane;
    
    // ProcessInfo
    @FXML Tab tabProcessInfo;
    @FXML ListView lvuUnsortedData;
    @FXML TextField tfuTitle;
    @FXML TextArea tauDescription;
    @FXML TextField tfuSource;
    @FXML TextField tfuLocation;
    @FXML ComboBox cbuTags;
    @FXML Slider suRelevance;
    @FXML Slider suReliability;
    @FXML Slider suQuality;
    
    // RequestInfo
    @FXML Tab tabRequestInfo;
    @FXML TextField tfrRequestTitle;
    @FXML TextField tfrTitle;
    @FXML TextArea tarDescription;
    @FXML TextField tfrSource;
    @FXML TextField tfrLocation;
    @FXML ComboBox cbrTags;
    
    private IData requestData;
    
    private ConnectionManager connectionManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.requestData = null;

    }
    
    public void configure(String ipAdressServer, int portnumber) {
        this.connectionManager = new ConnectionManager(this, ipAdressServer, portnumber);

        // Fill GUI with initial values
        cbuTags.setItems(FXCollections.observableArrayList(Tag.values()));
        cbuTags.getSelectionModel().selectFirst();
        this.connectionManager.getData();
    }
    
    /**
     * Displays the data that came from connectionManager.getData
     * @param output 
     */
    public void displayData(List<IData> output) {
        lvuUnsortedData.getItems().addAll(output);
    }
    
    /**
     * Fills the GUI with information of the selected unsorted data
     */
    public void selectUnsortedData() {
        IData unsortedData = 
                (IData) lvuUnsortedData.getSelectionModel().getSelectedItem();
        if(unsortedData != null) {
            // Fill GUI with information
            tfuTitle.setText(unsortedData.getTitle());
            tauDescription.setText(unsortedData.getDescription());
            tfuSource.setText(unsortedData.getSource());
            tfuLocation.setText(unsortedData.getLocation());
            // ComboBox checks weghalen
            //lvuTags.getItems().clear();
            cbuTags.getSelectionModel().selectFirst();
            suRelevance.setValue(suRelevance.getMin());
            suReliability.setValue(suReliability.getMin());
            suQuality.setValue(suQuality.getMin());
        }
    }
    
    /**
     * Add selected tag to the data
     */
    public void addTag() {
        // Nog via checkboxcombobox
//        try {
//            Tag tag = (Tag) cbuTags.getSelectionModel().getSelectedItem();
//            if(lvuTags.getItems().contains(tag)) {
//                throw new IllegalArgumentException("U heeft deze tag al toegevoegd");
//            }
//        } catch (IllegalArgumentException iaEx) {
//            System.out.println(iaEx.getMessage());
//        }
    }
    
    /**
     * Send the new sorted data to the server
     */
    public void sendSortedData() {
        try {
            // Load values from GUI
            IData unsortedData = 
                (IData) lvuUnsortedData.getSelectionModel().getSelectedItem();
            if(unsortedData == null) {
                throw new IllegalArgumentException("Selecteer eerst een "
                        + "unsorted data");
            }
            int id = unsortedData.getId();
            String title = tfuTitle.getText();
            String description = tauDescription.getText();
            String source = tfuSource.getText();
            String location = tfuLocation.getText();
            // Nog via checkboxCombobox
            //HashSet<Tag> tags = new HashSet<Tag>(lvuTags.getItems());
            HashSet<Tag> tags = new HashSet<Tag>();
            int relevance = (int) suRelevance.getValue();
            int reliability = (int) suReliability.getValue();
            int quality = (int) suQuality.getValue();
            
            // Make and send new sorted data
            ISortedData sortedData = new SortedData(id, title, description, 
                    location, source, relevance, reliability, quality, tags);
            this.connectionManager.sendSortedData(sortedData);
            
            // Remove old unsorted data
            lvuUnsortedData.getItems().remove(unsortedData);
            
            // If less than 10 items, load new unsorted data
            if(lvuUnsortedData.getItems().size() < 10) {
                this.connectionManager.getData();
            }
            lvuUnsortedData.getSelectionModel().selectFirst();
        } catch (IllegalArgumentException iaEx) {
            System.out.println(iaEx.getMessage());
        }
    }
    
    /**
     * Set status current data back to none
     */
    public void close() {
        if(this.connectionManager != null){
            this.connectionManager.stopWorkingOnData(
                    new ArrayList<>(lvuUnsortedData.getItems()));
        }
    }
    
    /**
     * Set status to discarded
     */
    public void discard() {
        try {
            // Load values from GUI
            IData unsortedData = 
                (IData) lvuUnsortedData.getSelectionModel().getSelectedItem();
            if(unsortedData == null) {
                throw new IllegalArgumentException("Selecteer eerst een "
                        + "unsorted data");
            }
            this.connectionManager.discardUnsortedData(unsortedData);
        } catch (IllegalArgumentException iaEx) {
            System.out.println(iaEx.getMessage());
        }
    }
    
    /**
     * Go to the tab RequestInfo
     */
    public void goToRequestInfo() {
        try {
            // Load values from GUI
            IData unsortedData = 
                (IData) lvuUnsortedData.getSelectionModel().getSelectedItem();
            if(unsortedData == null) {
                throw new IllegalArgumentException("Selecteer eerst een "
                        + "unsorted data");
            }
            // Load values into tabRequestInfo
            this.requestData = unsortedData;
            tfrRequestTitle.setText(this.requestData.getTitle());
            tabPane.getSelectionModel().select(tabRequestInfo);
            
        } catch (IllegalArgumentException iaEx) {
            System.out.println(iaEx.getMessage());
        }
    }
    
    /**
     * Sends a request for more data
     */
    public void sendRequest() {
        try {
            // Load values from GUI
            int requestId = -1;
            if(this.requestData != null) {
                requestId = requestData.getId();
            }
            String title = tfrTitle.getText();
            String description = tarDescription.getText();
            
            resetRequest();
        } catch (IllegalArgumentException iaEx) {
            System.out.println(iaEx.getMessage());
        }
        
    }
    
    /**
     * Resets the tabRequestInfo
     */
    public void resetRequest() {
        this.requestData = null;
        tfrRequestTitle.clear();
        tfrTitle.clear();
        tarDescription.clear();
        tfrSource.clear();
        tfrLocation.clear();
        // uncheck checkbox-thingy
    }
}
