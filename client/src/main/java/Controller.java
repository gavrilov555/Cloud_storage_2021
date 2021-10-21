
import corePackage.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


@Slf4j
public class Controller implements Initializable {

    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField input;
    public TextField clientPath;
    public TextField serverPath;
    private Path currentDir = Paths.get("D:\\cloud_storage_2021\\server", "root");
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    public AnchorPane mainScene;

    public TextField loginField;
    public TextField passwordField;
    public Button Authorization;

    private Net net;

    public void sendLoginAndPassword (ActionEvent actionEvent) {

        String login = loginField.getText();
        String password = passwordField.getText();
        loginField.clear();
        passwordField.clear();
        net.sendCommand(new AuthRequest(login,password));
    }

    public void sendFile (ActionEvent actionEvent) throws IOException {

        String fileName = input.getText();
        input.clear();
        Path file = Paths.get(String.valueOf(currentDir.resolve(fileName)));
        net.sendCommand(new FileMessage(file));
    }

    public void receiveArrayFiles(ActionEvent actionEvent) {
        net.sendCommand(new ListRequest());
    }

    public void updateArrayFiles(ActionEvent actionEvent) throws IOException {
        refreshClientView();
    }

    public void receiveFile(ActionEvent actionEvent) {
        String fileName = input.getText();
        input.clear();
        Path file = Paths.get(fileName);
        net.sendCommand(new FileRequest(file));
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            String userDir = System.getProperty("user.name");
            currentDir = Paths.get("client", "root");
            log.info("Current user: {}", System.getProperty("user.name"));
            refreshClientView();
            addNavigationListeners();
        }catch (IOException e) {
            e.printStackTrace();
        }

        net = Net.getInstance(cmd -> {
            switch (cmd.getType()) {
                case LIST_RESPONSE:
                    ListResponse listResponse = (ListResponse) cmd;
                    refreshServerView(listResponse.getNames());
                    break;
                case FILE_MESSAGE:
                    FileMessage fileMessage = (FileMessage) cmd;
                    Files.write(
                            currentDir.resolve(fileMessage.getName()),
                            fileMessage.getBytes()
                    );
                    refreshClientView();
                    break;
                case PATH_RESPONSE:
                    PathResponse pathResponse = (PathResponse) cmd;
                    serverPath.setText(pathResponse.getPath());
                    break;
                case AUTH_RESPONSE:
                    AuthResponse authResponse = (AuthResponse) cmd;
                    log.debug("AuthResponse {}", authResponse.getAuthStatus());
                    if (authResponse.getAuthStatus()) {
                        mainScene.setVisible(true);
                        loginField.setVisible(false);
                        passwordField.setVisible(false);
                        Authorization.setVisible(false);
                        net.sendCommand(new ListRequest());
                    } else {

                    }

                    break;
                default:
                    log.debug("Invalid command {}", cmd.getType());
            }
                }
        );
    }

    private void refreshClientView() throws IOException {
        clientPath.setText(currentDir.toString());
        List<String> names = Files.list(currentDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(() -> {
            clientView.getItems().clear();
            clientView.getItems().addAll(names);
        });
    }

    private void refreshServerView(List<String> names) {
        Platform.runLater(() -> {
            serverView.getItems().clear();
            serverView.getItems().addAll(names);
        });
    }

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        FileMessage message = new FileMessage(currentDir.resolve(fileName));
        os.writeObject(message);
        os.flush();
    }

    public void downLoad(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(Paths.get(fileName)));
        os.flush();
    }

    public void deleteClient (ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        try {
            Files.delete(currentDir.resolve(fileName));
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n", currentDir);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", currentDir);
        } catch (IOException x) {
            System.err.println(x);
        }
        os.flush();
    }

   /* public void deleteServer (ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        try {
            Files.delete();

    }

    не разобрался как удалять файлы с сервера

    */




  /*  public void rename (ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        Files.move(currentDir, currentDir.resolve(fileName));
    }

    не разобрался, как переменовать файл.... */


    private void addNavigationListeners() {
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = clientView.getSelectionModel().getSelectedItem();
                Path newPath = currentDir.resolve(item);
                if (Files.isDirectory(newPath)) {
                    currentDir = newPath;
                    try {
                        refreshClientView();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });

        serverView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = serverView.getSelectionModel().getSelectedItem();
                try {
                    os.writeObject(new PathInRequest(item));
                    os.flush();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    public void clientPathUp(ActionEvent actionEvent) throws IOException {
        currentDir = currentDir.getParent();
        clientPath.setText(currentDir.toString());
        refreshClientView();
    }

    public void serverPathUp(ActionEvent actionEvent) throws IOException {
        os.writeObject(new PathUpRequest());
        os.flush();
    }


}

