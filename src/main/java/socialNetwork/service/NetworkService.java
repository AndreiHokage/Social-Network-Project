package socialNetwork.service;

import socialNetwork.domain.models.FriendRequest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.InvitationStage;
import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.exceptions.InvitationStatusException;
import socialNetwork.repository.paging.*;
import socialNetwork.utilitaries.UndirectedGraph;
import socialNetwork.utilitaries.UnorderedPair;
import socialNetwork.utilitaries.events.ChangeEventType;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.events.FriendshipChangeEvent;
import socialNetwork.utilitaries.observer.Observable;
import socialNetwork.utilitaries.observer.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * business layer for Friendship model
 */
public class NetworkService implements Observable<Event> {
    private final PagingRepository<UnorderedPair<Long, Long>, Friendship> friendshipRepository;
    private final PagingRepository<UnorderedPair<Long, Long>, FriendRequest> friendRequestRepository;
    private final PagingRepository<Long, User> userRepository;
    private final EntityValidatorInterface<UnorderedPair<Long, Long>, Friendship> friendshipValidator;
    private List< Observer<Event> > observersFriendship = new ArrayList<>();

    public NetworkService(PagingRepository<UnorderedPair<Long, Long>, Friendship> friendshipRepository,
                          PagingRepository<UnorderedPair<Long, Long>, FriendRequest> friendRequestRepository,
                          PagingRepository<Long, User> userRepository,
                          EntityValidatorInterface<UnorderedPair<Long, Long>, Friendship> friendshipValidator) {
        this.friendshipRepository = friendshipRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
        this.friendshipValidator = friendshipValidator;
    }

    public List<User> getAllFriendshipForSpecifiedUserService(Long idUser){
        return friendshipRepository.getAll()
                .stream()
                .filter(friendship -> friendship.hasUser(idUser))
                .map(friendship -> {
                    Long idFriend = friendship.getId().left;
                    if(idFriend.equals(idUser))
                        idFriend = friendship.getId().right;
                    return userRepository.find(idFriend).get();
                })
                .toList();
    }

    public Page<User> getAllFriendshipForSpecifiedUserService(Long idUser,Pageable pageable){
        Paginator<User> paginator = new Paginator<User>(pageable,
                getAllFriendshipForSpecifiedUserService(idUser));
        return paginator.paginate();
    }

    /**
     * adds a new friendship
     * @param firstUserId - Long - id of first user
     * @param secondUserId - Long - id of second user
     * @return friendship Optional if the relationship was added, empty Optional if the relationship exists already
     * @throws socialNetwork.exceptions.InvalidEntityException one of users is not found
     */
    public Optional<Friendship> addFriendshipService(Long firstUserId, Long secondUserId, LocalDateTime date){
        UnorderedPair<Long, Long> idNewFriendship = new UnorderedPair<>(firstUserId, secondUserId);
        Optional<Friendship> existingFriendshipOptional = friendshipRepository.find(idNewFriendship);

        if(existingFriendshipOptional.isEmpty()){
            Friendship friendship = new Friendship(firstUserId, secondUserId, date);
            friendshipValidator.validate(friendship);
            friendshipRepository.save(friendship);
        }
        return existingFriendshipOptional;
    }

    /**
     * removes the friendship between the users with the given id-s(UNFRIEND functionality from real apps)
     * it removes the friendship and the request too
     * @param firstUserId - Long - id of first user
     * @param secondUserId - Long - id of second user
     * @return Optional containing the removed relationship, empty Optional if the users are not friends
     */
    public Optional<Friendship> removeFriendshipService(Long firstUserId, Long secondUserId){
        UnorderedPair<Long, Long> friendshipId = new UnorderedPair<>(firstUserId, secondUserId);
        friendRequestRepository.remove(friendshipId);
        Optional<Friendship> removedFriendship = friendshipRepository.remove(friendshipId);
        if(removedFriendship.isPresent())
            notifyObservers(new FriendshipChangeEvent(ChangeEventType.DELETE,removedFriendship.get()));
        return  removedFriendship;
    }

