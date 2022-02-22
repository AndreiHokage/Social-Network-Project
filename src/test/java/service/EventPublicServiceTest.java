package service;

import config.ApplicationContext;
import javafx.util.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import socialNetwork.domain.models.EventNotification;
import socialNetwork.domain.models.EventPublic;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.EventPublicValidator;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.database.EventPublicDatabaseRepository;
import socialNetwork.repository.database.EventPublicUserBindingDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.EventPublicService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class EventPublicServiceTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    UserDatabaseRepository userDatabaseRepository = new UserDatabaseRepository(url,user,password);
    EventPublicDatabaseRepository eventPublicDatabaseRepository =
            new EventPublicDatabaseRepository(url,user,password);
    EventPublicUserBindingDatabaseRepository eventPublicUserBindingDatabaseRepository =
            new EventPublicUserBindingDatabaseRepository(url,user,password);
    EventPublicValidator eventPublicValidator;
    EventPublicService eventPublicService;

    private EventPublicService getService(){
        if( eventPublicService == null ){
            eventPublicValidator = new EventPublicValidator();
            eventPublicService = new EventPublicService(eventPublicDatabaseRepository,
                    eventPublicUserBindingDatabaseRepository,eventPublicValidator);
        }
        return eventPublicService;
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

    private Long getMiniEventID(){

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement findSql = connection.prepareStatement("select min(id) from eventPublic")) {
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
            PreparedStatement deleteStatement = connection
                    .prepareStatement("delete from eventPublic");
            deleteStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @BeforeEach
    private void setUp(){
        tearDown();
        userDatabaseRepository.save(new User("Andrei","Balanici","A"));
        userDatabaseRepository.save(new User("Razvan","Berendi","R"));
        getService().addEventPublicService("a1","a1", LocalDateTime.now());
        getService().addEventPublicService("b1","b1", LocalDateTime.now());
        getService().addEventPublicService("c1","c1", LocalDateTime.now());
    }

    @Test
    void testSubscribe(){
        Long idUser = getMiniUserID();
        Long idEvent = getMiniEventID();
        getService().subscribeEventPublicService(idUser,idEvent);
        getService().subscribeEventPublicService(idUser,idEvent+1);
        getService().subscribeEventPublicService(idUser,idEvent+2);
        Assertions.assertEquals(3,eventPublicDatabaseRepository.getAll().size());
    }

    @Test
    void testStopNotify(){
        Long idUser = getMiniUserID();
        Long idEvent = getMiniEventID();
        getService().subscribeEventPublicService(idUser,idEvent);
        getService().subscribeEventPublicService(idUser,idEvent+1);
        getService().subscribeEventPublicService(idUser,idEvent+2);
        getService().stopNotificationEventPublicService(idUser,idEvent+1);

        int cnt = eventPublicUserBindingDatabaseRepository.getAll()
                .stream()
                .filter(x->x.getReceivedNotification().equals(EventNotification.APPROVE))
                .toList().size();
        Assertions.assertEquals(cnt,2);
    }

    @Test
    void testFilterAllEvent(){
        Long idUser = getMiniUserID();
        Long idEvent = getMiniEventID();
        getService().subscribeEventPublicService(idUser,idEvent);
        getService().subscribeEventPublicService(idUser,idEvent+1);
        getService().subscribeEventPublicService(idUser,idEvent+2);
        getService().stopNotificationEventPublicService(idUser,idEvent+1);


        List<EventPublic> eventPublicList = getService()
                .filterAllEventPublicForNotificationService(idUser,30L);
        Assertions.assertEquals(2,eventPublicList.size());
        Assertions.assertEquals("a1",eventPublicList.get(0).getDescription());
        Assertions.assertEquals("c1",eventPublicList.get(1).getDescription());
    }

    @Test
    void testEventPublicForUser(){
        Long idUser = getMiniUserID();
        Long idEvent = getMiniEventID();
        getService().subscribeEventPublicService(idUser,idEvent);
        getService().subscribeEventPublicService(idUser,idEvent+1);
        getService().subscribeEventPublicService(idUser,idEvent+2);
        getService().stopNotificationEventPublicService(idUser,idEvent+1);


        List<EventPublic> eventPublicList = getService()
                .getAllEventPublicForSpecifiedUserService(idUser);
        Assertions.assertEquals(3,eventPublicList.size());
    }
}
