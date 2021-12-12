package socialNetwork.controllers;

import socialNetwork.domain.models.*;
import socialNetwork.exceptions.LogInException;
import socialNetwork.service.AuthentificationService;
import socialNetwork.service.MessageService;
import socialNetwork.service.NetworkService;
import socialNetwork.service.UserService;
import socialNetwork.utilitaries.events.ChangeEventType;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.events.FriendshipChangeEvent;
import socialNetwork.utilitaries.observer.Observable;
import socialNetwork.utilitaries.observer.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * controller between socialNetwork.gui and business layer
 */
public class NetworkController implements Observable <Event> {
    private UserService userService;
    private NetworkService networkService;
    private MessageService messageService;
    private AuthentificationService authentificationService;
    private List<Observer<Event>> observers = new ArrayList<>();

    /**
     * constructor - creates a controller that accesses the given services
     * @param userService - service for users
     * @param networkService - service for friendships
     */
    public NetworkController(UserService userService, NetworkService networkService,
                             MessageService messageService, AuthentificationService authentificationService) {
        this.userService = userService;
        this.networkService = networkService;
        this.messageService = messageService;
        this.authentificationService = authentificationService;
    }

    /**
     * adds a new user
     * @param firstName - String
     * @param lastName - String
     * @return - empty Optional if the user was added, Optional containing the existing user otherwise
     */

    public Optional<User> addUser(String firstName, String lastName ,String username){
        return userService.addUserService(firstName, lastName ,username);
    }

    /**
     * update user
     * @param id - Long
     * @param firstName - String
     * @param lastName - String
     * @return - old value Optional if the user was updated, empty Optional otherwise
     */
    public Optional<User> updateUser(Long id, String firstName, String lastName, String username){
        return userService.updateUserService(id, firstName, lastName ,username);
    }

    /**
     * removes the user with the given id and his friendships with other users
     * @param id - int
     * @return Optional with the user that was removed, empty optional if user did not exist
     */
    public Optional<User> removeUser(Long id){
        List<Message> messageList = messageService.allMessagesUserAppearsIn(id);
        Optional<User> removeUser = userService.removeUserService(id);
        messageList.forEach(message -> messageService.removeMessageService(message.getId()));
        return removeUser;
    }

    /**
     * finds user with given id
     * @param id - identifier of user we search for
     * @return - Optional containing user that has given id if exists, empty optional otherwise
     */
    public Optional<User> findUser(Long id){
        return userService.findUserService(id);
    }

    /**
     * adds a friendship between 2 users
     * @param firstUserId - id first user
     * @param secondUserId - id second user
     * @return - empty Optional if the friendship was added, empty if friendship already exists
     * @throws socialNetwork.exceptions.InvalidEntityException - one of users not found
     */
    public Optional<Friendship> addFriendship(Long firstUserId, Long secondUserId, LocalDateTime date){
        return networkService.addFriendshipService(firstUserId, secondUserId, date);
    }

    /**
     * removes the friendship between 2 users
     * @param firstUserId - id first user
     * @param secondUserId - id second user
     * @return Optional containing removed friendship, empty Optional if users do not exist
     */
    public Optional<Friendship> removeFriendship(Long firstUserId, Long secondUserId){
        Optional<Friendship> removedFriendship =
                networkService.removeFriendshipService(firstUserId, secondUserId);
        if(removedFriendship.isEmpty())
            return Optional.empty();
        notifyObservers(new FriendshipChangeEvent(ChangeEventType.DELETE, removedFriendship.get()));
        return removedFriendship;
    }

    /**
     * finds a friendships after id of 2 users
     * @param firstUserId - Long
     * @param secondUserId - Long
     * @return Optional containing friendship if exists, empty optional otherwise
     */
    public Optional<Friendship> findFriendship(Long firstUserId, Long secondUserId){
        return networkService.findFriendshipService(firstUserId, secondUserId);
    }

    /**
     * @return - a list of users and all their friends are set
     */
    public List<User> getAllUsersAndTheirFriends(){
        return networkService.getAllUsersAndTheirFriendsService();
    }

    /**
     * @return - number of communities in the network
     */
    public int getNumberOfCommunitiesInNetwork(){
        return networkService.getNumberOfCommunitiesService();
    }

    /**
     * @return - list of the users of the most social community
     */
    public List<User> getMostSocialCommunity(){
        return networkService.getMostSocialCommunity();
    }

