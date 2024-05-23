package com.cab302.wellbeing.model;

import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AppSettings {
    private static DataBaseConnection dbConnection = new DataBaseConnection();
    public static final String MODE_LIGHT = "Light";
    public static final String MODE_NIGHT = "Night";
    public static final String MODE_AUTO = "Auto";
    public static final String MODE_EYEPROTECT = "EyeProtect";

    private static String currentMode = MODE_AUTO; // Ensure a default value
    private static Color lightColor = Color.web("#bfe7f7");
    private static Color nightColor = Color.web("#000000");
    private static Color autoColor = Color.web("#009ee0");
    private static Color eyeProtectColor = Color.web("#A3CCBE");

    public static String getCurrentMode() {
        return currentMode != null ? currentMode : MODE_AUTO; // Ensure a non-null value
    }

    public static void setCurrentMode(String mode) {
        currentMode = mode != null ? mode : MODE_AUTO; // Ensure a non-null value
    }

    public static Color getCurrentModeColor() {
        return getCurrentModeColorForMode(getCurrentMode());
    }

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

    public static Color getCurrentModeColorWithOpacity(double opacity) {
        Color baseColor = getCurrentModeColor();
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), opacity);
    }

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

    public static void saveModeToDatabase(int userId, String mode) {
        saveUserMode(userId, mode);
    }

    public static void loadModeFromDatabase(int userId) {
        String mode = getUserMode(userId);
        if (mode != null) {
            setCurrentMode(mode);
        } else {
            setCurrentMode(MODE_AUTO);
        }
    }
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