/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.UI;

import HeadquartersApp.Connection.ConnectionManager;
import Shared.*;
import Shared.Data.DataRequest;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.Data.SortedData;
import Shared.Tasks.IPlan;
import Shared.Tasks.IStep;
import Shared.Tasks.Plan;
import Shared.Tasks.Step;
import Shared.Tasks.Task;
import Shared.Tasks.TaskStatus;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import Shared.Users.ServiceUser;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
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
    
    // ProcessSortedData
    @FXML Tab tabProcessSortedData;
    @FXML ListView lvsSortedData;
    @FXML TextField tfsSortedDataTitle;
    @FXML TextArea tasSortedDataDescription;
    @FXML TextField tfsSource;
    @FXML TextField tfsLocation;
    @FXML ListView lvsTasks;
    @FXML TextField tfsTaskTitle;
    @FXML TextArea tasTaskDescription;
    @FXML ComboBox cbsExecutor;
    
    // SendPlan
    @FXML Tab tabSendPlan;
    @FXML TextField tfpPlanTitle;
    @FXML TextField tapPlanDescription;
    @FXML TextArea tapKeyWords;
    @FXML ListView lvpTasks;
    @FXML TextField tfpTaskTitle;
    @FXML TextArea tapTaskDescription;
    @FXML TextField tfpCondition;
    
    // ApplyPlan
    @FXML Tab tabApplyPlan;
    @FXML TextField tfaPlanTitle;
    @FXML TextArea taaPlanDescription;
    @FXML TextField tfaSearch;
    @FXML ListView lvaPlans;
    @FXML ListView lvaSteps;
    @FXML TextField tfaTaskTitle;
    @FXML TextArea tfaTaskDescription;
    @FXML ComboBox cbaExecutor;
    
    private IData requestData;
    private List<IStep> tempSteps;
    
    private ConnectionManager connectionManager;
    private Timer timer;
    private IUser user = null;
    private Headquarters main;
    
    public void setApp(Headquarters application)
    {
        this.main = application;
    }

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
    public void configure(ConnectionManager manager, IUser user) {
        this.connectionManager = manager;
        this.connectionManager.setHQController(this);
        this.user = user;

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
            this.startTimer();        
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
                if(timer != null && !output.isEmpty()) {
                    timer.cancel();
                    timer = null;
                }
                
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
            
            // Start timer
            if(lvuUnsortedData.getItems().size() == 0) {
                this.startTimer();
            }
        }

        // Select new
        lvuUnsortedData.getSelectionModel().selectFirst();
    }
    
    /**
     * Starts the timer for getting data 
     */
    private void startTimer() {
        if(this.timer == null) {
            this.timer = new Timer();
            this.timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    connectionManager.getData();
                }

            }, 10000, Long.valueOf(10000));
        }
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
        if(this.timer != null) {
            this.timer.cancel();
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
    
    /**
     * Fills the ListView with sorted data and selects the first value
     * @param sortedData 
     */
    public void displaySortedData(List<ISortedData> sortedData){
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvsSortedData.getItems().addAll(sortedData);
                if(lvsSortedData.getSelectionModel().getSelectedItem() == null) {
                    lvsSortedData.getSelectionModel().selectFirst();
                }
            }
            
        });
    }
    
    /**
     * Fills the ComboBox with service users and selects the first value
     * @param serviceUsers 
     */
    public void displayServiceUsers(List<IServiceUser> serviceUsers){
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                cbsExecutor.getItems().addAll(serviceUsers);
                if(cbsExecutor.getSelectionModel().getSelectedItem() == null) {
                    cbsExecutor.getSelectionModel().selectFirst();
                }
                
                cbaExecutor.getItems().addAll(serviceUsers);
                if(cbaExecutor.getSelectionModel().getSelectedItem() == null) {
                    cbaExecutor.getSelectionModel().selectFirst();
                }
            }
            
        });
    }
    
    /**
     * Fills the GUI with information of the selected sorted data
     */
    public void selectSortedData() {
        IData sortedData = 
                (IData) lvsSortedData.getSelectionModel().getSelectedItem();
        if(sortedData != null) {
            // Fill GUI with information
            tfsSortedDataTitle.setText(sortedData.getTitle());
            tasSortedDataDescription.setText(sortedData.getDescription());
            tfsSource.setText(sortedData.getSource());
            tfsLocation.setText(sortedData.getLocation());
        } else {
            // Clear GUI
            tfuTitle.clear();
            tasSortedDataDescription.clear();
            tfsSource.clear();
            tfsLocation.clear();
        }
    }
    
    /**
     * Send individual task to server
     */
    public void sendTask(){
        try{
            if(connectionManager == null){
                throw new NetworkException("Kon data niet wegschrijven");
            }
            
            String title = tfsTaskTitle.getText();
            String description = tasTaskDescription.getText();
            ServiceUser executor = null;
            
            if(cbsExecutor.getValue() != null)
                executor = (ServiceUser)cbsExecutor.getValue();
                

            connectionManager.sendTask(new Task(1, title, description, TaskStatus.UNASSIGNED, (ISortedData) lvsSortedData.getSelectionModel().getSelectedItem(), executor.getType(), executor));
        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }
    
    /**
     * Resets the tabPlanInfo
     */
    public void resetPlan(){
        lvpTasks.getItems().remove(tempSteps);
        this.tempSteps = null;        
        tfpPlanTitle.clear();
        tapPlanDescription.clear();
        tapKeyWords.clear();
        tfpTaskTitle.clear();
        tapTaskDescription.clear();
        tfpCondition.clear();
    }
    
    /**
     *  Adds a temporary step to the list of steps in a plan
     */
    public void addTempStep(){
        String title = tfpTaskTitle.getText();
        String description = tapTaskDescription.getText();
        String condition = tfpCondition.getText();
        int step;
        
        if(tempSteps != null)
            step = tempSteps.size() + 1;
        else
            step = 1;
        
        tempSteps.add(new Step(step, title, description, TaskStatus.UNASSIGNED, null, null, null, step, condition));
    }
    
    /**
     * Sends a plan to the server
     */
    public void sendPlan(){
        try{
            if(connectionManager == null){
                throw new NetworkException("Kon data niet wegschrijven");
            }
        
            String title = tfpPlanTitle.getText();
            String description = tapPlanDescription.getText();
            HashSet<String> keywords = null;
            TreeSet<IStep> steps = null;

            for(IStep s : tempSteps){
                steps.add(s);
            }

            connectionManager.sendNewPlan(new Plan(1, title, description, keywords, steps, false));
        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }
    
    public void displayPlans(List<IPlan> plans){
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvaPlans.getItems().addAll(plans);
                if(lvaPlans.getSelectionModel().getSelectedItem() == null) {
                    lvaPlans.getSelectionModel().selectFirst();
                }
            }
            
        });
    }
    
    public void displaySteps(){
        Platform.runLater(new Runnable() {
            IPlan p = (IPlan) lvaPlans.getSelectionModel().getSelectedItem();
            List<IStep> steps = null;
            
            @Override
            public void run() {                
                for(IStep s : p.getSteps()){
                    steps.add(s);
                }
                lvaSteps.getItems().addAll(steps);
                if(lvaSteps.getSelectionModel().getSelectedItem() == null) {
                    lvaSteps.getSelectionModel().selectFirst();
                }
            }
            
        });
    }
    
    public void selectStep(){
        IStep step = (IStep) lvaSteps.getSelectionModel().getSelectedItem();
        
        tfaTaskTitle.setText(step.getTitle());
        tfaTaskDescription.setText(step.getDescription());
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
