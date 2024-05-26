package com.cab302.wellbeing.controller;

import com.cab302.wellbeing.model.AppSettings;
import com.cab302.wellbeing.model.DataBaseConnection;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for controlling the report functionality in the application.
 * It provides functionalities such as displaying the daily usage and website usage reports.
 */
public class ReportController {
    /**
     * Label for Report
     */
    @FXML
    public Label lblReportMsg;  // Label for the report message
    /**
     * Button for History
     */
    @FXML
    public Button btnHist, btnDnld, btnCancel; // Buttons for history, download, and cancel
    /**
     * Pane for Report
     */
    @FXML
    public Pane paneReport; // Pane for the report
    /**
     * Label for Background
     */
    @FXML
    public Label lblBkGrd; // Label for the background
    /**
     * Line Chart for Daily Usage
     */
    @FXML
    public LineChart<String, Integer> lcDailyUsage; // Line chart for daily usage
    /**
     * Bar Chart for Website Usage
     */
    @FXML
    public BarChart<String, Integer> bcWebsiteUsage; // Bar chart for website usage
    int userId; // ID of the current user
    String firstName; // First name of the user
    private DataBaseConnection dbConnection = new DataBaseConnection(); // Database connection

    /**
     * Parse the user ID.
     *
     * @param userId The ID of the user
     */
    public void setUserId(int userId) {
        this.userId = userId;  // Use this userId to store browsing data linked to the user
    }

