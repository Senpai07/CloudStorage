package netty.byte_release;

public enum StateWork {
    IDLE,
    AUTH,
    REGISTRATION,
    FILE_LIST,
    FILE_GET,
    FILE_PUT,
    FILE_DELETE
}
