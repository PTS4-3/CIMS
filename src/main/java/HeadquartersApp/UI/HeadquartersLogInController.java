package HeadquartersApp.UI;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import HeadquartersApp.Connection.ConnectionManager;
import Shared.NetworkException;
import Shared.Users.IUser;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author Linda
 */
public class HeadquartersLogInController implements Initializable {

    @FXML
    Pane pane;
    @FXML
    TextField tfsUsername;
    @FXML
    TextField tfsPassword;
    @FXML
    Button btnLogIn;

    private ConnectionManager connectionManager;
    private IUser user = null;
    
    private Headquarters main;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

    }

    /**
     * Configures connectionManager
     *
     * @param ipAdressServer
     */
    public void configure(Headquarters main, String ipAdressServer) {
        this.main = main;
        this.connectionManager = new ConnectionManager(this, ipAdressServer);
    }

    public void onClickLogIn() {
        try {
            if (this.connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }
            String username = tfsUsername.getText();
            String password = tfsPassword.getText();

            user = null;
            //TODO
            //USername password, controleren
            //user opvragen
            //doorsturen naar nieuw scherm
            
            if(user != null) {
                try{
                    this.main.goToHeadquarters(connectionManager, user);
                } catch (Exception ex) {
                    Logger.getLogger(HeadquartersLogInController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                showDialog("Foutmelding", "User bestaat niet", true);
            }

        } catch (NetworkException ex) {
            Logger.getLogger(HeadquartersLogInController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void close() {
        if (this.connectionManager != null) {
            this.connectionManager.close();
        }
    }
    
    public void showDialog(String title, String melding, boolean warning)
    {
        Alert alert = null;
        
        if (warning)
        {
            alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("FoutMelding");
        }
        else
        {
            alert = new Alert(Alert.AlertType.INFORMATION);
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