    /**
     * Parse the user first name.
     *
     * @param firstName The first name of the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * This method is used to handle the history button click event.
     * It opens the browsing history window.
     */
    public void btnHistoryOnAction() {
        Color backgroundColor = (Color) paneReport.getBackground().getFills().get(0).getFill(); // Get the background color
        Color textColor = (Color) lblBkGrd.getTextFill(); // Get the text color
        Color buttonColor = (Color) btnHist.getBackground().getFills().get(0).getFill(); // Get the button color
        // Load the browsing history window
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/BrowsingHistory.fxml"));
            Parent root1 = fxmlLoader.load();
            BrowsingHistoryController browsingHistoryController = fxmlLoader.getController();
            Stage stage = new Stage();
            browsingHistoryController.applyModeColors();
            browsingHistoryController.applyColors(backgroundColor, textColor, buttonColor);
            browsingHistoryController.setUserId(userId);  // Pass the userId to the new controller
            browsingHistoryController.setFirstName(firstName);  // Pass the firstName to the new controller
            stage.setTitle("History");
            stage.setScene(new Scene(root1));
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading BrowsingHistory.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method is used to handle the download button click event.
     * It saves the reports as PNG images.
     */
    public void btnCancelOnAction() {
        Stage stage = (Stage) lblReportMsg.getScene().getWindow();
        stage.close();  // Closes the current window
    }

    /**
     * This method is used to handle the download button click event.
     * It saves the reports as PNG images.
     */
    public void btnSaveOnAction() {
        saveAsPng(this.lcDailyUsage, "src/main/resources/com/cab302/wellbeing/DownloadedReport/Report1.png");
        saveAsPng(this.bcWebsiteUsage, "src/main/resources/com/cab302/wellbeing/DownloadedReport/Report2.png");
    }

    /**
     * This method is used to save the line chart as a PNG image.
     *
     * @param lineChart The line chart to save
     * @param path      The path to save the image
     */
    public void saveAsPng(LineChart<String, Integer> lineChart, String path) {
        WritableImage wi = lineChart.snapshot(new SnapshotParameters(), new WritableImage(800, 600)); // Modify the size as needed
        File file = new File(path); // Modify the path as needed
        // Save the image
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wi, null), "png", file); // Save the image as a PNG file
            System.out.println("Image saved"); // Print a message
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to save the bar chart as a PNG image.
     *
     * @param barChart The bar chart to save
     * @param path     The path to save the image
     */
    public void saveAsPng(BarChart<String, Integer> barChart, String path) {
        WritableImage wi = barChart.snapshot(new SnapshotParameters(), new WritableImage(800, 600)); // Modify the size as needed
        File file = new File(path); // Modify the path as needed
        // Save the image
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wi, null), "png", file); // Save the image as a PNG file
            System.out.println("Image saved"); // Print a message
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to display the line chart.
     */
    public void displayLineChart() {
        this.initLineChart(); // Initialize the line chart
        List<LineChartModel> dataset = this.getLCDataset(); // Get the line chart dataset
        XYChart.Series<String, Integer> series = new XYChart.Series<>(); // Create a new series
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM"); // Create a date formatter
        // Add the data to the series
        for (LineChartModel lcModel : dataset) {
            series.getData().add(new XYChart.Data<>(lcModel.sessionDate.format(formatter), lcModel.durationSum)); // Add the data
        }
        this.lcDailyUsage.getData().add(series); // Add the series to the line chart
    }

    /**
     * This method is used to display the bar chart.
     */
    public void displayBarChart() {
        this.initBarChart(); // Initialize the bar chart
        List<BarChartModel> dataset = this.getTop5BCDataset(); // Get the bar chart dataset
        XYChart.Series<String, Integer> series = new XYChart.Series<>(); // Create a new series
        // Add the data to the series
        for (BarChartModel bcModel : dataset) {
            series.getData().add(new XYChart.Data<>(getHostName(bcModel.url), bcModel.durationSum)); // Add the data
        }
        this.bcWebsiteUsage.getData().clear(); // Clear previous data
        this.bcWebsiteUsage.getData().add(series);
    }

    /**
     * This method is used to get the host name from the URL.
     *
     * @param url The URL to get the host name from
     * @return The host name
     */
    public String getHostName(String url) {
        URI uri; // Create a new URI
        // Try to create a new URI
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String hostname = uri.getHost(); // Get the host name from the URI
        // to provide faultproof result, check if not null then return only hostname, without www.
        if (hostname != null) {
            return hostname.startsWith("www.") ? hostname.substring(4) : hostname;
        } else {
            hostname = url;
        }
        return hostname;
    }

    /**
     * This method is used to initialize the line chart.
     */
    public void initLineChart() {
        this.lcDailyUsage.setTitle("Daily Usage"); // Set the title
        this.lcDailyUsage.setPrefSize(620, 600); // Modify the size as needed
        this.lcDailyUsage.getStylesheets().add(getClass().getResource("/com/cab302/wellbeing/styles.css").toExternalForm()); // Add this line
    }

    /**
     * This method is used to initialize the bar chart.
     */
    public void initBarChart() {
        this.bcWebsiteUsage.setTitle("Usage of Websites");
        this.bcWebsiteUsage.setPrefSize(620, 600); // Modify the size as needed
        this.bcWebsiteUsage.getStylesheets().add(getClass().getResource("/com/cab302/wellbeing/styles.css").toExternalForm()); // Add this line
    }

    /**
     * This method is used to get the line chart dataset.
     *
     * @return The line chart dataset
     */
    public List<LineChartModel> getLCDataset() {
        String selectQuery = "SELECT SUM(Duration) as DurationSum, SessionDate FROM WellBeing.BrowsingData WHERE UserID = ?  GROUP BY SessionDate ORDER BY SessionDate"; // Query to get the line chart dataset
        List<LineChartModel> res = new ArrayList<>(); // Create a new list
        // Try to get the line chart dataset
        try (Connection conn = dbConnection.getConnection(); // Get a fresh connection
             PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
            pstmt.setInt(1, this.userId);
            ResultSet rs = pstmt.executeQuery();

            // Fetch each row from the result set
            while (rs.next()) {
                int durationSum = rs.getInt("DurationSum");
                LocalDate sessionDate = rs.getDate("SessionDate").toLocalDate();

                LineChartModel lineChartModel = new LineChartModel(sessionDate, durationSum);
                res.add(lineChartModel);
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

    /**
     * This method is used to get the bar chart dataset.
     *
     * @return The bar chart dataset
     */
    public List<BarChartModel> getBCDataset() {
        String selectQuery = "SELECT SUM(Duration) as DurationSum, URL FROM WellBeing.BrowsingData WHERE UserID = ?  GROUP BY URL"; // Query to get the bar chart dataset
        List<BarChartModel> res = new ArrayList<>(); // Create a new list
        // Try to get the bar chart dataset
        try (Connection conn = dbConnection.getConnection(); // Get a fresh connection
             PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
            pstmt.setInt(1, this.userId);
            ResultSet rs = pstmt.executeQuery();

            // Fetch each row from the result set
            while (rs.next()) {
                int durationSum = rs.getInt("DurationSum");
                String url = rs.getString("URL");

                BarChartModel barChartModel = new BarChartModel(url, durationSum);
                res.add(barChartModel);
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return sortDataset(res);
    }

    /**
     * This method is used to sort the dataset.
     *
     * @param dataset The dataset to sort
     * @return The sorted dataset
     */
    public List<BarChartModel> sortDataset(List<BarChartModel> dataset) {
        int n = dataset.size(); // Get the size of the dataset
        // Bubble sort
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (dataset.get(j).durationSum < dataset.get(j + 1).durationSum) {
                    // Swap
                    BarChartModel temp = dataset.get(j);
                    dataset.set(j, dataset.get(j + 1));
                    dataset.set(j + 1, temp);
                }
            }
        }
        return dataset;
    }

    /**
     * This method is used to get the top 5 of the dataset.
     *
     * @param dataset The dataset to get the top 5 from
     * @return The top 5 of the dataset
     */
    public List<BarChartModel> getTop5(List<BarChartModel> dataset) {
        return dataset.stream() // Get the stream
                .sorted((a, b) -> Integer.compare(b.durationSum, a.durationSum)) // Sort the dataset
                .limit(5) // Limit to the top 5
                .collect(Collectors.toList()); // Collect the result
    }

    /**
     * This method is used to get the top 5 of the bar chart dataset.
     *
     * @return The top 5 of the bar chart dataset
     */
    public List<BarChartModel> getTop5BCDataset() {
        List<BarChartModel> sortedDataset = getBCDataset(); // Get the sorted dataset
        return getTop5(sortedDataset); // Get the top 5 of the sorted dataset
    }

    /**
     * This method is used to apply the color theme based on the current mode.
     *
     * @param backgroundColor The background color
     * @param textColor       The text color
     * @param buttonColor     The button color
     */
    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor); // Get the background color
        String textHex = getHexColor(textColor); // Get the text color
        String buttonHex = getHexColor(buttonColor); // Get the button color

        if (paneReport != null) {
            paneReport.setStyle("-fx-background-color: " + backgroundHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnHist != null) {
            btnHist.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnDnld != null) {
            btnDnld.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (btnCancel != null) {
            btnCancel.setStyle("-fx-background-color: " + buttonHex + "; -fx-text-fill: " + textHex + ";");
        }
        if (lblReportMsg != null) {
            lblReportMsg.setStyle(" -fx-text-fill: " + textHex + ";");
        }
    }

    /**
     * This method is used to convert a Color object to a hex color string.
     *
     * @param color The Color object
     * @return The hex color string
     */
    private String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * This method is used to apply the color theme based on the current mode.
     */
    public void applyModeColors() {
        if (lblBkGrd == null) {
            System.out.println("lblBkGrd is null!");
            return;
        }

        String currentMode = AppSettings.getCurrentMode();
        double opacity = AppSettings.MODE_AUTO.equals(currentMode) ? 0.0 : 0.5; // 0% for auto, 70% for others

        updateLabelBackgroundColor(opacity);
    }

    /**
     * This method is used to update the background color of the label.
     *
     * @param opacity The opacity of the background color
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
     * This method is used to convert a Color object to an RGBA color string.
     *
     * @param color The Color object to convert
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

