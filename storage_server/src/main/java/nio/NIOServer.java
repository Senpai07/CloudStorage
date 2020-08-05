package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NIOServer implements Runnable {
    private static int cnt = 1;
    private final ServerSocketChannel server;
    private final Selector selector;

    private static final String FILE_NAME = "received_%d.tmp";
    private static String serverFilePath = "./storage_server/storage";

    private static class Storage {
        private final AsynchronousFileChannel fileChannel;
        private Future<Integer> writeOperation;
        private long position;
        private String originalFileName;
        private final String tmplFileName;

        public Storage() throws IOException {
            tmplFileName = String.format(FILE_NAME, System.currentTimeMillis());
            fileChannel = AsynchronousFileChannel.open(Paths.get(serverFilePath + "/" + tmplFileName),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            originalFileName = "";
        }

        public void write(ByteBuffer buffer) throws InterruptedException, ExecutionException {
            try {
                if (originalFileName.isEmpty()) {
                    System.out.print("write: ");
                    StringBuilder s = new StringBuilder();
                    while (buffer.hasRemaining()) {
                        char c = (char) buffer.get();
                        if (c == "#".charAt(0)) break;
                        s.append(c);
                    }
                    originalFileName = s.toString();
                    System.out.println(originalFileName);
                }
                if (writeOperation != null)
                    position += writeOperation.get(5, TimeUnit.SECONDS);
                writeOperation = fileChannel.write(buffer, position);


            } catch (TimeoutException exc) {
                close();
                System.err.println("Write timeout");
            }
        }

        public void close() {
            try {
                fileChannel.close();
                Files.move(Paths.get(serverFilePath + "/" + tmplFileName),
                        Paths.get(serverFilePath + "/" + originalFileName), StandardCopyOption.REPLACE_EXISTING);


            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }
    }

    public NIOServer() throws IOException {
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        String userName = "user" + cnt;
        cnt++;
        serverFilePath += "/" + userName;
        Path path = Paths.get(serverFilePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("server started");
            while (server.isOpen()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        System.out.println("client accepted");
                        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ, new Storage());
                        channel.write(ByteBuffer.wrap("Hello!".getBytes()));
                    }
                    if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        Storage storage = (Storage) key.attachment();
                        ByteBuffer buffer = ByteBuffer.allocate(256);

                        int read = socketChannel.read(buffer);
                        if (read != -1) {
                            buffer.flip();
                            storage.write(buffer);
                        } else {
                            storage.close();
                            socketChannel.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Thread(new NIOServer()).start();
    }
}
