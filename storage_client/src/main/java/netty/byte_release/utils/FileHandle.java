package netty.byte_release.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.control.*;
import netty.byte_release.Callback;
import netty.byte_release.CommandCodes;
import netty.byte_release.FileInfoMessage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class FileHandle {
    private static final int BUFFER_SIZE = 1024 * 1024 * 50;
    private static ByteBuffer buf;

    public static void putFileToServer(Path src, Path dst, ProgressBar progressBar, Label label, Callback callback, Callback error) {
        new Thread(() -> {
            try {
                File srcFile = src.toFile();
                FileInputStream in = new FileInputStream(srcFile);
                long fileSize = srcFile.length();
                int packBufSize = 1 + 4 + dst.toString().length() + 8;
                if (fileSize == 0) {
                    nullFileSizeAlert();
                    in.close();
                    callback.callback();
                    return;
                }
                buf = ByteBuffer.allocate(packBufSize);
                buf.put(CommandCodes.TO_SERVER_COMMAND);
                buf.putInt(dst.toString().length());
                buf.put(dst.toString().getBytes());
                buf.putLong(fileSize);
                buf.flip();
                StorageNetwork.getInstance().getCurrentChannel().write(buf);
                buf = ByteBuffer.allocate(BUFFER_SIZE);
                long bytesSend = 0;
                while (bytesSend < fileSize) {
                    int readByte = in.getChannel().read(buf);
                    bytesSend += readByte;
                    buf.flip();
                    StorageNetwork.getInstance().getCurrentChannel().write(buf);
                    buf.clear();
                    float finalPercent = (float) bytesSend / fileSize;
                    Platform.runLater(() -> {
                        progressBar.setProgress(finalPercent);
                        label.setText(String.format("%.1f", finalPercent * 100) + "%");
                    });
                }
                in.close();
                byte result = StorageNetwork.getInstance().getIn().readByte();
                if (result == CommandCodes.FINISH_COMMAND) {
                    callback.callback();
                } else {
                    Platform.runLater(error::callback);
                }
            } catch (Exception e) {
                Platform.runLater(error::callback);
            }
        }).start();
    }

    public static void getFileFromServer(Path src, Path dst, ProgressBar progressBar, Label label, Callback callback, Callback error) {
        new Thread(() -> {
            try {
                int packBufSize = 1 + 4 + src.toString().length();
                buf = ByteBuffer.allocate(packBufSize);
                buf.put(CommandCodes.FROM_SERVER_COMMAND);
                buf.putInt(src.toString().length());
                buf.put(src.toString().getBytes());
                buf.flip();
                StorageNetwork.getInstance().getCurrentChannel().write(buf);
                buf = ByteBuffer.allocate(BUFFER_SIZE);

                long readBytes = 0;
                long fileSize = StorageNetwork.getInstance().getIn().readLong();
                if (fileSize == 0) {
                    nullFileSizeAlert();
                }
                while (readBytes < fileSize) {
                    boolean append = true;
                    if (readBytes == 0) append = false;
                    FileOutputStream out = new FileOutputStream(dst.toString(), append);
                    int read = StorageNetwork.getInstance().getCurrentChannel().read(buf);
                    readBytes += read;
                    buf.flip();
                    out.getChannel().write(buf);
                    buf.clear();
                    float finalPercent = (float) readBytes / fileSize;
                    Platform.runLater(() -> {
                        progressBar.setProgress(finalPercent);
                        label.setText(String.format("%.1f", finalPercent * 100) + "%");
                    });
                    out.close();
                }
                callback.callback();

            } catch (IOException e) {
                Platform.runLater(error::callback);
            }
        }).start();
    }

    public static void deleteFileFromServer(Path path) {
        try {
            int bufSize = 1 + 4 + path.toString().length();
            buf = ByteBuffer.allocate(bufSize);
            buf.put(CommandCodes.DELETE_FILE_COMMAND);
            buf.putInt(path.toString().length());
            buf.put(path.toString().getBytes());
            buf.flip();
            StorageNetwork.getInstance().getCurrentChannel().write(buf);
            buf.clear();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setFileList(TableView<FileInfoMessage> tableView, String path) {
        new Thread(() -> {
            try {
                int bufSize = 1 + 4 + path.length();
                buf = ByteBuffer.allocate(bufSize);
                buf.put(CommandCodes.LIST_COMMAND);
                buf.putInt(path.length());
                buf.put(path.getBytes());
                buf.flip();
                StorageNetwork.getInstance().getCurrentChannel().write(buf);
                buf.clear();

                DataInputStream in = StorageNetwork.getInstance().getIn();
                byte result = in.readByte();
                if (result == CommandCodes.LIST_COMMAND) {
                    int listSize = in.readInt();
                    byte[] fileListBuf = new byte[listSize];
                    int readByte = 0;
                    while (readByte < listSize) {
                        readByte += in.read(fileListBuf);
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    String listString = new String(fileListBuf, StandardCharsets.UTF_8);
                    List<FileInfoMessage> fileInfoList = mapper.readValue(listString,
                            mapper.getTypeFactory().constructCollectionType(List.class, FileInfoMessage.class));

                    Platform.runLater(() -> {
                        tableView.getItems().clear();
                        tableView.getItems().addAll(fileInfoList);
                        tableView.sort();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private static void nullFileSizeAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Пустой файл!", ButtonType.OK);
            alert.getDialogPane().setHeaderText(null);
            alert.showAndWait();
        });
    }
}
