package netty.object_release;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import netty.object_release.FileDeleteMessage;
import netty.object_release.FileListMessage;
import netty.object_release.FileMessage;
import netty.object_release.FileRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) {
        System.out.println("channelRegistered!");
        FileListMessage flm = new FileListMessage(getFilesList());
        ctx.writeAndFlush(flm);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("channelActive!");
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx,
                            final Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }

            if (msg instanceof FileListMessage) {
                FileListMessage flm = new FileListMessage(getFilesList());
                ctx.writeAndFlush(flm);
            }

            if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(
                        Paths.get("storage_server/storage/" + fr.getFileName()))) {
                    FileMessage fm = new FileMessage(
                            Paths.get("storage_server/storage/" + fr.getFileName()));
                    ctx.writeAndFlush(fm);
                }
            }

            if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                Files.write(
                        Paths.get(
                                "storage_server/storage/" + fm.getFileName()),
                        fm.getData(),
                        StandardOpenOption.CREATE);

                FileListMessage flm = new FileListMessage(getFilesList());
                ctx.writeAndFlush(flm);
            }


            if (msg instanceof FileDeleteMessage) {
                FileDeleteMessage fdr = (FileDeleteMessage) msg;
                if (Files.exists(
                        Paths.get("storage_server/storage/" + fdr.getFilename()))) {
                    Files.delete(
                            Paths.get("storage_server/storage/" + fdr.getFilename()));

                    FileListMessage flm = new FileListMessage(getFilesList());
                    ctx.writeAndFlush(flm);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx,
                                final Throwable cause) {
        ctx.close();
    }

    private List<String> getFilesList() {
        List<String> files = new ArrayList<>();
        try {
            Files.newDirectoryStream(
                    Paths.get("storage_server/storage/")).forEach(
                    path -> files.add(path.getFileName().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }
}