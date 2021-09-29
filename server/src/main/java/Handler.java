import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class Handler implements Runnable {

    private static final int BUFFER_SIZE = 256;
    private final String ROOT_DIR = "server/root";
    private byte [] buffer = new byte[BUFFER_SIZE];
    private final Socket socket;
    private DataOutputStream os;
    private DataInputStream is;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    @SneakyThrows
    @Override
    public void run() {
        try {
            os = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());
            while (true) {
                String fileName = is.readUTF();
                log.debug("Received: {}", fileName);
                long size = is.readLong();
                log.debug(" File size: {} ", size);
                int read;
                try (OutputStream fos = Files.newOutputStream(Paths.get(ROOT_DIR, fileName))) {
                    for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                        read = is.read(buffer);
                        fos.write(buffer, 0, read);
                    }
                } catch (Exception e) {
                    log.error(" problem with file system");
                }
                os.writeUTF("OK");
            }
        } catch (Exception e) {
            log.error("stacktrace: ", e);
            os.writeUTF("Error" + e.getMessage());
        }
        is.close();
        os.close();
    }
}

