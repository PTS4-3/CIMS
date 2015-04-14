/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.UI;

import HeadquartersApp.Connection.ConnectionManager;
import Shared.Users.IUser;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Alexander
 */
public class Headquarters extends Application {

    private HeadquartersLogInController controller;
    private HeadquartersController hqController;

    @Override
    public void start(Stage stage) throws Exception {
        goToLogIn(stage);
    }
    
    public void goToLogIn(Stage stage) throws Exception
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
    
    public void goToHeadquarters(Stage stage, ConnectionManager manager, 
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
