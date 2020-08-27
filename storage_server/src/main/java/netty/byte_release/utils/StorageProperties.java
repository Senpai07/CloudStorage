package netty.byte_release.utils;

import java.io.InputStream;
import java.util.Properties;

public class StorageProperties {
    private final Properties properties;

    public StorageProperties() throws Exception{
        InputStream fis = getClass().getResourceAsStream("/settings.properties");
        this.properties = new Properties();
        properties.load(fis);
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("port"));
    }

    public String getDir() {
        return properties.getProperty("dir");
    }
}
