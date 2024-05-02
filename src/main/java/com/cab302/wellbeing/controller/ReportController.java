package com.cab302.wellbeing.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * This class is a controller for the Report functionality in the application.
 * It provides methods to handle the report generation and display.
 */
public class ReportController {

    @FXML
    public Label lblReportMsg;

    @FXML
    public LineChart lcDailyUsage;

    public void btnHistoryOnAction(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cab302/wellbeing/BrowsingHistory.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("History");
            stage.setScene(new Scene(root1));
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading BrowsingHistory.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void btnCancelOnAction(){
        Stage stage = (Stage) lblReportMsg.getScene().getWindow();
        stage.close();  // Closes the current window
    }

    public void btnSaveOnAction(){
        saveAsPng(this.lcDailyUsage,"src/main/resources/com/cab302/wellbeing/DownloadedReport/Report.png");
    }

    public void saveAsPng(LineChart lineChart, String path) {
        WritableImage wi = lineChart.snapshot(new SnapshotParameters(), new WritableImage(800, 600));
        File file = new File(path);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wi, null), "png", file);
            System.out.println("Image saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

