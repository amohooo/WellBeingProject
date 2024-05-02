package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.DataBaseConnection;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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
    public void setUserId(int userId) {
        this.userId = userId;  // Now you can use this userId to store browsing data linked to the user
    }
    @FXML private MediaView mediaView;
    @FXML private Button btnPlay, btnPause, btnStop, btnLoad, btnUpload;
    @FXML private Label lblDuration;
    @FXML private Slider slider;
    @FXML
    public ChoiceBox<String> chbMedia;
    private Media media;
    public MediaPlayer mediaPlayer;
    private boolean isPlayed = false;

    private DataBaseConnection dbConnection;

    private void setupMediaPlayer(String filePath) {
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
            setupMediaPlayer("src/main/resources/com/cab302/wellbeing/Media/MindRefresh.mp3");
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

        PauseTransition delay = new PauseTransition(Duration.seconds(0.1)); // Introduce a delay before closing the window for the test purpose
        delay.setOnFinished(event -> loadMediaFilesToChoiceBox());
        delay.play();

        setupMediaPlayer("src/main/resources/com/cab302/wellbeing/Media/MindRefresh.mp3");

        // Delay window-related setup until the MediaView is displayed
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
    private void loadMedia(String fileName) {
        if (userId == 0) {  // Assuming userId is 0 when not set
            System.out.println("No user ID provided");
            return; // Exit the method if userId is not set
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
                saveFileMetadata(file, filePath);  // Assuming you have a method to save file metadata to a database
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
//            PauseTransition delay = new PauseTransition(Duration.seconds(0.5)); // Introduce a delay before closing the window for the test purpose
//            delay.setOnFinished(event -> chbMedia.setItems(mediaFiles));
//            delay.play();
            chbMedia.setItems(mediaFiles);

            if (!mediaFiles.isEmpty()) {
//                PauseTransition delay1 = new PauseTransition(Duration.seconds(0.5)); // Introduce a delay before closing the window for the test purpose
//                delay1.setOnFinished(event -> chbMedia.getSelectionModel().selectFirst());
//                delay1.play();
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
}
