package repository.database;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.FriendRequest;
import socialNetwork.domain.models.InvitationStage;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.database.AutentificationDatabaseRepository;
import socialNetwork.repository.database.FriendRequestDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.utilitaries.UnorderedPair;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FriendRequestDatabaseRepositoryTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    FriendRequestDatabaseRepository friendRequestDatabaseRepository;
    UserDatabaseRepository userDataBaseRepository;


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

    private Long getMaxiUserID(){
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement findSql = connection.prepareStatement("select max(id) from users")) {
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables.getMessage());
        }
    }

    private FriendRequestDatabaseRepository getFriendRequestRepository(){
        if(friendRequestDatabaseRepository == null)
            friendRequestDatabaseRepository = new FriendRequestDatabaseRepository(url,user,password);
        return friendRequestDatabaseRepository;
    }

    private UserDatabaseRepository getUserRepository(){
        if(userDataBaseRepository == null)
            userDataBaseRepository = new UserDatabaseRepository(url,user,password);
        return userDataBaseRepository;
    }


    private void tearDown(){
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement deleteAllRecordsAuthentifications = connection
                    .prepareStatement("delete from friendrequests");
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
        getUserRepository().save(new User("andrei","balanici","andreibalanici"));
        getUserRepository().save(new User("razvan","bogdan","razvanbogdan"));
        getUserRepository().save(new User("nicolae","trus","nicolaetrus"));
    }

    private void saveSomeFriendRequests(){
        FriendRequest friendRequest1 = new FriendRequest(getMiniUserID(),getMiniUserID()+1,
                InvitationStage.PENDING, LocalDateTime.now());
        FriendRequest friendRequest2 = new FriendRequest(getMiniUserID(),getMiniUserID()+2,
                InvitationStage.APPROVED, LocalDateTime.now());
        FriendRequest friendRequest3 = new FriendRequest(getMiniUserID()+1,getMiniUserID()+2,
                InvitationStage.REJECTED, LocalDateTime.now());

        getFriendRequestRepository().save(friendRequest1);
        getFriendRequestRepository().save(friendRequest2);
        getFriendRequestRepository().save(friendRequest3);
    }

    @Test
    void testSaveFriendRequest(){
        saveSomeFriendRequests();
        List<FriendRequest> friendRequestList = getFriendRequestRepository().getAll();
        Assertions.assertEquals(friendRequestList.get(0).getFromUserID(),getMiniUserID());
        Assertions.assertEquals(friendRequestList.get(1).getFromUserID(),getMiniUserID());
        Assertions.assertEquals(friendRequestList.get(2).getFromUserID(),getMiniUserID()+1);

        FriendRequest friendRequest1 = new FriendRequest(getMiniUserID(),getMiniUserID()+1,
                InvitationStage.REJECTED, LocalDateTime.now());
        Assertions.assertEquals(friendRequest1.getFromUserID(),
                getFriendRequestRepository().save(friendRequest1).get().getFromUserID());
    }

    @Test
    void testFindFriendRequest(){
        saveSomeFriendRequests();
        Optional<FriendRequest> friendRequest = getFriendRequestRepository().find(
                new UnorderedPair<>(getMiniUserID(),getMiniUserID()+1));
        System.out.println(friendRequest);
        Assertions.assertEquals(friendRequest.get().getFromUserID(),getMiniUserID());

        Optional<FriendRequest> friendRequest2 = getFriendRequestRepository().find(
                new UnorderedPair<>(getMiniUserID()+1,getMiniUserID()+2));
        Assertions.assertEquals(friendRequest2.get().getInvitationStage(),InvitationStage.REJECTED);

        Assertions.assertEquals(Optional.empty(),getFriendRequestRepository().find(
                new UnorderedPair<>(getMiniUserID()+5,getMiniUserID()+2)));
    }

    @Test
    void testUpdateFriendRequest(){
        saveSomeFriendRequests();
        FriendRequest PA = new FriendRequest(getMiniUserID(),getMiniUserID()+1,
                InvitationStage.APPROVED, LocalDateTime.now());
        getFriendRequestRepository().update(PA);
        Assertions.assertEquals(getFriendRequestRepository().
                find(new UnorderedPair<>(getMiniUserID(),getMiniUserID()+1)).get().getInvitationStage(),
                InvitationStage.APPROVED);

        FriendRequest FRError= new FriendRequest(getMiniUserID()+5,getMiniUserID()+1,
                InvitationStage.APPROVED, LocalDateTime.now());
        Assertions.assertEquals(Optional.empty(),getFriendRequestRepository().update(FRError));
    }

    @Test
    void testRemoveFriendRequest(){
        FriendRequest friendRequest1 = new FriendRequest(getMiniUserID(),getMiniUserID()+1,
                InvitationStage.PENDING, LocalDateTime.now());
        getFriendRequestRepository().save(friendRequest1);

        Assertions.assertEquals(Optional.of(friendRequest1).get().getFromUserID(),
                getFriendRequestRepository().remove(new UnorderedPair<>(getMiniUserID(),getMiniUserID()+1))
                        .get().getFromUserID());

        Assertions.assertEquals(Optional.empty(),
                getFriendRequestRepository().remove(new UnorderedPair<>(getMiniUserID(),getMiniUserID()+1)));

    }

}
