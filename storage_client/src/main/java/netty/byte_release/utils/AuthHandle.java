package netty.byte_release.utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import netty.byte_release.Callback;
import netty.byte_release.CommandCodes;
import netty.byte_release.controller.ClientServer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class AuthHandle {

    public static void authorise(String login, String password, Label loginLabel, Callback callback) {
        new Thread(() -> {
            try {
                sendLoginPassword(login, password, false);
                byte result = StorageNetwork.getInstance().getIn().readByte();
                if (result == CommandCodes.ALREADY_AUTH_COMMAND) {
                    setLabelText(loginLabel, "Клиент с таким логином уже подключен");
                } else if (result == CommandCodes.SUCCESS_AUTH_COMMAND) {
                    setLabelText(loginLabel, "Успешная авторизация");
                    Platform.runLater(callback::callback);
                } else if (result == CommandCodes.FAIL_AUTH_COMMAND) {
                    setLabelText(loginLabel, "Неверный логин или пароль");
                } else {
                    setLabelText(loginLabel, "Неизвестная команда");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void registration(String login, String password, Label loginLabel) {
        new Thread(() -> {
            try {
                sendLoginPassword(login, password, true);
                byte result = StorageNetwork.getInstance().getIn().readByte();
                if (result == CommandCodes.LOGIN_EXIST_COMMAND) {
                    setLabelText(loginLabel, "Клиент с таким логином уже зарегистрирован!");
                } else if (result == CommandCodes.SUCCESS_AUTH_COMMAND) {
                    setLabelText(loginLabel, "Успешная регистрация!");
                } else {
                    setLabelText(loginLabel, "Неизвестная команда");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void sendLoginPassword(String login, String password, boolean isRegistration) throws IOException {
        int bufSize = 1 + 4 + login.length() + 4 + password.length();
        ByteBuffer buf = ByteBuffer.allocate(bufSize);
        if (isRegistration) {
            buf.put(CommandCodes.REGISTRATION_COMMAND);
        } else {
            buf.put(CommandCodes.AUTHORISE_COMMAND);
        }
        buf.putInt(login.length());
        buf.put(login.getBytes());
        buf.putInt(password.length());
        buf.put(password.getBytes());
        buf.flip();
        StorageNetwork.getInstance().getCurrentChannel().write(buf);
    }

    public static void setHomeDir(Callback callback) {
        new Thread(() -> {
            try {
                StorageNetwork.getInstance().getOut().write(CommandCodes.GET_ROOT_PATH_COMMAND);
                DataInputStream in = StorageNetwork.getInstance().getIn();
                byte result = in.readByte();
                if (result == CommandCodes.GET_ROOT_PATH_COMMAND) {
                    int rootPathSize = in.readInt();
                    byte[] rootPathBuf = new byte[rootPathSize];
                    int readByte = 0;
                    while (readByte < rootPathSize) {
                        readByte += in.read(rootPathBuf);
                    }

                    int clientPathSize = in.readInt();
                    byte[] clientPathBuf = new byte[clientPathSize];
                    readByte = 0;
                    while (readByte < clientPathSize) {
                        readByte += in.read(clientPathBuf);
                    }

                    String rootPath = new String(rootPathBuf, StandardCharsets.UTF_8);
                    String clientPath = new String(clientPathBuf, StandardCharsets.UTF_8);

//                    setServerPaths(rootPath, clientPath);
                    Platform.runLater(callback::callback);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private static void setLabelText(Label label, String text) {
        Platform.runLater(() -> label.setText(text));
    }

    public static void startMainForm(InputStream inputStream) {
        FXMLLoader loaderMainForm = new FXMLLoader();
        try {
            Parent root = loaderMainForm.load(inputStream);
            ClientServer clientServer = loaderMainForm.getController();
            clientServer.fillClientFileList(Paths.get("./storage_client/files"));
            setHomeDir(clientServer);
            clientServer.fillServerFileList();
            Stage primaryStage = StorageNetwork.getInstance().getPrimaryStage();
            primaryStage.setTitle("Storage client");
            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setHomeDir(ClientServer clientServer) {
        new Thread(() -> {
            try {
                StorageNetwork.getInstance().getOut().write(CommandCodes.GET_ROOT_PATH_COMMAND);
                DataInputStream in = StorageNetwork.getInstance().getIn();
                byte result = in.readByte();
                if (result == CommandCodes.GET_ROOT_PATH_COMMAND) {
                    int rootPathSize = in.readInt();
                    byte[] rootPathBuf = new byte[rootPathSize];
                    int readByte = 0;
                    while (readByte < rootPathSize) {
                        readByte += in.read(rootPathBuf);
                    }

                    int clientPathSize = in.readInt();
                    byte[] clientPathBuf = new byte[clientPathSize];
                    readByte = 0;
                    while (readByte < clientPathSize) {
                        readByte += in.read(clientPathBuf);
                    }

                    String rootPath = new String(rootPathBuf, StandardCharsets.UTF_8);
                    String clientPath = new String(clientPathBuf, StandardCharsets.UTF_8);

                    clientServer.setServerPaths(rootPath, clientPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }
}
