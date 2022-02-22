package socialNetwork.controllers;

import socialNetwork.domain.models.*;
import socialNetwork.exceptions.LogInException;
import socialNetwork.service.*;
import socialNetwork.utilitaries.events.*;
import socialNetwork.utilitaries.observer.Observer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * controller between socialNetwork.gui and business layer
 */
public class NetworkController {
    private UserService userService;
    private NetworkService networkService;
    private FriendRequestService friendRequestService;
    private MessageService messageService;
    private AuthentificationService authentificationService;
    private EventPublicService eventPublicService;
    private SecurityPassword securityPassword;
    private List<Observer<Event>> observers = new ArrayList<>();

    /**
     * constructor - creates a controller that accesses the given services
     *
     * @param userService    - service for users
     * @param networkService - service for friendships
     */
    public NetworkController(UserService userService, NetworkService networkService,
                             MessageService messageService, AuthentificationService authentificationService,
                             FriendRequestService friendRequestService, EventPublicService eventPublicService,
                             SecurityPassword securityPassword) {
        this.userService = userService;
        this.networkService = networkService;
        this.messageService = messageService;
        this.authentificationService = authentificationService;
        this.friendRequestService = friendRequestService;
        this.eventPublicService = eventPublicService;
        this.securityPassword = securityPassword;
    }

    public UserService getUserService() {
        return userService;
    }

    public NetworkService getNetworkService() {
        return networkService;
    }

