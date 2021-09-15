package prodo.marc.gosling.service;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;

import java.util.Optional;

public class Popups {

    //private static final Logger logger = LogManager.getLogger(ID3v2Utils.class);

    @FXML
    public static void giveInfoAlert(String title, String type, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public static boolean giveConfirmAlert(String title, String type, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(type);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();

        return result.filter(buttonType -> buttonType == ButtonType.OK).isPresent();
    }
}
