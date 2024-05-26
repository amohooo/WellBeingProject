package com.cab302.wellbeing.model;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class is the main application class for the Well Being application.
 * It provides a method to start the application.
 */
public class WellBeingApplication extends Application {
    /**
     * This method is used to start the application.
     * @param stage - the stage for the application
     * @throws IOException - if an I/O error occurs
     */
    @Override
    public void start(Stage stage) throws IOException {
        try{
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene scene = new Scene(root);

            stage.setTitle("Well Being");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This method is used to launch the application.
     * @param args - the arguments for the application
     */
    public static void main(String[] args) {
        DataBaseConnection dbConnection = new DataBaseConnection();
        dbConnection.initializeAndInsertUser();
        launch();
    }
}