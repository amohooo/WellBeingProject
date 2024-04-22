package com.cab302.wellbeing.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cab302.wellbeing.DataBaseConnection;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PasswordResetControllerTest {

    @InjectMocks
    private static PasswordResetController passwordResetController;

    @Mock
    private DataBaseConnection mockDataBaseConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private Stage mockStage;
    @BeforeAll
    public static void setUpAll() {
        Platform.startup(() -> {
            passwordResetController = new PasswordResetController();
        });
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
        passwordResetController = spy(new PasswordResetController() {
        });

        passwordResetController.txtEmailAdd = new TextField();
        passwordResetController.ptxtPwd = new PasswordField();
        passwordResetController.ptxtRePwd = new PasswordField();
        passwordResetController.lblMsg = new Label();
        passwordResetController.btnReset = new Button();
        passwordResetController.btnCncl = new Button();
    }

    @Test
    public void testResetPassword_AllFieldsEmpty() {
        // Scenario: All fields are empty
        passwordResetController.resetPassword();
        assertEquals("Please fill in all fields.", passwordResetController.lblMsg.getText());
    }

    @Test
    public void testResetPassword_PasswordsDoNotMatch() {
        // Scenario: Passwords do not match
        passwordResetController.txtEmailAdd.setText("user@example.com");
        passwordResetController.ptxtPwd.setText("password123");
        passwordResetController.ptxtRePwd.setText("password321");

        passwordResetController.resetPassword();
        assertEquals("Passwords do not match.", passwordResetController.lblMsg.getText());
    }

    @Test
    public void testResetPassword_EmailDoesNotExist() throws Exception {
        // Scenario: The email does not exist in the database
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // Simulate no results found

        passwordResetController.txtEmailAdd.setText("nonexistent@example.com");
        passwordResetController.ptxtPwd.setText("password123");
        passwordResetController.ptxtRePwd.setText("password123");

        passwordResetController.resetPassword();
        assertEquals("The email does not exist. Please try again.", passwordResetController.lblMsg.getText());
    }

    @Test
    public void testResetPassword_SuccessfulReset() throws Exception {
        // Scenario: Successful password reset
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true); // Simulate email exists

        passwordResetController.txtEmailAdd.setText("cab302@qut.edu.au");
        passwordResetController.ptxtPwd.setText("cab302");
        passwordResetController.ptxtRePwd.setText("cab302");

        passwordResetController.resetPassword();
        assertEquals("Password successfully reset. Check your email for the confirmation link.", passwordResetController.lblMsg.getText());
    }
}