package socialNetwork.utilitaries;

import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.PageUser;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.ExceptionBaseClass;

public class UsersSearchProcess {

    public static void sendFriendshipRequest(ListView<User> usersListView, PageUser rootPageUser,
                                               NetworkController networkController, Stage displayStage){
        User mainUser = rootPageUser.getRoot();
        if(usersListView.getSelectionModel().getSelectedItem() != null){
            User user = usersListView.getSelectionModel().getSelectedItem();
            Long idFirstUser = mainUser.getId();
            Long idSecondUser = user.getId();
            try {
                networkController.sendInvitationForFriendships(idFirstUser, idSecondUser);
                MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,
                        "Sent invitation", "The invitation has just been sent");
            } catch (ExceptionBaseClass exceptionBaseClass){
                MessageAlert.showErrorMessage(displayStage, exceptionBaseClass.getMessage());
            }
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
        }
    }

    public static void setUsersListViewOnVisible(ListView<User> usersListView, Polygon triangleAuxiliaryLabel){
        usersListView.setVisible(true);
        triangleAuxiliaryLabel.setVisible(true);
    }

    public static void setUsersListViewOnInvisible(ListView<User> usersListView, Polygon triangleAuxiliaryLabel,
                                                   TextField searchFriendshipField){
        usersListView.setVisible(false);
        triangleAuxiliaryLabel.setVisible(false);
        searchFriendshipField.clear();
    }
}
