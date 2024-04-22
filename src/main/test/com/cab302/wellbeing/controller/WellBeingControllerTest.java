package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.DataBaseConnection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class WellBeingControllerTest {

    @InjectMocks
    private WellBeingController wellBeingController;

    @Mock
    private DataBaseConnection mockDataBaseConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;


    @BeforeAll
    public static void setupAll() {
        Platform.startup(() -> {}); // Ensure JavaFX toolkit is initialized
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);

        wellBeingController.txtUsr = new TextField();
        wellBeingController.txtPwd = new PasswordField();
        wellBeingController.lblLoginMsg = new Label();
        wellBeingController.btnExit = new Button();

    }

    @Test
    public void testValidateLoginSuccess() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("passwordHash")).thenReturn("cab302");
        when(mockResultSet.getInt("userId")).thenReturn(1);

        wellBeingController.txtUsr.setText("cab302");
        wellBeingController.txtPwd.setText("cab302");

        wellBeingController.validateLogin(mock(ActionEvent.class));

        assertEquals("Welcome cab302", wellBeingController.lblLoginMsg.getText());
    }

    @Test
    public void testValidateLoginFailure() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        wellBeingController.txtUsr.setText("john");
        wellBeingController.txtPwd.setText("wrongpassword");

        wellBeingController.validateLogin(mock(ActionEvent.class));

        assertEquals("Your username or password is wrong", wellBeingController.lblLoginMsg.getText());
    }

    @Test
    public void testBtnExitOnAction() {
        Platform.runLater(() -> {
            wellBeingController.btnExitOnAction(mock(ActionEvent.class));
            assertFalse(wellBeingController.stage.isShowing());
        });
    }
}