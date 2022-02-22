package repository.database;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.Autentification;
import socialNetwork.domain.models.User;
import socialNetwork.repository.database.AutentificationDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthentificationDatabaseRepositoryTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    AutentificationDatabaseRepository autentificationDatabaseRepository;
    UserDatabaseRepository userDataBaseRepository;

    private AutentificationDatabaseRepository getAuthentificationRepository(){
        if(autentificationDatabaseRepository == null)
            autentificationDatabaseRepository = new AutentificationDatabaseRepository(url,user,password);
        return autentificationDatabaseRepository;
    }

    private UserDatabaseRepository getUserRepository(){
        if(userDataBaseRepository == null)
            userDataBaseRepository = new UserDatabaseRepository(url,user,password);
        return userDataBaseRepository;
    }


    private void tearDown(){
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement deleteAllRecordsAuthentifications = connection
                    .prepareStatement("delete from autentifications");
             PreparedStatement deleteAllRecordsUsers = connection
                .prepareStatement("delete from users")) {
            deleteAllRecordsAuthentifications.executeUpdate();
            deleteAllRecordsUsers.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @BeforeEach
    private void setUp(){
        tearDown();
        getUserRepository().save(new User("andrei78","balanici","andrei"));
        getUserRepository().save(new User("razvan34","bogdan","razvan"));
    }

    @Test
    void testSaveAuthentification(){
        Autentification authentification0 = new Autentification("andrei","casa");
        Autentification authentification1 = new Autentification("razvan","upup");
        Autentification authentification2 = new Autentification("andrei","copac");

        Assertions.assertEquals(Optional.empty(), getAuthentificationRepository().save(authentification0));
        Assertions.assertEquals(Optional.empty(), getAuthentificationRepository().save(authentification1));
        Assertions.assertEquals(Optional.of(authentification2),getAuthentificationRepository().save(authentification2));
    }

    @Test
    void testFindAuthentification(){
        Autentification authentification0 = new Autentification("andrei","casa");
        Autentification authentification1 = new Autentification("razvan","upup");
        getAuthentificationRepository().save(authentification0);
        getAuthentificationRepository().save(authentification1);

        Assertions.assertEquals(Optional.of(authentification0),
                getAuthentificationRepository().find("andrei"));
        Assertions.assertEquals(Optional.of(authentification1),
                getAuthentificationRepository().find("razvan"));
        Assertions.assertEquals(Optional.empty(),
                getAuthentificationRepository().find("upupdown"));
    }

    @Test
    void testGetAllAuthentification() {
        Autentification authentification0 = new Autentification("andrei", "casa");
        Autentification authentification1 = new Autentification("razvan", "upup");
        getAuthentificationRepository().save(authentification0);
        getAuthentificationRepository().save(authentification1);

        List<Autentification> autentificationList = getAuthentificationRepository().getAll();
        Assertions.assertEquals(Optional.of(authentification0).get(),autentificationList.get(0));
        Assertions.assertEquals(Optional.of(authentification1).get(),autentificationList.get(1));
        Assertions.assertEquals(2,autentificationList.size());
    }
}
