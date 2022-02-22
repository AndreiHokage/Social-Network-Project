package socialNetwork.domain.validators;

import socialNetwork.domain.models.Friendship;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.exceptions.InvalidEntityException;
import socialNetwork.repository.paging.PagingRepository;
import socialNetwork.utilitaries.UnorderedPair;

/**
 * validator for Friendship model
 */
public class FriendshipValidator
        implements EntityValidatorInterface<UnorderedPair<Long, Long>, Friendship> {
    private PagingRepository<Long, User> userRepository;

    /**
     * constructor that creates a new validator that accesses the given user repository for friendship validation
     * @param userRepository - repository of users
     * @throws IllegalArgumentException - userRepository is null
     */
    public FriendshipValidator(PagingRepository<Long, User> userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * validates the existence of the users in the repository (given in the constructor) for the given friendship
     * @param friendshipEntity - Friendship instance that will be validated
     * @throws socialNetwork.exceptions.EntityMissingValidationException - the users of the given repository do not exist
     */
    @Override
    public void validate(Friendship friendshipEntity) {
        String errorMessage = "";
        Long idFirstUser = friendshipEntity.getId().left;
        Long idSecondUser = friendshipEntity.getId().right;
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
