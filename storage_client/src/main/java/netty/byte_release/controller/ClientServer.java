package netty.byte_release.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import netty.byte_release.FileInfoMessage;
import netty.byte_release.utils.FileHandle;
import netty.byte_release.utils.Message;
import netty.byte_release.utils.StorageNetwork;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientServer {
    public ComboBox<String> driveSelect;
    public TableView<FileInfoMessage> localFiles;
    public Button exitButton;
    public Button delClientButton;
    public Button topFolderClient;
    public Button delServerButton;
    public TableView<FileInfoMessage> serverFiles;
    public Button topFolderServer;
    public Label clientPathLabel;
    public Label serverPathLabel;
    public HBox progressBox;
    public Label fileLabel;
    public Label progressLabel;
    public ProgressBar progressBar;
    public Button downloadButton;
    public Button uploadButton;
    public Button refreshServerFilesButton;
    public Button refreshClientFilesButton;

    private String serverRootPath;
    private String serverClientPath;
    private Path srcPath, dstPath;

    public void initialize() {

//        sendButton.setTooltip(new Tooltip("Отправить сообщение"));
//        exitButton.setTooltip(new Tooltip("Закрыть программу"));
    }

    public void closeProgram(ActionEvent actionEvent) {
        Platform.exit();
        StorageNetwork.getInstance().stop();
    }

    public void fillClientFileList(Path path) {
        TableColumn<FileInfoMessage, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfoMessage, String> filenameColumn = new TableColumn<>("Имя");
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setPrefWidth(240);

        TableColumn<FileInfoMessage, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> new TableCell<FileInfoMessage, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        });
        fileSizeColumn.setPrefWidth(120);

        TableColumn<FileInfoMessage, String> fileDateColumn = new TableColumn<>("Дата изменения");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified()));
        fileDateColumn.setPrefWidth(120);

        localFiles.getColumns().addAll(fileTypeColumn, filenameColumn, fileSizeColumn, fileDateColumn);
        localFiles.getSortOrder().add(fileTypeColumn);

        driveSelect.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            driveSelect.getItems().add(p.toString());
        }

        localFiles.setOnMouseClicked(this::clientHandle);

        updateClientFileList(path);
    }

    public void fillServerFileList() {
        TableColumn<FileInfoMessage, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfoMessage, String> filenameColumn = new TableColumn<>("Имя");
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setPrefWidth(240);

        TableColumn<FileInfoMessage, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> new TableCell<FileInfoMessage, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        });
        fileSizeColumn.setPrefWidth(120);

        TableColumn<FileInfoMessage, String> fileDateColumn = new TableColumn<>("Дата изменения");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified()));
        fileDateColumn.setPrefWidth(120);

        serverFiles.getColumns().addAll(fileTypeColumn, filenameColumn, fileSizeColumn, fileDateColumn);
        serverFiles.getSortOrder().add(fileTypeColumn);

        serverFiles.setOnMouseClicked(this::serverHandle);

        Platform.runLater(() -> updateServerFileList(Paths.get(serverClientPath)));
    }

    private void serverHandle(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 & getSelectedFilename(serverFiles) != null) {
            Path path = Paths.get(serverPathLabel.getText()).resolve(serverFiles.getSelectionModel().getSelectedItem().getFilename());
            if (Files.isDirectory(path)) {
                updateServerFileList(path);
            }
        }
    }

    public String getSelectedFilename(TableView<FileInfoMessage> tableView) {
        if (!tableView.isFocused()) {
            return null;
        }
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            return null;
        }
        return tableView.getSelectionModel().getSelectedItem().getFilename();
    }

    public void updateClientFileList(Path path) {
        try {
            driveSelect.getSelectionModel().select(path.toAbsolutePath().getRoot().toString());
            clientPathLabel.setText(path.normalize().toAbsolutePath().toString());
            localFiles.getItems().clear();
            localFiles.getItems().addAll(Files.list(path).filter(Files::isReadable).map(FileInfoMessage::new).collect(Collectors.toList()));
            localFiles.sort();
        } catch (IOException e) {
            Message.ShowMessage("Error", "Ошибка обновления локального списка файлов", Alert.AlertType.ERROR);
        }
    }

    public void updateServerFileList(Path path) {
        try {
            serverPathLabel.setText(path.normalize().toString());
            FileHandle.setFileList(serverFiles, path.toString());
        } catch (Exception e) {
            Message.ShowMessage("Error", "Ошибка обновления списка файлов сервера", Alert.AlertType.ERROR);
        }
    }

    private void clientHandle(MouseEvent event) {
        if (event.getClickCount() == 2 & getSelectedFilename(localFiles) != null) {
            Path path = Paths.get(clientPathLabel.getText()).resolve(localFiles.getSelectionModel().getSelectedItem().getFilename());
            if (Files.isDirectory(path)) {
                updateClientFileList(path);
            }
        }
    }

    public void setServerPaths(String serverRootPath, String serverClientPath) {
        this.serverRootPath = serverRootPath;
        this.serverClientPath = serverClientPath;
    }

    public void getParentPathServer(ActionEvent actionEvent) {
        Path upperPath = Paths.get(serverPathLabel.getText()).getParent();
        if (upperPath != null & !Objects.equals(upperPath, Paths.get(serverRootPath).normalize())) {
                updateServerFileList(upperPath);
        }
    }

    public void getParentPathClient(ActionEvent actionEvent) {
        Path upperPath = Paths.get(clientPathLabel.getText()).getParent();
        if (upperPath != null) {
            updateClientFileList(upperPath);
        }
    }

    public void changeDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateClientFileList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public void delServerFile(ActionEvent actionEvent) {
        if (invalidSelectFileOnServer()) {
            return;
        }
        Alert alert = Message.showAlert(Alert.AlertType.CONFIRMATION, "Удалить файл на сервере?");
        Optional<ButtonType> option = alert.showAndWait();

        if (option.isPresent()) {
            if (option.get() == ButtonType.OK) {
                FileHandle.deleteFileFromServer(srcPath);
                updateServerFileList(srcPath.getParent());
            }
        }
    }

    public void delLocalFile(ActionEvent actionEvent) {
        if (invalidSelectFileOnClient()) {
            return;
        }
        Alert alert = Message.showAlert(Alert.AlertType.CONFIRMATION, "Действительно удалить файл?");
        Optional<ButtonType> option = alert.showAndWait();

        if (option.isPresent()) {
            if (option.get() == ButtonType.OK) {
                try {
                    Files.delete(srcPath);
                    updateClientFileList(srcPath.getParent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean invalidSelectFileOnClient() {
        if (getSelectedLocalFilename() == null) {
            Message.ShowMessage("Ошибка", "Файл не выбран", Alert.AlertType.ERROR);
            return true;
        }
        if (localFiles.getSelectionModel().getSelectedItem().getType() == FileInfoMessage.FileType.DIRECTORY) {
            Message.ShowMessage("Ошибка", "Файл не выбран", Alert.AlertType.ERROR);
            return true;
        }
        srcPath = Paths.get(clientPathLabel.getText(), getSelectedLocalFilename());
        dstPath = Paths.get(serverPathLabel.getText(), srcPath.getFileName().toString());
        return false;
    }


    private boolean invalidSelectFileOnServer() {
        if (getSelectedServerFilename() == null) {
            Message.ShowMessage("Ошибка", "Файл на сервере не выбран", Alert.AlertType.ERROR);
            return true;
        }
        if (serverFiles.getSelectionModel().getSelectedItem().getType() == FileInfoMessage.FileType.DIRECTORY) {
            Message.ShowMessage("Ошибка", "Файл на сервере не выбран", Alert.AlertType.ERROR);
            return true;
        }
        srcPath = Paths.get(serverPathLabel.getText(), getSelectedServerFilename());
        dstPath = Paths.get(clientPathLabel.getText(), srcPath.getFileName().toString());
        return false;
    }

    public String getSelectedLocalFilename() {
        if (!localFiles.isFocused()) {
            return null;
        }
        if (localFiles.getSelectionModel().getSelectedItem() == null) {
            return null;
        }
        return localFiles.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getSelectedServerFilename() {
        if (!serverFiles.isFocused()) {
            return null;
        }
        if (serverFiles.getSelectionModel().getSelectedItem() == null) {
            return null;
        }
        return serverFiles.getSelectionModel().getSelectedItem().getFilename();
    }

    public void downloadFile(ActionEvent actionEvent) {
        if (invalidSelectFileOnServer()) {
            return;
        }
        Optional<ButtonType> option = Optional.empty();
        for (FileInfoMessage file : localFiles.getItems()) {
            if (file.getFilename().equals(srcPath.getFileName().toString())) {
                Alert alert = Message.showAlert(Alert.AlertType.CONFIRMATION, "Заменить файл в локальной папке?");
                option = alert.showAndWait();
                break;
            }
        }
        if (option.isPresent()) {
            if (option.get() != ButtonType.OK) {
                return;
            }
        }
        waitProcess();

        FileHandle.getFileFromServer(srcPath, dstPath, progressBar, progressLabel,
                this::finishLocalProcess,
                this::showServerConnectionError);
    }

    private void showServerConnectionError() {
        Message.ShowMessage("Ошибка", "Сервер недоступен!", Alert.AlertType.ERROR);
        manageProgress(false);
    }

    private void manageProgress(boolean status) {
        delClientButton.setDisable(status);
        delServerButton.setDisable(status);
        downloadButton.setDisable(status);
        uploadButton.setDisable(status);
        exitButton.setDisable(status);
        refreshClientFilesButton.setDisable(status);
        refreshServerFilesButton.setDisable(status);
        progressBox.setVisible(status);
        progressBox.setManaged(status);
    }

    public void UploadFile(ActionEvent actionEvent) {
        if (invalidSelectFileOnClient()) {
            return;
        }
        Optional<ButtonType> option = Optional.empty();
        for (FileInfoMessage file : serverFiles.getItems()) {
            if (file.getFilename().equals(srcPath.getFileName().toString())) {
                Alert alert = Message.showAlert(Alert.AlertType.CONFIRMATION, "Заменить файл на сервере?");
                option = alert.showAndWait();
                break;
            }
        }
        if (option.isPresent()) {
            if (option.get() != ButtonType.OK) {
                return;
            }
        }
        waitProcess();

        FileHandle.putFileToServer(srcPath, dstPath, progressBar, progressLabel,
                this::finishRemoteProcess,
                this::showServerConnectionError);
    }

    private void finishRemoteProcess() {
        updateServerFileList(dstPath.getParent());
        manageProgress(false);
    }

    private void waitProcess() {
        fileLabel.setText(dstPath.getFileName().toString());
        manageProgress(true);
    }

    private void finishLocalProcess() {
        updateClientFileList(dstPath.getParent());
        manageProgress(false);
    }

    public void refreshServerFilesAction(ActionEvent actionEvent) {
        Path path = Paths.get(serverPathLabel.getText());
        updateServerFileList(path);
    }

    public void refreshClientFilesAction(ActionEvent actionEvent) {
        Path path = Paths.get(clientPathLabel.getText());
        updateClientFileList(path);
    }
}
