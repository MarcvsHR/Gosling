module prodo.marc.gosling {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires java.persistence;
    requires lombok;
    requires javafx.media;
    requires java.sql;
    requires log4j;
    requires org.hibernate.orm.core;
    requires java.naming;
    requires org.apache.commons.lang3;
    requires java.desktop;
    requires org.controlsfx.controls;


    opens prodo.marc.gosling to javafx.fxml;
    exports prodo.marc.gosling;
    exports prodo.marc.gosling.controllers;
    exports prodo.marc.gosling.dao;
    opens prodo.marc.gosling.controllers to javafx.fxml,javafx.base;
    opens prodo.marc.gosling.dao to org.hibernate.orm.core;

    //ing does not "opens prodo.marc.gosling.dao" to module org.hibernate.orm.core
}