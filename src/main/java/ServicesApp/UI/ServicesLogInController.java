package ServicesApp.UI;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import HeadquartersApp.UI.Headquarters;
import ServicesApp.Connection.ConnectionManager;
import Shared.NetworkException;
import Shared.Users.IUser;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author Linda
 */
public class ServicesLogInController implements Initializable {
    @FXML
    Pane pane;
    @FXML
    TextField tfsUsername;
    @FXML
    TextField tfsPassword;
    @FXML
    Button btnLogIn;

    private ConnectionManager connectionManager;
    private IUser user;

    private Services main;
    
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
    public void configure(Services main, String ipAdressServer) {
        this.main = main;
        this.connectionManager = new ConnectionManager(this, ipAdressServer);
        
    }   
    
    public void onClickLogIn() throws NetworkException
    {
        if (this.connectionManager == null) {
                throw new NetworkException("Kon data niet wegschrijven");
            }
            String username = tfsUsername.getText();
            String password = tfsPassword.getText();
        try{
        this.main.goToServices(connectionManager, user);
        } catch (Exception ex) {
            Logger.getLogger(ServicesLogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
