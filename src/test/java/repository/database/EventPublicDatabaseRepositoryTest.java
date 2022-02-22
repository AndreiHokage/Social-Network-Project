package repository.database;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import socialNetwork.domain.models.EventPublic;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.database.EventPublicDatabaseRepository;
import socialNetwork.service.EventPublicService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class EventPublicDatabaseRepositoryTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    EventPublicDatabaseRepository eventPublicDatabaseRepository;

    private EventPublicDatabaseRepository getRepo(){
        if(eventPublicDatabaseRepository == null)
            eventPublicDatabaseRepository = new EventPublicDatabaseRepository(url,user,password);
        return eventPublicDatabaseRepository;
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

    private void tearDown(){
        try(Connection connection = DriverManager.getConnection(url,user,password);
        ) {
            PreparedStatement deleteAllRecordsAuthentifications = connection
                    .prepareStatement("delete from eventPublic");
            deleteAllRecordsAuthentifications.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @BeforeEach
    private void setUp(){
        tearDown();
    }

    @Test
    void testSave(){
        EventPublic eventPublic1 = new EventPublic("a1","a1", LocalDateTime.now());
        EventPublic eventPublic2 = new EventPublic("b1","b1", LocalDateTime.now());
        getRepo().save(eventPublic1);
        getRepo().save(eventPublic2);

        List<EventPublic> eventPublicList = getRepo().getAll();
        Assertions.assertEquals(2,eventPublicList.size());

        EventPublic rezEvent1 = eventPublicList.get(0);
        EventPublic rezEvent2 = eventPublicList.get(1);
        Assertions.assertEquals("a1",rezEvent1.getDescription());
        Assertions.assertEquals("b1",rezEvent2.getDescription());
    }

    @Test
    void testFind(){
        EventPublic eventPublic1 = new EventPublic("a1","a1", LocalDateTime.now());
        EventPublic eventPublic2 = new EventPublic("b1","b1", LocalDateTime.now());
        getRepo().save(eventPublic1);
        getRepo().save(eventPublic2);

        Long id = getMiniID();
        Assertions.assertEquals("a1",getRepo().find(id).get().getDescription());
        Assertions.assertEquals(Optional.empty(),getRepo().find(id-1));
    }

}
