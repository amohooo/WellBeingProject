import com.cab302.wellbeing.model.DataBaseConnection;
import com.cab302.wellbeing.controller.BrowsingHistoryController;
import com.cab302.wellbeing.controller.InternetExplorerController;
import com.cab302.wellbeing.controller.MainMenuController;
import com.cab302.wellbeing.controller.WellBeingController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrowsingHistoryControllerUnitTest {

    @InjectMocks
    private BrowsingHistoryController browsingHistoryController;

    @InjectMocks
    private InternetExplorerController internetExplorerController;

    @InjectMocks
    private WellBeingController wellBeingController;

    @Mock
    private DataBaseConnection mockDataBaseConnection;

    @Mock
    private static Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private static Stage loginStage;
    private static Stage mainMenuStage;
    private static Stage internetExplorerStage;
    private static Stage browsingHistoryStage;
    private static AutoCloseable closeable;

    private static boolean isPlatformInitialized = false;

    @BeforeAll
    public static void setupAll() {
        try {
            if (!isPlatformInitialized) {
                Platform.startup(() -> {
                    // JavaFX initialization logic
                });
                isPlatformInitialized = true;
            }
        } catch (IllegalStateException e) {
            System.out.println("JavaFX Toolkit already initialized.");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Mock the database connection and setup the user record
                when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                when(mockPreparedStatement.executeUpdate()).thenReturn(1);
                when(mockResultSet.next()).thenReturn(true);
                when(mockResultSet.getInt("userId")).thenReturn(1);
                when(mockResultSet.getString("passwordHash")).thenReturn("cab302");

                // Mock user existence in `useraccount` table
                String userQuery = "SELECT * FROM useraccount WHERE userId = ?";
                PreparedStatement userStatement = mock(PreparedStatement.class);
                ResultSet userResultSet = mock(ResultSet.class);
                when(mockConnection.prepareStatement(eq(userQuery))).thenReturn(userStatement);
                when(userStatement.executeQuery()).thenReturn(userResultSet);
                when(userResultSet.next()).thenReturn(true);

                // Load the Login page
                FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("login.fxml"));
                Parent loginRoot = loginLoader.load();
                wellBeingController = loginLoader.getController();
                loginStage = new Stage();
                loginStage.setScene(new Scene(loginRoot));
                loginStage.setTitle("Login");
                loginStage.show();

                // Perform login
                wellBeingController.txtUsr.setText("cab302");
                wellBeingController.txtPwd.setText("cab302");
                wellBeingController.lblLoginMsg.setText("");
                wellBeingController.lblLoginMsgOnAction(null);

                // Load the Main Menu page
                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(event -> {
                    try {
                        FXMLLoader mainMenuLoader = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
                        Parent mainMenuRoot = mainMenuLoader.load();
                        MainMenuController mainMenuController = mainMenuLoader.getController();
                        mainMenuController.setUserId(1, "login");
                        mainMenuController.setFirstName("cab302");

                        mainMenuStage = new Stage();
                        mainMenuStage.setScene(new Scene(mainMenuRoot));
                        mainMenuStage.setTitle("Main Menu");
                        mainMenuStage.show();
                        loginStage.close();

                        // Load the Internet Explorer page
                        FXMLLoader internetLoader = new FXMLLoader(getClass().getResource("InternetExplorer.fxml"));
                        Parent internetRoot = internetLoader.load();
                        internetExplorerController = internetLoader.getController();
                        internetExplorerController.setUserId(1);
                        internetExplorerController.setFirstName("cab302");
                        internetExplorerStage = new Stage();
                        internetExplorerStage.setScene(new Scene(internetRoot));
                        internetExplorerStage.setTitle("Internet Explorer");
                        internetExplorerStage.show();
                        mainMenuStage.close();

                        // Load the Browsing History page
                        FXMLLoader browsingHistoryLoader = new FXMLLoader(getClass().getResource("BrowsingHistory.fxml"));
                        Parent browsingHistoryRoot = browsingHistoryLoader.load();
                        browsingHistoryController = browsingHistoryLoader.getController();
                        browsingHistoryController.setFirstName("cab302");
                        browsingHistoryStage = new Stage();
                        browsingHistoryStage.setScene(new Scene(browsingHistoryRoot));
                        browsingHistoryStage.setTitle("Browsing History");

                        // Set the URL and start loading the page
                        internetExplorerController.txtAddr.setText("www.example.com");
                        internetExplorerController.LoadPage();

                        internetExplorerController.engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                            if (newState == Worker.State.SUCCEEDED) {
                                latch.countDown();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        fail("Could not initialize the stages properly.");
                        latch.countDown();
                    }
                });
                pause.play();
            } catch (Exception e) {
                e.printStackTrace();
                fail("Could not initialize the stages properly.");
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @Test
    public void testLoadHistory_Success() throws InterruptedException, SQLException {
        CountDownLatch latch = new CountDownLatch(1);

        // Mock browsing data query
        String mockQuery = "SELECT URL, StartTime, EndTime, SessionDate FROM BrowsingData WHERE UserID = ?";
        when(mockConnection.prepareStatement(eq(mockQuery))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // Only one record
        when(mockResultSet.getString("URL")).thenReturn("www.example.com");
        when(mockResultSet.getTimestamp("StartTime")).thenReturn(Timestamp.valueOf("2024-05-06 12:00:00"));
        when(mockResultSet.getTimestamp("EndTime")).thenReturn(Timestamp.valueOf("2024-05-06 12:05:00"));
        when(mockResultSet.getDate("SessionDate")).thenReturn(Date.valueOf("2024-05-06"));

        Platform.runLater(() -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                browsingHistoryStage.show();
                browsingHistoryController.loadHistory();
                latch.countDown();
            });
            pause.play();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }
    @Test
    public void testLoadHistory_NoResults() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Mock no results returned from the database
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                when(mockResultSet.next()).thenReturn(false); // Simulate no results found

                // Set meaningless input in the txtUrl field
                browsingHistoryController.txtUrl.setText("@#*123!random");

                // Set the user's first name for greeting purposes
                browsingHistoryController.setFirstName("cab302");

                // Attempt to load browsing history
                browsingHistoryController.loadHistory();

                // Add a delay to let the UI update
                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(event -> {
                    assertEquals("Sorry, cab302, No browsing history found", browsingHistoryController.lblGreeting.getText());
                    assertEquals("Failed to load browsing history: No results found.", browsingHistoryController.historyDisplayArea.getText());
                    latch.countDown();
                });
                pause.play();
            } catch (SQLException e) {
                fail("SQLException occurred: " + e.getMessage());
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @Test
    public void testClearHistoryDisplay() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            browsingHistoryController.clearHistoryDisplay();

            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> {
                assertEquals("", browsingHistoryController.historyDisplayArea.getText());
                assertEquals("Welcome, want to see your browsing history?", browsingHistoryController.lblGreeting.getText());
                latch.countDown();
            });
            pause.play();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        closeable.close();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            if (loginStage != null) loginStage.close();
            if (mainMenuStage != null) mainMenuStage.close();
            if (internetExplorerStage != null) internetExplorerStage.close();
            if (browsingHistoryStage != null) browsingHistoryStage.close();
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for FX platform.");

        if (mockConnection != null) {
            try {
                mockConnection.close();
            } catch (SQLException e) {
                System.err.println("Error closing mock connection: " + e.getMessage());
            }
        }
    }
}

