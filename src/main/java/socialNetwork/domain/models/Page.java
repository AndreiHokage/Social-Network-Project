package socialNetwork.domain.models;

import socialNetwork.controllers.NetworkController;
import socialNetwork.utilitaries.events.*;
import socialNetwork.utilitaries.observer.Observer;

import java.util.*;
import java.util.function.Predicate;

public class Page implements Observer<Event> {

    private User root;
    private List<User> friendList;
    private List<FriendRequest> friendRequestList;
    private Map< List<User> , Chat > chatMap;
    private NetworkController networkController;

    private void subscribePageToSubjects(){
        networkController.getNetworkService().addObserver(this);
        networkController.getFriendRequestService().addObserver(this);
        networkController.getMessageService().addObserver(this);
    }

    public Page(User root, List<User> friendList,
                List<FriendRequest> friendRequestList, List<Chat> chatList,
                NetworkController networkController) {
        this.root = root;
        this.friendList = friendList;
        this.friendRequestList = friendRequestList;
        chatMap = new HashMap<>();
        chatList.forEach(chat -> chatMap.put(chat.getMembers(),chat) );
        this.networkController = networkController;
        subscribePageToSubjects();
    }

    public User getRoot() {
        return root;
    }

    public void setRoot(User root) {
        this.root = root;
    }

    public NetworkController getNetworkController() {
        return networkController;
    }

    public void setNetworkController(NetworkController networkController) {
        this.networkController = networkController;
    }

    public List<User> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<User> friendList) {
        this.friendList = friendList;
    }

    public List<FriendRequest> getFriendRequestList() {
        return friendRequestList;
    }

    public void setFriendRequestList(List<FriendRequest> friendRequestList) {
        this.friendRequestList = friendRequestList;
    }

    public List<Chat> getChatMap() {
        return chatMap.entrySet()
                .stream()
                .map(x -> x.getValue())
                .toList();
    }

    public void setChatMap(List<Chat> chatList) {
        chatMap.clear();
        chatList.forEach( chat -> chatMap.put(chat.getMembers() , chat));
    }

    @Override
    public String toString() {
        return "Page{" +
                "firstName='" + root.getFirstName() + '\'' +
                ", lastName='" + root.getLastName() + '\'' +
                ", friendList=" + friendList +
                ", friendRequestList=" + friendRequestList +
                ", chatList=" + chatMap +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return Objects.equals(root, page.root) && Objects.equals(friendList, page.friendList) && Objects.equals(friendRequestList, page.friendRequestList) && Objects.equals(chatMap, page.chatMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, friendList, friendRequestList, chatMap);
    }

    public List<FriendRequest> getAllFriendRequestThatRootSends(){
        return friendRequestList.stream()
                .filter(friendRequest -> friendRequest.getFromUserID().equals(root.getId()))
                .toList();
    }

    public List<FriendRequest> getAllFriendRequestThatRootReceives(){
        return friendRequestList.stream()
                .filter(friendRequest -> friendRequest.getToUserID().equals(root.getId()))
                .toList();
    }

    @Override
    public void update(Event event) {
        if(event instanceof FriendRequestChangeEvent) {
            friendRequestList = networkController.getAllFriendRequestForSpecifiedUser(root.getId());
            if(!event.getType().equals(FriendRequestChangeEventType.PENDING))
                friendList = networkController.getAllFriendshipForSpecifiedUser(root.getId());
        }

        if(event instanceof FriendshipChangeEvent){
            friendRequestList = networkController.getAllFriendRequestForSpecifiedUser(root.getId());
            friendList = networkController.getAllFriendshipForSpecifiedUser(root.getId());
        }

        if(event instanceof MessageChangeEvent){
            MessageChangeEvent eventUpdate = (MessageChangeEvent) event;
            MessageDTO data = eventUpdate.getData();

            Message mainMessage = data.getMainMessage();
            List<User> members = mainMessage.getTo();
            members.add(mainMessage.getFrom());
            List<User> sortedMembers =  members.stream()
                    .sorted((User userX,User userY) -> {
                        return userX.getId().compareTo(userY.getId());
                    })
                    .toList();

            if(chatMap.containsKey(sortedMembers)) {
                if(eventUpdate.getType().equals(MessageChangeEventType.SEND))
                    chatMap.get(sortedMembers).addMessage(mainMessage);
                else if(eventUpdate.getType().equals(MessageChangeEventType.RESPOND)){
                    ReplyMessage replyMessage = new ReplyMessage(mainMessage.getFrom(),
                            mainMessage.getTo(), mainMessage.getText(),data.getMessageToRespondTo());
                    chatMap.get(sortedMembers).addReplyMessage(replyMessage);
                }
            }
            else { //here I will create a new Chat
                if(eventUpdate.getType().equals(MessageChangeEventType.SEND))
                    chatMap.put(sortedMembers,new Chat(sortedMembers,Arrays.asList(mainMessage),
                            new ArrayList<ReplyMessage>()));
            }
        }
    }
}
