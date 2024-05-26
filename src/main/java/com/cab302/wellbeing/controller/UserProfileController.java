package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is a controller for the User Profile functionality in the application.
 * It provides methods to handle the user profile (name, email, password, etc.) of the application.
 */
public class UserProfileController {
    /**
     * Text field for username, first name, last name, email, password, answer 1, answer 2.
     */
    @FXML
    public TextField txtUserName, txtFirstName, txtLastName, txtEmail, txtPassword,txtA1, txtA2; // Text fields for user profile
    /**
     * Toggle group for account type.
     */
    @FXML
    public ToggleGroup accTypeGroup; // Toggle group for account type
    /**
     * Choice box for security questions.
     */
    @FXML
    public ChoiceBox<String> chbQ1, chbQ2; // Choice boxes for security questions
    /**
     * Button for cancel, save, verify.
     */
    @FXML
    public Button btnCancel, btnSave, btnVerify; // Buttons for user profile
    /**
     * Radio button for admin, general, developer.
     */
    @FXML
    public RadioButton radbAdm, radbGen, radbDev; // Radio buttons for account type
    /**
     * Label for background, message, user profile, user, first name, last name, email, password, account type, security question 1, security question 2, answer 1, answer 2.
     */
    @FXML
    public Label lblBkGrd, lblMsg, lblUserPro, lblUser, lblFirst, lblLast, lblEmail, lblPwd, lblAccType, lblSQ1, lblSQ2, lblAn1, lblAn2; // Labels for user profile
    /**
     * Pane for user profile.
     */
    @FXML
    private Pane paneProfile; // Pane for user profile
    private static Color lightColor = Color.web("#bfe7f7"); // Light color
    private static Color nightColor = Color.web("#777777"); // Night color
    private static Color autoColor = Color.web("#009ee0"); // Auto color
    private static Color eyeProtectColor = Color.web("#A3CCBE"); // Eye protect color
    /**
     * This method is used to set the user id.
     */
    int userId; // User ID
    private String accType; // Account type

    /**
     * This method is used to set and parse the current user's account type.
     * @param accType - user's account type
     */
    public void setUserType(String accType) {
        this.accType = accType;
        System.out.println("User Type: " + accType);
        if (accType.equals("General")) {
            radbAdm.setDisable(true);
            radbDev.setDisable(true);
            txtUserName.setDisable(true);
        } else if (accType.equals("Admin")) {
            radbDev.setDisable(true);
            txtUserName.setDisable(true);
        } else if (accType.equals("Developer")) {
            radbGen.setDisable(true);
            radbAdm.setDisable(true);
            txtUserName.setDisable(true);
        }
    }

    private DataBaseConnection dbConnection = new DataBaseConnection(); // Database connection

    /**
     * This method is used to set the user ID.
     * @param userId - the user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;  // Now you can use this userId to store browsing data linked to the user
    }

    /**
     * This method is used to initialize the user profile controller.
     */
    @FXML
    private void initialize() {
        // Disable the reset button initially until answers are verified
        btnSave.setDisable(true);

        lblMsg.setText("");
    }
    /**
     * This method is used to display the user name.
     * @param userName - the user name
     */
    public void displayUserName(String userName){
        this.txtUserName.setText(userName);
    } // Display the user name
    /**
     * This method is used to display the first name.
     * @param firstName - the first name
     */
    public void displayFirstName(String firstName){
        this.txtFirstName.setText(firstName);
    } // Display the first name
    /**
     * This method is used to display the last name.
     * @param lastName - the last name
     */
    public void displayLastName(String lastName){
        this.txtLastName.setText(lastName);
    } // Display the last name
    /**
     * This method is used to display the email.
     * @param email - the email
     */
    public void displayEmail(String email){
        this.txtEmail.setText(email);
    } // Display the email

    /**
     * This method is used to display the account type.
     * @param accType - the account type
     */
    public void displayAccType(String accType){
        if("Developer".equals(accType)){
            this.radbDev.setSelected(true);
            this.radbDev.setDisable(true);
            this.radbAdm.setDisable(true);
            this.radbGen.setDisable(true);
        }else if("Admin".equals(accType)){
            this.radbAdm.setSelected(true);
            this.radbAdm.setDisable(true);
            this.radbDev.setDisable(true);
            this.radbGen.setDisable(true);
        }else{
            this.radbGen.setSelected(true);
            this.radbGen.setDisable(true);
            this.radbDev.setDisable(true);
            this.radbAdm.setDisable(true);
        }
    }

