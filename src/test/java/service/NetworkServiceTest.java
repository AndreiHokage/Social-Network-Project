package service;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import socialNetwork.domain.models.FriendRequest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.FriendshipValidator;
import socialNetwork.repository.csv.FriendshipCsvFileRepository;
import socialNetwork.repository.csv.UserCsvFileRepository;
import socialNetwork.repository.database.FriendRequestDatabaseRepository;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.service.NetworkService;
import socialNetwork.utilitaries.UnorderedPair;

import java.util.HashMap;
import java.util.Map;

class NetworkServiceTest {

    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");

    NetworkService createNetworkService(String usersFilePath, String friendshipsFilePath){
        PagingRepository<Long, User> userRepository = new UserCsvFileRepository(usersFilePath);

        var friendshipValidator = new FriendshipValidator(userRepository);
        PagingRepository<UnorderedPair<Long, Long>, Friendship> friendshipRepository =
                new FriendshipCsvFileRepository(friendshipsFilePath);
        PagingRepository<UnorderedPair<Long, Long>, FriendRequest> friendRequestRepository =
                new FriendRequestDatabaseRepository(url,user,password);

        return new NetworkService(friendshipRepository, friendRequestRepository,
                userRepository, friendshipValidator);
    }

    @Test
    void networkContainsOneCommunity(){
        NetworkService testService = createNetworkService(
                ApplicationContext.getProperty("network.users.one_community"),
                ApplicationContext.getProperty("network.friendships.one_community")
        );
        Assertions.assertEquals(1, testService.getNumberOfCommunitiesService());
    }

    @Test
    void networkContainsMultipleCommunities(){
        NetworkService testService = createNetworkService(
                ApplicationContext.getProperty("network.users.multiple_communities"),
                ApplicationContext.getProperty("network.friendships.multiple_communities")
        );
        Assertions.assertEquals(5, testService.getNumberOfCommunitiesService());
    }

    @Test
    void getAllUsersWithTheirFriendsTest(){
        var testService = createNetworkService(
                ApplicationContext.getProperty("network.users.one_community"),
                ApplicationContext.getProperty("network.friendships.one_community")
        );

        var users = testService.getAllUsersAndTheirFriendsService();

        Assertions.assertEquals(6, users.size());

        Map<Long, User> expectedUsers = new HashMap<>();

        for(long i = 1; i <= 6; i++)
            expectedUsers.put(i, new User(i, "" + i, "" + i, "" + i));

        for(User user : users) {
            var friends = user.getListOfFriends();
            if (user.getId() == 1) {
                Assertions.assertEquals(3, friends.size());
                Assertions.assertTrue(friends.contains(expectedUsers.get(2L)));
                Assertions.assertTrue(friends.contains(expectedUsers.get(3L)));
                Assertions.assertTrue(friends.contains(expectedUsers.get(4L)));
            } else if(user.getId() ==  2){
                Assertions.assertEquals(3, friends.size());
                Assertions.assertTrue(friends.contains(expectedUsers.get(1L)));
                Assertions.assertTrue(friends.contains(expectedUsers.get(3L)));
                Assertions.assertTrue(friends.contains(expectedUsers.get(4L)));
            } else if(user.getId() == 3){
                Assertions.assertEquals(2, friends.size());
                Assertions.assertTrue(friends.contains(expectedUsers.get(1L)));
                Assertions.assertTrue(friends.contains(expectedUsers.get(2L)));
            } else if(user.getId() == 4){
                Assertions.assertEquals(3, friends.size());
                Assertions.assertTrue(friends.contains(expectedUsers.get(1L)));
                Assertions.assertTrue(friends.contains(expectedUsers.get(2L)));
                Assertions.assertTrue(friends.contains(expectedUsers.get(5L)));
            } else if(user.getId() == 5){
                Assertions.assertEquals(2, friends.size());
                Assertions.assertTrue(friends.contains(expectedUsers.get(4L)));
                Assertions.assertTrue(friends.contains(expectedUsers.get(6L)));
            } else if(user.getId() == 6){
                Assertions.assertEquals(1, friends.size());
                Assertions.assertTrue(friends.contains(expectedUsers.get(5L)));
            } else assert false;
        }
    }

    @Test
    void testMostSocialCommunity(){
        var testService = createNetworkService(ApplicationContext.getProperty("network.users.most_social"),
                ApplicationContext.getProperty("network.friendships.most_social"));

        var mostSocialCommunity = testService.getMostSocialCommunity();

        Assertions.assertEquals(10, mostSocialCommunity.size());


        for(long i = 9; i <= 18; i++)
            mostSocialCommunity.contains(new User(i, "" + i, "" + i, "" + i));
    }
}
