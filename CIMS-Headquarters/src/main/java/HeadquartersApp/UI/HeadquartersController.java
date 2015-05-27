/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.UI;

import HeadquartersApp.Connection.ConnectionHandler;
import Shared.*;
import Shared.Data.DataRequest;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.INewsItem;
import Shared.Data.ISortedData;
import Shared.Data.NewsItem;
import Shared.Data.Situation;
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
import Shared.Users.UserRole;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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

    @FXML
    TabPane tabPane;

    // ProcessInfo
    @FXML
    Tab tabProcessInfo;
    @FXML
    ListView lvuUnsortedData;
    @FXML
    TextField tfuTitle;
    @FXML
    TextArea tauDescription;
    @FXML
    TextField tfuSource;
    @FXML
    TextField tfuLocation;
    @FXML
    Slider suRelevance;
    @FXML
    Slider suReliability;
    @FXML
    Slider suQuality;
    @FXML
    AnchorPane apuPane;
    @FXML
    CheckComboBox ccuTags;
    @FXML
    Label lblUnsortedReport;

    // RequestInfo
    @FXML
    Tab tabRequestInfo;
    @FXML
    TextField tfrRequestTitle;
    @FXML
    TextField tfrTitle;
    @FXML
    TextArea tarDescription;
    @FXML
    TextField tfrSource;
    @FXML
    TextField tfrLocation;
    @FXML
    AnchorPane aprPane;
    @FXML
    CheckComboBox ccrTags;
    @FXML
    Label lblInformationReport;

    // SendNewsItems
    @FXML
    Tab tabNews;
    @FXML
    ListView lvnSorted;
    @FXML
    TextField tfnTitleSorted;
    @FXML
    TextArea tanDescriptionSorted;
    @FXML
    TextField tfnSourceSorted;
    @FXML
    TextField tfnLocationSorted;
    @FXML
    TextField tfnTitle;
    @FXML
    TextArea tanDescription;
    @FXML
    TextField tfnLocation;
    @FXML
    TextField tfnVictims;
    @FXML
    CheckComboBox ccnSituations;
    @FXML
    Label lblnMessage;
    @FXML
    AnchorPane apnPane;

    // UpdateNewsItems
    @FXML
    Tab tabNewsUpdate;
    @FXML
    ListView lvbNews;
    @FXML
    TextField tfbTitle;
    @FXML
    TextArea tabDescription;
    @FXML
    TextField tfbLocation;
    @FXML
    TextField tfbVictims;
    @FXML
    CheckComboBox ccbSituations;
    @FXML
    Label lblbMessage;
    @FXML
    AnchorPane apbPane;

    // ProcessSortedData
    @FXML
    Tab tabProcessSortedData;
    @FXML
    ListView lvsSortedData;
    @FXML
    TextField tfsSortedDataTitle;
    @FXML
    TextArea tasSortedDataDescription;
    @FXML
    TextField tfsSource;
    @FXML
    TextField tfsLocation;
    @FXML
    ListView lvsTasks;
    @FXML
    TextField tfsTaskTitle;
    @FXML
    TextArea tasTaskDescription;
    @FXML
    ComboBox cbsExecutor;
    @FXML
    Label lblProcessSortedData;
    @FXML
    Button btnAddSortedTask;
    @FXML
    Button btnAddRoadMap;

    // SendPlan
    @FXML
    Tab tabSendPlan;
    @FXML
    TextField tfpPlanTitle;
    @FXML
    TextArea tapPlanDescription;
    @FXML
    TextArea tapKeyWords;
    @FXML
    ListView lvpTasks;
    @FXML
    TextField tfpTaskTitle;
    @FXML
    TextArea tapTaskDescription;
    @FXML
    ComboBox cbExecutor;
    @FXML
    TextField tfpCondition;
    @FXML
    Label lblSendPlan;

    // ApplyPlan
    @FXML
    Tab tabApplyPlan;
    @FXML
    TextField tfaDataTitle;
    @FXML
    TextArea taaDataDescription;
    @FXML
    TextField tfaSearch;
    @FXML
    ListView lvaPlans;
    @FXML
    ListView lvaSteps;
    @FXML
    TextField tfaTaskTitle;
    @FXML
    TextArea tfaTaskDescription;
    @FXML
    TextField tfaTaskCondition;
    @FXML
    ComboBox cbaExecutor;
    @FXML
    Label lblApplyPlan;

    // Tasks
    @FXML
    Tab tabTask;
    @FXML
    ListView lvtTasks;
    @FXML
    TextField tftTaskTitle;
    @FXML
    TextArea tatTaskDescription;
    @FXML
    TextField tftTitle;
    @FXML
    TextArea tatDescription;
    @FXML
    TextField tftExecutor;
    @FXML
    TextField tftReason;
    @FXML
    ComboBox cbtNewExecutor;
    @FXML
    Label lblTasks;
    @FXML
    Button btnNewTask;

    private IData requestData;
    private ISortedData sortedData;
    private ObservableList<IStep> tempSteps;
    private IPlan tempPlan;

    private ConnectionHandler connectionManager;
    private IUser user = null;
    private Headquarters main;

    private HashMap<Integer, Task> displayedTasks;

    public void setApp(Headquarters application) {
        this.main = application;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.requestData = null;
        this.sortedData = null;
        this.displayedTasks = new HashMap<>();

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

        lvnSorted.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        selectNewsSorted();
                    }
                });

        lvbNews.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        selectNewsItem();
                    }
                });
    }

    /**
     * Configures connectionManager and fills GUI with initial values
     *
     * @param manager
     * @param user
     */
    public void configure(ConnectionHandler manager, IUser user) {
        this.connectionManager = manager;
        try {
            this.connectionManager.setHQController(this);
        } catch (NetworkException nEx) {
            Logger.getLogger(HeadquartersController.class.getName()).log(Level.SEVERE, null, nEx);
        }
        this.user = user;
        tfrSource.setText(this.user.getUsername());

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

        this.cbExecutor.setItems(FXCollections.observableArrayList(Tag.values()));
        this.cbExecutor.getItems().remove(Tag.CITIZENS);
        if (this.cbExecutor.getSelectionModel().getSelectedItem() == null) {
            this.cbExecutor.getSelectionModel().selectFirst();
        }

        tabProcessSortedData.setDisable(true);
        tabSendPlan.setDisable(true);
        tabApplyPlan.setDisable(true);
        tabTask.setDisable(true);

        lblUnsortedReport.setVisible(false);
        lblInformationReport.setVisible(false);
        lblProcessSortedData.setVisible(false);
        lblSendPlan.setVisible(false);
        lblApplyPlan.setVisible(false);
        lblTasks.setVisible(false);

        if (user instanceof IHQChief) {
            tabProcessSortedData.setDisable(false);
            tabSendPlan.setDisable(false);
            tabApplyPlan.setDisable(false);
            tabTask.setDisable(false);
        }

        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon geen data ophalen");
            }
            this.connectionManager.getData();
            this.connectionManager.getAllSortedData();
            this.connectionManager.getSituations();
            //this.connectionManager.getNewsItems();
            UserRole role = UserRole.HQ;

            if (user instanceof IHQChief) {
                role = UserRole.CHIEF;
                this.connectionManager.getServiceUsers();
                this.connectionManager.getTasks();
            }
            connectionManager.registerForUpdates(role);
            connectionManager.subscribeUnsorted();
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Displays the data that came from connectionManager.getData
     *
     * @param output
     */
    public void displayData(List<IData> output) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (!output.isEmpty()) {
                    connectionManager.unsubscribeUnsorted();
                }

                lvuUnsortedData.getItems().addAll(output);
                if (lvuUnsortedData.getSelectionModel().getSelectedItem() == null) {
                    lvuUnsortedData.getSelectionModel().selectFirst();
                }
            }

        });
    }

    public void displaySituations(Set<Situation> situations) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (ccnSituations == null) {
                    ccnSituations = new CheckComboBox(FXCollections.observableArrayList(situations));
                    ccnSituations.setLayoutX(924);
                    ccnSituations.setLayoutY(325);
                    ccnSituations.prefWidth(262);
                    ccnSituations.prefHeight(25);
                    ccnSituations.setMaxSize(262, 25);
                    apnPane.getChildren().add(ccnSituations);
                }

                if (ccbSituations == null) {
                    ccbSituations = new CheckComboBox(FXCollections.observableArrayList(situations));
                    ccbSituations.setLayoutX(790);
                    ccbSituations.setLayoutY(333);
                    ccbSituations.prefWidth(396);
                    ccbSituations.prefHeight(25);
                    ccbSituations.setMaxSize(262, 25);
                    apbPane.getChildren().add(ccbSituations);
                }
            }

        });
    }

    /**
     * Fills the GUI with information of the selected unsorted data
     */
    public void selectUnsortedData() {
        IData unsortedData
                = (IData) lvuUnsortedData.getSelectionModel().getSelectedItem();
        if (unsortedData != null) {
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
        lblUnsortedReport.setVisible(false);
    }

    /**
     * Remove old data and select new
     *
     * @param unsortedData
     */
    private void updateLvuUnsortedData(IData unsortedData) {
        // Remove old unsorted data
        lvuUnsortedData.getItems().remove(unsortedData);

        // If less than 10 items, load new unsorted data
        if (lvuUnsortedData.getItems().size() < 10) {
            this.connectionManager.getData();

            // Start timer
            if (lvuUnsortedData.getItems().size() == 0) {
                this.connectionManager.subscribeUnsorted();
            } else {
                this.connectionManager.unsubscribeUnsorted();
            }
        }

        // Select new
        lvuUnsortedData.getSelectionModel().selectFirst();
    }

    /**
     * Send the new sorted data to the server
     */
    public void sendSortedData() {
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }

            // Load values from GUI
            IData unsortedData
                    = (IData) lvuUnsortedData.getSelectionModel().getSelectedItem();
            if (unsortedData == null) {
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
            ISortedData sorted = new SortedData(id, title, description,
                    location, source, relevance, reliability, quality, tags);
            this.connectionManager.sendSortedData(sorted);

            // Update ListView
            this.updateLvuUnsortedData(unsortedData);

            lblUnsortedReport.setVisible(true);
            lblUnsortedReport.setText("Data is verzonden naar de database");

        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Set status to discarded
     */
    public void discard() {
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon data niet verwijderen");
            }

            // Load values from GUI
            IData unsortedData
                    = (IData) lvuUnsortedData.getSelectionModel().getSelectedItem();
            if (unsortedData == null) {
                throw new IllegalArgumentException("Selecteer eerst een "
                        + "ongesorteerd bericht");
            }

            // Discard data
            this.connectionManager.discardUnsortedData(unsortedData);

            // Update ListView
            this.updateLvuUnsortedData(unsortedData);

            lblUnsortedReport.setVisible(true);
            lblUnsortedReport.setText("Data is verwijderd uit de database");

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
            IData unsortedData
                    = (IData) lvuUnsortedData.getSelectionModel().getSelectedItem();
            if (unsortedData == null) {
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
            if (this.connectionManager == null) {
                throw new NetworkException("Kon verzoek niet versturen");
            }

            // Load values from GUI
            int requestId = -1;
            if (this.requestData != null) {
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

            lblInformationReport.setVisible(true);
            lblInformationReport.setText("Verzoek is verstuurd");

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
        tfrLocation.clear();
        ccrTags.getCheckModel().clearChecks();
        lblInformationReport.setVisible(false);
    }

    // ProcessSortedData--------------------------------------------------------
    /**
     * Fills the ListView with sorted data and selects the first value
     *
     * @param sortedData
     */
    public void displaySortedData(List<ISortedData> sortedData) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvsSortedData.getItems().removeAll(sortedData);
                lvsSortedData.getItems().addAll(sortedData);
                if (lvsSortedData.getSelectionModel().getSelectedItem() == null) {
                    lvsSortedData.getSelectionModel().selectFirst();
                }

                lvnSorted.getItems().removeAll(sortedData);
                lvnSorted.getItems().addAll(sortedData);
                if (lvnSorted.getSelectionModel().getSelectedItem() == null) {
                    lvnSorted.getSelectionModel().selectFirst();
                }
            }

        });
    }

    /**
     * Fills the ListView with sorted data and selects the first value
     *
     * @param sortedData
     */
    public void displaySortedDataTasks(List<ITask> tasks) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvsTasks.getItems().clear();
                tfsTaskTitle.clear();
                tasTaskDescription.clear();

                if (tasks != null) {
                    lvsTasks.getItems().addAll(tasks);

                    if (lvsTasks.getSelectionModel().getSelectedItem() == null) {
                        lvsTasks.getSelectionModel().selectFirst();
                    }
                }
            }

        });
    }

    /**
     * Fills the ComboBoxes with service users and selects the first value
     *
     * @param serviceUsers
     */
    public void displayServiceUsers(List<IServiceUser> serviceUsers) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                ITask task = (ITask) lvsTasks.getSelectionModel().getSelectedItem();
                List<IServiceUser> users = new ArrayList<>();
                if (task != null) {
                    for (IServiceUser s : serviceUsers) {
                        if (s.getType() == task.getTargetExecutor()) {
                            users.add(s);
                        }
                    }
                } else {
                    users.addAll(serviceUsers);
                }
                ObservableList<IServiceUser> observableSU1 = observableArrayList(users);

                cbsExecutor.setItems(observableSU1);
                if (cbsExecutor.getSelectionModel().getSelectedItem() == null) {
                    cbsExecutor.getSelectionModel().selectFirst();
                }

                IStep step = (IStep) lvaSteps.getSelectionModel().getSelectedItem();
                users = new ArrayList<>();
                if (step != null) {
                    for (IServiceUser s : serviceUsers) {
                        if (s.getType() == step.getTargetExecutor()) {
                            users.add(s);
                        }
                    }
                } else {
                    users.addAll(serviceUsers);
                }

                ObservableList<IServiceUser> observableSU2 = observableArrayList(users);

                cbaExecutor.setItems(observableSU2);
                if (cbaExecutor.getSelectionModel().getSelectedItem() == null) {
                    cbaExecutor.getSelectionModel().selectFirst();
                }

                task = (ITask) lvtTasks.getSelectionModel().getSelectedItem();
                users = new ArrayList<>();
                if (task != null) {
                    for (IServiceUser s : serviceUsers) {
                        if (s.getType() == task.getTargetExecutor()) {
                            users.add(s);
                        }
                    }
                } else {
                    users.addAll(serviceUsers);
                }

                ObservableList<IServiceUser> observableSU3 = observableArrayList(users);

                cbtNewExecutor.setItems(observableSU3);
                if (cbaExecutor.getSelectionModel().getSelectedItem() == null) {
                    cbaExecutor.getSelectionModel().selectFirst();
                }
            }

        });
    }

    /**
     * Fills the GUI with information of the selected sorted data
     */
    public void selectSortedData() {
        ISortedData sorted
                = (ISortedData) lvsSortedData.getSelectionModel().getSelectedItem();
        if (sorted != null) {
            // Fill GUI with information
            tfsSortedDataTitle.setText(sorted.getTitle());
            tasSortedDataDescription.setText(sorted.getDescription());
            tfsSource.setText(sorted.getSource());
            tfsLocation.setText(sorted.getLocation());
            displaySortedDataTasks(sorted.getTasks());

            boolean plan = false;
            boolean tasks = false;

            if (sorted.getTasks() != null) {
                for (ITask t : sorted.getTasks()) {
                    tasks = true;

                    try {
                        IStep s = (IStep) t;
                        plan = true;
                    } catch (ClassCastException ex) {
                    }

                    if (t instanceof IStep) {
                        plan = true;
                    }
                }
            }

            if (plan) {
                btnAddSortedTask.setDisable(true);
                btnAddRoadMap.setDisable(true);
            } else if (tasks) {
                btnAddSortedTask.setDisable(false);
                btnAddRoadMap.setDisable(true);
            } else {
                btnAddSortedTask.setDisable(false);
                btnAddRoadMap.setDisable(false);
            }

        } else {
            // Clear GUI
            lvsTasks.getItems().clear();
            tfuTitle.clear();
            tasSortedDataDescription.clear();
            tfsSource.clear();
            tfsLocation.clear();
        }

        lblProcessSortedData.setVisible(false);
    }

    /**
     * Fills the GUI with information of the selected task
     */
    public void selectSortedTask() {
        ITask task
                = (ITask) lvsTasks.getSelectionModel().getSelectedItem();
        if (task != null) {
            tfsTaskTitle.setText(task.getTitle());
            tasTaskDescription.setText(task.getDescription());
            cbsExecutor.getSelectionModel().select(task.getExecutor());
        }
    }

    /**
     * Send individual task to server
     */
    public void sendTask() {
        try {
            if (connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }

            ISortedData data = (ISortedData) lvsSortedData.getSelectionModel().getSelectedItem();
            String title = tfsTaskTitle.getText();
            String description = tasTaskDescription.getText();
            ServiceUser executor = null;

            boolean correct = true;
            for (ITask t : data.getTasks()) {
                if (t.getTitle().equals(title)) {
                    correct = false;
                }
            }

            if (correct) {
                Object object = cbsExecutor.getSelectionModel().getSelectedItem();
                if (object != null && object instanceof ServiceUser) {
                    executor = (ServiceUser) cbsExecutor.getSelectionModel().getSelectedItem();
                } else {
                    showDialog("Foutmelding", "Geen uitvoerder geselecteerd", true);
                }

                Task task = new Task(-1, title, description, TaskStatus.SENT, (ISortedData) lvsSortedData.getSelectionModel().getSelectedItem(), executor.getType(), executor);

                connectionManager.sendTask(task);

                List<ITask> tempTasks = new ArrayList();
                tempTasks.addAll(data.getTasks());
                tempTasks.add(task);

                data.setTasks(tempTasks);
                displaySortedDataTasks(data.getTasks());

                lblProcessSortedData.setVisible(true);
                lblProcessSortedData.setText("Taak is verzonden naar de database");
            } else {
                showDialog("Foutmelding", "Titel mag niet hetzelfde zijn als een eerder toegevoegde taak", true);
            }

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
            ISortedData data
                    = (ISortedData) lvsSortedData.getSelectionModel().getSelectedItem();
            if (data == null) {
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
    public void resetPlanInfo() {
        tempSteps.removeAll(tempSteps);
        lvpTasks.setItems(tempSteps);
        this.tempSteps = null;
        tfpPlanTitle.clear();
        tapPlanDescription.clear();
        tapKeyWords.clear();
        tfpTaskTitle.clear();
        tapTaskDescription.clear();
        tfpCondition.clear();
        lblSendPlan.setVisible(false);
    }

    /**
     * Fills the GUI with information of the selected task
     */
    public void selectPlanTask() {
        ITask task
                = (ITask) lvsTasks.getSelectionModel().getSelectedItem();
        if (task != null) {
            tfsTaskTitle.setText(task.getTitle());
            tasTaskDescription.setText(task.getDescription());
            this.connectionManager.getServiceUsers();
            List<IServiceUser> users = cbsExecutor.getItems();
            for (IServiceUser user : users) {
                if (user.getType() != task.getTargetExecutor()) {
                    cbsExecutor.getItems().remove(user);
                }
            }
            cbsExecutor.getSelectionModel().select(task.getExecutor());

            lblSendPlan.setVisible(false);
        }
    }

    /**
     * Adds a temporary step to the list of steps in a plan
     */
    public void addTempStep() {
        String title = tfpTaskTitle.getText();
        String description = tapTaskDescription.getText();
        String condition = tfpCondition.getText();
        int step;

        if (tempSteps != null) {
            step = tempSteps.size() + 1;
        } else {
            tempSteps = observableArrayList();
            step = 1;
        }

        boolean correct = true;

        for (IStep s : tempSteps) {
            if (s.getTitle().equals(title)) {
                correct = false;
            }
        }

        if (correct) {
            try {
                tempSteps.add(new Step(step, title, description, TaskStatus.UNASSIGNED, null, (Tag) this.cbExecutor.getSelectionModel().getSelectedItem(), null, step, condition));
                tfpTaskTitle.clear();
                tapTaskDescription.clear();
                tfpCondition.clear();
            } catch (IllegalArgumentException iaEx) {
                showDialog("Foutmelding", iaEx.getMessage(), true);
            }
        } else {
            showDialog("Foutmelding", "Titel mag niet hetzelfde zijn als een eerder toegevoegde stap", true);
        }

        lvpTasks.setItems(tempSteps);
    }

    public void removeTempStep() {
        IStep step = (IStep) lvpTasks.getSelectionModel().getSelectedItem();
        if (step != null) {
            tempSteps.remove(step);
            for (int i = 0; i < tempSteps.size(); i++) {
                tempSteps.get(i).setStepnr(i + 1);
            }
            lvpTasks.setItems(tempSteps);
        }
    }

    /**
     * Sends a plan to the server
     */
    public void sendPlan() {
        try {
            if (connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }

            if (tempSteps != null) {
                String title = tfpPlanTitle.getText();
                String description = tapPlanDescription.getText();
                HashSet<String> keywords = new HashSet();

                String s = tapKeyWords.getText();
                String[] array = uniformString(s).split(" ");
                for (String word : array) {
                    if (!word.isEmpty()) {
                        keywords.add(word);
                    }
                }

                if (title != null) {
                    List<IStep> steps = new ArrayList<>();
                    steps.addAll(tempSteps);

                    connectionManager.sendNewPlan(new Plan(-1, title, description, keywords, steps, true));
                    resetPlanInfo();

                    lblSendPlan.setVisible(true);
                    lblSendPlan.setText("Plan is verzonden naar de database");
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
    public void resetApplyPlan() {
        sortedData = null;

        tfaDataTitle.clear();
        taaDataDescription.clear();
        lvaPlans.getItems().clear();
        lvaSteps.getItems().clear();
        tfaTaskTitle.clear();
        tfaTaskDescription.clear();
        lblApplyPlan.setVisible(false);
    }

    public void resetPlansList() {
        lvaPlans.getItems().clear();
        lvaSteps.getItems().clear();
        tfaTaskTitle.clear();
        tfaTaskDescription.clear();
        lblApplyPlan.setVisible(false);
    }

    /**
     * Fill the ListView with plans
     *
     * @param plans
     */
    public void displayPlans(List<IPlan> plans) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                resetPlansList();
                lvaPlans.getItems().addAll(plans);
                if (lvaPlans.getSelectionModel().getSelectedItem() == null) {
                    lvaPlans.getSelectionModel().selectFirst();
                }
            }

        });
    }

    /**
     * Fill the ListView with Steps
     */
    public void displaySteps() {
        IPlan plan = (IPlan) lvaPlans.getSelectionModel().getSelectedItem();
        List<IStep> steps = new ArrayList<>();
        for (IStep s : plan.getSteps()) {
            steps.add(new Step(-1, String.valueOf(s.getTitle()),
                    String.valueOf(s.getDescription()),
                    TaskStatus.SENT, sortedData,
                    s.getTargetExecutor(),
                    null,
                    s.getStepnr(),
                    s.getCondition()));
        }
        tempPlan = new Plan(-1, plan.getTitle(), plan.getDescription(), plan.getKeywords(), steps, false);

        if (tempPlan != null) {
            Platform.runLater(new Runnable() {
                IPlan p = (IPlan) lvaPlans.getSelectionModel().getSelectedItem();
                List<IStep> steps = new ArrayList();

                @Override
                public void run() {
                    steps.addAll(p.getSteps());

                    lvaSteps.getItems().addAll(steps);
                    if (lvaSteps.getSelectionModel().getSelectedItem() == null) {
                        lvaSteps.getSelectionModel().selectFirst();
                    }
                }

            });
        }
    }

    /**
     * Search for plans with similar keywords and display them in the listview.
     */
    public void searchPlan() {

        String s = tfaSearch.getText();

        if (!s.isEmpty()) {
            HashSet<String> keywords = new HashSet();
            String[] array = uniformString(s).split(" ");
            for (String word : array) {
                if (!word.isEmpty()) {
                    keywords.add(word);
                }
            }
            resetPlansList();
            connectionManager.searchPlans(keywords);
        } else {
            showDialog("Foutmelding", "Vul een of meer keywords in", true);
        }
    }

    /**
     * Show all plans
     */
    public void resetPlans() {
        HashSet<String> keywords = new HashSet();
        resetPlansList();
        connectionManager.searchPlans(keywords);
    }

    /**
     * Fills the GUI with information of the selected plan
     */
    public void selectPlan() {
        IPlan plan
                = (IPlan) lvaPlans.getSelectionModel().getSelectedItem();
        if (plan != null) {
            lblApplyPlan.setVisible(false);
            lvaSteps.getItems().clear();
            displaySteps();
            lblApplyPlan.setVisible(false);
        }
    }

    public void applyPlan() {
        try {
            if (connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }
            if (sortedData != null) {
                if (tempPlan != null) {
                    List<IStep> steps = tempPlan.getSteps();
                    if (steps != null) {
                        boolean done = true;

                        for (IStep s : steps) {
                            if (s.getExecutor() == null) {
                                done = false;
                            }
                        }

                        if (done) {
                            List<ITask> tasks = new ArrayList<>();
                            tasks.addAll(tempPlan.getSteps());
                            sortedData.setTasks(tasks);
                            connectionManager.applyPlan(tempPlan);
                            resetApplyPlan();

                            lblApplyPlan.setVisible(true);
                            lblApplyPlan.setText("Plan is in werking gezet");
                        } else {
                            showDialog("Foutmelding", "Niet alle stappen hebben een uitvoerder", true);
                        }

                    } else {
                        showDialog("Foutmelding", "Het plan heeft geen stappen", true);
                    }
                } else {
                    showDialog("Foutmelding", "Er is geen plan geselecteerd", true);
                }
            } else {
                showDialog("Foutmelding", "Het plan kan niet worden toegepast als er geen gesorteerde data is geselecteerd", true);
            }
        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Fill GUI with Step properties
     */
    public void selectStep() {
        IStep s = (IStep) lvaSteps.getSelectionModel().getSelectedItem();
        if (s != null) {
            lblApplyPlan.setVisible(false);
            tfaTaskTitle.setText(s.getTitle());
            tfaTaskDescription.setText(s.getDescription());
            tfaTaskCondition.setText(s.getCondition());
            this.connectionManager.getServiceUsers();

        } else {
            tfaTaskTitle.clear();
            tfaTaskDescription.clear();
            tfaTaskCondition.clear();
        }

        lblApplyPlan.setVisible(false);
    }

    /**
     * Apply step to a service user and plan
     */
    public void applyStep() {
        IStep s = (IStep) lvaSteps.getSelectionModel().getSelectedItem();
        if (s != null) {
            Object object = cbaExecutor.getSelectionModel().getSelectedItem();
            if (object != null && object instanceof IServiceUser) {
                tempPlan.getSteps().get(s.getStepnr() - 1).setExecutor((IServiceUser) object);
            } else {
                showDialog("Foutmelding", "Geen uitvoerder geselecteerd", true);
            }

            s.setSortedData(sortedData);

            lvaSteps.getItems().remove(s);
        } else {
            showDialog("Foutmelding", "Selecteer een stap voordat je een stap toekent.", true);
        }

        lblApplyPlan.setVisible(false);
    }

    /**
     * Remove step from plan
     */
    public void refuseStep() {
        IStep s = (IStep) lvaSteps.getSelectionModel().getSelectedItem();
        if (s != null) {
            tempPlan.getSteps().remove(s.getStepnr() - 1);
            lvaSteps.getItems().remove(s);
            for (int i = 0; i < tempPlan.getSteps().size(); i++) {
                tempPlan.getSteps().get(i).setStepnr(i + 1);
                ((IStep) lvaSteps.getItems().get(i)).setStepnr(i + 1);
            }
        } else {
            showDialog("Foutmelding", "Selecteer een stap voordat je een stap verwijdert.", true);
        }

        lblApplyPlan.setVisible(false);
    }

    // Tasks--------------------------------------------------------------------
    public void displayTasks(List<ITask> tasks) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvtTasks.getItems().removeAll(tasks);
                lvtTasks.getItems().addAll(tasks);
                if (lvtTasks.getSelectionModel().getSelectedItem() == null) {
                    lvtTasks.getSelectionModel().selectFirst();
                }
            }

        });
    }

    public void selectTask() {
        ITask task
                = (ITask) lvtTasks.getSelectionModel().getSelectedItem();
        if (task != null) {
            if (task.getSortedData() != null) {
                tftTaskTitle.setText(task.getSortedData().getTitle());
                tatTaskDescription.setText(task.getSortedData().getDescription());
            } else {
                tftTaskTitle.clear();
                tatTaskDescription.clear();
            }
            tftTitle.setText(task.getTitle());
            tatDescription.setText(task.getDescription());

            if (task.getExecutor() != null) {
                tftExecutor.setText(task.getExecutor().toString());
            }

            if (task.getStatus() != TaskStatus.REFUSED && task.getStatus() != TaskStatus.FAILED) {

                cbtNewExecutor.setDisable(true);
                btnNewTask.setDisable(true);
                tftReason.setDisable(true);
            } else {
                cbtNewExecutor.setDisable(false);
                btnNewTask.setDisable(false);
                tftReason.setDisable(false);
            }

            tftReason.setText(task.getDeclineReason());
            lblTasks.setVisible(false);

            this.connectionManager.getServiceUsers();
        }
    }

    public void markAsRead() {
        try {
            if (connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }

            ITask task = (ITask) lvtTasks.getSelectionModel().getSelectedItem();
            if (task != null) {
                task.setStatus(TaskStatus.READ);

                connectionManager.updateTask(task);
            }
        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }

        lblTasks.setVisible(true);
        lblTasks.setText("Taak is gelezen");
    }

    public void updateTask() {
        try {
            if (connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }

            ITask task = (ITask) lvtTasks.getSelectionModel().getSelectedItem();

            if (task != null) {
                Object object = cbtNewExecutor.getSelectionModel().getSelectedItem();
                if (object != null && object instanceof IServiceUser) {
                    task.setExecutor((IServiceUser) object);
                    task.setStatus(TaskStatus.SENT);
                    task.setDeclineReason(null);
                    connectionManager.sendTask(task);

                    lvtTasks.getItems().remove(lvtTasks.getSelectionModel().getSelectedItem());
                    lvtTasks.getSelectionModel().selectFirst();

                    lblTasks.setVisible(true);
                    lblTasks.setText("Taak is up-to-date");
                } else {
                    showDialog("Foutmelding", "Geen uitvoerder geselecteerd", true);
                }

            }
        } catch (IllegalArgumentException iaEx) {
            showDialog("", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    // News ----------------------------------------------------------
    /**
     * Fills the GUI with information of the selected sorted data
     */
    public void selectNewsSorted() {
        ISortedData sorted
                = (ISortedData) lvnSorted.getSelectionModel().getSelectedItem();
        if (sorted != null) {
            // Fill GUI with information
            tfnTitleSorted.setText(sorted.getTitle());
            tanDescriptionSorted.setText(sorted.getDescription());
            tfnSourceSorted.setText(sorted.getSource());
            tfnLocationSorted.setText(sorted.getLocation());
        } else {
            // Clear GUI
            tfnTitleSorted.clear();
            tanDescriptionSorted.clear();
            tfnSourceSorted.clear();
            tfnLocationSorted.clear();
        }

        lblnMessage.setVisible(false);
    }

    public void sendNewsItem() {
        try {
            if (connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }

            // Load values from GUI
            String title = tfnTitle.getText();
            String description = tanDescription.getText();
            String location = tfnLocation.getText();
            String source = this.user.getUsername();
            int victims = Integer.parseInt(tfnVictims.getText());
            HashSet<Situation> situations = new HashSet<>();
            situations.addAll(ccnSituations.getCheckModel().getCheckedItems());

            // Make and send new NewsItem
            INewsItem newsItem = new NewsItem(title, description, location,
                    source, situations, victims);
            this.connectionManager.sendNewsItem(newsItem);

            // Show message
            lblnMessage.setVisible(true);
            lblnMessage.setText("Nieuwsbericht is succesvol verzonden");

            // Reset GUI
            tfnTitle.clear();
            tanDescription.clear();
            tfnLocation.clear();
            tfnVictims.clear();
            ccnSituations.getCheckModel().clearChecks();
        } catch (NumberFormatException nfEx) {
            showDialog("Onjuiste invoer", "Voer een getal in bij aantal slachtoffers", false);
        } catch (IllegalArgumentException iaEx) {
            showDialog("Onjuiste invoer", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        } catch (Exception ex) {
            showDialog("Fout", ex.getMessage(), false);
            ex.printStackTrace();
        }
    }

    // News Update ----------------------------------------------------------
    public void displayNewsItems(List<INewsItem> news) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvbNews.getItems().removeAll(news);
                lvbNews.getItems().addAll(news);
                if (lvbNews.getSelectionModel().getSelectedItem() == null) {
                    lvbNews.getSelectionModel().selectFirst();
                }
            }

        });
    }

    /**
     * Fills the GUI with information of the selected newsitem
     */
    public void displayNews(List<INewsItem> news) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvbNews.getItems().removeAll(news);
                lvbNews.getItems().addAll(news);
                if (lvbNews.getSelectionModel().getSelectedItem() == null) {
                    lvbNews.getSelectionModel().selectFirst();
                }
            }

        });
    }
    
    public void selectNewsItem() {
        INewsItem news = (INewsItem) lvbNews.getSelectionModel().getSelectedItem();
        if (news != null) {
            // Fill GUI with information
            tfbTitle.setText(news.getTitle());
            tabDescription.setText(news.getDescription());
            tfbLocation.setText(news.getLocation());
            tfbVictims.setText(String.valueOf(news.getVictims()));

            for (Situation s : news.getSituations()) {
                ccbSituations.getCheckModel().check(s);
            }
        } else {
            // Clear GUI
            tfbTitle.clear();
            tabDescription.clear();
            tfbLocation.clear();
            tfbVictims.clear();
            ccbSituations.getCheckModel().clearChecks();
        }

        lblbMessage.setVisible(false);
    }

    public void updateNewsItem() {
        try {
            if (connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }

            // Load values from GUI
            String title = tfbTitle.getText();
            String description = tabDescription.getText();
            String location = tfbLocation.getText();
            String source = this.user.getUsername();
            int victims = Integer.parseInt(tfbVictims.getText());
            HashSet<Situation> situations = new HashSet<>();
            situations.addAll(ccbSituations.getCheckModel().getCheckedItems());

            // Make and send new NewsItem
            INewsItem newsItem = new NewsItem(title, description, location,
                    source, situations, victims);
            this.connectionManager.updateNewsItem(newsItem);

            // Show message
            lblnMessage.setVisible(true);
            lblnMessage.setText("Nieuwsbericht is succesvol verzonden");

            // Reset GUI
            tfnTitle.clear();
            tanDescription.clear();
            tfnLocation.clear();
            tfnVictims.clear();
            ccnSituations.getCheckModel().clearChecks();
        } catch (NumberFormatException nfEx) {
            showDialog("Onjuiste invoer", "Voer een getal in bij aantal slachtoffers", false);
        } catch (IllegalArgumentException iaEx) {
            showDialog("Onjuiste invoer", iaEx.getMessage(), false);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        } catch (Exception ex) {
            showDialog("Fout", ex.getMessage(), false);
            ex.printStackTrace();
        }
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

    /**
     * Set status current data back to none
     */
    public void close(boolean logout) {
        if (this.connectionManager != null) {
            this.connectionManager.stopWorkingOnData(
                    new ArrayList<>(lvuUnsortedData.getItems()));
            this.connectionManager.unsubscribeUnsorted();
            if (!logout) {
                this.connectionManager.close();
            }
        }
    }

    public void showDialog(String title, String melding, boolean warning) {
        Alert alert = null;

        if (warning) {
            alert = new Alert(AlertType.WARNING);
            alert.setTitle("Foutmelding");
        } else {
            alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Melding");
        }

        if (!title.isEmpty()) {
            alert.setHeaderText(title);
        } else {
            alert.setHeaderText(null);
        }

        alert.setContentText(melding);
        alert.showAndWait();
    }

    /**
     * Replaces every punctuation mark with a space. Sets everything to
     * lowercase.
     *
     * @param s
     * @return
     */
    public String uniformString(String s) {
        s.replace("\n", " ")
                .replace(",", " ")
                .replace(".", " ")
                .replace("!", " ")
                .replace("?", " ")
                .replace("  ", " ")
                .toLowerCase()
                .replace("ÃƒÂ©", "e");

        return s;
    }
}
