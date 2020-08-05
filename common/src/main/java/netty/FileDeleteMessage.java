package netty;

import java.util.List;

public class FileDeleteMessage extends AbstractMessage {

    private String fileName;

    public String getFilename() {
        return fileName;
    }

    public FileDeleteMessage(final String fileName) {
        this.fileName = fileName;
    }
}