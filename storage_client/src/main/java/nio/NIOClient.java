package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NIOClient {
    private final String clientFilePath = "./storage_client/files";
    private final String clientFileName = "3.txt";

    public static void main(String[] args) throws IOException {
        NIOClient client = new NIOClient();
        SocketChannel socketChannel = client.CreateChannel();
        client.sendFile(socketChannel);

    }

    private void sendFile(SocketChannel socketChannel) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(80);
//        CharBuffer charBuffer = buffer.asCharBuffer();
//        charBuffer.put(clientFileName);
//        charBuffer.flip();
        buffer.put((clientFileName + "#").getBytes());
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();

        Path path = Paths.get(clientFilePath + "/" + clientFileName);
        FileChannel inChannel = FileChannel.open(path);

        buffer = ByteBuffer.allocate(1024);
        while (inChannel.read(buffer) > 0) {
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }
        socketChannel.close();
    }

    private SocketChannel CreateChannel() throws IOException {
        //Remember that is code only works on blocking mode
        SocketChannel socketChannel = SocketChannel.open();

        //we don't need call this function as default value of blocking mode = true
        socketChannel.configureBlocking(true);

        SocketAddress sockAddr = new InetSocketAddress("localhost", 8189);
        socketChannel.connect(sockAddr);
        return socketChannel;
    }
}
