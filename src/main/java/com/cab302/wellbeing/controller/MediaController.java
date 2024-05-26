package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * This class is responsible for controlling the media player.
 * It provides functionalities such as playing, pausing, and stopping media files,
 * uploading and deleting media files, and applying color themes.
 */
public class MediaController implements Initializable {
    private int userId; // ID of the current user
    private static final Color DEFAULT_COLOR = Color.web("#009ee0"); // Default color for the media player
    private static final Color DEFAULT_TEXT_COLOR = Color.web("#ffffff"); // Default text color for the media player
    private String accType; // Account type of the user

    /**
     * Sets the account type of the user.
     *
     * @param accType The account type of the user
     */
    public void setUserType(String accType) {
        this.accType = accType; // Set the account type
        System.out.println("User type: " + accType);
        // Disable the upload and delete buttons for general users
        if (accType.equals("General")) {
            btnUpload.setDisable(true); // Disable the upload button
            btnDelete.setDisable(true); // Disable the delete button
        } else {
            btnUpload.setDisable(false); // Enable the upload button
            btnDelete.setDisable(false); // Enable the delete button
        }
    }

    /**
     * Sets the ID of the user.
     *
     * @param userId The ID of the user
     */
    public void setUserId(int userId) {
        this.userId = userId;  // Now you can use this userId to store browsing data linked to the user
    }
    @FXML private MediaView mediaView; // Media view for the media player
    @FXML private Button btnPlay, btnPause, btnStop, btnUpload, btnDelete, btnRFS, btnSelect; // Buttons for the media player
    @FXML private Label lblBkGrd, lblDuration, lblMedia; // Labels for the media player
    @FXML private Slider slider; // Slider for the media player
    @FXML
    private Pane paneMedia; // Pane for the media player
    /**
     * Choice box for the media file selection.
     */
    @FXML
    public ChoiceBox<String> chbMedia; // Choice box for the media player
    private Media media; // Media for the media player
    /**
     * Media player for the media player.
     */
    public MediaPlayer mediaPlayer; // Media player for the media player
    private boolean isPlayed = false; // Boolean to check if the media is played

    private DataBaseConnection dbConnection; // Database connection for the media player

