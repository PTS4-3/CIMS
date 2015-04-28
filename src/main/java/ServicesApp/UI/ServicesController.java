/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.UI;

import ServicesApp.*;
import ServicesApp.Connection.ConnectionManager;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.ISortedData;
import Shared.NetworkException;
import Shared.Data.Status;
import Shared.Tag;
import Shared.Data.UnsortedData;
import Shared.Tasks.IStep;
import Shared.Tasks.ITask;
import Shared.Tasks.TaskStatus;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import Shared.Users.ServiceUser;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import javafx.util.Callback;

/**
 *
 * @author Alexander
 */
public class ServicesController implements Initializable {

    @FXML
    TabPane tabPane;

    // SendInfo
    @FXML
    Tab tabSendInfo;
    @FXML
    TextField tfnTitle;
    @FXML
    TextArea tanDescription;
    @FXML
    TextField tfnSource;
    @FXML
    TextField tfnLocation;

    // UpdateInfo
    @FXML
    Tab tabUpdateInfo;
    @FXML
    ListView lvuSentData;
    @FXML
    TextField tfuTitle;
    @FXML
    TextArea tauDescription;
    @FXML
    TextField tfuSource;
    @FXML
    TextField tfuLocation;

    // ReadSortedData
    @FXML
    Tab tabReadSortedData;
    @FXML
    ListView lvsSortedData;
    @FXML
    CheckBox chbsData;
    @FXML
    CheckBox chbsRequests;
    @FXML
    TextField tfsTitle;
    @FXML
    TextArea tasDescription;
    @FXML
    TextField tfsSource;
    @FXML
    TextField tfsLocation;
    @FXML
    Button btnAnswerRequest;

    // TaskInfo
    @FXML
    Tab tabReadTask;
    @FXML
    ListView lvtTasks;
    @FXML
    TextField tftTaskTitle;
    @FXML
    TextArea tatDescription;
    @FXML
    TextField tftCondition;
    @FXML
    Button btnAcceptTask;
    @FXML
    Button btnDismissTask;
    @FXML
    Button btnNotDone;
    @FXML
    Button btnFailed;
    @FXML
    Button btnSucceed;

    //report message label
    @FXML
    Label lblMessageUpdate;
    @FXML
    Label lblMessageTask;
    @FXML
    Label lblMessageSend;

    //menuBar
    @FXML
    MenuBar menuHQ;

    private ConnectionManager connectionManager;
    private boolean showingDataItem;
    private IDataRequest answeredRequest;
    private ITask selectedTask = null;
    private IServiceUser user = null;
    private HashSet<Tag> tags = new HashSet<Tag>();

    private Services main;