    /**
     * @param idUser - Long - identifier for user
     * @return Map key is Optional of user, value is LocalDateTime
     */
    public Map<Optional<User>, LocalDateTime> findAllFriendshipsForUser(Long idUser){
        return userService.findAllFriendsForUserService(idUser);
    }

    public Map<Optional<User>, LocalDateTime> findAllFriendsForUserMonth(Long idUser,int month){
        return userService.findAllFriendsForUserMonthService(idUser,month);
    }

    public Optional<Message> sendMessages(Long idUserFrom, List<Long> to, String text){
        return messageService.sendMessagesService(idUserFrom, to, text);
    }

    public Optional<ReplyMessage> respondMessage(Long idUserFrom, Long idMessageAggregate, String text){
        return messageService.respondMessageService(idUserFrom, idMessageAggregate, text);
    }

    public List< List < HistoryConversationDTO > > historyConversation(Long idFirstUser, Long idSecondUser){
        return messageService.historyConversationService(idFirstUser, idSecondUser);
    }

    public List<MessagesToRespondDTO> getAllMessagesToRespondForUser(Long idUser){
        return messageService.getAllMessagesToRespondForUserService(idUser);
    }

    public Optional<Friendship> updateApprovedFriendship(Long firstUserId,Long secondUserId){
        Optional<Friendship> friendshipOptional = networkService.
                updateApprovedFriendshipService(firstUserId,secondUserId);
        notifyObservers(new FriendshipChangeEvent(ChangeEventType.UPDATE, friendshipOptional.get()));
        return friendshipOptional;
    }

    public Optional<Friendship> updateRejectedFriendship(Long firstUserId,Long secondUserId){
        Optional<Friendship> friendshipOptional = networkService.
                updateRejectedFriendshipService(firstUserId,secondUserId);
        notifyObservers(new FriendshipChangeEvent(ChangeEventType.UPDATE, friendshipOptional.get()));
        return friendshipOptional;
    }

    public Optional<Friendship> sendInvitationForFriendships(Long firstUserId,Long secondUserId){
        return networkService.sendInvitationForFriendshipsService(firstUserId,secondUserId);
    }

    public Optional<Friendship> updateRejectedToPendingFriendship(Long idUserThatSends,Long idUserThatReceive) {
        Optional<Friendship> friendshipOptional = networkService
                .updateRejectedToPendingFriendshipService(idUserThatSends,idUserThatReceive);
        notifyObservers(new FriendshipChangeEvent(ChangeEventType.UPDATE, friendshipOptional.get()));
        return friendshipOptional;
    }

        public Map<Optional<User>, LocalDateTime> findAllApprovedFriendshipsForUser(Long idUser){
        return userService.findAllApprovedFriendshipsForUserService(idUser);
    }

    public Optional<Autentification> saveAuthentification(String username,String password){
        return authentificationService.saveAuthentificationService(username,password);
    }

    public Optional<Autentification> findAuthentification(String username){
        return authentificationService.findAuthentificationService(username);
    }

    public List<Autentification> getAllAuthentification(String username){
        return authentificationService.getAllAuthentificationService();
    }

    public boolean signUp(String firstName,String lastName,String username,String password){
        Optional<User> signUpUser = userService.addUserService(firstName,lastName,username);
        if(signUpUser.isEmpty()){
            Optional<Autentification> savedAutentification = authentificationService
                    .saveAuthentificationService(username,password);
            return true;
        }
        return false;
    }

    public Optional<User> logIn(String username,String password){
        Optional<Autentification> findAutentification = authentificationService
                .findAuthentificationService(username);
        if(findAutentification.isEmpty())
            throw new LogInException("Username is invalid!");
        if(!findAutentification.get().getPassword().equals(password))
            throw new LogInException("Password is invalid!");
        return Optional.of( userService.getAllService()
                .stream()
                .filter(user -> user.getUsername().equals(username))
                .toList()
                .get(0) );
    }

    public List<User> getAllUsers(){
        return userService.getAllService();
    }

    public List<FriendshipRequestDTO> findAllRequestFriendsForUser(Long idUser){
        return userService.findAllRequestFriendsForUserService(idUser);
    }

    @Override
    public void addObserver(Observer<Event> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<Event> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Event event) {
        observers.forEach(e -> e.update(event));
    }
}
