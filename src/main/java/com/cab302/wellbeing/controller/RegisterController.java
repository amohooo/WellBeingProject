package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.DataBaseConnection;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    TextField txtFName;
    @FXML
    TextField txtLName;
    @FXML
    TextField txtUsername;
    @FXML
    TextField txtEmail;
    @FXML
    PasswordField ptxtPwd;
    @FXML
    PasswordField ptxtRetp;
    @FXML
    RadioButton radbAdm;
    @FXML
    private Button btnRgst, btnCncl;
    @FXML
    Label lblMsg;
    @FXML
    CheckBox ckUser;

    public void registerUser() {
        DataBaseConnection connectNow = new DataBaseConnection();
        Connection connectDB = connectNow.getConnection();

        String AccType = radbAdm.isSelected() ? "Admin" : "General";

        if (!validateInputs()) {
            return; // Exit if inputs are not valid
        }

        String hashedPassword = BCrypt.hashpw(ptxtPwd.getText(), BCrypt.gensalt());

        String registerUser = "INSERT INTO useraccount (userName, firstName, lastName, passwordHash, emailAddress, accType) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connectDB.prepareStatement(registerUser)) {
            preparedStatement.setString(1, txtUsername.getText());
            preparedStatement.setString(2, txtFName.getText());
            preparedStatement.setString(3, txtLName.getText());
            preparedStatement.setString(4, hashedPassword);
            preparedStatement.setString(5, txtEmail.getText());
            preparedStatement.setString(6, AccType);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                lblMsg.setText("Successfully registered.");
                PauseTransition delay = new PauseTransition(Duration.seconds(0.5)); // Introduce a delay before closing the window for the test purpose
                delay.setOnFinished(event -> closeWindow());
                delay.play();
            } else {
                lblMsg.setText("Registration failed. Please try again.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            lblMsg.setText("Registration error: " + ex.getMessage());
        }
    }
    private void closeWindow() {
        // You can use any FXML component here to get the scene and window. Using lblMsg as an example.
        Stage stage = (Stage) lblMsg.getScene().getWindow();
        stage.close();  // Closes the current window
    }

    boolean validateInputs() {
        if (txtFName.getText().isBlank() || txtLName.getText().isBlank() || txtUsername.getText().isBlank() || ptxtPwd.getText().isBlank() || txtEmail.getText().isBlank() || ptxtRetp.getText().isBlank()) {
            lblMsg.setText("Please fill all the information above.");
            return false;
        }

        if (!txtEmail.getText().contains("@")) {
            lblMsg.setText("Invalid email format.");
            return false;
        }

        if (!ptxtPwd.getText().equals(ptxtRetp.getText())) {
            lblMsg.setText("Passwords do not match.");
            return false;
        }

        if (usernameExists(txtUsername.getText())) {
            lblMsg.setText("Username already exists. Please choose a different one.");
            return false;
        }

        if (emailExists(txtEmail.getText())) {
            lblMsg.setText("Email address already exists. Please fill in a different one.");
            return false;
        }

        if (!ckUser.isSelected()) {
            lblMsg.setText("You must agree to the user agreement to register.");
            return false;
        }

        return true;
    }

    public void setBtnRgst(ActionEvent e) {
        registerUser(); // Just call registerUser without parameters
    }

    public void setBtnCncl(ActionEvent e) {
        closeWindow();
    }

    private boolean usernameExists(String username) {
        return exists("Username", username);
    }

    private boolean emailExists(String email) {
        return exists("emailAddress", email);
    }

    private boolean exists(String columnName, String value) {
        try (Connection connectDB = new DataBaseConnection().getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement("SELECT COUNT(*) FROM useraccount WHERE " + columnName + " = ?")) {
            preparedStatement.setString(1, value);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // True if count is greater than 0
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblMsg.setText("Failed to validate " + columnName + ": " + e.getMessage());
        }
        return false;
    }
    public void switchToUserAgreementScene(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/UserAgreement.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Explorer");
            stage.setScene(new Scene(root1));
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading InternetExplorer.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
