package socialNetwork.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import socialNetwork.config.ApplicationContext;
import socialNetwork.controllers.NetworkController;
import socialNetwork.domain.models.*;
import socialNetwork.domain.validators.*;
import socialNetwork.guiControllers.LoginController;
import socialNetwork.repository.database.*;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.service.*;
import socialNetwork.utilitaries.UnorderedPair;

import java.io.IOException;

public class StartApplication extends Application {

    private static final int INSTANCES = 3;
    private static int thereMore = 0;
    private static NetworkController networkController = null;

    private static NetworkController getNetworkController(){

        if(networkController == null ) {
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

            PagingRepository<UnorderedPair<Long, Long>, FriendRequest> friendRequestRepository =
                    new FriendRequestDatabaseRepository(url, user, password);
            EntityValidatorInterface<UnorderedPair<Long, Long>, FriendRequest> friendRequesttValidator =
                    new FriendRequestValidator(userRepository);

            PagingRepository<Long, MessageDTO> messagesRepository =
                    new MessageDTODatabaseRepository(url, user, password);

            PagingRepository<String, Autentification> autentificationRepository =
                    new AutentificationDatabaseRepository(url, user, password);
            EntityValidatorInterface<String, Autentification> autentificationValidator =
                    new AuthentificationValidator();

            PagingRepository<Long, EventPublic> eventPublicPagingRepository =
                    new EventPublicDatabaseRepository(url, user, password);
            PagingRepository<UnorderedPair<Long, Long>, DTOEventPublicUser> eventPublicUserPagingRepository =
                    new EventPublicUserBindingDatabaseRepository(url, user, password);
            EventPublicValidator eventPublicValidator = new EventPublicValidator();

            UserService userService = new UserService(userRepository, friendshipRepository
                    , friendRequestRepository, userValidator);
            NetworkService networkService = new NetworkService(friendshipRepository, friendRequestRepository,
                    userRepository, friendshipValidator);
            MessageService messageService = new MessageService(userRepository, messagesRepository);
            AuthentificationService authentificationService = new AuthentificationService(
                    autentificationRepository, autentificationValidator, securityPassword);
            FriendRequestService friendRequestService = new FriendRequestService(friendRequestRepository,
                    friendshipRepository, friendRequesttValidator);
            EventPublicService eventPublicService = new EventPublicService(eventPublicPagingRepository,
                    eventPublicUserPagingRepository, eventPublicValidator);
            networkController = new NetworkController(userService, networkService, messageService,
                            authentificationService, friendRequestService, eventPublicService, securityPassword);
        }
        return networkController;
    }

    @Override
    public void start(Stage stage) throws IOException {

        for(int i = 0 ; i < INSTANCES ; i++) {
            try {
                if (StartApplication.thereMore < INSTANCES) {
                    runAnotherApp(StartApplication.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void runAnotherApp(Class<? extends Application> anotherAppClass) throws Exception {
        StartApplication.thereMore ++;

        Application app2 = anotherAppClass.newInstance();
        Stage anotherStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/socialNetwork.gui/loginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(getClass().getResource("/css/logIn.css").toExternalForm());
        Image icon = new Image("images/applicationLogo.png");
        anotherStage.getIcons().add(icon);
        anotherStage.setTitle("Kage");
        anotherStage.setScene(scene);
        LoginController loginController = fxmlLoader.getController();
        loginController.setNetworkController(anotherStage, networkController);
        anotherStage.show();
        app2.start(anotherStage);


    }

    public static void main(String[] args) {
        getNetworkController();
        launch();
    }
}