package controllers;

import client.User;
import db.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class SignUpController {
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button authButton;

    @FXML
    private Text notAllFields;

    @FXML
    private RadioButton buttonFemale;

    @FXML
    private RadioButton buttonMale;

    @FXML
    private ToggleGroup gender;

    @FXML
    public TextField login_field;

    @FXML
    private PasswordField password_field;

    @FXML
    private TextField signUpCity;

    @FXML
    private TextField signUpCountry;

    @FXML
    private TextField signUpName;

    @FXML
    private TextField signUpSurname;

    @FXML
    void initialize() {
        authButton.setOnAction(actionEvent -> {
            try {
                signUpNewUser();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void signUpNewUser() throws IOException {
        Controller controller = new Controller();
        DatabaseHandler dbHandler = new DatabaseHandler();

        String login = login_field.getText();
        String password = password_field.getText();
        String name = signUpName.getText();
        String surName = signUpSurname.getText();
        String gender = "";
        String country = signUpCountry.getText();
        String city = signUpCity.getText();

        if (buttonMale.isSelected())
            gender = "Male";
        else
            gender = "Female";

        if (login.isEmpty() || password.isEmpty() || name.isEmpty() || surName.isEmpty() || country.isEmpty() || city.isEmpty()) {
            notAllFields.setVisible(true);
        } else {
            User user = new User(login, password, name, surName, gender, country, city);
            dbHandler.signUpUser(user);
            createDirectory(login);
            controller.openNewScene("/storageController.fxml", authButton, "Storage");
        }

    }

    public void createDirectory(String login) throws IOException {
        Path path = Paths.get("data/" + login);
        if(!Files.exists(path)){
            Files.createDirectory(path);
        }
    }

}