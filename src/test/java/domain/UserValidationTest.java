package domain;

import socialNetwork.domain.models.User;
import socialNetwork.domain.validators.UserValidator;
import socialNetwork.exceptions.InvalidEntityException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    UserValidator userValidator = new UserValidator();
    String invalidId = "Id cannot be negative!\n";
    String invalidFirstName = "First name cannot be empty!\n";
    String invalidLastName = "Last name cannot be empty!\n";
    String allFieldsInvalid = invalidId.concat(invalidFirstName).concat(invalidLastName);

    @Test
    void allFieldsAreValid(){
        User exampleUser = new User(10L, "Baltazar", "Baltazar","c1");
        userValidator.validate(exampleUser);
    }

    @Test
    void invalidIdTest(){
        User exampleUser = new User(-10L, "Baltazar", "Baltazar","c2");
        assertThrows(InvalidEntityException.class, ()-> userValidator.validate(exampleUser), invalidId);
    }

    @Test
    void invalidFirstNameTest(){
        User exampleUser = new User(10L, "", "Baltazar","c3");
        assertThrows(InvalidEntityException.class, ()-> userValidator.validate(exampleUser), invalidFirstName);
    }

    @Test
    void invalidLastNameTest(){
        User exampleUser = new User(10L, "Baltazar", "","c4");
        assertThrows(InvalidEntityException.class, ()-> userValidator.validate(exampleUser), invalidLastName);
    }

    @Test
    void allFieldsInvalidTest(){
        User exampleUser = new User(-10L, "","","c5");
        assertThrows(InvalidEntityException.class, ()-> userValidator.validate(exampleUser), allFieldsInvalid);
    }
}