package socialNetwork;

import socialNetwork.config.ApplicationContext;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.*;
import socialNetwork.domain.validators.*;
import socialNetwork.exceptions.CorruptedDataException;
import socialNetwork.gui.StartApplication;
import socialNetwork.repository.database.*;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.service.*;
import socialNetwork.ui.ConsoleInterface;
import socialNetwork.utilitaries.UnorderedPair;

public class Main {

    public static void main(String[] args){
        SecurityPassword securityPassword = new SecurityPassword();
        String url = ApplicationContext.getProperty("socialnetwork.database.url");
        String user = ApplicationContext.getProperty("socialnetwork.database.user");
        String password = ApplicationContext.getProperty("socialnetwork.database.password");

        PagingRepository<Long, User> userRepository = new UserDatabaseRepository(url, user, password);
        EntityValidatorInterface<Long, User> userValidator = new UserValidator();

        PagingRepository<UnorderedPair<Long, Long>, Friendship> friendshipRepository =
                new FriendshipDatabaseRepository(url, user, password);
        EntityValidatorInterface<UnorderedPair<Long, Long>, Friendship> friendshipValidator =
                new FriendshipValidator(userRepository);

        PagingRepository<UnorderedPair<Long,Long>, FriendRequest> friendRequestRepository =
                new FriendRequestDatabaseRepository(url,user,password);
        EntityValidatorInterface<UnorderedPair<Long, Long>, FriendRequest> friendRequesttValidator =
                new FriendRequestValidator(userRepository);

        PagingRepository<Long, MessageDTO> messagesRepository =
                new MessageDTODatabaseRepository(url, user, password);

        PagingRepository<String, Autentification> autentificationRepository =
                new AutentificationDatabaseRepository(url,user,password);
        EntityValidatorInterface<String,Autentification> autentificationValidator =
                new AuthentificationValidator();

        PagingRepository<Long,EventPublic> eventPublicPagingRepository =
                new EventPublicDatabaseRepository(url,user,password);
        PagingRepository<UnorderedPair<Long,Long>, DTOEventPublicUser> eventPublicUserPagingRepository =
                new EventPublicUserBindingDatabaseRepository(url,user,password);
        EventPublicValidator eventPublicValidator = new EventPublicValidator();

        UserService userService = new UserService(userRepository, friendshipRepository
                ,friendRequestRepository,userValidator);
        NetworkService networkService = new NetworkService(friendshipRepository, friendRequestRepository,
                userRepository,friendshipValidator);
        MessageService messageService = new MessageService(userRepository, messagesRepository);
        AuthentificationService authentificationService = new AuthentificationService(
                autentificationRepository,autentificationValidator,securityPassword);
        FriendRequestService friendRequestService = new FriendRequestService(friendRequestRepository,
                friendshipRepository,friendRequesttValidator);
        EventPublicService eventPublicService = new EventPublicService(eventPublicPagingRepository,
                eventPublicUserPagingRepository,eventPublicValidator);
        NetworkController networkController =
                new NetworkController(userService, networkService, messageService,
                        authentificationService,friendRequestService,eventPublicService,securityPassword);
        ConsoleInterface ui = new ConsoleInterface(networkController);
        ui.run();
        //StartApplication startApplication = new StartApplication();
       // startApplication.main(args,networkController);
    }
}
