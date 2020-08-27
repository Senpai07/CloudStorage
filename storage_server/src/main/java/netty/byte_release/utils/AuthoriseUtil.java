package netty.byte_release.utils;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class AuthoriseUtil {
    private int loginSize;
    private int passSize;
    private String login;
    private String password;

    public boolean setLoginSize(ByteBuf buf) {
        if (buf.readableBytes() >= 4) {
            loginSize = buf.readInt();
            return true;
        }
        return false;
    }

    public boolean setLogin(ByteBuf buf) {
        if (buf.readableBytes() >= loginSize) {
            byte[] loginBuf = new byte[loginSize];
            buf.readBytes(loginBuf);
            login = new String(loginBuf, StandardCharsets.UTF_8);
            return true;
        }
        return false;
    }

    public boolean setPassSize(ByteBuf buf) {
        if (buf.readableBytes() >= 4) {
            passSize = buf.readInt();
            return true;
        }
        return false;
    }

    public boolean setPassword(ByteBuf buf) {
        if (buf.readableBytes() >= passSize) {
            byte[] passBuf = new byte[passSize];
            buf.readBytes(passBuf);
            password = new String(passBuf, StandardCharsets.UTF_8);
            return true;
        }
        return false;
    }

    public boolean isAlreadyLogin() {
        return StartServer.getDbService().isLogin(login);
    }

    public boolean isSuccessAut() {
        return StartServer.getDbService().isAuthorise(login, password);
    }

    public boolean isLoginExist() {
        return StartServer.getDbService().isLoginExist(login);
    }

    public void registration() {
        StartServer.getDbService().registration(login, password);
    }

    public String getLogin() {
        return login;
    }

}
