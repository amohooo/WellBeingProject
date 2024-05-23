package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.*;
import com.cab302.wellbeing.UserSession;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WellBeingController {
    @FXML
    public Button btnExit;
    @FXML
    Button btnRegst;
    @FXML
    public TextField txtUsr;
    @FXML
    public PasswordField txtPwd;
    @FXML
    public Label lblLoginMsg;

    private Parent root;
    private Scene scene;
    public Stage stage;

    private void switchToMainMenuScene(ActionEvent e, String firstName, String accType, int userId, String source) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/MainMenu.fxml"));
            Parent root = loader.load();

            MainMenuController mainMenuController = loader.getController();
            mainMenuController.displayName(firstName);
            mainMenuController.setFirstName(firstName);
            mainMenuController.setUserId(userId, source);
            mainMenuController.setAccType(accType);
            mainMenuController.applyModeColors();

            //Scene scene = new Scene(root);
            Stage stage;

            if (e != null) {
                stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) lblLoginMsg.getScene().getWindow();  // Fallback to the current stage
            }

            stage.setScene(new Scene(root));
            stage.setTitle("Main Menu");
            stage.show();

        } catch (IOException ex) {
            System.err.println("Error loading MainMenu.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    public void btnExitOnAction(ActionEvent e) {
        // lblLoginMsg.setText("See you around!");
        Stage stage = (Stage) btnExit.getScene().getWindow();
        PauseTransition delay = new PauseTransition(Duration.seconds(0.1)); // Introduce a delay before closing the window for the test purpose
        delay.setOnFinished(event -> stage.close());
        delay.play();

    }

    public void lblLoginMsgOnAction(ActionEvent e) {
        lblLoginMsg.setText("Your username or password is wrong");

        if (txtUsr.getText().isBlank()) {
            lblLoginMsg.setText("Please fill in your username");
        } else if (txtPwd.getText().isBlank()) {
            lblLoginMsg.setText("Please fill in your password");
        } else {
            validateLogin(e);
        }
    }
    public void validateLogin(ActionEvent e) {
        DataBaseConnection connectNow = new DataBaseConnection();
        Connection connectDB = connectNow.getConnection();

        String username = txtUsr.getText();
        String password = txtPwd.getText();  // This is the plaintext password entered by the user

        // Query to retrieve the user ID, hashed password, account type, and first name from the database for the given username
        String fetchUserDetails = "SELECT userId, passwordHash, AccType, firstName FROM useraccount WHERE UserName = ?";

        try {
            PreparedStatement preparedStatement = connectDB.prepareStatement(fetchUserDetails);
            preparedStatement.setString(1, username);

            ResultSet queryResult = preparedStatement.executeQuery();

            if (queryResult.next()) {
                int userId = queryResult.getInt("userId");  // Retrieve user ID
                String storedHash = queryResult.getString("passwordHash"); // Retrieved hashed password
                String accType = queryResult.getString("AccType");         // Account type
                String firstName = queryResult.getString("firstName");

                // Use BCrypt to check if the entered password matches the hashed password
                if (BCrypt.checkpw(password, storedHash)) {
                    lblLoginMsg.setText("Welcome " + firstName);
                    UserSession.getInstance().setCurrentUserId(userId);
                    UserSession.getInstance().setFirstName(firstName);
                    switchToMainMenuScene(e, firstName, accType, userId, "login");
                } else {
                    lblLoginMsg.setText("Your username or password is wrong");
                }
            } else {
                lblLoginMsg.setText("Your username or password is wrong");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            lblLoginMsg.setText("Failed to connect to database.");
        }
    }
    public void switchToRegisterScene(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/Register.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading InternetExplorer.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void openPasswordResetScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/PasswordReset.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Password Reset");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public void btnForgotPwdOnAction(ActionEvent e) {
        openPasswordResetScene();
    }

}