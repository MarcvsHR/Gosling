module prodo.marc.gosling {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires java.persistence;
    requires lombok;
    requires javafx.media;

    opens prodo.marc.gosling to javafx.fxml;
    exports prodo.marc.gosling;
    exports prodo.marc.gosling.controllers;
    opens prodo.marc.gosling.controllers to javafx.fxml;
}