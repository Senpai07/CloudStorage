package netty.byte_release.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import netty.byte_release.utils.AuthHandle;
import netty.byte_release.utils.Message;
import netty.byte_release.utils.StorageNetwork;

public class AuthDialog {
    @FXML
    public Button exitButton;
    @FXML
    public TextField userNameEdit;
    @FXML
    public Button authButton;
    @FXML
    public TextField userPassEdit;
    public Label authStatus;


    @FXML
    public void closeProgram(ActionEvent actionEvent) {
        // get a handle to the stage
        Stage stage = (Stage) exitButton.getScene().getWindow();
        // do what you have to do
        stage.close();
        System.exit(0);
        StorageNetwork.getInstance().stop();
    }

    @FXML
    public void authButtonAction(ActionEvent actionEvent) {
        try {
            sendAuthorisationRequest();
        } catch (Exception e) {
            showError("Ошибка при попытке аутентификации!");
        }
    }

    private void sendAuthorisationRequest() {
        String login = userNameEdit.getText();
        String password = userPassEdit.getText();
        if (login.isEmpty() || password.isEmpty()) {
            authStatus.setText("Заполните все поля!");
        } else {
            AuthHandle.authorise(login, password, authStatus, this::successAuth);
        }
    }

    private void successAuth() {
        AuthHandle.startMainForm(getClass().getResourceAsStream("/ClientServer.fxml"));
    }


    public void showError(String errorMessage) {
        Platform.runLater(() -> Message.ShowMessage("Error", errorMessage, Alert.AlertType.ERROR));
    }
}
