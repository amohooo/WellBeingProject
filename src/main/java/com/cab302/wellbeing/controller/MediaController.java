package com.cab302.wellbeing.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MediaController implements Initializable {
    @FXML
    private MediaView mediaView;
    @FXML
    private Button btnPlay, btnPause, btnStop;
    private File file;
    private Media media;
    private MediaPlayer mediaPlayer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        file = new File("src/main/resources/com/cab302/wellbeing/Media/LCC_Mental-Wellbeing-Phase-2_Video_Subs_V1-1.mp4");
        media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
    }

    public void playMedia(){
        mediaPlayer.play();
    }
    public void pauseMedia(){
        mediaPlayer.pause();
    }
    public void stopMedia(){
        mediaPlayer.stop();
    }

}
