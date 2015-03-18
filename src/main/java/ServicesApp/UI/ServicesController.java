/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.UI;

import ServicesApp.Connection.ConnectionManager;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import Shared.NetworkException;
import Shared.Status;
import Shared.Tag;
import Shared.UnsortedData;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
    @FXML Button btnAnswerRequest;
    
    private ConnectionManager connectionManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
    /**
     * Configures connectionManager and fills GUI with initial values
     * @param ipAdressServer
     */
    public void configure(String ipAdressServer) {
        this.connectionManager = new ConnectionManager(this, ipAdressServer);

        // Fill GUI with initial values
        // Tags nog invullen?
        try {
            if(this.connectionManager == null) {
                throw new NetworkException("Geen verbinding met server: "
                        + "Kon geen data ophalen");
            }
            this.connectionManager.getSentData(null);
            if(chbsRequests.isSelected()) {
                this.connectionManager.getRequests(null);
            }
            if(chbsData.isSelected()) {
                this.connectionManager.getSortedData(null);
            }
        } catch (NetworkException nEx) {
            System.out.println(nEx.getMessage());
        }
    }
    
    /**
     * Displays the sendData that came from connectionManager.getSendData
     * @param sendData
     */
    public void displaySendData(List<IData> sendData) {
        lvuSendData.setItems(FXCollections.observableList(sendData));
        if(lvuSendData.getSelectionModel().getSelectedItem() == null) {
            lvuSendData.getSelectionModel().selectFirst();
        }
    }
    
    /**
     * Displays the requests that came from connectionManager.getRequests
     * @param requests
     */
    public void displayRequests(List<IDataRequest> requests) {
        // add requests to TableView TODO
        // if not selected, select first
    }
    
    /**
     * Displays the sortedData that came from connectionManager.getSortedData
     * @param sortedData
     */
    public void displaySortedData(List<ISortedData> sortedData) {
        // add sortedData to TableView TODO
        // if not selected, select first
    }
    
    /**
     * Displays the requestData that came from connectionManager.getData
     * @param requestData 
     */
    public void displayRequestData(IData requestData) {
        lvuSendData.getItems().clear();
        lvuSendData.getItems().add(requestData);
        lvuSendData.getSelectionModel().selectFirst();
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
            if(this.connectionManager == null) {
                throw new NetworkException("Geen verbinding met server: "
                        + "Kon data niet wegschrijven");
            }
            
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
        } catch(NetworkException nEx) {
            System.out.println(nEx.getMessage());
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
     * Fills the GUI with information of the selected SendData
     */
    public void selectSendData() {
        IData sendData = (IData) lvuSendData.getSelectionModel().getSelectedItem();
        if(sendData != null) {
            tfuTitle.setText(sendData.getTitle());
            tauDescription.setText(sendData.getDescription());
            tfuSource.setText(sendData.getSource());
            tfuLocation.setText(sendData.getLocation());
        }
    }
    
    /**
     * Sends an update to the server
     */
    public void sendUpdate() {
        try {
            if(this.connectionManager == null) {
                throw new NetworkException("Geen verbinding met server: "
                        + "Kon data niet wegschrijven");
            }
            
            // Load values from GUI
            IData sendData = (IData) lvuSendData.getSelectionModel().getSelectedItem();
            String title = tfuTitle.getText();
            String description = tauDescription.getText();
            String source = tfuSource.getText();
            String location = tfuLocation.getText();
            
            // Make and send update
            IData update = new UnsortedData(sendData.getId(), title, 
                    description, location, source, Status.NONE);
            this.connectionManager.updateUnsortedData(update);
            
            // Bevestiging tonen TODO
        } catch (IllegalArgumentException iaEx) {
            System.out.println(iaEx.getMessage());
        } catch (NetworkException nEx) {
            System.out.println(nEx.getMessage());
        }
    }
    
    /**
     * Resets the filter of sendData, all sendData becomes visible
     */
    public void resetSendData() {
        try {
            if(this.connectionManager == null) {
                throw new NetworkException("Geen verbinding met server: "
                        + "Kon geen data ophalen");
            }
            
            // Clear sendData
            lvuSendData.getItems().clear();
            
            // TODO tags
            this.connectionManager.getSentData(new HashSet<Tag>());
        } catch (NetworkException nEx) {
            System.out.println(nEx.getMessage());
        }
    }
    
    /**
     * Fills the GUI with the information of the selected data
     */
    public void selectData() {
        //TODO get selected from TableView
        IData data = null;
        if(data != null) {
            // Fill GUI with information
            tfsTitle.setText(data.getTitle());
            tasDescription.setText(data.getDescription());
            tfsSource.setText(data.getSource());
            tfsLocation.setText(data.getLocation());
            
            // Determine visibility button requests
            btnAnswerRequest.setVisible(data instanceof IDataRequest);
        }
    }
    
    /**
     * Changes the display of the sorted data
     * @param evt
     */
    public void changeDisplay(Event evt) {
        CheckBox source = (CheckBox) evt.getSource();
        if(source.isSelected()) {
            try {
                if(this.connectionManager == null) {
                    throw new NetworkException("Geen verbinding met server: "
                            + "Kon geen data ophalen");
                }
                
                // Add
                if(source == chbsData) {
                    //TODO tags
                    this.connectionManager.getSortedData(new HashSet<Tag>());
                } else if (source == chbsRequests) {
                    //TODO tags
                    this.connectionManager.getRequests(new HashSet<Tag>());
                }
            } catch (NetworkException nEx) {
                System.out.println(nEx.getMessage());
            }
        } else {
            // Remove TODO
            if(source == chbsData) {
                // Remove sortedData from TableView TODO
            } else if (source == chbsRequests) {
                // Remove dataRequest from TableView TODO
            }
        }
    }
    
    /**
     * Go to the tab sendUpdate
     */
    public void goToSendUpdate() {
        try {           
            //TODO get selected from TableView
            IDataRequest request = null;
            
            if(request != null) {
                if(request.getRequestId() == -1) {
                    // new data
                    tabPane.getSelectionModel().select(tabSendInfo);
                } else {
                    // update
                    if(this.connectionManager == null) {
                        throw new NetworkException("Geen verbinding met server: "
                                + "Kon data niet ophalen");
                    }
                    
                    this.connectionManager.getDataItem(request.getRequestId());
                    tabPane.getSelectionModel().select(tabUpdateInfo);
                }
            }
        } catch (NetworkException nEx) {
            System.out.println(nEx.getMessage());
        }
    }

    public void displaySentData(List<IData> output) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void displayDataItem(IData output) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
