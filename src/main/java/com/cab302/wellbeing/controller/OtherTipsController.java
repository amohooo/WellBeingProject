package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.DataBaseConnection;
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
    private Button btnGoBackToWell; // Button to go back to the WellBeing Tips
    /**
     * This method is used to initialize the Other Tips Controller.
     */
    public Label lblBkGrd; // Label for the background
    @FXML
    private Hyperlink link1, link2, link3, link4, link5; // Hyperlinks for the tips
    private int userId; // ID of the current user
    private String firstName; // First name of the user

    /**
     * This method is used to parse the user's first name.
     * @param firstName - the first name that parsed from previous scene
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName; // Parse the first name
    }

    /**
     * This method is used to handle the go back button click event.
     * It closes the current stage.
     * @param e - the action event that triggered the method
     */
    public void btnGoBackToWellAction(ActionEvent e) {
        Stage stage = (Stage) btnGoBackToWell.getScene().getWindow();
        // Close the current stage
        stage.close();
    }

    /**
     * This method is used to parse the user's ID.
     * @param userId - the user ID that parsed from previous scene
     */
    public void setUserId(int userId) {
        this.userId = userId; // Parse the user ID
        // Fetch the first name from the database
        if (this.firstName == null) {
            this.firstName = fetchFirstNameFromDatabase(userId);
        }
        System.out.println("userId: " + userId);
    }

    /**
     * This method is used to fetch the first name of the user from the database.
     * @param userId - the user ID to fetch the first name
     * @return the first name of the user
     */
    private String fetchFirstNameFromDatabase(int userId) {
        String query = "SELECT firstName FROM useraccount WHERE userId = ?"; // Query to fetch the first name
        // Try to fetch the first name from the database
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

    /**
     * This method is used to open the link in the Internet Explorer.
     * @param event - the action event that triggered the method
     */
    @FXML
    private void openLink(ActionEvent event) {
        Hyperlink clickedLink = (Hyperlink) event.getSource(); // Get the clicked hyperlink
        String url = clickedLink.getText(); // Get the URL from the hyperlink's text
        // Open the Internet Explorer with the URL
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

    /**
     * This method is used to apply the color theme based on the current mode.
     */
    public void applyModeColors() {
        // Get the current mode
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        String currentMode = AppSettings.getCurrentMode(); // Get the current mode
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others
        updateLabelBackgroundColor(opacity); // Update the label background color
    }

    /**
     * This method is used to update the background color of the label.
     * @param opacity - the opacity of the background color
     */
    public void updateLabelBackgroundColor(double opacity) {
        // Update the background color of the label
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity); // Get the background color
        lblBkGrd.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";"); // Set the background color
    }

    /**
     * This method is used to convert a Color object to an RGBA color string.
     * @param color - the Color object to convert
     * @return the RGBA color string
     */
    private String toRgbaColor(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }
}
