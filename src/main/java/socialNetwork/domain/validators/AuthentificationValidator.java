package socialNetwork.domain.validators;

import socialNetwork.domain.models.Autentification;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.InvalidEntityException;

public class AuthentificationValidator implements EntityValidatorInterface<String, Autentification>{
    @Override
    public void validate(Autentification entity) {
        String errorValidationMessage = "";
        if(entity.getUsername().equals(""))
            errorValidationMessage = errorValidationMessage.concat("Username cannot be empty!\n");
        if(entity.getPassword().equals(""))
            errorValidationMessage = errorValidationMessage.concat("Password cannot be empty!\n");
        if(!errorValidationMessage.equals(""))
            throw new InvalidEntityException(errorValidationMessage);
    }
}
