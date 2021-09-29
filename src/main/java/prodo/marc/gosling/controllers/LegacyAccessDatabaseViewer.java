package prodo.marc.gosling.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.service.FileUtils;
import prodo.marc.gosling.service.SongGlobal;


import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class LegacyAccessDatabaseViewer implements   Initializable {

    @FXML
    public ComboBox boxTableSelect;
    @FXML
    public AnchorPane LegacyAccessDatabaseViewerPane;
    @FXML
    MenuItem openAccessDBMenuItem;
    @FXML
    private TableView LegacyDatabaseTableView;

    String databasePath;

    private static final Logger logger = LogManager.getLogger(LegacyAccessDatabaseViewer.class);


    private static final String JDBC_START_STRING = "jdbc:ucanaccess://";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("Hello world!!");
    }


    public void openAccessDB(ActionEvent event){
        logger.debug("OpenAccessDb");

        File f = FileUtils.openFile("Access Db", "accdb", SongGlobal.getCurrentFolder());
        databasePath = f.getPath();

        logger.debug("file: "+ databasePath);

        connectToAccessDatabase(databasePath);
    }

    public void connectToAccessDatabase(String databaseUrl){
        try (Connection connection = DriverManager.getConnection(JDBC_START_STRING+databaseUrl)) {

            try (ResultSet rsMD = connection.getMetaData().getTables(null, null, null, null)) {
                //logger.debug("Table names: ");
                while (rsMD.next()) {
                    String tblName = rsMD.getString("TABLE_NAME");
                    //logger.debug("--> "+tblName);
                    boxTableSelect.getItems().add(tblName);
                }
            }

        } catch (SQLException e) {
            logger.error("boom",e);
        }

    }

    public void fillTable(String databaseUrl, String table){

        LegacyDatabaseTableView.getItems().clear();
        LegacyDatabaseTableView.getColumns().clear();

        String sql="SELECT * FROM "+table;
        ObservableList<ObservableList<String> > data = FXCollections.observableArrayList();
        try (Connection connection = DriverManager.getConnection(JDBC_START_STRING+databaseUrl)) {

            try (ResultSet rs = connection.createStatement().executeQuery(sql)) {
                logger.debug("execute sql "+sql );
                ResultSetMetaData rsmd = rs.getMetaData();
                logger.debug("Column count: "+rsmd.getColumnCount());

                for(int i=1;i<=rsmd.getColumnCount();i++){
                    final int j = i-1;
                    TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i));


                    col.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param
                            -> new SimpleStringProperty(param.getValue().get(j).toString()));

                    LegacyDatabaseTableView.getColumns().addAll(col);

                }

                logger.debug("Column done!");
                /********************************
                 * Data added to ObservableList *
                 ********************************/

                while(rs.next()){
                    //Iterate Row
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){

                        row.add(rs.getObject(i)==null?"":rs.getObject(i).toString());
                    }

                    data.add(row);

                }
                LegacyDatabaseTableView.setItems(data);
            }

        } catch (SQLException e) {
            logger.error("boom",e);
        }

    }



    public void closeAccessDB(ActionEvent event){
        logger.debug("closeAccessDB");

    }


    public void tableSelected(ActionEvent event) {
        String selectedTable = boxTableSelect.getSelectionModel().getSelectedItem().toString();
        logger.debug("Selected table: "+selectedTable);

        if (databasePath != null) {
            fillTable(databasePath, selectedTable); }

    }
}
