package netty.object_release;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {

    private final String fileName;

    private final byte[] fileData;

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return fileData;
    }

    public FileMessage(final Path path) throws IOException {
        fileName = path.getFileName().toString();
        fileData = Files.readAllBytes(path);
    }
}