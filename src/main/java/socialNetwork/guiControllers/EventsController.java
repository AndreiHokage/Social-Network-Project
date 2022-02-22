package socialNetwork.guiControllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.*;
import socialNetwork.exceptions.ExceptionBaseClass;
import socialNetwork.utilitaries.*;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.events.EventPublicChangeEvent;
import socialNetwork.utilitaries.events.FriendshipChangeEvent;
import socialNetwork.utilitaries.observer.Observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventsController implements Observer<Event>{

    ObservableList<User> modelFriends = FXCollections.observableArrayList();
    ObservableList<User> modelSearchFriends = FXCollections.observableArrayList();
    ObservableList<EventPublic> modelEventsNotSubscribed = FXCollections.observableArrayList();
    ObservableList<DTOEventPublicUser> modelEventsSubscribed = FXCollections.observableArrayList();
    ObservableList<EventPublic> modelNotifications = FXCollections.observableArrayList();

    @FXML
    AnchorPane mainAnchorPane;
    @FXML
    ListView<User> usersListView;
    @FXML
    ListView<EventPublic> unsubscribedEventsListView;
    @FXML
    ListView<DTOEventPublicUser> subscribedEventsListView;
    @FXML
    ListView<EventPublic> notificationsListView;
    @FXML
    Button friendRequestButton;
    @FXML
    Button requestFriendshipButton;
    @FXML
    Button addEventButton;
    @FXML
    Button subscribeToEventButton;
    @FXML
    TextField searchFriendshipField;
    @FXML
    Polygon triangleAuxiliaryLabel;
    @FXML
    Polygon secondPolygon;
    @FXML
    FontAwesomeIconView bellIconView;
    ScrollBar scrollBarListViewOfFriends;

    NetworkController networkController;
    PageUser rootPage;
    Stage displayStage;

    private void refreshPage(){
        RefreshPageUser refreshPageUser = new RefreshPageUser(false,false,false);
        rootPage.refresh(rootPage.getRoot().getUsername(),refreshPageUser);
    }

    public void setNetworkController(Stage primaryStage, NetworkController service, PageUser rootPage){
        this.networkController = service;
        networkController.getEventPublicService().addObserver(this);
        this.displayStage = primaryStage;
        this.rootPage = rootPage;
        ListViewInitialize.createListViewWithEvent(unsubscribedEventsListView, modelEventsNotSubscribed);
        ListViewInitialize.createListViewWithDtoEvent(subscribedEventsListView, modelEventsSubscribed, rootPage,
                displayStage, bellIconView);
        ListViewInitialize.createListViewWithNotification(notificationsListView, modelNotifications);
        refreshPage();
        initModelFriends();
        initModelEventPublic();
        initModelNotifications();
        if(notificationsListView.getItems().size() != 0)
            bellIconView.setFill(Color.valueOf("#d53939"));
        if(displayStage.getUserData()!=null && displayStage.getUserData().equals("seen"))
            bellIconView.setFill(Color.valueOf("#000000"));
    }

    private void initModelFriends(){
        List< User > friendListForUser = rootPage.getFriendList();
        modelFriends.setAll(friendListForUser);

    }

    private void initModelEventPublic(){
        List<EventPublic> publicEventsAll =
                rootPage.getNetworkController().getAllEventPublic();
        List<EventPublic> publicSubscribedEvents =
                rootPage.getNetworkController().getAllEventPublicForSpecifiedUser(rootPage.getRoot().getId());
        List<EventPublic> allUnsubscribedEvents = new ArrayList<>();
        publicEventsAll.forEach(event -> {
            if(!publicSubscribedEvents.contains(event))
                allUnsubscribedEvents.add(event);
        });
        modelEventsNotSubscribed.setAll(allUnsubscribedEvents);

        List<DTOEventPublicUser> allSubscribedEvents =
                rootPage.getNetworkController().getAllPublicEventsWithNotifications(rootPage.getRoot().getId());
        modelEventsSubscribed.setAll(allSubscribedEvents);
    }

    private void initModelNotifications(){
        List<EventPublic> notificationEvents =
                rootPage.getNetworkController().filterAllEventPublicForNotification
                        (rootPage.getRoot().getId(), 30L);
        modelNotifications.setAll(notificationEvents);
    }

    @FXML
    public void initialize(){
        ListViewInitialize.createListViewWithUser(usersListView, modelSearchFriends);
        searchFriendshipField.textProperty().addListener(o -> handleFilterInUserController());
    }

    @Override
    public void update(Event event) {

        if (event instanceof FriendshipChangeEvent)
            initModelFriends();
        if(event instanceof EventPublicChangeEvent) {
            initModelEventPublic();
            initModelNotifications();
        }
    }

    @FXML
    public void enableAllButtonsAndClearSelection(){
        requestFriendshipButton.setDisable(false);
        addEventButton.setDisable(false);
        subscribeToEventButton.setDisable(false);
    }

    @FXML
    public void disableDeleteFriendship(){
        addEventButton.setDisable(true);
        requestFriendshipButton.setDisable(false);
        subscribeToEventButton.setDisable(true);
    }

    @FXML
    public void disableWhenSelectFromUnsubscribedTable(){
        addEventButton.setDisable(true);
        requestFriendshipButton.setDisable(true);
        subscribeToEventButton.setDisable(false);
    }

    @FXML
    public void handleFriendshipRequestFromUserViewController(){
        UsersSearchProcess.sendFriendshipRequest(usersListView, rootPage, networkController, displayStage);
    }

    @FXML
    public void switchToFriendshipRequestSceneFromUserScene(ActionEvent event) throws IOException {
        SceneSwitcher.switchToFriendshipRequestScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void switchToUserViewSceneFromUserScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToUserViewScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void switchToMessagesViewSceneFromUserScene(ActionEvent event) throws IOException {
        SceneSwitcher.switchToMessageScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void switchToReportsViewSceneFromUserScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToReportsScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void switchToEventsViewFromUserScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToEventsScene(event, getClass(), networkController, rootPage, displayStage);
    }

    @FXML
    public void setUsersListViewOnVisible(){
        UsersSearchProcess.setUsersListViewOnVisible(usersListView, triangleAuxiliaryLabel);
    }

    @FXML
    public void setUsersListViewOnInvisible(){
        UsersSearchProcess.setUsersListViewOnInvisible(usersListView, triangleAuxiliaryLabel, searchFriendshipField);
        notificationsListView.setVisible(false);
        secondPolygon.setVisible(false);
    }

    @FXML
    private void handleFilterInUserController(){
        ListViewInitialize.handleFilter(networkController, rootPage, searchFriendshipField, modelSearchFriends);
    }

    @FXML
    public void handleAddEvent() throws IOException {
        UnorderedPair<Stage, FXMLLoader> unorderedPair = StageBuilder
                .buildStage(getClass(),"/socialNetwork.gui/addEvent.fxml",
                        Optional.of("/css/signUp.css") ,"Kage");
        Stage addEventStage = unorderedPair.left;
        FXMLLoader loader = unorderedPair.right;
        AddEventController addEventController = loader.getController();
        addEventController.setNetworkController(addEventStage, networkController);
        addEventStage.show();
    }

    @FXML
    public void subscribeToEventAction(){
        EventPublic eventWithNotifications = unsubscribedEventsListView.getSelectionModel().getSelectedItem();
        if(eventWithNotifications == null)
            return;
        try{
            networkController.subscribeEventPublic(rootPage.getRoot().getId(), eventWithNotifications.getId());
            MessageAlert.showInformationMessage(displayStage, "You have subscribed to the event!");
        }
        catch (ExceptionBaseClass error){
            MessageAlert.showErrorMessage(displayStage, error.getMessage());
        }
    }

    @FXML
    public void setNotificationsListViewOnVisible(){
        notificationsListView.setVisible(true);
        secondPolygon.setVisible(true);
        bellIconView.setFill(Color.BLACK);
    }
}
