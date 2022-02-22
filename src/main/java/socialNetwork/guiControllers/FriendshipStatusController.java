package socialNetwork.guiControllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.*;
import socialNetwork.exceptions.ExceptionBaseClass;
import socialNetwork.utilitaries.ListViewInitialize;
import socialNetwork.utilitaries.MessageAlert;
import socialNetwork.utilitaries.SceneSwitcher;
import socialNetwork.utilitaries.UsersSearchProcess;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.events.EventPublicChangeEvent;
import socialNetwork.utilitaries.events.FriendRequestChangeEvent;
import socialNetwork.utilitaries.observer.Observer;

import java.io.IOException;
import java.util.List;

public class FriendshipStatusController implements Observer<Event> {

    ObservableList<RequestInvitationGUIDTO> modelFriendshipRequestDTO = FXCollections.observableArrayList();
    ObservableList<RequestInvitationGUIDTO> modelFriendshipRequestDTOReact = FXCollections.observableArrayList();
    ObservableList<User> modelSearchFriends = FXCollections.observableArrayList();
    ObservableList<EventPublic> modelNotifications = FXCollections.observableArrayList();

    NetworkController networkController;
    PageUser rootPageUser;
    Stage displayStage;

    @FXML
    Button showRequestsSentButton;
    @FXML
    Button showRequestsReceivedButton;
    @FXML
    Button addFriendshipButton;
    @FXML
    Button friendRequestButton;
    @FXML
    TextField searchFriendshipField;
    @FXML
    Polygon triangleAuxiliaryLabel;
    @FXML
    ListView<User> usersListView;
    @FXML
    ListView<RequestInvitationGUIDTO> requestsSentListView;
    @FXML
    ListView<RequestInvitationGUIDTO> requestsReceivedListView;
    @FXML
    ListView<EventPublic> notificationsListView;
    @FXML
    Button approveRequestButton;
    @FXML
    Button rejectRequestButton;
    @FXML
    Button resubmissionRequestButton;
    @FXML
    Button withdrawRequestButton;
    @FXML
    Polygon secondPolygon;
    @FXML
    FontAwesomeIconView bellIconView;

    private void refreshPage(){
        RefreshPageUser refreshPageUser = new RefreshPageUser(true,true,false);
        rootPageUser.refresh(rootPageUser.getRoot().getUsername(),refreshPageUser);
    }

    public void setNetworkController(Stage primaryStage, NetworkController service, PageUser rootPageUser){
        this.networkController = service;
        networkController.getFriendRequestService().addObserver(this);
        this.displayStage = primaryStage;
        this.rootPageUser = rootPageUser;
        refreshPage();
        ListViewInitialize.createListViewWithNotification(notificationsListView, modelNotifications);
        initModelFriendRequest();
        initModelNotifications();
        if(notificationsListView.getItems().size() != 0)
            bellIconView.setFill(Color.valueOf("#d53939"));
        if(displayStage.getUserData()!=null && displayStage.getUserData().equals("seen"))
            bellIconView.setFill(Color.valueOf("#000000"));
    }

    private void initModelFriendRequest(){
        User mainUser = rootPageUser.getRoot();
        List<FriendshipRequestDTO> friendshipRequestDTOList = networkController
                .findAllRequestFriendsForUser(mainUser.getId());

        List<RequestInvitationGUIDTO> mainUserIsOneWhoSendsRequests = friendshipRequestDTOList
                .stream()
                .filter(x-> mainUser.getId().equals(x.getUserThatSendsRequest().getId()))
                .map(friendshipRequestDTO -> {
                    return new RequestInvitationGUIDTO(friendshipRequestDTO.getUserThatReceivesRequest().getId(),
                            friendshipRequestDTO.getUserThatReceivesRequest().getFirstName(),
                            friendshipRequestDTO.getUserThatReceivesRequest().getLastName(),
                            friendshipRequestDTO.getLocalDateTime(),
                            friendshipRequestDTO.getInvitationStage());
                })
                .toList();

        List<RequestInvitationGUIDTO> notMainUserSendsRequests = friendshipRequestDTOList
                .stream()
                .filter(x-> mainUser.getId().equals(x.getUserThatReceivesRequest().getId()))
                .map(friendshipRequestDTO -> {
                    return new RequestInvitationGUIDTO(friendshipRequestDTO.getUserThatSendsRequest().getId(),
                            friendshipRequestDTO.getUserThatSendsRequest().getFirstName(),
                            friendshipRequestDTO.getUserThatSendsRequest().getLastName(),
                            friendshipRequestDTO.getLocalDateTime(),
                            friendshipRequestDTO.getInvitationStage());
                })
                .toList();

        modelFriendshipRequestDTO.setAll(mainUserIsOneWhoSendsRequests);
        modelFriendshipRequestDTOReact.setAll(notMainUserSendsRequests);
    }

