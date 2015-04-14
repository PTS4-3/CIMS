/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.UI;

import HeadquartersApp.Connection.ConnectionManager;
import Shared.Users.IUser;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author Alexander
 */
public class Headquarters extends Application {

    private HeadquartersLogInController controller;
    private HeadquartersController hqController;

    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        try {
            this.stage = stage;
            this.stage.setTitle("Headquarters CIMS");
            this.stage.setMinWidth(100);
            this.stage.setMinHeight(100);
            goToLogIn();
            

            this.stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.goToLogIn();
    }
    
    public void goToLogIn() throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("HeadquartersApp/HeadquartersLogInController.fxml"));
        Parent root = (Parent) loader.load();
        controller = (HeadquartersLogInController) loader.getController();

        this.configure();

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Headquarters CIMS");
        stage.show();
    }
    
    public void goToHeadquarters(ConnectionManager manager, 
            IUser user) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
                .getResource("HeadquartersApp/HeadquartersController.fxml"));
        Parent root = (Parent) loader.load();
        hqController = (HeadquartersController) loader.getController();

        hqController.configure(manager, user);

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Headquarters CIMS");
        stage.show();
    }

    private void configure() {
        //Scanner input = new Scanner(System.in);
        //System.out.print("Client: Voer IP-adres server in: ");
        //String ipAdressServer = input.nextLine();
        String ipAdressServer = "127.0.0.1";

        //System.out.print("Client: Voer portnumber in: ");
        //int portnumber = input.nextInt();
        //int portnumber = 8189;

        controller.configure(ipAdressServer);
    }

    @Override
    public void stop() throws Exception {
        controller.close();
        super.stop();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
