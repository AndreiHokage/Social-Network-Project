package socialNetwork.utilitaries;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

public class ListViewInitialize {

    public static void createListViewWithUser(ListView<User> listView, ObservableList<User> modelFriends){
        //Image genericUserImage = new Image("images/emptyProfilePicture.jpg");
        listView.setItems(modelFriends);
        listView.setCellFactory(u -> new ListCell<User>(){
            private ImageView imageView = new ImageView();
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                    setGraphic(null);
                }
                else{
                    String idUser = String.valueOf(item.getId());
                    String path = "images/u" + idUser + ".jpg";
                    try {
                        Image genericUserImage = new Image(path);
                        imageView.setImage(genericUserImage);
                    }catch (IllegalArgumentException e){
                        Image genericUserImage = new Image("images/emptyProfilePicture.jpg");
                        imageView.setImage(genericUserImage);
                    }

                    imageView.setFitHeight(50);
                    imageView.setFitWidth(50);
                    imageView.setPreserveRatio(true);
                    setText(item.getUsername());
                    setGraphic(imageView);
                }
            }
        });
    }

    public static void createListViewWithMessages(ListView<Message> listView, ObservableList<Message> modelMessages){
        listView.setItems(modelMessages);
        listView.setCellFactory(u -> new ListCell<Message>(){
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                }
                else{
                    setText(item.getText());
                }
            }
        });
    }

    public static void createListViewWithEvent(ListView<EventPublic> listView, ObservableList<EventPublic> modelEvents){
        listView.setItems(modelEvents);
        listView.setCellFactory(u -> new ListCell<EventPublic>(){
            @Override
            protected void updateItem(EventPublic item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                }
                else{
                    setText(item.getName() + " - " + item.getDescription() + " - " + "taking place at " + item.getDate());
                }
            }
        });
    }

    public static void createListViewWithNotification(ListView<EventPublic> listView, ObservableList<EventPublic> modelNotifications){
        listView.setItems(modelNotifications);
        listView.setCellFactory(u -> new ListCell<EventPublic>(){
            @Override
            protected void updateItem(EventPublic item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                }
                else{
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime eventTime = item.getDate();
                    Duration duration = Duration.between(now, eventTime);
                    long hoursDifference = Math.abs(duration.toHours());
                    long days = hoursDifference / 24;
                    long hours = (hoursDifference - days*24);
                    setText("" + days + "d " + hours + "h until " + item.getName() + " begins");
                }
            }
        });
    }

    public static void createListViewWithDtoEvent(ListView<DTOEventPublicUser> listView,
                                                  ObservableList<DTOEventPublicUser> modelEvents,
                                                  PageUser rootPage,
                                                  Stage displayStage,
                                                  FontAwesomeIconView bellIcon){
        listView.setItems(modelEvents);
        listView.setCellFactory(u -> new ListCell<DTOEventPublicUser>(){

            @Override
            protected void updateItem(DTOEventPublicUser item, boolean empty){
                super.updateItem(item, empty);
                if(item == null){
                    setGraphic(null);
                    setText("");
                }
                else{
                     HBox hBox = new HBox();
                     hBox.setAlignment(Pos.CENTER_LEFT);
                     Button myButton = new Button("");
                     FontAwesomeIconView fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.BELL);
                     myButton.setGraphic(fontAwesomeIconView);
                     if(item.getReceivedNotification().equals(EventNotification.APPROVE))
                         myButton.setText("On");
                     else
                         myButton.setText("Off");
                     myButton.setOnAction(evt -> {
                         if(myButton.getText().equals("On")) {
                             rootPage.getNetworkController().stopNotificationEventPublic(item.getIdUser(), item.getIdEventPublic());
                             myButton.setText("Off");
                         }
                         else{
                             rootPage.getNetworkController().turnOnNotificationsEventPublic(item.getIdUser(), item.getIdEventPublic());
                             myButton.setText("On");
                             displayStage.setUserData("unseen");
                             bellIcon.setFill(Color.valueOf("#d53939"));
                         }
                     });
                    EventPublic publicEvent =
                            rootPage.getNetworkController().getPublicEventWithId(item.getIdEventPublic()).get();
                    Label label = new Label(publicEvent.getName() + " - " + publicEvent.getDescription() + " - taking place at " + publicEvent.getDate());
                    hBox.getChildren().addAll(label, myButton);
                    setGraphic(hBox);
                }
            }
        });
    }

    public static void createComboBoxWithFriends(ComboBox<User> comboBox, ObservableList<User> modelFriends){
        comboBox.setItems(modelFriends);
        comboBox.setCellFactory(c -> new ListCell<User>(){
            @Override
            protected void updateItem(User item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                }
                else{
                    setText(item.getUsername());
                }
            }
        });
    }

    public static void handleFilter(NetworkController networkController, PageUser rootPage, TextField searchFriendshipField,
                                    ObservableList<User> modelSearchFriends){
        User mainUser = rootPage.getRoot();
        Predicate<User> nameOfUserPredicate = u -> u.getUsername()
                .startsWith(searchFriendshipField.getText());
        List<User> userListWithoutMainUser = networkController.getAllUsers()
                .stream()
                .filter(x -> !x.getId().equals(mainUser.getId()))
                .toList();
        modelSearchFriends.setAll(userListWithoutMainUser.stream()
                .filter(nameOfUserPredicate)
                .toList());
    }

    public static void createListViewWithChats(ListView<Chat> listView, ObservableList<Chat> modelChats,User root){
        Image genericUserImage = new Image("images/emptyProfilePicture.jpg");
        listView.setItems(modelChats);
        listView.setCellFactory(u -> new ListCell<Chat>(){
            private ImageView imageView = new ImageView();
            @Override
            protected void updateItem(Chat item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                    setGraphic(null);
                }
                else{

                    imageView.setFitHeight(50);
                    imageView.setFitWidth(50);
                    imageView.setPreserveRatio(true);

                    if(item.getMembers().size() == 2){
                        User otherGuy = item.getMembers().get(0);
                        if(otherGuy.getId().equals(root.getId()))
                            otherGuy = item.getMembers().get(1);
                        setText(otherGuy.getUsername());

                        String idOtherGuy = String.valueOf(otherGuy.getId());
                        String path = "images/u" + idOtherGuy + ".jpg";
                        try {
                            Image genericUserImage = new Image(path);
                            imageView.setImage(genericUserImage);
                        }catch (IllegalArgumentException e){
                            Image genericUserImage = new Image("images/emptyProfilePicture.jpg");
                            imageView.setImage(genericUserImage);
                        }

                    }else{
                        Image genericUserImage = new Image("images/emptyProfilePicture.jpg");
                        imageView.setImage(genericUserImage);
                        String nameAllMembers = item.getMembers()
                                .stream()
                                .filter(user -> !user.getId().equals(root.getId()))
                                .map(user -> user.getUsername())
                                .reduce("",(x,y) -> x + ", " + y);
                        String editNameAllMembers = nameAllMembers.substring(2);
                        setText(editNameAllMembers);
                    }
                    setGraphic(imageView);
                }
            }
        });
    }
}
