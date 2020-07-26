package io;

import java.io.*;
import java.net.Socket;

public class FileUtility {

    public static void createFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public static void createDirectory(String dirName) {
        File file = new File(dirName);
        if (!file.exists()) {
            file.mkdir();
            System.out.println("Created directory: " + dirName);
        }
    }


    public static void move(File dir, File file) throws IOException {
        String path = dir.getAbsolutePath() + "/" + file.getName();
        createFile(path);
        InputStream is = new FileInputStream(file);
        try (OutputStream os = new FileOutputStream(new File(path))) {
            byte[] buffer = new byte[8192];
            while (is.available() > 0) {
                int readBytes = is.read(buffer);
                System.out.println(readBytes);
                os.write(buffer, 0, readBytes);
            }
        }
    }

    public static void sendFile(Socket socket, File file, String login) throws IOException {
        InputStream fis = new FileInputStream(file);
        long size = file.length();
        int count = (int) (size / 8192) / 10, readBuckets = 0;
        if (count == 0) count = 1;
        // /==========/
        try (DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
            byte[] buffer = new byte[8192];
            os.writeUTF("upload");
            os.writeUTF(login);
            os.writeUTF(file.getName());
            System.out.print("/");
            while (fis.available() > 0) {
                int readBytes = fis.read(buffer);
                readBuckets++;
                if (readBuckets % count == 0) {
                    System.out.print("=");
                }
                os.write(buffer, 0, readBytes);
            }
            System.out.println("/");
            System.out.println("File " + file.getName()
                    + " uploaded (" + file.length() + " bytes write)");
        }
    }

    public static void receiveFile(Socket socket, File file, String login) throws IOException {
        int bytesRead;
        int current = 0;
        InputStream ins = socket.getInputStream();
        OutputStream outs = socket.getOutputStream();

        try (DataInputStream in = new DataInputStream(ins); DataOutputStream out = new DataOutputStream(outs)) {


            out.writeUTF("download");
            out.writeUTF(login);
            out.writeUTF(file.getName());

            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            try {

                // receive file
                byte[] bytearray = new byte[8192];
                file.createNewFile();
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);
                bytesRead = in.read(bytearray, 0, bytearray.length);
                current += bytesRead;

//            do {
//                bytesRead =
//                        in.read(bytearray, current, (bytearray.length - current));
//                if (bytesRead >= 0) current += bytesRead;
//            } while (bytesRead > -1);


//            while (true) {
//                bytesRead =
//                        in.read(bytearray);
//                if (bytesRead == -1) break;
//                fos.write(bytearray, 0, bytesRead);
//                current += bytesRead;
//            }
                bos.write(bytearray, 0, bytesRead);
                bos.flush();

                System.out.println("File " + file.getName()
                        + " downloaded (" + current + " bytes read)");
            } finally {
                if (fos != null) fos.close();
                if (bos != null) bos.close();
            }
        }
    }

    public static void getListFiles(Socket socket, String login) throws IOException {
        InputStream ins = socket.getInputStream();
        OutputStream outs = socket.getOutputStream();

        try (DataInputStream in = new DataInputStream(ins); DataOutputStream out = new DataOutputStream(outs)) {
            out.writeUTF("list");
            out.writeUTF(login);
            out.writeUTF("");
            String listFiles = in.readUTF();
            System.out.println("List files: " + listFiles);
        }
    }

}
