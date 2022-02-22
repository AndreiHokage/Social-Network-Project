package socialNetwork.service;

import socialNetwork.domain.models.*;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.PageableImplementation;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.UnorderedPair;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * business layer for User model
 */
public class UserService {
    private PagingRepository<Long, User> userRepository;
    private PagingRepository<UnorderedPair<Long, Long>, Friendship> friendshipRepository;
    private PagingRepository<UnorderedPair<Long, Long>, FriendRequest> friendRequestRepository;
    private EntityValidatorInterface<Long, User> userValidator;

    /**
     * constructor for user service
     * @param userRepository - repository of users
     * @param friendshipRepository - repository of friendships
     * @param userValidator - validator for User model
     */
    public UserService(PagingRepository<Long, User> userRepository,
                       PagingRepository<UnorderedPair<Long, Long>, Friendship> friendshipRepository,
                       PagingRepository<UnorderedPair<Long, Long>, FriendRequest> friendRequestRepository,
                       EntityValidatorInterface<Long, User> userValidator) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.userValidator = userValidator;
        this.friendRequestRepository = friendRequestRepository;
    }

    /**
     * adds a user to the userRepository
     * @param firstName - String - first name of user
     * @param lastName - String - last name of user
     * @return empty Optional if the user was added, Optional containing the existing user with same id otherwise
     */

    public Optional<User> addUserService( String firstName, String lastName ,String username) {
        User user = new User( firstName, lastName ,username);
        userValidator.validate(user);
        return userRepository.save(user);
    }

    /**
     * updates user from the user repository
     * @param id - identifier
     * @param firstName - String
     * @param lastName - String
     * @return optional with old value if user updated, empty optional otherwise
     */
    public Optional<User> updateUserService(Long id, String firstName, String lastName ,String username){
        User user = new User(id, firstName, lastName, username);
        userValidator.validate(user);
        return userRepository.update(user);
    }

    /**
     * finds a user in the user repository after its identifier
     * @param id - identifier of user that is searched for
     * @return - optional containing user with given id if exists, empty optional otherwise
     */
    public Optional<User> findUserService(Long id){
        return userRepository.find(id);
    }

    /**
     * @return - a list with all users
     * the list of friends for each user is empty
     */
    public List<User> getAllService(){
        return userRepository.getAll();
    }

    /**
     * removes the user with the given id and his friendships with other users
     * @param id - Long - identifier of the user
     * @return - Optional with the user that was removed, empty Optional if user did not exist
     * @throws IllegalArgumentException - id is null
     */
    public Optional<User> removeUserService(Long id){
        if(userRepository.find(id).isEmpty())
            return Optional.empty();
        List<Friendship> friendships = friendshipRepository.getAll();
        friendships.forEach(
                friendship -> {
                    if(friendship.hasUser(id))
                        friendshipRepository.remove(friendship.getId());
                }
        );
        return userRepository.remove(id);
    }

    /**
     * finds all friends for a certain user
     * @param idUser - Long - identifier for one user
     * @return - Map - key is Optional of user, value is LocalDateTime
     */
    public Map<Optional<User>, LocalDateTime> findAllFriendsForUserService(Long idUser){
        Map<Optional<User>, LocalDateTime> mapOfFriendships = new HashMap<>();
        List<Friendship> friendships = friendshipRepository.getAll();
        friendships.stream()
                .filter(friendship -> friendship.hasUser(idUser))
                .forEach(friendship -> {
                    Long idOfFriend;
                    if(friendship.getId().left == idUser)
                        idOfFriend = friendship.getId().right;
                    else
                        idOfFriend = friendship.getId().left;
                    mapOfFriendships.put(userRepository.find(idOfFriend), friendship.getDate());
                });
        return mapOfFriendships;
    }


    public List<FriendshipRequestDTO> findAllRequestFriendsForUserService(Long idUser){
        List<FriendshipRequestDTO> allRequestFriendList = new ArrayList<>();
        List<FriendRequest> friendRequestList = friendRequestRepository.getAll();
        Predicate<FriendRequest> hasID = friendRequest -> friendRequest.getFromUserID().equals(idUser) ||
                friendRequest.getToUserID().equals(idUser);
        friendRequestList.stream()
                .filter(hasID)
                .forEach(friendRequest -> {
                    User userThatSendsRequest = userRepository.find(friendRequest.getFromUserID()).get();
                    User userThatReceivesRequest = userRepository.find(friendRequest.getToUserID()).get();
                    allRequestFriendList.add(
                            new FriendshipRequestDTO(userThatSendsRequest,userThatReceivesRequest,
                                    friendRequest.getDateRequest(),friendRequest.getInvitationStage()));
                });
        return allRequestFriendList;
    }

    public Map<Optional<User>, LocalDateTime > findAllFriendsForUserMonthService(Long idUser,int month){
        Map<Optional<User>, LocalDateTime> friendshipsMonth = new HashMap<>();
        findAllFriendsForUserService(idUser).entrySet()
                .stream()
                .filter(entry -> entry.getValue().getMonth().getValue() == month )
                .forEach(entry -> {
                    friendshipsMonth.put(entry.getKey(),entry.getValue());
                });
        return friendshipsMonth;
    }

    private int pageNumber = 0;
    private int pageSize = 1;

    private Pageable pageable;

    public void setPageSize(int pageSize){
        this.pageSize = pageSize;
    }

    public void setPageable(Pageable pageable){
        this.pageable = pageable;
    }

    public Set<User> getNextUsers(){
        this.pageNumber++;
        return getUsersOnPage(this.pageNumber);
    }

    public Set<User> getUsersOnPage(int pageNumber){
        this.pageNumber = pageNumber;
        Pageable pageable = new PageableImplementation(pageNumber,this.pageSize);
        Page<User> userPage = userRepository.getAll(pageable);
        return userPage.getContent().collect(Collectors.toSet());
    }
}