    public FriendRequestService getFriendRequestService() {
        return friendRequestService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public EventPublicService getEventPublicService() {
        return eventPublicService;
    }

    public AuthentificationService getAuthentificationService() {
        return authentificationService;
    }

    /**
     * adds a new user
     *
     * @param firstName - String
     * @param lastName  - String
     * @return - empty Optional if the user was added, Optional containing the existing user otherwise
     */

    public Optional<User> addUser(String firstName, String lastName, String username) {
        return userService.addUserService(firstName, lastName, username);
    }

    /**
     * update user
     *
     * @param id        - Long
     * @param firstName - String
     * @param lastName  - String
     * @return - old value Optional if the user was updated, empty Optional otherwise
     */
    public Optional<User> updateUser(Long id, String firstName, String lastName, String username) {
        return userService.updateUserService(id, firstName, lastName, username);
    }

    /**
     * removes the user with the given id and his friendships with other users
     *
     * @param id - int
     * @return Optional with the user that was removed, empty optional if user did not exist
     */
    public Optional<User> removeUser(Long id) {
        List<Message> messageList = messageService.allMessagesUserAppearsIn(id);
        Optional<User> removeUser = userService.removeUserService(id);
        messageList.forEach(message -> messageService.removeMessageService(message.getId()));
        return removeUser;
    }

    /**
     * finds user with given id
     *
     * @param id - identifier of user we search for
     * @return - Optional containing user that has given id if exists, empty optional otherwise
     */
    public Optional<User> findUser(Long id) {
        return userService.findUserService(id);
    }

    /**
     * adds a friendship between 2 users
     *
     * @param firstUserId  - id first user
     * @param secondUserId - id second user
     * @return - empty Optional if the friendship was added, empty if friendship already exists
     * @throws socialNetwork.exceptions.InvalidEntityException - one of users not found
     */
    public Optional<Friendship> addFriendship(Long firstUserId, Long secondUserId, LocalDateTime date) {
        return networkService.addFriendshipService(firstUserId, secondUserId, date);
    }

    /**
     * removes the friendship between 2 users
     *
     * @param firstUserId  - id first user
     * @param secondUserId - id second user
     * @return Optional containing removed friendship, empty Optional if users do not exist
     */
    public Optional<Friendship> removeFriendship(Long firstUserId, Long secondUserId) {
        Optional<Friendship> removedFriendship =
                networkService.removeFriendshipService(firstUserId, secondUserId);
        if (removedFriendship.isEmpty())
            return Optional.empty();
        return removedFriendship;
    }

    /**
     * finds a friendships after id of 2 users
     *
     * @param firstUserId  - Long
     * @param secondUserId - Long
     * @return Optional containing friendship if exists, empty optional otherwise
     */
    public Optional<Friendship> findFriendship(Long firstUserId, Long secondUserId) {
        return networkService.findFriendshipService(firstUserId, secondUserId);
    }

    public List<User> getAllFriendshipForSpecifiedUser(Long idUser) {
        return networkService.getAllFriendshipForSpecifiedUserService(idUser);
    }

    /**
     * @return - a list of users and all their friends are set
     */
    public List<User> getAllUsersAndTheirFriends() {
        return networkService.getAllUsersAndTheirFriendsService();
    }

    /**
     * @return - number of communities in the network
     */
    public int getNumberOfCommunitiesInNetwork() {
        return networkService.getNumberOfCommunitiesService();
    }

    /**
     * @return - list of the users of the most social community
     */
    public List<User> getMostSocialCommunity() {
        return networkService.getMostSocialCommunity();
    }

    /**
     * @param idUser - Long - identifier for user
     * @return Map key is Optional of user, value is LocalDateTime
     */
    public Map<Optional<User>, LocalDateTime> findAllFriendshipsForUser(Long idUser) {
        return userService.findAllFriendsForUserService(idUser);
    }

    public Map<Optional<User>, LocalDateTime> findAllFriendsForUserMonth(Long idUser, int month) {
        return userService.findAllFriendsForUserMonthService(idUser, month);
    }

    public Optional<Message> sendMessages(Long idUserFrom, List<Long> to, String text) {
        return messageService.sendMessagesService(idUserFrom, to, text);
    }

    public Optional<ReplyMessage> respondMessage(Long idUserFrom, Long idMessageAggregate, String text) {
        return messageService.respondMessageService(idUserFrom, idMessageAggregate, text);
    }

    public List<List<HistoryConversationDTO>> historyConversation(Long idFirstUser, Long idSecondUser) {
        return messageService.historyConversationService(idFirstUser, idSecondUser);
    }

    public List<MessagesToRespondDTO> getAllMessagesToRespondForUser(Long idUser) {
        return messageService.getAllMessagesToRespondForUserService(idUser);
    }

    public Optional<FriendRequest> withdrawFriendRequest(Long userIdThatSendInvitationButWithdrawIt,
                                                         Long userIdThatReceiveInvitation) {
        return friendRequestService.withdrawFriendRequestService(userIdThatSendInvitationButWithdrawIt,
                userIdThatReceiveInvitation);
    }

    public Optional<FriendRequest> sendInvitationForFriendships(Long userIdThatSendInvitation,
                                                                Long userIdThatReceiveInvitation) {
        return friendRequestService.sendInvitationForFriendRequestService(userIdThatSendInvitation,
                userIdThatReceiveInvitation);
    }

    public Optional<Friendship> updateApprovedFriendship(Long userThatReceivesInvitationAndAcceptedId,
                                                         Long userThatSendInvitationAndWaitVerdictId) {
        Optional<Friendship> friendshipOptional = friendRequestService.
                updateApprovedFriendRequestService(userThatReceivesInvitationAndAcceptedId,
                        userThatSendInvitationAndWaitVerdictId);
        return friendshipOptional;
    }

    public Optional<Friendship> updateRejectedFriendship(Long userThatReceivesInvitationAndRejectedId,
                                                         Long userThatSendInvitationAndWaitVerdictId) {
        Optional<Friendship> friendshipOptional = friendRequestService.
                updateRejectedFriendRequestService(userThatReceivesInvitationAndRejectedId,
                        userThatSendInvitationAndWaitVerdictId);
        return friendshipOptional;
    }


    public Optional<FriendRequest> updateRejectedToPendingFriendship(Long idUserThatRejectButChangeHisMind,
                                                                     Long idUserThatSendInitiallyInvitation) {
        Optional<FriendRequest> friendshipOptional = friendRequestService
                .updateRejectedToPendingFriendRequestService(idUserThatRejectButChangeHisMind,
                        idUserThatSendInitiallyInvitation);
        return friendshipOptional;
    }

    public Map<Optional<User>, LocalDateTime> findAllApprovedFriendshipsForUser(Long idUser) {
        return userService.findAllFriendsForUserService(idUser);
    }


    public Optional<Autentification> saveAuthentification(String username, String password) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        return authentificationService.saveAuthentificationService(username, password);
    }

