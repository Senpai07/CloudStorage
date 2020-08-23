package netty.byte_release;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Settings {
    public static class ServerSettings {
        public String port;
        public String source;
        public String login;
        public String password;
        public String pathFiles;
        public String pathLogs;
    }

//    public static class ClientSettings {
//        public String host;
//        public String port;
//        public String pathFiles;
//        public String pathLogs;
//    }

    public static ServerSettings getServerSettings() {
        ServerSettings ss = new ServerSettings();
        FileInputStream fis;
        Properties properties = new Properties();
        try {
            fis = new FileInputStream("server.properties");
            properties.load(fis);
            ss.port = (String) properties.get("port");
            ss.source = (String) properties.get("source");
            ss.login = (String) properties.get("login");
            ss.password = (String) properties.get("password");
            ss.pathFiles = (String) properties.get("pathfiles");
            ss.pathLogs = (String) properties.get("pathlogs");
        } catch (FileNotFoundException e) {
            //Нет файла настроек.
        } catch (IOException e) {
            //Ошибка открытия.
        }
        return ss;
    }

//    public static ClientSettings getClientSettings() {
//        ClientSettings cs = new ClientSettings();
//        FileInputStream fis;
//        Properties properties = new Properties();
//        try {
//            fis = new FileInputStream("client.properties");
//            properties.load(fis);
//            cs.port = (String) properties.get("port");
//            cs.host = (String) properties.get("host");
//            cs.pathFiles = (String) properties.get("pathfiles");
//            cs.pathLogs = (String) properties.get("pathlogs");
//        } catch (FileNotFoundException e) {
//            //Нет файла настроек.
//        } catch (IOException e) {
//            //Ошибка открытия.
//        }
//        return cs;
//    }
}
