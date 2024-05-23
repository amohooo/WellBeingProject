package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.AppSettings;
import com.cab302.wellbeing.ChatClientThread2;
import com.cab302.wellbeing.DataBaseConnection;
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

    /**
     * This method is used to handle the Cancel button click event.
     * It closes the current window.
     */

    @FXML
    private Button btnBack, btnSend, btnConnect, btnChat;
    @FXML
    private Label lblBkGrd, lblMsg, lblEmail;
    @FXML
    private Pane paneContact;
    @FXML
    private TextField txtEmail, txtChat;
    @FXML
    private TextArea txtMessage, txtDisplay;
    @FXML
    private Stage stage;
    int userId;
    private Socket socket = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread2 client2 = null;
    private String serverName = "localhost";
    private int serverPort = 4445;
    private DataBaseConnection dbConnection = new DataBaseConnection();
    @FXML
    private void initialize() {
        btnChat.setOnAction(this::sendMessage);
        btnConnect.setOnAction(this::connectToServer);
        btnBack.setOnAction(this::closeConnection);
        txtChat.setOnKeyPressed(event -> handleKeyPress(event));
    }
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            send();
            txtDisplay.requestFocus();
        }
    }
    @FXML
    private void sendMessage(ActionEvent e) {
        send();
        txtDisplay.requestFocus();
    }
    @FXML
    private void connectToServer(ActionEvent e) {
        connect(serverName, serverPort);
    }

    private void connect(String serverName, int serverPort) {
        println("Establishing connection. Please wait ...");
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

    private void send() {
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

    public void handle(String msg) {
        Platform.runLater(() -> {
            println(msg);
            lblMsg.setText("Team: " + msg);
        });
    }

    private void open() {
        try {
            streamOut = new DataOutputStream(socket.getOutputStream());
            client2 = new ChatClientThread2(this, socket);
        } catch (IOException ex) {
            println("Error opening output stream: " + ex);
        }
    }

    @FXML
    private void closeConnection(ActionEvent e) {
        connClose();
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }

    public void connClose() {
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

    void println(String msg) {
        txtDisplay.appendText(msg + "\n");
    }

    public void setUserId(int userId) {
        this.userId = userId;  // Now you can use this userId to store browsing data linked to the user
    }

    @FXML
    public void btnSendOnAction(ActionEvent e){
        this.saveMessage();
    }
    @FXML
    public void btnCancelOnAction(ActionEvent e) {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }
    public void saveMessage(){
        if(!validateInputs())return;
        String insertQuery = "INSERT INTO ContactUs (UserID, Email, Message) VALUES(?, ?, ?)";
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

    private boolean validateInputs() {
        if (txtMessage.getText().isEmpty() || txtEmail.getText().isEmpty() ) {
            lblMsg.setText("Please fill all the information above.");
            return false;
        }

        if (!txtEmail.getText().contains("@")) {
            lblMsg.setText("Invalid email format.");
            return false;
        }

        if (emailExists(txtEmail.getText())) {
            lblMsg.setText("Email address already exists. Please use a different one.");
            return false;
        }

        if(isSameMessage()){
            lblMsg.setText("The message already exist.");
            return false;
        }

        return true;
    }

    private boolean emailExists(String email) {
        return exists("emailAddress", email);
    }

    private boolean exists(String columnName, String value) {
        String query = "SELECT COUNT(*) FROM useraccount WHERE " + columnName + " = ? and userId != " + userId;
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

    private boolean isSameMessage(){
        String query = "SELECT COUNT(*) FROM ContactUs WHERE  Email = ? and Message = ? and userId = ? ";
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

    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    public void applyModeColors() {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode();
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others

        updateLabelBackgroundColor(opacity);
    }
    public void updateLabelBackgroundColor(double opacity) {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity);
        lblBkGrd.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";");
    }

    private String toRgbaColor(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }
}
