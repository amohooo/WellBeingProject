import com.cab302.wellbeing.controller.MainMenuController;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MainMenuControllerTest {

    @InjectMocks
    private MainMenuController mainMenuController;

    private static boolean isPlatformReady = false; // To ensure Platform.startup() is called only once

    @BeforeAll
    public static void setupAll() {
        try {
            if (!isPlatformReady) {
                Platform.startup(() -> {});
                isPlatformReady = true; // Set the flag to true once Platform is initialized
            }
        } catch (IllegalStateException e) {
            System.out.println("Platform already initialized. Skipping reinitialization.");
        }
    }
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mainMenuController.lblName = new Label();
        mainMenuController.btnLogOut = new Button();

        Platform.runLater(() -> {
            mainMenuController = new MainMenuController();
        });
    }

    @Test
    public void testDisplayName() {
        Platform.runLater(() -> {
            mainMenuController.displayName("Alice");
            assertEquals("Alice, wish you are having a bright day!", mainMenuController.lblName.getText());
        });
    }
}