package netty.byte_release.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientProperties {
    private final Properties properties;

    public ClientProperties() throws IOException {
        InputStream fis = getClass().getResourceAsStream("/settings.properties");
        this.properties = new Properties();
        properties.load(fis);
    }

    public String getHost() {
        return properties.getProperty("host");
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("port"));
    }
}
