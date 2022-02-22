package service;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.FriendRequest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.InvitationStage;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.domain.validators.FriendRequestValidator;
import socialNetwork.domain.validators.FriendshipValidator;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.exceptions.InvalidEntityException;
import socialNetwork.exceptions.InvitationStatusException;
import socialNetwork.repository.database.FriendRequestDatabaseRepository;
import socialNetwork.repository.database.FriendshipDatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.FriendRequestService;
import socialNetwork.service.NetworkService;
import socialNetwork.utilitaries.UnorderedPair;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InvitationServiceTest {
    private final String url = ApplicationContext.getProperty("network.database.url");
    private final String user = ApplicationContext.getProperty("network.database.user");
    private final String password = ApplicationContext.getProperty("network.database.password");

    UserDatabaseRepository testUserRepository;
    FriendshipDatabaseRepository testFriendshipsRepository;
    EntityValidatorInterface<UnorderedPair<Long, Long>, Friendship> friendshipValidator;
    NetworkService testNetworkService;
    FriendRequestDatabaseRepository testFriendRequestRepository;
    EntityValidatorInterface<UnorderedPair<Long, Long>, FriendRequest> friendRequestValidator;
    FriendRequestService friendRequestService;

    private FriendRequestService getService() {
        if(testNetworkService == null) {
            testUserRepository = new UserDatabaseRepository(url, user, password);
            testFriendshipsRepository = new FriendshipDatabaseRepository(url, user, password);
            friendshipValidator = new FriendshipValidator(testUserRepository);
            testFriendRequestRepository = new FriendRequestDatabaseRepository(url,user,password);
            testNetworkService = new NetworkService(testFriendshipsRepository,testFriendRequestRepository,
                    testUserRepository, friendshipValidator);
            friendRequestValidator = new FriendRequestValidator(testUserRepository);
            friendRequestService = new FriendRequestService(testFriendRequestRepository,testFriendshipsRepository,
                    friendRequestValidator);
        }
        return friendRequestService;
    }

    public Long getMaximumId(){
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String findMaximumString = "select max(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMaximumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    public Long getMinimumId(){

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String findMinimumString = "select min(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMinimumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return  resultSet.getLong(1);
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    public List<User> getTestData() {
        return new ArrayList<>(Arrays.asList(
                new User("Baltazar","Baltazar","f1"),
                new User( "Bradley","Bradley","f2"),
                new User("Frank","Frank","f3"),
                new User("Johnny","John","f4"),
                new User( "Johnny","John","f5")
        ));
    }

    public void tearDown(){
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            var deleteUsersStatement =
                    connection.prepareStatement("DELETE FROM users");
            deleteUsersStatement.executeUpdate();
            var deleteFriendshipsStatement =
                    connection.prepareStatement("DELETE FROM friendships");
            deleteFriendshipsStatement.executeUpdate();
            var deleteFriendRequestStatement =
                    connection.prepareStatement("DELETE FROM friendrequests");
            deleteFriendRequestStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @BeforeEach
    public void setUp(){
        tearDown();

        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            String insertStatementString = "INSERT INTO users( first_name, last_name,username) VALUES (?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertStatementString);

            for(User user : getTestData()){
                insertStatement.setString(1, user.getFirstName());
                insertStatement.setString(2, user.getLastName());
                insertStatement.setString(3, user.getUsername());
                insertStatement.executeUpdate();
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Test
    void sendInvitationTest(){
        getService().sendInvitationForFriendRequestService(getMinimumId(),getMinimumId() + 1);
        getService().updateApprovedFriendRequestService(getMinimumId() + 1,getMinimumId()); // prieten 1->2

        //------------------------------------getMinimumId() + 1     getMinimumId() + 2 -----------------
        getService().sendInvitationForFriendRequestService(getMinimumId() + 1, getMinimumId() + 2);
        Optional<Friendship> findFriendshipPendingStatusOptional =
                testNetworkService.findFriendshipService(getMinimumId() + 1, getMinimumId() + 2);
        Assertions.assertTrue(findFriendshipPendingStatusOptional.isEmpty());
        FriendRequest findFriendshipPendingStatus = getService().find(getMinimumId()+1,getMinimumId()+2).get();
        Assertions.assertEquals(findFriendshipPendingStatus.getInvitationStage(),
                InvitationStage.PENDING
        );
        Assertions.assertThrows(InvitationStatusException.class,
                () -> getService().sendInvitationForFriendRequestService(getMinimumId() + 1, getMinimumId() + 2),
                "Invitation already pending when sending invitation");
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().sendInvitationForFriendRequestService(getMinimumId(), getMaximumId() + 5),
                "First user not found when sending invitation");
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().sendInvitationForFriendRequestService(getMaximumId() + 5, getMinimumId()),
                "Second user not found when sending invitation");
        Assertions.assertThrows(InvalidEntityException.class,
                () -> getService().sendInvitationForFriendRequestService(getMaximumId() + 5, getMaximumId() + 5),
                "Both users not found when sending invitation");
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().sendInvitationForFriendRequestService(getMinimumId() + 1, getMinimumId() + 2),
                "The user that send invitation accept his own invitation");
        getService().updateApprovedFriendRequestService(getMinimumId() + 2, getMinimumId() + 1);
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().sendInvitationForFriendRequestService(getMinimumId() + 1, getMinimumId() + 2),
                "Sending invitation that exists and approved");
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().updateRejectedFriendRequestService(getMinimumId() + 1, getMinimumId() + 2),
                "The user that send invitation reject it");
        getService().updateRejectedFriendRequestService(getMinimumId() + 2, getMinimumId() + 1);
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().sendInvitationForFriendRequestService(getMinimumId() + 1, getMinimumId() + 2),
                "Sending invitation that exists and rejected");
       // Assertions.assertFalse(Optional.empty(),testNetworkService.findFriendshipService(getMinimumId() + 1,getMinimumId() + 2));
    }

    @Test
    void rejectInvitationTest(){
        Assertions.assertThrows(EntityMissingValidationException.class,
                ()-> getService().updateRejectedFriendRequestService(getMinimumId(), getMinimumId() + 1),
                "Reject ");

        getService().sendInvitationForFriendRequestService(getMaximumId() - 1, getMaximumId());
        var friendshipPendingOptional = testNetworkService.findFriendshipService(getMaximumId() - 1, getMaximumId());
        Assertions.assertTrue(friendshipPendingOptional.isEmpty());
        var friendshipPending = getService().find(getMaximumId()-1,getMaximumId()).get();
        Assertions.assertEquals(friendshipPending.getInvitationStage(),
                InvitationStage.PENDING);
        getService().updateRejectedFriendRequestService(getMaximumId() , getMaximumId() - 1);
        var friendshipRejectedOptional = testNetworkService.findFriendshipService(getMaximumId() - 1, getMaximumId());
        Assertions.assertTrue(friendshipRejectedOptional.isEmpty());
        var friendshipRejected = getService().find(getMaximumId()-1,getMaximumId()).get();
        Assertions.assertEquals(friendshipRejected.getInvitationStage(),
                InvitationStage.REJECTED);
        getService().updateRejectedFriendRequestService(getMaximumId() , getMaximumId() - 1);
        var friendshipRejectedAgain = getService().find(getMaximumId() - 1, getMaximumId()).get();
        Assertions.assertEquals(friendshipRejectedAgain.getInvitationStage(),
                InvitationStage.REJECTED);

        getService().sendInvitationForFriendRequestService(getMinimumId(), getMinimumId() + 2);
        getService().updateApprovedFriendRequestService(getMinimumId() + 2, getMinimumId());
        var getNewOptionalFriendship = testNetworkService.findFriendshipService(getMinimumId() + 2, getMinimumId() );
        Assertions.assertFalse(getNewOptionalFriendship.isEmpty());
        var getNewFriendship = getService().find(getMinimumId(), getMinimumId() + 2).get();
        Assertions.assertEquals(getNewFriendship.getInvitationStage(),
                InvitationStage.APPROVED);
        getService().updateRejectedFriendRequestService(getMinimumId() + 2, getMinimumId() );
        var checkIfFriendshipRejected = getService().find(getMinimumId(), getMinimumId() + 2).get();
        Assertions.assertEquals(checkIfFriendshipRejected.getInvitationStage(),
                InvitationStage.REJECTED);
    }

    @Test
    void approveInvitationTest(){
        getService().sendInvitationForFriendRequestService(getMinimumId() + 1, getMaximumId() - 1);

        var optionalSentInvitation = getService().find(getMinimumId() + 1, getMaximumId() - 1);
        Assertions.assertFalse(optionalSentInvitation.isEmpty());
        var sentInvitation = optionalSentInvitation.get();
        Assertions.assertEquals(sentInvitation.getInvitationStage(),
                InvitationStage.PENDING);
        getService().updateApprovedFriendRequestService(getMaximumId() - 1, getMinimumId() + 1);
        var checkIfInvitationApproved = getService().find(getMaximumId() - 1, getMinimumId() + 1).get();
        Assertions.assertEquals(checkIfInvitationApproved.getInvitationStage(),
                InvitationStage.APPROVED);
        getService().updateRejectedFriendRequestService(getMaximumId() - 1, getMinimumId() + 1);
        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().updateApprovedFriendRequestService(getMaximumId() - 1, getMinimumId() + 1),
                "Approving an invitation already rejected");


        getService().sendInvitationForFriendRequestService(getMinimumId() , getMaximumId() );
        getService().updateApprovedFriendRequestService(getMaximumId() , getMinimumId() );

        Assertions.assertThrows(InvitationStatusException.class,
                ()-> getService().updateApprovedFriendRequestService(getMinimumId() , getMaximumId() ),
                "Approving an invitation already approved");
        Assertions.assertEquals(getService().find(getMinimumId(), getMaximumId()).get().getInvitationStage(),
                InvitationStage.APPROVED);
        Assertions.assertThrows(EntityMissingValidationException.class,
                ()-> getService().updateApprovedFriendRequestService(getMinimumId(), getMinimumId() + 1),
                "Approving a not existing invitation");
    }

    @Test
    void testRelationBetweenFriendshipsAndFriendRequest(){
        getService().sendInvitationForFriendRequestService(getMinimumId(),getMinimumId()+1); //1->2
        getService().sendInvitationForFriendRequestService(getMinimumId()+3,getMinimumId()+4); //5->6
        getService().sendInvitationForFriendRequestService(getMinimumId()+1,getMinimumId()+2); //3->4

        getService().updateApprovedFriendRequestService(getMinimumId() + 1,getMinimumId());
        getService().updateApprovedFriendRequestService(getMinimumId()+2,getMinimumId()+1);
        Assertions.assertNotEquals(Optional.empty(),
                testNetworkService.findFriendshipService(getMinimumId(),getMinimumId()+1));
        Assertions.assertNotEquals(Optional.empty(),
                testNetworkService.findFriendshipService(getMinimumId()+1,getMinimumId()+2));
        Assertions.assertEquals(Optional.empty(),
                testNetworkService.findFriendshipService(getMinimumId()+3,getMinimumId()+4));

        getService().updateRejectedFriendRequestService(getMinimumId()+2,getMinimumId()+1);
        Assertions.assertEquals(Optional.empty(),
                testNetworkService.findFriendshipService(getMinimumId()+2,getMinimumId()+1));
    }

    @Test
    void testWithdrawInvitation(){
        //PENDING
        getService().sendInvitationForFriendRequestService(getMinimumId() , getMinimumId() + 1);
        //APPROVED
        getService().sendInvitationForFriendRequestService(getMinimumId() , getMinimumId() + 2);
        getService().updateApprovedFriendRequestService(getMinimumId() + 2 , getMinimumId());
        //REJECTED
        getService().sendInvitationForFriendRequestService(getMinimumId() + 1 , getMinimumId() + 2);
        getService().updateRejectedFriendRequestService(getMinimumId() + 2 , getMinimumId() + 1);

        Assertions.assertThrows(InvitationStatusException.class,
                ()->getService().withdrawFriendRequestService(getMinimumId() ,getMinimumId() + 2),
                "Try to withdraw an approved invitation");
        Assertions.assertThrows(InvitationStatusException.class,
                ()->getService().withdrawFriendRequestService(getMinimumId() + 1,getMinimumId() + 2),
                "Try to withdraw a rejected invitation");
        Assertions.assertThrows(InvitationStatusException.class,
                ()->getService().withdrawFriendRequestService(getMinimumId() + 2,getMinimumId() + 1),
                "Try to withdraw a pending invitation by re ceiver");
        Optional<FriendRequest> cancelFriendRequest = getService()
                .withdrawFriendRequestService(getMinimumId(),getMinimumId() + 1);
        Assertions.assertEquals(cancelFriendRequest.get().getInvitationStage(),
                InvitationStage.PENDING);
        Assertions.assertEquals(Optional.empty(),getService()
                .withdrawFriendRequestService(getMinimumId(),getMinimumId() + 1));

    }

}
