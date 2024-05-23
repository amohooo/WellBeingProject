package com.cab302.wellbeing.model;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WellBeingApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/com/cab302/wellbeing/login.fxml"));
            Scene scene = new Scene(root);

            stage.setTitle("Well Being");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        DataBaseConnection dbConnection = new DataBaseConnection();
        dbConnection.initializeAndInsertUser();
        launch();
    }
}