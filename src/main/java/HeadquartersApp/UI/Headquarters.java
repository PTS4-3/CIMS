/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HeadquartersApp.UI;

import java.net.URL;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Alexander
 */
public class Headquarters extends Application {

    private FXMLLoader fxmlLoader;

    @Override
    public void start(Stage stage) throws Exception {
//        URL location = getClass().getResource("HeadquartersApp.HeadquartersFX.fxml");
//        fxmlLoader = new FXMLLoader();
//        fxmlLoader.setLocation(location);
//        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
//        Parent root = (Parent) fxmlLoader.load(location.openStream());

        //FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Login.fxm"));
        //Parent root = (Parent) loader.load();
        //LoginFX controller = (LoginFX) loader.getController();

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("HeadquartersFX.fxml"));
        this.configure();

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
        int portnumber = 1099;

        ((HeadquartersFXController) fxmlLoader.getController()).configure(ipAdressServer, portnumber);
    }

    @Override
    public void stop() throws Exception {
        ((HeadquartersFXController) fxmlLoader.getController()).close();
        super.stop();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
