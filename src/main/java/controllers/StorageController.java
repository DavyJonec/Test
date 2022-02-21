package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.CloudMessage;
import model.FileMessage;
import model.FileRequest;
import model.ListMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class StorageController implements Initializable {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextArea textArea;

    @FXML
    public ListView<String> filesStorage;


    private Path clientDir;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;


    // read from network
    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = (CloudMessage) is.readObject();
                log.info("received: {}", message);
                switch (message.getType()) {
                    case FILE -> processFileMessage((FileMessage) message);
                    case LIST -> processListMessage((ListMessage) message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processListMessage(ListMessage message) {
        Platform.runLater(() -> {
            filesStorage.getItems().clear();
            filesStorage.getItems().addAll(message.getFiles());
        });
    }

    private void processFileMessage(FileMessage message) throws IOException {
        Files.write(clientDir.resolve(message.getFileName()), message.getBytes());
        Platform.runLater(this::updateClientView);
    }

    private void updateClientView() {
//        try {
//            clientView.getItems().clear();
//            Files.list(clientDir)
//                    .map(p -> p.getFileName().toString())
//                    .forEach(f -> clientView.getItems().add(f));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            clientDir = Paths.get(System.getProperty("user.home"), "Downloads");
            updateClientView();
            initMouseListeners();
            Socket socket = new Socket("localhost", 8189);
            System.out.println("Network created...");
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMouseListeners() {

//        clientView.setOnMouseClicked(e -> {
//            if (e.getClickCount() == 2) {
//                Path current = clientDir.resolve(getItem());
//                if (Files.isDirectory(current)) {
//                    clientDir = current;
//                    Platform.runLater(this::updateClientView);
//                }
//            }
//        });

    }

//    private String getItem() {
//        return clientView.getSelectionModel().getSelectedItem();
//    }

    public void sendText(ActionEvent actionEvent) {

    }

    public void upload(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Files");
        Stage stage = (Stage) anchorPane.getScene().getWindow();

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("Text file", "*.txt")
        );

        List<File> list = fileChooser.showOpenMultipleDialog(stage);
        ObservableList<File> files = FXCollections.observableList(list);
        for (File file : files) {
            os.writeObject(new FileMessage(clientDir.resolve(String.valueOf(file))));
        }

//        String fileName = clientView.getSelectionModel().getSelectedItem();
//        os.writeObject(new FileMessage(clientDir.resolve(fileName)));
    }

    public void downloadFileAction(ActionEvent actionEvent) throws IOException {

        String fileName = filesStorage.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileName));
    }

    @FXML
    void about(ActionEvent event) {

    }

    @FXML
    void deleteAllFiles(ActionEvent event) {
        filesStorage.getItems().clear();
        Paths.get("data").toFile().listFiles(File::delete);
    }

    @FXML
    void deleteFile(ActionEvent event) throws IOException {
        int index = filesStorage.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            String file = filesStorage.getItems().remove(index);
            Paths.get("data/"+ file).toFile().delete();
        }
    }

    @FXML
    void downloadFile(ActionEvent event) {

    }


}
