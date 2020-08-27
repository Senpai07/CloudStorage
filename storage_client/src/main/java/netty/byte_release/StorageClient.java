package netty.byte_release;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import netty.byte_release.utils.StorageNetwork;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class StorageClient  extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        StorageNetwork.getInstance().setPrimaryStage(primaryStage);
        Parent root = FXMLLoader.load(getClass().getResource("/AuthDialog.fxml"));
        primaryStage.setTitle("Storage client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) throws Exception {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> StorageNetwork.getInstance().start(networkStarter)).start();
        networkStarter.await();
        launch(args);
    }
}
