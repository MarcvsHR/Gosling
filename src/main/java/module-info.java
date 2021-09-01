module prodo.marc.gosling {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires java.persistence;
    requires lombok;
    requires javafx.media;

    opens prodo.marc.gosling to javafx.fxml;
    exports prodo.marc.gosling;
    exports prodo.marc.gosling.controllers;
    opens prodo.marc.gosling.controllers to javafx.fxml;
}