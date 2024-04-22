package com.cab302.wellbeing.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.cab302.wellbeing.DataBaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterControllerTest {

    private RegisterController registerController;
    private DataBaseConnection mockConnection;
    private Connection mockDB;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    @BeforeEach
    void setUp() throws SQLException {
        registerController = new RegisterController();
        mockConnection = mock(DataBaseConnection.class);
        mockDB = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        // Mock the RadioButtons
        registerController.radbAdm.setText("Admin");

        // Make isSelected() always return false
        when(mockConnection.getConnection()).thenReturn(mockDB);
        registerController.radbAdm = mock(RadioButton.class);
        when(registerController.radbAdm.isSelected()).thenReturn(false);
        when(mockDB.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @AfterEach
    void tearDown() {
        // Close the database connection after each test
        if (mockDB != null) {
            try {
                mockDB.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testRegisterUser() throws SQLException {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(4)).thenReturn(1); // No existing user

        registerController.registerUser();

        verify(mockStatement, times(1)).executeUpdate();
    }
}
