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
import Shared.Tasks.ITask;
import Shared.Tasks.Plan;
import Shared.Tasks.Step;
import Shared.Tasks.Task;
import Shared.Tasks.TaskStatus;
import Shared.Users.IHQChief;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import static javafx.collections.FXCollections.observableArrayList;
import javafx.collections.ObservableList;
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
    @FXML TextArea tapPlanDescription;
    @FXML TextArea tapKeyWords;
    @FXML ListView lvpTasks;
    @FXML TextField tfpTaskTitle;
    @FXML TextArea tapTaskDescription;
    @FXML TextField tfpCondition;
    
    // ApplyPlan
    @FXML Tab tabApplyPlan;
    @FXML TextField tfaDataTitle;
    @FXML TextArea taaDataDescription;
    @FXML TextField tfaSearch;
    @FXML ListView lvaPlans;
    @FXML ListView lvaSteps;
    @FXML TextField tfaTaskTitle;
    @FXML TextArea tfaTaskDescription;
    @FXML ComboBox cbaExecutor;
    
    // Tasks
    @FXML Tab tabTask;
    @FXML ListView lvtTasks;
    @FXML TextField tftTaskTitle;
    @FXML TextArea tatTaskDescription;
    @FXML TextField tftTitle;
    @FXML TextArea tatDescription;
    @FXML TextField tftExecutor;
    @FXML TextField tftReason;
    @FXML ComboBox cbtNewExecutor;
    
    private IData requestData;
    private IData sortedData;
    private ObservableList<IStep> tempSteps;
    private List<ITask> tempTasks;
    private IPlan tempPlan;
    
    private ConnectionManager connectionManager;
    private Timer timer;
    private IUser user = null;
    private Headquarters main;
    
    public void setApp(Headquarters application) {
        this.main = application;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.requestData = null;
        this.sortedData = null;
        
        // Add ChangeListeners
        lvuUnsortedData.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    selectUnsortedData();
                }                
            });
        
        lvsSortedData.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    selectSortedData();
                }                
            });
        
        lvsTasks.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    selectSortedTask();
                }                
            });
        
        lvpTasks.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    selectPlanTask();
                }                
            });
        
        lvaPlans.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    selectPlan();
                }                
            });
        
        lvaSteps.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    selectStep();
                }                
            });
        
        lvtTasks.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    selectTask();
                }                
            });
    }
    
    /**
     * Configures connectionManager and fills GUI with initial values
     * @param manager
     * @param user
     */
    public void configure(ConnectionManager manager, IUser user) {
        this.connectionManager = manager;
        try{
        this.connectionManager.setHQController(this);
        }
        catch(NetworkException nEx){
            Logger.getLogger(HeadquartersController.class.getName()).log(Level.SEVERE, null, nEx);
        }
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
        ccrTags.setLayoutY(328);
        ccrTags.prefWidth(395);
        ccrTags.prefHeight(25);
        ccrTags.setMaxSize(395, 25);
        aprPane.getChildren().add(ccrTags);
        
        tabProcessSortedData.setDisable(true);
        tabSendPlan.setDisable(true);
        tabApplyPlan.setDisable(true);
        
        if(user instanceof IHQChief){
            tabProcessSortedData.setDisable(false);
            tabSendPlan.setDisable(false);
            tabApplyPlan.setDisable(false);
        }
        
        try {
            if(this.connectionManager == null) {
                throw new NetworkException("Kon geen data ophalen");
            }
            this.connectionManager.getData();
            this.connectionManager.getSortedData();
            this.connectionManager.getServiceUsers();
                        
            //this.connectionManager.subscribeSortedData(this.user.getUsername());
            
            if(user instanceof IHQChief){
                connectionManager.subscribeTasks();
            }
            
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
            //this.connectionManager.unsubscribeSortedData(this.user.getUsername());
            
            if(user instanceof IHQChief){
                connectionManager.unsubscribeTasks();
            }
            
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
    
    // ProcessSortedData--------------------------------------------------------
    
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
     * Fills the ListView with sorted data and selects the first value
     * @param sortedData 
     */
    public void displaySortedDataTasks(List<ITask> tasks){
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvsTasks.getItems().addAll(tasks);
                if(lvsTasks.getSelectionModel().getSelectedItem() == null) {
                    lvsTasks.getSelectionModel().selectFirst();
                }
            }
            
        });
    }
    
    /**
     * Fills the ComboBoxes with service users and selects the first value
     * @param serviceUsers 
     */
    public void displayServiceUsers(List<IServiceUser> serviceUsers){
        Platform.runLater(new Runnable() {
            ObservableList<IServiceUser> observableSU = observableArrayList(serviceUsers);

            @Override
            public void run() {
                cbsExecutor.setItems(observableSU);
                if(cbsExecutor.getSelectionModel().getSelectedItem() == null) {
                    cbsExecutor.getSelectionModel().selectFirst();
                }
                
                cbaExecutor.setItems(observableSU);
                if(cbaExecutor.getSelectionModel().getSelectedItem() == null) {
                    cbaExecutor.getSelectionModel().selectFirst();
                }
                
                cbtNewExecutor.setItems(observableSU);
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
        ISortedData sortedData = 
                (ISortedData) lvsSortedData.getSelectionModel().getSelectedItem();
        if(sortedData != null) {
            // Fill GUI with information
            tfsSortedDataTitle.setText(sortedData.getTitle());
            tasSortedDataDescription.setText(sortedData.getDescription());
            tfsSource.setText(sortedData.getSource());
            tfsLocation.setText(sortedData.getLocation());
            displaySortedDataTasks(sortedData.getTasks());
            
        } else {
            // Clear GUI
            tfuTitle.clear();
            tasSortedDataDescription.clear();
            tfsSource.clear();
            tfsLocation.clear();
        }
    }
    
    /**
     * Fills the GUI with information of the selected task
     */
    public void selectSortedTask(){
        ITask task =
                (ITask) lvsTasks.getSelectionModel().getSelectedItem();
        if(task != null) {
            tfsTaskTitle.setText(task.getTitle());
            tasTaskDescription.setText(task.getDescription());
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
            
            if(cbsExecutor.getSelectionModel().getSelectedItem() != null)
                executor = (ServiceUser)cbsExecutor.getSelectionModel().getSelectedItem();
                
            Task task = new Task(1, title, description, TaskStatus.UNASSIGNED, (ISortedData) lvsSortedData.getSelectionModel().getSelectedItem(), executor.getType(), executor);
            connectionManager.sendTask(task);
            ISortedData data = (ISortedData) lvsSortedData.getSelectionModel().getSelectedItem();
            
            if(tempTasks == null)
                tempTasks = new ArrayList();
            
            tempTasks.add(task);
            
            data.setTasks(tempTasks);
            displaySortedDataTasks(tempTasks);
            
        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }
    
    /**
     * Go to tabApplyPlan tab
     */
    public void goToApplyPlan() {
        try {
            // Load values from GUI
            IData data = 
                (IData) lvsSortedData.getSelectionModel().getSelectedItem();
            if(data == null) {
                showDialog("Foutmelding", "Selecteer eerst een gesorteerd bericht", true);
            }
            // Load values into tabApplyPlan
            this.sortedData = data;
            tfaDataTitle.setText(data.getTitle());
            taaDataDescription.setText(data.getDescription());
            tabPane.getSelectionModel().select(tabApplyPlan);
        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        }
    }
    
    // SendPlan-----------------------------------------------------------------
    
    /**
     * Resets the tabPlanInfo
     */
    public void resetPlanInfo(){
        tempSteps.removeAll(tempSteps);
        lvpTasks.setItems(tempSteps);
        this.tempSteps = null;        
        tfpPlanTitle.clear();
        tapPlanDescription.clear();
        tapKeyWords.clear();
        tfpTaskTitle.clear();
        tapTaskDescription.clear();
        tfpCondition.clear();
    }
    
    /**
     * Fills the GUI with information of the selected task
     */
    public void selectPlanTask(){
        ITask task =
                (ITask) lvsTasks.getSelectionModel().getSelectedItem();
        if(task != null){
            tfsTaskTitle.setText(task.getTitle());
            tasTaskDescription.setText(task.getDescription());
            cbsExecutor.getSelectionModel().select(task.getExecutor());
        }
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
        else {
            tempSteps = observableArrayList();
            step = 1;
        }
        
        if(title != ""){
            tempSteps.add(new Step(step, title, description, TaskStatus.UNASSIGNED, null, null, null, step, condition));
            tfpTaskTitle.clear();
            tapTaskDescription.clear();
            tfpCondition.clear();
        } else
            showDialog("Foutmelding", "Vul een titel in.", true);
        
        lvpTasks.setItems(tempSteps);
    }
    
    public void removeTempStep(){
        IStep step = (IStep)lvpTasks.getSelectionModel().getSelectedItem();
        if(step != null){
            tempSteps.remove(step);
            lvpTasks.setItems(tempSteps);
        }
    }
    
    /**
     * Sends a plan to the server
     */
    public void sendPlan(){
        try{
            if(connectionManager == null){
                throw new NetworkException("Kon data niet wegschrijven");
            }
        
            if(tempSteps != null){
                String title = tfpPlanTitle.getText();
                String description = tapPlanDescription.getText();
                HashSet<String> keywords = new HashSet();
                
                String s = tapKeyWords.getText();
                String[] array = uniformString(s).split(" ");
                for(String word : array){
                    if(!word.isEmpty()){
                        keywords.add(word);
                    }                   
                }
                
                if(title != null) {
                    connectionManager.sendNewPlan(new Plan(1, title, description, keywords, tempSteps, false));
                    resetPlanInfo();
                } else {
                    showDialog("Foutmelding", "Voer een titel voor het stappenplan in", true);
                }
            } else {
                showDialog("Foutmelding", "Voeg stappen aan het stappenplan toe", true);
            }
        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }
    
    // ApplyPlan----------------------------------------------------------------
    public void resetApplyPlan(){
        lvaPlans.getItems().clear();
        lvaSteps.getItems().clear();
        tfaTaskTitle.clear();
        tfaTaskDescription.clear();
    }
    
    /**
     * Fill the ListView with plans
     * @param plans 
     */
    public void displayPlans(List<IPlan> plans){
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                resetApplyPlan();
                lvaPlans.getItems().addAll(plans);
                if(lvaPlans.getSelectionModel().getSelectedItem() == null) {
                    lvaPlans.getSelectionModel().selectFirst();
                }
            }
            
        });
    }
    
    /**
     * Fill the ListView with Steps
     */
    public void displaySteps(){
        tempPlan = (IPlan) lvaPlans.getSelectionModel().getSelectedItem();
        if(tempPlan != null){
            Platform.runLater(new Runnable() {
                IPlan p = (IPlan) lvaPlans.getSelectionModel().getSelectedItem();
                List<IStep> steps = new ArrayList();

                @Override
                public void run() {  
                    steps.addAll(p.getSteps());
                        
                    lvaSteps.getItems().addAll(steps);
                    if(lvaSteps.getSelectionModel().getSelectedItem() == null) {
                        lvaSteps.getSelectionModel().selectFirst();
                    }
                }
                
            });
        }
    }
    
    /**
     * Search for plans with similar keywords and display them in the listview.
     */
    public void searchPlan(){
        HashSet<String> keywords = new HashSet();
        
        String s = tfaSearch.getText();
        String[] array = uniformString(s).split(" ");
        for(String word : array){
            if(!word.isEmpty()){
                keywords.add(word);
            }          
        }
        
        resetApplyPlan();
        connectionManager.searchPlans(keywords);
    }
    
    /**
     * Fills the GUI with information of the selected plan
     */
    public void selectPlan(){
        IPlan plan =
                (IPlan) lvaPlans.getSelectionModel().getSelectedItem();
        if(plan != null){
            lvaSteps.getItems().clear();
            displaySteps();            
        }
    }
    
    public void applyPlan(){
        List<IStep> steps = tempPlan.getSteps();
        if(steps != null){
            boolean done = true;
            int step = 1;
            
            for(IStep s : steps){
                s.setStepnr(step);
                step++;
                if(s.getExecutor() == null)
                    done = false;
            }
            
            if(done){
                connectionManager.applyPlan(tempPlan);
            }
            else
                showDialog("Foutmelding", "Niet alle stappen hebben een uitvoerder", true);
        }
        else
            showDialog("Foutmelding", "Het plan heeft geen stappen", true);
    }
    
    /**
     * Fill GUI with Step properties
     */
    public void selectStep(){
        IStep s = (IStep) lvaSteps.getSelectionModel().getSelectedItem();
        if(s != null){
            tfaTaskTitle.setText(s.getTitle());
            tfaTaskDescription.setText(s.getDescription());
        } else {
            tfaTaskTitle.clear();
            tfaTaskDescription.clear();
        }
    }
    
    /**
     * Apply step to a service user and plan
     */
    public void applyStep(){
        IStep s = (IStep) lvaSteps.getSelectionModel().getSelectedItem();
        if(s != null){
            s.setExecutor((IServiceUser) cbaExecutor.getSelectionModel().getSelectedItem());        
            tempPlan.getSteps().add(s);
        } else {
            showDialog("Foutmelding", "Selecteer een stap voordat je een stap toekent.", true);
        }
    }
    
    /**
     * Remove step from plan
     */
    public void refuseStep(){
        IStep s = (IStep) lvaSteps.getSelectionModel().getSelectedItem();
        if(s != null){
            tempPlan.getSteps().remove(s);
        } else {
            showDialog("Foutmelding", "Selecteer een stap voordat je een stap verwijdert.", true);
        }
    }
    
    // Tasks--------------------------------------------------------------------
    public void displayTasks(List<ITask> tasks){
        Platform.runLater(new Runnable(){

            @Override
            public void run() {
                lvtTasks.getItems().addAll(tasks);
                if(lvtTasks.getSelectionModel().getSelectedItem() == null) {
                    lvtTasks.getSelectionModel().selectFirst();
                }
            }
            
        });
    }
    
    public void selectTask(){
        ITask task =
                (ITask) lvtTasks.getSelectionModel().getSelectedItem();
        if(task != null){
            tftTaskTitle.setText(task.getTitle());
            tatTaskDescription.setText(task.getDescription());
            tftTitle.setText(task.getTitle());
            tatDescription.setText(task.getDescription());
            tftExecutor.setText(task.getExecutor().getName());
            tftReason.setText(task.getDeclineReason());
        }
    }
    
    public void markAsRead(){
        //TODO
        ITask task = (ITask) lvtTasks.getSelectionModel().getSelectedItem();
        task.setStatus(TaskStatus.READ);
        
        connectionManager.updateTask(task);
    }
    
    public void updateTask(){
        //TODO
        ITask task = (ITask) lvtTasks.getSelectionModel().getSelectedItem();
        task.setExecutor((IServiceUser) cbtNewExecutor.getSelectionModel().getSelectedItem());
        
        connectionManager.updateTask(task);
    }
    
    public void logOutClick() {
        try {
            //log out connectionmanager
            this.close(true);
            main.goToLogIn();
        } catch (Exception ex) {
            Logger.getLogger(HeadquartersController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void close(boolean logout) {
        if (this.connectionManager != null) {
            this.connectionManager.unsubscribeSortedData();
            this.connectionManager.unsubscribeTasks();
        }
        if (!logout) {
            this.connectionManager.close();
        }
    }
    
    public void showDialog(String title, String melding, boolean warning)
    {
        Alert alert = null;
        
        if (warning)
        {
            alert = new Alert(AlertType.WARNING);
            alert.setTitle("Foutmelding");
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
    
    /**
     * Replaces every punctuation mark with a space. 
     * Sets everything to lowercase.
     * @param s
     * @return 
     */
    public String uniformString(String s){
        s.replace("\n", " ")
        .replace(",", " ")
        .replace(".", " ")
        .replace("!", " ")
        .replace("?", " ")
        .replace("  ", " ")
        .toLowerCase()
        .replace("é", "e");
        
        return s;
    }
}