    /**
     * Sets the database connection for the media player.
     *
     * @param dbConnection The database connection for the media player
     */
    public void setDbConnection(DataBaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Initializes the media player.
     * Sets up the media player with the default media file.
     *
     * @param filePath The filepath of the media player
     */
    private void setupMediaPlayer(String filePath) {
        // Set the default file path if no file path is provided
        if (filePath == null || filePath.isEmpty()) {
            filePath = "src/main/java/com/cab302/wellbeing/Media/MindRefresh.mp3";  // Default file path
        }
        File mediaFile = new File(filePath); // Create a new file with the provided file path
        // Check if the file exists
        if (!mediaFile.exists()) {
            System.err.println("File does not exist: " + filePath);
            return;
        }
        String uriString = mediaFile.toURI().toString(); // Convert the file to a URI string
        System.out.println("URI for media: " + uriString);
        // Stop the media player if it is already playing
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        media = new Media(uriString);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        bindMediaPlayer(); // Bind the media player
    }

    /**
     * Selects a media file.
     * Opens a file chooser dialog to select a media file.
     */
    @FXML
    private void selectMedia() {
        FileChooser fileChooser = new FileChooser(); // Create a new file chooser
        fileChooser.setTitle("Select Media"); // Set the title of the file chooser
        File file = fileChooser.showOpenDialog(null); // Show the file chooser dialog
        // Check if a file is selected
        if (file != null) {
            setupMediaPlayer(file.getAbsolutePath());
        } else {
            setupMediaPlayer("src/main/java/com/cab302/wellbeing/Media/MindRefresh.mp3");
        }
    }

    /**
     * Plays the media.
     * Plays the media file.
     */
    private void bindMediaPlayer() {
        // Bind the slider to the media player
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            slider.setValue(newValue.toSeconds()); // Set the value of the slider
            lblDuration.setText(formatDuration(newValue) + " / " + formatDuration(media.getDuration())); // Set the duration label
        });
        // Set the maximum value of the slider
        mediaPlayer.setOnReady(() -> {
            Duration duration = media.getDuration(); // Get the duration of the media
            slider.setMax(duration.toSeconds()); // Set the maximum value of the slider
            lblDuration.setText("Duration: 00:00:00 / " + formatDuration(duration)); // Set the duration label
        });
        mediaPlayer.setAutoPlay(false); // Set auto play to false
    }

    /**
     * Pauses the media.
     * Pauses the media file.
     */
    private String formatDuration(Duration duration) {
        int hours = (int) duration.toHours(); // Get the hours
        int minutes = (int) duration.toMinutes() % 60; // Get the minutes
        int seconds = (int) duration.toSeconds() % 60; // Get the seconds
        return String.format("%02d:%02d:%02d", hours, minutes, seconds); // Return the formatted duration
    }

    /**
     * Pauses the media.
     * Pauses the media file.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DataBaseConnection(); // Create a new database connection
        setUserId(userId); // Set the user ID
        loadMediaFilesToChoiceBox(); // this method sets up the media files in the ChoiceBox
        setupMediaPlayer("src/main/java/com/cab302/wellbeing/Media/MindRefresh.mp3"); // Set up the media player with the default media file
        // Set the media player to play the media file
        Platform.runLater(() -> {
            // Set the media player to play the media file
            if (mediaView.getScene() != null && mediaView.getScene().getWindow() != null) {
                Stage stage = (Stage) mediaView.getScene().getWindow(); // Get the stage
                // Set the close request for the stage
                stage.setOnCloseRequest(event -> {
                    // Stop the media player if the stage is closed
                    if (mediaPlayer != null) {
                        mediaPlayer.stop(); // Stop the media player
                        mediaPlayer.dispose(); // Dispose the media player
                    }
                });
            }
        });
        // Set the media player to play the media file
        chbMedia.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            // Load the media file when a new file is selected
            if (newValue != null) {
                loadMedia(newValue); // Load the media file
            }
        });
    }

    /**
     * Plays the media.
     * Plays the media file.
     * @param fileName The file name of the media
     */
    public void loadMedia(String fileName) {
        // Check if the user ID is provided
        if (userId == 0) {
            System.out.println("No user ID provided");
            return;
        }
        String query = "SELECT FilePath FROM MediaFiles WHERE FileName = ? AND UserID = ? AND IsDeleted = FALSE AND IsPublic = TRUE"; // Query to select the file path
        // Retrieve the file path from the database
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, fileName);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String filePath = rs.getString("FilePath");
                setupMediaPlayer(filePath);
            } else {
                System.err.println("File path not found for selected media: " + fileName);
            }
        } catch (SQLException e) {
            System.err.println("Error loading selected media file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Plays the media.
     * Plays the media file.
     */
    public void playMedia() {
        mediaPlayer.play();
    }

    /**
     * Pauses the media.
     * Pauses the media file.
     */
    public void pauseMedia() {
        mediaPlayer.pause();
    }

    /**
     * Stops the media.
     * Stops the media file.
     */
    public void refreshMediaList() {
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5)); // Introduce a delay before closing the window for the test purpose
        delay.setOnFinished(event -> loadMediaFilesToChoiceBox());
        delay.play();
        //loadMediaFilesToChoiceBox();
    }

    /**
     * Stops the media.
     * Stops the media file.
     */
    public void stopMedia() {
        mediaPlayer.stop();
    }

    /**
     * Plays the media.
     * Plays the media file.
     */
    @FXML
    private void sliderPressed() {
        mediaPlayer.seek(Duration.seconds(slider.getValue()));
    }

    /**
     * Plays the media.
     * Plays the media file.
     */
    @FXML
    private void uploadMedia() {
        FileChooser fileChooser = new FileChooser(); // Create a new file chooser
        fileChooser.setTitle("Upload Media"); // Set the title of the file chooser
        File file = fileChooser.showOpenDialog(null); // Show the file chooser dialog
        // Check if a file is selected
        if (file != null && !checkFileExists(file.getName())) {
            String path = saveFileToServer(file);
        } else {
            System.out.println("File already exists or selection was cancelled.");
        }
    }

    /**
     * Deletes the media.
     * Deletes the media file.
     */
    @FXML
    private void deleteMedia() {
        String fileName = chbMedia.getValue();  // Assuming chbMedia is a ComboBox with file names
        if (fileName != null) {
            // Retrieve the file path from the database before deletion
            String querySelect = "SELECT FilePath FROM MediaFiles WHERE FileName = ? AND UserID = ?";
            String queryDelete = "DELETE FROM MediaFiles WHERE FileName = ? AND UserID = ?";

            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmtSelect = conn.prepareStatement(querySelect);
                 PreparedStatement pstmtDelete = conn.prepareStatement(queryDelete)) {

                // Retrieve the file path
                pstmtSelect.setString(1, fileName);
                pstmtSelect.setInt(2, userId);
                ResultSet rs = pstmtSelect.executeQuery();
                String filePath = null;
                if (rs.next()) {
                    filePath = rs.getString("FilePath");
                }
                if (filePath == null) {
                    System.err.println("File not found in database: " + fileName);
                    return;
                }
                // Delete from database
                pstmtDelete.setString(1, fileName);
                pstmtDelete.setInt(2, userId);

                refreshMediaList();

            } catch (SQLException e) {
                System.err.println("Error deleting media file from database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Deletes the media.
     * Deletes the media file.
     */
    private boolean checkFileExists(String fileName) {
        String query = "SELECT COUNT(*) FROM MediaFiles WHERE FileName = ? AND UserID = ? AND IsDeleted = FALSE"; // Query to check if the file exists
        // Check if the file exists
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, fileName);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // File exists
            }
        } catch (SQLException e) {
            System.err.println("Error checking file existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // File does not exist
    }

    /**
     * Saves the file to the server.
     * Saves the file to the server storage.
     *
     * @param file The file to save
     * @return The file path of the saved file
     */
    private String saveFileToServer(File file) {
        String directoryPath = "src/main/resources/com/cab302/wellbeing/Media/";  // Set your server storage path
        Path directory = Paths.get(directoryPath); // Create a new path with the directory path
        // Save the file to the server
        try {
            // Ensure the directory exists
            Files.createDirectories(directory);
            // Define the target path for the file
            Path targetPath = directory.resolve(file.getName());
            // Copy the file to the target directory, only if it does not exist
            if (!Files.exists(targetPath)) {
                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                String filePath = targetPath.toString();
                saveFileMetadata(file, filePath);
                return filePath;
            } else {
                System.err.println("File already exists on server: " + file.getName());
                return null;
            }
        } catch (IOException e) {
            System.err.println("Failed to save the file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads the media files to the choice box.
     * Loads the media files to the choice box.
     */
    public void loadMediaFilesToChoiceBox() {
        // Check if the user ID is valid
        if (userId <= 0) {
            return;
        }
        String query = "SELECT FileName FROM MediaFiles WHERE UserID = ? AND IsDeleted = FALSE AND IsPublic = TRUE"; // Query to select the file names
        // Retrieve the file names from the database
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            ObservableList<String> mediaFiles = FXCollections.observableArrayList();
            boolean foundData = false;
            while (rs.next()) {
                foundData = true;
                String fileName = rs.getString("FileName");
                mediaFiles.add(fileName);
                System.out.println("Loaded file: " + fileName);  // Log loaded file names
            }
            if (!foundData) {
                System.out.println("No files found for user with ID " + userId);
            }
            chbMedia.setItems(mediaFiles); // Set the media files to the choice box
            // Select the first media file if available
            if (!mediaFiles.isEmpty()) {
                chbMedia.getSelectionModel().selectFirst(); // Select the first media file
            }
        } catch (SQLException e) {
            System.err.println("Error loading media files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Saves the file metadata.
     * Saves the metadata of the file to the database.
     *
     * @param file The file to save
     * @param filePath The file path of the saved file
     */
    private void saveFileMetadata(File file, String filePath) {
        String mediaType = null; // Media type of the file
        // Determine the media type based on the file content
        try {
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType != null) {
                if (mimeType.startsWith("video/")) {
                    mediaType = "Video";
                } else if (mimeType.startsWith("audio/")) {
                    mediaType = "Audio";
                } else {
                    throw new IllegalArgumentException("Unsupported media type: " + mimeType);
                }
            } else {
                throw new NullPointerException("MediaType could not be determined for the file: " + file.getName());
            }
        } catch (IOException e) {
            System.err.println("Error determining media type: " + e.getMessage());
            return; // Stop further processing if the media type can't be determined
        }

        String query = "INSERT INTO MediaFiles (UserID, FileName, FilePath, MediaType, FileSize, IsPublic, IsDeleted, Comments) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"; // Query to insert the file metadata
        // Save the file metadata to the database
        try (Connection conn = dbConnection.getConnection(); // Assuming databaseLink is a valid and open connection
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, file.getName());
            pstmt.setString(3, filePath);
            pstmt.setString(4, mediaType); // Set the adjusted media type
            pstmt.setLong(5, file.length());
            pstmt.setBoolean(6, true);  // IsPublic
            pstmt.setBoolean(7, false);  // IsDeleted
            pstmt.setString(8, "");      // Comments

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("File metadata saved successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error uploading file metadata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Applies the color theme.
     * Applies the color theme to the media player.
     *
     * @param backgroundColor The background color
     * @param textColor The text color
     * @param buttonColor The button color
     */
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

        if (paneMedia != null) {
            paneMedia.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
        if (lblDuration != null) {
            lblDuration.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblMedia != null) {
            lblMedia.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (btnUpload != null) {
            btnUpload.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnDelete != null) {
            btnDelete.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnStop != null) {
            btnStop.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnSelect != null) {
            btnSelect.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnPlay != null) {
            btnPlay.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnRFS != null) {
            btnRFS.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnPause != null) {
            btnPause.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (chbMedia != null) {
            chbMedia.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
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
