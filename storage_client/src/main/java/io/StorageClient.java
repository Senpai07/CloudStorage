package io;

import io.FileUtility;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class StorageClient {

    static String login = "2";

    public static void main(String[] args) throws IOException {

        FileUtility.sendFile(new Socket("localhost", 8189),
                new File("./storage_client/files/" + login + ".txt"), login);

        FileUtility.receiveFile(new Socket("localhost", 8189),
                new File("./storage_client/files/" + login + ".txt"), login);

        FileUtility.getListFiles(new Socket("localhost", 8189), login);
    }
}