    /**
     * finds a friendship with the id-s of the 2 friends
     * @param firstUserId - Long
     * @param secondUserId - Long
     * @return Optional containing friendship if exists, empty optional otherwise
     */
    public Optional<Friendship> findFriendshipService(Long firstUserId, Long secondUserId){
        UnorderedPair<Long, Long> friendshipId = new UnorderedPair<>(firstUserId, secondUserId);
        return friendshipRepository.find(friendshipId);
    }

    /**
     * @return int - number of communities in the network
     */
    public int getNumberOfCommunitiesService(){
        UndirectedGraph<Long> graphOfUserNetwork = new UndirectedGraph<>();
        for(User user: userRepository.getAll())
            graphOfUserNetwork.addVertex(user.getId());
        for(Friendship friendship: friendshipRepository.getAll())
                graphOfUserNetwork.addEdge(friendship.getId().left, friendship.getId().right);
        return graphOfUserNetwork.findNumberOfConnectedComponents();
    }

    /**
     * @return a list with all the users from the most sociable community
     */
    public List<User> getMostSocialCommunity(){
        UndirectedGraph<User> graphOfUsers = new UndirectedGraph<>(userRepository.getAll());

        for(Friendship friendship: friendshipRepository.getAll()){
                User user1 = userRepository.find(friendship.getId().left).get();
                User user2 = userRepository.find(friendship.getId().right).get();
                graphOfUsers.addEdge(user1, user2);
            }

        return graphOfUsers.findConnectedComponentWithLongestPath().getVertices();
    }

    /**
     * @return - a list of all users and their friends
     */
    public List<User> getAllUsersAndTheirFriendsService(){
        List<Friendship> allFriendships = friendshipRepository.getAll();
        UndirectedGraph<User> userUndirectedGraph = new UndirectedGraph<>(userRepository.getAll());

        for(Friendship friendship: allFriendships) {
                User user1 = userRepository.find(friendship.getId().left).get();
                User user2 = userRepository.find(friendship.getId().right).get();
                userUndirectedGraph.addEdge(user1, user2);
            }
        List<User> allUsersAndFriends = new ArrayList<User>(userRepository.getAll());
        for(User currentUser: allUsersAndFriends){
            currentUser.setListOfFriends(userUndirectedGraph.getNeighboursOf(currentUser).stream().toList());
        }
        return allUsersAndFriends;
    }

    @Override
    public void addObserver(Observer<Event> observer) {
        observersFriendship.add(observer);
    }

    @Override
    public void removeObserver(Observer<Event> observer) {
        observersFriendship.remove(observer);
    }

    @Override
    public void notifyObservers(Event event) {
        observersFriendship.forEach(obs -> obs.update(event));
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

    public Set<Friendship> getNextFriendships(){
        this.pageNumber++;
        return getFriendshipsOnPage(this.pageNumber);
    }

    public Set<Friendship> getFriendshipsOnPage(int pageNumber){
        this.pageNumber = pageNumber;
        Pageable pageable = new PageableImplementation(pageNumber,this.pageSize);
        Page<Friendship> friendshipsPage = friendshipRepository.getAll(pageable);
        return friendshipsPage.getContent().collect(Collectors.toSet());
    }

    public Set<User> getNextFriendshipsForUser(Long idUser){
        this.pageNumber++;
        return getFriendshipsOnPageForUser(idUser,this.pageNumber);
    }

    public Set<User> getFriendshipsOnPageForUser(Long idUser,int pageNumber){
        this.pageNumber = pageNumber;
        Pageable pageable = new PageableImplementation(pageNumber,this.pageSize);
        Page<User> userFriendshipsPage = getAllFriendshipForSpecifiedUserService(idUser,pageable);
        return userFriendshipsPage.getContent().collect(Collectors.toSet());
    }

}
