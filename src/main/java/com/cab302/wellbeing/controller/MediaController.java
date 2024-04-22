package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.DataBaseConnection;
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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class MediaController implements Initializable {

    @FXML private MediaView mediaView;
    @FXML private Button btnPlay, btnPause, btnStop;
    @FXML private Label lblDuration;
    @FXML private Slider slider;

    private Media media;
    private MediaPlayer mediaPlayer;
    private boolean isPlayed = false;
    private int userID;
    private DataBaseConnection dbConnection;

    @FXML
    private void selectMedia() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Media");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            setupMediaPlayer(file.toURI().toString());
        } else {
            setupMediaPlayer("src/main/resources/com/cab302/wellbeing/Media/Mental_Wellbeing.mp4");
        }
    }

    private void setupMediaPlayer(String mediaPath) {
        File mediaFile = new File(mediaPath);
        String uriString = mediaFile.toURI().toString();  // Converts the file path to a URI string with the 'file:' scheme
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        media = new Media(uriString);  // Use the URI string here
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        bindMediaPlayer();
    }

    private void bindMediaPlayer() {
        mediaPlayer.currentTimeProperty().addListener((observableValue, oldValue, newValue) -> {
            slider.setValue(newValue.toSeconds());
            lblDuration.setText("Duration: " + (int)slider.getValue() + " / " + (int)media.getDuration().toSeconds());
        });
        mediaPlayer.setOnReady(() -> {
            Duration duration = media.getDuration();
            slider.setMax(duration.toSeconds());
            lblDuration.setText("Duration: 00 / " + (int)media.getDuration().toSeconds());
        });
        mediaPlayer.setAutoPlay(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DataBaseConnection();
        setupMediaPlayer("src/main/resources/com/cab302/wellbeing/Media/Mental_Wellbeing.mp4");
    }

    public void playMedia() {
        if (!isPlayed) {
            btnPlay.setText("Pause");
            mediaPlayer.play();
            isPlayed = true;
        } else {
            btnPlay.setText("Play");
            mediaPlayer.pause();
            isPlayed = false;
        }
    }

    public void pauseMedia() {
        mediaPlayer.pause();
    }

    public void stopMedia() {
        mediaPlayer.stop();
    }

    @FXML
    private void sliderPressed() {
        mediaPlayer.seek(Duration.seconds(slider.getValue()));
    }

    public void setUserId(int userId) {
        this.userID = userId;  // Now you can use this userId to store browsing data linked to the user
    }

    @FXML
    private void uploadMedia() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Media");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            String path = saveFileToServer(file);
        }
    }

    private String saveFileToServer(File file) {
        String directoryPath = "uploadMedia";  // Set your server storage path
        Path directory = Paths.get(directoryPath);

        try {
            // Ensure the directory exists
            Files.createDirectories(directory);

            // Define the target path for the file
            Path targetPath = directory.resolve(file.getName());

            // Copy the file to the target directory, replacing existing file with the same name if it exists
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String filePath = targetPath.toString();
            saveFileMetadata(file, filePath);  // Assuming you have a method to save file metadata to a database
            return filePath;
        } catch (IOException e) {
            System.err.println("Failed to save the file: " + e.getMessage());
            e.printStackTrace();
            return null;
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
            pstmt.setInt(1, userID);
            pstmt.setString(2, file.getName());
            pstmt.setString(3, filePath);
            pstmt.setString(4, mediaType); // Set the adjusted media type
            pstmt.setLong(5, file.length());
            pstmt.setBoolean(6, false);  // IsPublic
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
