package netty.byte_release;

public enum StateProcess {
    IDLE,
    LOGIN_SIZE,
    LOGIN_STRING,
    PASS_SIZE,
    PASS_STRING,
    PATH_SIZE,
    PATH_STRING,
    FILE_SIZE,
    FILE
}
