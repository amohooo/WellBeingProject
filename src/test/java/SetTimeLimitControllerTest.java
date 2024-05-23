import com.cab302.wellbeing.controller.SetTimeLimitController;
import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SetTimeLimitControllerTest {

    @InjectMocks
    private SetTimeLimitController setTimeLimitController;

    @Mock
    private DataBaseConnection mockDataBaseConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private static AutoCloseable closeable;

    private static boolean isPlatformInitialized = false;

    @BeforeAll
    public static void setupAll() {
        new JFXPanel();
    }

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);

        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        setInitialData();
    }

    private void setInitialData() {
        setTimeLimitController.txtHH = new TextField();
        setTimeLimitController.txtMM = new TextField();
        setTimeLimitController.txtSS = new TextField();
        setTimeLimitController.lblMsg = new Label();
        setTimeLimitController.chbActive = new CheckBox();
        setTimeLimitController.rdbNotify = new RadioButton();
        setTimeLimitController.rdbAsk = new RadioButton();
        setTimeLimitController.rdbExit = new RadioButton();
        setTimeLimitController.btnSaveT = new Button();
        setTimeLimitController.btnCancelT = new Button();
    }

    @Test
    @Order(1)
    public void testInitialize() {
        Platform.runLater(() -> {
            setTimeLimitController.initialize();
            assertTrue(setTimeLimitController.rdbNotify.isSelected());
        });
    }

    @Test
    @Order(2)
    public void testSetTimeLimits() {
        Platform.runLater(() -> {
            setTimeLimitController.setTimeLimits(1, 30, 45, true, "Ask");
            assertEquals("1", setTimeLimitController.txtHH.getText());
            assertEquals("30", setTimeLimitController.txtMM.getText());
            assertEquals("45", setTimeLimitController.txtSS.getText());
            assertTrue(setTimeLimitController.chbActive.isSelected());
            assertTrue(setTimeLimitController.rdbAsk.isSelected());
        });
    }

    @Test
    @Order(3)
    public void testSaveTimeLimits() throws SQLException, InterruptedException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Platform.runLater(() -> {
            setTimeLimitController.txtHH.setText("1");
            setTimeLimitController.txtMM.setText("30");
            setTimeLimitController.txtSS.setText("45");
            setTimeLimitController.chbActive.setSelected(true);
            setTimeLimitController.rdbNotify.setSelected(true);

            setTimeLimitController.saveTimeLimits();
            assertEquals("Time limits have been disabled.", setTimeLimitController.lblMsg.getText());
        });

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(2, TimeUnit.SECONDS);
    }

    @Test
    @Order(4)
    public void testDeleteTimeLimits() throws SQLException, InterruptedException {
        Platform.runLater(() -> {
            setTimeLimitController.chbActive.setSelected(false);

            setTimeLimitController.saveTimeLimits();
            assertEquals("Time limits have been disabled.", setTimeLimitController.lblMsg.getText());
        });

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(2, TimeUnit.SECONDS);
    }

    @Test
    @Order(5)
    public void testInvalidTimeInputs() throws InterruptedException {
        Platform.runLater(() -> {
            setTimeLimitController.txtHH.setText("25"); // Invalid hour
            setTimeLimitController.saveTimeLimits();
            assertEquals("Hours must be between 0 and 23", setTimeLimitController.lblMsg.getText());

            setTimeLimitController.txtHH.setText("2");
            setTimeLimitController.txtMM.setText("60"); // Invalid minute
            setTimeLimitController.saveTimeLimits();
            assertEquals("Minutes must be between 0 and 59", setTimeLimitController.lblMsg.getText());

            setTimeLimitController.txtMM.setText("30");
            setTimeLimitController.txtSS.setText("60"); // Invalid second
            setTimeLimitController.saveTimeLimits();
            assertEquals("Seconds must be between 0 and 59", setTimeLimitController.lblMsg.getText());
        });

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(2, TimeUnit.SECONDS);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
        if (mockConnection != null) {
            mockConnection.close();
        }
    }
}