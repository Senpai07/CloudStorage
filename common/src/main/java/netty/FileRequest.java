package netty;

public class FileRequest extends AbstractMessage {

    private final String fileName;

    public String getFileName() {
        return fileName;
    }

    public FileRequest(final String fileName) {
        this.fileName = fileName;
    }
}