import io.FileUtility;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class StorageServer {
    DataInputStream is;
    DataOutputStream os;
    ServerSocket server;

    public StorageServer() throws IOException {
        server = new ServerSocket(8189);
        while (true) {
            System.out.println("Waiting...");
            try {
                Socket socket = server.accept();

                System.out.println("Client accepted!");
                is = new DataInputStream(socket.getInputStream());
                os = new DataOutputStream(socket.getOutputStream());
                String command = is.readUTF();
                String login = is.readUTF();
                String fileName = is.readUTF();
                System.out.println("command: " + command);
                System.out.println("login: " + login);
                System.out.println("fileName: " + fileName);
                if (!login.isEmpty()) {
                    FileUtility.createDirectory("./storage_server/storage/" + login + "/");
                }
                File file;
                switch (command) {
                    case "upload":
                        file = new File("./storage_server/storage/" + login + "/" + fileName);
                        file.createNewFile();
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            byte[] buffer = new byte[8192];
                            while (true) {
                                int r = is.read(buffer);
                                if (r == -1) break;
                                fos.write(buffer, 0, r);
                            }
                        }
                        System.out.println("File uploaded!");
                        break;
                    case "download":
                        byte[] bytearray = new byte[1024 * 16];


                        file = new File("./storage_server/storage/" + login + "/" + fileName);
                        try (FileInputStream fis = new FileInputStream(file)) {
//                            BufferedInputStream bis = new BufferedInputStream(fis);

//                            int readLength = -1;
                            System.out.println("File " + file.getName() + " start downloading...");
                            while (fis.available() > 0) {
                                int readBytes = fis.read(bytearray);
                                os.write(bytearray, 0, readBytes);
                            }

//                            while ((readLength = bis.read(bytearray)) > 0) {
//                                os.write(bytearray, 0, readLength);
//                                os.flush();
//                            }
//                            bis.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        System.out.println("File downloaded!");
                        break;
                    case "list":
                        File myFolder = new File("./storage_server/storage/" + login + "/");
                        File[] files = myFolder.listFiles();
                        if (files != null) {
                            System.out.println(Arrays.toString(files));
                            for (File fileInList : files) {
                                System.out.println(fileInList.getName());
                            }
                            os.writeUTF(Arrays.toString(files)); // отсылаем клиенту список файлов
                            os.flush();
                        }

                        System.out.println("List sent!");
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new StorageServer();
    }
}
