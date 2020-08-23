package netty.byte_release.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import netty.byte_release.Callback;
import netty.byte_release.CommandCodes;
import netty.byte_release.StateProcess;
import netty.byte_release.StateWork;
import netty.byte_release.utils.AuthoriseUtil;
import netty.byte_release.utils.DataUtil;
import netty.byte_release.utils.StartServer;
import org.apache.log4j.Logger;

import java.io.IOException;

public class InboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(StartServer.class);
    private StateWork workState;
    private StateProcess processState;
    private String login;
    private AuthoriseUtil auth;
    private DataUtil dataUtil;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        workState = StateWork.IDLE;
        processState = StateProcess.IDLE;
        logger.info("Клиент подключился: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        StartServer.getDbService().setIsLogin(login, false);
        logger.info("Клиент отключился: " + ctx.channel().remoteAddress() + " Login: " + login);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
            switch (workState) {
                case IDLE: {
                    byte command = buf.readByte();
                    if (command == CommandCodes.AUTHORISE_COMMAND) {
                        auth = new AuthoriseUtil();
                        workState = StateWork.AUTH;
                        processState = StateProcess.LOGIN_SIZE;
                    } else if (command == CommandCodes.REGISTRATION_COMMAND) {
                        auth = new AuthoriseUtil();
                        workState = StateWork.REGISTRATION;
                        processState = StateProcess.LOGIN_SIZE;
                    } else if (command == CommandCodes.GET_ROOT_PATH_COMMAND) {
                        dataUtil.sendClientDir(ctx, login);
                        workState = StateWork.IDLE;
                    } else if (command == CommandCodes.LIST_COMMAND) {
                        workState = StateWork.FILE_LIST;
                        processState = StateProcess.PATH_SIZE;
                    } else if (command == CommandCodes.TO_SERVER_COMMAND) {
                        workState = StateWork.FILE_GET;
                        processState = StateProcess.PATH_SIZE;
                    } else if (command == CommandCodes.DELETE_FILE_COMMAND) {
                        workState = StateWork.FILE_DELETE;
                        processState = StateProcess.PATH_SIZE;
                    } else if (command == CommandCodes.FROM_SERVER_COMMAND) {
                        workState = StateWork.FILE_PUT;
                        processState = StateProcess.PATH_SIZE;
                    }
                    break;
                }
                case AUTH: {
                    receiveLoginPassword(buf, () -> {
                        ByteBuf tmp = Unpooled.buffer();
                        if (auth.isAlreadyLogin()) {
                            tmp.writeByte(CommandCodes.ALREADY_AUTH_COMMAND);
                            ctx.writeAndFlush(tmp);
                        } else if (auth.isSuccessAut()) {
                            tmp.writeByte(CommandCodes.SUCCESS_AUTH_COMMAND);
                            this.login = auth.getLogin();
                            dataUtil = new DataUtil();
                            ctx.writeAndFlush(tmp);
                        } else {
                            tmp.writeByte(CommandCodes.FAIL_AUTH_COMMAND);
                            ctx.writeAndFlush(tmp);
                        }
                        tmp.clear();
                    });

                    break;
                }
                case REGISTRATION: {
                    receiveLoginPassword(buf, () -> {
                        ByteBuf tmp = Unpooled.buffer();
                        if (auth.isLoginExist()) {
                            tmp.writeByte(CommandCodes.LOGIN_EXIST_COMMAND);
                        } else {
                            auth.registration();
                            tmp.writeByte(CommandCodes.SUCCESS_AUTH_COMMAND);
                        }
                        ctx.writeAndFlush(tmp);
                    });
                    break;
                }
                case FILE_LIST: {
                    switch (processState) {
                        case PATH_SIZE:
                            if (dataUtil.getPathSize(buf)) {
                                processState = StateProcess.PATH_STRING;
                            }
                            break;
                        case PATH_STRING:
                            if (dataUtil.getPathName(buf, false)) {
                                dataUtil.sendFileList(ctx);
                                processState = StateProcess.IDLE;
                                workState = StateWork.IDLE;
                            }
                            break;
                    }
                    break;
                }
                case FILE_GET: {
                    switch (processState) {
                        case PATH_SIZE:
                            if (dataUtil.getPathSize(buf)) {
                                processState = StateProcess.PATH_STRING;
                            }
                            break;
                        case PATH_STRING:
                            if (dataUtil.getPathName(buf, true)) {
                                processState = StateProcess.FILE_SIZE;
                            }
                            break;
                        case FILE_SIZE:
                            if (dataUtil.getFileSize(buf)) {
                                processState = StateProcess.FILE;
                            }
                            break;
                        case FILE:
                            dataUtil.getFile(ctx, buf, () -> {
                                processState = StateProcess.IDLE;
                                workState = StateWork.IDLE;
                            });
                            break;
                    }
                    break;
                }
                case FILE_PUT: {
                    switch (processState) {
                        case PATH_SIZE:
                            if (dataUtil.getPathSize(buf)) {
                                processState = StateProcess.PATH_STRING;
                            }
                            break;
                        case PATH_STRING:
                            if (dataUtil.getPathName(buf, true)) {
                                dataUtil.putFile(ctx, () -> {
                                    processState = StateProcess.IDLE;
                                    workState = StateWork.IDLE;
                                });
                            }
                            break;
                    }
                    break;
                }
                case FILE_DELETE: {
                    switch (processState) {
                        case PATH_SIZE:
                            if (dataUtil.getPathSize(buf)) {
                                processState = StateProcess.PATH_STRING;
                            }
                            break;
                        case PATH_STRING:
                            if (dataUtil.getPathName(buf, true)) {
                                dataUtil.deleteFile();
                                processState = StateProcess.IDLE;
                                workState = StateWork.IDLE;
                            }
                            break;
                    }
                    break;
                }
            }
        }
        buf.release();
    }

    private void receiveLoginPassword(ByteBuf buf, Callback callback) {
        switch (processState) {
            case LOGIN_SIZE:
                if (auth.setLoginSize(buf)) {
                    processState = StateProcess.LOGIN_STRING;
                }
                break;
            case LOGIN_STRING:
                if (auth.setLogin(buf)) {
                    processState = StateProcess.PASS_SIZE;
                }
                break;
            case PASS_SIZE:
                if (auth.setPassSize(buf)) {
                    processState = StateProcess.PASS_STRING;
                }
                break;
            case PASS_STRING:
                if (auth.setPassword(buf)) {
                    callback.callback();
                    workState = StateWork.IDLE;
                    processState = StateProcess.IDLE;
                }
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            logger.info("Клиент разорвал соединение: " + ctx.channel().remoteAddress() + " Login: " + login);
            StartServer.getDbService().setIsLogin(login, false);
        } else {
            cause.printStackTrace();
            logger.fatal("exceptionCaught: " + cause.getMessage());
        }
        ctx.close();
    }

}
