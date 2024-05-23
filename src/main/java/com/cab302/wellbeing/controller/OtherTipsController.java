package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is a controller for the Other Tips functionality in the application.
 * It provides some other wellbeing tips and tricks (text or links...) to the user.
 */
public class OtherTipsController {
    @FXML
    private Button btnGoBackToWell;
    public Label lblBkGrd;
    @FXML
    private Hyperlink link1, link2, link3, link4, link5;
    private int userId;
    private String firstName;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void btnGoBackToWellAction(ActionEvent e) {
        Stage stage = (Stage) btnGoBackToWell.getScene().getWindow();
        // Close the current stage
        stage.close();
    }

    public void setUserId(int userId) {
        this.userId = userId;
        if (this.firstName == null) {
            this.firstName = fetchFirstNameFromDatabase(userId);
        }
        System.out.println("userId: " + userId);
    }

    private String fetchFirstNameFromDatabase(int userId) {
        String query = "SELECT firstName FROM useraccount WHERE userId = ?";
        try (Connection conn = new DataBaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("firstName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "User"; // Default value if the user is not found
    }

    @FXML
    private void openLink(ActionEvent event) {
        Hyperlink clickedLink = (Hyperlink) event.getSource();
        String url = clickedLink.getText(); // Get the URL from the hyperlink's text

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/InternetExplorer.fxml"));
            Parent root = loader.load();

            InternetExplorerController controller = loader.getController();
            controller.loadUrl(url); // Pass the URL to the Internet Explorer Controller
            controller.setUserId(userId);
            controller.setFirstName(firstName);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            System.out.println("user id: " + userId);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
