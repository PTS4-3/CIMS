/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServicesApp.UI;

import ServicesApp.Connection.ConnectionManager;
import Shared.Users.IUser;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Alexander
 */
public class Services extends Application {
    private ServicesLogInController controller;
    private ServicesController servicesController;
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        this.goToLogIn();
    }
    
    public void goToLogIn() throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ServicesApp/ServicesFX.fxml"));
        Parent root = (Parent) loader.load();
        controller = (ServicesLogInController) loader.getController();

        this.configure();

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Services CIMS");
        stage.show();
    }
    
    public void goToServices(ConnectionManager manager, IUser user)
            throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ServicesApp/ServicesFX.fxml"));
        Parent root = (Parent) loader.load();
        servicesController = (ServicesController) loader.getController();

        servicesController.configure(manager, user);

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Services CIMS");
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
