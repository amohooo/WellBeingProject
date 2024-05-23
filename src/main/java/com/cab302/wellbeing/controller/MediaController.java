package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.*;
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

public class MediaController implements Initializable {
    private int userId;
    private static final Color DEFAULT_COLOR = Color.web("#009ee0");
    private static final Color DEFAULT_TEXT_COLOR = Color.web("#ffffff");
    private String accType;
    public void setUserType(String accType) {
        this.accType = accType;
        System.out.println("User type: " + accType);
        if (accType.equals("General")) {
            btnUpload.setDisable(true);
            btnDelete.setDisable(true);
        } else {
            btnUpload.setDisable(false);
            btnDelete.setDisable(false);
        }
    }
    public void setUserId(int userId) {
        this.userId = userId;  // Now you can use this userId to store browsing data linked to the user
    }
    @FXML private MediaView mediaView;
    @FXML private Button btnPlay, btnPause, btnStop, btnUpload, btnDelete, btnRFS, btnSelect;
    @FXML private Label lblBkGrd, lblDuration, lblMedia;
    @FXML private Slider slider;
    @FXML
    private Pane paneMedia;
    @FXML
    public ChoiceBox<String> chbMedia;
    private Media media;
    public MediaPlayer mediaPlayer;
    private boolean isPlayed = false;

    private DataBaseConnection dbConnection;
    public void setDbConnection(DataBaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    private void setupMediaPlayer(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            filePath = "src/main/java/com/cab302/wellbeing/Media/MindRefresh.mp3";  // Default file path
        }
        File mediaFile = new File(filePath);
        if (!mediaFile.exists()) {
            System.err.println("File does not exist: " + filePath);
            return;
        }

        String uriString = mediaFile.toURI().toString();
        System.out.println("URI for media: " + uriString);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        media = new Media(uriString);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        bindMediaPlayer();
    }
    @FXML
    private void selectMedia() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Media");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            setupMediaPlayer(file.getAbsolutePath());
        } else {
            setupMediaPlayer("src/main/java/com/cab302/wellbeing/Media/MindRefresh.mp3");
        }
    }
    private void bindMediaPlayer() {
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            slider.setValue(newValue.toSeconds());
            lblDuration.setText(formatDuration(newValue) + " / " + formatDuration(media.getDuration()));
        });
        mediaPlayer.setOnReady(() -> {
            Duration duration = media.getDuration();
            slider.setMax(duration.toSeconds());
            lblDuration.setText("Duration: 00:00:00 / " + formatDuration(duration));
        });
        mediaPlayer.setAutoPlay(false);
    }

    private String formatDuration(Duration duration) {
        int hours = (int) duration.toHours();
        int minutes = (int) duration.toMinutes() % 60;
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DataBaseConnection();
        setUserId(userId);
        loadMediaFilesToChoiceBox(); // this method sets up the media files in the ChoiceBox
        setupMediaPlayer("src/main/java/com/cab302/wellbeing/Media/MindRefresh.mp3");
        Platform.runLater(() -> {
            if (mediaView.getScene() != null && mediaView.getScene().getWindow() != null) {
                Stage stage = (Stage) mediaView.getScene().getWindow();
                stage.setOnCloseRequest(event -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();
                    }
                });
            }
        });
        chbMedia.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadMedia(newValue);
            }
        });
    }
    public void loadMedia(String fileName) {
        if (userId == 0) {
            System.out.println("No user ID provided");
            return;
        }
        String query = "SELECT FilePath FROM MediaFiles WHERE FileName = ? AND UserID = ? AND IsDeleted = FALSE AND IsPublic = TRUE";
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
    public void playMedia() {
        mediaPlayer.play();
    }
    public void pauseMedia() {
        mediaPlayer.pause();
    }
    public void refreshMediaList() {
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5)); // Introduce a delay before closing the window for the test purpose
        delay.setOnFinished(event -> loadMediaFilesToChoiceBox());
        delay.play();
        //loadMediaFilesToChoiceBox();
    }
    public void stopMedia() {
        mediaPlayer.stop();
    }
    @FXML
    private void sliderPressed() {
        mediaPlayer.seek(Duration.seconds(slider.getValue()));
    }
    @FXML
    private void uploadMedia() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Media");
        File file = fileChooser.showOpenDialog(null);
        if (file != null && !checkFileExists(file.getName())) {
            String path = saveFileToServer(file);
        } else {
            System.out.println("File already exists or selection was cancelled.");
        }
    }
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
    private boolean checkFileExists(String fileName) {
        String query = "SELECT COUNT(*) FROM MediaFiles WHERE FileName = ? AND UserID = ? AND IsDeleted = FALSE";
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

    private String saveFileToServer(File file) {
        String directoryPath = "src/main/resources/com/cab302/wellbeing/Media/";  // Set your server storage path
        Path directory = Paths.get(directoryPath);

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
    public void loadMediaFilesToChoiceBox() {
        if (userId <= 0) {
            return;
        }
        String query = "SELECT FileName FROM MediaFiles WHERE UserID = ? AND IsDeleted = FALSE AND IsPublic = TRUE";
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
            chbMedia.setItems(mediaFiles);

            if (!mediaFiles.isEmpty()) {
                chbMedia.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            System.err.println("Error loading media files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveFileMetadata(File file, String filePath) {
        String mediaType = null;
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

        String query = "INSERT INTO MediaFiles (UserID, FileName, FilePath, MediaType, FileSize, IsPublic, IsDeleted, Comments) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