    /**
     * This method is used to display the user profile.
     */
    public void displayUserProfile(){
        // Display the user profile
        try {
            Connection conn = dbConnection.getConnection(); // Get a fresh connection
            String selectQuery = " SELECT userId, userName, emailAddress, firstName, lastName, passwordHash, accType,Question_1, Question_2, Answer_1, Answer_2  FROM WellBeing.useraccount " +
                    " LEFT JOIN WellBeing.PwdQuestions1 ON WellBeing.PwdQuestions1.QuestionID_1 = WellBeing.useraccount.QuestionID_1 " +
                    " LEFT JOIN WellBeing.PwdQuestions2 ON WellBeing.PwdQuestions2.QuestionID_2 = WellBeing.useraccount.QuestionID_2 " +
                    " WHERE userId = " + this.userId;
            PreparedStatement preparedStatement = conn.prepareStatement(selectQuery);
            ResultSet queryResult = preparedStatement.executeQuery();
            if (queryResult.next()){
                String accType = queryResult.getString("accType");
                String userName = queryResult.getString("userName");// Account type
                String firstName = queryResult.getString("firstName");
                String lastName = queryResult.getString("lastName");
                String email = queryResult.getString("emailAddress");
                String q1 = queryResult.getString("Question_1");
                String q2 = queryResult.getString("Question_2");

                this.displayLastName(lastName);
                this.displayFirstName(firstName);
                this.displayEmail(email);
                this.displayUserName(userName);
                this.displayAccType(accType);
                this.chbQ1.setValue(q1);
                this.chbQ2.setValue(q2);
            }
        } catch(SQLException e){
                System.out.println("SQL Exception: " + e.getMessage());
                e.printStackTrace();
            }
    }

