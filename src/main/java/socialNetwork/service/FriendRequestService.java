package socialNetwork.service;

import socialNetwork.domain.models.FriendRequest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.InvitationStage;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.exceptions.InvitationStatusException;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.PageableImplementation;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.UnorderedPair;
import socialNetwork.utilitaries.events.Event;
import socialNetwork.utilitaries.events.FriendRequestChangeEvent;
import socialNetwork.utilitaries.events.FriendRequestChangeEventType;
import socialNetwork.utilitaries.observer.Observable;
import socialNetwork.utilitaries.observer.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FriendRequestService implements Observable<Event> {

    PagingRepository<UnorderedPair<Long,Long>, FriendRequest> friendRequestRepository;
    PagingRepository<UnorderedPair<Long,Long>, Friendship> friendshipRepository;
    EntityValidatorInterface<UnorderedPair<Long, Long>, FriendRequest> friendRequestValidator;
    private List< Observer<Event> > observersFriendRequest = new ArrayList<>();

    public FriendRequestService(PagingRepository<UnorderedPair<Long, Long>, FriendRequest> friendRequestRepository,
                                PagingRepository<UnorderedPair<Long, Long>, Friendship> friendshipRepository,
                                EntityValidatorInterface<UnorderedPair<Long, Long>, FriendRequest> friendRequestValidator) {
        this.friendRequestRepository = friendRequestRepository;
        this.friendshipRepository = friendshipRepository;
        this.friendRequestValidator = friendRequestValidator;
    }

    public Optional<FriendRequest> find(Long firstUserId, Long secondUserId){
        UnorderedPair<Long,Long> idEntity = new UnorderedPair<>(firstUserId,secondUserId);
        return friendRequestRepository.find(idEntity);
    }

    public List<FriendRequest> getAllFriendRequestForSpecifiedUserService(Long idUser){
        Predicate<FriendRequest> userInFriendRequest = friendRequest -> {
            return friendRequest.getFromUserID().equals(idUser) ||
                    friendRequest.getToUserID().equals(idUser);
        };
        return friendRequestRepository.getAll()
                .stream()
                .filter(userInFriendRequest)
                .toList();
    }

    public Optional<FriendRequest> withdrawFriendRequestService(Long userIdThatSendInvitationButWithdrawIt,
                                                                Long userIdThatReceiveInvitation){
        UnorderedPair<Long,Long> idEntity = new UnorderedPair<>(userIdThatSendInvitationButWithdrawIt,userIdThatReceiveInvitation);
        Optional<FriendRequest> withdrawalFriendRequest = friendRequestRepository.find(idEntity);
        if(withdrawalFriendRequest.isEmpty())
            return Optional.empty();
        FriendRequest friendRequestThatHasToBeWithdraw = withdrawalFriendRequest.get();
        if(!friendRequestThatHasToBeWithdraw.getFromUserID().equals(userIdThatSendInvitationButWithdrawIt))
            throw new InvitationStatusException("The invitation can be withdrawn only by the one who sent it");
        if(!friendRequestThatHasToBeWithdraw.getInvitationStage().equals(InvitationStage.PENDING))
            throw new InvitationStatusException("The invitation cannot be withdrawn!It has to be a pending one");
        Optional<FriendRequest> removedFriendRequest = friendRequestRepository.remove(idEntity);
        notifyObservers(new FriendRequestChangeEvent(FriendRequestChangeEventType.WITHDRAW, removedFriendRequest.get()));
        return removedFriendRequest;
    }

    /**
     * one user(firstUserID) sends an invitation to another(secondUserID)
     * exception thrown if invitation already exists
     */
    public Optional<FriendRequest> sendInvitationForFriendRequestService(Long userIdThatSendInvitation,
                                                                         Long userIdThatReceiveInvitation){
        Optional<FriendRequest> optionalFriendRequestBetweenUsers =
                searchForFriendRequestInRepository(userIdThatSendInvitation, userIdThatReceiveInvitation);
        if(optionalFriendRequestBetweenUsers.isEmpty())
            return addPendingInvitation(userIdThatSendInvitation, userIdThatReceiveInvitation);
        FriendRequest friendRequestBetweenUsers = optionalFriendRequestBetweenUsers.get();;
        throwExceptionValidateInvitationStatusWhenSendingInvitation(friendRequestBetweenUsers);
        return optionalFriendRequestBetweenUsers;
    }

    /**
     * sets invitation stage of a friendship to approved
     * exception thrown if there is no invitation from one user to another
     * exception thrown if invitation has already been refused
     */
    public Optional<Friendship> updateApprovedFriendRequestService(Long userThatReceivesInvitationAndAcceptedId,
                                                                   Long userThatSendInvitationAndWaitVerdictId){
        Optional<FriendRequest> optionalFriendRequestBetweenUsers = searchForFriendRequestInRepository(
                userThatSendInvitationAndWaitVerdictId , userThatReceivesInvitationAndAcceptedId);
        if(optionalFriendRequestBetweenUsers.isEmpty())
            throw new EntityMissingValidationException("There is no pending invitation between users");
        FriendRequest friendRequest = optionalFriendRequestBetweenUsers.get();
        if(!friendRequest.getToUserID().equals(userThatReceivesInvitationAndAcceptedId))
            throw new InvitationStatusException("You cannot accept an invitation that you send it");
        throwExceptionIfInvitationIsRejected(friendRequest);
        throwExceptionIfInvitationIsApproved(friendRequest);
        return setInvitationStatusToApproved(friendRequest);
    }

    /**
     * sets invitation stage of a friendship to rejected
     * exception thrown if there is no invitation from one user to another
     */
    public Optional<Friendship> updateRejectedFriendRequestService(Long userThatReceivesInvitationAndRejectedId,
                                                                   Long userThatSendInvitationAndWaitVerdictId){
        Optional<FriendRequest> optionalFriendRequestBetweenUsers = searchForFriendRequestInRepository(
                userThatSendInvitationAndWaitVerdictId , userThatReceivesInvitationAndRejectedId);
        if(optionalFriendRequestBetweenUsers.isEmpty())
            throw new EntityMissingValidationException("Friend request between users doesn't exist");
        FriendRequest friendRequestBetweenUsers = optionalFriendRequestBetweenUsers.get();
        if(!friendRequestBetweenUsers.getToUserID().equals(userThatReceivesInvitationAndRejectedId))
            throw new InvitationStatusException("You cannot reject an invitation that you send it");
        return setInvitationStatusToRejected(friendRequestBetweenUsers);
    }

    public Optional<FriendRequest> updateRejectedToPendingFriendRequestService(Long idUserThatRejectButChangeHisMind,
                                                                               Long idUserThatSendInitiallyInvitation){

        Optional<FriendRequest> optionalFriendRequestBetweenUsers = searchForFriendRequestInRepository(
                idUserThatSendInitiallyInvitation, idUserThatRejectButChangeHisMind);
        if(optionalFriendRequestBetweenUsers.isEmpty())
            throw new EntityMissingValidationException("Friendship between users doesn't exist");
        FriendRequest friendRequestBetweenUsers = optionalFriendRequestBetweenUsers.get();
        if(!friendRequestBetweenUsers.getToUserID().equals(idUserThatRejectButChangeHisMind))
            throw new InvitationStatusException("Cannot resubmit your sending invitation");
        if(!friendRequestBetweenUsers.getInvitationStage().equals(InvitationStage.REJECTED))
            throw new InvitationStatusException("Cannot resubmit an invitation that is not rejected");
        friendRequestRepository.remove(friendRequestBetweenUsers.getId());
        return sendInvitationForFriendRequestService(idUserThatRejectButChangeHisMind,
                idUserThatSendInitiallyInvitation);
    }

    private Optional<Friendship> setInvitationStatusToRejected(FriendRequest friendRequest){
        friendRequest.setInvitationStage(InvitationStage.REJECTED);
        friendRequest.setDateRequest(LocalDateTime.now());
        friendRequestRepository.update(friendRequest);

        Optional<Friendship> deletedFriendship = Optional.empty();
        UnorderedPair<Long,Long> idFriendshipRemove = new UnorderedPair<>(friendRequest.getFromUserID(),
                friendRequest.getToUserID());
        if(friendshipRepository.find(idFriendshipRemove).isPresent())
            deletedFriendship = friendshipRepository.remove(idFriendshipRemove);
        notifyObservers(new FriendRequestChangeEvent(FriendRequestChangeEventType.REJECTED,
                friendRequest));
        return deletedFriendship;
    }

    private Optional<Friendship> setInvitationStatusToApproved(FriendRequest friendRequest){
        friendRequest.setInvitationStage(InvitationStage.APPROVED);
        friendRequest.setDateRequest(LocalDateTime.now());
        friendRequestRepository.update(friendRequest);
        Friendship friendship = new Friendship(friendRequest.getFromUserID(),friendRequest.getToUserID(),
                friendRequest.getDateRequest());
        friendshipRepository.save(friendship);
        notifyObservers(new FriendRequestChangeEvent(FriendRequestChangeEventType.APPROVED,
                friendRequest));
        return Optional.of(friendship);
    }

    private Optional<FriendRequest> searchForFriendRequestInRepository(Long firstUserId,Long secondUserId){
        UnorderedPair<Long, Long> idNewFriendship = new UnorderedPair<>(firstUserId, secondUserId);
        return friendRequestRepository.find(idNewFriendship);
    }

    private Optional<FriendRequest> addPendingInvitation(Long userIdThatSendInvitation,
                                                         Long userIdThatReceiveInvitation){
        FriendRequest pendingFriendRequest = new FriendRequest(
                userIdThatSendInvitation,userIdThatReceiveInvitation, InvitationStage.PENDING, LocalDateTime.now());
        friendRequestValidator.validate(pendingFriendRequest);
        friendRequestRepository.save(pendingFriendRequest);
        notifyObservers(new FriendRequestChangeEvent(FriendRequestChangeEventType.PENDING
                ,pendingFriendRequest));
        return Optional.of(pendingFriendRequest);
    }

    private void throwExceptionValidateInvitationStatusWhenSendingInvitation(FriendRequest friendRequest) {
        if( friendRequest.getInvitationStage().equals(InvitationStage.APPROVED) )
            throw new InvitationStatusException("The invitation is already accepted");
        if( friendRequest.getInvitationStage().equals(InvitationStage.PENDING) )
            throw new InvitationStatusException("The invitation is already pending");
        throw new InvitationStatusException("The invitation is rejected");
    }

    private void throwExceptionIfInvitationIsRejected(FriendRequest friendRequest) throws InvitationStatusException{
        if(friendRequest.getInvitationStage().equals(InvitationStage.REJECTED))
            throw new InvitationStatusException("Invitation has already been rejected");
    }

    private void throwExceptionIfInvitationIsApproved(FriendRequest friendRequest) throws InvitationStatusException{
        if(friendRequest.getInvitationStage().equals(InvitationStage.APPROVED))
            throw new InvitationStatusException("Invitation has already been approved");
    }

    @Override
    public void addObserver(Observer<Event> observer) {
        observersFriendRequest.add(observer);
    }

    @Override
    public void removeObserver(Observer<Event> observer) {
        observersFriendRequest.remove(observer);
    }

    @Override
    public void notifyObservers(Event event) {
        observersFriendRequest.forEach(obs -> obs.update(event));
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

    public Set<FriendRequest> getNextFriendRequests(){
        this.pageNumber++;
        return getFriendRequestsOnPage(this.pageNumber);
    }

    public Set<FriendRequest> getFriendRequestsOnPage(int pageNumber){
        this.pageNumber = pageNumber;
        Pageable pageable = new PageableImplementation(pageNumber,this.pageSize);
        Page<FriendRequest> friendRequestPage = friendRequestRepository.getAll(pageable);
        return friendRequestPage.getContent().collect(Collectors.toSet());
    }

}
