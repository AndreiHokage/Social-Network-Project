package socialNetwork.guiControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.exceptions.ExceptionBaseClass;
import socialNetwork.utilitaries.MessageAlert;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;

public class SignUpController {

    NetworkController networkController;

    @FXML
    TextField firstNameTextField;
    @FXML
    TextField lastNameTextField;
    @FXML
    TextField usernameSignUpTextField;
    @FXML
    PasswordField passwordSignUpField;
    @FXML
    PasswordField passwordConfirmationSignUpField;
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
    public void doSignUp(){
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        String username = usernameSignUpTextField.getText();
        String password = passwordSignUpField.getText();
        String confirmationPassword = passwordConfirmationSignUpField.getText();
        if(!password.equals(confirmationPassword)){
            MessageAlert.showErrorMessage(stage, "Mismatch between password and confirmation password");
            return;
        }
        try {
            boolean checkIfSigned = networkController.signUp(firstName, lastName, username, password);
            if(checkIfSigned) {
                MessageAlert.showMessage(stage, Alert.AlertType.INFORMATION,
                        "Successful sign up", "You have successfully signed up");
                stage.close();
            }
            else{
                MessageAlert.showErrorMessage(stage, "Username already exists");
            }
        } catch (ExceptionBaseClass | IllegalBlockSizeException | BadPaddingException | InvalidKeyException exceptionBaseClass){
            MessageAlert.showErrorMessage(stage, exceptionBaseClass.getMessage());
        }
    }

    @FXML
    public void exitWindow(){
        stage.close();
    }
}
