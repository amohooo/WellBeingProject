package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.DataBaseConnection;
import com.cab302.wellbeing.model.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
/**
 * This class is responsible for controlling the browsing history.
 * It provides functionalities such as loading and clearing the browsing history,
 * and applying color themes.
 */
public class BrowsingHistoryController {
    /**
     * This method is used to set the DatePicker.
     */
    @FXML
    public DatePicker startDatePicker, endDatePicker;  // Date pickers for start and end dates
    /**
     * This method is used to set the TextArea.
     */
    @FXML
    public TextArea historyDisplayArea;  // Text area to display the browsing history
    /**
     * This method is used to set the Button.
     */
    @FXML
    public Button btnSearch, btnClear;  // Buttons for searching and clearing the history
    /**
     * This method is used to set the Pane.
     */
    @FXML
    public Pane paneHistory;  // Pane for the history
    /**
     * This method is used to set the TextField.
     */
    @FXML
    public TextField txtUrl;  // Text field for the URL
    /**
     * This method is used to set the Label.
     */
    @FXML
    public Label lblGreeting, lblStart, lblEnd, lblWeb, lblBkGrd;  // Labels for the UI
    private String firstName;  // First name of the user
    private int currentUserId;  // ID of the current user

    /**
     * Sets the first name of the user.
     *
     * @param firstName The first name of the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the ID of the user.
     *
     * @param userId The ID of the user
     */
    public void setUserId(int userId) {
        this.currentUserId = userId;
        System.out.println("currentUserId: " + currentUserId);
        System.out.println("firstName: " + firstName);
    }

    /**
     * Initializes the controller.
     * Sets up the event handler for the Enter key in the txtUrl TextField.
     */
    public void initialize() {
        // Set up the event handler for the Enter key in the txtUrl TextField
        txtUrl.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loadHistory();  // Call your method to load or search the history
            }
        });
    }

    /**
     * Loads the browsing history.
     * Retrieves the browsing history from the database and displays it in the text area.
     */
    public void loadHistory() {
        historyDisplayArea.clear(); // Clear the text area

        LocalDate startDate = startDatePicker.getValue(); // Get the start date
        LocalDate endDate = endDatePicker.getValue(); // Get the end date
        String searchURL = txtUrl.getText(); // Get the search URL
        int currentUserId = UserSession.getInstance().getCurrentUserId();  // Retrieve current user ID
        String firstName = UserSession.getInstance().getFirstName();  // Retrieve first name

        // Query to retrieve the browsing history
        String query = "SELECT u.firstName, b.URL, b.StartTime, b.SessionDate, b.Duration " +
                "FROM BrowsingData b " +
                "JOIN useraccount u ON b.UserID = u.userId " +
                "WHERE b.UserID = ? ";
        // Add conditions to the query based on the search criteria
        if (searchURL != null && !searchURL.isEmpty()) {
            query += "AND b.URL LIKE ? ";
        }
        // Add conditions to the query based on the start and end dates
        if (startDate != null && endDate != null) {
            query += "AND b.SessionDate BETWEEN ? AND ? ";
        } else if (searchURL == null || searchURL.isEmpty()) {
            query += "AND b.SessionDate = CURRENT_DATE() ";
        }
        // Order the results by session date and start time in descending order
        query += "ORDER BY b.SessionDate DESC, b.StartTime DESC;";
        // Execute the query and retrieve the browsing history
        try (Connection conn = new DataBaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            int paramIndex = 1;
            stmt.setInt(paramIndex++, currentUserId); // Set the user ID

            if (searchURL != null && !searchURL.isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchURL + "%");
            }
            if (startDate != null && endDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
            }

            ResultSet rs = stmt.executeQuery();
            //String firstName = rs.getString("firstName");
            if (rs.next()) {

                lblGreeting.setText(firstName + ", here is your browsing history:");
                rs.beforeFirst(); // Now this will work as rs is scrollable
                //String firstName = rs.getString("firstName");

                //return;
            } else {
                lblGreeting.setText("Sorry, " + firstName + ", No browsing history found");
                historyDisplayArea.setText("Failed to load browsing history: No results found.");
                return;
            }

            while (rs.next()) { // Iterate through the results and display the browsing history

                String url = rs.getString("URL");
                String startTime = rs.getString("StartTime").toString();
                String sessionDate = rs.getString("SessionDate").toString();
                int duration = rs.getInt("Duration");

                String displayText = String.format("URL: %s, Start: %s, Date: %s, Duration: %d seconds\n",
                        url, startTime, sessionDate, duration);
                historyDisplayArea.appendText(displayText);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            historyDisplayArea.setText("Failed to load browsing history: " + e.getMessage());
        }

    }

    /**
     * Clears the browsing history display.
     * Clears the text area and resets the greeting.
     */
    @FXML
    public void clearHistoryDisplay() {
        historyDisplayArea.clear();  // Clears the text area
        lblGreeting.setText("Welcome, want to see your browsing history?"); // Reset greeting
    }

    /**
     * Applies color themes to the UI.
     *
     * @param backgroundColor The background color
     * @param textColor The text color
     * @param buttonColor The button color
     */
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

        if (lblGreeting != null) {
            lblGreeting.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblStart != null) {
            lblStart.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblEnd != null) {
            lblEnd.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblWeb != null) {
            lblWeb.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (btnSearch != null) {
            btnSearch.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnClear != null) {
            btnClear.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (paneHistory != null) {
            paneHistory.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
    }

    /**
     * Converts a Color object to a hex color string.
     *
     * @param color The Color object
     * @return The hex color string
     */
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * Applies the color theme based on the current mode.
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
     * Updates the background color of the label.
     *
     * @param opacity The opacity of the color
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
     * Converts a Color object to an RGBA color string.
     *
     * @param color The Color object
     * @return The RGBA color string
     */
    private String toRgbaColor(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }
}
