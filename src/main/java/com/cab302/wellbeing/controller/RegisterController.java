package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.*;
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
    public TextField txtFName, txtLName, txtUsername, txtEmail, txtA1, txtA2, txtRegiCode;
    @FXML
    public PasswordField ptxtPwd, ptxtRetp;
    @FXML
    public RadioButton radbAdm, radbGen, radbDev;
    @FXML
    private ToggleGroup accTypeGroup;
    @FXML
    private Button btnRgst, btnCncl;
    @FXML
    public ChoiceBox<String> chbQ1, chbQ2;
    @FXML
    public Label lblMsg;
    @FXML
    public CheckBox ckUser;

    @FXML
    public void initialize() {
        loadQuestionsToChoiceBox(chbQ1, "PwdQuestions1", "Question_1");
        loadQuestionsToChoiceBox(chbQ2, "PwdQuestions2", "Question_2");

        accTypeGroup = new ToggleGroup();
        radbAdm.setToggleGroup(accTypeGroup);
        radbGen.setToggleGroup(accTypeGroup);
        radbDev.setToggleGroup(accTypeGroup);

        // Initially disable the Developer Radio Button
        radbDev.setDisable(true);

        // Listen for changes in the registration code
        txtRegiCode.textProperty().addListener((observable, oldValue, newValue) -> toggleRadioButtonsBasedOnCode());
    }

    private void loadQuestionsToChoiceBox(ChoiceBox<String> choiceBox, String tableName, String questionColumn) {
        String query = "SELECT " + questionColumn + " FROM " + tableName;
        try (Connection connectDB = new DataBaseConnection().getConnection();
             PreparedStatement pstmt = connectDB.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String question = rs.getString(questionColumn);
                choiceBox.getItems().add(question);
            }
        } catch (SQLException e) {
            System.err.println("Error loading questions from " + tableName + ": " + e.getMessage());
        }
    }

    @FXML
    public void registerUser() {
        if (!validateInputs()) {
            return; // Exit if inputs are not valid
        }

        String accType = determineAccountType(txtRegiCode.getText());
        String hashedPassword = BCrypt.hashpw(ptxtPwd.getText(), BCrypt.gensalt());
        String hashedAnswer1 = BCrypt.hashpw(txtA1.getText(), BCrypt.gensalt());
        String hashedAnswer2 = BCrypt.hashpw(txtA2.getText(), BCrypt.gensalt());

        try (Connection connectDB = new DataBaseConnection().getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(
                     "INSERT INTO useraccount (userName, firstName, lastName, passwordHash, emailAddress, QuestionID_1, QuestionID_2, Answer_1, Answer_2, RegistrationCode, accType) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            int questionID1 = getQuestionID(chbQ1.getSelectionModel().getSelectedItem(), "PwdQuestions1", "Question_1", "QuestionID_1", connectDB);
            int questionID2 = getQuestionID(chbQ2.getSelectionModel().getSelectedItem(), "PwdQuestions2", "Question_2", "QuestionID_2", connectDB);

            preparedStatement.setString(1, txtUsername.getText());
            preparedStatement.setString(2, txtFName.getText());
            preparedStatement.setString(3, txtLName.getText());
            preparedStatement.setString(4, hashedPassword);
            preparedStatement.setString(5, txtEmail.getText());
            preparedStatement.setInt(6, questionID1);
            preparedStatement.setInt(7, questionID2);
            preparedStatement.setString(8, hashedAnswer1);
            preparedStatement.setString(9, hashedAnswer2);
            preparedStatement.setString(10, txtRegiCode.getText());
            preparedStatement.setString(11, accType);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                lblMsg.setText("Successfully registered.");
                closeWindowWithDelay();
            } else {
                lblMsg.setText("Registration failed. Please try again.");
            }
        } catch (SQLException ex) {
            lblMsg.setText("Registration error: " + ex.getMessage());
            System.err.println("SQL error during registration: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String determineAccountType(String registrationCode) {
        if (checkDeveloperCode(registrationCode)) {
            return "Developer";
        } else {
            return radbAdm.isSelected() ? "Admin" : "General";
        }
    }

    private boolean checkDeveloperCode(String code) {
        String query = "SELECT COUNT(*) FROM developer WHERE RegistrationCode = ?";
        try (Connection connectDB = new DataBaseConnection().getConnection();
             PreparedStatement pstmt = connectDB.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking developer code: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void toggleRadioButtonsBasedOnCode() {
        if (checkDeveloperCode(txtRegiCode.getText())) {
            radbAdm.setDisable(true);
            radbGen.setDisable(true);
            radbDev.setDisable(false);
            radbDev.setSelected(true);
        } else {
            radbAdm.setDisable(false);
            radbGen.setDisable(false);
            radbDev.setDisable(true);
            accTypeGroup.selectToggle(radbAdm.isSelected() ? radbAdm : radbGen);
        }
    }

    private int getQuestionID(String question, String tableName, String questionColumn, String idColumn, Connection connectDB) {
        String query = "SELECT " + idColumn + " FROM " + tableName + " WHERE " + questionColumn + " = ?";
        try (PreparedStatement pstmt = connectDB.prepareStatement(query)) {
            pstmt.setString(1, question);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(idColumn);
                } else {
                    throw new SQLException("Question not found: " + question);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving question ID from " + tableName + ": " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private void closeWindowWithDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(0.2));
        delay.setOnFinished(event -> closeWindow());
        delay.play();
    }

    @FXML
    public void closeWindow() {
        if (lblMsg != null && lblMsg.getScene() != null) {
            Stage stage = (Stage) lblMsg.getScene().getWindow();
            if (stage != null) {
                stage.close();
            } else {
                System.out.println("Stage is null, cannot close the window");
            }
        } else {
            System.out.println("Scene is null or lblMsg is null, cannot close the window");
        }
    }

    public boolean validateInputs() {
        if (txtFName.getText().isEmpty() || txtLName.getText().isEmpty() || txtUsername.getText().isEmpty() || ptxtPwd.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || ptxtRetp.getText().isEmpty() || chbQ1.getSelectionModel().isEmpty() || chbQ2.getSelectionModel().isEmpty() ||
                txtA1.getText().trim().isEmpty() || txtA2.getText().trim().isEmpty()) {
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
            lblMsg.setText("Email address already exists. Please use a different one.");
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
        return exists("userName", username);
    }

    private boolean emailExists(String email) {
        return exists("emailAddress", email);
    }

    private boolean exists(String columnName, String value) {
        String query = "SELECT COUNT(*) FROM useraccount WHERE " + columnName + " = ?";
        try (Connection connectDB = new DataBaseConnection().getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(query)) {
            preparedStatement.setString(1, value);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblMsg.setText("Failed to validate " + columnName + ": " + e.getMessage());
        }
        return false;
    }

    @FXML
    public void switchToUserAgreementScene(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/UserAgreement.fxml"));
            Parent root1 = fxmlLoader.load();
            UserAgreementController userAgreementController = fxmlLoader.getController();
            userAgreementController.setRegisterCheckbox(ckUser);

            Stage stage = new Stage();
            stage.setTitle("User Agreement");
            stage.setScene(new Scene(root1));
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading UserAgreement.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

//public void setBtnRgst(ActionEvent e) {
//    registerUser(); // Just call registerUser without parameters
//}
//
//public void setBtnCncl(ActionEvent e) {
//    closeWindow();
//}