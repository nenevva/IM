package GUI;

import GUI.Model.StageManager;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getClassLoader().getResource("login.fxml"));

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent e) {
                    System.exit(0);
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }

        StageManager.STAGE.put("Login",primaryStage);
    }
    public static void main(String[] args) {
        launch();
    }
}