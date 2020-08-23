package netty.byte_release.utils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import netty.byte_release.handler.InboundHandler;
import netty.byte_release.db.DBAuthService;
import org.apache.log4j.Logger;

public class StartServer {
    private final int port;
    private static final DBAuthService dbService = new DBAuthService();
    private static final Logger logger = Logger.getLogger(StartServer.class);

    public StartServer(int port) {
        this.port = port;
    }

    public static DBAuthService getDbService() {
        return dbService;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    new InboundHandler()
                            );
                        }
                    });
            ChannelFuture f = b.bind(port).sync();
            logger.info("Сервер запущен на порту " + port);
            dbService.start();
            f.channel().closeFuture().sync();
        } finally {
            logger.info("Сервер остановлен.");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            dbService.stop();
        }
    }
}

