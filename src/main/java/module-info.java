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