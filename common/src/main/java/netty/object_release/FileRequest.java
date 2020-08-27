package netty.object_release;

public class FileRequest extends AbstractMessage {

    private final String fileName;

    public String getFileName() {
        return fileName;
    }

    public FileRequest(final String fileName) {
        this.fileName = fileName;
    }
}