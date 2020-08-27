package netty.byte_release.utils;

import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

public class StorageNetwork {
    private static final StorageNetwork ourInstance = new StorageNetwork();
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8189;
    private static final Logger logger = Logger.getLogger(StorageNetwork.class);
    private DataOutputStream out;
    private DataInputStream in;
    private SocketChannel currentChannel;
    private Stage primaryStage;

    public static StorageNetwork getInstance() {
        return ourInstance;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public SocketChannel getCurrentChannel() {
        return currentChannel;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private StorageNetwork() {
    }

    public void start(CountDownLatch countDownLatch) {
        try {
            ClientProperties properties = new ClientProperties();
            String host = properties.getHost();
            int port = properties.getPort();
            if (host == null) {
                logger.info("Отсутствует файл конфигурации. Установлены настройки по умолчанию.");
                host = DEFAULT_HOST;
            }
            if (port == 0) {
                logger.info("Отсутствует файл конфигурации. Установлены настройки по умолчанию.");
                port = DEFAULT_PORT;
            }
            InetSocketAddress serverAddress = new InetSocketAddress(host, port);
            currentChannel = SocketChannel.open(serverAddress);
            out = new DataOutputStream(currentChannel.socket().getOutputStream());
            in = new DataInputStream(currentChannel.socket().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        countDownLatch.countDown();

    }

    public void stop() {
        try {
            in.close();
            out.close();
            currentChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
