/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.UI;

import HeadquartersApp.Connection.ConnectionHandler;
import Shared.Users.IUser;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 *
 * @author Alexander
 */
public class Headquarters extends Application {

    private HeadquartersLogInController controller;
    private HeadquartersController hqController;
    private ConnectionHandler connectionHandler;
    private String ipAdressServer = "127.0.0.1";
    ;

    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        try {
            this.stage = stage;
            this.stage.setTitle("Headquarters CIMS");
            this.stage.setMinWidth(100);
            this.stage.setMinHeight(100);
            this.connectionHandler = new ConnectionHandler(this.ipAdressServer);
            goToLogIn();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.goToLogIn();
    }

    public void goToLogIn() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("HeadquartersApp/HeadquartersLogInFX.fxml"));
        Parent root = (Parent) loader.load();
        controller = (HeadquartersLogInController) loader.getController();

        this.controller.configure(this, this.connectionHandler);
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Headquarters CIMS");
        stage.show();
    }

    public void goToHeadquarters(ConnectionHandler manager,
            IUser user) throws Exception {
        if (user != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                    .getResource("HeadquartersApp/HeadquartersFX.fxml"));
            Parent root = (Parent) loader.load();
            hqController = (HeadquartersController) loader.getController();

            this.hqController.configure(manager, user);
            this.hqController.setApp(this);

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Headquarters CIMS");
            stage.show();
        } else {
            showDialog("Foutmelding", "User bestaat niet", true);
        }
    }

    @Override
    public void stop() throws Exception {
        if(hqController != null){
            hqController.close(false);
        }    
        super.stop();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public void showDialog(String title, String melding, boolean warning) {
        Alert alert = null;

        if (warning) {
            alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("FoutMelding");
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
}
