/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.UI;

import ServicesApp.Connection.ConnectionHandler;
import Shared.Users.IUser;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Alexander
 */
public class Services extends Application {
    private ConnectionHandler connectionmanager;
    private ServicesLogInController controller;
    private ServicesController servicesController;
    private Stage stage;
    private String ipAdressServer = "127.0.0.1";
    private boolean logIn = true;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        this.connectionmanager = new ConnectionHandler(this.ipAdressServer);
        this.goToLogIn();
    }
    
    public void goToLogIn() throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ServicesApp/ServicesLogInFX.fxml"));
        Parent root = (Parent) loader.load();
        controller = (ServicesLogInController) loader.getController();

        this.controller.configure(this, this.connectionmanager);

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Services CIMS");
        logIn = true;
        stage.show();
    }
    
    public void goToServices(ConnectionHandler manager, IUser user)
            throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ServicesApp/ServicesFX.fxml"));
        Parent root = (Parent) loader.load();
        servicesController = (ServicesController) loader.getController();
        servicesController.setApp(this);
        servicesController.configure(manager, user);

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Services CIMS - " + user.getName());
        logIn = false;
        stage.show();
    }
    
    @Override
    public void stop() throws Exception {
        if(logIn){
            controller.close();
        }
        else{
            servicesController.close(false);
        }
        super.stop();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
