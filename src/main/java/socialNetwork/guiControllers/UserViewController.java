package socialNetwork.guiControllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.*;
import socialNetwork.utilitaries.ListViewInitialize;
import socialNetwork.utilitaries.MessageAlert;
import socialNetwork.utilitaries.SceneSwitcher;
import socialNetwork.utilitaries.UsersSearchProcess;
import socialNetwork.utilitaries.events.Event;

import socialNetwork.utilitaries.events.EventPublicChangeEvent;

import socialNetwork.utilitaries.events.FriendRequestChangeEvent;

import socialNetwork.utilitaries.events.FriendshipChangeEvent;
import socialNetwork.utilitaries.observer.Observer;

import java.io.IOException;
import java.util.List;


public class UserViewController implements Observer<Event> {
    ObservableList<User> modelFriends = FXCollections.observableArrayList();
    ObservableList<User> modelSearchFriends = FXCollections.observableArrayList();
    ObservableList<EventPublic> modelNotifications = FXCollections.observableArrayList();

    @FXML
    AnchorPane mainAnchorPane;
    @FXML
    ImageView mainImage;
    @FXML
    AnchorPane secondAnchorPane;
    @FXML
    Pagination paginationListView;
    @FXML
    ListView<User> listViewOfFriends;
    @FXML
    ListView<User> usersListView;
    @FXML
    ListView<EventPublic> notificationsListView;
    @FXML
    Button deleteFriendshipButton;
    @FXML
    Button addFriendshipButton;
    @FXML
    Button friendRequestButton;
    @FXML
    TextField searchFriendshipField;
    @FXML
    Polygon triangleAuxiliaryLabel;
    @FXML
    Polygon secondPolygon;
    @FXML
    FontAwesomeIconView bellIconView;


    NetworkController networkController;
    PageUser rootPage;
    Stage displayStage;

    private int itemsPerPage(){
        return 3;
    }

    private ListView<User> createPage(int pageIndex){
        List<User> userList = networkController
                .getNetworkService()
                .getFriendshipsOnPageForUser(rootPage.getRoot().getId(),pageIndex)
                .stream().toList();
        modelFriends.setAll(userList);
        return listViewOfFriends;
    }

    private void createPagination(){
        networkController.getNetworkService().setPageSize( itemsPerPage() );
        int amountOfFriends = networkController.getNetworkService()
                .getAllFriendshipForSpecifiedUserService(rootPage.getRoot().getId()).size();
        int numberOfPages = amountOfFriends / itemsPerPage() +
                ( amountOfFriends % itemsPerPage() != 0 ? 1 : 0 );
        if( numberOfPages == 0 )
            numberOfPages = 1;
        paginationListView.setPageCount(numberOfPages);
        paginationListView.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer pageIndex) {
                return createPage(pageIndex);
            }
        });
    }

    private void refreshPage(){
        RefreshPageUser refreshPageUser = new RefreshPageUser(true,true,false);
        rootPage.refresh(rootPage.getRoot().getUsername(),refreshPageUser);
    }

    private void setImage(User mainUser){
        String idUser = String.valueOf(mainUser.getId());
        String path = "images/u" + idUser + ".jpg";
        try {
            Image genericUserImage = new Image(path);
            mainImage.setImage(genericUserImage);
        }catch (IllegalArgumentException e){
            Image genericUserImage = new Image("images/emptyProfilePicture.jpg");
            mainImage.setImage(genericUserImage);
        }
    }

    public void setNetworkController(Stage primaryStage, NetworkController service, PageUser rootPage){
        this.networkController = service;
        networkController.getNetworkService().addObserver(this);
        networkController.getFriendRequestService().addObserver(this);
        this.displayStage = primaryStage;
        this.rootPage = rootPage;
        setImage(rootPage.getRoot());
        refreshPage();
        ListViewInitialize.createListViewWithNotification(notificationsListView, modelNotifications);
        initModelFriends();
        initModelNotifications();
        createPagination();
        if(notificationsListView.getItems().size() != 0)
            bellIconView.setFill(Color.valueOf("#d53939"));
        if(displayStage.getUserData() != null && displayStage.getUserData().equals("seen"))
            bellIconView.setFill(Color.valueOf("#000000"));
    }


    private void initModelFriends(){
        List< User > friendListForUser = rootPage.getFriendList();
        modelFriends.setAll(friendListForUser);
    }

    private void initModelNotifications(){
        List<EventPublic> notificationEvents =
                rootPage.getNetworkController().filterAllEventPublicForNotification
                        (rootPage.getRoot().getId(), 30L);
        modelNotifications.setAll(notificationEvents);
    }

    @FXML
    public void initialize(){
        ListViewInitialize.createListViewWithUser(listViewOfFriends, modelFriends);
        ListViewInitialize.createListViewWithUser(usersListView, modelSearchFriends);
        searchFriendshipField.textProperty().addListener(o -> handleFilterInUserController());
    }

    @Override
    public void update(Event event) {
        if(event instanceof EventPublicChangeEvent) {
            initModelNotifications();
        }
        if(event instanceof FriendshipChangeEvent) {
            initModelFriends();
            createPagination();
        }
        if(event instanceof FriendRequestChangeEvent){
            initModelFriends();
            createPagination();
        }
    }

    @FXML
    public void handleScrollListViewFriends(ScrollEvent event){
        //System.out.println(event.getX() + " " +  event.getY());
    }

    @FXML
    public void handleFriendshipDelete(){

        User mainUser = rootPage.getRoot();
        if(listViewOfFriends.getSelectionModel().getSelectedItem() != null){
            User user = listViewOfFriends.getSelectionModel().getSelectedItem();
            Long idFirstUser = mainUser.getId();
            Long idSecondUser =  user.getId();
            networkController.removeFriendship(idFirstUser,idSecondUser);
            MessageAlert.showMessage(displayStage, Alert.AlertType.INFORMATION,
                    "Delete Friendship","The Friendship has been deleted successfully!");
        }
        else{
            MessageAlert.showErrorMessage(displayStage,"There is no selection!");
        }
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
    public void switchToMessagesViewSceneFromUserScene(ActionEvent event) throws IOException{
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
    public void disableAddFriendship(){
        if(listViewOfFriends.getSelectionModel().getSelectedItem() != null){
            addFriendshipButton.setDisable(true);
            deleteFriendshipButton.setDisable(false);
        }
    }

    @FXML
    public void disableDeleteFriendship(){
        if(usersListView.getSelectionModel().getSelectedItem() != null){
            addFriendshipButton.setDisable(false);
            deleteFriendshipButton.setDisable(true);
        }
    }

    @FXML
    public void enableAllButtonsAndClearSelection(){
        addFriendshipButton.setDisable(false);
        deleteFriendshipButton.setDisable(false);
        listViewOfFriends.getSelectionModel().clearSelection();
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
    public void setNotificationsListViewOnVisible(){
        notificationsListView.setVisible(true);
        secondPolygon.setVisible(true);
        bellIconView.setFill(Color.BLACK);
        displayStage.setUserData("seen");
    }

}
