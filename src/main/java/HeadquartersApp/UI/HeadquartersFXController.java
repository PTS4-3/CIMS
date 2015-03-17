/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.UI;

import HeadquartersApp.*;
import Shared.*;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

/**
 *
 * @author Alexander
 */
public class HeadquartersFXController implements Initializable {
    // GUI Components
    @FXML ListView lvUnsortedData;
    @FXML TextField tfTitle;
    @FXML TextField tfDescription;
    @FXML TextField tfSource;
    @FXML TextField tfLocation;
    @FXML ListView lvTags;
    @FXML ComboBox cbTags;
    @FXML Slider sRelevance;
    @FXML Slider sReliability;
    @FXML Slider sQuality;
    
    private ConnectionManager connectionManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }
    
    public void configure(String ipAdressServer, int portnumber) {
        this.connectionManager = new ConnectionManager(ipAdressServer, portnumber);
        
        // Fill GUI with initial values
        cbTags.setItems(FXCollections.observableArrayList(Tag.values()));
        cbTags.getSelectionModel().selectFirst();
        sRelevance.setValue(sRelevance.getMax() / 2);
        sReliability.setValue(sReliability.getMax() / 2);
        sQuality.setValue(sQuality.getMax() / 2);
        lvUnsortedData.setItems(
                FXCollections.observableList(connectionManager.getData()));
    }
    
    /**
     * Fills the GUI with information of the selected unsorted data
     */
    public void selectUnsortedData() {
        IData unsortedData = 
                (IData) lvUnsortedData.getSelectionModel().getSelectedItem();
        if(unsortedData != null) {
            // Fill GUI with information
            tfTitle.setText(unsortedData.getTitle());
            tfDescription.setText(unsortedData.getDescription());
            tfSource.setText(unsortedData.getSource());
            tfLocation.setText(unsortedData.getLocation());
            lvTags.getItems().clear();
            cbTags.getSelectionModel().selectFirst();
            sRelevance.setValue(sRelevance.getMax() / 2);
            sReliability.setValue(sReliability.getMax() / 2);
            sQuality.setValue(sQuality.getMax() / 2);
        }
    }
    
    /**
     * Add selected tag to the data
     */
    public void addTag() {
        try {
            Tag tag = (Tag) cbTags.getSelectionModel().getSelectedItem();
            if(lvTags.getItems().contains(tag)) {
                throw new IllegalArgumentException("U heeft deze tag al toegevoegd");
            }
        } catch (IllegalArgumentException iaEx) {
            System.out.println(iaEx.getMessage());
        }
    }
    
    /**
     * Send the new sorted data to the server
     */
    public void sendSortedData() {
        try {
            // Load values from GUI
            IData unsortedData = 
                (IData) lvUnsortedData.getSelectionModel().getSelectedItem();
            if(unsortedData == null) {
                throw new IllegalArgumentException("Selecteer eerst een "
                        + "unsorted data");
            }
            int id = unsortedData.getId();
            String title = tfTitle.getText();
            String description = tfDescription.getText();
            String source = tfSource.getText();
            String location = tfLocation.getText();
            HashSet<Tag> tags = new HashSet<Tag>(lvTags.getItems());
            int relevance = (int) sRelevance.getValue();
            int reliability = (int) sReliability.getValue();
            int quality = (int) sQuality.getValue();
            
            // Make and send new sorted data
            ISortedData sortedData = new SortedData(id, title, description, 
                    location, source, relevance, reliability, quality, tags);
            this.connectionManager.sendSortedData(sortedData);
            
            // Remove old unsorted data
            lvUnsortedData.getItems().remove(unsortedData);
            
            // If less than 10 items, load new unsorted data
            if(lvUnsortedData.getItems().size() < 10) {
                lvUnsortedData.getItems().addAll(connectionManager.getData());
            }
            lvUnsortedData.getSelectionModel().selectFirst();
        } catch (IllegalArgumentException iaEx) {
            System.out.println(iaEx.getMessage());
        }
    }
    
    /**
     * Set status current data back to none
     */
    public void close() {
        connectionManager.stopWorkingOnData(lvUnsortedData.getItems());
    }
    
    /**
     * Set status to discarded
     */
    public void discard() {
        try {
            // Load values from GUI
            IData unsortedData = 
                (IData) lvUnsortedData.getSelectionModel().getSelectedItem();
            if(unsortedData == null) {
                throw new IllegalArgumentException("Selecteer eerst een "
                        + "unsorted data");
            }
            this.connectionManager.discardUnsortedData(unsortedData);
        } catch (IllegalArgumentException iaEx) {
            System.out.println(iaEx.getMessage());
        }
    }
    
    public void goToSendRequest() {
        
    }
}
