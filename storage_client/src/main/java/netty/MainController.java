package netty;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController extends Window implements Initializable {

    @FXML
    private ListView<String> serverFilesList;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        Network.start();
        initializeDragAndDrop();
        Thread t = new Thread(() -> {
            try {
                while (true) {

                    AbstractMessage am = Network.readObject();

                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        downloadFile(fm);
                    }

                    if (am instanceof FileListMessage) {
                        FileListMessage flm = (FileListMessage) am;
                        refreshRemoteFilesList(flm);
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void pressOnDownloadBtn() {
        Network.sendMsg(
                new FileRequest(
                        serverFilesList.getSelectionModel().getSelectedItem()));
    }

    public void pressOnDeleteBtn() {
        Network.sendMsg(
                new FileDeleteMessage(
                        serverFilesList.getSelectionModel().getSelectedItem()));
    }

    public void pressOnUploadBtn() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        File file = fileChooser.showOpenDialog(this);
        uploadFile(file);
    }

    private void downloadFile(final FileMessage fm) {
        if (Platform.isFxApplicationThread()) {
            saveFile(fm);
        } else {
            Platform.runLater(() -> saveFile(fm));
        }
    }

    private void saveFile(final FileMessage fm) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(fm.getFileName());
        FileChooser.ExtensionFilter extFilter;
        extFilter = new FileChooser.ExtensionFilter("Any files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);
        File dest = fileChooser.showSaveDialog(this);
        if (dest != null) {
            try {
                Files.write(
                        Paths.get(
                                String.valueOf(dest)),
                        fm.getData(),
                        StandardOpenOption.CREATE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void refreshRemoteFilesList(final FileListMessage flm) {
        if (Platform.isFxApplicationThread()) {
            serverFilesList.getItems().clear();
            flm.getFiles().forEach(s -> serverFilesList.getItems().add(s));
        } else {
            Platform.runLater(() -> {
                serverFilesList.getItems().clear();
                flm.getFiles().forEach(s -> serverFilesList.getItems().add(s));
            });
        }
    }

    private void uploadFile(final File file) throws IOException {
        if (file != null) {
            Network.sendMsg(new FileMessage(Paths.get(file.getAbsolutePath())));
        }
    }

    private void initializeDragAndDrop() {
        serverFilesList.setOnDragOver(event -> {
            if (event.getGestureSource() != serverFilesList
                    && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        serverFilesList.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File o : db.getFiles()) {
                    try {
                        uploadFile(o.getAbsoluteFile());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
