package socialNetwork.ui;

import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.HistoryConversationDTO;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.CorruptedDataException;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.exceptions.ExceptionBaseClass;
import socialNetwork.exceptions.InvalidNumericalValueException;
import socialNetwork.service.NetworkService;
import socialNetwork.service.UserService;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

class Command{
    public static final String EXIT = "exit";
    public static final String ADD_USER = "add user";
    public static final String UPDATE_USER = "update user";
    public static final String REMOVE_USER = "remove user";
    public static final String FIND_USER = "find user";
    public static final String GET_ALL_USERS = "get all users";
    public static final String FIND_ALL_FRIENDSHIPS_USER = "find friendships user";
    public static final String FIND_ALL_FRIENDSHIPS_MONTH = "find friendships month";
    public static final String ADD_FRIENDSHIP = "add friendship";
    public static final String REMOVE_FRIENDSHIP = "remove friendship";
    public static final String FIND_FRIENDSHIP = "find friendship";
    public static final String COUNT_COMMUNITIES = "count communities";
    public static final String MOST_SOCIAL = "most social";
    public static final String SEND_MESSAGE = "send message";
    public static final String RESPOND_TO_MESSAGE = "respond to message";
    public static final String HISTORY_CONVERSATION = "history conversation";
    public static final String SEND_INVITATION = "send invitation";
    public static final String APPROVE_INVITATION = "approve invitation";
    public static final String REJECT_INVITATION = "reject invitation";
    public static final String PASSWORD_HASH = "password to hash";
}

public class ConsoleInterface {
    private NetworkController networkController;
    private final Scanner inputReader = new Scanner(System.in);
    private Map<String, Runnable> commandMap = new HashMap<>();
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a");
    private static final int MENU_INDENTATION = 4;

    private void initializeCommands(){
        commandMap.put(Command.ADD_USER, this::addUser);
        commandMap.put(Command.UPDATE_USER, this::updateUser);
        commandMap.put(Command.REMOVE_USER, this::removeUser);
        commandMap.put(Command.FIND_USER, this::findUser);
        commandMap.put(Command.FIND_ALL_FRIENDSHIPS_USER, this::findFriendshipsUser);
        commandMap.put(Command.FIND_ALL_FRIENDSHIPS_MONTH,this::findFriendshipMonth);
        commandMap.put(Command.ADD_FRIENDSHIP, this::addFriendship);
        commandMap.put(Command.REMOVE_FRIENDSHIP, this::removeFriendship);
        commandMap.put(Command.FIND_FRIENDSHIP, this::findFriendship);
        commandMap.put(Command.GET_ALL_USERS, this::getAllUsersWithTheirFriends);
        commandMap.put(Command.COUNT_COMMUNITIES, this::countCommunities);
        commandMap.put(Command.MOST_SOCIAL, this::findMostSocialCommunities);
        commandMap.put(Command.SEND_MESSAGE, this::sendMessage);
        commandMap.put(Command.RESPOND_TO_MESSAGE, this::respondMessage);
        commandMap.put(Command.HISTORY_CONVERSATION, this::historyConversation);
        commandMap.put(Command.SEND_INVITATION, this::sendInvitation);
        commandMap.put(Command.APPROVE_INVITATION, this::approveInvitation);
        commandMap.put(Command.REJECT_INVITATION, this::rejectInvitation);
        commandMap.put(Command.PASSWORD_HASH,this::passwordHash);
    }



    public ConsoleInterface(NetworkController networkController) {
        this.networkController = networkController;
        initializeCommands();
    }

