package com.cab302.wellbeing.controller;

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
    @FXML
    public TextField txtUserName, txtFirstName, txtLastName, txtEmail, txtPassword,txtA1, txtA2;
    @FXML
    private ToggleGroup accTypeGroup;
    @FXML
    public ChoiceBox<String> chbQ1, chbQ2;
    @FXML
    public Button btnCancel, btnSave, btnVerify;
    @FXML
    public RadioButton radbAdm, radbGen, radbDev;
    @FXML
    public Label lblBkGrd, lblMsg, lblUserPro, lblUser, lblFirst, lblLast, lblEmail, lblPwd, lblAccType, lblSQ1, lblSQ2, lblAn1, lblAn2;
    @FXML
    private Pane paneProfile;
    private static Color lightColor = Color.web("#bfe7f7");
    private static Color nightColor = Color.web("#777777");
    private static Color autoColor = Color.web("#009ee0");
    private static Color eyeProtectColor = Color.web("#A3CCBE");
    int userId;
    private String accType;
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
    private String fetchAccTypeFromDatabase(int userId) {
        String query = "SELECT accType FROM useraccount WHERE userId = ?";
        try (Connection conn = new DataBaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("accType");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Admin"; // Default value if the user is not found
    }
    private DataBaseConnection dbConnection = new DataBaseConnection();

    public void setUserId(int userId) {
        this.userId = userId;  // Now you can use this userId to store browsing data linked to the user
    }
    @FXML
    private void initialize() {
        // Disable the reset button initially until answers are verified
        btnSave.setDisable(true);

        lblMsg.setText("");
    }

    public void displayUserName(String userName){
        this.txtUserName.setText(userName);
    }

    public void displayFirstName(String firstName){
        this.txtFirstName.setText(firstName);
    }

    public void displayLastName(String lastName){
        this.txtLastName.setText(lastName);
    }

    public void displayEmail(String email){
        this.txtEmail.setText(email);
    }


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

    public void displayUserProfile(){
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
    public void cancelOnAction(){
        Stage stage = (Stage) txtUserName.getScene().getWindow();
        stage.close();  // Closes the current window
    }

    public void saveOnAction(){
        this.saveUserProfile();
        this.displayUserProfile();
        Stage stage = (Stage) txtUserName.getScene().getWindow();
        stage.close();  // Closes the current window
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

    private boolean validateInputs() {
        if (txtFirstName.getText().isEmpty() || txtLastName.getText().isEmpty() || txtUserName.getText().isEmpty() || txtPassword.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || chbQ1.getSelectionModel().isEmpty() || chbQ2.getSelectionModel().isEmpty() ||
                txtA1.getText().trim().isEmpty() || txtA2.getText().trim().isEmpty()) {
            lblMsg.setText("Please fill all the information above.");
            return false;
        }

        if (!txtEmail.getText().contains("@")) {
            lblMsg.setText("Invalid email format.");
            return false;
        }

        if (usernameExists(txtUserName.getText())) {
            lblMsg.setText("Username already exists. Please choose a different one.");
            return false;
        }

        if (emailExists(txtEmail.getText())) {
            lblMsg.setText("Email address already exists. Please use a different one.");
            return false;
        }

        return true;
    }
    public void verifyAnswers() {
        String email = txtEmail.getText();
        String answer1 = txtA1.getText();
        String answer2 = txtA2.getText();

        if (email.isEmpty() || answer1.isEmpty() || answer2.isEmpty()) {
            lblMsg.setText("Please fill in all fields for verification.");
            return;
        }

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
    private boolean usernameExists(String username) {
        return exists("userName", username);
    }

    private boolean emailExists(String email) {
        return exists("emailAddress", email);
    }

    private boolean exists(String columnName, String value) {
        String query = "SELECT COUNT(*) FROM useraccount WHERE " + columnName + " = ? and userId != " + userId;
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

    public void loadQuestions(){
        loadQuestionsToChoiceBox(chbQ1, "PwdQuestions1", "Question_1");
        loadQuestionsToChoiceBox(chbQ2, "PwdQuestions2", "Question_2");
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
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    public void applyModeColors() {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode();
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others

        updateLabelBackgroundColor(opacity);
    }

    public void updateLabelBackgroundColor(double opacity) {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity);
        lblBkGrd.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";");
    }

    private String toRgbaColor(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }
}

