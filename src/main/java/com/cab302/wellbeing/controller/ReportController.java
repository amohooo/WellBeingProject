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


public class ReportController {

    @FXML
    public Label lblReportMsg;
    @FXML
    public Button btnHist, btnDnld, btnCancel;
    @FXML
    public Pane paneReport;
    public Label lblBkGrd;

    @FXML
    public LineChart<String, Integer> lcDailyUsage;

    @FXML
    public BarChart<String, Integer> bcWebsiteUsage;

    int userId;
    String firstName;

    private DataBaseConnection dbConnection = new DataBaseConnection();

    public void setUserId(int userId) {
        this.userId = userId;  // Now you can use this userId to store browsing data linked to the user
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void btnHistoryOnAction() {
        Color backgroundColor = (Color) paneReport.getBackground().getFills().get(0).getFill();
        Color textColor = (Color) lblBkGrd.getTextFill();
        Color buttonColor = (Color) btnHist.getBackground().getFills().get(0).getFill();
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

    public void btnCancelOnAction() {
        Stage stage = (Stage) lblReportMsg.getScene().getWindow();
        stage.close();  // Closes the current window
    }

    public void btnSaveOnAction() {
        saveAsPng(this.lcDailyUsage, "src/main/resources/com/cab302/wellbeing/DownloadedReport/Report1.png");
        saveAsPng(this.bcWebsiteUsage, "src/main/resources/com/cab302/wellbeing/DownloadedReport/Report2.png");
    }

    public void saveAsPng(LineChart<String, Integer> lineChart, String path) {
        WritableImage wi = lineChart.snapshot(new SnapshotParameters(), new WritableImage(800, 600));
        File file = new File(path);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wi, null), "png", file);
            System.out.println("Image saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAsPng(BarChart<String, Integer> barChart, String path) {
        WritableImage wi = barChart.snapshot(new SnapshotParameters(), new WritableImage(800, 600));
        File file = new File(path);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wi, null), "png", file);
            System.out.println("Image saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayLineChart() {
        this.initLineChart();
        List<LineChartModel> dataset = this.getLCDataset();
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (LineChartModel lcModel : dataset) {
            series.getData().add(new XYChart.Data<>(lcModel.sessionDate.format(formatter), lcModel.durationSum));
        }
        this.lcDailyUsage.getData().add(series);
    }

    public void displayBarChart() {
        this.initBarChart();
        List<BarChartModel> dataset = this.getTop5BCDataset();
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        for (BarChartModel bcModel : dataset) {
            series.getData().add(new XYChart.Data<>(getHostName(bcModel.url), bcModel.durationSum));
        }
        this.bcWebsiteUsage.getData().clear(); // Clear previous data
        this.bcWebsiteUsage.getData().add(series);
    }

    public String getHostName(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String hostname = uri.getHost();
        // to provide faultproof result, check if not null then return only hostname, without www.
        if (hostname != null) {
            return hostname.startsWith("www.") ? hostname.substring(4) : hostname;
        } else {
            hostname = url;
        }
        return hostname;
    }

    public void initLineChart() {
        this.lcDailyUsage.setTitle("Daily Usage");
        this.lcDailyUsage.setPrefSize(620, 600); // Modify the size as needed
        this.lcDailyUsage.getStylesheets().add(getClass().getResource("/com/cab302/wellbeing/styles.css").toExternalForm()); // Add this line
    }

    public void initBarChart() {
        this.bcWebsiteUsage.setTitle("Usage of Websites");
        this.bcWebsiteUsage.setPrefSize(620, 600); // Modify the size as needed
        this.bcWebsiteUsage.getStylesheets().add(getClass().getResource("/com/cab302/wellbeing/styles.css").toExternalForm()); // Add this line
    }

    public List<LineChartModel> getLCDataset() {
        String selectQuery = "SELECT SUM(Duration) as DurationSum, SessionDate FROM WellBeing.BrowsingData WHERE UserID = ?  GROUP BY SessionDate ORDER BY SessionDate";
        List<LineChartModel> res = new ArrayList<>();
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

    public List<BarChartModel> getBCDataset() {
        String selectQuery = "SELECT SUM(Duration) as DurationSum, URL FROM WellBeing.BrowsingData WHERE UserID = ?  GROUP BY URL";
        List<BarChartModel> res = new ArrayList<>();
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

    public List<BarChartModel> sortDataset(List<BarChartModel> dataset) {
        int n = dataset.size();
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

    public List<BarChartModel> getTop5(List<BarChartModel> dataset) {
        return dataset.stream()
                .sorted((a, b) -> Integer.compare(b.durationSum, a.durationSum))
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<BarChartModel> getTop5BCDataset() {
        List<BarChartModel> sortedDataset = getBCDataset();
        return getTop5(sortedDataset);
    }

    public void applyColors(Color backgroundColor, Color textColor, Color buttonColor) {
        String backgroundHex = getHexColor(backgroundColor);
        String textHex = getHexColor(textColor);
        String buttonHex = getHexColor(buttonColor);

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

