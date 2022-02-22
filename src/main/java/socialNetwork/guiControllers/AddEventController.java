package socialNetwork.guiControllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.exceptions.ExceptionBaseClass;
import socialNetwork.utilitaries.MessageAlert;


import java.time.LocalDate;
import java.time.LocalDateTime;

public class AddEventController {

    NetworkController networkController;

    @FXML
    TextField nameTextField;
    @FXML
    TextField descriptionTextField;
    @FXML
    DatePicker datePicker;
    @FXML
    Button signUpButton;
    @FXML
    Button cancelButton;

    Stage stage;

    public void setNetworkController(Stage primaryStage, NetworkController service){
        networkController = service;
        this.stage = primaryStage;
    }

    @FXML
    public void doAddEvent(){
        String name = nameTextField.getText();
        String description = descriptionTextField.getText();
        LocalDate date = datePicker.getValue();
        if(date == null){
            MessageAlert.showErrorMessage(stage, "No date has been selected!");
            return;
        }
        if(date.isBefore(LocalDate.now())) {
            MessageAlert.showErrorMessage(stage, "The selected date should be from today onwards");
            return;
        }
        try {
            LocalDateTime time = date.atStartOfDay();
            networkController.addEventPublic(name, description, time);
            MessageAlert.showMessage(stage, Alert.AlertType.INFORMATION,
                    "Successful creation", "You have successfully created an event");
            stage.close();
        } catch (ExceptionBaseClass exceptionBaseClass){
            MessageAlert.showErrorMessage(stage, exceptionBaseClass.getMessage());
        }
    }

    @FXML
    public void exitWindow(){
        stage.close();
    }
}
