package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.util.ArrayList;
import java.util.List;


/**
 * This class is a controller for managing the developer's interactions in the application.
 * It provides functionalities for displaying user feedback, sending messages, and handling received messages.
 */
public class DeveloperController {

    @FXML
    private TableView<UserFeedback> table; // Table for displaying user feedback
    @FXML
    private TableColumn<UserFeedback, String> colEmail; // Column for the email
    @FXML
    private TableColumn<UserFeedback, String> colFeedback; // Column for the feedback
    @FXML
    private Pane paneDeveloper; // Pane for the developer
    @FXML
    private Label lblDev; // Label for the developer
    @FXML
    private TextArea txtDisplay; // Text area for displaying messages
    @FXML
    private TextField txtMsg; // Text field for entering messages
    @FXML
    private Button btnSend, btnConnect, btnClose; // Buttons for sending, connecting, and closing

    private Socket socket = null; // Socket for the connection
    private DataOutputStream streamOut = null; // Output stream for sending messages
    private ChatClientThread1 client = null; // Chat client for the connection
    private ChatServer chatServer = null; // Chat server for the connection
    private String serverName = "localhost"; // Use localhost as the server is running on the same machine
    private int serverPort = 4445; // Port number for the server
    private DataBaseConnection dbConnection = new DataBaseConnection(); // Database connection

    /**
     * Initializes the controller.
     * Sets up the event handlers for the buttons and text field.
     */
    @FXML
    private void initialize() {
        btnSend.setOnAction(this::sendMessage); // Set the event handler for the send button
        btnConnect.setOnAction(this::connectToServer); // Set the event handler for the connect button
        btnClose.setOnAction(this::closeConnection); // Set the event handler for the close button
        txtMsg.setOnKeyPressed(event -> handleKeyPress(event)); // Set the event handler for the text field
    }

    /**
     * Handles the key press event for the text field.
     * If the Enter key is pressed, sends the message.
     *
     * @param event The key event
     */
    private void handleKeyPress(KeyEvent event) {
        // If the Enter key is pressed, send the message
        if (event.getCode() == KeyCode.ENTER) {
            send(); // Send the message
            txtDisplay.requestFocus(); // Set the focus to the text area
        }
    }

    /**
     * Displays the user feedback in the table.
     */
    public void displayTable(){
        List<UserFeedback> userFeedbacks = this.getFeedbacks(); // Get the user feedbacks
        ObservableList<UserFeedback> tableData = FXCollections.observableArrayList(userFeedbacks); // Create an observable list
        this.colEmail.setCellValueFactory(new PropertyValueFactory<>("ColEmail")); // Set the cell value factory for the email column
        this.colFeedback.setCellValueFactory(new PropertyValueFactory<>("ColFeedback")); // Set the cell value factory for the feedback column
        this.table.setItems(tableData); // Set the table data
    }

    /**
     * Gets the user feedback from the database.
     *
     * @return The list of user feedback
     */
    public List<UserFeedback> getFeedbacks(){
        String selectQuery = "SELECT Email, Message FROM ContactUs"; // SQL query to select the email and message from the ContactUs table
        List<UserFeedback> res = new ArrayList<UserFeedback>(); // List to store the user feedback
        try (Connection conn = dbConnection.getConnection(); // Get a fresh connection
             PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
            ResultSet rs = pstmt.executeQuery();

            // Fetch each row from the result set
            while (rs.next()) {
                String email = rs.getString("Email");
                String feedback = rs.getString("Message");

                UserFeedback userFeedback = new UserFeedback(email, feedback);
                res.add(userFeedback);
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Applies the specified colors to the developer pane.
     *
     * @param backgroundColor The background color
     * @param textColor The text color
     * @param buttonColor The button color
     */
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor); // Get the hexadecimal color for the background
        String textHex = getHexColor(textColor); // Get the hexadecimal color for the text
        String buttonHex = getHexColor(buttonColor); // Get the hexadecimal color for the button

        if (paneDeveloper != null) {
            paneDeveloper.setStyle("-fx-background-color: " + backgroundHex + ";");
        }
        if (btnSend != null) {
            btnSend.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnConnect != null) {
            btnConnect.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnClose != null) {
            btnClose.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
    }

    /**
     * Gets the hexadecimal color representation of the specified color.
     *
     * @param color The color
     * @return The hexadecimal color representation
     */
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * Applies the color theme to the UI.
     */
    public void applyModeColors() {
        if (lblDev == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode(); // Get the current mode
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others

        updateLabelBackgroundColor(opacity); // Update the label background color
    }

    /**
     * Sends a message to the server.
     * @param e The action event
     */
    @FXML
    private void sendMessage(ActionEvent e) {
        send(); // Send the message
        txtDisplay.requestFocus(); // Set the focus to the text area
    }

    /**
     * Connects to the server.
     * @param e The action event
     */
    @FXML
    private void connectToServer(ActionEvent e) {
        startServer();
        connect(serverName, serverPort);
    }

    /**
     * Starts the chat server.
     */
    private void startServer() {
        // Start the chat server if it is not running
        if (chatServer == null) {
            chatServer = new ChatServer(serverPort); // Create a new chat server
            new Thread(chatServer).start(); // Start the chat server in a new thread
            println("Server started on port: " + serverPort); // Print the message
        }
    }

    /**
     * Connects to the specified server.
     * @param serverName The server name
     * @param serverPort The server port
     */
    private void connect(String serverName, int serverPort) {
        println("Establishing connection. Please wait ..."); // Print the message
        // Try to connect to the server
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
     * Sends a message to the server.
     */
    private void send() {
        // Send the message
        try {
            streamOut.writeUTF(txtMsg.getText());
            streamOut.flush();
            txtMsg.setText("");
        } catch (IOException ex) {
            println("Sending error: " + ex.getMessage());
            connClose();
        }
    }

    /**
     * Handles the received message.
     * @param msg The message
     */
    public void handle(String msg) {
        println(msg);
    }

    /**
     * Opens the output stream.
     */
    private void open() {
        // Open the output stream
        try {
            streamOut = new DataOutputStream(socket.getOutputStream());
            client = new ChatClientThread1(this, socket);
        } catch (IOException ex) {
            println("Error opening output stream: " + ex);
        }
    }

    /**
     * Closes the connection.
     * @param e The action event
     */
    @FXML
    private void closeConnection(ActionEvent e) {
        connClose(); // Close the connection
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    /**
     * Closes the connection.
     */
    public void connClose() {
        // Close the connection
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
        // Stop the chat server
        if (client != null) {
            client.connClose();
        }
        //  Stop the chat server
        if (chatServer != null) {
            chatServer.stop();
            chatServer = null;
        }
    }

    /**
     * Prints the message to the text area.
     * @param msg The message
     */
    void println(String msg) {
        txtDisplay.appendText(msg + "\n"); // Append the message to the text area
    }

    /**
     * Closes the developer window.
     * @param e The action event
     */
    @FXML
    private void closeDeveloper(ActionEvent e) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    /**
     * Updates the label background color with the specified opacity.
     * @param opacity The opacity
     */
    public void updateLabelBackgroundColor(double opacity) {
        if (lblDev == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity);
        lblDev.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";");
    }

    /**
     * Converts a Color object to an RGBA color string.
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