    public void run(){
        while (true){
            printMenu();
            System.out.println(">>");
            String commandFromUser = readStringFromUser();

            if(commandFromUser.compareTo(Command.EXIT) == 0){
                inputReader.close();
                return;
            }

            if(commandMap.containsKey(commandFromUser)){
                try{
                    commandMap.get(commandFromUser).run();
                } catch (ExceptionBaseClass exception){
                    System.out.println(exception.getMessage());
                }
            }
            else
                System.out.println("Invalid command");

            System.out.println("\nPress Enter to continue!");
            try {
                System.in.read();
                System.in.skipNBytes(System.in.available());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private String readStringFromUser(){
        return inputReader
                .nextLine()
                .stripLeading()
                .stripTrailing();
    }

    private Long readLongFromUser(){
        Long userLongInput = inputReader
                .nextLong();
        inputReader.nextLine();
        return userLongInput;
    }

    private Long readLongFromUser(String invalidNumericalValueMessage){
        long userInput;
        try{
            userInput = readLongFromUser();
        } catch (InputMismatchException e){
            inputReader.nextLine();
            throw new InvalidNumericalValueException(invalidNumericalValueMessage);
        }
        return userInput;
    }

    private int readIntFromUser(){
        int userIntInput = inputReader
                .nextInt();
        inputReader.nextLine();
        return userIntInput;
    }

    private int readIntFromUser(String invalidNumericalValueMessage){
        int userInput;
        try{
            userInput = readIntFromUser();
        } catch (InputMismatchException e){
            inputReader.nextLine();
            throw new InvalidNumericalValueException(invalidNumericalValueMessage);
        }
        return userInput;
    }

    private void printMenu(){
        System.out.print("SOCIAL NETWORK APPLICATION".indent(MENU_INDENTATION));
        System.out.print("MENU".indent(MENU_INDENTATION));
        System.out.printf("0. %s".indent(MENU_INDENTATION), Command.EXIT);
        System.out.printf("1. %s\n".indent(MENU_INDENTATION), Command.ADD_USER);
        System.out.printf("2. %s".indent(MENU_INDENTATION), Command.REMOVE_USER);
        System.out.printf("3. %s".indent(MENU_INDENTATION), Command.UPDATE_USER);
        System.out.printf("4. %s".indent(MENU_INDENTATION), Command.FIND_USER);
        System.out.printf("5. %s".indent(MENU_INDENTATION), Command.ADD_FRIENDSHIP);
        System.out.printf("6. %s".indent(MENU_INDENTATION), Command.REMOVE_FRIENDSHIP);
        System.out.printf("7. %s".indent(MENU_INDENTATION), Command.FIND_FRIENDSHIP);
        System.out.printf("8. %s".indent(MENU_INDENTATION), Command.GET_ALL_USERS);
        System.out.printf("9. %s".indent(MENU_INDENTATION), Command.COUNT_COMMUNITIES);
        System.out.printf("10. %s".indent(MENU_INDENTATION), Command.MOST_SOCIAL);
        System.out.printf("11. %s".indent(MENU_INDENTATION), Command.FIND_ALL_FRIENDSHIPS_USER);
        System.out.printf("12. %s".indent(MENU_INDENTATION), Command.FIND_ALL_FRIENDSHIPS_MONTH);
        System.out.printf("13. %s".indent(MENU_INDENTATION), Command.SEND_MESSAGE);
        System.out.printf("14. %s".indent(MENU_INDENTATION), Command.RESPOND_TO_MESSAGE);
        System.out.printf("15. %s".indent(MENU_INDENTATION), Command.HISTORY_CONVERSATION);
        System.out.printf("16. %s".indent(MENU_INDENTATION), Command.SEND_INVITATION);
        System.out.printf("17. %s".indent(MENU_INDENTATION), Command.APPROVE_INVITATION);
        System.out.printf("18. %s".indent(MENU_INDENTATION), Command.REJECT_INVITATION);
        System.out.printf("19. %s".indent(MENU_INDENTATION), Command.PASSWORD_HASH);
    }

    private void passwordHash(){
        System.out.println("Username: ");
        String username =  readStringFromUser();
        System.out.println("Password: ");
        String password =  readStringFromUser();
        try {
            networkController.changePasswordToHash(username,password);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private void sendInvitation(){
        getAllUsersWithTheirFriends();
        System.out.print("ID first user: ");
        Long idFirstUser = readLongFromUser("Invalid value for ID");
        System.out.print("ID second user: ");
        Long idSecondUser = readLongFromUser("Invalid value for ID");
        networkController.sendInvitationForFriendships(idFirstUser, idSecondUser);
        System.out.println("Invitation has been sent");
    }

    private void approveInvitation(){
        getAllUsersWithTheirFriends();
        System.out.print("ID first user: ");
        Long idFirstUser = readLongFromUser("Invalid value for ID");
        System.out.print("ID second user: ");
        Long idSecondUser = readLongFromUser("Invalid value for ID");
        networkController.updateApprovedFriendship(idFirstUser, idSecondUser);
        System.out.println("Invitation has been accepted");
    }

    private void rejectInvitation(){
        getAllUsersWithTheirFriends();
        System.out.print("ID first user: ");
        Long idFirstUser = readLongFromUser("Invalid value for ID");
        System.out.print("ID second user: ");
        Long idSecondUser = readLongFromUser("Invalid value for ID");
        networkController.updateRejectedFriendship(idFirstUser, idSecondUser);
        System.out.println("Invitation has been rejected");
    }

    private void historyConversation() {
        List<User> listOfUsers = networkController.getAllUsersAndTheirFriends();
        listOfUsers.forEach(System.out::println);
        System.out.print("ID first user: ");
        Long idFirstUser = readLongFromUser("Invalid value for ID");
        System.out.print("ID second user: ");
        Long idSecondUser = readLongFromUser("Invalid value for ID");
        List< List< HistoryConversationDTO > > conversation =
                networkController.historyConversation(idFirstUser, idSecondUser);
        if(conversation.size() == 0)
            System.out.println("No conversation between users");
        else
            conversation.stream()
                    .forEach(listHistoryConversationDTO->{
                        System.out.println("------------------------------------");
                        listHistoryConversationDTO.stream()
                                .forEach(System.out::println);
                    });
    }

    private void respondMessage() {
        List<User> listOfUsers = networkController.getAllUsersAndTheirFriends();
        listOfUsers.forEach(System.out::println);
        System.out.print("Id user to respond: ");
        Long idUserResponds = readLongFromUser("Invalid value for ID");
        networkController.getAllMessagesToRespondForUser(idUserResponds).forEach(System.out::println);
        System.out.print("Introduce ID of message to respond to: ");
        Long idMessageToRespondTo = readLongFromUser("Invalid value for ID");
        System.out.print("Introduce message: ");
        String text = readStringFromUser();
        networkController.respondMessage(idUserResponds, idMessageToRespondTo, text);
        System.out.println("Successful response!");
    }

    private void sendMessage() {
        List<User> listOfUsers = networkController.getAllUsersAndTheirFriends();
        listOfUsers.forEach(System.out::println);
        System.out.print("ID user that sends message: ");
        Long idUserSends = readLongFromUser("Invalid value for id");
        System.out.print("ID users that receive: ");
        String idUsersReceive = readStringFromUser();
        String[] idUsersReceiveAttributes = idUsersReceive.split(",");
        List<Long> idReceivers = new ArrayList<>();
        for(int i = 0; i < idUsersReceiveAttributes.length; i++){
            try {
                idReceivers.add(Long.parseLong(idUsersReceiveAttributes[i]));
            }
            catch (InputMismatchException exception){
                throw new InvalidNumericalValueException(exception.getMessage());
            }
        }
        System.out.print("Introduce message: ");
        String text = readStringFromUser();
        networkController.sendMessages(idUserSends, idReceivers, text);
        System.out.println("Message has been sent");
    }

    private void findFriendshipMonth(){
        System.out.print("ID: ");
        Long idUser = readLongFromUser("Invalid value for ID");
        System.out.print("Month: ");
        int month = readIntFromUser("Invalid value for month");
        Map<Optional<User>,LocalDateTime> mapOfFriends =
                networkController.findAllFriendsForUserMonth(idUser,month);
        if(mapOfFriends.isEmpty())
            System.out.println("User does not have any friends for that month");
        else
            mapOfFriends.forEach((friend, date) ->{
                System.out.printf("%s | %s | %s\n",
                        friend.get().getLastName(),
                        friend.get().getFirstName(),
                        date.format(DATE_TIME_FORMATTER));
            });
    }

    private void findFriendshipsUser(){
        System.out.print("ID: ");
        Long idUser = readLongFromUser("Invalid value for ID");
        Map<Optional<User>, LocalDateTime> mapOfFriends =
                networkController.findAllFriendshipsForUser(idUser);
        if(mapOfFriends.isEmpty())
            System.out.println("User does not have any friends");
        else
            mapOfFriends.forEach((friend, date) ->{
                System.out.printf("%s | %s | %s\n",
                        friend.get().getLastName(),
                        friend.get().getFirstName(),
                        date.format(DATE_TIME_FORMATTER));
            });
    }

    private void findMostSocialCommunities(){
        List<User> usersOfMostSocialCommunity = networkController.getMostSocialCommunity();
        System.out.println("Most social community is:");
        usersOfMostSocialCommunity.forEach(System.out::println);
    }

    private void countCommunities(){
        int numberOfCommunities = networkController.getNumberOfCommunitiesInNetwork();
        if(numberOfCommunities > 1)
            System.out.printf("%d communities are in the network\n", numberOfCommunities);
        else if(numberOfCommunities == 1)
            System.out.println("1 community is in the network");
        else if(numberOfCommunities == 0)
            System.out.println("There are no communities!\n");
    }

    private void addUser(){
        System.out.print("First name: ");
        String firstName = readStringFromUser();

        System.out.print("Last name: ");
        String lastName = readStringFromUser();

        Optional<User> existingUserOptional = networkController.addUser(firstName, lastName,"");


        if(existingUserOptional.isPresent()){
            User existingUser = existingUserOptional.get();
            System.out.println("User with same id already exists: ".concat(existingUser.toString()));
        }
        else
            System.out.println("User has been added");
    }

    private void updateUser(){
        System.out.print("ID: ");
        Long id = readLongFromUser("Invalid value for ID");

        System.out.print("First name: ");
        String firstName = readStringFromUser();

        System.out.print("Last name: ");
        String lastName = readStringFromUser();

        Optional<User> existingUserOptional = networkController.updateUser(id, firstName, lastName,"");

        if(existingUserOptional.isPresent()){
            System.out.println("User has been updated");
        }
        else
            System.out.println("There is no user with the ID you have given");
    }

    private void findUser(){
        System.out.print("ID: ");
        Long id = readLongFromUser("Invalid value for ID");

        Optional<User> optionalFoundUser = networkController.findUser(id);
        if(optionalFoundUser.isEmpty()){
            System.out.println("User with id " + id + " could not be found");
        }
        else{
            User foundUser = optionalFoundUser.get();
            System.out.println(foundUser.toString());
        }
    }

    private void removeUser(){
        System.out.print("ID: ");
        Long id = readLongFromUser("Invalid value for id");
        Optional<User> removedUserOptional = networkController.removeUser(id);

        if(removedUserOptional.isPresent()){
            User removedUser = removedUserOptional.get();
            System.out.println("User ".concat(removedUser.toString()).concat(" has been removed"));
        }
        else
            System.out.println("User with the specified id does not exist");
    }

    private void addFriendship(){
        System.out.print("ID of first user: ");
        Long firstUserId = readLongFromUser("Invalid value for first user's id");
        System.out.print("ID of second user: ");
        Long secondUserId = readLongFromUser("Invalid value for second user's id");
        LocalDateTime date = LocalDateTime.now();
        Optional<Friendship> existingFriendshipOptional = networkController.addFriendship(firstUserId,
                secondUserId, date);
        if(existingFriendshipOptional.isPresent())
            System.out.printf("Friendship between %d and %d already exists\n", firstUserId, secondUserId);
        else
            System.out.printf("Friendship between %d and %d has been added\n", firstUserId, secondUserId);
    }

    private void removeFriendship(){
        System.out.print("ID of first user: ");
        Long firstUserId = readLongFromUser("Invalid value for first user's id");
        System.out.print("ID of second user: ");
        Long secondUserId = readLongFromUser("Invalid value for second user's id");
        Optional<Friendship> existingFriendshipOptional = networkController.removeFriendship(firstUserId,
                secondUserId);
        if(existingFriendshipOptional.isEmpty())
            System.out.printf("Friendship between %d and %d does not exist\n", firstUserId, secondUserId);
        else
            System.out.printf("Friendship between %d and %d has been removed\n", firstUserId, secondUserId);
    }

    private void findFriendship(){
        System.out.print("Id of first user: ");
        Long firstUserId = readLongFromUser("Invalid value for first user's id");
        System.out.print("ID of second user: ");
        Long secondUserId = readLongFromUser("Invalid value for second user's id");
        Optional<Friendship> existingFriendshipOptional = networkController.findFriendship(firstUserId,
                secondUserId);
        if(existingFriendshipOptional.isEmpty())
            System.out.println("Friendship has not been found");
        else{
            Friendship existingFriendship = existingFriendshipOptional.get();
            System.out.println(existingFriendship.toString());
        }
    }

    private void getAllUsersWithTheirFriends(){
        List<User> allUsers = networkController.getAllUsersAndTheirFriends();
        Consumer<User> userPrinterConsumer = user -> {
            System.out.println(user.toString());
        };
        Consumer<User> userPrinterConsumerWithFriends = user ->{
            userPrinterConsumer.accept(user);
            System.out.println("Friends: ");
            if(user.getListOfFriends().size() == 0)
                System.out.println("No friends");
            else
                user.getListOfFriends().forEach(userPrinterConsumer);
            System.out.println();
        } ;
        allUsers.forEach(userPrinterConsumerWithFriends);
    }
}


