package netty.byte_release.settings;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerSettings extends Application {
    public static Stage primaryStage;
    @Override
    public void start(Stage primaryStage) throws Exception{
        ServerSettings.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/settingsServer.fxml"));
        primaryStage.setTitle("Settings server");
        primaryStage.setScene(new Scene(root,600,450));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}