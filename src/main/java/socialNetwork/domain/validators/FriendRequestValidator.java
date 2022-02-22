package socialNetwork.domain.validators;

import socialNetwork.domain.models.FriendRequest;
import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.exceptions.InvalidEntityException;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.UnorderedPair;

public class FriendRequestValidator implements
        EntityValidatorInterface<UnorderedPair<Long, Long>, FriendRequest>{

    private PagingRepository<Long, User> userRepository;

    public FriendRequestValidator(PagingRepository<Long, User> userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public void validate(FriendRequest friendRequest) {
        String errorMessage = "";
        Long idFirstUser = friendRequest.getFromUserID();
        Long idSecondUser = friendRequest.getToUserID();
        if(idFirstUser.equals(idSecondUser))
            throw new InvalidEntityException("Id's of friendship must be different!");
        if(!checkIfUserExists(idFirstUser))
            errorMessage += "User with id " + idFirstUser + " does not exist!\n";
        if(!checkIfUserExists(idSecondUser))
            errorMessage += "User with id " + idSecondUser + " does not exist!\n";
        if(errorMessage.length() > 0)
            throw new EntityMissingValidationException(errorMessage);
    }

    /**
     * checks if the user exists in the repository
     * @param idUser - Long - identifier for user
     * @return true if the user exists, false otherwise
     */
    private boolean checkIfUserExists(Long idUser){
        return userRepository.find(idUser).isPresent();
    }
}
