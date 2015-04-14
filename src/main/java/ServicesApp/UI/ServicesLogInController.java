package ServicesApp.UI;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ServicesApp.Connection.ConnectionManager;
import Shared.Users.IUser;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

/**
 * FXML Controller class
 *
 * @author Linda
 */
public class ServicesLogInController implements Initializable {

    private ConnectionManager connectionManager;
    private IUser user;

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
    public void configure(String ipAdressServer) {
        this.connectionManager = new ConnectionManager(this, ipAdressServer);
    }   
}
