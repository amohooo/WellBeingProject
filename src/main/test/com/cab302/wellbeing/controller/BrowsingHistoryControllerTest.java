package com.cab302.wellbeing.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.cab302.wellbeing.DataBaseConnection;
import javafx.scene.control.TextArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BrowsingHistoryControllerTest {
    private BrowsingHistoryController controller;
    private DataBaseConnection mockConnection;
    private Connection mockDB;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        controller = new BrowsingHistoryController();
        controller.historyDisplayArea = new TextArea();
        controller = new BrowsingHistoryController();
        mockConnection = mock(DataBaseConnection.class);
        mockDB = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockConnection.getConnection()).thenReturn(mockDB);
        when(mockDB.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @Test
    void testLoadHistory() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false); // Two rows in the result set
        when(mockResultSet.getString("URL")).thenReturn("http://example.com");
        when(mockResultSet.getString("StartTime")).thenReturn("10:00:00");
        when(mockResultSet.getString("SessionDate")).thenReturn("2022-01-01");
        when(mockResultSet.getInt("Duration")).thenReturn(60);

        controller.loadHistory();

        verify(mockStatement, times(1)).executeQuery();
    }
}