    public Optional<Autentification> findAuthentification(String username) {
        return authentificationService.findAuthentificationService(username);
    }

    public List<Autentification> getAllAuthentification(String username) {
        return authentificationService.getAllAuthentificationService();
    }

    public boolean signUp(String firstName, String lastName, String username, String password) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Optional<User> signUpUser = userService.addUserService(firstName, lastName, username);
        if (signUpUser.isEmpty()) {
            Optional<Autentification> savedAutentification = authentificationService
                    .saveAuthentificationService(username, password);
            return true;
        }
        return false;
    }

    private PageUser createPageObject(String username) {
        User root = getAllUsers()
                .stream()
                .filter(user -> user.getUsername().equals(username))
                .toList()
                .get(0);
        List<User> friendList = new ArrayList<>();
        List<FriendRequest> friendRequestList = new ArrayList<>();
        List<Chat> chatList = new ArrayList<>();
        return new PageUser(root, friendList, friendRequestList, chatList, this);
    }

    public PageUser logIn(String username, String password) {
        Optional<Autentification> findAutentification = authentificationService
                .findAuthentificationService(username);
        if (findAutentification.isEmpty())
            throw new LogInException("Username is invalid!");
        try {
            if (!securityPassword.checkPassword(password, findAutentification.get().getPassword()))
                throw new LogInException("Password is invalid!");
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new LogInException("Error to hash the password!");
        }
        return createPageObject(username);
    }

    public List<User> getAllUsers() {
        return userService.getAllService();
    }

    public List<FriendshipRequestDTO> findAllRequestFriendsForUser(Long idUser) {
        return userService.findAllRequestFriendsForUserService(idUser);
    }

    public List<Chat> getAllChatsSpecifiedUser(Long idUser) {
        return messageService.getAllChatsSpecifiedUserMessageService(idUser);
    }

    public List<FriendRequest> getAllFriendRequestForSpecifiedUser(Long idUser) {
        return friendRequestService.getAllFriendRequestForSpecifiedUserService(idUser);
    }

    public Optional<Autentification> changePasswordToHash(String username, String password) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        return authentificationService.changePasswordToHashService(username, password);
    }

    public Optional<EventPublic> addEventPublic(String name, String description, LocalDateTime date) {
        Optional<EventPublic> eventPublic = eventPublicService.addEventPublicService(name, description, date);
        return eventPublic;
    }

    public Optional<DTOEventPublicUser> subscribeEventPublic(Long idUser, Long idEventPublic) {
        Optional<DTOEventPublicUser> dtoEventPublicUser = eventPublicService
                .subscribeEventPublicService(idUser, idEventPublic);
        return dtoEventPublicUser;
    }

    public Optional<DTOEventPublicUser> stopNotificationEventPublic(Long idUser, Long idEventPublic) {
        Optional<DTOEventPublicUser> dtoEventPublicUser = eventPublicService
                .stopNotificationEventPublicService(idUser, idEventPublic);
        return dtoEventPublicUser;
    }

    public Optional<DTOEventPublicUser> turnOnNotificationsEventPublic(Long idUser, Long idEventPublic) {
        Optional<DTOEventPublicUser> dtoEventPublicUser = eventPublicService
                .turnOnNotificationEventPublicService(idUser, idEventPublic);
        return dtoEventPublicUser;
    }

    public List<EventPublic> filterAllEventPublicForNotification(Long idUser, Long days) {
        List<EventPublic> eventPublicList = eventPublicService
                .filterAllEventPublicForNotificationService(idUser,days);
        return eventPublicList;
    }

    public List<EventPublic> getAllEventPublic(){
        List <EventPublic> eventPublicList = eventPublicService.getAllEventPublicService();
        return eventPublicList;
    }

    public List<EventPublic> getAllEventPublicForSpecifiedUser(Long idUser){
        List<EventPublic> eventPublicList = eventPublicService
                .getAllEventPublicForSpecifiedUserService(idUser);
        return eventPublicList;
    }

    public List<DTOEventPublicUser> getAllPublicEventsWithNotifications(Long idUser){
        return eventPublicService.getAllEventsWithNotificationStatus(idUser);
    }

    public Optional<EventPublic> getPublicEventWithId(Long idEvent){
        return eventPublicService.findPublicEvent(idEvent);
    }
}
