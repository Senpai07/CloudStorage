package netty.object_release;

import java.util.List;

public class FileListMessage extends AbstractMessage {

    private List<String> files;

    public List<String> getFiles() {
        return files;
    }

    public FileListMessage(final List<String> files) {
        this.files = files;
    }
}