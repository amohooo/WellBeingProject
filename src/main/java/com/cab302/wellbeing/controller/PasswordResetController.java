package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is responsible for controlling the password reset functionality.
 * It provides functionalities such as verifying answers and resetting the password.
 */
public class PasswordResetController {
    /**
     * Test field for email address, security answer 1, security answer 2.
     */
    @FXML
    public TextField txtEmailAdd, txtAn1, txtAn2; // Text field for email address
    /**
     * Test field for new password, re-enter new password.
     */
    @FXML
    public PasswordField ptxtPwd, ptxtRePwd; // Password field for new password
    /**
     * Label for the message, verification, question 1, question 2.
     */
    @FXML
    public Label lblMsg, lblVerify, lblQ1, lblQ2; // Label for the message
    /**
     * Button for reset, cancel, verify.
     */
    @FXML
    public Button btnReset, btnCncl, btnVerify;

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // Disable the reset button initially until answers are verified
        btnReset.setDisable(true);

        // Setup any initializations here, such as loading questions if needed
        lblVerify.setText("");
        lblMsg.setText("");

        // Optionally add listeners if needed
        txtEmailAdd.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Focus lost
                displayQuestions();
            }
        });
    }

    /**
     * This method is used to display the security questions.
     */
    public void displayQuestions() {
        String email = txtEmailAdd.getText().trim(); // Trim to remove leading/trailing spaces
        // Check if email is empty
        if (email.isEmpty()) {
            lblMsg.setText("Please enter your email address.");
            return;
        }
        // Fetch the questions from the database
        try {
            DataBaseConnection connectNow = new DataBaseConnection();
            Connection connectDB = connectNow.getConnection();
            String query = "SELECT QuestionID_1, QuestionID_2 FROM useraccount WHERE emailAddress = ?";
            PreparedStatement pst = connectDB.prepareStatement(query);
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int questionID1 = rs.getInt("QuestionID_1");
                int questionID2 = rs.getInt("QuestionID_2");
                System.out.println("Retrieved Question IDs: " + questionID1 + ", " + questionID2); // Logging for debugging
                lblQ1.setText(getQuestionByID(questionID1, "PwdQuestions1", connectDB));
                lblQ2.setText(getQuestionByID(questionID2, "PwdQuestions2", connectDB));
            } else {
                lblMsg.setText("No account associated with this email.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblMsg.setText("Error retrieving security questions: " + e.getMessage());
        }
    }

    /**
     * This method is used to get the question by ID.
     * @param questionID - the ID of the question
     * @param tableName - the name of the table
     * @param connectDB - the database connection
     * @return the question
     */
    private String getQuestionByID(int questionID, String tableName, Connection connectDB) {
        // Fetch the question from the database
        try {
            String questionColumn = tableName.equals("PwdQuestions1") ? "Question_1" : "Question_2";
            String idColumn = tableName.equals("PwdQuestions1") ? "QuestionID_1" : "QuestionID_2";
            String query = "SELECT " + questionColumn + " FROM " + tableName + " WHERE " + idColumn + " = ?";
            PreparedStatement pst = connectDB.prepareStatement(query);
            pst.setInt(1, questionID);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String question = rs.getString(questionColumn);
                System.out.println("Question Retrieved: " + question); // Logging for debugging
                return question;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblMsg.setText("Error retrieving question: " + e.getMessage());
        }
        return "Question not found";
    }

    /**
     * This method is used to verify the answers.
     */
    public void verifyAnswers() {
        String email = txtEmailAdd.getText(); // Get the email address
        String answer1 = txtAn1.getText(); // Get the answer 1
        String answer2 = txtAn2.getText(); // Get the answer 2
        // Check if any field is empty
        if (email.isEmpty() || answer1.isEmpty() || answer2.isEmpty()) {
            lblVerify.setText("Please fill in all fields for verification.");
            return;
        }
        // Verify the answers
        try {
            DataBaseConnection connectNow = new DataBaseConnection();
            Connection connectDB = connectNow.getConnection();
            String query = "SELECT Answer_1, Answer_2 FROM useraccount WHERE emailAddress = ?";
            PreparedStatement pst = connectDB.prepareStatement(query);
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String storedAnswer1 = rs.getString("Answer_1");
                String storedAnswer2 = rs.getString("Answer_2");

                if (BCrypt.checkpw(answer1, storedAnswer1) && BCrypt.checkpw(answer2, storedAnswer2)) {
                    lblVerify.setText("Your answers are correct. You can now reset your password.");
                    btnReset.setDisable(false); // Enable reset button if answers are correct
                } else {
                    lblVerify.setText("Incorrect answers. Please try again.");
                }
            } else {
                lblVerify.setText("No account associated with this email.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblVerify.setText("Error verifying answers: " + e.getMessage());
        }
    }

    /**
     * This method is used to reset the password.
     */
    public void resetPassword() {
        String email = txtEmailAdd.getText(); // Get the email address
        String newPassword = ptxtPwd.getText(); // Get the new password
        // Check if any field is empty
        if (email.isEmpty() || newPassword.isEmpty() || ptxtRePwd.getText().isEmpty()) {
            lblMsg.setText("Please fill in all fields.");
            return;
        }
        // Check if passwords match
        if (!newPassword.equals(ptxtRePwd.getText())) {
            lblMsg.setText("Passwords do not match.");
            return;
        }
        // Check if reset is allowed after verification
        if (!btnReset.isDisabled()) {
            // Reset the password
            try {
                DataBaseConnection connectNow = new DataBaseConnection();
                Connection connectDB = connectNow.getConnection();

                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                String query = "UPDATE useraccount SET passwordHash = ? WHERE emailAddress = ?";
                PreparedStatement pst = connectDB.prepareStatement(query);
                pst.setString(1, hashedPassword);
                pst.setString(2, email);
                int result = pst.executeUpdate();

                if (result > 0) {
                    lblMsg.setText("Password successfully reset. Check your email for the confirmation link.");
                    btnReset.setDisable(true); // Disable reset button after successful reset
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.1));
                    delay.setOnFinished(event -> closeWindow());
                    delay.play();
                } else {
                    lblMsg.setText("Failed to reset password.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                lblMsg.setText("Error: " + e.getMessage());
            }
        } else {
            lblMsg.setText("Please verify your answers before resetting your password.");
        }
    }

    /**
     * This method is used to close the window.
     */
    private void closeWindow() {
        // get the scene and window.
        Stage stage = (Stage) lblMsg.getScene().getWindow();
        stage.close();  // Closes the current window
    }

    /**
     * This method is used to check if the email exists.
     * @param email - the email address
     * @param connectDB - the database connection
     * @return true if the email exists, false otherwise
     */
    private boolean emailExists(String email, Connection connectDB) {
        try {
            PreparedStatement pst = connectDB.prepareStatement("SELECT COUNT(*) FROM useraccount WHERE emailAddress = ?");
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblMsg.setText("Error checking email: " + e.getMessage());
        }
        return false;
    }

    /**
     * This method is used to cancel the password reset.
     * @param e - the action event that triggered the cancel
     */
    public void btnExitOnAction(ActionEvent e) {
        closeWindow();
    }

}
