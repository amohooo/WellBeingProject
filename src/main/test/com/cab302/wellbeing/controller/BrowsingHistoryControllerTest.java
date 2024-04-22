package com.cab302.wellbeing.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cab302.wellbeing.DataBaseConnection;
import com.cab302.wellbeing.UserSession;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.time.LocalDate;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BrowsingHistoryControllerTest {

    @InjectMocks
    private BrowsingHistoryController browsingHistoryController;

    @InjectMocks
    private InternetExplorerController internetExplorerController;

    @Mock
    private DataBaseConnection mockDataBaseConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private static AutoCloseable closeable;

    @BeforeAll
    public static void init() {
        Platform.startup(() -> {}); // Ensure JavaFX toolkit is initialized
    }

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);

        // Initialize controllers within the JavaFX thread to avoid toolkit initialization errors
        Platform.runLater(() -> {
            internetExplorerController = new InternetExplorerController();
            internetExplorerController.txtAddr = new TextField("http://www.example.com");
            internetExplorerController.LoadPage(); // Ensure LoadPage() is called within the JavaFX thread

            browsingHistoryController = new BrowsingHistoryController();
            browsingHistoryController.startDatePicker = new DatePicker();
            browsingHistoryController.endDatePicker = new DatePicker();
            browsingHistoryController.historyDisplayArea = new TextArea();
            browsingHistoryController.txtUrl = new TextField();
            browsingHistoryController.btnLoadHistory = new Button();
            browsingHistoryController.lblGreeting = new Label();
            browsingHistoryController.txtUrl.setText("example"); // Set text as part of simulated navigation
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close(); // Close resources related to Mockito
    }

    @Test
    public void testLoadHistory_Success() throws Exception {
        // Simulate successful database query
        Platform.runLater(() -> {
            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
                when(mockResultSet.next()).thenReturn(true);
                when(mockResultSet.getString("firstName")).thenReturn("John");


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            browsingHistoryController.loadHistory();

            assertEquals("John, here is your browsing history:", browsingHistoryController.lblGreeting.getText());
            assertTrue(browsingHistoryController.historyDisplayArea.getText().contains("example"));
        });
    }

    @Test
    public void testLoadHistory_Failure() throws Exception {
        // Simulate a database error
        Platform.runLater(() -> {
            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
                when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


            browsingHistoryController.loadHistory();

            assertEquals("Failed to load browsing history: Database error", browsingHistoryController.lblGreeting.getText());
        });
    }

    @Test
    public void testClearHistoryDisplay() {
        // Ensure UI modifications are performed on the JavaFX thread
        Platform.runLater(() -> {
            browsingHistoryController.clearHistoryDisplay();

            assertEquals("", browsingHistoryController.historyDisplayArea.getText());
            assertEquals("Welcome, want to see your browsing history?", browsingHistoryController.lblGreeting.getText());
        });
    }
}