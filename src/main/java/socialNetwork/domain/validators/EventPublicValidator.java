package socialNetwork.domain.validators;

import socialNetwork.domain.models.EventPublic;
import socialNetwork.domain.models.User;
import socialNetwork.exceptions.EventPublicException;

public class EventPublicValidator implements EntityValidatorInterface<Long, EventPublic>{
    @Override
    public void validate(EventPublic entity) {
        String errors = "";
        if(entity.getName().equals(""))
            errors += "The name of event cannot be empty!";
        if(entity.getDescription().equals(""))
            errors += "The description of event cannot be empty!";
        if(!errors.equals(""))
            throw  new EventPublicException(errors);
    }
}
