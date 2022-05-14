module com.example.demo {
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;
    requires com.google.gson;


    opens GUI to javafx.fxml;
    exports GUI;
    exports GUI.Controller;
    opens GUI.Controller to javafx.fxml;
}