package netty.byte_release.utils;

import javafx.scene.control.Alert;

public class Message {
    public static void ShowMessage(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Alert showAlert(Alert.AlertType type, String text) {
        Alert alert = new Alert(type, text);
        alert.getDialogPane().setHeaderText(null);
        return alert;
    }
}