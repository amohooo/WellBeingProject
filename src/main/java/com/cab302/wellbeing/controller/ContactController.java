package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.ChatClientThread2;
import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is a controller for managing the contact information in the application.
 * Currently, it is an empty class and does not contain any methods or fields.
 * Future implementations will include methods for sending messages through "contact us".
 */
public class ContactController {
    @FXML
    private Button btnBack, btnSend, btnConnect, btnChat;  // Buttons for various actions
    @FXML
    private Label lblBkGrd, lblMsg, lblEmail;  // Labels for displaying information
    @FXML
    private Pane paneContact;  // Pane for the contact form
    @FXML
    private TextField txtEmail, txtChat;  // Text fields for email and chat
    @FXML
    private TextArea txtMessage, txtDisplay;  // Text areas for message and display
    @FXML
    private Stage stage;  // The current stage
    private int userId;  // The user ID
    private Socket socket = null;  // The socket for the connection
    private DataOutputStream streamOut = null;  // The output stream for the connection
    private ChatClientThread2 client2 = null;  // The client thread for handling messages
    private String serverName = "localhost";  // The server name
    private int serverPort = 4445;  // The server port
    private DataBaseConnection dbConnection = new DataBaseConnection();  // The database connection

    /**
     * Initializes the controller.
     * Sets up the event handlers for the buttons and the key press event for the chat text field.
     */
    @FXML
    private void initialize() {
        btnChat.setOnAction(this::sendMessage); // Set the event handler for the chat button
        btnConnect.setOnAction(this::connectToServer); // Set the event handler for the connect button
        btnBack.setOnAction(this::closeConnection); // Set the event handler for the back button
        txtChat.setOnKeyPressed(event -> handleKeyPress(event)); // Set the event handler for the key press event
    }

    /**
     * Handles the key press event for the chat text field.
     *
     * @param event The key event
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) { // Check if the Enter key is pressed
            send(); // Send the message
            txtDisplay.requestFocus(); // Move the focus to the display text area
        }
    }

    /**
     * Sends a message through the chat.
     *
     * @param e The action event
     */
    @FXML
    private void sendMessage(ActionEvent e) {
        send(); // Send the message
        txtDisplay.requestFocus(); // Move the focus to the display text area
    }

    /**
     * Connects to the chat server.
     *
     * @param e The action event
     */
    @FXML
    private void connectToServer(ActionEvent e) {
        connect(serverName, serverPort);
    }

    /**
     * Establishes a connection to the chat server.
     *
     * @param serverName The server name
     * @param serverPort The server port
     */
    private void connect(String serverName, int serverPort) {
        println("Establishing connection. Please wait ..."); // Print a message to the display text area
        // Attempt to connect to the server
        try {
            socket = new Socket(serverName, serverPort);
            println("Connected: " + socket);
            open();
        } catch (UnknownHostException ex) {
            println("Host unknown: " + ex.getMessage());
        } catch (IOException ex) {
            println("Unexpected exception: " + ex.getMessage());
        }
    }

    /**
     * Sends a message through the chat.
     */
    private void send() {
        // Check if the output stream is not null
        try {
            if (this.streamOut == null) {
                lblMsg.setText("The server is currently closed.");
            } else{
                streamOut.writeUTF(txtChat.getText());
                streamOut.flush();
                txtChat.setText("");
            }
        } catch (IOException ex) {
            println("Sending error: " + ex.getMessage());
            connClose();
        }
    }

    /**
     * Handles a message received from the chat server.
     *
     * @param msg The message
     */
    public void handle(String msg) {
        // Print the message to the display text area
        Platform.runLater(() -> {
            println(msg);
            lblMsg.setText("Team: " + msg);
        });
    }

    /**
     * Opens the output stream for the connection.
     */
    private void open() {
        // Attempt to open the output stream
        try {
            streamOut = new DataOutputStream(socket.getOutputStream());
            client2 = new ChatClientThread2(this, socket);
        } catch (IOException ex) {
            println("Error opening output stream: " + ex);
        }
    }

    /**
     * Closes the connection to the chat server.
     *
     * @param e The action event
     */
    @FXML
    private void closeConnection(ActionEvent e) {
        connClose(); // Close the connection
        Stage stage = (Stage) btnBack.getScene().getWindow(); // Get the current stage
        stage.close(); // Close the stage
    }

