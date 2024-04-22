package com.cab302.wellbeing.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;
import org.mockito.*;


public class MainMenuControllerTest {

    @InjectMocks
    private MainMenuController mainMenuController;

    @BeforeAll
    public static void setupAll() {
        Platform.startup(() -> {});
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