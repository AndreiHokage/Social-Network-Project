package repository.database;

import config.ApplicationContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import socialNetwork.domain.models.DTOEventPublicUser;
import socialNetwork.domain.models.EventNotification;
import socialNetwork.domain.models.EventPublic;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.database.EventPublicDatabaseRepository;
import socialNetwork.repository.database.EventPublicUserBindingDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.utilitaries.UnorderedPair;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class EventPublicUserBindingDatabaseRepoTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    UserDatabaseRepository userDatabaseRepository = new UserDatabaseRepository(url,user,password);
    EventPublicDatabaseRepository eventPublicDatabaseRepository =
            new EventPublicDatabaseRepository(url,user,password);
    EventPublicUserBindingDatabaseRepository eventPublicUserBindingDatabaseRepository;

    private EventPublicUserBindingDatabaseRepository getRepo(){
        if(eventPublicUserBindingDatabaseRepository == null)
            eventPublicUserBindingDatabaseRepository = new EventPublicUserBindingDatabaseRepository(url,user,password);
        return eventPublicUserBindingDatabaseRepository;
    }

    private Long getMiniID(){

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement findSql = connection.prepareStatement("select min(id) from eventPublic")) {
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    private Long getMiniUserID(){

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement findSql = connection.prepareStatement("select min(id) from users")) {
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    private void tearDown(){
        try(Connection connection = DriverManager.getConnection(url,user,password);
        ) {
            PreparedStatement deleteAllRecordsAuthentifications = connection
                    .prepareStatement("delete from eventPublicUser");
            deleteAllRecordsAuthentifications.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @BeforeEach
    private void setUp(){
        tearDown();
        userDatabaseRepository.save(new User("Andrei","Balanici","A"));
        userDatabaseRepository.save(new User("Razvan","Berendi","R"));
        eventPublicDatabaseRepository.save(new EventPublic("a1","a2", LocalDateTime.now()));
        eventPublicDatabaseRepository.save(new EventPublic("b1","b2", LocalDateTime.now()));
    }

    @Test
    void testSave(){
        Long idUser = getMiniUserID();
        Long idEvent = getMiniID();
        DTOEventPublicUser dtoEventPublicUser1 = new DTOEventPublicUser(idUser,idEvent);
        DTOEventPublicUser dtoEventPublicUser2 = new DTOEventPublicUser(idUser+1,idEvent);
        getRepo().save(dtoEventPublicUser1);
        getRepo().save(dtoEventPublicUser2);

        List<DTOEventPublicUser>  dtoEventPublicUserList = getRepo().getAll();
        DTOEventPublicUser dtoResult1 = dtoEventPublicUserList.get(0);
        DTOEventPublicUser dtoResult2 = dtoEventPublicUserList.get(1);

        Assertions.assertEquals(idUser,dtoResult1.getIdUser());
        Assertions.assertEquals(idUser+1,dtoResult2.getIdUser());
    }

    @Test
    void testFind(){
        Long idUser = getMiniUserID();
        Long idEvent = getMiniID();
        DTOEventPublicUser dtoEventPublicUser1 = new DTOEventPublicUser(idUser,idEvent);
        DTOEventPublicUser dtoEventPublicUser2 = new DTOEventPublicUser(idUser+1,idEvent);
        getRepo().save(dtoEventPublicUser1);
        getRepo().save(dtoEventPublicUser2);

        DTOEventPublicUser findDtoEventPublic = getRepo().find( new UnorderedPair<Long,Long>(idUser,idEvent) ).get();
        Assertions.assertEquals(idUser,findDtoEventPublic.getIdUser());
        Assertions.assertEquals(Optional.empty(),getRepo().find( new UnorderedPair<Long,Long>(idUser-1,idEvent) ));
    }

    @Test
    void testUpdate(){
        Long idUser = getMiniUserID();
        Long idEvent = getMiniID();
        DTOEventPublicUser dtoEventPublicUser1 = new DTOEventPublicUser(idUser,idEvent);
        DTOEventPublicUser dtoEventPublicUser2 = new DTOEventPublicUser(idUser+1,idEvent);
        getRepo().save(dtoEventPublicUser1);
        getRepo().save(dtoEventPublicUser2);

        DTOEventPublicUser dtoUpdate = new DTOEventPublicUser(idUser,idEvent);
        dtoUpdate.setReceivedNotification(EventNotification.REJECT);
        getRepo().update(dtoUpdate);
        DTOEventPublicUser dtoEventPublicUserAfterUpdate = getRepo().update(dtoUpdate).get();
        Assertions.assertEquals(EventNotification.REJECT,dtoEventPublicUserAfterUpdate
                .getReceivedNotification());
    }
}
