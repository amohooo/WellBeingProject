package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.DataBaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PasswordResetController {
    @FXML
    private TextField txtEmailAdd;
    @FXML
    private PasswordField ptxtPwd, ptxtRePwd;
    @FXML
    private Label lblMsg;
    @FXML
    private Button btnReset, btnCncl;

    public void resetPassword() {
        String email = txtEmailAdd.getText();
        String newPassword = ptxtPwd.getText();

        if (email.isEmpty() || newPassword.isEmpty() || ptxtRePwd.getText().isEmpty()) {
            lblMsg.setText("Please fill in all fields.");
            return;
        }

        if (!newPassword.equals(ptxtRePwd.getText())) {
            lblMsg.setText("Passwords do not match.");
            return;
        }

        try {
            DataBaseConnection connectNow = new DataBaseConnection();
            Connection connectDB = connectNow.getConnection();
            if (!emailExists(email, connectDB)) {
                lblMsg.setText("The email does not exist. Please try again.");
                return;
            }

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            String query = "UPDATE useraccount SET passwordHash = ? WHERE emailAddress = ?";
            PreparedStatement pst = connectDB.prepareStatement(query);
            pst.setString(1, hashedPassword);
            pst.setString(2, email);
            int result = pst.executeUpdate();

            if (result > 0) {
                lblMsg.setText("Password successfully reset. Check your email for the confirmation link.");
                closeWindow();
            } else {
                lblMsg.setText("Failed to reset password.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblMsg.setText("Error: " + e.getMessage());
        }
    }
    private void closeWindow() {
        // You can use any FXML component here to get the scene and window. Using lblMsg as an example.
        Stage stage = (Stage) lblMsg.getScene().getWindow();
        stage.close();  // Closes the current window
    }

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

    public void btnExitOnAction(ActionEvent e) {
        closeWindow();
    }
}
