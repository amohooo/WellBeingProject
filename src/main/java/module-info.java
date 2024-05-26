/**
 * This module-info.java file is used to define the module for the model package.
 * It requires javafx.controls, javafx.fxml, java.sql, javafx.web, java.desktop, jbcrypt, javafx.media, mockito.inline, org.mockito, javafx.graphics and javafx.swing.
 * It exports the model package and controller package.
 * It opens the controller and model package to javafx.fxml.
 */
module com.cab302.wellbeing.model {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.web;
    requires java.desktop;
    requires jbcrypt;
    requires javafx.media;
    requires mockito.inline;
    requires org.mockito;
    requires javafx.graphics;
    requires javafx.swing;

    exports com.cab302.wellbeing.model;
    exports com.cab302.wellbeing.controller;
    opens com.cab302.wellbeing.controller to javafx.fxml;
    opens com.cab302.wellbeing.model to javafx.fxml;
}