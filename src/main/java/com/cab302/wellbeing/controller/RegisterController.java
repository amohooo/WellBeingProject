package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.DataBaseConnection;
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

/**
 * This class is responsible for controlling the registration process.
 * It provides functionalities such as validating user inputs, registering a new user, and closing the window.
 */
public class RegisterController {
    /**
     * Text fields for user information
     */
    @FXML
    public TextField txtFName, txtLName, txtUsername, txtEmail, txtA1, txtA2, txtRegiCode; // Text fields for user information
    /**
     * Password fields for password and retype password
     */
    @FXML
    public PasswordField ptxtPwd, ptxtRetp; // Password fields for password and retype password
    /**
     * Radio buttons for account type
     */
    @FXML
    public RadioButton radbAdm, radbGen, radbDev; // Radio buttons for account type
    /**
     * Toggle group for account type
     */
    @FXML
    private ToggleGroup accTypeGroup; // Toggle group for account type
    @FXML
    private Button btnRgst, btnCncl; // Buttons for register and cancel
    /**
     * Choice boxes for security questions
     */
    @FXML
    public ChoiceBox<String> chbQ1, chbQ2; // Choice boxes for security questions
    /**
     * Label for messages
     */
    @FXML
    public Label lblMsg; // Label for messages
    /**
     * Check box for user agreement
     */
    @FXML
    public CheckBox ckUser; // Check box for user agreement

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        loadQuestionsToChoiceBox(chbQ1, "PwdQuestions1", "Question_1"); // Load questions to choice box 1
        loadQuestionsToChoiceBox(chbQ2, "PwdQuestions2", "Question_2"); // Load questions to choice box 2

        accTypeGroup = new ToggleGroup();  // Create a new toggle group for account type
        radbAdm.setToggleGroup(accTypeGroup);
        radbGen.setToggleGroup(accTypeGroup);
        radbDev.setToggleGroup(accTypeGroup);

        // Initially disable the Developer Radio Button
        radbDev.setDisable(true);

        // Listen for changes in the registration code
        txtRegiCode.textProperty().addListener((observable, oldValue, newValue) -> toggleRadioButtonsBasedOnCode());
    }

    /**
     * Load questions to the choice box.
     *
     * @param choiceBox     The choice box to load the questions to
     * @param tableName     The name of the table to load the questions from
     * @param questionColumn The name of the column containing the questions
     */
    private void loadQuestionsToChoiceBox(ChoiceBox<String> choiceBox, String tableName, String questionColumn) {
        String query = "SELECT " + questionColumn + " FROM " + tableName; // Query to select questions from the table
        // Try to load the questions to the choice box
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

    /**
     * Register a new user.
     */
    @FXML
    public void registerUser() {
        // Validate the inputs
        if (!validateInputs()) {
            return; // Exit if inputs are not valid
        }
        String accType = determineAccountType(txtRegiCode.getText()); // Determine the account type
        String hashedPassword = BCrypt.hashpw(ptxtPwd.getText(), BCrypt.gensalt()); // Hash the password
        String hashedAnswer1 = BCrypt.hashpw(txtA1.getText(), BCrypt.gensalt()); // Hash the first answer
        String hashedAnswer2 = BCrypt.hashpw(txtA2.getText(), BCrypt.gensalt()); // Hash the second answer
        // Try to register the user
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

    /**
     * Determine the account type based on the registration code.
     *
     * @param registrationCode The registration code to determine the account type
     * @return The account type
     */
    private String determineAccountType(String registrationCode) {
        // Check if the registration code is a developer code
        if (checkDeveloperCode(registrationCode)) {
            return "Developer";
        } else {
            return radbAdm.isSelected() ? "Admin" : "General"; // Return the selected account type
        }
    }

    /**
     * Check if the registration code is a valid developer code.
     *
     * @param code The registration code to check
     * @return True if the code is a valid developer code, false otherwise
     */
    private boolean checkDeveloperCode(String code) {
        String query = "SELECT COUNT(*) FROM developer WHERE RegistrationCode = ?"; // Query to check the developer code
        // Try to check the developer code
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

    /**
     * Toggle radio buttons based on the registration code.
     */
    private void toggleRadioButtonsBasedOnCode() {
        // Check if the registration code is a developer code, else disable the Developer Radio Button
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

    /**
     * Get the ID of a question from the database.
     *
     * @param question      The question to get the ID of
     * @param tableName     The name of the table to get the ID from
     * @param questionColumn The name of the column containing the questions
     * @param idColumn      The name of the column containing the ID
     * @param connectDB     The database connection
     * @return The ID of the question
     */
    private int getQuestionID(String question, String tableName, String questionColumn, String idColumn, Connection connectDB) {
        String query = "SELECT " + idColumn + " FROM " + tableName + " WHERE " + questionColumn + " = ?"; // Query to get the question ID
        // Try to get the question ID
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

    /**
     * Close the window with a delay.
     */
    private void closeWindowWithDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(0.2)); // Set the delay to 0.2 seconds
        delay.setOnFinished(event -> closeWindow()); // Close the window after the delay
        delay.play(); // Start the delay
    }

    /**
     * Close the window.
     */
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

    /**
     * Validate the user inputs.
     *
     * @return True if the inputs are valid, false otherwise
     */
    public boolean validateInputs() {
        if (txtFName.getText().isEmpty() || txtLName.getText().isEmpty() || txtUsername.getText().isEmpty() || ptxtPwd.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || ptxtRetp.getText().isEmpty() || chbQ1.getSelectionModel().isEmpty() || chbQ2.getSelectionModel().isEmpty() ||
                txtA1.getText().trim().isEmpty() || txtA2.getText().trim().isEmpty()) {
            lblMsg.setText("Please fill all the information above.");
            return false;
        }
        // Check if the email is in a valid format
        if (!txtEmail.getText().contains("@")) {
            lblMsg.setText("Invalid email format.");
            return false;
        }
        // Check if the password and retype password match
        if (!ptxtPwd.getText().equals(ptxtRetp.getText())) {
            lblMsg.setText("Passwords do not match.");
            return false;
        }
        // Check if the password is at least 8 characters long
        if (usernameExists(txtUsername.getText())) {
            lblMsg.setText("Username already exists. Please choose a different one.");
            return false;
        }
        // Check if the username already exists
        if (emailExists(txtEmail.getText())) {
            lblMsg.setText("Email address already exists. Please use a different one.");
            return false;
        }
        // Check if the username already exists
        if (!ckUser.isSelected()) {
            lblMsg.setText("You must agree to the user agreement to register.");
            return false;
        }
        return true;
    }

    /**
     * Set the register button.
     * @param e The action event
     */
    public void setBtnRgst(ActionEvent e) {
    registerUser(); // Just call registerUser without parameters
    }

    /**
     * Set the cancel button.
     * @param e The action event
     */
    public void setBtnCncl(ActionEvent e) {
        closeWindow();
    }

    /**
     * Check if a username already exists in the database.
     *
     * @param username The username to check
     * @return True if the username exists, false otherwise
     */
    private boolean usernameExists(String username) {
        return exists("userName", username);
    }

    /**
     * Check if an email already exists in the database.
     *
     * @param email The email to check
     * @return True if the email exists, false otherwise
     */
    private boolean emailExists(String email) {
        return exists("emailAddress", email);
    }

    /**
     * Check if a value exists in the database.
     *
     * @param columnName The name of the column to check
     * @param value      The value to check
     * @return True if the value exists, false otherwise
     */
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

    /**
     * Switch to the user agreement scene.
     *
     * @param event The action event
     */
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