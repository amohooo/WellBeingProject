package com.cab302.wellbeing.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cab302.wellbeing.DataBaseConnection;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;

/**
 * This class is used to test the RegisterController class.
 * It uses Mockito to mock the database connection and test the user registration functionality.
 * The tests check if the user registration is successful, fails due to a database issue, fails due to blank inputs, and fails due to an invalid email format.
 * The tests also clean up the database after they are run.
 * The test cases include:
 * - testRegisterUser_SuccessfulRegistration
 * - testRegisterUser_FailedRegistration_DatabaseIssue
 * - testRegisterUser_ValidationFailure_BlankInputs
 * - testRegisterUser_ValidationFailure_EmailFormat
 * The testRegisterUser_SuccessfulRegistration test checks if the user registration is successful.
 * The testRegisterUser_FailedRegistration_DatabaseIssue test checks if the user registration fails due to a database issue.
 * The testRegisterUser_ValidationFailure_BlankInputs test checks if the user registration fails due to blank inputs.
 * The testRegisterUser_ValidationFailure_EmailFormat test checks if the user registration fails due to an invalid email format.
 * The tests clean up the database after they are run.
 * The tests use the setUpAll method to initialize the RegisterController object.
 * The tests use the setUp method to initialize the mocks and set up the test environment.
 * The tests use the tearDown method to clean up the database after they are run.
 */
public class RegisterControllerTest {

    @InjectMocks
    private static RegisterController registerController;

    @Mock
    private static DataBaseConnection mockDataBaseConnection;

    @Mock
    private static Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;
    private static Connection realTestDbConnection;

    /**
     * This method is run once before all tests.
     * It initializes the RegisterController object.
     */
    @BeforeAll
    public static void setUpAll() {
        Platform.startup(() -> {
            registerController = new RegisterController();
            try {
                realTestDbConnection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/wellbeing", "cab302", "cab302");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * This method is run before each test.
     * It initializes the mocks and sets up the test environment.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(mockDataBaseConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        registerController.txtFName = new TextField();
        registerController.txtLName = new TextField();
        registerController.txtUsername = new TextField();
        registerController.txtEmail = new TextField();
        registerController.ptxtPwd = new PasswordField();
        registerController.ptxtRetp = new PasswordField();
        registerController.lblMsg = new Label();
        registerController.radbAdm = new RadioButton();
        registerController.ckUser = new CheckBox();
    }

    /**
     * This test checks if the user registration is successful.
     */
    @Test
    public void testRegisterUser_SuccessfulRegistration() throws Exception {
        // Set up user input
        registerController.txtFName.setText("John");
        registerController.txtLName.setText("Doe");
        registerController.txtUsername.setText("johndoe");
        registerController.txtEmail.setText("john.doe@example.com");
        registerController.ptxtPwd.setText("password123");
        registerController.ptxtRetp.setText("password123");
        registerController.ckUser.setSelected(true);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        registerController.registerUser();

        assertEquals("Successfully registered.", registerController.lblMsg.getText());
    }

    /**
     * This test checks if the user registration fails due to a database issue.
     */
    @Test
    public void testRegisterUser_FailedRegistration_DatabaseIssue() throws Exception {
        registerController.txtFName.setText("John");
        registerController.txtLName.setText("Doe");
        registerController.txtUsername.setText("johndoe");
        registerController.txtEmail.setText("john.doe@example.com");
        registerController.ptxtPwd.setText("password123");
        registerController.ptxtRetp.setText("password123");
        registerController.ckUser.setSelected(true);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        registerController.registerUser();

        assertEquals("Username already exists. Please choose a different one.", registerController.lblMsg.getText());
    }

    /**
     * This test checks if the user registration fails due to blank inputs.
     */
    @Test
    public void testRegisterUser_ValidationFailure_BlankInputs() {
        registerController.registerUser();

        assertEquals("Please fill all the information above.", registerController.lblMsg.getText());
    }

    /**
     * This test checks if the user registration fails due to an invalid email format.
     */
    @Test
    public void testRegisterUser_ValidationFailure_EmailFormat() {
        registerController.txtFName.setText("John");
        registerController.txtLName.setText("Doe");
        registerController.txtUsername.setText("johndoe");
        registerController.txtEmail.setText("john.doe");
        registerController.ptxtPwd.setText("password123");
        registerController.ptxtRetp.setText("password123");
        registerController.ckUser.setSelected(true);

        assertFalse(registerController.validateInputs());
        assertEquals("Invalid email format.", registerController.lblMsg.getText());

    }
    /**
     * This method is run after all tests.
     * It cleans up the database after the tests are run.
     */
    @AfterAll
    public static void tearDown() throws Exception {
        // Clean up the database
        String deleteUserQuery = "DELETE FROM useraccount WHERE userName = ? OR emailAddress = ?";
        try (PreparedStatement deleteStmt = realTestDbConnection.prepareStatement(deleteUserQuery)) {
            deleteStmt.setString(1, "johndoe");
            deleteStmt.setString(2, "john.doe@example.com");
            deleteStmt.executeUpdate();
        } finally {
            realTestDbConnection.close();
        }
    }

}