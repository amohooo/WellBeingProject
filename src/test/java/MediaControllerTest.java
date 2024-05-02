import com.cab302.wellbeing.DataBaseConnection;
import com.cab302.wellbeing.controller.MediaController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

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
    // Static flag to ensure JavaFX toolkit initialization only happens once
    private static boolean isPlatformInitialized = false;

    @BeforeAll
    public static void setupAll() {
        try {
            if (!isPlatformInitialized) {
                Platform.startup(() -> {
                    // Place any setup code here that must run once JavaFX is initialized
                });
                isPlatformInitialized = true;
            }
        } catch (IllegalStateException e) {
            System.out.println("JavaFX toolkit already initialized.");
        }
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
    void testRefreshMediaListWithNullUserId() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // Wrap the method call and verification in Platform.runLater to ensure it runs on the JavaFX thread
        Platform.runLater(() -> {
            try {
                mediaController.setUserId(0); // Simulate user not set scenario
                mediaController.refreshMediaList();
                try {
                    verify(preparedStatement, never()).executeQuery(); // Assert that the query was never executed
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                latch.countDown(); // Ensure latch is counted down even if there is an exception
            }
        });

        latch.await(); // Wait for the JavaFX thread to finish execution
    }
}