    public void setApp(Services application) {
        this.main = application;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.showingDataItem = false;
        this.answeredRequest = null;

        //labels to ""
        lblMessageUpdate.setText("");
        lblMessageTask.setText("");
        lblMessageSend.setText("");
        
        //tab change refresh sentData
        this.tabPane.selectionModelProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                resetSentData();            }
        });
        
        // Add Change Listeners
        lvuSentData.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener() {

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        selectSentData();
                        lblMessageUpdate.setText("");
                        lblMessageTask.setText("");
                        lblMessageSend.setText("");
                    }
                });

        lvsSortedData.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener() {

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        selectData();
                        lblMessageUpdate.setText("");
                        lblMessageTask.setText("");
                        lblMessageSend.setText("");
                    }
                });

        lvtTasks.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        selectTask();
                        lblMessageUpdate.setText("");
                        lblMessageTask.setText("");
                        lblMessageSend.setText("");
                    }
                });

        lvsSortedData.setCellFactory(new Callback<ListView<IData>, ListCell<IData>>() {

            @Override
            public ListCell<IData> call(ListView<IData> param) {
                return new ListCell<IData>() {

                    @Override
                    protected void updateItem(IData item, boolean empty) {
                        super.updateItem(item, empty);
                        lblMessageUpdate.setText("");
                        lblMessageTask.setText("");
                        lblMessageSend.setText("");
                        if (!empty) {
                            setItem(item);
                            setText(item.toString());

                            if (item instanceof IDataRequest) {
                                setTextFill(Color.RED);
                            } else if (item instanceof ISortedData) {
                                setTextFill(Color.BLACK);
                            }
                        } else {
                            setItem(null);
                            setText("");
                        }

                    }
                };
            }
        });
    }

    /**
     * Configures connectionManager and fills GUI with initial values
     *
     * @param ipAdressServer
     */
    public void configure(ConnectionManager manager, IUser user) {
        this.connectionManager = manager;
        if(user instanceof IServiceUser)
        {
        this.user = (IServiceUser) user;
        this.tags.add(this.user.getType());
        }
        this.connectionManager.setServicesController(this);

        // Fill GUI with initial values
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon geen data ophalen");
            }

            // Subscribe
            this.connectionManager.subscribeRequests(user.getUsername());
            this.connectionManager.subscribeSorted(user.getUsername());
            this.connectionManager.subscribeUnsorted(user.getUsername());
            this.connectionManager.subscribeTasks(user.getUsername());

            // Get initial values
            if (chbsRequests.isSelected()) {
                this.connectionManager.getRequests(tags);
            }
            if (chbsData.isSelected()) {
                this.connectionManager.getSortedData(tags);
            }
            //TODO source
            this.connectionManager.getSentData(this.user.getUsername());
            this.connectionManager.getTasks(this.user.getUsername());
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Displays the sentData that came from connectionManager.getSentData and
     * updates
     *
     * @param sentData
     */
    public void displaySentData(List<IData> sentData) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (!showingDataItem) {
                    lvuSentData.getItems().addAll(sentData);
                    if (lvuSentData.getSelectionModel().getSelectedItem() == null) {
                        lvuSentData.getSelectionModel().selectFirst();
                    }
                }
            }
        });
    }

    /**
     * Displays the task that came from connectionManager.getTasks
     *
     * @param tasks
     */
    public void displayTasks(List<ITask> tasks) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (!showingDataItem) {
                    lvtTasks.getItems().addAll(tasks);

                    if (lvtTasks.getSelectionModel().getSelectedItem() == null) {
                        lvtTasks.getSelectionModel().selectFirst();
                    }
                }
            }
        });
    }

    /**
     * Displays the requests that came from connectionManager.getRequests and
     * updates
     *
     * @param requests
     */
    public void displayRequests(List<IDataRequest> requests) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (chbsRequests.isSelected()) {
                    lvsSortedData.getItems().addAll(requests);
                    if (lvsSortedData.getSelectionModel().getSelectedItem() == null) {
                        lvsSortedData.getSelectionModel().selectFirst();
                    }
                }
            }

        });
    }

    /**
     * Displays the sortedData that came from connectionManager.getSortedData
     * and updates
     *
     * @param sortedData
     */
    public void displaySortedData(List<ISortedData> sortedData) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (chbsData.isSelected()) {
                    lvsSortedData.getItems().addAll(sortedData);
                    if (lvsSortedData.getSelectionModel().getSelectedItem() == null) {
                        lvsSortedData.getSelectionModel().selectFirst();
                    }
                }
            }

        });
    }

    /**
     * Displays the requestData that came from connectionManager.getDataItem
     *
     * @param dataItem
     */
    public void displayDataItem(IData dataItem) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (showingDataItem) {
                    lvuSentData.getItems().clear();
                    lvuSentData.getItems().add(dataItem);
                    lvuSentData.getSelectionModel().selectFirst();
                }
            }

        });
    }

    /**
     * Unsubscribe to the information from the connectionManager
     */
    public void close(boolean logout) {
        if (this.connectionManager != null) {
            this.connectionManager.unsubscribeRequests(user.getUsername());
            this.connectionManager.unsubscribeSorted(user.getUsername());
            this.connectionManager.unsubscribeTasks(user.getUsername());
            this.connectionManager.unsubscribeUnsorted(user.getUsername());
        }
        if (!logout) {
            this.connectionManager.closeConnection();
        }
    }

    /**
     * log out on server
     */
    public void logOutClick() {
        try {
            //log out connectionmanager
            this.close(true);
            main.goToLogIn();
        } catch (Exception ex) {
            Logger.getLogger(ServicesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sends the unsortedData to the server
     */
    public void sendUnsortedData() {
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
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

            this.removeAnsweredRequest();

            // Bevestiging tonen
            lblMessageSend.setText("Verzenden van ongesorteerde data is"
                    + " geslaagd");
        } catch (IllegalArgumentException iaEx) {
            showDialog("Invoer onjuist", iaEx.getMessage(), true);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
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
     * Removes the request that the user just answered
     */
    private void removeAnsweredRequest() {
        if (this.answeredRequest != null) {
            lvsSortedData.getItems().remove(this.answeredRequest);
            this.answeredRequest = null;
        }
    }

    /**
     * Fills the GUI with information of the selected SentData
     */
    public void selectSentData() {
        IData sentData = (IData) lvuSentData.getSelectionModel().getSelectedItem();
        if (sentData != null) {
            tfuTitle.setText(sentData.getTitle());
            tauDescription.setText(sentData.getDescription());
            tfuSource.setText(sentData.getSource());
            tfuLocation.setText(sentData.getLocation());
        } else {
            tfuTitle.clear();
            tauDescription.clear();
            tfuSource.clear();
            tfuLocation.clear();
        }
    }

    /**
     * Sends an update to the server
     */
    public void sendUpdate() {
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }

            // Load values from GUI
            IData sentData = (IData) lvuSentData.getSelectionModel().getSelectedItem();
            String title = tfuTitle.getText();
            String description = tauDescription.getText();
            String source = tfuSource.getText();
            String location = tfuLocation.getText();

            // Make and send update
            IData update = new UnsortedData(sentData.getId(), title,
                    description, location, source, Status.NONE);
            this.connectionManager.updateUnsortedData(update);

            this.removeAnsweredRequest();

            // Reset SentData
            if (this.showingDataItem) {
                this.resetSentData();
            }

            // Bevestiging tonen
            lblMessageUpdate.setText("Het verzenden van de aanvraag voor "
                    + "een update is geslaagd");
        } catch (IllegalArgumentException iaEx) {
            showDialog("Invoer onjuist", iaEx.getMessage(), true);
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Resets the filter of sentData, all sentData becomes visible
     */
    public void resetSentData() {
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon geen data ophalen");
            }

            this.showingDataItem = false;
            this.answeredRequest = null;

            // Clear sentData
            lvuSentData.getItems().clear();

            // TODO source
            this.connectionManager.getSentData(this.user.getUsername());
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Fills the GUI with the information of the selected data
     */
    public void selectData() {
        IData data = (IData) lvsSortedData.getSelectionModel().getSelectedItem();
        if (data != null) {
            // Fill GUI with information
            tfsTitle.setText(data.getTitle());
            tasDescription.setText(data.getDescription());
            tfsSource.setText(data.getSource());
            tfsLocation.setText(data.getLocation());

            // Determine visibility button requests
            btnAnswerRequest.setVisible(data instanceof IDataRequest);
        } else {
            // Clear GUI
            tfsTitle.clear();
            tasDescription.clear();
            tfsSource.clear();
            tfsLocation.clear();
            btnAnswerRequest.setVisible(false);
        }
    }

    /**
     * Fills the GUI with the information of the tasks
     */
    public void selectTask() {
        ITask data = (ITask) lvtTasks.getSelectionModel().getSelectedItem();
        //all buttons onvisible
        btnAcceptTask.setVisible(false);
        btnDismissTask.setVisible(false);
        btnFailed.setVisible(false);
        btnSucceed.setVisible(false);
        btnNotDone.setVisible(false);

        if (data != null) {
            this.selectedTask = data;
            if (data instanceof IStep) {
                IStep step = (IStep) data;
                // Fill GUI with information
                tftTaskTitle.setText(step.getTitle());
                tatDescription.setText(step.getDescription());
                tftCondition.setText(step.getCondition());
            } else {
                tftTaskTitle.setText(data.getTitle());
                tatDescription.setText(data.getDescription());
            }

            // Determine visibility button requests
            //if status is sent -- accept and dismiss button
            //if status is accept -- failed, notdone and succeed button
            if (data.getStatus().equals(TaskStatus.SENT)) {
                btnAcceptTask.setVisible(true);
                btnDismissTask.setVisible(true);
            } else if (data.getStatus().equals(TaskStatus.INPROCESS)) {
                btnFailed.setVisible(true);
                btnSucceed.setVisible(true);
                btnNotDone.setVisible(true);
            }
        } else {
            // Clear GUI
            this.selectedTask = null;
            tftTaskTitle.clear();
            tatDescription.clear();
            tftCondition.clear();
        }
    }

    /**
     * Changes the display of the sorted data
     *
     * @param evt
     */
    public void changeDisplay(Event evt) {
        CheckBox source = (CheckBox) evt.getSource();
        if (source.isSelected()) {
            try {
                if (this.connectionManager == null) {
                    throw new NetworkException("Kon geen data ophalen");
                }

                // Add
                if (source == chbsData) {
                    //TODO tags
                    this.connectionManager.getSortedData(tags);
                } else if (source == chbsRequests) {
                    //TODO tags
                    this.connectionManager.getRequests(tags);
                }
            } catch (NetworkException nEx) {
                showDialog("Geen verbinding met server", nEx.getMessage(), true);
            }
        } else {
            // Remove
            List<ISortedData> sortedData = new ArrayList<>();
            List<IDataRequest> requests = new ArrayList<>();

            for (Object o : lvsSortedData.getItems()) {
                if (o instanceof ISortedData) {
                    sortedData.add((ISortedData) o);
                } else if (o instanceof IDataRequest) {
                    requests.add((IDataRequest) o);
                }
            }

            if (source == chbsData) {
                // Remove sortedData
                for (ISortedData s : sortedData) {
                    lvsSortedData.getItems().remove(s);
                }
            } else if (source == chbsRequests) {
                // Remove dataRequest
                for (IDataRequest r : requests) {
                    lvsSortedData.getItems().remove(r);
                }
            }
        }
    }

    /**
     * Go to the tab sendUpdate
     */
    public void goToSendUpdate() {
        try {
            IDataRequest request = (IDataRequest) lvsSortedData.getSelectionModel().getSelectedItem();

            if (request != null) {
                if (request.getRequestId() == -1) {
                    // new data
                    tabPane.getSelectionModel().select(tabSendInfo);
                } else {
                    // update
                    if (this.connectionManager == null) {
                        throw new NetworkException("Kon geen data ophalen");
                    }

                    this.showingDataItem = true;

                    this.connectionManager.getDataItem(request.getRequestId());
                    tabPane.getSelectionModel().select(tabUpdateInfo);
                }
            }
        } catch (NetworkException nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Accept Task
     */
    public void acceptTask() {
        try {
            if (selectedTask != null) {
                selectedTask.setStatus(TaskStatus.INPROCESS);
                this.connectionManager.updateTask(selectedTask);
                //dismiss task succeed message
                lblMessageTask.setText("Het accepteren van de taak is gelukt.");
            } else {
                showDialog("Taak selectie", "Geen taak geselecteerd", false);
            }
            
        } catch (Exception nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
//        
    }

    /**
     * Dismiss Task with argument
     */
    public void dismissTask() {
        try {
            String argument = showArgumentDialog();
            if (argument.isEmpty() || argument.equals(" ")) {
                showDialog("Argument", "Er moet een argument ingevuld worden", true);
            } else {
                if (selectedTask != null) {
                    selectedTask.setStatus(TaskStatus.REFUSED);
                    selectedTask.setDeclineReason(argument);
                    this.connectionManager.updateTask(selectedTask);
                    //dismiss task succeed message
                    lblMessageTask.setText("Het weigeren van de taak is gelukt.");
                } else {
                    showDialog("Taak selectie", "Geen taak geselecteerd", false);
                }
                
            }
        } catch (Exception nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }


    /**
     * Status Task to failed
     */
    public void failedTask() {
        try {
            if (selectedTask != null) {
                selectedTask.setStatus(TaskStatus.FAILED);
                this.connectionManager.updateTask(selectedTask);
                //dismiss task succeed message
                lblMessageTask.setText("Het veranderen van de status is gelukt.");
            } else {
                showDialog("Taak selectie", "Geen taak geselecteerd", false);
            }
            
        } catch (Exception nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    /**
     * Status Task to succeed
     */
    public void succeedTask() {
        try {
            if (selectedTask != null) {
                selectedTask.setStatus(TaskStatus.SUCCEEDED);
                this.connectionManager.updateTask(selectedTask);
                //dismiss task succeed message
                lblMessageTask.setText("Het veranderen van de status is gelukt.");
            } else {
                showDialog("Taak selectie", "Geen taak geselecteerd", false);
            }
            
        } catch (Exception nEx) {
            showDialog("Geen verbinding met server", nEx.getMessage(), true);
        }
    }

    public void showDialog(String title, String melding, boolean warning) {
        Alert alert = null;

        if (warning) {
            alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Foutmelding");
        } else {
            alert = new Alert(Alert.AlertType.INFORMATION);
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

    public String showArgumentDialog() {
        TextInputDialog dialog = new TextInputDialog("Argument");
        dialog.setTitle("Argument");
        dialog.setContentText("Voer een argument in:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        } else {
            return null;
        }
    }
}