    private void initModelNotifications(){
        List<EventPublic> notificationEvents =
                rootPageUser.getNetworkController().filterAllEventPublicForNotification
                        (rootPageUser.getRoot().getId(), 30L);
        modelNotifications.setAll(notificationEvents);
    }

    @FXML
    public void initialize(){
        showRequestsSentButton.setStyle("-fx-font-weight: bold");
        initializeListView(requestsSentListView, modelFriendshipRequestDTO);
        initializeListView(requestsReceivedListView, modelFriendshipRequestDTOReact);
        ListViewInitialize.createListViewWithUser(usersListView, modelSearchFriends);
        searchFriendshipField.textProperty().addListener(o -> handleFilterInFriendshipStatusController());
    }

    private void initializeListView(ListView<RequestInvitationGUIDTO> listView,
                                    ObservableList<RequestInvitationGUIDTO> modelFriendshipRequest) {
        listView.setItems(modelFriendshipRequest);
        listView.setCellFactory(r -> new ListCell<RequestInvitationGUIDTO>(){
            @Override
            protected void updateItem(RequestInvitationGUIDTO item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                }
                else{
                    setText("Friendship with " + item.getFirstName() + " " + item.getLastName() + " is currently on " +
                            item.getInvitationStage() + " since " + item.getLocalDateTime().toLocalDate());
                }
            }
        });
    }

    @Override
    public void update(Event event) {
        if(event instanceof EventPublicChangeEvent) {
            initModelNotifications();
        }
        if(event instanceof FriendRequestChangeEvent)
            initModelFriendRequest();
    }

    @FXML
    public void switchToUserViewScene(ActionEvent event) throws IOException {
        SceneSwitcher.switchToUserViewScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void switchToFriendshipRequestScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToFriendshipRequestScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void switchToMessagesScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToMessageScene(event, getClass(), networkController, rootPageUser, displayStage);

    }

    @FXML
    public void switchToReportsScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToReportsScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void switchToEventsViewFromUserScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToEventsScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void handleApprovedFriend(){

        User mainUser = rootPageUser.getRoot();
        RequestInvitationGUIDTO requestInvitationGUIDTO = requestsReceivedListView
                .getSelectionModel().getSelectedItem();
        if(requestInvitationGUIDTO != null){
            Long userThatReceivesInvitationAndAcceptedId = mainUser.getId();
            Long userThatSendInvitationAndWaitVerdictId = requestInvitationGUIDTO.getId();
            try {
                networkController.updateApprovedFriendship(userThatReceivesInvitationAndAcceptedId,userThatSendInvitationAndWaitVerdictId);
                MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,"Approved Request",
                        "The friendship request has been approved!");
            }
            catch(ExceptionBaseClass exceptionBaseClass){
                MessageAlert.showErrorMessage(displayStage,exceptionBaseClass.getMessage());
            }
            finally {
                requestsReceivedListView.getSelectionModel().clearSelection();
            }
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
        }
   }

    @FXML
    public void handleRejectFriend(){

        User mainUser = rootPageUser.getRoot();
        RequestInvitationGUIDTO requestInvitationGUIDTO = requestsReceivedListView
                .getSelectionModel().getSelectedItem();
        if(requestInvitationGUIDTO != null){

            Long userThatReceivesInvitationAndRejectedId = mainUser.getId();
            Long userThatSendInvitationAndWaitVerdictId = requestInvitationGUIDTO.getId();
            try {
                networkController.updateRejectedFriendship(userThatReceivesInvitationAndRejectedId , userThatSendInvitationAndWaitVerdictId);
                MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,"Rejected Request",
                        "The friendship request has been rejected!");
            }
            catch(ExceptionBaseClass exceptionBaseClass){
                MessageAlert.showErrorMessage(displayStage,exceptionBaseClass.getMessage());
            }
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
        }
    }

    @FXML
    public void handleResubmissionRequest(){

        User mainUser = rootPageUser.getRoot();
        RequestInvitationGUIDTO requestInvitationGUIDTO = requestsReceivedListView
                .getSelectionModel().getSelectedItem();
        if(requestInvitationGUIDTO == null){
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
            return;
        }

        if(!requestInvitationGUIDTO.getInvitationStage().equals(InvitationStage.REJECTED)){
            MessageAlert.showErrorMessage(displayStage,"You cannot resubmit request that is already pending/approved");
            return;
        }

        try{
            Long idUserThatRejectButChangeHisMind = mainUser.getId();
            Long idUserThatSendInitiallyInvitation = requestInvitationGUIDTO.getId();
            networkController.updateRejectedToPendingFriendship(idUserThatRejectButChangeHisMind,idUserThatSendInitiallyInvitation);
            MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,"Resubmitted Request",
                    "The friendship request has been resubmitted!");
        }
        catch(ExceptionBaseClass exceptionBaseClass){
            MessageAlert.showErrorMessage(displayStage,exceptionBaseClass.getMessage());
        }
        finally {
            requestsReceivedListView.getSelectionModel().clearSelection();
        }
    }

    @FXML
    public void handleWithdrawRequest(){
        User mainUser = rootPageUser.getRoot();
        RequestInvitationGUIDTO requestInvitationGUIDTO = requestsSentListView
                .getSelectionModel().getSelectedItem();

        if(requestInvitationGUIDTO == null){
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
            return;
        }

        try{
            Long userIdThatSendInvitationButWithdrawIt = mainUser.getId();
            Long userIdThatReceiveInvitation = requestInvitationGUIDTO.getId();
            networkController.withdrawFriendRequest(userIdThatSendInvitationButWithdrawIt,
                    userIdThatReceiveInvitation);
            MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,"Withdraw Request",
                    "The friendship request was withdrawn");
        }
        catch (ExceptionBaseClass exceptionBaseClass){
            MessageAlert.showErrorMessage(displayStage,exceptionBaseClass.getMessage());
        }

    }

    @FXML
    public void showRequestsReceivedListView(){
        showRequestsSentButton.setStyle("");
        showRequestsReceivedButton.setStyle("-fx-font-weight: bold");
        requestsSentListView.setVisible(false);
        requestsReceivedListView.setVisible(true);
        approveRequestButton.setDisable(false);
        rejectRequestButton.setDisable(false);
        resubmissionRequestButton.setDisable(false);
        withdrawRequestButton.setDisable(true);
    }

    @FXML
    public void showRequestsSentListView(){
        showRequestsSentButton.setStyle("-fx-font-weight: bold");
        showRequestsReceivedButton.setStyle("");
        requestsSentListView.setVisible(true);
        requestsReceivedListView.setVisible(false);
        approveRequestButton.setDisable(true);
        rejectRequestButton.setDisable(true);
        resubmissionRequestButton.setDisable(true);
        withdrawRequestButton.setDisable(false);
    }

    private void handleFilterInFriendshipStatusController(){
        ListViewInitialize.handleFilter(networkController, rootPageUser, searchFriendshipField, modelSearchFriends);
    }

    @FXML
    public void setUsersListViewOnVisible(){
        usersListView.setVisible(true);
        triangleAuxiliaryLabel.setVisible(true);
    }

    @FXML
    public void setUsersListViewOnInvisible(){
        usersListView.setVisible(false);
        triangleAuxiliaryLabel.setVisible(false);
        searchFriendshipField.clear();
        notificationsListView.setVisible(false);
        secondPolygon.setVisible(false);
    }

    @FXML
    public void handleFriendshipRequestFromRequestsController(){
        UsersSearchProcess.sendFriendshipRequest(usersListView, rootPageUser, networkController, displayStage);
    }

    @FXML
    public void enableAllButtonsAndClearSelection(){
        addFriendshipButton.setDisable(true);
        rejectRequestButton.setDisable(false);
        approveRequestButton.setDisable(false);
        resubmissionRequestButton.setDisable(false);
        requestsSentListView.getSelectionModel().clearSelection();
    }

    @FXML
    public void disableWhenSearchForUser(){
        if(usersListView.getSelectionModel().getSelectedItem() != null){
            addFriendshipButton.setDisable(false);
            rejectRequestButton.setDisable(true);
            approveRequestButton.setDisable(true);
            resubmissionRequestButton.setDisable(true);
        }
    }

    @FXML
    public void disableButtonsOnRequestSentListView(){
        if(requestsSentListView.getSelectionModel().getSelectedItem() != null){
            addFriendshipButton.setDisable(true);
            rejectRequestButton.setDisable(true);
            approveRequestButton.setDisable(true);
            resubmissionRequestButton.setDisable(true);
        }
    }

    @FXML
    public void disableButtonsOnRequestReceivedListView(){
        if(requestsReceivedListView.getSelectionModel().getSelectedItem() != null){
            addFriendshipButton.setDisable(true);
            rejectRequestButton.setDisable(false);
            approveRequestButton.setDisable(false);
            resubmissionRequestButton.setDisable(false);
        }
    }

    @FXML
    public void setNotificationsListViewOnVisible(){
        notificationsListView.setVisible(true);
        secondPolygon.setVisible(true);
        bellIconView.setFill(Color.BLACK);
    }

}
