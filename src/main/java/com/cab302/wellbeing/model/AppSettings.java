package com.cab302.wellbeing.model;

import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is used to store and manage the application settings.
 */
public class AppSettings {
    private static DataBaseConnection dbConnection = new DataBaseConnection(); // Database connection
    /**
     * The different modes for the application
     */
    public static final String MODE_LIGHT = "Light"; // Light mode
    /**
     * The different modes for the application
     */
    public static final String MODE_NIGHT = "Night"; // Night mode
    /**
     * The different modes for the application
     */
    public static final String MODE_AUTO = "Auto"; // Auto mode
    /**
     * The different modes for the application
     */
    public static final String MODE_EYEPROTECT = "EyeProtect"; // Eye protect mode

    /**
     * The current mode
     */
    private static String currentMode = MODE_AUTO; // Ensure a default value
    /**
     * The light color
     */
    private static Color lightColor = Color.web("#bfe7f7"); // Default light color
    /**
     * The night color
     */
    private static Color nightColor = Color.web("#000000"); // Default night color
    /**
     * The auto color
     */
    private static Color autoColor = Color.web("#009ee0"); // Default auto color
    /**
     * The eye protect color
     */
    private static Color eyeProtectColor = Color.web("#A3CCBE"); // Default eye protect color

    /**
     * This method is used to get the current mode.
     * @return the current mode
     */
    public static String getCurrentMode() {
        return currentMode != null ? currentMode : MODE_AUTO; // Ensure a non-null value
    }

    /**
     * This method is used to set the current mode.
     * @param mode - the mode to set
     */
    public static void setCurrentMode(String mode) {
        currentMode = mode != null ? mode : MODE_AUTO; // Ensure a non-null value
    }

    /**
     * This method is used to get the current mode color.
     * @return the current mode color
     */
    public static Color getCurrentModeColor() {
        return getCurrentModeColorForMode(getCurrentMode());
    }

    /**
     * This method is used to get the current mode color for a specific mode.
     * @param mode - the mode to get the color for
     * @return the color for the mode
     */
    public static Color getCurrentModeColorForMode(String mode) {
        switch (mode) {
            case MODE_LIGHT:
                return lightColor;
            case MODE_NIGHT:
                return nightColor;
            case MODE_AUTO:
                return autoColor;
            case MODE_EYEPROTECT:
                return eyeProtectColor;
            default:
                return autoColor;
        }
    }

    /**
     * This method is used to get the current mode color with a specific opacity.
     * @param opacity - the opacity to apply
     * @return the color with the opacity
     */
    public static Color getCurrentModeColorWithOpacity(double opacity) {
        Color baseColor = getCurrentModeColor();
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), opacity);
    }

    /**
     * This method is used to set the current mode color.
     * @param mode - the mode to set the color for
     * @param color - the color to set
     */
    public static void setCurrentModeColor(String mode, Color color) {
        switch (mode) {
            case MODE_LIGHT:
                lightColor = color;
                break;
            case MODE_NIGHT:
                nightColor = color;
                break;
            case MODE_AUTO:
                autoColor = color;
                break;
            case MODE_EYEPROTECT:
                eyeProtectColor = color;
                break;
            default:
                autoColor = color;
                break;
        }
        currentMode = mode != null ? mode : MODE_AUTO; // Ensure a non-null value
    }

    /**
     * This method is used to save the mode to the database.
     * @param userId - the user ID
     * @param mode - the mode to save
     */
    public static void saveModeToDatabase(int userId, String mode) {
        saveUserMode(userId, mode);
    }

    /**
     * This method is used to load the mode from the database.
     * @param userId - the user ID
     */
    public static void loadModeFromDatabase(int userId) {
        String mode = getUserMode(userId);
        if (mode != null) {
            setCurrentMode(mode);
        } else {
            setCurrentMode(MODE_AUTO);
        }
    }

    /**
     * This method is used to save the user mode to the database.
     * @param userId - the user ID
     * @param mode - the mode to save
     */
    public static void saveUserMode(int userId, String mode) {

        Color color = AppSettings.getCurrentModeColorForMode(mode); // Get the color for the current mode

        String query = "INSERT INTO Mode (userId, mode, Red, Green, Blue, Opacity) VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE mode = VALUES(mode), Red = VALUES(Red), Green = VALUES(Green), Blue = VALUES(Blue), Opacity = VALUES(Opacity)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, mode);
            pstmt.setInt(3, (int) (color.getRed() * 255));
            pstmt.setInt(4, (int) (color.getGreen() * 255));
            pstmt.setInt(5, (int) (color.getBlue() * 255));
            double opacity = color.getOpacity();
            System.out.println("Setting opacity: " + opacity); // Debug print
            pstmt.setDouble(6, opacity);
            pstmt.executeUpdate();
            System.out.println("User mode and color inserted successfully.");
        } catch (SQLException e) {
            System.err.println("Error inserting user mode and color: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method is used to get the user mode from the database.
     * @param userId - the user ID
     * @return the user mode
     */
    public static String getUserMode(int userId) {
        String query = "SELECT Mode, Red, Green, Blue, Opacity FROM Mode WHERE UserID = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                String mode = resultSet.getString("Mode");
                int red = resultSet.getInt("Red");
                int green = resultSet.getInt("Green");
                int blue = resultSet.getInt("Blue");
                double opacity = resultSet.getDouble("Opacity");
                System.out.println("Retrieved opacity: " + opacity); // Debug print

                // Set the current mode color based on retrieved values
                Color retrievedColor = Color.rgb(red, green, blue, opacity);
                AppSettings.setCurrentModeColor(mode, retrievedColor);

                return mode;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user mode and color: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}