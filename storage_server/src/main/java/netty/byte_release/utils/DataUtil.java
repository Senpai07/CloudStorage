package netty.byte_release.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import netty.byte_release.Callback;
import netty.byte_release.CommandCodes;
import netty.byte_release.FileInfoMessage;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class DataUtil {
    private static final Path DEFAULT_DIR = Paths.get(".", "data");
    private static final Logger logger = Logger.getLogger(StartServer.class);
    private static final int BUFFER_SIZE = 1024 * 1024 * 50;

    private int pathSize;
    private String pathName;
    private long fileSize;
    private long receivedFileSize;

    public String[] createHomeDir(String login) throws IOException {
        Path clientPath;
        String[] paths = new String[2];
        try {
            String rootDir = new StorageProperties().getDir();
            clientPath = Paths.get(rootDir, login);
            paths[0] = rootDir;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Отсутствует файл конфигурации. Установлены настройки по умолчанию.");
            clientPath = Paths.get(DEFAULT_DIR.toString(), login);
            paths[0] = DEFAULT_DIR.toString();
        }
        if (!Files.exists(clientPath)) {
            Files.createDirectories(clientPath);
        }
        paths[1] = clientPath.toString();
        return paths;
    }

    public String getFileListJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<FileInfoMessage> list = Files.list(Paths.get(pathName)).map(FileInfoMessage::new).collect(Collectors.toList());
        return mapper.writeValueAsString(list);
    }

    public void sendFileList(ChannelHandlerContext ctx) throws IOException {
        String fileList = getFileListJson();
        ByteBuf tmp = Unpooled.buffer();
        tmp.writeByte(CommandCodes.LIST_COMMAND);
        tmp.writeInt(fileList.length());
        tmp.writeBytes(fileList.getBytes());
        ctx.writeAndFlush(tmp);
        tmp.clear();
    }

    public void sendClientDir(ChannelHandlerContext ctx, String login) throws IOException {
        String[] paths = createHomeDir(login);
        ByteBuf tmp = Unpooled.buffer();
        tmp.writeByte(CommandCodes.GET_ROOT_PATH_COMMAND);
        tmp.writeInt(paths[0].length());
        tmp.writeBytes(paths[0].getBytes());
        tmp.writeInt(paths[1].length());
        tmp.writeBytes(paths[1].getBytes());
        ctx.writeAndFlush(tmp);
        tmp.clear();
    }

    public boolean getPathSize(ByteBuf buf) {
        if (buf.readableBytes() >= Integer.BYTES) {
            pathSize = buf.readInt();
            return true;
        }
        return false;
    }

    public boolean getPathName(ByteBuf buf, boolean isFile) {
        if (buf.readableBytes() >= pathSize) {
            byte[] passBuf = new byte[pathSize];
            buf.readBytes(passBuf);
            pathName = new String(passBuf, StandardCharsets.UTF_8);
            if (isFile) {
                receivedFileSize = -1L;
            }
            return true;
        }
        return false;
    }

    public boolean getFileSize(ByteBuf buf) {
        if (buf.readableBytes() >= Long.BYTES) {
            fileSize = buf.readLong();
            receivedFileSize = -1;
            return true;
        }
        return false;
    }

    public void getFile(ChannelHandlerContext ctx, ByteBuf buf, Callback callback) {
        boolean append = true;
        if (receivedFileSize == -1) {
            append = false;
            receivedFileSize = 0;
        }
        try (FileOutputStream out = new FileOutputStream(pathName, append)) {
            if (fileSize == 0) {
                sendFinish(ctx);
                callback.callback();
            }
            while (buf.readableBytes() > 0) {
                int write = out.getChannel().write(buf.nioBuffer());
                receivedFileSize += write;
                buf.readerIndex(buf.readerIndex() + write);
                if (receivedFileSize == fileSize) {
                    sendFinish(ctx);
                    callback.callback();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putFile(ChannelHandlerContext ctx, Callback callback) {
        new Thread(() -> {
            try {
                File srcFile = Paths.get(pathName).toFile();
                FileInputStream in = new FileInputStream(srcFile);
                long fileSize = srcFile.length();
                ByteBuf tmp = Unpooled.buffer();
                tmp.writeLong(fileSize);
                ctx.writeAndFlush(tmp);

                long bytesSend = 0;
                while (bytesSend < fileSize) {
                    tmp = Unpooled.buffer();
                    int readByte = tmp.writeBytes(in.getChannel(), bytesSend, BUFFER_SIZE);
                    bytesSend += readByte;
                    ctx.writeAndFlush(tmp);
                }
                in.close();
                callback.callback();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void deleteFile() {
        try {
            Files.delete(Paths.get(pathName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFinish(ChannelHandlerContext ctx) {
        ByteBuf tmp = Unpooled.buffer();
        tmp.writeByte(CommandCodes.FINISH_COMMAND);
        ctx.writeAndFlush(tmp);
        tmp.clear();
    }
}
