package netty.byte_release;

import netty.byte_release.utils.StartServer;
import netty.byte_release.utils.StorageProperties;
import org.apache.log4j.Logger;

public class StorageServer {
    private static final int DEFAULT_PORT = 8189;
    private static final Logger logger = Logger.getLogger(StartServer.class);

    private static int getServerPort() {
        try {
            return new StorageProperties().getPort();
        } catch (Exception e) {
            logger.info("Отсутствует файл конфигурации. Установлены настройки по умолчанию.");
            return DEFAULT_PORT;
        }
    }


    public static void main(String[] args) throws Exception {
        int port = getServerPort();
        new StartServer(port).run();
    }
}
