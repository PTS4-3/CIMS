package HeadquartersApp.UI;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import HeadquartersApp.ConnectionHandler.ConnectionHandler;
import Shared.NetworkException;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
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
    PasswordField pfPassword;
    @FXML
    Button btnLogIn;

    private ConnectionHandler connectionManager;
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
    public void configure(Headquarters main, ConnectionHandler manager) {
        this.main = main;
        this.connectionManager = manager;
        this.connectionManager.setLogInController(this);
    }

    public void onClickLogIn() {
        
        try {
            System.out.println("starting inlog");
            if (this.connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }
            String username = tfsUsername.getText();
            String password = pfPassword.getText();
            
            connectionManager.getSigninUser(username, password);

        } catch (NetworkException ex) {
            Logger.getLogger(HeadquartersLogInController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void logIn(IUser user) {       
        Platform.runLater(new Runnable(){

            @Override
            public void run() {
                try {
                    if (user == null) {
                        System.out.println("user null");
                        showDialog("Log in fout", "De combinatie van wachtwoord en "+
                                "gebruikersnaam is onjuist", true);
                    } else if (user instanceof IServiceUser) {
                        showDialog("Log in fout", "Je mag hier niet inloggen met deze "
                                + "gegevens", true);
                    }else {
                        System.out.println("go to");
                        main.goToHeadquarters(connectionManager, user);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(HeadquartersLogInController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }        
        });        
    }
    
    public void showDialog(String title, String melding, boolean warning)
    {
        Alert alert = null;
        
        if (warning)
        {
            alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Foutmelding");
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
