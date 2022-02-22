package socialNetwork.guiControllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.Lighting;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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
import socialNetwork.utilitaries.events.MessageChangeEvent;
import socialNetwork.utilitaries.events.MessageChangeEventType;
import socialNetwork.utilitaries.observer.Observer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageController implements Observer<Event> {
    ObservableList<User> modelSearchFriends = FXCollections.observableArrayList();
    ObservableList<Chat> modelChatsName = FXCollections.observableArrayList();
    ObservableList<EventPublic> modelNotifications = FXCollections.observableArrayList();
    ObservableList<User> modelParticipantsToChat = FXCollections.observableArrayList();
    ObservableList<HBox> modelHBox = FXCollections.observableArrayList();
    List<HBox> hBoxArrayList = new ArrayList<>();
    Chat chatConversation = null;

    @FXML
    AnchorPane mainAnchorPane;
    @FXML
    HBox mainHorizontalBox;
    @FXML
    ListView<User> usersListView;
    @FXML
    Button friendRequestButton;
    @FXML
    TextField searchFriendshipField;
    @FXML
    Polygon triangleAuxiliaryLabel;
    @FXML
    FontAwesomeIconView newPersonConversationIcon;
    @FXML
    FontAwesomeIconView closeConversationStartIcon;
    @FXML
    FontAwesomeIconView confirmDestinationIcon;
    @FXML
    TextField searchUserToStartConversationField;
    @FXML
    ListView<User> startConversationListView;
    @FXML
    ListView<EventPublic> notificationsListView;
    @FXML
    ListView<User> participantsToChatListView;
    @FXML
    VBox newConversationBox;
    @FXML
    Label usernameLabelChat;
    @FXML
    ListView<Chat> chatsNameListView;
    @FXML
    TextField messageField;
    @FXML
    FontAwesomeIconView sendMessageIcon;
    @FXML
    ListView<HBox> discussionListView;
    @FXML
    Label welcomeMessageLabel;
    @FXML
    AnchorPane conversationAnchorPane;
    @FXML
    Polygon secondPolygon;
    @FXML
    FontAwesomeIconView bellIconView;
    @FXML
    FontAwesomeIconView showParticipantsIcon;
    @FXML
    VBox participantsChatVBox;
    @FXML
    FontAwesomeIconView closeChatParticipantsIcon;

    NetworkController networkController;
    PageUser rootPageUser;
    Stage displayStage;
    boolean firstTime = true;
    Long idUserLastMessage = -1L;

    private void refreshPage(){
        RefreshPageUser refreshPageUser = new RefreshPageUser(false,false,true);
        rootPageUser.refresh(rootPageUser.getRoot().getUsername(),refreshPageUser);
    }

    public void setNetworkController(Stage primaryStage, NetworkController service, PageUser rootPageUser){
        this.networkController = service;
        networkController.getMessageService().addObserver(this);
        this.displayStage = primaryStage;
        this.displayStage.getScene().getStylesheets().add("/css/listCell.css");
        this.rootPageUser = rootPageUser;
        refreshPage();
        usernameLabelChat.setText(rootPageUser.getRoot().getUsername());
        ListViewInitialize.createListViewWithChats(chatsNameListView,modelChatsName, rootPageUser.getRoot());
        ListViewInitialize.createListViewWithNotification(notificationsListView, modelNotifications);
        initModelChatsName();
        initModelNotifications();
        if(notificationsListView.getItems().size() != 0)
            bellIconView.setFill(Color.valueOf("#d53939"));
        if(displayStage.getUserData()!=null && displayStage.getUserData().equals("seen"))
            bellIconView.setFill(Color.valueOf("#000000"));
    }

    private void initModelChatsName(){
        modelChatsName.setAll(rootPageUser.getChatList());
    }

    private void initModelNotifications(){
        List<EventPublic> notificationEvents =
                rootPageUser.getNetworkController().filterAllEventPublicForNotification
                        (rootPageUser.getRoot().getId(), 30L);
        modelNotifications.setAll(notificationEvents);
    }

    @FXML
    public void initialize(){
        discussionListView.setVisible(false);
        startConversationListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ListViewInitialize.createListViewWithUser(usersListView, modelSearchFriends);
        ListViewInitialize.createListViewWithUser(startConversationListView, modelSearchFriends);
        ListViewInitialize.createListViewWithUser(participantsToChatListView, modelParticipantsToChat);
        discussionListView.setItems(modelHBox);
        searchFriendshipField.textProperty().addListener(o -> handleFilterInUserController());
        searchUserToStartConversationField.textProperty().addListener(o -> handleFilterSearchUserForNewConversation());
        messageField.textProperty().addListener(o -> handleMessageIcon());

        messageField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    sendMessage();
                }
            }
        });
    }

    private void updateActualChatWithMessages(Event event){
        MessageChangeEvent messageChangeEvent = (MessageChangeEvent) event;
        MessageChangeEventType type = messageChangeEvent.getType();
        Message message = messageChangeEvent.getData().getMainMessage();
        User userThatSendMessage = message.getFrom();

        if(type.equals(MessageChangeEventType.SEND) || type.equals(MessageChangeEventType.RESPOND)){

            Map< List<User> , Chat > chatMap = rootPageUser.getChatMap();
            chatConversation = chatMap.get(chatConversation.getMembers());
            loadConversationForSpecificSchat();
        }

    }

    @Override
    public void update(Event event) {
        if(event instanceof EventPublicChangeEvent) {
            initModelNotifications();
        }
        if(event instanceof MessageChangeEvent && chatConversation != null)
             updateActualChatWithMessages(event);
        modelChatsName.setAll(rootPageUser.getChatList());
    }

    private void loadConversationForSpecificSchat(){
        hBoxArrayList.clear();
        modelHBox.setAll(hBoxArrayList);

        User root = rootPageUser.getRoot();
        List<Message> chatMessages = chatConversation.getMessageList();
        List<ReplyMessage> chatReplyMessages = chatConversation.getReplyMessageList();
        int i = 0, j = 0;
        int n = chatMessages.size();
        int m = chatReplyMessages.size();
        while(i < n && j < m){
            if(chatMessages.get(i).getDate().compareTo(chatReplyMessages.get(j).getDate()) < 0){
                Message message = chatMessages.get(i);
                if(message.getFrom().getId().equals(root.getId())){
                    putMessageInScrollPane("sent",message);
                }
                else{
                    putMessageInScrollPane("receive",message);
                }
                i++;
            }
            else{
                ReplyMessage replyMessage = chatReplyMessages.get(j);
                if(replyMessage.getFrom().getId().equals(root.getId())){
                    putMessageInScrollPane("sent",replyMessage);
                }
                else{
                    putMessageInScrollPane("receive",replyMessage);
                }
                j++;
            }
        }

        while(i < n){
            Message message = chatMessages.get(i);
            if(message.getFrom().getId().equals(root.getId())){
                putMessageInScrollPane("sent",message);
            }
            else{
                putMessageInScrollPane("receive",message);
            }
            i++;
        }

        while(j < m){
            ReplyMessage replyMessage = chatReplyMessages.get(j);
            if(replyMessage.getFrom().getId().equals(root.getId())){
                putMessageInScrollPane("sent",replyMessage);
            }
            else{
                putMessageInScrollPane("receive",replyMessage);
            }
            j++;
        }

        modelHBox.setAll(hBoxArrayList);
        List<HBox> items = discussionListView.getItems();
        int index = items.size();
        items.add(new HBox());
        discussionListView.scrollTo(index);
    }

    @FXML
    public void loadConversation(){
        if(chatsNameListView.getSelectionModel().getSelectedItem() == null)
            return;
        if(firstTime) {
            discussionListView.setVisible(true);
            welcomeMessageLabel.setVisible(false);
            messageField.setVisible(true);
            firstTime = false;
        }
        idUserLastMessage = -1L;
        modelHBox.setAll(new ArrayList<>());
        chatConversation = chatsNameListView.getSelectionModel().getSelectedItem();

        loadConversationForSpecificSchat();

        List<User> allParticipants = chatConversation.getMembers();
        modelParticipantsToChat.setAll(allParticipants);
        participantsToChatListView.setItems(modelParticipantsToChat);
        if(chatConversation.getMembers().size() > 2)
            showParticipantsIcon.setVisible(true);
        else
            showParticipantsIcon.setVisible(false);
    }

    private void putMessageInScrollPane(String action, Message message){
        String messageText = message.getText();
        HBox hBox = new HBox();
        hBox.setId(String.valueOf(message.getId()));
        if(action.equals("sent")) {
            hBox.setAlignment(Pos.CENTER_RIGHT);
        }
        else{
            hBox.setAlignment(Pos.CENTER_LEFT);
        }
        hBox.setPadding(new Insets(5,5,5,10));
        Text text = new Text(messageText);
        TextFlow textFlow = new TextFlow(text);
        if(action.equals("sent")) {
            textFlow.setStyle("-fx-color: rgb(239,242,255);" +
                    "-fx-background-color: rgb(15,125,242);" +
                    "-fx-background-radius: 20px"
                    );
        }
        else{
            textFlow.setStyle("-fx-background-color: rgb(233,233,235);" +
                    "-fx-background-radius: 20px"
            );
        }
        textFlow.setPadding(new Insets(5,10,5,10));
        if(action.equals("sent")){
            text.setFill(Color.color(0.934, 0.945, 0.996));
        }

        User userThatSendTheLastMessage = message.getFrom();
        if(!userThatSendTheLastMessage.getId().equals(idUserLastMessage)){
            idUserLastMessage = userThatSendTheLastMessage.getId();
            HBox hBoxLabelNameUser = createLabelForUserThatWriteMessages(action,userThatSendTheLastMessage);
            hBoxArrayList.add(hBoxLabelNameUser);
        }

        if(message instanceof ReplyMessage){
            hBox.setId(null); //<------------- nu e posibila reply la reply
            ReplyMessage replyMessage = (ReplyMessage) message;
            HBox hBoxReplyMessage = createReplyMessageForShowGUI(action,
                    replyMessage.getMessage().getText());
            hBoxArrayList.add(hBoxReplyMessage);
            hBox.setPrefHeight(text.prefHeight(100));
        }

        hBox.getChildren().add(textFlow);
        hBoxArrayList.add(hBox);
    }

    private HBox createReplyMessageForShowGUI(String action,String messageText){
        HBox hBox = new HBox();
        if(action.equals("sent")) {
            hBox.setAlignment(Pos.CENTER_RIGHT);
        }
        else{
            hBox.setAlignment(Pos.CENTER_LEFT);
        }
        hBox.setPadding(new Insets(5,5,5,10));
        Text text = new Text(messageText);
        hBox.setPrefHeight(text.prefHeight(100));

        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(5,10,5,10));
        textFlow.setStyle("-fx-background-color: #948c8c;" +
                "-fx-background-radius: 20px"
        );

        hBox.getChildren().add(textFlow);
        return hBox;
    }

    private HBox createLabelForUserThatWriteMessages(String action,User userThatSendTheLastMessage){
        HBox hBox = new HBox();
        Label label = new Label();
        label.setText(userThatSendTheLastMessage.getFirstName() + " "
                + userThatSendTheLastMessage.getLastName());
        label.setFont(new Font("Arial",10));
        label.setTextFill(Color.web("#9c9999"));
        hBox.getChildren().add(label);
        if(action.equals("sent")) {
            hBox.setAlignment(Pos.CENTER_RIGHT);
        }
        else{
            hBox.setAlignment(Pos.CENTER_LEFT);
        }
        hBox.setPadding(new Insets(5,5,5,10));
        return hBox;
    }

    @FXML
    public void deselectAllMessages(){
        discussionListView.getSelectionModel().clearSelection();
    }

    /**
     * check if the selection is good.Clear Selection if the message is a reply one
     * or it was sent by the root
     */
    @FXML
    public void respondToMessage(){
        HBox hBox = discussionListView.getSelectionModel().getSelectedItem();
        if(hBox.getId() == null || hBox.getId().equals("")) {
            discussionListView.getSelectionModel().clearSelection();
            return;
        }
        System.out.println(Long.valueOf(hBox.getId()));
    }

    @FXML
    public void sendMessage(){
        HBox hBox = discussionListView.getSelectionModel().getSelectedItem();
        String text = messageField.getText();
        messageField.clear();
        Long idUserFrom = rootPageUser.getRoot().getId();

        if(hBox != null){
            Long idMessageAggregate = Long.valueOf(hBox.getId());
            try {
                networkController.respondMessage(idUserFrom, idMessageAggregate, text);
            }
            catch (ExceptionBaseClass e){
                MessageAlert.showErrorMessage(displayStage,e.getMessage());
            }
            finally {
                discussionListView.getSelectionModel().clearSelection();
            }
            return;
        }

        List<User> to = idMembersWithoutRootForChat(chatConversation, rootPageUser.getRoot());
        List<Long> idTo = to.stream().map(user -> user.getId()).toList();
        networkController.sendMessages(idUserFrom,idTo,text);

    }

    private List<User> idMembersWithoutRootForChat(Chat chat,User root){
        return chat.getMembers()
                .stream()
                .filter(user -> !user.getId().equals(root.getId()))
                .toList();
    }

    private void handleMessageIcon(){
        if(messageField.getText().equals(""))
            sendMessageIcon.setVisible(false);
        else
            sendMessageIcon.setVisible(true);
    }

    @FXML
    public void handleFriendshipRequestFromMessageController(){
        UsersSearchProcess.sendFriendshipRequest(usersListView, rootPageUser, networkController, displayStage);
    }

    @FXML
    public void switchToFriendshipRequestSceneFromMessageScene(ActionEvent event) throws IOException {
        SceneSwitcher.switchToFriendshipRequestScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void switchToUserViewSceneFromMessageScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToUserViewScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void switchToMessagesSceneFromMessagesScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToMessageScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void switchToReportsSceneFromMessagesScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToReportsScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void switchToEventsViewFromUserScene(ActionEvent event) throws IOException{
        SceneSwitcher.switchToEventsScene(event, getClass(), networkController, rootPageUser, displayStage);
    }

    @FXML
    public void enableAllButtonsAndClearSelection(){
        usersListView.getSelectionModel().clearSelection();

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

    private void handleFilterInUserController(){
        ListViewInitialize.handleFilter(networkController, rootPageUser, searchFriendshipField, modelSearchFriends);
    }

    private void handleFilterSearchUserForNewConversation(){
        ListViewInitialize.handleFilter(networkController, rootPageUser, searchUserToStartConversationField, modelSearchFriends);
    }

    @FXML
    public void closeStartConversationWindow(){
        mainAnchorPane.setEffect(null);
        mainAnchorPane.setDisable(false);
        mainHorizontalBox.setEffect(null);
        mainHorizontalBox.setDisable(false);
        newConversationBox.setVisible(false);
        conversationAnchorPane.setEffect(null);
        conversationAnchorPane.setDisable(false);
    }

    @FXML
    public void proceedWithNewConversation(){
        if(startConversationListView.getSelectionModel().getSelectedItems()
                .equals( FXCollections.observableArrayList() )){
            MessageAlert.showErrorMessage(displayStage, "You have not selected anyone to start a conversation with!");
            closeStartConversationWindow();
            return;
        }
        List<User> members = new ArrayList<>( startConversationListView.getSelectionModel()
                        .getSelectedItems()
                        .stream()
                        .toList() );
        closeStartConversationWindow();
        members.add(rootPageUser.getRoot());

        if(checkIfChatExists(members, rootPageUser.getChatList()))
            return;
        Chat temporaryChat = new Chat(members,new ArrayList<Message>(), new ArrayList<ReplyMessage>());
        modelChatsName.add(temporaryChat);
    }

    private boolean checkIfChatExists(List<User> members, List<Chat> allChats){
        for(Chat chat: allChats){
            boolean checkIfCurrentChatExists = true;
            if(chat.getMembers().size() != members.size())
                continue;
            for (User member : members) {
                if (!chat.getMembers().contains(member)) {
                    checkIfCurrentChatExists = false;
                    break;
                }
            }
            if(checkIfCurrentChatExists)
                return true;
        }
        return false;
    }

    @FXML
    public void openWindowNewConversation(){
        Lighting lighting = new Lighting();
        mainAnchorPane.setEffect(lighting);
        mainAnchorPane.setDisable(true);
        mainHorizontalBox.setEffect(lighting);
        mainHorizontalBox.setDisable(true);
        newConversationBox.setVisible(true);
        conversationAnchorPane.setEffect(lighting);
        conversationAnchorPane.setDisable(true);
    }

    @FXML
    public void setNotificationsListViewOnVisible(){
        notificationsListView.setVisible(true);
        secondPolygon.setVisible(true);
        bellIconView.setFill(Color.BLACK);
    }

    @FXML
    public void closeParticipantsList(){
        participantsChatVBox.setVisible(false);
    }

    @FXML
    public void showChatParticipants(){
        participantsChatVBox.setVisible(true);
    }
}
