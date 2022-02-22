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
import socialNetwork.domain.validators.FriendshipValidator;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.exceptions.InvalidEntityException;
import socialNetwork.repository.csv.FriendshipCsvFileRepository;
import socialNetwork.repository.csv.UserCsvFileRepository;
import socialNetwork.repository.database.FriendRequestDatabaseRepository;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.service.NetworkService;
import socialNetwork.utilitaries.UndirectedGraph;
import socialNetwork.utilitaries.UnorderedPair;


import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class NetworkServiceCrudTest {
    private String url = ApplicationContext.getProperty("network.database.url");
    private String user = ApplicationContext.getProperty("network.database.user");
    private String password = ApplicationContext.getProperty("network.database.password");
    PagingRepository<Long, User> userTestRepository = new UserDatabaseRepository(url, user, password);;
    PagingRepository<UnorderedPair<Long, Long>, Friendship> friendshipTestRepository
            = new FriendshipDatabaseRepository(url, user, password);
    PagingRepository<UnorderedPair<Long, Long>, FriendRequest> friendRequestTestRepository
            = new FriendRequestDatabaseRepository(url, user, password);;

    NetworkService testService = new NetworkService(friendshipTestRepository,friendRequestTestRepository,
            userTestRepository,
            new FriendshipValidator(userTestRepository));

    public List<User> getUserData() {
        return new ArrayList<>(Arrays.asList(
                new User("Baltazar","Baltazar","z1"),
                new User("Bradley","Bradley","z2"),
                new User("Frank","Frank","z3"),
                new User("Johnny","John","z4"),
                new User("Johnny","John","z5")
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
    void addExistingFriendship(){
        Assertions.assertTrue(testService.addFriendshipService(getMinimumId(), getMinimumId() + 1, LocalDateTime.now()).isPresent());
    }

    @Test
    void addNewFriendship(){
        Assertions.assertTrue(testService.addFriendshipService(getMaximumId() - 1, getMaximumId(), LocalDateTime.now()).isEmpty());
    }

    @Test
    void addWithNonExistingUsers(){
        Assertions.assertThrows(InvalidEntityException.class,
                ()->testService.addFriendshipService(getMaximumId() + 1, getMaximumId() + 2, LocalDateTime.now()));
    }

    @Test
    void removeWithNonExistingFriendship(){
        Assertions.assertTrue(testService.removeFriendshipService(getMaximumId() + 2, getMaximumId() + 3).isEmpty());
    }

    @Test
    void removeWithExistingFriendship(){
        Assertions.assertTrue(testService.removeFriendshipService(getMinimumId(), getMinimumId() + 1).isPresent());
    }

    @Test
    void getUsersWithAllFriends(){
        UndirectedGraph<User> graph = new UndirectedGraph<>(getUserData());

        for(Friendship friendship : getFriendshipData()) {
            User user1 = userTestRepository.find(friendship.getId().left).get();
            User user2 = userTestRepository.find(friendship.getId().right).get();
            graph.addEdge(user1, user2);
        }

        for(User user : testService.getAllUsersAndTheirFriendsService()){
            Set<User> expectedFriends = graph.getNeighboursOf(user);
            Assertions.assertEquals(expectedFriends.size(), user.getListOfFriends().size());
            Assertions.assertTrue(expectedFriends.containsAll(user.getListOfFriends()));
        }
    }
}
