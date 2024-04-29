package com.cab302.wellbeing.controller;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

import com.cab302.wellbeing.DataBaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class MediaControllerTest {

    @InjectMocks
    private MediaController mediaController;

    @Mock
    private MediaView mediaView;
    @Mock
    private MediaPlayer mediaPlayer;
    @Mock
    private DataBaseConnection dbConnection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private Connection connection;
    @Mock
    private ResultSet resultSet;

    // Ensure JavaFX toolkit is initialized
    @BeforeAll
    public static void setupAll() {
        // Initialize JavaFX toolkit
        Platform.startup(() -> {});
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mediaController.chbMedia = Mockito.mock(ChoiceBox.class);
        Mockito.when(mediaController.chbMedia.getItems()).thenReturn(FXCollections.observableArrayList());

        when(dbConnection.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false); // Simulate one result found, then end
        when(resultSet.getString("FileName")).thenReturn("test.mp4");
        when(resultSet.getString("FilePath")).thenReturn("file:/test.mp4");

        // Mocking the media player setup to avoid actual media loading
        mediaController.mediaPlayer = mediaPlayer;
    }

    @Test
    void testLoadMediaFilesToChoiceBoxWithNullUserId() throws Exception {
        mediaController.setUserId(0); // Ensure userId is explicitly set to 0
        mediaController.loadMediaFilesToChoiceBox();
        verify(preparedStatement, never()).executeQuery(); // Assert that the query was never executed
        assertTrue(mediaController.chbMedia.getItems().isEmpty()); // Assert the choice box remains empty
    }

    @Test
    void testPlayMedia() {
        mediaController.playMedia();
        verify(mediaPlayer, times(1)).play();
    }

    @Test
    void testPauseMedia() {
        mediaController.pauseMedia();
        verify(mediaPlayer, times(1)).pause();
    }

    @Test
    void testStopMedia() {
        mediaController.stopMedia();
        verify(mediaPlayer, times(1)).stop();
    }

    @Test
    void testRefreshMediaListWithNullUserId() throws SQLException {
        mediaController.setUserId(0); // Simulate user not set scenario
        mediaController.refreshMediaList();
        verify(preparedStatement, never()).executeQuery(); // Assert that the query was never executed
    }
}