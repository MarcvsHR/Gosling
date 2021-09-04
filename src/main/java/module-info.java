module prodo.marc.gosling {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires java.persistence;
    requires lombok;
    requires javafx.media;
    requires mp3agic;
    requires java.sql;
    requires log4j;
    requires org.hibernate.orm.core;
    requires java.naming;


    opens prodo.marc.gosling to javafx.fxml;
    exports prodo.marc.gosling;
    exports prodo.marc.gosling.controllers;
    opens prodo.marc.gosling.controllers to javafx.fxml;
    opens prodo.marc.gosling.dao to org.hibernate.orm.core;

    //ing does not "opens prodo.marc.gosling.dao" to module org.hibernate.orm.core
}