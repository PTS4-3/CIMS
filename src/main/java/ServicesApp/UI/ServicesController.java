/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.UI;

import ServicesApp.Connection.ConnectionManager;
import Shared.Connection.ConnCommand;
import Shared.IData;
import Shared.IDataRequest;
import Shared.ISortedData;
import Shared.NetworkException;
import Shared.Status;
import Shared.Tag;
import Shared.UnsortedData;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
    @FXML
    TableView tvData;

    private ConnectionManager connectionManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Add Change Listeners
        lvuSentData.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener() {

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        selectSentData();
                    }
                });

        lvsSortedData.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener() {

                    @Override
                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                        selectData();
                    }
                });

        lvsSortedData.setCellFactory(new Callback<ListView<IData>, ListCell<IData>>() {

            @Override
            public ListCell<IData> call(ListView<IData> param) {
                return new ListCell<IData>() {

                    @Override
                    protected void updateItem(IData item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!empty) {
                            setItem(item);
                            setText(item.toString());

                            if (item instanceof IDataRequest) {
                                setTextFill(Color.RED);
                            } else if (item instanceof ISortedData) {
                                setTextFill(Color.BLACK);
                            }
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
    public void configure(String ipAdressServer) {
        this.connectionManager = new ConnectionManager(this, ipAdressServer);

        // Fill GUI with initial values
        // Tags nog invullen?
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Geen verbinding met server: "
                        + "Kon geen data ophalen");
            }
            //TODO source
            this.connectionManager.getSentData("");
            if (chbsRequests.isSelected()) {
                this.connectionManager.getRequests(new HashSet<Tag>());
            }
            if (chbsData.isSelected()) {
                this.connectionManager.getSortedData(new HashSet<Tag>());
            }
        } catch (NetworkException nEx) {
            System.out.println(nEx.getMessage());
        }
    }

    /**
     * Displays the sentData that came from connectionManager.getSentData
     *
     * @param sentData
     */
    public void displaySentData(List<IData> sentData) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvuSentData.setItems(FXCollections.observableList(sentData));
                if (lvuSentData.getSelectionModel().getSelectedItem() == null) {
                    lvuSentData.getSelectionModel().selectFirst();
                }
            }

        });
    }

    /**
     * Displays the requests that came from connectionManager.getRequests
     *
     * @param requests
     */
    public void displayRequests(List<IDataRequest> requests) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvsSortedData.getItems().addAll(requests);
                if (lvsSortedData.getSelectionModel().getSelectedItem() == null) {
                    lvsSortedData.getSelectionModel().selectFirst();
                }
            }

        });
    }

    /**
     * Displays the sortedData that came from connectionManager.getSortedData
     *
     * @param sortedData
     */
    public void displaySortedData(List<ISortedData> sortedData) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvsSortedData.getItems().addAll(sortedData);
                if (lvsSortedData.getSelectionModel().getSelectedItem() == null) {
                    lvsSortedData.getSelectionModel().selectFirst();
                }
            }

        });
    }

    /**
     * Displays the requestData that came from connectionManager.getData
     *
     * @param dataItem
     */
    public void displayDataItem(IData dataItem) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lvuSentData.getItems().clear();
                lvuSentData.getItems().add(dataItem);
                lvuSentData.getSelectionModel().selectFirst();
            }

        });
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
            if (this.connectionManager == null) {
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
        } catch (IllegalArgumentException iaEx) {
            System.out.println(iaEx.getMessage());
        } catch (NetworkException nEx) {
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
     * Fills the GUI with information of the selected SentData
     */
    public void selectSentData() {
        IData sentData = (IData) lvuSentData.getSelectionModel().getSelectedItem();
        if (sentData != null) {
            tfuTitle.setText(sentData.getTitle());
            tauDescription.setText(sentData.getDescription());
            tfuSource.setText(sentData.getSource());
            tfuLocation.setText(sentData.getLocation());
        }
    }

    /**
     * Sends an update to the server
     */
    public void sendUpdate() {
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Geen verbinding met server: "
                        + "Kon data niet wegschrijven");
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

            // Reset SentData
            this.resetSentData();

            // Bevestiging tonen TODO
        } catch (IllegalArgumentException iaEx) {
            System.out.println(iaEx.getMessage());
        } catch (NetworkException nEx) {
            System.out.println(nEx.getMessage());
        }
    }

    /**
     * Resets the filter of sentData, all sentData becomes visible
     */
    public void resetSentData() {
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Geen verbinding met server: "
                        + "Kon geen data ophalen");
            }

            // Clear sentData
            lvuSentData.getItems().clear();

            // TODO source
            this.connectionManager.getSentData("");
        } catch (NetworkException nEx) {
            System.out.println(nEx.getMessage());
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
                    throw new NetworkException("Geen verbinding met server: "
                            + "Kon geen data ophalen");
                }

                // Add
                if (source == chbsData) {
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

    /**
     * Connection calls this to deliver freshly received new ISortedData from
     * the server buffer.
     *
     * @param output
     */
    public void displayNewData(List<ISortedData> output) {
        System.out.println("displaying new data");
    }

    /**
     * Connection calls this to deliver fresh requests from server buffer.
     *
     * @param output
     */
    public void displayNewRequests(List<IDataRequest> output) {
        System.out.println("displaying new requests");
    }

    /**
     * Connection calls this to notify gui that the server has handled a
     * specific action. Used for blocking related actions until the server has
     * processed the previous one.
     *
     * @param connCommand detailing the specific command. Corresponds to
     * commands transmitted to server.
     */
    public void notifyConnDone(ConnCommand connCommand) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
