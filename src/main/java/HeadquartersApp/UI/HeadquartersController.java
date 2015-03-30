/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.UI;

import HeadquartersApp.*;
import HeadquartersApp.Connection.ConnectionManager;
import Shared.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.CheckComboBox;

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
    @FXML Slider suRelevance;
    @FXML Slider suReliability;
    @FXML Slider suQuality;
    @FXML AnchorPane apuPane;
    @FXML CheckComboBox ccuTags;
    
    // RequestInfo
    @FXML Tab tabRequestInfo;
    @FXML TextField tfrRequestTitle;
    @FXML TextField tfrTitle;
    @FXML TextArea tarDescription;
    @FXML TextField tfrSource;
    @FXML TextField tfrLocation;
    @FXML AnchorPane aprPane;
    @FXML CheckComboBox ccrTags;    
    
    private IData requestData;
    
    private ConnectionManager connectionManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.requestData = null;
        
        // Add ChangeListeners
        lvuUnsortedData.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                selectUnsortedData();
            }
                
            });
    }
    
    /**
     * Configures connectionManager and fills GUI with initial values
     * @param ipAdressServer 
     */
    public void configure(String ipAdressServer) {
        this.connectionManager = new ConnectionManager(this, ipAdressServer);

        // Fill GUI with initial values        
        ccuTags = new CheckComboBox(FXCollections.observableArrayList(Tag.values()));
        ccuTags.setLayoutX(955);
        ccuTags.setLayoutY(20);
        ccuTags.prefWidth(220);
        ccuTags.prefHeight(25);       
        ccuTags.setMaxSize(220, 25);
        apuPane.getChildren().add(ccuTags);
        
        ccrTags = new CheckComboBox(FXCollections.observableArrayList(Tag.values()));
        ccrTags.setLayoutX(173);
        ccrTags.setLayoutY(374);
        ccrTags.prefWidth(395);
        ccrTags.prefHeight(25);
        ccrTags.setMaxSize(395, 25);
        aprPane.getChildren().add(ccrTags);
        
        try {
            if(this.connectionManager == null) {
                throw new NetworkException("Kon geen data ophalen");
            }
            this.connectionManager.getData();
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }
    
    /**
     * Displays the data that came from connectionManager.getData
     * @param output 
     */
    public void displayData(List<IData> output) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvuUnsortedData.getItems().addAll(output);
                if(lvuUnsortedData.getSelectionModel().getSelectedItem() == null) {
                    lvuUnsortedData.getSelectionModel().selectFirst();
                }
            }
            
        });
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
        } else {
            // Clear GUI
            tfuTitle.clear();
            tauDescription.clear();
            tfuSource.clear();
            tfuLocation.clear();
        }
        ccuTags.getCheckModel().clearChecks();
        suRelevance.setValue(suRelevance.getMin());
        suReliability.setValue(suReliability.getMin());
        suQuality.setValue(suQuality.getMin());
    }
    
    /**
     * Remove old data and select new
     * @param unsortedData 
     */
    private void updateLvuUnsortedData(IData unsortedData) {
        // Remove old unsorted data
        lvuUnsortedData.getItems().remove(unsortedData);

        // If less than 10 items, load new unsorted data
        if(lvuUnsortedData.getItems().size() < 10) {
            this.connectionManager.getData();
        }

        // Select new
        lvuUnsortedData.getSelectionModel().selectFirst();
    }
    
    /**
     * Send the new sorted data to the server
     */
    public void sendSortedData() {
        try {
            if(this.connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }
            
            // Load values from GUI
            IData unsortedData = 
                (IData) lvuUnsortedData.getSelectionModel().getSelectedItem();
            if(unsortedData == null) {
                throw new IllegalArgumentException("Selecteer eerst een "
                        + "ongesorteerd bericht");
            }
            int id = unsortedData.getId();
            String title = tfuTitle.getText();
            String description = tauDescription.getText();
            String source = tfuSource.getText();
            String location = tfuLocation.getText();
            HashSet<Tag> tags = new HashSet<>(ccuTags.getCheckModel().getCheckedItems());
            int relevance = (int) suRelevance.getValue();
            int reliability = (int) suReliability.getValue();
            int quality = (int) suQuality.getValue();
            
            // Make and send new sorted data
            ISortedData sortedData = new SortedData(id, title, description, 
                    location, source, relevance, reliability, quality, tags);
            this.connectionManager.sendSortedData(sortedData);
            
            // Update ListView
            this.updateLvuUnsortedData(unsortedData);
            
            // Bevestiging tonen
            showDialog("Verzenden geslaagd", "Het verzenden van de gesorteerde " +
                    "data is geslaagd", false);
        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }
    
    /**
     * Set status current data back to none
     */
    public void close() {
        if(this.connectionManager != null){
            this.connectionManager.stopWorkingOnData(
                    new ArrayList<>(lvuUnsortedData.getItems()));
            this.connectionManager.close();
        }
    }
    
    /**
     * Set status to discarded
     */
    public void discard() {
        try {
            if(this.connectionManager == null) {
                throw new NetworkException("Kon data niet verwijderen");
            }
            
            // Load values from GUI
            IData unsortedData = 
                (IData) lvuUnsortedData.getSelectionModel().getSelectedItem();
            if(unsortedData == null) {
                throw new IllegalArgumentException("Selecteer eerst een "
                        + "ongesorteerd bericht");
            }
            
            // Discard data
            this.connectionManager.discardUnsortedData(unsortedData);
            
            // Update ListView
            this.updateLvuUnsortedData(unsortedData);
            
            // Bevestiging tonen
            showDialog("Verwijderen geslaagd", "Het verwijderen van de ongesorteerde " +
                    "data is geslaagd", false);
        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
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
                        + "ongesorteerd bericht");
            }
            // Load values into tabRequestInfo
            this.requestData = unsortedData;
            tfrRequestTitle.setText(this.requestData.getTitle());
            tabPane.getSelectionModel().select(tabRequestInfo);
        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        }
    }
    
    /**
     * Sends a request for more data
     */
    public void sendRequest() {
        try {
            if(this.connectionManager == null) {
                throw new NetworkException("Kon verzoek niet versturen");
            }
            
            // Load values from GUI
            int requestId = -1;
            if(this.requestData != null) {
                requestId = requestData.getId();
            }
            String title = tfrTitle.getText();
            String description = tarDescription.getText();
            String source = tfrSource.getText();
            String location = tfrLocation.getText();
            HashSet<Tag> tags = new HashSet<>(ccrTags.getCheckModel().getCheckedItems());
            
            // Make and send request
            IDataRequest request = new DataRequest(-1, title, description, 
                    location, source, requestId, tags);
            this.connectionManager.requestUpdate(request);
            
            // Update ListView
            this.updateLvuUnsortedData(requestData);
            
            // Reset tab
            resetRequest();
            
            // Bevestiging tonen
            showDialog("Verzenden geslaagd", "Het verzenden van de aanvraag " +
                    "is geslaagd", false);
        } catch (IllegalArgumentException iaEx) {
            showDialog("Invoer onjuist", iaEx.getMessage(), true);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
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
        ccrTags.getCheckModel().clearChecks();
    }
    
    public void showDialog(String title, String melding, boolean warning)
    {
        Alert alert = null;
        
        if (warning)
        {
            alert = new Alert(AlertType.WARNING);
            alert.setTitle("FoutMelding");
        }
        else
        {
            alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Melding");            
        }     
        
        if (!title.isEmpty())
        {
            alert.setHeaderText(title);
        }
        else
        {
            alert.setHeaderText(null);
        }
        
        alert.setContentText(melding);
        alert.showAndWait();
    }
}
