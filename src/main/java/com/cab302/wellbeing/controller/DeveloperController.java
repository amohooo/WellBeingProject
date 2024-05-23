package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
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

public class DeveloperController {

    @FXML
    private TableView<UserFeedback> table;

    @FXML
    private TableColumn<UserFeedback, String> colEmail;

    @FXML
    private TableColumn<UserFeedback, String> colFeedback;
    @FXML
    private Pane paneDeveloper;
    @FXML
    private Label lblDev;
    @FXML
    private TextArea txtDisplay;
    @FXML
    private TextField txtMsg;
    @FXML
    private Button btnSend, btnConnect, btnClose;

    private Socket socket = null;
    private DataOutputStream streamOut = null;
    private DataOutputStream streamOut1 = null;
    private DataOutputStream streamOut2 = null;
    private DataOutputStream streamOut3 = null;
    private DataOutputStream streamOut4 = null;
    private ChatClientThread1 client = null;
    private ChatServer chatServer = null;
    private String serverName = "localhost"; // Use localhost as the server is running on the same machine
    private int serverPort = 4445;

    private DataBaseConnection dbConnection = new DataBaseConnection();
    @FXML
    private void initialize() {
        btnSend.setOnAction(this::sendMessage);
        btnConnect.setOnAction(this::connectToServer);
        btnClose.setOnAction(this::closeConnection);
        txtMsg.setOnKeyPressed(event -> handleKeyPress(event));
    }
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            send();
            txtDisplay.requestFocus();
        }
    }
    public void displayTable(){
        List<UserFeedback> userFeedbacks = this.getFeedbacks();
        ObservableList<UserFeedback> tableData = FXCollections.observableArrayList(userFeedbacks);
        this.colEmail.setCellValueFactory(new PropertyValueFactory<>("ColEmail"));
        this.colFeedback.setCellValueFactory(new PropertyValueFactory<>("ColFeedback"));
        this.table.setItems(tableData);
    }
    public List<UserFeedback> getFeedbacks(){
        String selectQuery = "SELECT Email, Message FROM ContactUs";
        List<UserFeedback> res = new ArrayList<UserFeedback>();
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
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

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

    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
    public void applyModeColors() {
        if (lblDev == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode();
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others

        updateLabelBackgroundColor(opacity);
    }
    @FXML
    private void sendMessage(ActionEvent e) {
        send();
        txtDisplay.requestFocus();
    }

    @FXML
    private void connectToServer(ActionEvent e) {
        startServer();
        connect(serverName, serverPort);
    }

    private void startServer() {
        if (chatServer == null) {
            chatServer = new ChatServer(serverPort);
            new Thread(chatServer).start();
            println("Server started on port: " + serverPort);
        }
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
            streamOut.writeUTF(txtMsg.getText());
            streamOut.flush();
            txtMsg.setText("");
        } catch (IOException ex) {
            println("Sending error: " + ex.getMessage());
            connClose();
        }
    }

    public void handle(String msg) {
        println(msg);
    }

    private void open() {
        try {
            streamOut = new DataOutputStream(socket.getOutputStream());
            client = new ChatClientThread1(this, socket);
        } catch (IOException ex) {
            println("Error opening output stream: " + ex);
        }
    }

    @FXML
    private void closeConnection(ActionEvent e) {
        connClose();
        Stage stage = (Stage) btnClose.getScene().getWindow();
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

        if (client != null) {
            client.connClose();
        }

        if (chatServer != null) {
            chatServer.stop();
            chatServer = null;
        }
    }

    void println(String msg) {
        txtDisplay.appendText(msg + "\n");
    }
    @FXML
    private void closeDeveloper(ActionEvent e) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
    public void updateLabelBackgroundColor(double opacity) {
        if (lblDev == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }
        Color backgroundColor = AppSettings.getCurrentModeColorWithOpacity(opacity);
        lblDev.setStyle("-fx-background-color: " + toRgbaColor(backgroundColor) + ";");
    }

    private String toRgbaColor(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }
}