    /**
     * This method is used to save the user profile.
     */
    public void saveUserProfile(){
        if (!validateInputs()) {
            return; // Exit if inputs are not valid
        }
        String insertQuery = "UPDATE useraccount SET userName = ?, emailAddress = ?, firstName = ?, lastName = ?, passwordHash = ?, QuestionID_1 = ? , QuestionID_2 = ? , Answer_1 = ?, Answer_2 = ? WHERE userId = ?";
        try (Connection conn = dbConnection.getConnection(); // Get a fresh connection
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setString(1, this.txtUserName.getText());
            pstmt.setString(2, this.txtEmail.getText());
            pstmt.setString(3, this.txtFirstName.getText());
            pstmt.setString(4, this.txtLastName.getText());
            pstmt.setString(5, BCrypt.hashpw(this.txtPassword.getText(), BCrypt.gensalt()));
            int questionID1 = getQuestionID(chbQ1.getSelectionModel().getSelectedItem(), "PwdQuestions1", "Question_1", "QuestionID_1", conn);
            int questionID2 = getQuestionID(chbQ2.getSelectionModel().getSelectedItem(), "PwdQuestions2", "Question_2", "QuestionID_2", conn);
            pstmt.setInt(6, questionID1);
            pstmt.setInt(7,questionID2);
            String hashedAnswer1 = BCrypt.hashpw(txtA1.getText(), BCrypt.gensalt());
            String hashedAnswer2 = BCrypt.hashpw(txtA2.getText(), BCrypt.gensalt());
            pstmt.setString(8,hashedAnswer1);
            pstmt.setString(9,hashedAnswer2);
            pstmt.setInt(10,userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method is used to cancel the user profile.
     */
    public void cancelOnAction(){
        Stage stage = (Stage) txtUserName.getScene().getWindow();
        stage.close();  // Closes the current window
    }

    /**
     * This method is used to save the user profile.
     */
    public void saveOnAction(){
        this.saveUserProfile();
        this.displayUserProfile();
        Stage stage = (Stage) txtUserName.getScene().getWindow();
        stage.close();  // Closes the current window
    }

    /**
     * This method is used to verify the answers for the user profile.
     */
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

    /**
     * This method is used to verify the answers for the user profile.
     */
    private boolean validateInputs() {
        if (txtFirstName.getText().isEmpty() || txtLastName.getText().isEmpty() || txtUserName.getText().isEmpty() || txtPassword.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || chbQ1.getSelectionModel().isEmpty() || chbQ2.getSelectionModel().isEmpty() ||
                txtA1.getText().trim().isEmpty() || txtA2.getText().trim().isEmpty()) {
            lblMsg.setText("Please fill all the information above.");
            return false;
        }
        // Check if email is valid
        if (!txtEmail.getText().contains("@")) {
            lblMsg.setText("Invalid email format.");
            return false;
        }
        // Check if password is at least 8 characters long
        if (usernameExists(txtUserName.getText())) {
            lblMsg.setText("Username already exists. Please choose a different one.");
            return false;
        }
        // Check if email already exists
        if (emailExists(txtEmail.getText())) {
            lblMsg.setText("Email address already exists. Please use a different one.");
            return false;
        }

        return true;
    }

    /**
     * This method is used to verify the answers for the user profile.
     */
    public void verifyAnswers() {
        String email = txtEmail.getText(); // Get email
        String answer1 = txtA1.getText(); // Get answer 1
        String answer2 = txtA2.getText(); // Get answer 2
        // Check if email, answer 1, and answer 2 are empty
        if (email.isEmpty() || answer1.isEmpty() || answer2.isEmpty()) {
            lblMsg.setText("Please fill in all fields for verification.");
            return;
        }
        // Verify answers
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
                    lblMsg.setText("Your answers are correct. You can now reset your password.");
                    btnSave.setDisable(false); // Enable reset button if answers are correct
                } else {
                    lblMsg.setText("Incorrect answers. Please try again.");
                }
            } else {
                lblMsg.setText("No account associated with this email.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblMsg.setText("Error verifying answers: " + e.getMessage());
        }
    }

    /**
     * This method is used to check if the username exists.
     * @param username - the username
     * @return true if the username exists, false otherwise
     */
    private boolean usernameExists(String username) {
        return exists("userName", username);
    }

    /**
     * This method is used to check if the email exists.
     * @param email - the email
     * @return true if the email exists, false otherwise
     */
    private boolean emailExists(String email) {
        return exists("emailAddress", email);
    }

    /**
     * This method is used to check if the column value exists.
     * @param columnName - the column name
     * @param value - the value
     * @return true if the column value exists, false otherwise
     */
    private boolean exists(String columnName, String value) {
        String query = "SELECT COUNT(*) FROM useraccount WHERE " + columnName + " = ? and userId != " + userId; // Check if the value exists in the database
        // Check if the value exists in the database
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
     * This method is used to load the questions.
     */
    public void loadQuestions(){
        loadQuestionsToChoiceBox(chbQ1, "PwdQuestions1", "Question_1");
        loadQuestionsToChoiceBox(chbQ2, "PwdQuestions2", "Question_2");
    }

    /**
     * This method is used to load the questions to the choice box.
     * @param choiceBox - the choice box
     * @param tableName - the table name
     * @param questionColumn - the question column
     */
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

    /**
     * This method is used to apply the colors.
     * @param backgroundColor - the background color
     * @param textColor - the text color
     * @param buttonColor - the button color
     */
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

        if (paneProfile != null) {
            paneProfile.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
        if (btnCancel != null) {
            btnCancel.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnSave != null) {
            btnSave.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnVerify != null) {
            btnVerify.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (radbAdm != null) {
            radbAdm.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (radbGen != null) {
            radbGen.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (radbDev != null) {
            radbDev.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblMsg != null) {
            lblMsg.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblUserPro != null) {
            lblUserPro.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblUser != null) {
            lblUser.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblFirst != null) {
            lblFirst.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblLast != null) {
            lblLast.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblEmail != null) {
            lblEmail.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblPwd != null) {
            lblPwd.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblAccType != null) {
            lblAccType.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblSQ1 != null) {
            lblSQ1.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblSQ2 != null) {
            lblSQ2.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblAn1 != null) {
            lblAn1.setStyle(" -fx-text-fill: " + textHex + ";");
        }
        if (lblAn2 != null) {
            lblAn2.setStyle(" -fx-text-fill: " + textHex + ";");
        }
    }

    /**
     * This method is used to get the hex color.
     * @param color - the color
     * @return the hex color
     */
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * This method is used to apply the mode colors.
     */
    public void applyModeColors() {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode();
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others

        updateLabelBackgroundColor(opacity);
    }

    /**
     * This method is used to update the label background color.
     * @param opacity - the opacity
     */
    public void updateLabelBackgroundColor(double opacity) {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity);
        lblBkGrd.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";");
    }

    /**
     * This method is used to convert the color to RGBA.
     * @param color - the color
     * @return the RGBA color
     */
    private String toRgbaColor(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }
}