    /**
     * Closes the connection to the chat server.
     */
    public void connClose() {
        // Attempt to close the connection
        try {
            if (streamOut != null) {
                streamOut.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
            println("Error closing: " + ex.getMessage());
        }

        if (client2 != null) {
            client2.close();
        }
    }

    /**
     * Prints a message to the display text area.
     *
     * @param msg The message
     */
    void println(String msg) {
        txtDisplay.appendText(msg + "\n");
    }

    /**
     * Sets the user ID.
     *
     * @param userId The user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;  // Now you can use this userId to store browsing data linked to the user
    }

    /**
     * Sends a message to the contact form.
     *
     * @param e The action event
     */
    @FXML
    public void btnSendOnAction(ActionEvent e){
        this.saveMessage();
    }

    /**
     * Closes the contact form.
     *
     * @param e The action event
     */
    @FXML
    public void btnCancelOnAction(ActionEvent e) {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }

    /**
     * Saves a message to the database.
     */
    public void saveMessage(){
        // Check if the inputs are valid
        if(!validateInputs())return;
        String insertQuery = "INSERT INTO ContactUs (UserID, Email, Message) VALUES(?, ?, ?)"; // The query to insert the message
        try (Connection conn = dbConnection.getConnection(); // Get a fresh connection
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setInt(1, this.userId);
            pstmt.setString(2, this.txtEmail.getText());
            pstmt.setString(3, this.txtMessage.getText());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates the inputs for the contact form.
     *
     * @return True if the inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        // Check if the message and email fields are empty
        if (txtMessage.getText().isEmpty() || txtEmail.getText().isEmpty() ) {
            lblMsg.setText("Please fill all the information above.");
            return false;
        }
        // Check if the email is in a valid format
        if (!txtEmail.getText().contains("@")) {
            lblMsg.setText("Invalid email format.");
            return false;
        }
        // Check if the email already exists
        if (emailExists(txtEmail.getText())) {
            lblMsg.setText("Email address already exists. Please use a different one.");
            return false;
        }
        // Check if the message already exists
        if(isSameMessage()){
            lblMsg.setText("The message already exist.");
            return false;
        }

        return true;
    }

    /**
     * Checks if an email address already exists in the database.
     *
     * @param email The email address
     * @return True if the email address exists, false otherwise
     */
    private boolean emailExists(String email) {
        return exists("emailAddress", email);
    }

    /**
     * Checks if a value already exists in the database.
     *
     * @param columnName The column name
     * @param value The value
     * @return True if the value exists, false otherwise
     */
    private boolean exists(String columnName, String value) {
        String query = "SELECT COUNT(*) FROM useraccount WHERE " + columnName + " = ? and userId != " + userId; // The query to check if the value exists
        // Attempt to execute the query
        try (Connection connectDB = new DataBaseConnection().getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(query)) {
            preparedStatement.setString(1, value);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblMsg.setText("Failed to validate " + columnName + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if a message already exists in the database.
     *
     * @return True if the message exists, false otherwise
     */
    private boolean isSameMessage(){
        String query = "SELECT COUNT(*) FROM ContactUs WHERE  Email = ? and Message = ? and userId = ? "; // The query to check if the message exists
        // Attempt to execute the query
        try (Connection connectDB = new DataBaseConnection().getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(query)) {
            preparedStatement.setString(1, txtEmail.getText());
            preparedStatement.setString(2, txtMessage.getText());
            preparedStatement.setInt(3, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblMsg.setText( e.getMessage());
        }
        return false;
    }

    /**
     * Applies colors to the UI elements.
     *
     * @param backgroundColor The background color
     * @param textColor The text color
     * @param buttonColor The button color
     */
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

        if (btnBack != null) {
            btnBack.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnSend != null) {
            btnSend.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnConnect != null) {
            btnConnect.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnChat != null) {
            btnChat.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (lblMsg != null) {
            lblMsg.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (lblEmail != null) {
            lblEmail.setStyle("-fx-text-fill: " + textHex + ";");
        }
        if (paneContact != null) {
            paneContact.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
    }

    /**
     * Converts a Color object to a hex color string.
     *
     * @param color The Color object
     * @return The hex color string
     */
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * Applies the color theme to the UI.
     */
    public void applyModeColors() {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode(); // Get the current mode
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others

        updateLabelBackgroundColor(opacity);
    }

    /**
     * Updates the background color of the label.
     *
     * @param opacity The opacity
     */
    public void updateLabelBackgroundColor(double opacity) {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity);
        lblBkGrd.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";");
    }

    /**
     * Converts a Color object to an RGBA color string.
     *
     * @param color The Color object
     * @return The RGBA color string
     */
    private String toRgbaColor(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }
}
