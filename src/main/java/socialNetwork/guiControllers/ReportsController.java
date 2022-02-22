package socialNetwork.guiControllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.*;
import socialNetwork.utilitaries.ListViewInitialize;
import socialNetwork.utilitaries.MessageAlert;
import socialNetwork.utilitaries.SceneSwitcher;
import socialNetwork.utilitaries.UsersSearchProcess;
import socialNetwork.utilitaries.events.EventPublicChangeEvent;
import socialNetwork.utilitaries.events.FriendshipChangeEvent;
import socialNetwork.utilitaries.observer.Observer;
import socialNetwork.utilitaries.events.Event;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ReportsController implements Observer<Event> {
    ObservableList<User> modelSearchFriends = FXCollections.observableArrayList();
    ObservableList<User> modelFriends = FXCollections.observableArrayList();
    ObservableList<Message> modelMessages = FXCollections.observableArrayList();
    ObservableList<RequestInvitationGUIDTO> modelFriendships = FXCollections.observableArrayList();
    ObservableList<Chat> modelChats = FXCollections.observableArrayList();
    ObservableList<EventPublic> modelNotifications = FXCollections.observableArrayList();

    FileChooser fileChooser = new FileChooser();

    @FXML
    AnchorPane mainAnchorPane;
    @FXML
    ListView<User> usersListView;
    @FXML
    Button addFriendshipButton;
    @FXML
    Button friendRequestButton;
    @FXML
    TextField searchFriendshipField;
    @FXML
    Polygon triangleAuxiliaryLabel;
    @FXML
    Button messagesReceivedPdfButton;
    @FXML
    Button userActivitiesPdfButton;
    @FXML
    DatePicker firstDatePicker;
    @FXML
    DatePicker secondDatePicker;
    @FXML
    Button messagesReceivedTabButton;
    @FXML
    Button userActivitiesTabButton;
    @FXML
    ComboBox<User> friendComboBox;
    @FXML
    Label chooseFriendLabel;
    @FXML
    Label chooseFirstDateLabel;
    @FXML
    Label chooseSecondDateLabel;
    @FXML
    Button userActivitiesButton;
    @FXML
    Button messagesReceivedButton;
    @FXML
    Label specifyTimeLabel;
    @FXML
    Label specifyFriendLabel;
    @FXML
    ListView<Message> messagesListView;
    @FXML
    ListView<EventPublic> notificationsListView;
    @FXML
    VBox mainVerticalBox;
    @FXML
    VBox userActivitiesNewFriendshipsVBox;
    @FXML
    ListView<RequestInvitationGUIDTO> newFriendsListView;
    @FXML
    Label userActivitiesFriendshipsIntervalLabel;
    @FXML
    ListView<Chat> messagesNumberFromChatsListView;
    @FXML
    VBox userActivitiesMessagesReceivedVBox;
    @FXML
    Label userActivitiesMessagesReceivedIntervalLabel;
    @FXML
    Polygon secondPolygon;
    @FXML
    FontAwesomeIconView bellIconView;

    NetworkController networkController;
    PageUser rootPage;
    Stage displayStage;

    private void refreshPage(){
        RefreshPageUser refreshPageUser = new RefreshPageUser(false,false,false);
        rootPage.refresh(rootPage.getRoot().getUsername(),refreshPageUser);
    }

    public void setNetworkController(Stage primaryStage, NetworkController service, PageUser rootPage){
        this.networkController = service;
        networkController.getNetworkService().addObserver(this);
        this.displayStage = primaryStage;
        this.rootPage = rootPage;
        refreshPage();
        ListViewInitialize.createListViewWithNotification(notificationsListView, modelNotifications);
        initModelFriends();
        initModelNotifications();
        if(notificationsListView.getItems().size() != 0)
            bellIconView.setFill(Color.valueOf("#d53939"));
        if(displayStage.getUserData()!=null && displayStage.getUserData().equals("seen"))
            bellIconView.setFill(Color.valueOf("#000000"));
    }

    private void initModelFriends(){
        List<User> friends = rootPage.getFriendList();
        modelFriends.setAll(friends);
    }

    private void initModelNotifications(){
        List<EventPublic> notificationEvents =
                rootPage.getNetworkController().filterAllEventPublicForNotification
                        (rootPage.getRoot().getId(), 30L);
        modelNotifications.setAll(notificationEvents);
    }

    private void initializeListView(ListView<RequestInvitationGUIDTO> listView,
                                    ObservableList<RequestInvitationGUIDTO> modelFriendships,
                                    LocalDate firstDate, LocalDate secondDate) {
        User mainUser = rootPage.getRoot();
        List<RequestInvitationGUIDTO> listOfApprovedFriends = new ArrayList<RequestInvitationGUIDTO>();
        networkController.findAllApprovedFriendshipsForUser(mainUser.getId())
                .forEach((userOptional, time) -> {
                    User user = userOptional.get();
                    listOfApprovedFriends.add(new RequestInvitationGUIDTO(user.getId(), user.getFirstName(), user.getLastName(),
                            time, InvitationStage.APPROVED));
                });
        Predicate<RequestInvitationGUIDTO> afterCertainDate = r -> (r.getLocalDateTime().toLocalDate().isEqual(firstDate) ||
                r.getLocalDateTime().toLocalDate().isAfter(firstDate));
        Predicate<RequestInvitationGUIDTO> beforeCertainDate = r -> (r.getLocalDateTime().toLocalDate().isEqual(secondDate) ||
                r.getLocalDateTime().toLocalDate().isBefore(secondDate));
        List<RequestInvitationGUIDTO> filteredList = listOfApprovedFriends
                .stream()
                .filter(afterCertainDate.and(beforeCertainDate))
                .toList();
        modelFriendships.setAll(filteredList);
        listView.setItems(modelFriendships);
        listView.setCellFactory(r -> new ListCell<RequestInvitationGUIDTO>(){
            @Override
            protected void updateItem(RequestInvitationGUIDTO item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                }
                else{
                    setText("You have become friends with " + item.getFirstName() + " " + item.getLastName() + " on "
                            + item.getLocalDateTime().toLocalDate());
                }
            }
        });
    }

    private void initializeMessagesStatistics(ListView<Chat> chats, ObservableList<Chat> modelChats,
                                              LocalDate firstDate, LocalDate secondDate){
        modelChats.setAll(rootPage.getChatList());
        chats.setItems(modelChats);
        chats.setCellFactory(r -> new ListCell<Chat>(){
            @Override
            protected void updateItem(Chat item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                }
                else{
                    setText(editListOfMessagesNumberFromChats(item, firstDate, secondDate));
                }
            }
        });
    }

    private String editListOfMessagesNumberFromChats(Chat item, LocalDate firstDate, LocalDate secondDate){
        String chatName;
        if(item.getMembers().size() == 2){
            User otherGuy = item.getMembers().get(0);
            if(otherGuy.getId().equals(rootPage.getRoot().getId()))
                otherGuy = item.getMembers().get(1);
            chatName = otherGuy.getUsername();
        }else {
            String nameAllMembers = item.getMembers()
                    .stream()
                    .filter(user -> !user.getId().equals(rootPage.getRoot().getId()))
                    .map(user -> user.getUsername())
                    .reduce("", (x, y) -> x + ", " + y);
            chatName = nameAllMembers.substring(2);
        }
        return "" + getNumberOfReceivedMessagesFromAChat(item, firstDate, secondDate) + " messages received from the "
                + chatName + " chat";
    }

    @FXML
    public void initialize(){
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        ListViewInitialize.createListViewWithUser(usersListView, modelSearchFriends);
        ListViewInitialize.createComboBoxWithFriends(friendComboBox, modelFriends);
        searchFriendshipField.textProperty().addListener(o -> handleFilterInUserController());
        friendComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                if (user == null){
                    return null;
                } else {
                    return user.getUsername();
                }
            }

            @Override
            public User fromString(String username) {
                return null;
            }
        });
    }

    @Override
    public void update(Event event) {
        if(event instanceof EventPublicChangeEvent) {
            initModelNotifications();
        }
        if(event instanceof FriendshipChangeEvent)
            initModelFriends();
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
    public void disableButtonsWhenSendingRequest(){
        if(usersListView.getSelectionModel().getSelectedItem() != null){
            addFriendshipButton.setDisable(false);
            messagesReceivedPdfButton.setDisable(true);
            userActivitiesPdfButton.setDisable(true);
            userActivitiesButton.setDisable(true);
            messagesReceivedButton.setDisable(true);
        }
    }

    @FXML
    public void enableAllButtonsAndClearSelection(){
        if(chooseFriendLabel.isVisible())
            changeTabToMessagesReceived();
        else
            changeTabToUserActivities();
    }

    @FXML
    public void changeTabToUserActivities(){
        messagesReceivedTabButton.setStyle("");
        userActivitiesTabButton.setStyle("-fx-font-weight: bold");
        addFriendshipButton.setDisable(true);
        messagesReceivedPdfButton.setDisable(true);
        userActivitiesPdfButton.setDisable(true);
        chooseFriendLabel.setVisible(false);
        friendComboBox.setVisible(false);
        userActivitiesButton.setDisable(false);
        messagesReceivedButton.setDisable(true);
        firstDatePicker.setVisible(true);
        secondDatePicker.setVisible(true);
        chooseFirstDateLabel.setVisible(true);
        chooseSecondDateLabel.setVisible(true);
    }

    @FXML
    public void changeTabToMessagesReceived(){
        messagesReceivedTabButton.setStyle("-fx-font-weight: bold");
        userActivitiesTabButton.setStyle("");
        addFriendshipButton.setDisable(true);
        userActivitiesPdfButton.setDisable(true);
        messagesReceivedPdfButton.setDisable(true);
        chooseFriendLabel.setVisible(true);
        friendComboBox.setVisible(true);
        userActivitiesButton.setDisable(true);
        messagesReceivedButton.setDisable(false);
        firstDatePicker.setVisible(true);
        secondDatePicker.setVisible(true);
        chooseFirstDateLabel.setVisible(true);
        chooseSecondDateLabel.setVisible(true);
    }

    @FXML
    public void backToUserActivities(){
        userActivitiesNewFriendshipsVBox.setVisible(false);
        userActivitiesMessagesReceivedVBox.setVisible(false);
        firstDatePicker.setVisible(true);
        secondDatePicker.setVisible(true);
        chooseFirstDateLabel.setVisible(true);
        chooseSecondDateLabel.setVisible(true);
        firstDatePicker.setValue(null);
        secondDatePicker.setValue(null);
        userActivitiesButton.setDisable(false);
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
    public void generatePdfUserActivities() throws IOException {
        List<RequestInvitationGUIDTO> listOfNewFriends = newFriendsListView.getItems();
        float fontSize = 14;
        float fontHeight = fontSize;
        float leading = 20;

        fileChooser.setTitle("Save pdf");
        fileChooser.setInitialFileName("userActivities");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("pdf","*.pdf"));
        File file = fileChooser.showSaveDialog(displayStage);

        if(file == null)
            return;

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(PDType1Font.TIMES_ROMAN, fontSize);

        float yCoordinate = page.getCropBox().getUpperRightY() - 30;
        float startX = page.getCropBox().getLowerLeftX() + 30;
        float endX = page.getCropBox().getUpperRightX() - 30;

        contentStream.beginText();
        contentStream.newLineAtOffset(startX, yCoordinate);
        contentStream.showText(userActivitiesFriendshipsIntervalLabel.getText());
        yCoordinate -= fontHeight;
        contentStream.endText();

        contentStream.moveTo(startX, yCoordinate);
        contentStream.lineTo(endX, yCoordinate);
        contentStream.stroke();
        yCoordinate -= leading;

        for(RequestInvitationGUIDTO request: listOfNewFriends){
            if(yCoordinate - fontHeight < 50){
                PDPage anotherPage = new PDPage();
                contentStream.close();
                document.addPage(anotherPage);
                contentStream = new PDPageContentStream(document, anotherPage);
                contentStream.setFont(PDType1Font.TIMES_ROMAN, fontSize);
                yCoordinate = page.getCropBox().getUpperRightY() - 30;
            }
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, yCoordinate);
            contentStream.showText("You have become friends with " + request.getFirstName() + " " + request.getLastName()
                            + " on " + request.getLocalDateTime().toLocalDate());
            yCoordinate -= fontHeight;
            contentStream.endText();
        }

        contentStream.close();

        List<Chat> listOfChats = messagesNumberFromChatsListView.getItems();

        PDPage messagesPage = new PDPage();
        document.addPage(messagesPage);
        contentStream = new PDPageContentStream(document, messagesPage);
        contentStream.setFont(PDType1Font.TIMES_ROMAN, fontSize);
        yCoordinate = page.getCropBox().getUpperRightY() - 30;

        contentStream.beginText();
        contentStream.newLineAtOffset(startX, yCoordinate);
        contentStream.showText(userActivitiesMessagesReceivedIntervalLabel.getText());
        yCoordinate -= fontHeight;
        contentStream.endText();

        contentStream.moveTo(startX, yCoordinate);
        contentStream.lineTo(endX, yCoordinate);
        contentStream.stroke();
        yCoordinate -= leading;

        LocalDate firstDate = firstDatePicker.getValue();
        LocalDate secondDate = secondDatePicker.getValue();
        for(Chat chat: listOfChats){
            if(yCoordinate - fontHeight < 50){
                PDPage anotherPage = new PDPage();
                contentStream.close();
                document.addPage(anotherPage);
                contentStream = new PDPageContentStream(document, anotherPage);
                contentStream.setFont(PDType1Font.TIMES_ROMAN, fontSize);
                yCoordinate = page.getCropBox().getUpperRightY() - 30;
            }
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, yCoordinate);
            contentStream.showText(editListOfMessagesNumberFromChats(chat, firstDate, secondDate));
            yCoordinate -= fontHeight;
            contentStream.endText();
        }

        contentStream.close();

        document.save(file.getAbsolutePath());

        MessageAlert.showInformationMessage(displayStage, "You have successfully created a pdf file with the messages received");
    }

    @FXML
    public void generatePdfMessagesWithFriend() throws IOException {
        List<Message> listOfMessages = messagesListView.getItems();
        float fontSize = 14;
        float fontHeight = fontSize;
        float leading = 20;

        fileChooser.setTitle("Save pdf");
        fileChooser.setInitialFileName("messagesReceived");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("pdf","*.pdf"));
        File file = fileChooser.showSaveDialog(displayStage);

        if(file == null)
            return;

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(PDType1Font.TIMES_ROMAN, fontSize);

        float yCoordinate = page.getCropBox().getUpperRightY() - 30;
        float startX = page.getCropBox().getLowerLeftX() + 30;
        float endX = page.getCropBox().getUpperRightX() - 30;

        contentStream.beginText();
        contentStream.newLineAtOffset(startX, yCoordinate);
        contentStream.showText(specifyFriendLabel.getText());
        yCoordinate -= fontHeight;
        contentStream.newLineAtOffset(0, -leading);
        yCoordinate -= leading;
        contentStream.showText(specifyTimeLabel.getText());
        yCoordinate -= fontHeight;
        contentStream.endText();

        contentStream.moveTo(startX, yCoordinate);
        contentStream.lineTo(endX, yCoordinate);
        contentStream.stroke();
        yCoordinate -= leading;

        for(Message message: listOfMessages){
            if(yCoordinate - fontHeight < 50){
                PDPage anotherPage = new PDPage();
                contentStream.close();
                document.addPage(anotherPage);
                contentStream = new PDPageContentStream(document, anotherPage);
                contentStream.setFont(PDType1Font.TIMES_ROMAN, fontSize);
                yCoordinate = page.getCropBox().getUpperRightY() - 30;
            }
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, yCoordinate);
            contentStream.showText(message.getText());
            yCoordinate -= fontHeight;
            contentStream.endText();
        }

        contentStream.close();
        document.save(file.getAbsolutePath());

        MessageAlert.showInformationMessage(displayStage, "You have successfully created a pdf file with the messages received");
    }

    @FXML
    public void closeMessagesWindow(){
        mainVerticalBox.setVisible(false);
        firstDatePicker.setVisible(true);
        secondDatePicker.setVisible(true);
        chooseFirstDateLabel.setVisible(true);
        chooseSecondDateLabel.setVisible(true);
        friendComboBox.setVisible(true);
        chooseFriendLabel.setVisible(true);
        firstDatePicker.setValue(null);
        secondDatePicker.setValue(null);
        friendComboBox.setValue(null);
        messagesReceivedButton.setDisable(false);
    }

    @FXML
    public void generateMessagesWithFriend(){
        LocalDate firstDate = firstDatePicker.getValue();
        LocalDate secondDate = secondDatePicker.getValue();
        if(firstDate == null || secondDate == null){
            MessageAlert.showErrorMessage(displayStage, "You have to set the 2 dates in order to see the messages");
            return;
        }
        User friendUser = friendComboBox.getValue();
        if(friendUser == null){
            MessageAlert.showErrorMessage(displayStage, "No friend has been selected");
            return;
        }

        if(firstDate.isAfter(secondDate)){
            MessageAlert.showErrorMessage(displayStage, "First date must be before the second one");
            return;
        }

        Chat chatWithFriend = getChatWithFriend(friendUser);
        if(chatWithFriend == null){
            MessageAlert.showInformationMessage(displayStage, "You have no conversation with this friend");
            return;
        }

        mainVerticalBox.setVisible(true);
        firstDatePicker.setVisible(false);
        secondDatePicker.setVisible(false);
        chooseFirstDateLabel.setVisible(false);
        chooseSecondDateLabel.setVisible(false);
        friendComboBox.setVisible(false);
        chooseFriendLabel.setVisible(false);

        specifyFriendLabel.setText(friendUser.getUsername());
        specifyTimeLabel.setText(firstDate + "   " + secondDate);

        List<Message> messageList = chatWithFriend.getMessageList();
        List<ReplyMessage> replyMessageList = chatWithFriend.getReplyMessageList();
        List<Message> replyMessageJustText = replyMessageList
                .stream()
                .map(Message.class::cast)
                .toList();
        List<Message> fullListOfMessages = new ArrayList<Message>(messageList);
        fullListOfMessages.addAll(replyMessageJustText);
        Predicate<Message> filterDateAfterFirstDate = message -> message.getDate().toLocalDate().isEqual(firstDate) ||
                message.getDate().toLocalDate().isAfter(firstDate);
        Predicate<Message> filterDateBeforeSecondDate = message -> message.getDate().toLocalDate().isEqual(secondDate)
                || message.getDate().toLocalDate().isBefore(secondDate);
        Predicate<Message> filterPersonWhoSends = message -> !message.getFrom().getId().equals(rootPage.getRoot().getId());
        List<Message> availableListOfMessages = fullListOfMessages
                .stream()
                .filter(filterDateAfterFirstDate.and(filterDateBeforeSecondDate).and(filterPersonWhoSends))
                .sorted((Message m1,Message m2) -> {
                    return m1.getDate().compareTo(m2.getDate());
                })
                .toList();

        modelMessages.setAll(availableListOfMessages);
        ListViewInitialize.createListViewWithMessages(messagesListView, modelMessages);

        messagesReceivedButton.setDisable(true);
        messagesReceivedPdfButton.setDisable(false);
    }

    private Chat getChatWithFriend(User friendUser){
        List<Chat> findChat = rootPage.getChatList()
                .stream()
                .filter(chat -> chat.getMembers().size() == 2 && chat.getMembers().contains(friendUser))
                .toList();
        if(findChat.size() == 0)
            return null;
        return findChat.get(0);
    }

    @FXML
    public void generateUserActivities(){
        LocalDate firstDate = firstDatePicker.getValue();
        LocalDate secondDate = secondDatePicker.getValue();
        if(firstDate == null || secondDate == null){
            MessageAlert.showErrorMessage(displayStage, "You have to set the 2 dates in order to see the messages");
            return;
        }
        if(firstDate.isAfter(secondDate)){
            MessageAlert.showErrorMessage(displayStage, "First date must be before the second one");
            return;
        }
        userActivitiesNewFriendshipsVBox.setVisible(true);
        firstDatePicker.setVisible(false);
        secondDatePicker.setVisible(false);
        chooseFirstDateLabel.setVisible(false);
        chooseSecondDateLabel.setVisible(false);

        userActivitiesFriendshipsIntervalLabel.setText("Friends you made during " + firstDate + " ... " + secondDate);
        userActivitiesMessagesReceivedIntervalLabel.setText("Messages received during " + firstDate + " ... " + secondDate);

        initializeListView(newFriendsListView, modelFriendships, firstDate, secondDate);
        initializeMessagesStatistics(messagesNumberFromChatsListView, modelChats, firstDate, secondDate);

        userActivitiesMessagesReceivedVBox.setVisible(false);
        userActivitiesButton.setDisable(true);
        userActivitiesPdfButton.setDisable(false);
    }

    private long getNumberOfReceivedMessagesFromAChat(Chat chat, LocalDate firstDate, LocalDate secondDate){
        Predicate<Message> messagesReceived = m -> !m.getFrom().getId().equals(rootPage.getRoot().getId());
        Predicate<Message> dateIsBefore = m -> (m.getDate().toLocalDate().isEqual(secondDate) ||
                m.getDate().toLocalDate().isBefore(secondDate));
        Predicate<Message> dateIsAfter = m -> (m.getDate().toLocalDate().isEqual(firstDate) ||
                m.getDate().toLocalDate().isAfter(firstDate));
        Predicate<Message> checkDate = dateIsBefore.and(dateIsAfter);
        long messages = chat.getMessageList()
                .stream()
                .filter(messagesReceived.and(checkDate))
                .count();
        long replies = chat.getReplyMessageList()
                .stream()
                .filter(messagesReceived.and(checkDate))
                .count();
        return messages + replies;
    }

    @FXML
    public void fromFriendshipsToMessages(){
        userActivitiesNewFriendshipsVBox.setVisible(false);
        userActivitiesMessagesReceivedVBox.setVisible(true);
    }

    @FXML
    private void fromMessagesToFriendships(){
        userActivitiesNewFriendshipsVBox.setVisible(true);
        userActivitiesMessagesReceivedVBox.setVisible(false);
    }

    @FXML
    public void sortMessages(){
        LocalDate date1 = firstDatePicker.getValue();
        LocalDate date2 = secondDatePicker.getValue();
        List<Chat> chats = rootPage.getChatList()
                .stream()
                .sorted((chat1, chat2) -> {
                    long firstChatMessagesReceived = getNumberOfReceivedMessagesFromAChat(chat1, date1, date2);
                    long secondChatMessagesReceived = getNumberOfReceivedMessagesFromAChat(chat2, date1, date2);
                    return Long.compare(firstChatMessagesReceived, secondChatMessagesReceived);
                })
                .toList();
        modelChats.setAll(chats);
        messagesNumberFromChatsListView.setItems(modelChats);
    }

    @FXML
    public void setNotificationsListViewOnVisible(){
        notificationsListView.setVisible(true);
        secondPolygon.setVisible(true);
        bellIconView.setFill(Color.BLACK);
    }
}
