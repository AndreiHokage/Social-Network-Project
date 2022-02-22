package socialNetwork.domain.validators;

import socialNetwork.domain.models.User;
import socialNetwork.exceptions.InvalidEntityException;

/**
 * validator for User model
 */
public class UserValidator implements EntityValidatorInterface<Long, User> {

    /**
     * validates the given user
     * @param userEntity - User entity
     * throws {@link InvalidEntityException} if id negative or first name empty or last name empty
     */
    @Override
    public void validate(User userEntity) {
        String errorValidationMessage = "";
        if (userEntity.getFirstName().equals(""))
            errorValidationMessage = errorValidationMessage.concat("First name cannot be empty!\n");
        if (userEntity.getLastName().equals(""))
            errorValidationMessage = errorValidationMessage.concat("Last name cannot be empty!\n");
        if(userEntity.getUsername().equals(""))
            errorValidationMessage = errorValidationMessage.concat("Username cannot be empty!\n");
        if(!errorValidationMessage.equals(""))
            throw new InvalidEntityException(errorValidationMessage);
    }

}
