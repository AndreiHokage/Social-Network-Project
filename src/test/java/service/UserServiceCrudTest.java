package service;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.database.FriendshipDatabaseTableSetter;
import repository.database.UserDatabaseTableSetter;
import socialNetwork.domain.models.FriendRequest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.UserValidator;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.exceptions.InvalidEntityException;
import socialNetwork.repository.database.FriendRequestDatabaseRepository;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.service.UserService;
import socialNetwork.utilitaries.UnorderedPair;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class UserServiceCrudTest {
    private String url = ApplicationContext.getProperty("network.database.url");
    private String user = ApplicationContext.getProperty("network.database.user");
    private String password = ApplicationContext.getProperty("network.database.password");
    PagingRepository<Long, User> userTestRepository = new UserDatabaseRepository(url, user, password);;
    PagingRepository<UnorderedPair<Long, Long>, Friendship> friendshipTestRepository
            = new FriendshipDatabaseRepository(url, user, password);
    PagingRepository<UnorderedPair<Long, Long>, FriendRequest> friendRequestTestRepository
            = new FriendRequestDatabaseRepository(url, user, password);
    UserService testService = new UserService(userTestRepository, friendshipTestRepository,
            friendRequestTestRepository,new UserValidator());

    public List<User> getUserData() {
        return new ArrayList<>(Arrays.asList(
                new User("Baltazar","Baltazar","t1"),
                new User("Bradley","Bradley","t2"),
                new User("Frank","Frank","t3"),
                new User("Johnny","John","t4"),
                new User("Johnny","John","t5")
        ));
    }

    public Long getMaximumId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMaximumString = "select max(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMaximumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    public Long getMinimumId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMinimumString = "select min(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMinimumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    public List<Friendship> getFriendshipData() {
        return Arrays.asList(
                new Friendship(getMinimumId(),getMinimumId() + 1, LocalDateTime.of(2021,10,20,10,30)),
                new Friendship(getMinimumId(),getMinimumId() + 2, LocalDateTime.of(2021,10,20,10,30)),
                new Friendship(getMinimumId() + 1,getMinimumId() + 2, LocalDateTime.of(2021,10,20,10,30))
        );
    }

    @BeforeEach
    void setUp(){
        FriendshipDatabaseTableSetter.tearDown();
        UserDatabaseTableSetter.tearDown();

        UserDatabaseTableSetter.setUp(getUserData());
        FriendshipDatabaseTableSetter.setUp(getFriendshipData());
    }

    @Test
    void addWithInvalidUser(){
        Assertions.assertThrows(InvalidEntityException.class,

                ()->testService.addUserService("", "",""));

    }

    @Test
    void addWithValidUser(){

        Assertions.assertTrue(testService.addUserService("John", "Snow","ba1").isEmpty());
    }

    @Test
    void addWithExistingUser(){
        Assertions.assertTrue(testService.addUserService("John", "Snow","ba1").isEmpty());
        Assertions.assertTrue(testService.addUserService("John", "Snow","ba1").isPresent());
    }


    @Test
    void removeNonExitingUser(){
        Assertions.assertTrue(testService.removeUserService(getMaximumId() + 1).isEmpty());
    }

    @Test
    void removeExistingUserWithNoFriends(){
        int oldNumberOfFriendships = friendshipTestRepository.getAll().size();
        Assertions.assertTrue(testService.removeUserService(getMaximumId()).isPresent());
        Assertions.assertEquals(oldNumberOfFriendships, friendshipTestRepository.getAll().size());
    }

    @Test
    void removeExistingUserWithFriends(){
        int oldNumberOfFriends = friendshipTestRepository.getAll().size();
        Assertions.assertTrue(testService.removeUserService(getMinimumId()).isPresent());
        Assertions.assertEquals(oldNumberOfFriends - 2, friendshipTestRepository.getAll().size());
    }
}
